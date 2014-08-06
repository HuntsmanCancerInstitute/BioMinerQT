package hci.biominer.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;


@Entity
@Table (name="GeneAnnotation")
public class GeneAnnotation {
	@Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idGeneAnnotation")
	private Long idGeneAnnotation;
	
	@Column(name="name")
	private String name;
	
	public GeneAnnotation() {
		
	}
	
	public GeneAnnotation(String name) {
		this.name = name;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return this.name;
	}
	
	public Long getIdGeneAnnotation() {
		return idGeneAnnotation;
	}

	public void setIdGeneAnnotation(Long idGeneAnnotation) {
		this.idGeneAnnotation = idGeneAnnotation;
	}
	

	
}
