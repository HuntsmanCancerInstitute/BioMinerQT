package hci.biominer.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

@Entity
@Table(name="SampleCondition")
public class SampleCondition {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idSampleCondition")
	Long idSampleCondition;
	
	@Column(name="cond")
	String cond;
	
	@OneToOne
	@JoinColumn(name="idOrganismBuild")
	@JsonIgnore
	OrganismBuild organismBuild;
	
	public SampleCondition() {
		
	}
	
	public void setOrganismBuild(OrganismBuild organismBuild) {
		this.organismBuild = organismBuild;
	}

	public OrganismBuild getOrganismBuild() {
		return organismBuild;
	}

	public Long getIdOrganismBuild() {
		if (organismBuild != null) {
			return organismBuild.idOrganismBuild;
		} else {
			return null;
		}
	}
	
	public SampleCondition(String cond) {
		this.cond = cond;
	}

	public Long getIdSampleCondition() {
		return idSampleCondition;
	}

	public void setIdSampleCondition(Long idSampleCondition) {
		this.idSampleCondition = idSampleCondition;
	}

	public String getCond() {
		return cond;
	}

	public void setCond(String cond) {
		this.cond = cond;
	}
	
	
}
