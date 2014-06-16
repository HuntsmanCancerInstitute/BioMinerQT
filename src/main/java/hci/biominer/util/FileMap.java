package hci.biominer.util;

import java.util.LinkedList;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

public class FileMap {
	
	private  LinkedList<FileMeta>   files;
	
	public FileMap() {
		files = new LinkedList<FileMeta>();
	}

	public LinkedList<FileMeta> getFiles() {
		return files;
	}

	public void setFiles(LinkedList<FileMeta> files) {
		this.files = files;
	}
	
	public void addFile(FileMeta file) {
		this.files.add(file);
	}
	
	
}