package hci.biominer.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

@Entity
@Table(name="SampleType")
public class SampleType {
	@Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idSampleType")
	Long idSampleType;
	
	@Column(name="type")
	String type;
	
	public SampleType() {
		
	}
	
	public SampleType(String type) {
		this.type = type;
	}

	public Long getIdSampleType() {
		return idSampleType;
	}

	public void setIdSampleType(Long idSampleType) {
		this.idSampleType = idSampleType;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
