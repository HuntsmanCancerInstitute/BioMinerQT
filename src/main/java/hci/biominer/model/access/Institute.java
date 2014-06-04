package hci.biominer.model.access;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

@Entity
@Table (name = "institute")
public class Institute {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment",strategy="increment")
	@Column(name="idx")
	private Long idx;
	
	@Column(name="name")
	private String name;

	public Institute() {
		
	}
	
	public Institute(String name) {
		this.name = name;
	}
	
	public Long getIdx() {
		return this.idx;
	}
	
	public void setIdx(Long idx) {
		this.idx = idx;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
