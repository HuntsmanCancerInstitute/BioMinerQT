package hci.biominer.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hci.biominer.model.LiftoverChain;
import hci.biominer.model.LiftoverSupport;
import returnModel.QueryResult;
import returnModel.QueryResultContainer;
import returnModel.QueryResultMessage;

public class LiftoverUtilities {
	public static QueryResultMessage runLiftOver(QueryResultContainer qrc, LiftoverSupport ls, String pathToLiftover, File pathToLiftoverChains, String pathToLiftoverWorking, String idTab) {
		ArrayList<QueryResult> workingQR = (ArrayList<QueryResult>) qrc.getResultList();
		HashMap<String,Integer> errorCounts = new HashMap<String,Integer>();
		errorCounts.put("Total", workingQR.size());
		
		
		for (LiftoverChain chain: ls.getChains()) {
			File bedFile = new File(pathToLiftoverWorking,idTab + "_input.bed");
			File successFile = new File(pathToLiftoverWorking,idTab + "_success.bed");
			File failFile = new File(pathToLiftoverWorking,idTab + "_fail.bed");
			
			System.out.println(chain.getChainFile() + "X");
			
			//Write out bed file
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(bedFile));
				for (QueryResult qr: workingQR) {
					bw.write(String.format("chr%s\t%d\t%d\n",qr.getChrom(),qr.getStart(),qr.getEnd()));
				}
				bw.close();
			} catch (IOException ioex) {
				System.out.println("Could not write file bed file");
				QueryResultMessage qrm = new QueryResultMessage("Could not write bed file, please contact admins");
				cleanFiles(bedFile, successFile, failFile);
				return qrm;
			}
				
			//Set up external command
			ProcessBuilder pb = new ProcessBuilder(pathToLiftover + "/liftOver",bedFile.getAbsolutePath(),pathToLiftoverChains + "/" + chain.getChainFile(),
					successFile.getAbsolutePath(),failFile.getAbsolutePath(),"-minMatch=0.1");
			
			//Run the external commmand and attempt to catch errors.
			try {
				System.out.println("Staring liftover");
				Process p = pb.start();
				BufferedReader errStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String line = null;
				StringBuffer errorMessage = new StringBuffer();
				while((line = errStream.readLine()) != null) {
					errorMessage.append(line + "\n");
				}
				int retVal = p.waitFor();
				if (retVal != 0) {
					System.out.println("Error running Liftover: " + errorMessage);
					QueryResultMessage qrm = new QueryResultMessage("Error running Liftover: " + errorMessage);
					cleanFiles(bedFile, successFile, failFile);
					return qrm;
				} 
			} catch (InterruptedException irex) {
				System.out.println("Liftover was interrupted before it was finished: " + irex.getMessage());
				QueryResultMessage qrm = new QueryResultMessage("Liftover was interrupted before it was finished: " + irex.getMessage());
				cleanFiles(bedFile, successFile, failFile);
				return qrm;
			} catch (IOException ioex) {
				System.out.println("Could not process Liftover command: " + ioex.getMessage());
				QueryResultMessage qrm = new QueryResultMessage("Could not process liftover command: " + ioex.getMessage());
				cleanFiles(bedFile, successFile, failFile);
				return qrm;
			}
			
			//Read through input and outputs 
			try {
				if (!successFile.exists()) {
					System.out.println("Could not find liftover output!");
					QueryResultMessage qrm = new QueryResultMessage("Could not find liftover output");
					cleanFiles(bedFile, successFile, failFile);
					return qrm;
				}
				if (!failFile.exists()) {
					System.out.println("Could not find liftover output!");
					QueryResultMessage qrm = new QueryResultMessage("Could not find liftover output");
					cleanFiles(bedFile,successFile,failFile);
					return qrm;
				}
				BufferedReader successReader = new BufferedReader(new FileReader(successFile));
				BufferedReader failReader = new BufferedReader(new FileReader(failFile));
				String failLine = null;
				String failKey = null;
				ArrayList<QueryResult> newQrList = new ArrayList<QueryResult>();
				
				int successCount = 0;
				for (QueryResult qr: workingQR) {
					String key = String.format("chr%s:%d:%d",qr.getChrom(),qr.getStart(),qr.getEnd());
					
					if (failLine == null) {
						failLine = failReader.readLine();
						if (failLine != null) {
							while(true) {
								if (failLine.startsWith("#")) {
									failLine = failReader.readLine();
								} else {
									break;
								}
							}
							String[] failParts = failLine.split("\t");
							failKey = String.format("%s:%s:%s",failParts[0],failParts[1],failParts[2]);
						}
					}
					
					if (failKey.equals(key)) {
						failLine = null;
					} else {
						successCount++;
						String successLine = successReader.readLine();
						if (successLine != null) {
							String[] successParts = successLine.split("\t");
							String coordinates = String.format("%s:%s-%s",successParts[0],successParts[1],successParts[2]);
							try {
								QueryResult newQR = qr.clone();
								if (newQR.getEnsemblName() == null || newQR.getMappedName() == null) {
									newQR.setSearch(ls.getSourceBuild().getName() + "_" + qr.getCoordinates());
								} else {
									newQR.setSearch(ls.getSourceBuild().getName() + "_" + qr.getCoordinates() + "_" + qr.getEnsemblName() + "_" + qr.getMappedName());
								}
								newQR.setEnsemblName("NA");
								newQR.setMappedName("NA");
								newQR.setCoordinates(coordinates);
								newQrList.add(newQR);
							} catch (CloneNotSupportedException cnhex) {
								System.out.println("Can't clone");
							}	
						} else {
							System.out.println(failKey);
							System.out.println(key);
							throw new EOFException();
						}
						
					}
				}
				System.out.println(String.format("Starting: %d",workingQR.size()));
				System.out.println(String.format("Ending: %d",successCount));
				errorCounts.put("Converted", successCount);
				successReader.close();
				failReader.close();
				workingQR = newQrList;
			} catch (EOFException eofex) {
				System.out.println("Reached the end of liftover output prematurely.  Please contact admins");
				QueryResultMessage qrm = new QueryResultMessage("Reached the end of liftover output prematurely.  Please contact admins");
				cleanFiles(bedFile, successFile, failFile);
				return qrm;
			} catch (IOException ioex) {
				System.out.println("Error reading liftover output files");
				QueryResultMessage qrm = new QueryResultMessage("Could not write bed file, please contact admins");
				cleanFiles(bedFile, successFile, failFile);
				return qrm;
			} 
			cleanFiles(bedFile, successFile, failFile);
		}
		
		StringBuilder sb = new StringBuilder("");
		sb.append(String.format("Total Starting Regions: %d<br>Total Converted Regions: %d<br>",errorCounts.get("Total"),errorCounts.get("Converted")));
		QueryResultMessage qrm = new QueryResultMessage(workingQR,sb.toString());
		return qrm;
	}
	
	private static void cleanFiles(File bedFile, File successFile, File failFile) {
		if (bedFile.exists()) {
			bedFile.delete();
		}
		
		if (successFile.exists()) {
			successFile.delete();
		}
		
		if (failFile.exists()) {
			failFile.delete();
		}
	}
	
}
