package hci.biominer.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Table(name="TransFactor")
public class TransFactor {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="idTransFactor")
	Long idTransFactor;
	
	@Column(name="name")
	String name;
	
	@Column(name="description",nullable=true)
	String description;
	
	@Column(name="filename")
	String filename;
	
	@OneToOne
	@JoinColumn(name="idOrganismBuild")
	OrganismBuild organismBuild;
	public TransFactor() {
		
	}

	public Long getIdTransFactor() {
		return idTransFactor;
	}

	public void setIdTransFactor(Long idTransFactor) {
		this.idTransFactor = idTransFactor;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public OrganismBuild getOrganismBuild() {
		return organismBuild;
	}

	public void setOrganismBuild(OrganismBuild organismBuild) {
		this.organismBuild = organismBuild;
	}

	
	
	
}
