package hci.biominer.model.access;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Column;


@Entity(name="Permission")
public class Permission {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="idPermission")
	private Long idRole;
	
	@Column(name="permission")
	private String permission;
	
	public Permission() {
		
	}

	public Long getIdRole() {
		return idRole;
	}

	public void setIdRole(Long idRole) {
		this.idRole = idRole;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}
	
}
