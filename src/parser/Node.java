package parser;

import java.util.ArrayList;

import parser.KoddQL.DBMS;

public abstract class Node {

	// No need to have multiple instances of identical objects
	public final static Lexeme ROUND_BR_L = new Lexeme(Lexer.Lexeme.ROUND_BR_L);
	public final static Lexeme ROUND_BR_R = new Lexeme(Lexer.Lexeme.ROUND_BR_R);
	public final static Lexeme SQUARE_BR_L = new Lexeme(Lexer.Lexeme.SQUARE_BR_L);
	public final static Lexeme SQUARE_BR_R = new Lexeme(Lexer.Lexeme.SQUARE_BR_R);
	
	public final static Lexeme AND = new Lexeme(Lexer.Lexeme.AND);
	public final static Lexeme OR = new Lexeme(Lexer.Lexeme.OR);
	public final static Lexeme DIVISION = new Lexeme(Lexer.Lexeme.DIVISION);
	
	public final static Lexeme OP_LESS = new Lexeme(Lexer.Lexeme.OP_LESS);
	public final static Lexeme OP_LESS_EQUAL = new Lexeme(Lexer.Lexeme.OP_LESS_EQUAL);
	public final static Lexeme OP_MORE = new Lexeme(Lexer.Lexeme.OP_MORE);
	public final static Lexeme OP_MORE_EQUAL = new Lexeme(Lexer.Lexeme.OP_MORE_EQUAL);
	public final static Lexeme OP_EQUAL = new Lexeme(Lexer.Lexeme.OP_EQUAL);
	public final static Lexeme OP_NOT_EQUAL = new Lexeme(Lexer.Lexeme.OP_NOT_EQUAL);
	public final static Lexeme OP_NOT = new Lexeme(Lexer.Lexeme.OP_NOT);
	public final static Lexeme OP_LIKE = new Lexeme(Lexer.Lexeme.OP_LIKE);
	public final static Lexeme OP_IN = new Lexeme(Lexer.Lexeme.OP_IN);
	public final static Lexeme OP_BETWEEN = new Lexeme(Lexer.Lexeme.OP_BETWEEN);
	public final static Lexeme OP_DIVISION = new Lexeme(Lexer.Lexeme.OP_DIVISION);
	
	public final static Lexeme COMMA = new Lexeme(Lexer.Lexeme.COMMA);
	public final static Lexeme ASSIGN = new Lexeme(Lexer.Lexeme.ASSIGN);
	public final static Lexeme SEMICOLON = new Lexeme(Lexer.Lexeme.SEMICOLON);
	
	public abstract NodeType getType();
	public void toSQL(StringBuilder sb, DBMS dbms) {}
	

	private static void _analyze(Node node, StringBuilder sb) {
		if(node instanceof Lexeme) {
			sb.append(((Lexeme)node).lexeme.toString());
		} else {
			sb.append('(');
			node.toSQL(sb, null);
			sb.append(')');
		}
	}
	
	private static void _analyzeInner(ArrayList<Lexer.Lexeme> list, StringBuilder sb) {
		for(int i = 0; i < list.size(); ++i)
			switch(list.get(i).type) {
			
			case OP_LESS:
				sb.append('<'); break;
			case OP_LESS_EQUAL:
				sb.append("<="); break;
			case OP_MORE:
				sb.append('>'); break;
			case OP_MORE_EQUAL:
				sb.append(">="); break;
			case OP_EQUAL:
				sb.append('='); break;
			case OP_NOT_EQUAL:
				sb.append("<>"); break;
				
			case OP_IN:
				sb.append("IN");  break;
			case OP_BETWEEN:
				sb.append("BETWEEN");  break;
			case OP_LIKE:
				sb.append("LIKE");  break;
			}
	}
	
	
	@Override
	public abstract String toString();
	
	
	public static class Lexeme extends Node {
		public Lexer.Lexeme lexeme;
		
		public Lexeme(Lexer.Lexeme lexeme) {
			this.lexeme = lexeme;
		}
		
		public void toSQL(StringBuilder sb, DBMS dbms) {
			sb.append("SELECT * FROM ");
			sb.append(lexeme);
		}
		
		public NodeType getType() {
			return NodeType.LEXEME;
		}
		
		@Override
		public String toString() {
			return lexeme.toString();
		}
	}
	
	public static class Assign extends Node {
		public Node left;
		public Node right;
		
		public Assign(Node left, Node right) {
			this.left = left;
			this.right = right;
		}
		
		public void toSQL(StringBuilder sb, DBMS dbms) {
			
			switch(right.getType()) {
			case LEXEME:
				switch((((Lexeme)right).lexeme).type) {
// SELECT
					case RESULT:
			
						switch(left.getType()) {
						
						case PROJECTION:
							sb.append("SELECT ");
							for(Lexer.Lexeme lexeme : ((Projection)left).selection) {
								sb.append(lexeme);
								sb.append(", ");
							}
							sb.delete(sb.length()-2, sb.length());
							sb.append( " FROM " );
							
							// Optimization: It's neater to output query like this, rather then making subquery.  
							switch(((Projection)left).table.getType()) {
// SELECT PROJECTION EQUIJOIN						
							case EQUIJOIN:
								_analyze(((EquiJoin)((Projection)left).table).left, sb);
								
								sb.append(" AS L, ");
								_analyze(((EquiJoin)((Projection)left).table).right, sb);
								
								sb.append(" AS R WHERE ");
								
								for(Lexer.Lexeme lexeme : ((EquiJoin)((Projection)left).table).selection) {
									
									sb.append("L.");
									sb.append(lexeme);
									
									sb.append('=');
									
									sb.append("R.");
									sb.append(lexeme);
									
									sb.append(" AND ");
								}
								sb.delete(sb.length()-5, sb.length());
								break;
							}
							
							if(((Projection)left).table.getType() == NodeType.LEXEME)
								sb.append(((Lexeme)((Projection)left).table).lexeme);
							else {
								sb.append('(');
								((Projection)left).table.toSQL(sb, null);
								sb.append(')');
							}
							break;
// SELECT EQUIJOIN						
						case EQUIJOIN:
							sb.append("SELECT * FROM ");
							
							if(((EquiJoin)left).left.getType() == NodeType.LEXEME)
								sb.append(((Lexeme)((EquiJoin)left).left).lexeme);
							else {
								sb.append('(');
								((EquiJoin)left).left.toSQL(sb, null);
								sb.append(')');
							}
							
							sb.append(" AS L, ");
							
							if(((EquiJoin)left).right.getType() == NodeType.LEXEME)
								sb.append(((Lexeme)((EquiJoin)left).right).lexeme);
							else {
								sb.append('(');
								((EquiJoin)left).right.toSQL(sb, null);
								sb.append(')');
							}
							sb.append(" AS R WHERE ");
							
							for(Lexer.Lexeme lexeme : ((EquiJoin)left).selection) {
								
								sb.append("L.");
								sb.append(lexeme);
								
								sb.append('=');
								
								sb.append("R.");
								sb.append(lexeme);
								
								sb.append(" AND ");
							}
							sb.delete(sb.length()-5, sb.length());
							break;
// SELECT JOIN		
						case JOIN:
							sb.append("SELECT * FROM ");
							_analyze(((Join)left).left, sb);
							sb.append(", ");
							_analyze(((Join)left).right, sb);
							sb.append(" WHERE ");
							//((Join)left).selection
							break;
// SELECT UNION	
						case OR:
							left.toSQL(sb, dbms);
							break;
// SELECT INTERSECT	
						case AND:
							left.toSQL(sb, dbms);
							break;
// SELECT MINUS	
						case MINUS:
							left.toSQL(sb, dbms);
							break;
// SELECT DIFFERENCE
						case DIFFERENCE:
							left.toSQL(sb, dbms);
							break;
						}
					break;
// DELETE
					case DELETE:
						switch(left.getType()) {
// DELETE PROJECTION
						case PROJECTION:	//TODO: compile-time check if node is lexeme
							sb.append("ALTER TABLE ");
							sb.append(((Lexeme)((Projection)left).table).lexeme);
							sb.append(" DROP ");
							switch(dbms) {
// DELETE PROJECTION Oracle
							case Oracle:
								sb.append('(');
								for(Lexer.Lexeme lexeme : ((Projection)left).selection) {
									sb.append(lexeme);
									sb.append(", ");
								}
								sb.delete(sb.length()-2, sb.length());
								sb.append(')');
								break;
// DELETE PROJECTION Microsoft SQL Server
							case Microsoft_SQL_Server:
								sb.append("COLUMN ");
								for(Lexer.Lexeme lexeme : ((Projection)left).selection) {
									sb.append(lexeme);
									sb.append(", ");
								}
								sb.delete(sb.length()-2, sb.length());
								break;
// DELETE PROJECTION MySQL
							case MySQL:
								for(Lexer.Lexeme lexeme : ((Projection)left).selection) {
									sb.append(lexeme);
									sb.append(", DROP ");
								}
								sb.delete(sb.length()-7, sb.length());
								break;
// DELETE PROJECTION PostgreSQL
							case PostgreSQL:
								sb.append("COLUMN ");
								for(Lexer.Lexeme lexeme : ((Projection)left).selection) {
									sb.append(lexeme);
									sb.append(", DROP COLUMN ");
								}
								sb.delete(sb.length()-14, sb.length());
								break;
							}
							break;
// DELETE SELECTION
						case SELECTION:
							sb.append("DELETE FROM ");
							((Selection)left).table.toSQL(sb, null);
							sb.append(" WHERE ");
							((Join)left).right.toSQL(sb, null);
							sb.append(" WHERE ");
							break;
// DELETE GROUP
						case GROUP:
							sb.append("DROP TABLE IF EXISTS ");	//TODO: Do compile-time check if everything in group is lexeme
							for(Node node : ((Group)left).nodes) {
								sb.append(((Lexeme)node).lexeme);
								sb.append(", ");
							}
							sb.delete(sb.length()-2, sb.length());
							break;
// DELETE LEXEME
						case LEXEME:
							sb.append("DROP TABLE ");
							sb.append(((Lexeme)left).lexeme);
							break;
						}
						break;
					
					case NAME:
						switch(left.getType()) {
						case COLUMN:
							sb.append("CREATE TABLE ");
							sb.append(right.toString());
							sb.append(" ( ");
							left.toSQL(sb, dbms);
							sb.append(')');
							break;
						case OR:
							if(((Union)left).right.getType() == NodeType.LEXEME) {
								sb.append("ALTER TABLE ");
								sb.append(right.toString());
								sb.append(" ( ");

								Union iterator = (Union)left;
								while(iterator.left.getType() != NodeType.COLUMN) {
									sb.append(" ADD ");
									iterator.right.toSQL(sb, dbms);
									iterator = (Union)iterator.left;
								}
								
								sb.append(')');
							} else {
								sb.append("CREATE TABLE ");
								sb.append(right.toString());
								sb.append(" ( ");
								
								Node iterator = left;
								while(iterator.getType() != NodeType.COLUMN) {
									((Union)iterator).right.toSQL(sb, dbms);
									iterator = ((Union)iterator).left;
									sb.append(", ");
								}
								iterator.toSQL(sb, dbms);
								
								sb.append(')');
							}
							break;
						}
						break;
					
					}	
					break;
					
					default:
						left.toSQL(sb, null);
						break;
				}
			sb.append(";");
		}
		
		public NodeType getType() {
			return NodeType.ASSIGN;
		}
		
		@Override
		public String toString() {
			return "Assign: (" + left.toString() + " -> " + right.toString() + ")";
		}
	}
	
	// Set operations
	public static class Union extends Node {
		public Node left;
		public Node right;
		
		public NodeType getType() {
			return NodeType.OR;
		}
		
		public void toSQL(StringBuilder sb, DBMS dbms) {
			if(left instanceof Lexeme) {
				left.toSQL(sb, null);
			}
			else {
				sb.append('(');
				left.toSQL(sb, null);
				sb.append(')');
			}
			
			sb.append(" UNION ");
			
			if(right instanceof Lexeme) {
				right.toSQL(sb, null);
			}
			else {
				sb.append('(');
				right.toSQL(sb, null);
				sb.append(')');
			}
			/*
			sb.append("SELECT * FROM ");
			left.toSQL(sb);
			sb.append(" UNION SELECT * FROM ");
			right.toSQL(sb);*/
		}
		
		@Override
		public String toString() {
			return "Union: (" + left.toString() + " | " + right.toString() + ")";
		}
	}
	
	public static class Intersection extends Node {
		public Node left;
		public Node right;
		
		public NodeType getType() {
			return NodeType.AND;
		}
		
		@Override
		public String toString() {
			return "Intersection: (" + left.toString() + " & " + right.toString() + ")";
		}
	}
	
	public static class Minus extends Node {
		public Node left;
		public Node right;
		
		public NodeType getType() {
			return NodeType.MINUS;
		}
		
		@Override
		public String toString() {
			return "Minus: (" + left.toString() + " / " + right.toString() + ")";
		}
	}
	
	public static class Difference extends Node {
		public Node left;
		public Node right;
		
		public NodeType getType() {
			return NodeType.DIFFERENCE;
		}
		
		@Override
		public String toString() {
			return "Difference: (" + left.toString() + " ^ " + right.toString() + ")";
		}
	}
	
	
	// Unary
	public static class Projection extends Node {
		public Node table;
		public ArrayList<Lexer.Lexeme> selection;
		
		public Projection(Node table, ArrayList<Lexer.Lexeme> selection) {
			this.table = table;
			this.selection = selection;
		}
		
		@Override
		public void toSQL(StringBuilder sb, DBMS dbms) {
			sb.append("SELECT ");
			for(Lexer.Lexeme lexeme : this.selection) {
				sb.append(lexeme);
				sb.append(", ");
			}
			sb.delete(sb.length()-2, sb.length());
			sb.append( " FROM " );
			
			// Optimization: It's neater to output query like this, rather then making subquery.  
			switch(this.table.getType()) {
			
			case EQUIJOIN:
				if(((EquiJoin)this.table).left instanceof Lexeme)
					((EquiJoin)this.table).left.toSQL(sb, null);
				else {
					sb.append('(');
					((EquiJoin)this.table).left.toSQL(sb, null);
					sb.append(')');
				}
				sb.append(" AS L, ");
				if(((EquiJoin)this.table).right instanceof Lexeme)
					((EquiJoin)this.table).right.toSQL(sb, null);
				else {
					sb.append('(');
					((EquiJoin)this.table).right.toSQL(sb, null);
					sb.append(')');
				}
				sb.append(" AS R WHERE ");
				
				for(Lexer.Lexeme lexeme : ((EquiJoin)this.table).selection) {
					
					sb.append("L.");
					sb.append(lexeme);
					
					sb.append('=');
					
					sb.append("R.");
					sb.append(lexeme);
					
					sb.append(" AND ");
				}
				sb.delete(sb.length()-5, sb.length());
				break;
				
			case GROUP:
				for(Node node : ((Group)this.table).nodes) {
					_analyze(node, sb);
					sb.append(", ");
				}
				sb.delete(sb.length()-2, sb.length());
				break;
				
			case LEXEME:
				sb.append(((Lexer.Lexeme.Data)((Lexeme)this.table).lexeme).data); 
				break;
			}
		}
		
		public NodeType getType() {
			return NodeType.PROJECTION;
		}
		
		@Override
		public String toString() {
			return "Projection: (" + table.toString() + " " + selection + ")";
		}
	}
	
	public static class Selection extends Node {
		public Node table;
		public ArrayList<Lexer.Lexeme> selection;
		
		public Selection(Node table, ArrayList<Lexer.Lexeme> selection) {
			this.table = table;
			this.selection = selection;
		}
		
		public NodeType getType() {
			return NodeType.SELECTION;
		}
		
		@Override
		public String toString() {
			return "Selection: (" + table.toString() + " " + selection + ")";
		}
	}
	
	// Binary
	public static class EquiJoin extends Node {
		public Node left;
		public Node right;
		public ArrayList<Lexer.Lexeme> selection;
		
		public EquiJoin(Node left, Node right, ArrayList<Lexer.Lexeme> selection) {
			this.left = left;
			this.right = right;
			this.selection = selection;
		}
		
		public NodeType getType() {
			return NodeType.EQUIJOIN;
		}
		
		@Override
		public String toString() {
			return "EquiJoin: (" + left.toString() + " " + selection + " " + right.toString() + ")";
		}
	}
	
	public static class Join extends Node {
		public Node left;
		public Node right;
		public ArrayList<Lexer.Lexeme> selection;
		
		public Join(Node left, Node right, ArrayList<Lexer.Lexeme> selection) {
			this.left = left;
			this.right = right;
			this.selection = selection;
		}
		
		public NodeType getType() {
			return NodeType.JOIN;
		}
		
		@Override
		public String toString() {
			return "Join: (" + left.toString() + " " + selection + " " + right.toString() + ")";
		}
	}
	
	
	public static class Column extends Node {
		public String name;
		public DataType type;
		public int typeModifier = 0;
		public int typeCount = 1;
		public boolean notNull = false;
		public boolean unique = false;
		public boolean primaryKey = false;
		public String foreignTable = "";
		public String foreignColumn = "";
		public String defaultValue = "";
		
		@Override
		public NodeType getType() {
			return NodeType.COLUMN;
		}

		@Override
		public void toSQL(StringBuilder sb, DBMS dbms) {
			sb.append(name);
			sb.append(' ');
			switch(dbms) {
			case Microsoft_SQL_Server:
				switch(type) {
				case BOOLEAN:
					sb.append("bit ");
				case INT:
					switch(typeModifier) {
					case 1:
						sb.append("tinyint "); break;
					case 2:
						sb.append("smallint "); break;
					case 4:
						sb.append("int "); break;
					case 8:
						sb.append("bigint "); break;
					}
					break;
				case FLOAT:
					switch(typeModifier) {
					case 4:
						sb.append("real "); break;
					case 8:
						sb.append("float "); break;
					}
					break;
				case CHAR:
					switch(typeModifier) {
					case 1:
						sb.append("varchar("); break;
					case 2:
						sb.append("nvarchar("); break;
					}
					sb.append(typeCount);
					sb.append(") ");
					break;
				}
				break;
			case MS_Access:
				switch(type) {
				case BOOLEAN:
					sb.append("Yes/No ");
				case INT:
					switch(typeModifier) {
					case 1:
						sb.append("Byte "); break;
					case 2:
						sb.append("Integer "); break;
					case 4:
						sb.append("Long "); break;
					}
					break;
				case FLOAT:
					switch(typeModifier) {
					case 4:
						sb.append("Single "); break;
					case 8:
						sb.append("Double "); break;
					}
					break;
				case CHAR:
					if(typeCount > 255) 
						sb.append("Text ");
					else 
						sb.append("Memo ");
					break;
				}
				break;
			case MySQL:
				switch(type) {
				case BOOLEAN:
					sb.append("BOOLEAN ");
				case INT:
					switch(typeModifier) {
					case 1:
						sb.append("Byte "); break;
					case 2:
						sb.append("Integer "); break;
					case 4:
						sb.append("Long "); break;
					}
					break;
				case FLOAT:
					switch(typeModifier) {
					case 4:
						sb.append("Single "); break;
					case 8:
						sb.append("Double "); break;
					}
					break;
				case CHAR:
					sb.append("VARCHAR(");
					sb.append(typeCount);
					sb.append(") ");
					break;
				}
				break;
			}
			
			if(notNull)
				sb.append(" NOT NULL ");
			if(unique)
				sb.append(" UNIQUE ");
			if(primaryKey)
				sb.append(" PRIMARY KEY ");
			if(!defaultValue.isEmpty()) {
				sb.append(" DEFAULT(");
				sb.append(defaultValue);
				sb.append(") ");
			}
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Column: (");
			sb.append(name);
			sb.append(" | ");
			sb.append(type.name());
			sb.append('(');
			sb.append(typeModifier);
			sb.append(')');
			if(typeCount != 1) {
				sb.append('[');
				sb.append(typeCount);
				sb.append(']');
			}
			sb.append(' ');
			
			if(notNull)
				sb.append("NOT NULL, ");
			if(unique)
				sb.append("UNIQUE, ");
			if(primaryKey)
				sb.append("PRIMARY KEY, ");
			if(!foreignTable.isEmpty()) {
				sb.append("FOREIGN KEY(");
				sb.append(foreignTable);
				sb.append('[');
				sb.append(foreignColumn);
				sb.append("]), ");
			}
			if(!defaultValue.isEmpty())
				sb.append("DEFAULT(" + defaultValue + "), ");
			
			//sb.deleteCharAt(sb.length()-1);
			sb.append(')');
			
			return sb.toString(); 
		}
		
	}
	
	public static class Group extends Node {

		ArrayList<Node> nodes = new ArrayList<Node>();
		
		@Override
		public void toSQL(StringBuilder sb, DBMS dbms) {
			sb.append("SELECT * FROM ");
			for(Node node : this.nodes) {
				_analyze(node, sb);
				sb.append(", ");
			}
			sb.delete(sb.length()-2, sb.length());
		}
		
		@Override
		public NodeType getType() {
			return NodeType.GROUP;
		}

		@Override
		public String toString() {
			return "Group: (" + nodes.toString() + ")"; 
		}
		
	}
	
	public static class Names extends Node {

		ArrayList<Lexer.Lexeme> names = new ArrayList<Lexer.Lexeme>();
		
		@Override
		public NodeType getType() {
			return NodeType.NAMES;
		}

		@Override
		public String toString() {
			return "Names: (" + names.toString() + ")";
		}
		
	}
	
	public static class Tuple extends Node {
		
		ArrayList<Node> values = new ArrayList<Node>();

		@Override
		public NodeType getType() {
			return NodeType.TUPLE;
		}

		@Override
		public String toString() {
			return "Tuple: (" + values.toString() + ")";
		}
		
		
	}
	
	public static class TupleNamed extends Node {
		
		ArrayList<Lexer.Lexeme> names = new ArrayList<Lexer.Lexeme>();
		ArrayList<Lexer.Lexeme> values = new ArrayList<Lexer.Lexeme>();

		@Override
		public NodeType getType() {
			return NodeType.TUPLE_NAMED;
		}

		@Override
		public String toString() {
			return "TupleNames: (" + names.toString() + " " + values.toString() + ")";
		}
	}
	
	public static class InnerOp {
		
		Lexer.Lexeme left;
		Lexer.Lexeme right;
		Operation operation;
		
		public InnerOp(Lexer.Lexeme left, Lexer.Lexeme right, Operation operation) {
			this.left = left;
			this.right = right;
			this.operation = operation;
		}
		
		@Override
		public String toString() {
			return left.toString() + " " + operation.name() + " " + right.toString();
		}
		
		public static enum Operation {
			EQUAL, NOT_EQUAL, MORE, LESS, MORE_EQUAL, LESS_EQUAL, DIVISION
		}
	}
	
	public enum NodeType {
		LEXEME, GROUP, COLUMN, NAMES, TUPLE, TUPLE_NAMED, ASSIGN, AND, OR, MINUS, DIFFERENCE, PROJECTION, SELECTION, EQUIJOIN, JOIN
	}
	
	public enum DataType {
		NOT_SET, BOOLEAN, INT, FLOAT, CHAR, DYNAMIC
	}
}