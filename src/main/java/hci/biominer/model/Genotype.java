package hci.biominer.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;


@Entity
@Table (name="Genotype")
public class Genotype {
	@Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idGenotype")
	private Long idGenotype;
	
	@Column(name="name")
	private String name;
	
	public Genotype() {
		
	}
	
	public Genotype(String name) {
		this.name = name;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return this.name;
	}
	
	public Long getIdGenotype() {
		return idGenotype;
	}

	public void setIdGenotype(Long idGenotype) {
		this.idGenotype = idGenotype;
	}
	

	
}
