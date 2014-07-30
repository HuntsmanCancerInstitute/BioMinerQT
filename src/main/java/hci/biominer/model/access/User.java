package hci.biominer.model.access;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.ManyToMany;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

@Entity
@Table ( name = "User")
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
	
	@Column(name = "admin")
	private Boolean admin;
	
	@Column(name = "salt")
	private String salt;
	
	public User() {
		
	}
	
	public User(String firstName, String lastName, String userName, String password, String salt, String email, Long phone, boolean admin, List<Lab> labs, List<Institute> institutes) {
		this.first = firstName;
		this.last = lastName;
		this.username = userName;
		this.password = password;
		this.email = email;
		this.phone = phone;
		this.admin = admin;
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

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
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
