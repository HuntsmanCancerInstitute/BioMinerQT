package hci.biominer.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;


@Entity
@Table(name="BiominerGene")
public class BiominerGene {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="idBiominerGene")
	@AccessType("property")
	Long idBiominerGene;
	
	@Column(name="BiominerGeneName")
	String BiominerGeneName;
	
	@OneToMany(mappedBy="biominerGene")
	List<ExternalGene> externalGenes;
	
	public BiominerGene() {
		
	}
	
	public List<ExternalGene> getExternalGenes() {
		return externalGenes;
	}

	public void setExternalGenes(List<ExternalGene> externalGenes) {
		this.externalGenes = externalGenes;
	}

	public BiominerGene(String biominerGeneName) {
		this.BiominerGeneName = biominerGeneName;
	}

	public Long getIdBiominerGene() {
		return idBiominerGene;
	}

	public void setIdBiominerGene(Long idBiominerGene) {
		this.idBiominerGene = idBiominerGene;
	}

	public String getBiominerGeneName() {
		return BiominerGeneName;
	}

	public void setBiominerGeneName(String biominerGeneName) {
		BiominerGeneName = biominerGeneName;
	}
}
