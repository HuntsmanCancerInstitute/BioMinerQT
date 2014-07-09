package hci.biominer.model;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToOne;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.JoinColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.GenerationType;

import java.util.Date;
import java.util.List;

@Entity
@Table(name="Analysis")
public class Analysis {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idAnalysis")
	Long idAnalysis;
	
	@Column(name="name")
	String name;
	
	@Column(name="description")
	String description;
	
	@Column(name="date")
	Long date;
	
	@ManyToMany()
	@JoinTable(name="AnalysisSample",
				joinColumns={@JoinColumn(name="idAnalysis")},
				inverseJoinColumns={@JoinColumn(name="idSample")})
	List<Sample> samples;
	
	@ManyToMany()
	@JoinTable(name="AnalysisDataTrack",
				joinColumns={@JoinColumn(name="idAnalysis")},
				inverseJoinColumns={@JoinColumn(name="idDataTrack")})
	List<DataTrack> dataTracks;
	
	@OneToOne
	@JoinColumn(name="idFileUpload")
	FileUpload file;
	
	@OneToOne
	@JoinColumn(name="idAnalysisType")
	AnalysisType analysisType;
	
	@ManyToOne()
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

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
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

	public FileUpload getFile() {
		return file;
	}

	public void setFile(FileUpload files) {
		this.file = files;
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
