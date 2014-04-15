package hci.biominer.model.genome;

import java.io.Serializable;

import hci.biominer.util.ModelUtil;

/**Representation of a chromosome*/
public class Chromosome implements Serializable {
	
	//fields
	private String name;
	/**Includes the name*/
	private String[] aliases;
	private int length;
	private static final long serialVersionUID = 1L;
	
	//constructors
	public Chromosome (int length, String name, String[] aliases){
		this.length = length;
		this.name = name;
		this.aliases = aliases;
	}
	
	//methods
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(name + "\tName\n");
		sb.append(length + "\tLength\n");
		sb.append(ModelUtil.stringArrayToString(aliases, ", ") + "\tAliases\n");
		return sb.toString();
	}

	//getters and setters
	public String getName() {
		return name;
	}
	public String[] getAliases() {
		return aliases;
	}
	public int getLength() {
		return length;
	}
	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}
}
