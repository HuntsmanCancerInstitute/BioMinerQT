package hci.biominer.util;

import hci.biominer.controller.FileController;
import hci.biominer.model.Analysis;
import hci.biominer.model.GenericResult;
import hci.biominer.model.genome.Genome;
import hci.biominer.model.intervaltree.IntervalTree;
import hci.biominer.parser.ChipIntervalTreeParser;
import hci.biominer.parser.RnaSeqIntervalTreeParser;
import hci.biominer.util.Enumerated.AnalysisTypeEnum;

import java.io.File;
import java.util.HashMap;


public class IntervalTrees {
	private static HashMap<String,HashMap<String,IntervalTree<GenericResult>>> analysisTrees = new HashMap<String,HashMap<String,IntervalTree<GenericResult>>>();
	
	public static void loadChipIntervalTree(Analysis analysis, Genome genome) throws Exception {
		if (analysis.getFile() != null) {
			try {
				File file = FileController.generateFilePath(analysis.getFile());
				if (!file.exists() || !file.canRead()) {
					throw new Exception(String.format("Can't read the file %s from analysis %s. Maybe it got deleted?",file.getAbsolutePath(),analysis.getName()));
				}
				System.out.println("Creating interval tree for " + file.getAbsolutePath());
				if (analysis.getAnalysisType().getType() == AnalysisTypeEnum.ChIPSeq) {
					ChipIntervalTreeParser ctp = new ChipIntervalTreeParser(file, genome);
					HashMap<String,IntervalTree<GenericResult>> it = ctp.getChromNameIntervalTree();
					analysisTrees.put(file.getAbsolutePath(), it);
				}else if (analysis.getAnalysisType().getType() == AnalysisTypeEnum.RNASeq) {
					RnaSeqIntervalTreeParser rtp = new RnaSeqIntervalTreeParser(file, genome);
					HashMap<String,IntervalTree<GenericResult>> it = rtp.getChromNameIntervalTree();
					analysisTrees.put(file.getAbsolutePath(), it);
				}
				
			} catch (Exception ex) {
				throw ex;
			}
		} else {
			throw new Exception(String.format("The analysis %s does not an associated file that can parsed into an intervalTree",analysis.getName()));
		}
	}
	
	public static HashMap<String,IntervalTree<GenericResult>> getChipIntervalTree(Analysis analysis) throws Exception{
		if (analysis.getFile() != null) {
			File file = FileController.generateFilePath(analysis.getFile());
			if (analysisTrees.containsKey(file.getAbsolutePath())) {
				return analysisTrees.get(file.getAbsolutePath());
			} else {
				throw new Exception(String.format("There is no interval tree for the specified analysis: %s!",analysis.getName()));
			}
		} else {
			throw new Exception(String.format("There is no file associated with the specified analysis: %s. Can't get interval tree",analysis.getName()));
		}
	}
	
	public static boolean doesChipIntervalTreeExist(Analysis analysis) throws Exception {
		if (analysis.getFile() != null) {
			File file = FileController.generateFilePath(analysis.getFile());
			if (analysisTrees.containsKey(file.getAbsolutePath())) {
				return true;
			} else {
				return false;
			}
		} else {
			throw new Exception(String.format("There is no file associated with the specified analysis: %s. Can't get interval tree",analysis.getName()));
		}

	}
}