package hci.biominer.model.access;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;



@Entity (name = "Role")
@Cache(usage= CacheConcurrencyStrategy.READ_WRITE)
public class Role {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="idRole")
	private Long idRole;
	
	@Column(name="name")
	private String name;
	
	@Column(name="description")
	private String description;
	
	@ManyToMany
	@JoinTable(name="RolePermission",
				joinColumns={@JoinColumn(name="idRole")},
				inverseJoinColumns={@JoinColumn(name="idPermission")})
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private List<Permission> permission;
	
	
	public Role() {
		
	}


	public Long getIdRole() {
		return idRole;
	}


	public void setIdRole(Long idRole) {
		this.idRole = idRole;
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


	public List<Permission> getPermissions() {
		return permission;
	}


	public void setPermissions(List<Permission> permission) {
		this.permission = permission;
	}
	
	
}
