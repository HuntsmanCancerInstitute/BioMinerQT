package hci.biominer.util;

public class IdxModel {
	private String message;
	private Long idx;
	
	public IdxModel(String message, Long idx) {
		this.message = message;
		this.idx = idx;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getIdx() {
		return idx;
	}

	public void setIdx(Long idx) {
		this.idx = idx;
	}
	
	
}
