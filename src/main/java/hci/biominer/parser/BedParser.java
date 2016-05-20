package hci.biominer.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import hci.biominer.model.genome.Genome;
import hci.biominer.util.ColumnValidators;
import hci.biominer.util.ModelUtil;

public class BedParser {
	private File inputFile = null;
	private File outputFile = null;
	private Genome genomeBuild = null;
	private Boolean isConverted = null;
	private ArrayList<String> parsedData = null;
	private String warningMessage = "";
	
	/* 
	 * Constructor for internal call. Assumes 0-based indexes.
	 */
	public BedParser(File inputFile, File outputFile, Genome build, Boolean isConverted) throws Exception {
		
		this.genomeBuild = build;
		
		if (!inputFile.exists()) {
			throw new Exception("[ChipParser] Specified input file does not exist");
		}
		this.isConverted = true;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		
	}
	

	public String run() throws Exception {
		//Process bed file
		this.processData();
		
		//Write output
		this.writeData();
		
		return this.warningMessage;

	}
	

	/*
	 * Write minimum format Chip file
	 */
	private void writeData() throws Exception{
		BufferedWriter bw = null;
		
		
		try {
			//Open file handle
			GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(this.outputFile));
			bw = new BufferedWriter(new OutputStreamWriter(zip));
			
			//Write data
			for (String line: this.parsedData) {
				bw.write(line);
			}
			
			//Close handle
			bw.close();
		} catch (IOException ioex) {
			throw new IOException(String.format("Error writing to output file file: %s.",ioex.getMessage()));
		} finally {
			try {
				bw.close();
			} catch (Exception e){}
		}
	}
	
	/*
	 * Parse and validate Chip file
	 */
	private void processData() throws Exception {
		BufferedReader br = null;
		try {
			//Open file handle
			br = ModelUtil.fetchBufferedReader(this.inputFile);
			
			//Read header and make sure there are six lines
			String[] header = br.readLine().split("\t");
			int headLength = header.length;
			if (headLength != 6) {
				throw new Exception("The loaded file does not contain six columns. The bed file parser expects a six column bed file.\n");
			}
			
			//Initialize input file variables
			this.parsedData = new ArrayList<String>();
			String line = null;
			int lineCount = 1;
			
			//Initialize warning variables
			boolean allTransformedFdrLessThanOne = true;
			

			NumberFormat formatter = new DecimalFormat("0.##E0");
			
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t");
				
				//Initialize line variables
				String tempChrom;
				int tempStart = -1;
				int tempEnd = -1;
				double tempFdr = -1;
				
				//Make sure the line length matches header. Parsing might go OK if this fails, but I 
				//think it's best to error out when the file is at all malformed.
				if (parts.length != headLength) {
					throw new Exception(String.format("The number of columns in row %d ( %d ) doesn't match the header ( %d )\n",
							lineCount,parts.length,headLength));
				}
				
				//Parse chromsome, check to make sure it's recognizable
				tempChrom = ColumnValidators.validateChromosome(this.genomeBuild, parts[0]);
				
				//Parse start position, make sure it's an integer and within boundaries
				tempStart = ColumnValidators.validateCoordiate(this.genomeBuild, tempChrom, parts[1]);
				
				//Parse end position, make sure it's an integer and within boundaries
				tempEnd = ColumnValidators.validateCoordiate(this.genomeBuild, tempChrom, parts[2]);
			
				//Check to sure end is greater than start
				if (tempEnd <= tempStart) {
					throw new Exception(String.format("Region end %d is less than or equal to region start %d, exiting.",tempEnd,tempStart));
				}
				
				//Parse FDR
				tempFdr = ColumnValidators.validateFdr(parts[4], this.isConverted);
					
				if (this.isConverted && tempFdr > 1) {
					allTransformedFdrLessThanOne = false;
				}
				
				if (this.isConverted) {
					double decimal = tempFdr / -10;
					tempFdr = Math.pow(10, decimal);
				}
				
				String stringFdr = formatter.format(tempFdr);
				
			
				//Create output string and add to data list
				String outputFile = String.format("%s\t%d\t%d\t%s\n",tempChrom,tempStart,tempEnd,stringFdr);
				this.parsedData.add(outputFile);
				
				lineCount++;
			}
			
			//Throw failure message or warning messages
		    if (allTransformedFdrLessThanOne) {
		    	this.warningMessage = "WARNING: FDR formatting style was set as transformed, but all values were between 0 and 1.  Are you sure the FDR values were transformed? "
						+ "FDR values are transformed using the formula -10 * log10(FDR)";		
			}
			
		} catch (IOException ioex) {
			throw new IOException(String.format("Error processing data file: %s.",ioex.getMessage()));
		} finally {
			try {
				br.close();
			} catch (Exception e){}
		}
		
	}
	
	
	
	
	/*
	 * Command line start
	 */
//	public static void main(String[] args) {
//		if (args.length == 0) {
//			printDocs();
//			System.exit(1);
//		}
//		new ChipParser(args);
//	}
//	
//	/*
//	 * Help docs
//	 */
//	private static void printDocs(){
//		System.out.println("\n" +
//				"**************************************************************************************\n" +
//				"**                                  ChipParserCommand                               **\n" +
//				"**************************************************************************************\n" +
//				"ChipParserCommand reads in ChIPSeq data from a tab-delimited text file.  The user \n" +
//				"specifies the locations of necessary columns and the application reads in the data. \n" +
//				"The application tries to identify errors as it's reading in the data.  If everything \n" +
//				"looks kosher, the data is written out to a text file.\n\n" +
//				
//				"\nNotes:\n" +
//				"1) This application assumes here is a header. If your input file does not have\n " +
//				"a header the first line will be skipped.\n" +
//				"2) This application checks the number of columns using the second line of the input\n" +
//				"file.  The USeq header lines often contain a extra column with DAS2 information.\n" +
//				"3) This application only handles 'standard' chromosomes.\n\n" +
//				
//				"\nRequired:\n" +
//				"-i Input file.  Currently only supports reading tab-delimited files\n" +
//				"-b Genome Build. Currently, the only build supported is hg19.\n" +
//				"-o Output File.  Parsed output file.\n"+
//				"-c Chromosome column index. (1-based)\n" +
//				"-s Region start column index. (1-based)\n" +
//				"-e Region end column index. (1-based)\n" +
//				"-f FDR column index. (1-based)\n" +
//				"-l Log2Ratio column index. (1-based)\n" +
//				
//				"\nOptional:\n" +
//				"-a FDR is already converted to -10 * log10(FDR)\n" +
//				
//				"\n" +
//				"Example: java -Xmx500M -jar path/to/ChipParserCommand -i 10594R.chip.txt -b hg19\n" +
//				"      -o 10594R.parsedChip.txt -c 2 -s 3 -e 4 -f 10 -l 12\n" +
//				
//
//				"**************************************************************************************\n");
//	}
//	
//
//	
//	/*
//	 * Process command line arguments
//	 */
//	private void processArgs(String[] args){
//		File transcriptomeFile = null;
//		
//		Pattern pat = Pattern.compile("-[a-z]");
//		for (int i = 0; i<args.length; i++){
//			String lcArg = args[i].toLowerCase();
//			Matcher mat = pat.matcher(lcArg);
//			if (mat.matches()){
//				char test = args[i].charAt(1);
//				try{
//					switch (test){
//					
//					case 'i': this.inputFile = new File(args[++i]); break;
//					case 'b': transcriptomeFile = new File(args[++i]); break;
//					case 'o': this.outputFile = new File(args[++i]); break;
//					case 'c': this.chromColumn = Integer.parseInt(args[++i])-1; break;
//					case 's': this.startColumn = Integer.parseInt(args[++i])-1; break;
//					case 'e': this.endColumn = Integer.parseInt(args[++i])-1; break;
//					case 'f': this.fdrColumn = Integer.parseInt(args[++i])-1; break;
//					case 'l': this.logColumn = Integer.parseInt(args[++i])-1; break;
//					case 'a': this.isConverted = true; break;
//					
//					case 'h': printDocs(); System.exit(0);
//					default: System.out.println("\nProblem, unknown option! " + mat.group());
//					}
//				}
//				catch (Exception e){
//					System.out.print("\n[ChipParser] Sorry, something doesn't look right with this parameter request: -"+test);
//					System.out.println();
//					System.exit(0);
//				}
//			}
//		}
//		
//		
//		if (transcriptomeFile == null) {
//			System.out.println("[ChipParser] The genome build wasn't specified, please re-run the app with the appropriate parameters\n");
//			System.exit(1);
//		}
//		
//		if (!transcriptomeFile.exists()) {
//			System.out.println("[ChipParser] The transcriptome file does not exist, please re-run the app with the appropriate parameters");
//			System.exit(1);
//		}
//		
//		try {
//			
//			GenomeParser gp = new GenomeParser(transcriptomeFile);
//			this.genomeBuild = gp.getGenome();
//		} catch (Exception ex) {
//			System.out.println("[ChipParser] Error parsing genome file: " + ex.getMessage());
//			System.exit(1);
//		}
//
//		//Make sure the file are formed properly
//		if (this.inputFile ==null){
//			System.out.println("[ChipParser] Input file ( -i ) not specified, please re-run the app with the appropriate parameters.\n");
//			System.exit(1);
//		}
//		
//		if (!this.inputFile.exists()) {
//			System.out.println(String.format("[ChipParser] Specified input file does not exist: %s\n",this.inputFile.getAbsolutePath()));
//			System.exit(1);
//		}
//		
//		if (this.outputFile == null) {
//			System.out.println("[ChipParser] Output file ( -o ) not specified, please re-run the app with the approprate paramaters.\n");
//			System.exit(1);
//		}
//		
//		//Slurp header for number of columns:
//		Integer[] colsToCheck = new Integer[]{this.chromColumn,this.startColumn,this.endColumn,this.fdrColumn,this.logColumn};
//		String[] colNames = new String[] {"Chromsome Column","Start Column","End Column","FDR Column","Log Column"};
//		try {
//			colMax = ColumnValidators.validateColumns(colsToCheck, colNames, this.inputFile);
//		} catch (Exception ex) {
//			System.out.println(ex.getMessage());
//			ex.printStackTrace();
//		}
//		
//	}
//	


}
