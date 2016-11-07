package returnModel;

import java.util.LinkedList;


public class PreviewMap {

	private String message;
	private LinkedList<String[]> previewData;
	
	public PreviewMap() {
		this.message = "nada";
		this.previewData = new LinkedList<String[]>();
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public LinkedList<String[]> getPreviewData() {
		return this.previewData;
	}
	
	public void setPreviewData(LinkedList<String[]> previewData) {
		this.previewData = previewData;
	}
	
	public void addPreviewData(String[] data) {
		this.previewData.add(data);
	}

}
