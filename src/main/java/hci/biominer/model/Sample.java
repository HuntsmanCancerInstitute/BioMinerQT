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
import javax.persistence.JoinColumn;

@Entity
@Table(name="Sample")
public class Sample {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment",strategy="increment")
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
	
	@ManyToOne
	@JoinColumn(name="idAnalysis")
	@JsonIgnore
	Analysis analysis;
	
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
	
	public Analysis getAnalysis() {
		return analysis;
	}

	public void setAnalysis(Analysis analysis) {
		this.analysis = analysis;
	}
	
}
