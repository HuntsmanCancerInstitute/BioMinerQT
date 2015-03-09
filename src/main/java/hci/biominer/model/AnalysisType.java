package hci.biominer.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

import hci.biominer.util.Enumerated.*;

@Entity
@Table(name="AnalysisType")
public class AnalysisType {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idAnalysisType")
	Long idAnalysisType;
	
	@Column(name="type")
	@Enumerated(EnumType.STRING)
	AnalysisTypeEnum type;
	
	@Column(name="codeResultTypes")
	String codeResultTypes;

	
	public AnalysisType() {
		
	}
	
	public AnalysisType(AnalysisTypeEnum type) {
		this.type = type;
	}

	public AnalysisTypeEnum getType() {
		return type;
	}

	public void setType(AnalysisTypeEnum type) {
		this.type = type;
	}

	public Long getIdAnalysisType() {
		return idAnalysisType;
	}

	public void setIdAnalysisType(Long idAnalysisType) {
		this.idAnalysisType = idAnalysisType;
	}

  public String getCodeResultTypes() {
    return codeResultTypes;
  }

  public void setCodeResultTypes(String codeResultTypes) {
    this.codeResultTypes = codeResultTypes;
  }
	

}
