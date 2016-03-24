package hci.biominer.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.springframework.context.annotation.Lazy;

@Entity
@Table(name="ExternalGene")
public class ExternalGene {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="idExternalGene")
	Long idExternalGene;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="idBiominerGene")
	@JsonIgnore
	BiominerGene biominerGene;
	
	@Column(name="ExternalGeneName")
	String ExternalGeneName;
	
	@Column(name="ExternalGeneSource")
	String ExternalGeneSource;
	
	@OneToOne()
	@JoinColumn(name="idOrganismBuild")
	OrganismBuild organismBuild;
	
	
	@Column(name="idBiominerGene",updatable=false,insertable=false)
	Long idBiominerGene;
	

	public ExternalGene(BiominerGene bg, OrganismBuild ob, String ExternalGeneName, String ExternalGeneSource) {
		this.biominerGene = bg;
		this.organismBuild = ob;
		this.ExternalGeneName = ExternalGeneName;
		this.ExternalGeneSource = ExternalGeneSource;
	}
	
	public ExternalGene() {
		
	}
	
	public OrganismBuild getOrganismBuild() {
		return organismBuild;
	}

	public void setOrganismBuild(OrganismBuild organismBuild) {
		this.organismBuild = organismBuild;
	}

	
	public Long getIdExternalGene() {
		return idExternalGene;
	}

	public void setIdExternalGene(Long idExternalGene) {
		this.idExternalGene = idExternalGene;
	}

	public BiominerGene getBiominerGene() {
		return biominerGene;
	}

	public void setBiominerGene(BiominerGene biominerGene) {
		this.biominerGene = biominerGene;
	}

	public String getExternalGeneName() {
		return ExternalGeneName;
	}

	public void setExternalGeneName(String externalGeneName) {
		ExternalGeneName = externalGeneName;
	}

	public String getExternalGeneSource() {
		return ExternalGeneSource;
	}

	public void setExternalGeneSource(String externalGeneSource) {
		ExternalGeneSource = externalGeneSource;
	}
	
	public Long getIdBiominerGene() {
		return idBiominerGene;
	}

	public void setIdBiominerGene(Long idBiominerGene) {
		this.idBiominerGene = idBiominerGene;
	}
	
	
	
}
