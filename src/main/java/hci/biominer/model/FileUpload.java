package hci.biominer.model;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idFileUpload")
	Long idFileUpload;
	
	@ManyToOne
	@JoinColumn(name="idProject")
	@JsonIgnore()
	Project project;
	
	@OneToOne
	@JoinColumn(name="idAnalysisType")
	AnalysisType analysisType;
	
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
	
	@OneToOne(mappedBy="file")
	@JsonIgnore
	Analysis analysis;
	

	public FileUpload() {
		
	}
	
	public FileUpload(String name, String directory, Long size, FileStateEnum state, String message, 
			FileTypeEnum type, Project project) {
		this.name = name;
		this.directory = directory;
		this.size = size;
		this.state = state;
		this.message = message;
		this.type = type;
		this.project = project;
	}
	
	public FileUpload(String name, String directory, Long size, FileStateEnum state, String message, 
			FileTypeEnum type, Project project, AnalysisType analysisType) {
		this.name = name;
		this.directory = directory;
		this.size = size;
		this.state = state;
		this.message = message;
		this.type = type;
		this.project = project;
		this.analysisType = analysisType;
	}
	
	
	
	public AnalysisType getAnalysisType() {
		return analysisType;
	}

	public void setAnalysisType(AnalysisType analysisType) {
		this.analysisType = analysisType;
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
	
	public boolean getIsAnalysisSet() {
		if (this.analysis == null) {
			return false;
		} else {
			return true;
		}
	}

}
