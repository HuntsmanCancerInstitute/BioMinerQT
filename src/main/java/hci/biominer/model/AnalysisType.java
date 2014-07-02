package hci.biominer.model;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

@Entity
@Table(name="AnalysisType")
public class AnalysisType {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment",strategy="increment")
	@Column(name="idAnalysisType")
	Long idAnalysisType;
	
	@Column(name="type")
	String type;
	
	public AnalysisType() {
		
	}
	
	public AnalysisType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getIdAnalysisType() {
		return idAnalysisType;
	}

	public void setIdAnalysisType(Long idAnalysisType) {
		this.idAnalysisType = idAnalysisType;
	}
	
	
	
	

}
