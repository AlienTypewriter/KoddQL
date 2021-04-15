package parser;

import java.util.ArrayList;
import java.util.HashMap;

import parser.Lexer.Lexeme;
import parser.Lexer.LexemeType;
import parser.Node.DataType;
import parser.Node.NodeType;
import parser.Utilities.IntRef;


public class Syntaxer {
	
	// Натуральне з'єднання
	// customers[]id

	// Внутрішнє з'єднання
	// customers[L.id = R.id]table
	
	// Ліве зовнішнє з'єднання
	// customers>[L.id = R.id]table
	
	// Праве зовнішнє з'єднання
	// customers[L.id = R.id]<table
	
	// Повне зовнішнє з'єднання
	// customers>[L.id = R.id]<table
	
	// Екві-з'єднання
	// customers[id]table
	
	// Тета-з'єднання
	// customers[ordersCount > number]table
	
	// Тета-обмеження
	// customers[credit > 1]
	
	
	// GROUP BY
	// -> RESULT^[name]
	
	// ORDER BY
	// -> RESULT[ > name, < surname]
	
	// CASE
	/* (id > 50 : 'One', id ~ 10 & 50 : 'Two', 'Three')
	 * 
	 * (
	 * 		id > 50 : 'One',
	 * 		id ~ 10 & 50 : 'Two',
	 * 		'Three'
	 * );
	*/
	
	// IIF
	// (id > 50 : 'Then', 'Else')
	
	
	// Procedure
	// table[@arg1, @arg2] -> procedure(@arg1, @arg2)
	
	
	
	
	public static Node syntaxRoot(ArrayList<Lexer.Lexeme> list, KoddQL instance) {
		Node root = null;
		IntRef shift = new IntRef(0);
		
		while(shift.value < list.size()) {
			root = syntax(root, list, shift, instance);
			//if(root.getType() == NodeType.ASSIGN)
			//	System.out.println(root);
			++shift.value;
		}
		
		System.out.println(root);
		System.out.println();
		
		return root;
	}
	
	public static Node syntax(Node previous, ArrayList<Lexer.Lexeme> list, IntRef shift, KoddQL instance) {
		switch(list.get(shift.value).type) {

		case RESULT:
			return new Node.Lexeme(Lexeme.RESULT);
		case DELETE:
			return new Node.Lexeme(Lexeme.DELETE);
		case USE:
			return new Node.Lexeme(Lexeme.USE);
		case CREATE:
			return new Node.Lexeme(Lexeme.CREATE);
		
		case NAME:
			return new Node.Lexeme(list.get(shift.value));
		case VARIABLE:
			String var = list.get(shift.value).toString();
			if(instance.variables.containsKey(var))
				return instance.variables.get(var);
			else {
				instance._errors.add(new Notification(Notification.Type.ERROR_UNDECLARED_VARIABLE, 0, 0, var));
				return new Node.Lexeme(list.get(shift.value));
			}
			
		case SEMICOLON:
			return previous;
			
		case ASSIGN:
			++shift.value;
			
			if(list.get(shift.value).type == LexemeType.VARIABLE) {
				instance.variables.put(list.get(shift.value).toString(), previous);
				return null;
			} else {
				StringBuilder sb = new StringBuilder();
				Node node = syntax(null, list, shift, instance);
				
				node = new Node.Assign(previous, node);
				node.toSQL(sb, instance.dbms);
				instance.queries.add(sb.toString());
				
				return node;
			}
			
		case OP_LESS:
			Node.Column column = new Node.Column();
			
			column.name = list.get(++shift.value).toString();
			
			if(list.get(++shift.value) != Lexeme.OR) {
				instance._errors.add(new Notification(Notification.Type.ERROR_MISSING_SYMBOL,list,list.get(shift.value),"|"));
				return null;
			}
			
			switch(list.get(++shift.value).toString()) {
			case "BOOLEAN":
				column.type = DataType.BOOLEAN;
				column.typeModifier = 0;
				if(list.get(++shift.value) == Lexeme.ROUND_BR_L) {
					instance._errors.add(new Notification(Notification.Type.ERROR_UNEXPECTED_SYMBOL,list,list.get(shift.value),list.get(shift.value).toString()));
					return null;
				}
				break;
			case "BYTE":
				column.type = DataType.INT;
				column.typeModifier = 1;
				if(list.get(++shift.value) == Lexeme.ROUND_BR_L) {
					instance._errors.add(new Notification(Notification.Type.ERROR_UNEXPECTED_SYMBOL,list,list.get(shift.value),list.get(shift.value).toString()));
					return null;
				}
				break;
			case "SHORT":
				column.type = DataType.INT;
				column.typeModifier = 2;
				if(list.get(++shift.value) == Lexeme.ROUND_BR_L) {
					instance._errors.add(new Notification(Notification.Type.ERROR_UNEXPECTED_SYMBOL,list,list.get(shift.value),list.get(shift.value).toString()));
					return null;
				}
				break;
			case "INTEGER":
				column.type = DataType.INT;
				column.typeModifier = 4;
				if(list.get(++shift.value) == Lexeme.ROUND_BR_L) {
					instance._errors.add(new Notification(Notification.Type.ERROR_UNEXPECTED_SYMBOL,list,list.get(shift.value),list.get(shift.value).toString()));
					return null;
				}
				break;
			case "LONG":
				column.type = DataType.INT;
				column.typeModifier = 8;
				if(list.get(++shift.value) == Lexeme.ROUND_BR_L) {
					instance._errors.add(new Notification(Notification.Type.ERROR_UNEXPECTED_SYMBOL,list,list.get(shift.value),list.get(shift.value).toString()));
					return null;
				}
				break;
				
			case "INT":
				column.type = DataType.INT;
				column.typeModifier = 4;
				if(list.get(++shift.value) == Lexeme.ROUND_BR_L) {
					if(list.get(++shift.value).type == LexemeType.INTEGER) {
						column.typeModifier = ((Long)((Lexeme.Data)list.get(shift.value)).data).intValue();
					} else {
						instance._errors.add(new Notification(Notification.Type.ERROR_UNEXPECTED_SYMBOL,list,list.get(shift.value),list.get(shift.value).toString()));
						return null;
					}
					
					if(list.get(++shift.value) != Lexeme.ROUND_BR_R) {
						instance._errors.add(new Notification(Notification.Type.ERROR_MISSING_SYMBOL,list,list.get(shift.value),")"));
						return null;
					}
					++shift.value;
				}
				break;
			}
			
			if(list.get(shift.value) == Lexeme.OR)
				while(list.get(++shift.value) != Lexeme.OP_MORE) {
					switch(list.get(shift.value).type) {
					case UNIQUE:
						column.unique = true; break;
					case OP_NOT:
						if(list.get(++shift.value) == Lexeme.NULL)
							column.notNull = true;
						else {
							instance._errors.add(new Notification(Notification.Type.ERROR_MISSING_SYMBOL,list,list.get(shift.value),"NULL"));
							return null;
						}
						break;
					case PRIMARY:
						if(list.get(++shift.value) == Lexeme.KEY)
							column.primaryKey = true;
						else {
							instance._errors.add(new Notification(Notification.Type.ERROR_MISSING_SYMBOL,list,list.get(shift.value),"KEY"));
							return null;
						}
						break;
					case DEFAULT:
						if(list.get(++shift.value) == Lexeme.ROUND_BR_L)
							if(list.get(shift.value += 2) == Lexeme.ROUND_BR_R)
								column.defaultValue = (String)((Lexeme.Data)list.get(shift.value - 1)).toString();
							else {
								instance._errors.add(new Notification(Notification.Type.ERROR_MISSING_SYMBOL,list,list.get(shift.value),")"));
								return null;
							}
						else {
							instance._errors.add(new Notification(Notification.Type.ERROR_MISSING_SYMBOL,list,list.get(shift.value),"("));
							return null;
						}
						break;
					}
				}
			
			if(list.get(shift.value) != Lexeme.OP_MORE) {
				instance._errors.add(new Notification(Notification.Type.ERROR_MISSING_SYMBOL,list,list.get(shift.value),">"));
				return null;
			}
			
			return column;
			
		case OP_MORE:
			return previous;
			
		case ROUND_BR_L:
			++shift.value;
			if(list.get(shift.value + 1) == Lexeme.COMMA) {
				Node.Group node = new Node.Group();
				
				while(list.get(shift.value + 1) != Lexeme.ROUND_BR_R) {
					if(list.get(shift.value).type != LexemeType.NAME)
						instance._errors.add(new Notification(
								Notification.Type.ERROR_NOT_NAME_IN_GROUP,
								list,
								list.get(shift.value),
								list.get(shift.value).toString()));
					node.nodes.add(new Node.Lexeme(list.get(shift.value)));
					shift.value += 2;
				}
				node.nodes.add(new Node.Lexeme(list.get(shift.value)));
				++shift.value;
				
				return node;
			}
			else
				return syntaxGroup(list, shift, instance);
			
		case ROUND_BR_R:
			return previous;
		
		case SQUARE_BR_L:
			ArrayList<Lexeme> arr = new ArrayList<Lexeme>();
			
			if(list.get(++shift.value + 1) == Lexeme.COMMA) {			// If projection / equi-join
								
				while(list.get(shift.value + 1) != Lexeme.SQUARE_BR_R) {	// If not bracket, then comma
					if(list.get(shift.value).type != LexemeType.NAME)
						instance._errors.add(new Notification(
								Notification.Type.ERROR_NOT_NAME_IN_PROJECTION_OR_EQUIJOIN,
								list,
								list.get(shift.value),
								'"' + list.get(shift.value).toString() + '"'));
					arr.add(list.get(shift.value));
					shift.value += 2;
				}
				arr.add(list.get(shift.value));
				++shift.value;
				
				//if(list.get(shift.value - 1).type != LexemeType.NAME & list.get(shift.value - 1).type != LexemeType.VARIABLE) 
					if(
							(list.get(++shift.value).type == LexemeType.NAME) | 
							(list.get(shift.value).type == LexemeType.VARIABLE) |
							(list.get(shift.value).type == LexemeType.ROUND_BR_L)
					) {
						previous = new Node.EquiJoin(
								previous,
								syntax(previous, list, shift, instance),
								arr
						);
						
						return previous;
					} else {
						previous = new Node.Projection(
								previous,
								arr
						);
						--shift.value;
						return previous;
					}
				/*else {
					//previous = new Node.Names();
					//((Node.Names)previous).names = arr;
					return previous;
				}*/
			} else {													// Else selection / theta-join

				while(list.get(shift.value + 1) != Lexeme.SQUARE_BR_R) {
					arr.add(list.get(shift.value));
					++shift.value;
				}
				arr.add(list.get(shift.value));
				++shift.value;
				
				if(
						(list.get(++shift.value).type == LexemeType.NAME) | 
						(list.get(shift.value).type == LexemeType.VARIABLE) |
						(list.get(shift.value).type == LexemeType.ROUND_BR_L)
				) {
					previous = new Node.Join(
							previous,
							syntax(previous, list, shift, instance),
							arr
					);
					
					return previous;
				} else {
					previous = new Node.Selection(
							previous,
							arr
					);
					--shift.value;
					return previous;
				}
			}
			
		case AND:
			Node.Intersection and = new Node.Intersection();
			
			and.left = previous;
			++shift.value;
			and.right = syntax(null, list, shift, instance);
			
			return and;
			
		case OR:
			Node.Union or = new Node.Union();
			
			or.left = previous;
			++shift.value;
			or.right = syntax(null, list, shift, instance);
			
			return or;
			
		case DIVISION:
			Node.Minus div = new Node.Minus();
			
			div.left = new Node.Lexeme(list.get(shift.value - 1));
			++shift.value;
			div.right = syntax(null, list, shift, instance);
			
			return div;
			
		case XOR:
			Node.Difference xor = new Node.Difference();
			
			xor.left = new Node.Lexeme(list.get(shift.value - 1));
			++shift.value;
			xor.right = syntax(null, list, shift, instance);
			
			return xor;
			
		default:
			//throw new Exception("Invalid syntax! Started with: " + list.get(shift).type.name());
			return previous;
		}
	}
	
	public static Node syntaxGroup(ArrayList<Lexer.Lexeme> list, IntRef shift, KoddQL instance) {
		Node root = null;
		
		// Operations are implemented as static members, so address check is safe
		while(list.get(shift.value) != Lexeme.ROUND_BR_R) {
			root = syntax(root, list, shift, instance);
			++shift.value;
		}
		
		return root;
	}
}





