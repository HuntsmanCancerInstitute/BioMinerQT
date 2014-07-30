package hci.biominer.util;

public class ErrorModel {
	private String errorName = null;
	private String errorMessage = null;
	private String errorStackTrace = null;
	private String errorTime = null;
	
	
	public String getErrorTime() {
		return errorTime;
	}
	public void setErrorTime(String errorTime) {
		this.errorTime = errorTime;
	}
	public String getErrorStackTrace() {
		return errorStackTrace;
	}
	public void setErrorStackTrace(String errorStackTrace) {
		this.errorStackTrace = errorStackTrace;
	}
	public String getErrorName() {
		return errorName;
	}
	public void setErrorName(String errorName) {
		this.errorName = errorName;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	
	
	
}
