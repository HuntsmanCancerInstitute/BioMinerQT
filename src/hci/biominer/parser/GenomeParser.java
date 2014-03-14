package hci.biominer.parser;

import hci.biominer.util.ModelUtil;
import java.io.BufferedReader;
import java.io.File;
import java.util.HashMap;


public class GenomeParser {
	
	//fields
	private String species;
	private String genomeBuildName;
	private File[] transcriptomeFiles;
	private HashMap<String, Integer> chrNameLength = new HashMap<String, Integer>();
	
	public GenomeParser(File desc) throws Exception{
		parseDescriptorFile(desc);
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(species); sb.append("\n");
		sb.append(genomeBuildName); sb.append("\n");
		for (File f : transcriptomeFiles){
			sb.append(f.getName()); sb.append(",");
		}
		sb.append("\n");
		sb.append(chrNameLength.toString()); sb.append("\n");
		return sb.toString();
	}
	

	private void parseDescriptorFile(File desc) throws Exception{
		BufferedReader in = ModelUtil.fetchBufferedReader(desc);
		//species
		String line = in.readLine();
		String[] tokens = ModelUtil.TAB.split(line);
		if (tokens.length !=2 || tokens[0].equals("Species") == false) throw new Exception("Error: could not parse species name from "+line+", aborting.\n");
		species = tokens[1];
		
		//name
		line = in.readLine();
		tokens = ModelUtil.TAB.split(line);
		if (tokens.length !=2 || tokens[0].equals("BuildName") == false) throw new Exception("Error: could not parse build name from "+line+", aborting.\n");
		genomeBuildName = tokens[1];
		
		//transcript file
		line = in.readLine();
		tokens = ModelUtil.TAB.split(line);
		if (tokens.length <2 || tokens[0].equals("TranscriptomeFile") == false) throw new Exception("Error: could not parse transcriptome file from "+line+", aborting.\n");
		transcriptomeFiles = new File[tokens.length-1];
		int index = 0;
		for (int i=1; i< tokens.length; i++){
			transcriptomeFiles[index] = new File (tokens[i]);
			if (transcriptomeFiles[index].canRead() == false) throw new Exception("Error: could not read transcriptome file from "+line+", aborting.\n");
			index++;
		}
		//chromosome names and lengths
		line = in.readLine();
		if (line.startsWith("ChromosomeLengths") == false) throw new Exception("Error: could not find the ChromosomeLengths line in "+line+", aborting.\n");
		while ((line = in.readLine()) != null){
			tokens = ModelUtil.TAB.split(line);
			Integer length = Integer.parseInt(tokens[0]);
			for (int i=1; i< tokens.length; i++){
				//look to see if it is already present
				Integer old = chrNameLength.get(tokens[i]);
				if (old != null){
					//different lengths?
					if (old.intValue() != length.intValue()) throw new Exception("Error: same chromosome name points to two different lengths in "+line+", aborting.\n");
				}
				else chrNameLength.put(tokens[i], length);
			}
		}
	}

	public String getSpecies() {
		return species;
	}

	public String getGenomeBuildName() {
		return genomeBuildName;
	}

	public File[] getTranscriptomeFiles() {
		return transcriptomeFiles;
	}

	public HashMap<String, Integer> getChrNameLength() {
		return chrNameLength;
	}
}
