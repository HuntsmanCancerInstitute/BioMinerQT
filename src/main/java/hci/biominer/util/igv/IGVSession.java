package hci.biominer.util.igv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**Container and methods to build a text xml doc for driving IGV.
 * Note, chromosome and genome version must be recognized by IGV.*/
public class IGVSession {

	//fields
	private String genomeVersion;
	private String chromosome;
	private int start;
	private int stop;
	private IGVResource[] igvResources;
	
	//constants
	private static final String sessionVersion = "3";
	public static final String igvLaunchURL = "http://www.broadinstitute.org/igv/projects/current/igv.php?sessionURL=";
	public static final String igvGoToURL = "http://localhost:60151/goto?locus=";

	//constructors
	/**Be sure the genomeVersion and chromosome names follow IGV conventions.  Don't forget to add some IGVResources too!*/
	public IGVSession (String igvGenomeVersion, String igvChromosome, int start, int stop){
		this.genomeVersion = igvGenomeVersion;
		this.chromosome = igvChromosome;
		this.start = start;
		this.stop = stop;
	}

	public String getSessionXML(){
		StringBuilder sb = new StringBuilder();
		//xml version
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		//global
		sb.append("<Global genome=\"");
		sb.append(genomeVersion);
		sb.append("\" locus=\"");
		sb.append(chromosome);
		sb.append(":");
		sb.append(start);
		sb.append("-");
		sb.append(stop);
		sb.append("\" version=\"");
		sb.append(sessionVersion);
		sb.append("\" >");
		//resources
		sb.append("\n\t<Resources>\n");
		for (IGVResource r: igvResources){
			sb.append("\t\t");
			r.addXMLResource(sb);
			sb.append("\n");
		}
		//close resources 
		sb.append("\t</Resources>\n");
		
		//make panel info for each
		sb.append("\t<Panel>\n");
		for (IGVResource r : igvResources){
			r.addTrackXML(sb);
		}
		
		sb.append("\t</Panel>\n");
		
		//close global
		sb.append("</Global>");

		return sb.toString();
	}

	/**Returns url to move a running instance of igv to a given chrom and position.*/
	public static String fetchGoToURL(String igvChrom, int start, int stop){
		return igvGoToURL+":"+start+"-"+stop;
	}

	/**Writes the session xml doc to file.*/
	public void writeXMLSession(File xml) throws IOException{
		PrintWriter out = new PrintWriter( new FileWriter (xml));
		out.println(getSessionXML());
		out.close();
	}
	
	
	public URL fetchIGVLaunchURL(URL sessionXMLFile) throws MalformedURLException{
		return new URL(igvLaunchURL + sessionXMLFile.toString());
	}

	//getters and setters
	public String getGenomeVersion() {
		return genomeVersion;
	}
	public void setGenomeVersion(String genomeVersion) {
		this.genomeVersion = genomeVersion;
	}
	public String getChromosome() {
		return chromosome;
	}
	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getStop() {
		return stop;
	}
	public void setStop(int stop) {
		this.stop = stop;
	}
	public IGVResource[] getIgvResources() {
		return igvResources;
	}
	public void setIgvResources(IGVResource[] igvResources) {
		this.igvResources = igvResources;
	}

	/**For testing....*/
	public static void main (String[] args){
		try {
			//for BRCA1  chr17:41,095,285-41,378,498
			IGVSession is = new IGVSession ("hg19", "chr17", 41095285, 41378498 );
			
			//make some resources to load
			IGVResource[] irs = new IGVResource[4];
			irs[0] = new IGVResource("RelRC Morula+", new URL("http://bioserver.hci.utah.edu/IGVSessionsTestData/MOR_Plus.bw"), null, true);
			irs[0].setColor("255,0,0");
			irs[1] = new IGVResource("RelRC Morula-", new URL("http://bioserver.hci.utah.edu/IGVSessionsTestData/MOR_Minus.bw"), new URL("http://en.wikipedia.org/wiki/Morula"), true);
			irs[1].setColor("0,255,0");
			irs[2] = new IGVResource("VCF Clinvar", new URL("http://bioserver.hci.utah.edu/IGVSessionsTestData/clinvar_00-latest.vcf.gz"), new URL("https://www.ncbi.nlm.nih.gov/clinvar/"), false);
			irs[3] = new IGVResource("Bed TSS", new URL("http://bioserver.hci.utah.edu/IGVSessionsTestData/hg19EnsKwnTransTSS500bpMerged.bed.gz"), null, false);
			irs[3].setColor("255,0,255");
			is.setIgvResources(irs);
			
			//write to screen
			//System.out.println(is.getSessionXML());
			
			//write to file
			is.writeXMLSession(new File ("/Users/u0028003/Desktop/testIGVSession.xml"));
			
			//upload file to bioserver scp ~/Desktop/testIGVSession.xml bioserver.hci.utah.edu:/var/www/html/IGVSessionsTestData/
			
			//fetch launch igv URL
			URL sessionFile = new URL("http://bioserver.hci.utah.edu/IGVSessionsTestData/testIGVSession.xml");
			System.out.println(  is.fetchIGVLaunchURL(sessionFile)  );
			
			//go to BRCA2 local host link
			System.out.println(  IGVSession.fetchGoToURL("chr13",32854430,33008995)  );
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
