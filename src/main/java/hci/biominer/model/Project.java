package hci.biominer.model;

import java.util.List;

import org.hibernate.annotations.IndexColumn;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.ManyToMany;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Enumerated;

import hci.biominer.util.Enumerated.ProjectVisibilityEnum;
import hci.biominer.model.access.Lab;
import hci.biominer.model.access.Institute;

@Entity
@Table(name="Project")
public class Project {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	Long idProject;
	
	@Column(name="name")
	String name;
	
	@Column(name="description")
	String description;
	
	@Column(name="dataUrls")
	String dataUrls;
	
	@Column(name="visibility")
	@Enumerated(EnumType.STRING)
	ProjectVisibilityEnum visibility;

	@OneToOne
	@JoinColumn(name="idOrganismBuild")
	OrganismBuild organismBuild;
	
	@OneToMany(mappedBy="project")
	List<Sample> samples;
	
	@OneToMany(mappedBy="project")
	List<DataTrack> dataTracks;
	
	@OneToMany(mappedBy="project")
	List<FileUpload> files;
	
	@OneToMany(mappedBy="project")
	List<Analysis> analyses;
	
	@ManyToMany()
    @JoinTable(name="ProjectLab",
                joinColumns={@JoinColumn(name="idProject")},
                inverseJoinColumns={@JoinColumn(name="idLab")})
	@IndexColumn(name = "labOrder")
    List<Lab> labs;
	
	@ManyToMany()
	@JoinTable(name="ProjectOwner",
				joinColumns={@JoinColumn(name="idProject")},
				inverseJoinColumns={@JoinColumn(name="idLab")})
	@IndexColumn(name="ownerOrder")
	List<Lab> owners;
	
	@ManyToMany()
	@JoinTable(name="ProjectInstitute",
			joinColumns={@JoinColumn(name="idProject")},
			inverseJoinColumns={@JoinColumn(name="idInstitute")})
	@IndexColumn(name="instituteOrder")
	List<Institute> institutes;
	
	
	
	public Project() {
		
	}
	
		
	public List<Lab> getOwners() {
		return owners;
	}

	public void setOwners(List<Lab> owners) {
		this.owners = owners;
	}

	public Long getIdProject() {
		return idProject;
	}

	public void setIdProject(Long idProject) {
		this.idProject = idProject;
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

	public ProjectVisibilityEnum getVisibility() {
		return visibility;
	}

	public void setVisibility(ProjectVisibilityEnum visibility) {
		this.visibility = visibility;
	}

	public OrganismBuild getOrganismBuild() {
		return organismBuild;
	}

	public void setOrganismBuild(OrganismBuild organismBuild) {
		this.organismBuild = organismBuild;
	}

	public List<Sample> getSamples() {
		return samples;
	}

	public void setSamples(List<Sample> samples) {
		this.samples = samples;
	}

	public List<DataTrack> getDataTracks() {
		return dataTracks;
	}

	public void setDataTracks(List<DataTrack> dataTracks) {
		this.dataTracks = dataTracks;
	}

	public List<FileUpload> getFiles() {
		return files;
	}

	public void setFiles(List<FileUpload> files) {
		this.files = files;
	}

	public List<Lab> getLabs() {
		return labs;
	}

	public void setLabs(List<Lab> labs) {
		this.labs = labs;
	}
	
	public List<Analysis> getAnalyses() {
		return analyses;
	}

	public void setAnalyses(List<Analysis> analyses) {
		this.analyses = analyses;
	}

	public List<Institute> getInstitutes() {
		return institutes;
	}

	public void setInstitutes(List<Institute> institutes) {
		this.institutes = institutes;
	}
	
	public String getDataUrls() {
		return dataUrls;
	}

	public void setDataUrls(String dataUrls) {
		this.dataUrls = dataUrls;
	}
	
	
	
	
	
}
