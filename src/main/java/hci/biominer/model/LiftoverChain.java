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
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnore;



@Entity
@Table(name="LiftoverChain")
public class LiftoverChain {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="idLiftoverChain")
	Long idLiftoverChain;
	
	@OneToOne()
	@JoinColumn(name="idSourceBuild")
	OrganismBuild sourceBuild;
	
	@OneToOne()
	@JoinColumn(name="idDestBuild")
	OrganismBuild destBuild;
	
	@Column(name="chainFile")
	String chainFile;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="LiftoverJoin",
				joinColumns={@JoinColumn(name="idLiftoverChain")},
				inverseJoinColumns={@JoinColumn(name="idLiftoverSupport")})
	@JsonIgnore
	@OrderColumn
	List<LiftoverSupport> supports;
	
	Integer supportCount;
	
	public LiftoverChain() {
		
	}

	
	public LiftoverChain(OrganismBuild sourceBuild, OrganismBuild destBuild, String chainFile) {
		this.sourceBuild = sourceBuild;
		this.destBuild = destBuild;
		this.chainFile = chainFile;
	}

	public Long getIdLiftoverChain() {
		return idLiftoverChain;
	}

	public void setIdLiftoverChain(Long idLiftoverChain) {
		this.idLiftoverChain = idLiftoverChain;
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

	public String getChainFile() {
		return chainFile;
	}

	public void setChainFile(String chainFile) {
		this.chainFile = chainFile;
	}


	public Integer getSupportCount() {
		return supports.size();
	}

	
	
}
