package hci.biominer.model.access;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.FetchType;

import java.util.List;

@Entity
@Table ( name = "Lab")
public class Lab {
	@Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idLab")
	private Long idLab;
	
	@Column(name="first")
	private String first;
	
	@Column(name="last")
	private String last;
	
	@Column(name="email")
	private String email;
	
	@Column(name="phone")
	private String phone;
	
	
	public Lab() {
		
	}
	
	public Lab(String first, String last, String email, String phone) {
		this.first = first;
		this.last = last;
		this.email = email;
		this.phone = phone;
	}
	
	
	public String getFirst() {
		return this.first;
	}
	
	public String getLast() {
		return this.last;
	}
	
	public String getEmail() {
		return this.email;
	}	

	public String getPhone() {
		return this.phone;
	}
	
	public void setFirst(String first) {
		this.first = first;
	}
	
	public void setLast(String last) {
		this.last = last;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public Long getIdLab() {
		return idLab;
	}

	public void setIdLab(Long idLab) {
		this.idLab = idLab;
	}
	
	
}
