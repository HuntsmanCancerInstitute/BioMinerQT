package hci.biominer.model;

public class DashboardModel {
	private int[] data;
	private String label;
	
	public DashboardModel(int count, String label) {
		this.data = new int[]{1};
		this.data[0] = count;
		this.label = label;
	}

	public int[] getData() {
		return data;
	}

	public void setData(int[] data) {
		this.data = data;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	
}

