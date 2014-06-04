package hci.biominer.model.access;


import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Entity;
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
@Table ( name = "lab")
public class Lab {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment",strategy="increment")
	@Column(name="idx")
	private Long idx;
	
	@Column(name="first")
	private String first;
	
	@Column(name="last")
	private String last;
	
	@ManyToMany()
	@JoinTable(name="lab_institute",
	joinColumns={@JoinColumn(name="l_idx",referencedColumnName="idx")},
	inverseJoinColumns={@JoinColumn(name="i_idx",referencedColumnName="idx")})
	private List<Institute> institutes = null;
	
	public Lab() {
		
	}
	
	public Lab(String first, String last, List<Institute> institutes) {
		this.first = first;
		this.last = last;
		this.institutes = institutes;
	}
	
	public Long getId() {
		return this.idx;
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
	
	public void setInstitutes(List<Institute> institutes) {
		this.institutes = institutes;
	}
	
	public List<Institute> getInstitutes() {
		return this.institutes;
	}
}
