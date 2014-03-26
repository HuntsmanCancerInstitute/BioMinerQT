package hci.biominer.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CellData {
	private String name = null;
	private int column = -1;
	
	
	public CellData(String name, int column) {
		this.name = name;
		this.column = column;
	}
	
	public int getColumn() {
		return this.column;
	}
	
	public String getName() {
		return this.name;
	}

}

public class CreateBiominerIdDb {
	
	    private File inputFile = null;
	    private File database = null;
	  
	    private ArrayList<CellData> columnNew = new ArrayList<CellData>();
	    private ArrayList<CellData> columnExist = new ArrayList<CellData>();
	    private ArrayList<String> columnNames = new ArrayList<String>();
		private ArrayList<Integer> columnIdxes = new ArrayList<Integer>(); 
	   
	    private HashMap<String,ArrayList<String>> databaseInfo = null;
	    private HashMap<String,HashMap<String,ArrayList<Integer>>> databaseIndex = null;
	    private ArrayList<String> headerList = null;
	    

		public CreateBiominerIdDb(String[] args) {
			System.out.println("Parsing command line arguments");
			this.processArgs(args);
			
			System.out.println("Checking for existing database");
			this.loadDatabase();
			
			if (databaseInfo.size() == 0) {
				System.out.println("Loading information into a new database\n");
				this.loadTableNew();
			} else {
				System.out.println("Loading information into existing database\n");
				this.loadTableExisting();
			}
			
			System.out.println("Writing database");
			this.writeDatabase();
			System.out.println("Finished");
			
		}
		
		public static void main(String[] args) {
			if (args.length == 0) {
				printDocs();
				System.exit(1);
			}
			new CreateBiominerIdDb(args);
		}
		
		public String parseValue(String value) {
			if (value.equals("")) {
				return "null";
			} else {
				return value;
			}
		}
		
		private void loadTableExisting() {
			try {
				BufferedReader br = new BufferedReader(new FileReader(this.inputFile));
				br.readLine(); //slurp header
				
				//Counters
				int totalLines = 0;
				int uniqueLines = 0;
				int matchCount = 0;
				int partialCount = 0;
				int novelCount = 0;
				int multipleCount = 0;
				int updatedCount = 0;
				
				//Get next index
				ArrayList<Integer> ids = new ArrayList<Integer>();
				ArrayList<String> newColumn = new ArrayList<String>();
				for (String v: this.databaseInfo.get("BioMinerID")) {
					ids.add(Integer.parseInt(v));
					newColumn.add("null");
				}
				Collections.sort(ids);
				Collections.reverse(ids);
				int biominerIdx = ids.get(0) + 1;
				
				
				//Add new column names
				for (CellData cn: this.columnNew) {
					headerList.add(cn.getName());
					this.databaseInfo.put(cn.getName(), this.cloneListString(newColumn));
				}
				
				HashSet<String> alreadyProcessed = new HashSet<String>();
				HashSet<Integer> processedRows = new HashSet<Integer>();
				
				String tableLine = null;
				int counter  = 0;
				
				
				while( (tableLine = br.readLine()) != null ) {
					String[] tableItems = tableLine.split("\t",-1);
					
					if (counter % 10000 == 0 && counter != 0) {
						System.out.println(counter);
					}
					counter += 1;
					
					HashMap<String,String> resultHash = new HashMap<String,String>();
					
					//Load data into new entries
					for (CellData cd: this.columnNew) {
						resultHash.put(cd.getName(), this.parseValue(tableItems[cd.getColumn()]));
					}
				
					
					//Determine matching rows for each existing column.
					ArrayList<Integer> possibleMatches = null;
					boolean allNull = true;
					for (CellData cd: this.columnExist) {
						String val = this.parseValue(tableItems[cd.getColumn()]);
						resultHash.put(cd.getName(), val);
						
						ArrayList<Integer> currentMatches = null;
						if (!val.equals("null")) {
							allNull = false;
							if (this.databaseIndex.get(cd.getName()).containsKey(val)) {
								currentMatches = this.cloneListInt(this.databaseIndex.get(cd.getName()).get(val));
							}
						
							if (currentMatches != null) {
								if (possibleMatches == null) {
									possibleMatches = currentMatches;
								} else {
									possibleMatches.retainAll(currentMatches);
								}
							} else {
								possibleMatches = new ArrayList<Integer>();
							}
						} 
					}
					
					if (resultHash.containsKey("Hugo")) {
						if (resultHash.get("Hugo").equals("REXO1L2P")) {
							System.out.println("First encountered: " + tableLine);
						}
					}
					
					
					//Check to see if encountered
					String resultString = "";
					for (String v: resultHash.values()) {
						resultString += v;
					}
					totalLines += 1;
					if (alreadyProcessed.contains(resultString)) {
						continue;
					} else {
						uniqueLines += 1;
						alreadyProcessed.add(resultString);
					}
					
					if (allNull) {
						possibleMatches = null;
					}
					
					
					if ((possibleMatches == null) || (possibleMatches.size() == 0)) { //new match
						novelCount += 1;
						if (possibleMatches != null) {
							partialCount += 1;

						}
						
						//Add biominer index
						this.addToDatabase(resultHash, biominerIdx);
						biominerIdx++;
						
					} else { //existing match
						matchCount += 1;
						if (possibleMatches.size() > 1) {
							multipleCount++;
						}
						
						for (Integer m: possibleMatches) {
							HashMap<String,String> tempResult = this.cloneMapString(resultHash);
							if (processedRows.contains(m)) { //If row was already handled
								for (String cn: this.headerList) { //If existing row has additional information, add to resulthash
									if (tempResult.containsKey(cn) && tempResult.get(cn).equals("null")) {
										tempResult.put(cn, databaseInfo.get(cn).get(m));
									}
								}
								this.addToDatabase(tempResult, biominerIdx); //Create new entry
								biominerIdx++;
							} else {
								boolean updated = false;
								for (String cn: this.headerList) {
									if (resultHash.containsKey(cn)) {
										if (resultHash.get(cn).equals("null")) {
											continue;
										}
										
										if (databaseInfo.get(cn).get(m).equals("null")) {
											updated = true;
										}
										databaseInfo.get(cn).set(m, resultHash.get(cn));
										processedRows.add(m);
									}
								}
								if (updated) {
									updatedCount++;
								}
							}
						}
					}
				}
				
				System.out.println(String.format("There were %d lines in the input file, %d were unique",totalLines,uniqueLines));
				System.out.println(String.format("Matched %d input table entries to existing entries, %d were multiple matches, %d entries were updated",matchCount,multipleCount,updatedCount));
				System.out.println(String.format("Added %d new entries to the database, %d were partial matches",novelCount,partialCount));
				
				br.close();
			} catch (IOException ioex) {
				System.out.println("Error reading input table, exiting: " + ioex.getMessage());
				System.exit(1);
			}
		}
		
		private void addToDatabase(HashMap<String,String> resultHash,int biominerIdx) {
			//Add biominer index
			resultHash.put("BioMinerID", String.valueOf(biominerIdx));
			
			for (String cn: this.headerList) {
				if (resultHash.containsKey(cn)) {
					databaseInfo.get(cn).add(resultHash.get(cn));
				} else {
					databaseInfo.get(cn).add(null);
				}
			}
		}
		
		
		private void loadTableNew() {
			try {
				//Initialize reader
				BufferedReader br = new BufferedReader(new FileReader(this.inputFile));
				br.readLine(); //slurp header
				
				//Initialize counters
				int totalLines = 0;
				int uniqueLines = 0;
				int biominerIdx = 0;
				
				//Create BioMinerID column
				headerList.add("BioMinerID");
				this.databaseInfo.put("BioMinerID",new ArrayList<String>());
				
				//Create data columns
				for (CellData cn: this.columnNew) {
					headerList.add(cn.getName());
					this.databaseInfo.put(cn.getName(), new ArrayList<String>());
				}
				
				//Stores already-encounted information
				HashSet<String> alreadyProcessed = new HashSet<String>();
				
				//Parse the table
				String tableLine = null;
				while( (tableLine = br.readLine()) != null ) {
					String[] tableItems = tableLine.split("\t",-1);
					
					HashMap<String,String> resultHash = new HashMap<String,String>();
					
					//Load data into new entries
					for (CellData cd: this.columnNew) {
						resultHash.put(cd.getName(), this.parseValue(tableItems[cd.getColumn()]));
					}
					
					//Check to see if encountered
					String resultString = "";
					for (String v: resultHash.values()) {
						resultString += v;
					}
					totalLines += 1;
					if (alreadyProcessed.contains(resultString)) {
						continue;
					} else {
						uniqueLines += 1;
						alreadyProcessed.add(resultString);
					}
					resultHash.put("BioMinerID",String.valueOf(biominerIdx));
					biominerIdx++;
					
					for (String cn: this.headerList) {
						databaseInfo.get(cn).add(resultHash.get(cn));
					}
					
				}
				
				System.out.println(String.format("There were %d lines in the input file, %d were unique",totalLines,uniqueLines));
				System.out.println(String.format("Added %d new entries to the database",uniqueLines));
				
				br.close();
			} catch (IOException ioex) {
				System.out.println("Error reading input table, exiting: " + ioex.getMessage());
				System.exit(1);
			}
		}
		
		
		private void writeDatabase() {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(this.database));
				
				String headerString = "";
				for (String h: this.headerList) {
					headerString += "\t" + h;
				}
				headerString = headerString.substring(1) + "\n";
				
				bw.write(headerString);
				
				for(int i=0;i<this.databaseInfo.get(headerList.get(0)).size();i++) {
					String outString = "";
					
					for (String h: this.headerList) {
						outString += "\t" + this.databaseInfo.get(h).get(i);
					}
					outString = outString.substring(1) + "\n";
					
					bw.write(outString);
				}
				
				bw.close();
			} catch (IOException ioex) {
				System.out.println("Error writing database file: " + ioex.getMessage());
				System.exit(1);
				
			}
		}
		
		private void processArgs(String[] args){
			String columnNameString = null;
			String columnIndexString = null;
			
			Pattern pat = Pattern.compile("-[a-z]");
			for (int i = 0; i<args.length; i++){
				String lcArg = args[i].toLowerCase();
				Matcher mat = pat.matcher(lcArg);
				if (mat.matches()){
					char test = args[i].charAt(1);
					try{
						switch (test){
							case 'i': this.inputFile = new File(args[++i]); break;
							case 'd': this.database = new File(args[++i]); break;
							case 'n': columnNameString = args[++i]; break;
							case 'x': columnIndexString = args[++i]; break;
							case 'h': printDocs(); System.exit(0);
							default: System.out.println("\nProblem, unknown option! " + mat.group());
						}
					}
					catch (Exception e){
						System.out.print("\nSorry, something doesn't look right with this parameter request: -"+test);
						System.out.println();
						System.exit(0);
					}
				}
			}
			
			//Make sure input data exists
			if (this.inputFile == null) {
				System.out.println("The input table file was not specified.  Please re-run app with the appropriate paramters.");
				System.exit(1);
			}
			
			if (!this.inputFile.exists()) {
				System.out.println(String.format("Specified input file does not exist: %s",this.inputFile.getAbsolutePath()));
				System.exit(1);
			}
			
			String[] exampleRow = null;
			try {
				BufferedReader br = new BufferedReader(new FileReader(this.inputFile));
				br.readLine(); // skip header, some USeq apps have more header tokens than body tokens
				exampleRow = br.readLine().split("\t");
				br.close();
			} catch (IOException ioex ) {
				System.out.println("Error reading input table: \n" + ioex.getMessage());
				System.exit(1);
			}
			
			//Make sure database exists
			if (database == null) {
				System.out.println("The database was not specified.  Please re-run app with the appropriate paramters.");
				System.exit(1);
			}
			
			
			//Load up column names / indexes
			String[] tempNames = columnNameString.split(",");
			String[] tempIdxes = columnIndexString.split(",");
			if (tempNames.length != tempIdxes.length) {
				System.out.println("The column name list length does not match the column index list length.");
				System.exit(1);
			}
			
			for (String tn: tempNames) {
				columnNames.add(tn);
			}
			
			for (String ti: tempIdxes) {
				int idx = Integer.parseInt(ti)-1;
				if (exampleRow.length <= idx) {
					System.out.println(String.format("The column index specified (%d) is greater than the number of rows in the table (%d), exiting",idx,exampleRow.length));
					System.exit(1);
				}
				columnIdxes.add(idx);
			}
			
			
			
		} 
		
		private void loadDatabase() {
			//Load the database
			this.databaseInfo = new HashMap<String,ArrayList<String>>();
			this.databaseIndex = new HashMap<String,HashMap<String,ArrayList<Integer>>>();
		
			if (database.exists()) {
				System.out.println("Loading database..."); 
				
				//Load database
				try {
					BufferedReader br = new BufferedReader(new FileReader(this.database));
					
					//Attempt to parse header
					this.headerList = new ArrayList(Arrays.asList(br.readLine().split("\t")));
					for (String headerName: headerList) {
						this.databaseInfo.put(headerName, new ArrayList<String>());
					}
					
					//Load rows 
					String tempdb = null;
					while ((tempdb = br.readLine()) != null) {
						String[] items = tempdb.split("\t",-1);
						
						for (int i=0;i<items.length;i++) {
							this.databaseInfo.get(this.headerList.get(i)).add(items[i]);
						}
					}
					
					br.close();
				} catch (IOException ioex) {
					System.out.println("Error reading database files!");
					ioex.printStackTrace();
					System.exit(1);
				}
				
				//Organize columns into new/existing
				for (int i=0;i<columnNames.size();i++) {
				    String cn = columnNames.get(i);
				    int ci = columnIdxes.get(i);
				    System.out.println(cn);
					if (this.databaseInfo.containsKey(cn)) {
						CellData cd = new CellData(cn,ci);
						this.columnExist.add(cd);
					} else {
						CellData cd = new CellData(cn,ci);
						this.columnNew.add(cd);
					}
				}
				
				if (this.columnExist.size() == 0) {
					System.out.println("None of the column names match existing column names in database! If you want to create a new database,"
							+ " don't specify an existing database name");
					System.exit(1);
				}
				
				this.databaseIndex = new HashMap<String,HashMap<String,ArrayList<Integer>>>();
				for (CellData cd: this.columnExist) {
					HashMap<String,ArrayList<Integer>> idx = new HashMap<String,ArrayList<Integer>>();
					
					ArrayList<String> colData = this.databaseInfo.get(cd.getName());
					for (int i=0; i<colData.size();i++) {
						String val = colData.get(i);
						if (!idx.containsKey(val)) {
							idx.put(val, new ArrayList<Integer>());	
						} 
						idx.get(val).add(i);
					}
					this.databaseIndex.put(cd.getName(), idx);
				}
				
			} else {
				System.out.println("\nThe specified databaes does not exist, creating a new database");
				
				this.headerList = new ArrayList<String>();
				
				for (int i=0;i<columnNames.size();i++) {
				    String cn = columnNames.get(i);
				    int ci = columnIdxes.get(i);
				
					CellData cd = new CellData(cn,ci);
					this.columnNew.add(cd);
				}
				
				if (this.columnNew.size() == 0) {
					System.out.println("There aren't any columns in your input table");
					System.exit(1);
				}
			}
		}
		
		private static void printDocs(){
			System.out.println("\n" +
					"**************************************************************************************\n" +
					"**                                  CreateBiominerIdDb                              **\n" +
					"**************************************************************************************\n" +
					"CreateBiominerIdDb creates/updates the biominer id database. Users specify an input \n" +
					"table and the columns they want to import.  Columns that already exist in the db are \n" +
					"used in matching and new columns are added.  If an existing column has no value in the \n" +
					"database and a value in input table, the database will be updated.  Partially matching\n" +
					"rows and new rows are appended to the database.\n" +
					
					"\nNotes:\n" +
					"1) It isn't necessary to eliminate duplicate rows in the input table, the app should\n" +
					"     handle this.\n" +
					"2) If the database doesn't exist, a new one will be created.  If the database exsits\n" +
					"     and none of the column names match, the program will exit.\n" +
					
					"\nRequired:\n" +
					"-i Input Table.  Currently only supports reading tab-delimited files\n" +
					"-d Database name. Path to database\n" +
					"-n Column names. Comma separated list of column names\n" +
					"-x Column indexes (1-based). Comma separated list of column indexes that correspond \n" +
					"     with names\n" +
					
					"\nExample: java -Xmx2g -jar CreateBiominerIdDb -i Ensembl74.txt -d /path/to/database\n" +
					"         -n Ensembl74,Entrez,Hugo -i 1,3,5\n" +
					

					"**************************************************************************************\n");
		}
		
		public ArrayList<Integer> cloneListInt(ArrayList<Integer> l) {
			ArrayList<Integer> newList = new ArrayList<Integer>();
			for (Integer i: l) {
				newList.add(i);
			}
			return newList;
		}
		
		public ArrayList<String> cloneListString(ArrayList<String> l) {
			ArrayList<String> newList = new ArrayList<String>();
			for (String i: l) {
				newList.add(i);
			}
			return newList;
		}
		
		public HashMap<String,String> cloneMapString(HashMap<String,String> h) {
			HashMap<String,String> temp = new HashMap<String,String>();
			for (String k: h.keySet()) {
				temp.put(k,h.get(k));
			}
			return temp;
		}
		
		

}
