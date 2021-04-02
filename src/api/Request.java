package api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Request {
	private final String[] content;
	private final String db;
	
	public Request() {
		content = new String[0];
		db = "";
	}

	public Request(String[] content, String db) {
		this.content = content;
		this.db = db;
	}

	public String getDb() {
		return db;
	}

	public String[] getContent() {
		return content;
	}
}