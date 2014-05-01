package hci.biominer.model.genome;

import hci.biominer.model.intervaltree.Interval;
import hci.biominer.model.intervaltree.IntervalTree;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Transcriptome implements Serializable {

	//fields
	private String name;
	private Gene[] genes;
	private File sourceFile;
	private Genome genome;
	private HashMap<String, Gene> geneNameGene;
	private HashMap<String, IntervalTree<Gene>> chromGeneIntervalTrees = null;
	private HashMap<String, IntervalTree<Gene>> chromGene100KIntervalTrees = null;
	private HashMap<String, IntervalTree<Transcript>> chromTSS100KIntervalTrees = null;
	private static final long serialVersionUID = 1L;

	//constructors
	public Transcriptome() {}

	public Transcriptome(String transName, File transFile) {
		this.name = transName;
		this.sourceFile = transFile;
	}

	//methods
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("TranscriptomeName:\t"+ name); sb.append("\n");
		sb.append("TranscriptomeFile:\t"+ sourceFile); sb.append("\n");
		sb.append("NumberGenes:\t"+ genes.length); sb.append("\n");
		sb.append("\nFirst 5:\n"); 
		for (int i=0; i< 5; i++) sb.append(genes[i]);
		return sb.toString();
	}

	/**@return HashMap of gene name : Gene .  Triggers HashMap generation if needed.  Otherwise returns cached.*/
	public HashMap<String, Gene> getGeneNameGene() {
		if (geneNameGene == null){
			geneNameGene = new HashMap<String, Gene>();
			for (Gene g : genes) geneNameGene.put(g.getName(), g);
		}
		return geneNameGene;
	}
	
	/**Splits the genes by chromosome*/
	public HashMap<String, ArrayList<Gene>> getChromGeneArrayLists(){
		HashMap<String, ArrayList<Gene>> hm = new HashMap<String, ArrayList<Gene>>();
		for (int i=0; i< genes.length; i++){
			String chrom = genes[i].getChromosome().getName();
			ArrayList<Gene> al = hm.get(chrom);
			if (al == null){
				al = new ArrayList<Gene>();
				hm.put(chrom, al);
			}
			al.add(genes[i]);
		}
		return hm;
	}

	/**Lazy loading of interval trees for neighboring gene lookup.*/
	public HashMap<String, IntervalTree<Gene>> getChromGeneIntervalTrees(){
		
		if (chromGeneIntervalTrees == null){			
			//make hashes
			chromGeneIntervalTrees = new HashMap<String, IntervalTree<Gene>>();
			chromGene100KIntervalTrees = new HashMap<String, IntervalTree<Gene>>();
			chromTSS100KIntervalTrees = new HashMap<String, IntervalTree<Transcript>>();

			//fetch chromosome name : Chromosome for alias lookup
			LinkedHashMap<String, Chromosome> nameChromosome = genome.getNameChromosome();

			//for each chromosome of genes
			HashMap<String, ArrayList<Gene>> chromGenes = getChromGeneArrayLists();
			for (String chr: chromGenes.keySet()){				
				//fetch chrom specific info
				ArrayList<Gene> genes = chromGenes.get(chr);
				ArrayList<Interval<Gene>> intervalAL = new ArrayList<Interval<Gene>>();
				ArrayList<Interval<Gene>> intervalAL100K = new ArrayList<Interval<Gene>>();
				ArrayList<Interval<Transcript>> intervalAL100KTSS = new ArrayList<Interval<Transcript>>();
				
				int num = genes.size();
				int chrLength = nameChromosome.get(chr).getLength();
				
				//for each gene
				for (int i= 0; i< num; i++){
					//create intervals and add to ArrayLists
					Gene gene = genes.get(i);
					Transcript merge = gene.getMergedTranscript();
					
					//create interval for normal gene intersection
					intervalAL.add(new Interval<Gene> (merge.getTxStart(), merge.getTxEnd(), gene));
					
					//create interval for extended gene intersection, does it matter if it runs off the end?
					int start = merge.getTxStart() - 100000;
					if (start < 0) start = 0;
					int stop = merge.getTxEnd() + 100000;
					if (stop > chrLength ) stop = chrLength;
					intervalAL100K.add(new Interval<Gene> (start, stop, gene));
					
					//for all TSS
					for (Transcript t: gene.getTranscripts()){
						start = t.getTss() - 100000;
						if (start < 0) start = 0;
						stop = t.getTss() + 100000;
						if (stop > chrLength ) stop = chrLength;
						intervalAL100KTSS.add(new Interval<Transcript> (start, stop, t));
					}
				}
				
				//make IntervalTrees 
				IntervalTree<Gene> it = new IntervalTree<Gene>(intervalAL, false);
				IntervalTree<Gene> it100K = new IntervalTree<Gene>(intervalAL100K, false);
				IntervalTree<Transcript> it100KTSS = new IntervalTree<Transcript>(intervalAL100KTSS, false);
				//fetch chrom name aliases
				String[] aliases = nameChromosome.get(chr).getAliases();
				for (String name: aliases) {
					chromGeneIntervalTrees.put(name, it);
					chromGene100KIntervalTrees.put(name, it100K);
					chromTSS100KIntervalTrees.put(name, it100KTSS);
				}
				
			}
		}
		return chromGeneIntervalTrees;
	}
	
	/**Lazy loading of interval tree for extended gene neighbor search.*/
	public HashMap<String, IntervalTree<Gene>> getChromGene100KIntervalTrees(){
		if (chromGene100KIntervalTrees == null) getChromGeneIntervalTrees();
		return chromGene100KIntervalTrees;
	}
	
	/**Lazy loading of interval tree for extended TSS search.*/
	public HashMap<String, IntervalTree<Transcript>> getChromTSS100KIntervalTrees(){
		if (chromGene100KIntervalTrees == null) getChromGeneIntervalTrees();
		return chromTSS100KIntervalTrees;
	}

	//getters and setters
	public String getName() {
		return name;
	}
	public Gene[] getGenes() {
		return genes;
	}
	public void setGenes(Gene[] genes) {
		this.genes = genes;
	}
	public File getSourceFile() {
		return sourceFile;
	}
	public void setSourceFile(File sourceFile) {
		this.sourceFile = sourceFile;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setGenome(Genome genome){
		this.genome = genome;
	}
	public Genome getGenome() {
		return genome;
	}



}
