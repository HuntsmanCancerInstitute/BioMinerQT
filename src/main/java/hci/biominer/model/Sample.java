package hci.biominer.model;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToOne;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.JoinColumn;

@Entity
@Table(name="Sample")
public class Sample {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idSample")
	Long idSample;
	
	@OneToOne
	@JoinColumn(name="idSampleType")
	SampleType sampleType;
	
	@OneToOne
	@JoinColumn(name="idSamplePrep")
	SamplePrep samplePrep;
	
	@OneToOne
	@JoinColumn(name="idSampleSource")
	SampleSource sampleSource;
	
	@OneToOne
	@JoinColumn(name="idSampleCondition")
	SampleCondition sampleCondition;
	
	@ManyToOne
	@JoinColumn(name="idProject")
	@JsonIgnore
	Project project;
	
	@ManyToMany
	@JoinTable(name="AnalysisSample",
				joinColumns={@JoinColumn(name="idSample")},
				inverseJoinColumns={@JoinColumn(name="idAnalysis")})
	@JsonIgnore
	List<Analysis> analyses;
	
	@Column(name="name")
	String name;
	
	public Sample() {
		
	}
	
	public Sample(String name, SampleType sampleType, SamplePrep samplePrep, SampleSource sampleSource, 
			SampleCondition sampleCondition, Project project) {
		this.name = name;
		this.sampleType = sampleType;
		this.samplePrep = samplePrep;
		this.sampleSource = sampleSource;
		this.sampleCondition = sampleCondition;
		this.project = project;
	}
	

	public Long getIdSample() {
		return idSample;
	}

	public void setIdSample(Long idSample) {
		this.idSample = idSample;
	}

	public SampleType getSampleType() {
		return sampleType;
	}

	public void setSampleType(SampleType sampleType) {
		this.sampleType = sampleType;
	}

	public SamplePrep getSamplePrep() {
		return samplePrep;
	}

	public void setSamplePrep(SamplePrep samplePrep) {
		this.samplePrep = samplePrep;
	}

	public SampleSource getSampleSource() {
		return sampleSource;
	}

	public void setSampleSource(SampleSource sampleSource) {
		this.sampleSource = sampleSource;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SampleCondition getSampleCondition() {
		return sampleCondition;
	}

	public void setSampleCondition(SampleCondition sampleCondition) {
		this.sampleCondition = sampleCondition;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	
	public List<Analysis> getAnalyses() {
		return analyses;
	}

	public void setAnalyses(List<Analysis> analyses) {
		this.analyses = analyses;
	}
	
	public boolean isAnalysisSet() {
		if (this.analyses == null || this.analyses.size() == 0) {
			return false;
		} else {
			return true;
		}
	}
	
}
