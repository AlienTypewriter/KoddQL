package api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
	private String[] queries;
	private String[] errors;
	private String[] warnings;

	public Response(String[] queries, String[] errors, String[] warnings) {
		this.queries = queries;
		this.errors = errors;
		this.warnings = warnings;
	}
	public String[] getQueries() {
		return queries;
	}

	public void setQueries(String[] queries) {
		this.queries = queries;
	}

	public String[] getErrors() {
		return errors;
	}

	public void setErrors(String[] errors) {
		this.errors = errors;
	}

	public String[] getWarnings() {
		return warnings;
	}

	public void setWarnings(String[] warnings) {
		this.warnings = warnings;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Response:\n\nQueries:\n");
		for (String s: queries) {
			sb.append(s).append('\n');
		}
		sb.append("Warnings:\n");
		for (String s: warnings) {
			sb.append(s).append('\n');
		}
		sb.append("Errors:\n");
		for (String s: errors) {
			sb.append(s).append('\n');
		}
		return sb.toString();
	}
}
