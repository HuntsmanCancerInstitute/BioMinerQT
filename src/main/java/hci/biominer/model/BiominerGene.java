package hci.biominer.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="BiominerGene")
public class BiominerGene {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="idBiominerGene")
	Long idBiominerGene;
	
	@Column(name="BiominerGeneName")
	String BiominerGeneName;
	
	public BiominerGene() {
		
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
