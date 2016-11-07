package hci.biominer.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import returnModel.HomologyModel;
import hci.biominer.model.ExternalGene;
import hci.biominer.model.OrganismBuild;
import hci.biominer.util.ModelUtil;

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
		int stableCount = 0;
		int sourceCount = 0;
		int destCount = 0;
		
		int homologyMissingCount = 0;
		int homologyAmbigCount = 0;
		int homologyOkCount = 0;
		int stableRetiredCount = 0;
		int stableAmbigCount = 0;
		int stableOkCount = 0;
		int stableIncompleteCount = 0;
		int stableNoHistoryCount = 0;
		
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
				boolean isHomologyAmbig = false;
				boolean isStableAmbig = false;
				
				String[] parts = line.split("\t");
				
				if (parts.length != 3) {
					failed = true;
					errorMessage.append("There should exactly three columns of data in the homology file\n");
					break;
				}
				
				//Check source ID to see if it's found in our annotation
				if (!sourceNameSet.contains(parts[0])) {
					sourceCount++;
					sourceListFailed.add(parts[0]);
				}
				
				//Check homology ID and classify
				if (parts[1].equals("no_homology")) {
					homologyMissingCount++;
					continue;
				} else if (parts[1].endsWith("*")) {
					homologyAmbigCount++;
					isHomologyAmbig = true;
				} else {
					homologyOkCount++;
				}
				
				//Check stable ID and classify
				String finalID = null;
				stableCount++;
				if (parts[2].equals("none")) {
					stableRetiredCount++;
					continue;
				} else if (parts[2].equals("incomplete")) {
					stableIncompleteCount++;
					continue;
				} else if (parts[2].equals("no_history")) {
					stableNoHistoryCount++;
					continue;
				} else if (parts[2].endsWith("*")) {
					stableAmbigCount++;
					isStableAmbig = true;
					finalID = parts[2].substring(0, parts[2].length()-1);
				} else {
					stableOkCount++;
					finalID = parts[2];
				}
				
				//Check stable ID to see if it's found in our annotation
				if (!observed.contains(finalID)) {
                	if (!destNameSet.contains(finalID)) {
                		destCount++;
						destListFailed.add(finalID);
					}
                	observed.add(finalID);
				}
				
				//If either the homology or stable ID is ambiguous, mark it
				if (isHomologyAmbig) {
					finalID += "*";
				}
				if (isStableAmbig) {
					finalID += "+";
				}
				
			
				homologyMap.put(parts[0], finalID);
			}
			
			StringBuffer stats = new StringBuffer("");
			stats.append(String.format("Homolog Statistics:<br>"));
			stats.append(String.format("Total IDs searched %d<br>",homologyCount));
			stats.append(String.format("Single Homolog %d (%.2f)<br>",homologyOkCount, (float)stableOkCount / homologyCount*100));
			stats.append(String.format("Multiple Homolog %d (%.2f)<br>",homologyAmbigCount, (float)homologyOkCount / homologyCount*100));
			stats.append(String.format("No Homology %d (%.2f)<br>",homologyMissingCount, (float)homologyMissingCount / homologyCount*100));
			stats.append("<br><br>");
			
			stats.append(String.format("Stable ID Statistics:<br>"));
			stats.append(String.format("Total IDs searched %d<br>",stableCount));
			stats.append(String.format("Single Stable ID %d (%.2f)<br>",stableOkCount, (float)stableOkCount / stableCount * 100));
			stats.append(String.format("Multiple StableID %d (%.2f)<br>",stableAmbigCount, (float)stableAmbigCount / stableCount * 100));
			stats.append(String.format("Retired Stable ID %d (%.2f)<br>",stableRetiredCount, (float)stableRetiredCount / stableCount * 100));
			stats.append(String.format("Retired Stable ID %d (%.2f)<br>",stableNoHistoryCount, (float)stableNoHistoryCount / stableCount * 100));
			stats.append(String.format("Incomplete Stable ID %d (%.2f)<br>",stableIncompleteCount, (float)stableIncompleteCount / stableCount * 100));
			stats.append("<br><br>");
			
			errorMessage.append(stats.toString());
			warningMessage.append(stats.toString());
			
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

