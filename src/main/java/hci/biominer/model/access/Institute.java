package hci.biominer.model.access;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

@Entity
@Table (name = "Institute")
public class Institute {
	@Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idInstitute")
	private Long idInstitute;
	
	@Column(name="name")
	private String name;

	public Institute() {
		
	}
	
	public Institute(String name) {
		this.name = name;
	}
	
	public Long getIdInstitute() {
		return idInstitute;
	}

	public void setIdInstitute(Long idInstitute) {
		this.idInstitute = idInstitute;
	}

	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
