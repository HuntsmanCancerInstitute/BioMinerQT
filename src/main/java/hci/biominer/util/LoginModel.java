package hci.biominer.util;

import hci.biominer.model.access.User;


public class LoginModel {
	private String message;
	private User user;
	private String referring;
	private String time;
	private Long timeout;
	
	
	public Long getTimeout() {
		return timeout;
	}
	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setReferring(String referring) {
		this.referring = referring;
	}
	
	public String getReferring() {
		return this.referring;
	}
	
	

}
