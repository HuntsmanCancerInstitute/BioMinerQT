package hci.biominer.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import hci.biominer.model.ExternalGene;
import hci.biominer.model.OrganismBuild;
import hci.biominer.util.ModelUtil;
import hci.biominer.util.HomologyModel;

public class HomologyParser {
	HashSet<String> sourceNameSet;
	HashSet<String> destNameSet;
	OrganismBuild sourceBuild;
	OrganismBuild destBuild;
	
	File inputFile;

	public HomologyParser(File inputFile,  List<ExternalGene> sourceList, List<ExternalGene> destList, 
			OrganismBuild sourceBuild, OrganismBuild destBuild) throws Exception {
		this.sourceBuild = sourceBuild;
		this.destBuild = destBuild;
		
		if (!inputFile.exists()) {
			throw new Exception("Specified input file does not exist");
		}
		
		this.inputFile = inputFile;
		
		sourceNameSet = new HashSet<String>();
		for (ExternalGene eg: sourceList) {
			String source = eg.getExternalGeneSource();
			String name = eg.getExternalGeneName();
			if (source.equals("ensembl")) {
				sourceNameSet.add(name);
			}
		}
		
		destNameSet = new HashSet<String>();
		for (ExternalGene eg: destList) {
			String source = eg.getExternalGeneSource();
			String name = eg.getExternalGeneName();
			if (source.equals("ensembl")) {
				destNameSet.add(name);
			}
		}
	}
	
	
	
	/*
	 * parse and validate homology file
	 */
	public HomologyModel processData() {
		BufferedReader br = null;
		boolean failed = false;
		StringBuffer warningMessage = new StringBuffer("");
		StringBuffer errorMessage = new StringBuffer("");
		int homologyCount = 0;
		int sourceCount = 0;
		int destCount = 0;
		ArrayList<String> sourceListFailed = new ArrayList<String>();
		ArrayList<String> destListFailed = new ArrayList<String>();
		HashMap<String,String> homologyMap = new HashMap<String,String>();
		HashSet<String> observed = new HashSet<String>();
		
		try {
			//Open file handle
			br = ModelUtil.fetchBufferedReader(inputFile);
			
			String line = null;
			while ((line = br.readLine()) != null) {
				homologyCount++;
				String[] parts = line.split("\t");
				
				if (parts.length != 2) {
					failed = true;
					errorMessage.append("There should exactly two columns of data in the homology file\n");
					break;
				}
				
				
				if (!parts[1].equals("None")) {
                    if (!observed.contains(parts[1])) {
                    	if (!destNameSet.contains(parts[1])) {
                    		destCount++;
    						destListFailed.add(parts[1]);
    					}
                    	observed.add(parts[1]);
					}
				}
				
				
				if (!sourceNameSet.contains(parts[0])) {
					sourceCount++;
					sourceListFailed.add(parts[0]);
				}
				
				homologyMap.put(parts[0], parts[1]);
			}
			
			float pSourceFailed = (float)sourceCount / homologyCount;
			float pDestFailed = (float)destCount / observed.size();
			
			if (pSourceFailed > 0.2) {
				failed = true;
				errorMessage.append(String.format("Too many missing source ids: %d of %d (%.2f)<br>",sourceCount, homologyCount, pSourceFailed));
			} else {
				warningMessage.append(String.format("Missing source ids: %d of %d (%.2f)<br>",sourceCount, homologyCount, pSourceFailed));
			}
			
			if (pDestFailed > 0.2) {
				failed = true;
				errorMessage.append(String.format("Too many missing destination ids: %d of %d (%.2f)<br>",destCount, observed.size(), pDestFailed));
			} else {
				warningMessage.append(String.format("Missing dest ids: %d of %d (%.2f)<br>",destCount, observed.size(), pDestFailed));
			}
			
			if (sourceListFailed.size() > 100) {
				warningMessage.append("Too many missing source IDs to list<br>");
			} else {
				for (String s: sourceListFailed) {
					warningMessage.append(String.format("%s<br>",s));
				}
			}
			
			if (destListFailed.size() > 100) {
				warningMessage.append("Too many missing dest IDs to list<br>");
			} else {
				for (String s: destListFailed) {
					warningMessage.append(String.format("%s<br>",s));
				}
			}
		
		
		} catch (IOException ioex) {
			failed = true;
			errorMessage.append(String.format("Error processing data file: %s.",ioex.getMessage()));
		} finally {
			try {
				br.close();
			} catch (Exception e){}
		}
		HomologyModel results;
		if (failed) {
			results = new HomologyModel(errorMessage);
		} else {
			results = new HomologyModel(warningMessage, homologyMap);
		}
		return results;
	}
}

