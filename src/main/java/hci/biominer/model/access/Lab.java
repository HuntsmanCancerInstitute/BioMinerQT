package hci.biominer.model.access;


import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;

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
	
	public Lab() {
		
	}
	
	public Lab(String first, String last) {
		this.first = first;
		this.last = last;
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
}
