package hci.biominer.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import hci.biominer.model.Analysis;
import hci.biominer.model.AnalysisType;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.Project;
import hci.biominer.model.QueryResult;
import hci.biominer.model.Sample;
import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;
import hci.biominer.model.chip.Chip;
import hci.biominer.model.genome.Genome;
import hci.biominer.model.intervaltree.IntervalTree;
import hci.biominer.service.OrganismBuildService;
import hci.biominer.service.LabService;
import hci.biominer.service.UserService;
import hci.biominer.service.AnalysisService;
import hci.biominer.service.AnalysisTypeService;
import hci.biominer.util.BiominerProperties;
import hci.biominer.util.GenomeBuilds;
import hci.biominer.util.IntervalTrees;


@Controller
@RequestMapping("/query")
public class QueryController {
	
    
    @Autowired
    private LabService labService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AnalysisService analysisService;
    
    @Autowired
    private OrganismBuildService organismBuildService;
    
    @Autowired
    private AnalysisTypeService analysisTypeService;
    
    private StringBuilder warnings = new StringBuilder("");
    
    @PostConstruct
    public void loadAllData() throws Exception {
    	if (!BiominerProperties.isLoaded()) {
    		BiominerProperties.loadProperties();
    	}
    	
    	if (BiominerProperties.getProperty("loadDataOnLaunch").equals("true")) {
    		List<OrganismBuild> obList = organismBuildService.getAllOrganismBuilds();
        	AnalysisType chipType = analysisTypeService.getAnalysisTypeByName("ChIPSeq");
        	
        	for (OrganismBuild ob: obList) {
        		if (ob.getGenomeFile() != null) {
        			GenomeBuilds.loadGenome(ob);
        			Genome g = GenomeBuilds.getGenome(ob);
        			if (chipType == null) {
        	    		System.out.print("Analysis type ChIPSeq is not present in the database!!");
        	    		continue;
        	    	} else {
        	    		List<Analysis> analyses = this.analysisService.getAnalysesToPreload(ob, chipType);
        	    		for (Analysis a: analyses) {
        	    			if (!IntervalTrees.doesChipIntervalTreeExist(a)) {
        	    				IntervalTrees.loadChipIntervalTree(a, g);
        	    			}
        	    			
        	    		}
        	    	}	
        		}
        	}
    	}
    }
  
    @RequestMapping("/layout")
    public String getQueryPartialPage(ModelMap modelMap) {
        return "query/layout";
    }
    
    @RequestMapping(value="warnings",method=RequestMethod.GET)
    @ResponseBody
    public String getWarnings() {
    	return this.warnings.toString();
    }
    
    @RequestMapping(value = "run", method=RequestMethod.GET)
    @ResponseBody
    //public List<QueryResult> run(
    public List<QueryResult> run (
        @RequestParam(value="codeResultType") String codeResultType,
        @RequestParam(value="idOrganismBuild") Long idOrganismBuild,
        @RequestParam(value="idAnalysisTypes") List<Long> idAnalysisTypes,
        @RequestParam(value="idLabs") List<Long> idLabs,
        @RequestParam(value="idProjects") List<Long> idProjects,
        @RequestParam(value="idAnalyses") List<Long> idAnalyses,
        @RequestParam(value="idSampleSources") List<Long> idSampleSources,
        @RequestParam(value="isIntersect") boolean isIntersect,
        @RequestParam(value="regions") String regions,
        @RequestParam(value="regionMargins") Integer regionMargins,
        @RequestParam(value="genes") String genes,
        @RequestParam(value="geneMargins") Integer geneMargins,
        @RequestParam(value="idGeneAnnotations") List<Long> idGeneAnnotations,
        @RequestParam(value="isThresholdBasedQuery") boolean isThresholdBasedQuery,
        @RequestParam(value="FDR") String FDR,
        @RequestParam(value="codeFDRComparison") String codeFDRComparison,
        @RequestParam(value="log2Ratio") String log2Ratio,
        @RequestParam(value="codeLog2RatioComparison") String codeLog2RatioComparison
        ) throws Exception {
      
    	//Clear out warnings
    	this.warnings = new StringBuilder("");
    	
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = null;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    	} 
    	
    	//Get Organism Build
    	OrganismBuild ob = this.organismBuildService.getOrganismBuildById(idOrganismBuild);   	
    	
    	//Get genome
    	Genome genome = this.fetchGenome(ob);
    	
    	//Build AnalysisTypeList
    	HashMap<String,AnalysisType> atMap = new HashMap<String,AnalysisType>();
    	for (Long id: idAnalysisTypes) {
    		AnalysisType at = this.analysisTypeService.getAnalysisTypeById(id);
    		atMap.put(at.getType(),at);
    	}
    	
    	//Create intervals
    	IntervalParser ip = new IntervalParser();
    	List<Interval> intervalsToCheck = ip.parseIntervals(regions, regionMargins, genome);
    	this.warnings = ip.getWarnings();
    	
    	List<QueryResult> fullRegionResults = new ArrayList<QueryResult>();
    	
    	if (atMap.containsKey("ChIPSeq")) {
    		System.out.println("Looking for ChIPSeq analyses");
    		//Get analyses
        	List<Analysis> analyses = this.analysisService.getAnalysesByQuery(idLabs, idProjects, idAnalyses, idSampleSources, atMap.get("ChIPSeq").getIdAnalysisType(), idOrganismBuild, user);
        	System.out.println("Number of analyses: " + analyses.size());
        	
        	//Convert analyses to interval trees
        	ArrayList<HashMap<String,IntervalTree<Chip>>> itList = generateIntervalTreesChip(analyses, genome);
        	System.out.println("Number of interval tree lists " + itList.size());
        	
        	//Run basic search
        	List<QueryResult> results = this.getIntersectingRegionsChip(itList, analyses, genome, intervalsToCheck);
     
        	fullRegionResults.addAll(results);
    	}
    	
    	if (fullRegionResults.size() > 100) {
    		List<QueryResult> subset = fullRegionResults.subList(0, 100);
    		return subset;	
    	} else {
    		return fullRegionResults;
    	}
    	
    }
    
    
    private Genome fetchGenome(OrganismBuild ob) throws Exception {
    	try {
    		if (!GenomeBuilds.doesGenomeExist(ob)) {
        		GenomeBuilds.loadGenome(ob);
        	} 
        	return GenomeBuilds.getGenome(ob);
    	} catch (Exception ex) {
    		throw ex;
    	}
    }
    	
    private ArrayList<HashMap<String,IntervalTree<Chip>>> generateIntervalTreesChip(List<Analysis> analyses, Genome genome) throws Exception{
    	ArrayList<HashMap<String,IntervalTree<Chip>>> itList = new ArrayList<HashMap<String,IntervalTree<Chip>>>();
    	for (Analysis a: analyses) {
    		if (!IntervalTrees.doesChipIntervalTreeExist(a)) {
    			IntervalTrees.loadChipIntervalTree(a, genome);
    		}
    		itList.add(IntervalTrees.getChipIntervalTree(a));
    	}	
    	return itList;
    }
    
    
    private List<QueryResult> getIntersectingRegionsChip(ArrayList<HashMap<String,IntervalTree<Chip>>> treeList, List<Analysis> analyses, Genome genome, 
    		List<Interval> intervals) throws Exception{
    	List<QueryResult> queryResults = new ArrayList<QueryResult>();
    	
    	for (int i=0; i<treeList.size(); i++) {
    		
    		HashMap<String, IntervalTree<Chip>> it = treeList.get(i);
    		for (Interval inv: intervals) {
    			if (it.containsKey(inv.getChrom())) {
    				List<Chip> hits = it.get(inv.getChrom()).search(inv.getStart(), inv.getEnd());
        			Analysis a = analyses.get(i);
        			for (Chip c: hits) {
        				QueryResult result = new QueryResult();
        	    		result.setProjectName(a.getProject().getName());
        	    		result.setAnalysisType(a.getAnalysisType().getType());
        	    		result.setAnalysisName(a.getName());
        	    		result.setAnalysisSummary(a.getDescription());
        	    		String conditions = "";
        	    		for (Sample sample: a.getSamples()) {
        	    			conditions = sample.getSampleCondition().getCond() + ",";
        	    		}
        	    		result.setSampleConditions(conditions.substring(0,conditions.length()-1));
        	    		String coordinate = inv.getChrom() + ":" + String.valueOf(c.getStart()) + "-" + String.valueOf(c.getStop());
        	    		result.setCoordinates(coordinate);
        	    		result.setFDR(c.getTransFDR());
        	    		result.setLog2Ratio(c.getLog2Rto());
        	    		queryResults.add(result);
        			}
    			}
    			
    		}

    	}
    	
    	return queryResults;
    }
   
    
    private class IntervalParser {
    	private Pattern pattern1 = Pattern.compile("^(.+?):(\\d+)-(\\d+)$");
    	private Pattern pattern2 = Pattern.compile("^(.+)$");
    	private Pattern marginP = Pattern.compile("^(\\d+)$");
    	private StringBuilder warnings = new StringBuilder("");
    	
    	
    	
    	public List<Interval> parseIntervals(String region, Integer regionMargin, Genome genome) throws Exception {
    		List<Interval> intervals = new ArrayList<Interval>();
    		
    	
    		
//    		if (!regionMargin.equals("")) {
//    			Matcher marginM = marginP.matcher(regionMargin);
//    			if (marginM.matches()) {
//    				margin = Integer.parseInt(marginM.group(1));
//    			} else {
//    				throw new Exception(String.format("The region margin %s cannot be parsed.  Must be an integer.\n",regionMargin));
//    			}
//    		}
    		
    		if (region == "") {
    			for (String chrom: genome.getNameChromosome().keySet()) {
    				Interval inv = new Interval(chrom,0,genome.getNameChromosome().get(chrom).getLength());
    				intervals.add(inv);
    			}
    		} else {
    			String[] regionList = region.split(";");
    			for (String r: regionList) {
    				Matcher m1 = pattern1.matcher(r);
    	    		Matcher m2 = pattern2.matcher(r);
    	    		
    	    		if (m1.matches()) {
    	    			int end = 0;
    	    			int start = 0;
    	    			try {
    	    				start = Integer.parseInt(m1.group(2)) - regionMargin;
    	    			} catch (NumberFormatException nfe) {
    	    				warnings.append(String.format("Start boundary not an integer %s, skipping.",m1.group(2)));
    	    				continue;
    	    			}
    	    			
    	    			try {
    	    				end = Integer.parseInt(m1.group(3)) + regionMargin;
    	    			} catch (NumberFormatException nfe) {
    	    				warnings.append(String.format("End boundary not an integer %s, skipping.",m1.group(3)));
    	    				continue;
    	    			}
    	    			
    	    			String chrom = m1.group(1);
    	    			
    	    			if (!genome.getNameChromosome().containsKey(chrom)) {
    	    				warnings.append(String.format("The chromsome %s could not be found in the genome %s.\n",chrom,genome.getBuildName()));
    	    				continue;
    	    			}
    	    			if (start >= end) {
    	    				warnings.append(String.format("The start coordinate (%d) is greater or equal to the end coordinate (%d).\n",start,end));
    	    				continue;
    	    			}
    	    			
    	    			if (start < 0) {
    	    				warnings.append(String.format("The start coordinate ( %d ) is less than 0.  Setting to zero.\n",start));
    	    				start = 0;
    	    			} 
    	    			if (end > genome.getNameChromosome().get(chrom).getLength()) {
    	    				warnings.append(String.format("The end coordinate ( %d) is greater than the chromsome length (%d). Setting to chromsome"
    	    						+ " end.\n",end,genome.getNameChromosome().get(chrom).getLength()));
    	    				end = genome.getNameChromosome().get(chrom).getLength();
    	    			}  
    	    			
    	    			Interval inv = new Interval(chrom,start,end);
    	    			intervals.add(inv);
    	    			
    	    		} else if (m2.matches()) {
    	    			String chrom  = m2.group(1);
    	    			if (!genome.getNameChromosome().containsKey(chrom)) {
    	    				warnings.append(String.format("The chromsome %s could not be found in the genome %s.\n",chrom,genome.getBuildName()));
    	    				continue;
    	    			}
    	    			
    	    			int start = 0;
    	    			int end = genome.getNameChromosome().get(chrom).getLength();
    	    			
    	    			Interval inv = new Interval(chrom,start,end);
    	    			intervals.add(inv);

    	    			
    	    		} else {
    	    			warnings.append(String.format("The string %s does not match a region format.\n",region));
    	    			
    	    		}
    			}
    		}
    		
    		return intervals;
    	}
    	
    	public StringBuilder getWarnings() {
    		return warnings;
    	}
    }
    
    private class Interval {
    	private String chrom;
    	private int start;
    	private int end;

	
    	public Interval(String chrom, int start, int end) {
    		this.chrom = chrom;
    		this.start = start;
    		this.end = end;
    	}
    	

    	public String getChrom() {
    		return this.chrom;
    	} 
    	
    	public int getStart() {
    		return this.start;
    	}
    	
    	public int getEnd() {
    		return this.end;
    	}
    }
    
}
