package hci.biominer.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.EnumType;

import hci.biominer.util.Enumerated.*;

@Entity
@Table(name="FileUpload")
public class FileUpload {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment",strategy="increment")
	@Column(name="idFileUpload")
	Long idFileUpload;
	
	@OneToOne
	@JoinColumn(name="idParentFileUpload")
	FileUpload parent;
	
	@ManyToOne
	@JoinColumn(name="idProject")
	@JsonIgnore()
	Project project;
	
	@ManyToOne
	@JoinColumn(name="idAnalysis")
	@JsonIgnore()
	Analysis analysis;
	
	@Column(name="name")
	String name;
	
	@Column(name="directory")
	String directory;
	
	@Column(name="size")
	Long size;
	
	@Column(name="message")
	String message;
	
	@Column(name="state")
	@Enumerated(EnumType.STRING)
	FileStateEnum state;
	
	@Column(name="type")
	@Enumerated(EnumType.STRING)
	FileTypeEnum type;
	
	public FileUpload() {
		
	}
	
	public FileUpload(String name, String directory, Long size, FileStateEnum state, String message, 
			FileTypeEnum type, FileUpload parent, Project project) {
		this.name = name;
		this.directory = directory;
		this.size = size;
		this.state = state;
		this.message = message;
		this.type = type;
		this.parent = parent;
		this.project = project;
	}
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public FileStateEnum getState() {
		return state;
	}

	public void setState(FileStateEnum state) {
		this.state = state;
	}

	public Long getIdFileUpload() {
		return idFileUpload;
	}

	public void setIdFileUpload(Long idFileUpload) {
		this.idFileUpload = idFileUpload;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}
	
	public FileUpload getParent() {
		return parent;
	}

	public void setParent(FileUpload parent) {
		this.parent = parent;
	}

	public FileTypeEnum getType() {
		return type;
	}

	public void setType(FileTypeEnum type) {
		this.type = type;
	}
	
	public Analysis getAnalysis() {
		return analysis;
	}

	public void setAnalysis(Analysis analysis) {
		this.analysis = analysis;
	}
	
	
}
