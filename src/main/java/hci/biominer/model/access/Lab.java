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
	
	
	
	public Lab() {
		
	}
	
	public Lab(String first, String last) {
		this.first = first;
		this.last = last;
	}
	
	
	public String getFirst() {
		return this.first;
	}
	
	public String getLast() {
		return this.last;
	}
	
	public void setFirst(String first) {
		this.first = first;
	}
	
	public void setLast(String last) {
		this.last = last;
	}
	
	public Long getIdLab() {
		return idLab;
	}

	public void setIdLab(Long idLab) {
		this.idLab = idLab;
	}
	
	
}
