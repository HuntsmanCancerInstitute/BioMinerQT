package returnModel;

public class DashboardModel {
	private int y;
	private String key;
	
	public DashboardModel(int count, String label) {
		this.y = count;
		this.key = label;
		
	}

	public int getY() {
		return y;
	}

	public void setY(int data) {
		this.y = data;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String label) {
		this.key = label;
	}
	
	
}

