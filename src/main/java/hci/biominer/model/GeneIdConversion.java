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
@Table(name="GeneIdConversion")
public class GeneIdConversion {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="idGeneIdConversion")
	Long idGeneIdConversion;
	
	@OneToOne()
	@JoinColumn(name="idSourceBuild")
	OrganismBuild sourceBuild;
	
	@OneToOne()
	@JoinColumn(name="idDestBuild")
	OrganismBuild destBuild;
	
	@Column(name="conversionFile")
	String conversionFile;
	
	public GeneIdConversion() {
		
	}
	
	public GeneIdConversion(OrganismBuild sourceBuild, OrganismBuild destBuild, String conversionFile) {
		this.conversionFile = conversionFile;
		this.sourceBuild = sourceBuild;
		this.destBuild = destBuild;
	}

	public Long getIdGeneIdConversion() {
		return idGeneIdConversion;
	}

	public void setIdGeneIdConversion(Long idGeneIdConversion) {
		this.idGeneIdConversion = idGeneIdConversion;
	}

	public OrganismBuild getSourceBuild() {
		return sourceBuild;
	}

	public void setSourceBuild(OrganismBuild sourceBuild) {
		this.sourceBuild = sourceBuild;
	}

	public OrganismBuild getDestBuild() {
		return destBuild;
	}

	public void setDestBuild(OrganismBuild destBuild) {
		this.destBuild = destBuild;
	}

	public String getConversionFile() {
		return conversionFile;
	}

	public void setConversionFile(String conversionFile) {
		this.conversionFile = conversionFile;
	}
	
}
