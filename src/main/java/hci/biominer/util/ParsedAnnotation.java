package hci.biominer.util;

import hci.biominer.model.BiominerGene;
import hci.biominer.model.ExternalGene;

import java.util.ArrayList;

public class ParsedAnnotation {
	private ArrayList<BiominerGene> biominerGenes = null;
	private ArrayList<ExternalGene> externalGenes = null;
	private String message = "";
	
	

	public ParsedAnnotation() {
		biominerGenes = new ArrayList<BiominerGene>();
		externalGenes = new ArrayList<ExternalGene>();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public ArrayList<BiominerGene> getBiominerGenes() {
		return biominerGenes;
	}

	public void setBiominerGenes(ArrayList<BiominerGene> biominerGenes) {
		this.biominerGenes = biominerGenes;
	}

	public ArrayList<ExternalGene> getExternalGenes() {
		return externalGenes;
	}

	public void setExternalGenes(ArrayList<ExternalGene> externalGenes) {
		this.externalGenes = externalGenes;
	}
	
	

}
