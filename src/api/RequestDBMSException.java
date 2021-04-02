package api;

public class RequestDBMSException extends RuntimeException {

	private static final long serialVersionUID = -7563742700589502873L;
	private static final String MSG = "Invalid DBMS option selected. Available options: ";

	public RequestDBMSException() {
		super();
	}
	
	public RequestDBMSException(String[] dbs) {
		super(MSG+String.join(", ", dbs)+'.');
	}
}
