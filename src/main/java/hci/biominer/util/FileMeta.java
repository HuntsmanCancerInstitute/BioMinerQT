package hci.biominer.util;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties({"bytes"})
public class FileMeta {

	private String name;
	private String size;
	private String url;
	private String deleteUrl;
	private String deleteType = "DELETE";
	private String message;
	private String directory;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
	public String getDirectory() {
		return this.directory;
	}
	
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getDeleteType() {
		return deleteType;
	}
	public void setDeleteType(String deleteType) {
		this.deleteType = deleteType;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}
}
