package hci.biominer.parser;

import hci.biominer.model.LocalInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hci.biominer.util.ModelUtil;

public class BedLocalIntervalParser {
	private List<LocalInterval> intervalList;
	private File bedFile = null;
	

	public BedLocalIntervalParser(File bedFile) throws Exception{
		if (!bedFile.canRead()) {
			throw new Exception(String.format("Specified bed file does not exist or can't be read: %s",this.bedFile.getAbsolutePath()));
		}
		this.bedFile = bedFile;
		
	}
	
	public void parseBed(String name, Integer margins) throws Exception{
		BufferedReader br = ModelUtil.fetchBufferedReader(this.bedFile);
		intervalList  = new ArrayList<LocalInterval>();
		
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] items = line.split("\t");
				
				//Parse values
				String chrom = items[0];
				int start = Math.max(Integer.parseInt(items[1]) - margins,0);	
				int end = Integer.parseInt(items[2]) + margins;
				//String name2 = chrom + ":" + Integer.toString(start) + ":" + Integer.toString(end);
				LocalInterval li = new LocalInterval(chrom,start,end,name);
				intervalList.add(li);
			}
		} catch (IOException ioex) {
			throw ioex;
		} finally {
			try {
				br.close();
			} catch (Exception ex) {};
		}
	}
	

	public List<LocalInterval> getLocalIntervals(String name, Integer margins) throws Exception {
		parseBed(name, margins);
		return this.intervalList;
	}

}
