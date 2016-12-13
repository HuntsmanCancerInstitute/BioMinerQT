package hci.biominer.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnore;

@Entity
@Table(name="LiftoverSupport")
public class LiftoverSupport {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="idLiftoverSupport")
	Long idLiftoverSupport;
	
	@OneToOne()
	@JoinColumn(name="idSourceBuild")
	OrganismBuild sourceBuild;
	
	@OneToOne()
	@JoinColumn(name="idDestBuild")
	OrganismBuild destBuild;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="LiftoverJoin",
				joinColumns={@JoinColumn(name="idLiftoverSupport")},
				inverseJoinColumns={@JoinColumn(name="idLiftoverChain")})
	List<LiftoverChain> chains;
	
	public LiftoverSupport() {
		
	}
	
	public LiftoverSupport(OrganismBuild sourceBuild, OrganismBuild destBuild, List<LiftoverChain> chains) {
		this.sourceBuild = sourceBuild;
		this.destBuild = destBuild;
		this.chains = chains;
	}

	public Long getIdLiftoverSupport() {
		return idLiftoverSupport;
	}

	public void setIdLiftoverSupport(Long idLiftoverSupport) {
		this.idLiftoverSupport = idLiftoverSupport;
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

	public List<LiftoverChain> getChains() {
		return chains;
	}

	public void setChains(List<LiftoverChain> chains) {
		this.chains = chains;
	}
	
	
	
}
