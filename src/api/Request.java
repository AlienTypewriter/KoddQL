package api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Request {
	private final String[] content;
	
	public Request() {
		content = new String[0];
	}

	public Request(String[] content) {
		this.content = content;
	}

	public String[] getContent() {
		return content;
	}
}