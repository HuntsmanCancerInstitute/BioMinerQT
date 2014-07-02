package hci.biominer.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToOne;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import java.util.Date;
import java.util.List;

@Entity
@Table(name="Analysis")
public class Analysis {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment",strategy="increment")
	@Column(name="idAnalysis")
	Long idAnalysis;
	
	@Column(name="name")
	String name;
	
	@Column(name="description")
	String description;
	
	@Column(name="date")
	@Temporal(TemporalType.DATE)
	Date date;
	
	@OneToMany(mappedBy="analysis")
	List<Sample> samples;
	
	@OneToMany(mappedBy="analysis")
	List<DataTrack> dataTracks;
	
	@OneToMany(mappedBy="analysis")
	List<FileUpload> files;
	
	@OneToOne
	@JoinColumn(name="idAnalysisType")
	AnalysisType analysisType;
	
	@ManyToOne
	@JoinColumn(name="idProject")
	@JsonIgnore
	Project project;
	
	public Analysis() {
		
	}

	public Long getIdAnalysis() {
		return idAnalysis;
	}

	public void setIdAnalysis(Long idAnalysis) {
		this.idAnalysis = idAnalysis;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<Sample> getSamples() {
		return samples;
	}

	public void setSamples(List<Sample> samples) {
		this.samples = samples;
	}

	public List<DataTrack> getDataTracks() {
		return dataTracks;
	}

	public void setDataTracks(List<DataTrack> dataTracks) {
		this.dataTracks = dataTracks;
	}

	public List<FileUpload> getFiles() {
		return files;
	}

	public void setFiles(List<FileUpload> files) {
		this.files = files;
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
	
	
}
