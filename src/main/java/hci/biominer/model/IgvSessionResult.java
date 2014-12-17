package hci.biominer.model;

import java.io.File;




public class IgvSessionResult {
	private String error = null;
	private String warnings = null;
	private String url = null;
	private String url2 = null;
	private File sessionFile = null;
	
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getWarnings() {
		return warnings;
	}
	public void setWarnings(String warnings) {
		this.warnings = warnings;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrl2() {
		return url2;
	}
	public void setUrl2(String url2) {
		this.url2 = url2;
	}
	public File getSessionFile() {
		return sessionFile;
	}
	public void setSessionFile(File sessionFile) {
		this.sessionFile = sessionFile;
	}
	
	
	

}
