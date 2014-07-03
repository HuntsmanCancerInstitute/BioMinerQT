package hci.biominer.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;


@Entity
@Table(name="SampleSource")
public class SampleSource {
	@Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="idSampleSource")
	Long idSampleSource;
	
	@Column(name="source")
	String source;
	
	public SampleSource() {
		
	}
	
	public SampleSource(String source) {
		this.source = source;
	}

	public Long getIdSampleSource() {
		return idSampleSource;
	}

	public void setIdSampleSource(Long idSampleSource) {
		this.idSampleSource = idSampleSource;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	
}
