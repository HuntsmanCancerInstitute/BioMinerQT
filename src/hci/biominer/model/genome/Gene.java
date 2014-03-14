package hci.biominer.model.genome;

import java.util.ArrayList;
import java.util.HashMap;

public class Gene {
	
	//fields
	private String name;
	private Transcript[] transcripts;
	private Transcript mergedTranscript;
	private boolean plusStrand;
	
	//methods
	public void makeMergedTranscript() {
		if (transcripts.length == 1) mergedTranscript = transcripts[0];
		else {
			mergedTranscript = transcripts[0].getPartialClone();
			for (int i=1; i< transcripts.length; i++){
				Region[] merged = Region.merge(mergedTranscript.getExons(), transcripts[i].getExons());
				mergedTranscript.setExons(merged);
					//reset tx start and stop
					if (mergedTranscript.getTxStart() > transcripts[i].getTxStart()) mergedTranscript.setTxStart(transcripts[i].getTxStart());
					if (mergedTranscript.getTxEnd() < transcripts[i].getTxEnd()) mergedTranscript.setTxEnd(transcripts[i].getTxEnd());
					//reset cds start stop
					if (mergedTranscript.getCdsStart() > transcripts[i].getCdsStart()) mergedTranscript.setCdsStart(transcripts[i].getCdsStart());
					if (mergedTranscript.getCdsEnd() < transcripts[i].getCdsEnd()) mergedTranscript.setCdsEnd(transcripts[i].getCdsEnd());
			}
			
			mergedTranscript.makeIntrons();
			mergedTranscript.setTss();
			mergedTranscript.setTranscriptName("merge");
		}
		
	}
	
	public static Gene makeGene(ArrayList<String[]> lines, HashMap<String, Integer> chrNameLength) throws Exception{
		int num = lines.size();
		Gene gene = new Gene();
		
		//make transcripts
		Transcript[] transcripts = new Transcript[num];
		transcripts[0] = new Transcript(lines.get(0), chrNameLength);
		boolean plusStrand = transcripts[0].isPlusStand();
		for (int i=1; i< num; i++){
			transcripts[i] = new Transcript(lines.get(i), chrNameLength);
			if (plusStrand != transcripts[i].isPlusStand()) throw new Exception ("\nError: strand differs between transcripts for " +transcripts[0].getGeneName());
		}
		
		//populate gene fields
		gene.setTranscripts(transcripts);
		gene.setName(transcripts[0].getGeneName());
		gene.setPlusStrand(plusStrand);
		
		//trigger creation of mergedTranscript
		gene.makeMergedTranscript();
		
		return gene;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(name); sb.append("\n");
		sb.append(plusStrand); sb.append("\n");
		sb.append(mergedTranscript.toString()); sb.append("\n");
		for (Transcript t: transcripts){
			sb.append(t.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	//getters and setters
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Transcript[] getTranscripts() {
		return transcripts;
	}
	public void setTranscripts(Transcript[] transcripts) {
		this.transcripts = transcripts;
	}
	public Transcript getMergedTranscript() {
		return mergedTranscript;
	}
	public void setMergedTranscript(Transcript mergedTranscript) {
		this.mergedTranscript = mergedTranscript;
	}
	public boolean isPlusStrand() {
		return plusStrand;
	}
	public void setPlusStrand(boolean plusStrand) {
		this.plusStrand = plusStrand;
	}

	
}
