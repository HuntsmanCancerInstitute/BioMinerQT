package hci.biominer.parser;

import hci.biominer.model.BiominerGene;
import hci.biominer.model.ExternalGene;
import hci.biominer.util.ModelUtil;
import hci.biominer.util.ParsedAnnotation;
import hci.biominer.model.OrganismBuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class AnnotationFileParser {
	private File inputFile = null;
	private OrganismBuild ob = null;
	private Long bmIdx;
	private Integer refseqIdx;
	private Integer hugoIdx;
	private Integer ensemblIdx;
	private Integer ucscIdx;
	
	public AnnotationFileParser(OrganismBuild ob, File inputFile, Integer ensemblIdx, Integer hugoIdx, Integer refseqIdx, Integer ucscIdx, Long bmIdx) {
		this.ob = ob;
		this.bmIdx = bmIdx;
		this.inputFile = inputFile;
		this.hugoIdx = hugoIdx;
		this.ensemblIdx = ensemblIdx;
		this.ucscIdx = ucscIdx;
		this.refseqIdx = refseqIdx;
	}

		
	public ParsedAnnotation run() throws Exception {
		ParsedAnnotation pa = new ParsedAnnotation();
		HashMap<String,ArrayList<Gene>> observed = new HashMap<String,ArrayList<Gene>>();
		
		
		try {
			BufferedReader br = ModelUtil.fetchBufferedReader(this.inputFile);
			br.readLine(); //slurp header
			
			String line = null;
			
			
			while ((line = br.readLine()) != null) {
				String[] items = line.split("\t",-1); //Get tokens
			
				Gene newGene = new Gene();
				
				if (this.ensemblIdx != null) {
					if (this.ensemblIdx >= items.length) {
						throw new Exception(String.format("The specified ensembl index (%d) is greater than the number of columns (%d).",this.ensemblIdx,items.length));
					}
					newGene.setEnsembl(parseValue(items[this.ensemblIdx]));
				}
				if (this.hugoIdx != null) {
					if (this.hugoIdx >= items.length) {
						throw new Exception(String.format("The specified hugo index (%d) is greater than the number of columns (%d).",this.hugoIdx,items.length));
					}
					newGene.setHugo(parseValue(items[this.hugoIdx]));
				}
				if (this.refseqIdx != null) {
					if (this.refseqIdx >= items.length) {
						throw new Exception(String.format("The specified refseq index (%d) is greater than the number of columns (%d).",this.refseqIdx,items.length));
					}
					newGene.setRefseq(parseValue(items[this.refseqIdx]));
				}
				if (this.ucscIdx != null) {
					if (this.ucscIdx >= items.length) {
						throw new Exception(String.format("The specified ucsc index (%d) is greater than the number of columns (%d).",this.ucscIdx,items.length));
					}
					newGene.setUcsc(parseValue(items[this.ucscIdx]));
				}
				
				if (!observed.containsKey(newGene.getEnsembl())) {
					observed.put(newGene.getEnsembl(), new ArrayList<Gene>());
					observed.get(newGene.getEnsembl()).add(newGene);
				} else {
					boolean match = true;
					for (Gene g: observed.get(newGene.getEnsembl())) {
						if (!g.compare(newGene)) {
							match = false;
						}
					}
					
					if (!match) {
						observed.get(newGene.getEnsembl()).add(newGene);
					}
				}
			}
			
			ArrayList<BiominerGene> biominerGeneList = new ArrayList<BiominerGene>();
			ArrayList<ExternalGene> externalGeneList = new ArrayList<ExternalGene>();
			for (String name: observed.keySet()) {
				for (Gene g: observed.get(name)) {
					String biominerName = "BM" + String.valueOf(this.bmIdx);
					this.bmIdx += 1;
					BiominerGene bg = new BiominerGene(biominerName);
					biominerGeneList.add(bg);
					externalGeneList.addAll(g.getExternalGenes(this.ob, bg));
				}
			}
			
			pa.setBiominerGenes(biominerGeneList);
			pa.setExternalGenes(externalGeneList);
			
			br.close();
		} catch (IOException ioex) {
			throw new Exception(ioex.getMessage());
		}
		
		return pa;
	}
	
	private String parseValue(String value) {
		if (value.equals("")) {
			return null;
		} else {
			return value;
		}
	}
	
	private class Gene {
		private String ensembl = null;
		private String hugo = null;
		private String ucsc = null;
		private String refseq = null;
		
		private Gene() {
			
		}
		
		public void setEnsembl(String ensembl) {
			this.ensembl = ensembl;
		}
		
		public void setHugo(String hugo) {
			this.hugo = hugo;
		}
		
		public void setUcsc(String ucsc) {
			this.ucsc = ucsc;
		}
		
		public void setRefseq(String refseq) {
			this.refseq = refseq;
		}
		
		public String getEnsembl() {
			return this.ensembl;
		}
		
		public String getHugo() {
			return this.hugo;
		}
		
		public String getRefseq() {
			return this.refseq;
		}
		
		public String getUcsc() {
			return this.ucsc;
		}
		
		public boolean compare(Gene gene2) {
			boolean same = true;
			
			if (this.ensembl != null && gene2.getEnsembl() != null && !this.ensembl.equals(gene2.getEnsembl())) {
				same = false;
			}
			if (this.hugo != null && gene2.getHugo() != null && !this.hugo.equals(gene2.getHugo())) {
				same = false;
			}
			if (this.ucsc != null && gene2.getUcsc() != null && !this.ucsc.equals(gene2.getUcsc())) {
				same = false;
			}
			if (this.refseq != null && gene2.getRefseq() != null && !this.refseq.equals(gene2.getRefseq())) {
				same = false;
			}
			
			return same;
		}
		
		public ArrayList<ExternalGene> getExternalGenes(OrganismBuild ob, BiominerGene bg) {
			ArrayList<ExternalGene> genes = new ArrayList<ExternalGene>();
			if (this.ensembl != null) {
				ExternalGene eg = new ExternalGene(bg, ob, this.ensembl, "ensembl");
				genes.add(eg);
			}
			if (this.hugo != null) {
				ExternalGene eg = new ExternalGene(bg, ob, this.hugo, "hugo");
				genes.add(eg);
			}
			if (this.refseq != null) {
				ExternalGene eg = new ExternalGene(bg, ob, this.refseq, "refseq");
				genes.add(eg);
			}
			if (this.ucsc != null) {
				ExternalGene eg = new ExternalGene(bg, ob, this.ucsc, "ucsc");
				genes.add(eg);
			}
			
			return genes;
		}
	}
	

	
	
	
	
	
	
	
}
