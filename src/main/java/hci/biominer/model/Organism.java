package hci.biominer.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;


@Entity
@Table (name="Organism")
public class Organism {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idOrganism")
	private Long idOrganism;
	
	@Column(name="common")
	private String common;
	
	@Column(name="binomial")
	private String binomial;
	
	public Organism() {
		
	}
	
	public Organism(String common, String binomial) {
		this.common = common;
		this.binomial = binomial;
	}
	
	
	public void setCommon(String common) {
		this.common = common;
	}
	
	public void setBinomial(String binomial) {
		this.binomial = binomial;
	}
	
	
	public String getCommon() {
		return this.common;
	}
	
	public String getBinomial() {
		return this.binomial;
	}

	public Long getIdOrganism() {
		return idOrganism;
	}

	public void setIdOrganism(Long idOrganism) {
		this.idOrganism = idOrganism;
	}
	

	
}
