package hci.biominer.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;


@Entity
@Table(name="SampleSource")
public class SampleSource {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idSampleSource")
	Long idSampleSource;
	
	@Column(name="source")
	String source;
	
	public SampleSource() {
		
	}
	
	@OneToOne
	@JoinColumn(name="idOrganismBuild")
	@JsonIgnore
	OrganismBuild organismBuild;

	public void setOrganismBuild(OrganismBuild organismBuild) {
		this.organismBuild = organismBuild;
	}

	public OrganismBuild getOrganismBuild() {
		return organismBuild;
	}

	public Long getIdOrganismBuild() {
		if (organismBuild != null) {
			return organismBuild.idOrganismBuild;
		} else {
			return null;
		}
	}
	
	public SampleSource(String source) {
		this.source = source;
	}

	public Long getIdSampleSource() {
		return idSampleSource;
	}

	public void setIdSampleSource(Long idSampleSource) {
		this.idSampleSource = idSampleSource;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	
}
