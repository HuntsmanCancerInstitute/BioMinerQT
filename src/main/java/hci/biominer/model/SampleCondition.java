package hci.biominer.model;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

@Entity
@Table(name="SampleCondition")
public class SampleCondition {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment",strategy="increment")
	@Column(name="idSampleCondition")
	Long idSampleCondition;
	
	@Column(name="condition")
	String condition;
	
	public SampleCondition() {
		
	}
	
	public SampleCondition(String condition) {
		this.condition = condition;
	}

	public Long getIdSampleCondition() {
		return idSampleCondition;
	}

	public void setIdSampleCondition(Long idSampleCondition) {
		this.idSampleCondition = idSampleCondition;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	
}
