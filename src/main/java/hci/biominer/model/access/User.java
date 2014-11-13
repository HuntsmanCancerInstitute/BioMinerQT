package hci.biominer.model.access;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

@Entity
@Table ( name = "User")
@Cache(usage= CacheConcurrencyStrategy.READ_WRITE)
public class User {
	@Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idUser")
	private Long idUser;
	
	@ManyToMany()
	@JoinTable(name="UserLab",
	joinColumns={@JoinColumn(name="idUser",referencedColumnName="idUser")},
	inverseJoinColumns={@JoinColumn(name="idLab",referencedColumnName="idLab")})
	private List<Lab> labs = null;
	
	@ManyToMany()
	@JoinTable(name="UserInstitute",
	joinColumns={@JoinColumn(name="idUser",referencedColumnName="idUser")},
	inverseJoinColumns={@JoinColumn(name="idInstitute",referencedColumnName="idInstitute")})
	private List<Institute> institutes = null;
	
	@ManyToMany()
	@JoinTable(name="UserRole",
	joinColumns={@JoinColumn(name="idUser",referencedColumnName="idUser")},
	inverseJoinColumns={@JoinColumn(name="idRole",referencedColumnName="idRole")})
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private List<Role> roles = null;
	
	
	@Column(name = "first")
	private String first;
	
	@Column(name = "last")
	private String last;
	
	@Column(name = "username")
	private String username;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "phone")
	private Long phone;
	
	@Column(name = "salt")
	private String salt;
	
	@Column(name = "guid")
	private String guid;
	
	@Column(name = "guidExpiration")
	private Timestamp guidExpiration;
	
	
	public User() {
		
	}
	
	public User(String firstName, String lastName, String userName, String password, String salt, String email, Long phone, String guid, Timestamp guidExpiration,List<Role> roles, List<Lab> labs, List<Institute> institutes) {
		this.first = firstName;
		this.last = lastName;
		this.username = userName;
		this.password = password;
		this.email = email;
		this.phone = phone;
		this.guid = guid;
		this.guidExpiration = guidExpiration;
		this.roles = roles;
		this.labs = labs;
		this.institutes = institutes;
		this.salt = salt;
	}

	
	public Long getIdUser() {
		return idUser;
	}

	public void setIdUser(Long idUser) {
		this.idUser = idUser;
	}

	public List<Lab> getLabs() {
		return labs;
	}

	public void setLabs(List<Lab> labs) {
		this.labs = labs;
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getLast() {
		return last;
	}

	public void setLast(String last) {
		this.last = last;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@JsonIgnore
	public String getPassword() {
		return password;
	}

	@JsonIgnore
	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getPhone() {
		return phone;
	}

	public void setPhone(Long phone) {
		this.phone = phone;
	}

	@JsonIgnore
	public String getGuid() {
		return guid;
	}
	
	@JsonIgnore
	public void setGuid(String guid) {
		this.guid = guid;
	}
	
	@JsonIgnore
	public Timestamp getGuidExpiration () {
		return guidExpiration;		
	}
	
	@JsonIgnore
	public void setGuidExpiration(Timestamp guidExpiration) {
		this.guidExpiration = guidExpiration;
	}
	
	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
	@JsonIgnore
	public String getSalt() {
		return this.salt;
	}
	
	@JsonIgnore
	public void setSalt(String salt) {
		this.salt = salt;
	}
	
	public void setInstitutes(List<Institute> institutes) {
		this.institutes = institutes;
	}
	
	public List<Institute> getInstitutes() {
		return this.institutes;
	}
	
}
