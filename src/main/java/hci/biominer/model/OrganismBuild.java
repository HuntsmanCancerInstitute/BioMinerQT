package hci.biominer.model;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;

@Entity
@Table(name="OrganismBuild")
public class OrganismBuild {
	@Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idOrganismBuild")
	Long idOrganismBuild;
	
	@OneToOne()
	@JoinColumn(name="idOrganism")
	Organism organism;
	
	@Column(name="name")
	String name;
	
	@Column(name="GenomeFile")
	String genomeFile;
	
	@Column(name="GeneIdFile")
	String geneIdFile;
	
	@Column(name="TranscriptFile")
	String transcriptFile;


	public OrganismBuild() {
		
	}
	
	public OrganismBuild(Organism organism, String name) {
		this.name = name;
		this.organism = organism;
	}

	public Organism getOrganism() {
		return organism;
	}

	public void setOrganism(Organism organism) {
		this.organism = organism;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getIdOrganismBuild() {
		return idOrganismBuild;
	}

	public void setIdOrganismBuild(Long idOrganismBuild) {
		this.idOrganismBuild = idOrganismBuild;
	}
	
	public String getGeneIdFile() {
		return geneIdFile;
	}

	public void setGeneIdFile(String geneIdFile) {
		this.geneIdFile = geneIdFile;
	}

	public String getGenomeFile() {
		return genomeFile;
	}

	public void setGenomeFile(String genomeFile) {
		this.genomeFile = genomeFile;
	}
	
	public String getTranscriptFile() {
		return transcriptFile;
	}

	public void setTranscriptFile(String transcriptFile) {
		this.transcriptFile = transcriptFile;
	}
	
	
	
}
