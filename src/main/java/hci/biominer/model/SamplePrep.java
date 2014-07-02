package hci.biominer.model;


import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;

@Entity
@Table(name="SamplePrep")
public class SamplePrep {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment",strategy="increment")
	@Column(name="idSamplePrep")
	Long idSamplePrep;
	
	@Column(name="description")
	String description;
	
	@OneToOne()
	@JoinColumn(name="idSampleType")
	SampleType sampleType;

	public SamplePrep() {
		
	}
	
	public SamplePrep(String description, SampleType sampleType) {
		this.description = description;
		this.sampleType = sampleType;
	}
	
	public Long getIdSamplePrep() {
		return idSamplePrep;
	}

	public void setIdSamplePrep(Long idSamplePrep) {
		this.idSamplePrep = idSamplePrep;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public SampleType getSampleType() {
		return sampleType;
	}

	public void setSampleType(SampleType sampleType) {
		this.sampleType = sampleType;
	}
	
	
	

}
