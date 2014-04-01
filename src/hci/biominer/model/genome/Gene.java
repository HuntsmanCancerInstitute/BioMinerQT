package hci.biominer.model.genome;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Gene implements Serializable {
	
	//fields
	private String name;
	private Chromosome chromosome;
	private Transcript[] transcripts;
	private Transcript mergedTranscript;
	private boolean plusStrand;
	private static final long serialVersionUID = 1L;
	
	//methods
	
	/**Returns all the 5' Exons from the transcripts, relative to the gene, not genome.*/
	public Region[] get5Exons(){
		Region[] e = new Region[transcripts.length];
		//plus strand?
		if (plusStrand){
			for (int i=0; i< transcripts.length; i++){
				e[i] = transcripts[i].getExons()[0];
			}
		}
		else {
			for (int i=0; i< transcripts.length; i++){
				int end = transcripts[i].getExons().length -1;
				e[i] = transcripts[i].getExons()[end];
			}
		}
		return e;
	}
	
	/**Returns all the 3' Exons from the transcripts, relative to the gene, not genome.*/
	public Region[] get3Exons(){
		Region[] e = new Region[transcripts.length];
		//plus strand?
		if (plusStrand == false){
			for (int i=0; i< transcripts.length; i++){
				e[i] = transcripts[i].getExons()[0];
			}
		}
		else {
			for (int i=0; i< transcripts.length; i++){
				int end = transcripts[i].getExons().length -1;
				e[i] = transcripts[i].getExons()[end];
			}
		}
		return e;
	}

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
				//add name
				mergedTranscript.setTranscriptName(mergedTranscript.getTranscriptName()+ "+" + transcripts[i].getTranscriptName());
			}

			mergedTranscript.makeIntrons();
			mergedTranscript.setTss();
		}
	}
	

	
	public static Gene makeGene(ArrayList<String[]> lines, LinkedHashMap<String, Chromosome> nameChromosome) throws Exception{
		int num = lines.size();
		Gene gene = new Gene();
		
		//make transcripts
		Transcript[] transcripts = new Transcript[num];
		transcripts[0] = new Transcript(lines.get(0), nameChromosome);
		boolean plusStrand = transcripts[0].isPlusStand();
		for (int i=1; i< num; i++){
			transcripts[i] = new Transcript(lines.get(i), nameChromosome);
			if (plusStrand != transcripts[i].isPlusStand()) throw new Exception ("\nError: strand differs between transcripts for " +transcripts[0].getGeneName());
		}
		
		//populate gene fields
		gene.setTranscripts(transcripts);
		gene.setName(transcripts[0].getGeneName());
		gene.setPlusStrand(plusStrand);
		gene.setChromsome(nameChromosome.get(transcripts[0].getChrom()));
		
		//trigger creation of mergedTranscript
		gene.makeMergedTranscript();
		
		return gene;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("GeneName:\t" +name); sb.append("\n");
		sb.append("IsPlusStrand:\t" +plusStrand); sb.append("\n");
		sb.append("MergedTranscript:\t" +mergedTranscript.toString()); sb.append("\n");
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
	private void setChromsome(Chromosome chromosome) {
		this.chromosome = chromosome;
	}
	public Chromosome getChromosome() {
		return chromosome;
	}
	/**@return + or -*/
	public char getStrand(){
		if (plusStrand) return '+';
		return '-';
	}

	
}
