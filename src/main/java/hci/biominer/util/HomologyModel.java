package hci.biominer.util;


import java.util.HashMap;

public class HomologyModel {
	String message = null;
	HashMap<String,String> homologyMap;
	Boolean failed = false;
	
	public HomologyModel(StringBuffer warning,  HashMap<String,String> homologyMap) {
		this.homologyMap = homologyMap;
		this.message = warning.toString();
		this.failed = false;
	}
	
	public HomologyModel(StringBuffer warning) {
		this.message = warning.toString();
		this.failed = true;
	} 
	

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	

	public HashMap<String, String> getHomologyMap() {
		return homologyMap;
	}

	public void setHomologyMap(HashMap<String, String> homologyMap) {
		this.homologyMap = homologyMap;
	}

	public Boolean getFailed() {
		return failed;
	}

	public void setFailed(Boolean failed) {
		this.failed = failed;
	}
	
	
	
	
	
}
