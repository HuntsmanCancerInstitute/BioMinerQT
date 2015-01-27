package hci.biominer.util;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class VCFUtilities {

	public static void unzipTabix(File tabixFile, File outputFile, String pathToTabix) throws Exception{
		//Make sure the file is the correct format and grab the file prefix
		Pattern fp = Pattern.compile("(.+?.vcf).gz");
		Matcher fm = fp.matcher(tabixFile.getName());
		
		//Exit if not the correct format
		if (!fm.matches()) {
			System.out.println("Tabix-compressed VCF file does not have the correct extension, exiting");
			throw new Exception("Tabix-compressed VCF file does not have the correct extension, exiting");
		}
		
		outputFile.deleteOnExit();
		
		ProcessBuilder pb = new ProcessBuilder(pathToTabix + "/bgzip","-c","-d",tabixFile.getAbsoluteFile().toString());
	
		
		try {
			Process p = pb.start();
			BufferedReader outStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader errStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			StringBuffer sb = new StringBuffer("");
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			String line = null;
			
			while ((line = outStream.readLine()) != null) {
				bw.write(line + "\n");
			}
			
			while((line = errStream.readLine()) != null) {
				sb.append(line + "\n");
			}
			
			int retVal = p.waitFor();
			if (retVal != 0) {
				System.out.println("File decompression failed: " +sb.toString());
				bw.close();
				throw new Exception("File decompression failed: " +sb.toString());
			} else {
				tabixFile.delete();
			}
			
			System.out.println("Unzipping OK");
			bw.close();
			
		} catch (IOException ioex ) {
			System.out.println("Failed to read/write file stream : " + ioex.getMessage());
			ioex.printStackTrace();
			throw new Exception("Failed to read/write file stream : " + ioex.getMessage());
		} catch (InterruptedException irex) {
			System.out.println("Gunzip was interruped before it was finished: " + irex.getMessage());
			irex.printStackTrace();
			throw new Exception("Gunzip was interruped before it was finished: " + irex.getMessage());
		} 
	}
	
	
	public static void createTabix(File vcfFile, File compressedFile, String pathToTabix) throws Exception {
		//Make sure the file is the correct format and grab the file prefix
		Pattern fp = Pattern.compile(".+?.vcf");
		Matcher fm = fp.matcher(vcfFile.getName());
		
		//Exit if not the correct format
		if (!fm.matches()) {
			System.out.println("VCF file does not have the correct extension, exiting");
			throw new Exception("VCF file does not have the correct extension, exiting");
		}
		
		
		ProcessBuilder pb1 = new ProcessBuilder(pathToTabix + "/bgzip",vcfFile.getAbsolutePath().toString());
		ProcessBuilder pb2 = new ProcessBuilder(pathToTabix + "/tabix","-p","vcf",compressedFile.getAbsolutePath().toString());
		
		ArrayList<ProcessBuilder> pbList = new ArrayList<ProcessBuilder>(); 
		pbList.add(pb1);
		pbList.add(pb2);
		
		try {
			for (ProcessBuilder pb: pbList) {
				pb.redirectErrorStream(true);
				Process p = pb.start();
				
				BufferedReader outStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
				StringBuffer sb = new StringBuffer("");
				
				String line = null;
				
				while ((line = outStream.readLine()) != null) {
					sb.append(line + "\n");
				}
				
				int retVal = p.waitFor();
				if (retVal != 0) {
					System.out.println("File compression failed failed: " + pb.command().toString() + ": " + sb.toString());
					throw new Exception("File compression failed failed: " + pb.command().toString() + ": " + sb.toString());
				} else {
					vcfFile.delete();
				}
			}
		} catch (IOException ioex ) {
			System.out.println("Failed to read/write file stream : " + ioex.getMessage());
			throw new Exception("Failed to read/write file stream : " + ioex.getMessage());
		} catch (InterruptedException irex) {
			System.out.println("Gunzip was interruped before it was finished: " + irex.getMessage());
			throw new Exception("Gunzip was interruped before it was finished: " + irex.getMessage());
		} 

	}

}
