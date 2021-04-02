package server;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import api.Request;
import api.Response;
import parser.KoddQL;

@RestController
public class APIController {
	
	private static final KoddQL.DBMS db = KoddQL.DBMS.PostgreSQL;
	private KoddQL compiler;
	
	public APIController() {
		compiler = new KoddQL();
		compiler.dbms = db;
	}
	
	
	@PostMapping("/api/")
	public Response request(@RequestBody Request r) {
		compiler.toSQL(r.getContent());
		String[] qs = compiler.queries.toArray(new String[0]);
		String[] ws = compiler._warnings.toArray(new String[0]);
		String[] es = compiler._errors.toArray(new String[0]);
		return new Response(qs,ws,es);
	}
}
