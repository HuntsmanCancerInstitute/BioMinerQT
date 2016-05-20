package hci.biominer.util;

import hci.biominer.controller.FileController;
import hci.biominer.model.DataTrack;
import hci.biominer.model.TransFactor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;

public class JBrowseUtilities {
	
	public static void createJbrowseFromBigBed(File bigBedFile, String trackName, String pathToJBrowse, String pathToUCSC) throws Exception {
		//Make sure file has a bigbed extension
		Pattern fp = Pattern.compile(".+?(.bb)");
		Matcher fm = fp.matcher(bigBedFile.getName());
		
		if (!fm.matches()) {
			System.out.println("Specified bigbed file does not have the correct extension, exiting");
			throw new Exception("Specified bigbed file does not have the correct extension, exiting");
		}
		
		//Setup file paths
		File workingDir = bigBedFile.getParentFile();
		
		//Create bed file using BigBedToBed
		File outputBedFile = new File(workingDir,bigBedFile.getName() + ".bed");
		
		ProcessBuilder pb = new ProcessBuilder(pathToUCSC + "/bigBedToBed",bigBedFile.getAbsolutePath(),outputBedFile.getAbsolutePath());
		
		try {
			Process p = pb.start();
			BufferedReader errStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			StringBuffer errorMessage = new StringBuffer();
			while((line = errStream.readLine()) != null) {
				errorMessage.append(line + "\n");
			}
			int retVal = p.waitFor();
			if (retVal != 0) {
				System.out.println("bigBedToBed failed: " + errorMessage.toString());
				throw new Exception("bigBedToBed failed: " + errorMessage.toString());
			} 
			
		} catch (InterruptedException irex) {
			System.out.println("BigBedToBed was interruped before it was finished: " + irex.getMessage());
			irex.printStackTrace();
			throw new Exception("BigBedToBed was interruped before it was finished: " + irex.getMessage());
		} 
		
		File tempBedFile = new File(outputBedFile + ".tmp");
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(outputBedFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempBedFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t");
				if (parts[0].equals("chrM")) {
					parts[0] = "MT";
				} else if (parts[0].startsWith("chr")) {
					parts[0] = parts[0].substring(3, parts[0].length());
				}
				String outLine = "";
				for (String p: parts) {
					outLine += p + "\t";
				}
				bw.write(outLine.substring(0, outLine.length()-1) + "\n");
			}
			bw.close();
			br.close();
		} catch (IOException ioex) {
			System.out.println("Error processing bed file: " + ioex.getMessage());
			throw new Exception("Bed decompression was interrupted before it was finished");
		}
		
		
		//create directory for jbrowe data
		File jbrowseDir = new File(workingDir,bigBedFile.getName() + "_jbrowse");
		jbrowseDir.mkdir();
		
		pb = new ProcessBuilder(pathToJBrowse + "/bin/flatfile-to-json.pl","--bed",tempBedFile.getAbsolutePath(),"--out",jbrowseDir.getAbsolutePath(),
				"--trackLabel",trackName,"--trackType","CanvasFeatures","--sortMem","10000000000");
		
		try {
			Process p = pb.start();
			BufferedReader errStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			StringBuffer errorMessage = new StringBuffer();
			while((line = errStream.readLine()) != null) {
				errorMessage.append(line + "\n");
			}
			int retVal = p.waitFor();
			outputBedFile.delete();
			tempBedFile.delete();
			if (retVal != 0) {
				System.out.println("flatfile-to-json.pl failed: " + errorMessage.toString());
				throw new Exception("flatfile-to-json.pl failed: " + errorMessage.toString());
			} 
			
		} catch (InterruptedException irex) {
			outputBedFile.delete();
			tempBedFile.delete();
			System.out.println("flatfile-to-json.pl was interruped before it was finished: " + irex.getMessage());
			irex.printStackTrace();
			throw new Exception("flatfile-to-json.pl was interruped before it was finished: " + irex.getMessage());
		} 
		
	}
	
	public static void createJbrowseFromBed(File bedFile, String trackName, String pathToJBrowse) throws Exception {
		//Make sure file has a bigbed extension
		Pattern fp = Pattern.compile("(.+?).gz");

		Matcher fm = fp.matcher(bedFile.getName());
		
		if (!fm.matches()) {
			System.out.println("Specified bed file does not have the correct extension, exiting");
			throw new Exception("Specified bed file does not have the correct extension, exiting");
		}
		
		//Setup file paths
		String fileStem = null;
		fileStem = fm.group(1);
		File tempBedFile = new File(bedFile.getParentFile(),fileStem + ".temp.bed");
		
		try {
			BufferedReader br = IO.fetchReaderOnGZippedFile(bedFile);
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempBedFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t");
				if (parts[0].equals("chrM")) {
					parts[0] = "MT";
				} else if (parts[0].startsWith("chr")) {
					parts[0] = parts[0].substring(3, parts[0].length());
				}
				String outLine = "";
				for (String p: parts) {
					outLine += p + "\t";
				}
				bw.write(outLine.substring(0, outLine.length()-1) + "\n");
			}
			bw.close();
			br.close();
		} catch (IOException ioex) {
			System.out.println("Error processing bed file: " + ioex.getMessage());
			throw new Exception("Bed decompression was interrupted before it was finished");
		}
	
		//create directory for jbrowe data
		File jbrowseDir = new File(bedFile.getParentFile(),bedFile.getName() + "_jbrowse");
		jbrowseDir.mkdir();
		
		ProcessBuilder pb = new ProcessBuilder(pathToJBrowse + "/bin/flatfile-to-json.pl","--bed",tempBedFile.getAbsolutePath(),"--out",jbrowseDir.getAbsolutePath(),
				"--trackLabel",trackName,"--trackType","CanvasFeatures","--key",trackName,"--sortMem","10000000000");
		
		try {
			Process p = pb.start();
			BufferedReader errStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			StringBuffer errorMessage = new StringBuffer();
			while((line = errStream.readLine()) != null) {
				errorMessage.append(line + "\n");
			}
			int retVal = p.waitFor();
			tempBedFile.delete();
			if (retVal != 0) {
				System.out.println("flatfile-to-json.pl failed: " + errorMessage.toString());
				throw new Exception("flatfile-to-json.pl failed: " + errorMessage.toString());
			} 
			
		} catch (InterruptedException irex) {
			tempBedFile.delete();
			System.out.println("flatfile-to-json.pl was interruped before it was finished: " + irex.getMessage());
			irex.printStackTrace();
			throw new Exception("flatfile-to-json.pl was interruped before it was finished: " + irex.getMessage());
		} 		
	}
	
	public static boolean createJbrowseRepo(String username, String genomeBuild, List<DataTrack> dtList, TransFactor tf, String pathToJBrowse) throws Exception {
		boolean foundTracks = false;
		
		System.out.println("REPO SIZE " + dtList.size());
		
		
		
		Pattern p1 = Pattern.compile("^\\s+\\\"tracks\\\" : \\[$");
		Pattern p2 = Pattern.compile("\\s+\\],*");
		Pattern p3 = Pattern.compile("\\s+\\\"style\\\" : \\{");
		
		StringBuffer newJson = new StringBuffer("");
		StringBuffer newConf = new StringBuffer("");
		ArrayList<File> dirToMap = new ArrayList<File>();
		ArrayList<String> dirNames = new ArrayList<String>();
		ArrayList<File> filesToMap = new ArrayList<File>();
		ArrayList<String> fileNames = new ArrayList<String>();
		ArrayList<String> trackNames = new ArrayList<String>();
		
		//Parse datatrack information
		int counter = 0;
		for (DataTrack dt: dtList) {
			counter += 1;
			String filename = dt.getPath();
			String name = dt.getName();
			if (filename.endsWith(".bb")) {
				File trackDir = new File(FileController.getIgvDirectory(),dt.getProject().getIdProject() + "/" + filename + "_jbrowse/tracks/" + name);
				File trackConf = new File(FileController.getIgvDirectory(),dt.getProject().getIdProject() + "/" + filename + "_jbrowse/trackList.json");
				if (trackDir.exists() && trackConf.exists()) {
					
					foundTracks = true;
					dirToMap.add(trackDir);
					dirNames.add(name);
					
					try {
						BufferedReader br = new BufferedReader(new FileReader(trackConf));
						String line = null;
						boolean start = false;
						
						while ((line = br.readLine()) != null) {
							if (start) {
								Matcher m2 = p2.matcher(line);
								if (m2.matches()) {
									newJson.append(",\n");
									break;
								}
								newJson.append(line + "\n");
							} else {
								Matcher m1 = p1.matcher(line);
								if (m1.matches()) {
									start= true;
								}
							}
						}
						br.close();
						
					} catch (IOException ioex) {
						System.out.println("Error reading bb config file");
						System.out.println(ioex.getMessage());
						throw new Exception("Error parsing bb config file: " + ioex.getMessage());
					}
					
				}
			} else if (filename.endsWith(".bw")) {
				File trackDir = new File(FileController.getIgvDirectory(),dt.getProject().getIdProject() + "/" + filename);
				if (trackDir.exists()) {
					foundTracks = true;
					filesToMap.add(trackDir);
					fileNames.add(filename);
					trackNames.add(name);
					
					String conf = new String("[ tracks." + String.valueOf(counter) + "]\n"
							+ "storeClass = JBrowse/Store/SeqFeature/BigWig\n"
							+ "urlTemplate = raw/" + filename + "\n" 
							+ "category = Quantitative\n"
							+ "type = JBrowse/View/Track/Wiggle/XYPlot\n"
							+ "autoscale = local\n" 
							+ "key = " + name + "\n"
							+ "label = " + name + "\n"
							+ "style.height=50\n");
					
					newConf.append(conf);
				}
			}
		}
		
		if (tf != null) {
			System.out.println("YO: TF");
			String filename = tf.getFilename();
			String name = tf.getName();
			File trackDir = new File(FileController.getTfParseDirectory(), filename + "_jbrowse/tracks/" + name);
			File trackConf = new File(FileController.getTfParseDirectory(), filename + "_jbrowse/trackList.json");
			

			if (trackDir.exists() && trackConf.exists()) {
				System.out.println("Oi");
				foundTracks = true;
				dirToMap.add(trackDir);
				dirNames.add(name);
				
				try {
					BufferedReader br = new BufferedReader(new FileReader(trackConf));
					String line = null;
					boolean start = false;
					boolean next = false;
					while ((line = br.readLine()) != null) {
						if (start) {
							Matcher m2 = p2.matcher(line);
							Matcher m3 = p3.matcher(line);
							if (next) {
								newJson.append("\t\t\"label\" : false,\n");
								next = false;
							}
							if (m3.matches()) {
								next = true;
							} 
							
							if (m2.matches()) {
								newJson.append(",\n");
								break;
							}
							newJson.append(line + "\n");
						} else {
							Matcher m1 = p1.matcher(line);
							if (m1.matches()) {
								start= true;
							}
						}
					}
					br.close();
					
				} catch (IOException ioex) {
					System.out.println("Error reading tf config file");
					System.out.println(ioex.getMessage());
					throw new Exception("Error parsing tf config file: " + ioex.getMessage());
				}	
			}
		}
		
		//Build the repo
		if (foundTracks) {
			//Destroy old directory and create new one
			File userDir = new File(pathToJBrowse,"data/" + username);
			if (userDir.exists()) {
				FileUtils.deleteDirectory(userDir);
			}
			userDir.mkdirs();
			
			//Create link to genome
			File genomeDir = new File(pathToJBrowse,"data/" + genomeBuild);
			
			//Create directory structure
			File trackDir = new File(userDir,"tracks");
			trackDir.mkdir();
			File rawDir = new File(userDir,"raw");
			rawDir.mkdir();
			
			// Create link to sequence
			File eSeqDir = new File(genomeDir,"seq");
			File nSeqDir = new File(userDir,"seq");
			createLink(eSeqDir,nSeqDir);
			
			// Create link to genes
			File eGeneDir = new File(genomeDir,"tracks/genes");
			File nGeneDir = new File(userDir,"tracks/genes");
			createLink(eGeneDir,nGeneDir);
			
			// Create link to genes
			File eNamesDir = new File(genomeDir,"names");
			File nNamesDir = new File(userDir,"names");
			createLink(eNamesDir,nNamesDir);
			
			//Create new trackList
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(userDir,"trackList.json")));
				BufferedReader br = new BufferedReader(new FileReader(new File(genomeDir,"trackList.json")));
				String line = null;
				boolean start = false;
				boolean finish = false;
				while ((line = br.readLine()) != null) {
					if (start) {
						if (finish) {
							bw.write(line + "\n");
						} else {
							bw.write(newJson.toString());
							bw.write(line + "\n");
							finish = true;
						}
					} else {
						Matcher m1 = p1.matcher(line);
						if (m1.matches()) {
							start = true;
						}
						bw.write(line + "\n");
					}
				}
				br.close();
				bw.close();
				
				BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(userDir,"tracks.conf")));
				
				
				StringBuffer sb = new StringBuffer("");
				int counter2 = 0;
				for (String name: dirNames) {
					if (counter2 < 10) {
						sb.append(name + ",");
						counter++;
					}
				}
				for (String name: trackNames) {
					if (counter2 < 10) {
						sb.append(name + ",");
						counter++;
					}
				}
				String tracks = sb.toString();
				if (tracks.length() > 0) {
					bw2.write("[ general ]\n");
					bw2.write("alwaysOnTracks = " + tracks.substring(0, tracks.length()-1) + ",genes\n\n");
				}
				
				bw2.write(newConf.toString());
				bw2.close();
			} catch (IOException ioex) {
				System.out.println("Error writing json config file: " + ioex.getMessage());
				throw new Exception("Error writing json config file: " + ioex.getMessage());
			}
			
			//Create links
			for (int i=0; i<dirToMap.size();i++) {
				File dir = dirToMap.get(i);
				String name = dirNames.get(i);
				File localFile = new File(trackDir,name);
				createLink(dir,localFile);
			}
			
			for (int i=0; i<filesToMap.size();i++) {
				File file = filesToMap.get(i);
				String name = fileNames.get(i);
				File localFile = new File(rawDir,name);
				createLink(file,localFile);
			}
		}
		
		
		
		return foundTracks;
	}
	
	private static void createLink(File filename, File linkname) throws Exception{
		try {
			if (linkname.exists()) {
				linkname.delete();
			}
				
			ProcessBuilder pb = new ProcessBuilder("ln","-s",filename.getAbsolutePath(),linkname.getAbsolutePath());
			Process p = pb.start();
			
			int val = p.waitFor();
			
			if (val != 0) {
				System.out.println("[createLink]  System could not create a link to your file " + filename.getAbsolutePath());
				BufferedReader br2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String line2 = null;
				while((line2 = br2.readLine()) != null) {
					System.out.println(line2);
				}
				throw new Exception("System could not create lik to your file: " + filename.getAbsolutePath());
			}
				
			
		} catch (IOException ioex) {
			System.out.println("[createLink]  IO Exception while trying to create a link to your file: " + filename.getAbsolutePath());
			throw new Exception("IOException while trying to create a link to your file: " + filename.getAbsolutePath());
		} catch (InterruptedException ieex) {
			System.out.println("[createLink]  Process was interrupted while trying to create a link to your file: " + filename.getAbsolutePath());
			throw new Exception("Process interrupted while trying to create a link to your file: " + filename.getAbsolutePath());
		}	
	}
}
