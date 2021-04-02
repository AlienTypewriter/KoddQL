package server;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import api.Request;
import api.RequestDBMSException;
import api.Response;
import parser.KoddQL;

@RestController
public class APIController {
	
	@PostMapping("/api/")
	public Response request(@RequestBody Request r) throws RequestDBMSException {
		KoddQL.DBMS db;
		switch (r.getDb()) {
		case "oracle":
			db = KoddQL.DBMS.Oracle;
			break;
		case "mysql":
			db = KoddQL.DBMS.MySQL;
			break;
		case "postgres":
			db = KoddQL.DBMS.PostgreSQL;
			break;
		case "msserver":
			db = KoddQL.DBMS.Microsoft_SQL_Server;
			break;
		case "access":
			db = KoddQL.DBMS.MS_Access;
			break;
		default:
			String[] dbs = {"oracle", "mysql", "postgres", "msserver", "access"};
			throw new RequestDBMSException(dbs);
		}
		
		KoddQL transpiler = new KoddQL();
		transpiler.dbms = db;
		transpiler.toSQL(r.getContent());
		String[] qs = transpiler.queries.toArray(new String[0]);
		String[] ws = transpiler._warnings.toArray(new String[0]);
		String[] es = transpiler._errors.toArray(new String[0]);
		return new Response(qs,ws,es);
	}
}
