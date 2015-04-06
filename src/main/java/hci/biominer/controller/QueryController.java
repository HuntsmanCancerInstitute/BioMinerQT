package hci.biominer.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import hci.biominer.model.Analysis;
import hci.biominer.model.AnalysisType;
import hci.biominer.model.DataTrack;
import hci.biominer.model.GeneNameModel;
import hci.biominer.model.GenericResult;
import hci.biominer.model.IgvSessionResult;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.Project;
import hci.biominer.model.QueryResult;
import hci.biominer.model.QueryResultContainer;
import hci.biominer.model.RegionUpload;
import hci.biominer.model.Sample;
import hci.biominer.model.SampleSource;
import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;
import hci.biominer.model.genome.Gene;
import hci.biominer.model.genome.Genome;
import hci.biominer.model.genome.Transcript;
import hci.biominer.model.intervaltree.Interval;
import hci.biominer.model.intervaltree.IntervalTree;
import hci.biominer.model.ExternalGene;
import hci.biominer.service.DashboardService;
import hci.biominer.service.OrganismBuildService;
import hci.biominer.service.LabService;
import hci.biominer.service.UserService;
import hci.biominer.service.AnalysisService;
import hci.biominer.service.AnalysisTypeService;
import hci.biominer.service.ExternalGeneService;
import hci.biominer.util.BiominerProperties;
import hci.biominer.util.Enumerated.AnalysisTypeEnum;
import hci.biominer.util.GenomeBuilds;
import hci.biominer.util.IntervalTrees;
import hci.biominer.util.QuerySettings;
import hci.biominer.util.igv.IGVResource;
import hci.biominer.util.igv.IGVSession;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.zip.GZIPOutputStream;



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
    
    @Autowired
    private ExternalGeneService externalGeneService;
    
    @Autowired
    private DashboardService dashboardService;
    
    //private StringBuilder warnings = new StringBuilder("");
    
    private HashMap<String,QueryResultContainer> resultsDict =  new HashMap<String,QueryResultContainer>();
    private HashMap<String,StringBuilder> queryWarningsDict = new HashMap<String,StringBuilder>();
    private HashMap<String,QuerySettings> settingsDict = new HashMap<String,QuerySettings>();
    private HashMap<String,String> regionDict = new HashMap<String,String>();
    private HashMap<String,String> geneDict = new HashMap<String,String>();
    
    
    private HashMap<String,File> fileDict = new HashMap<String,File>();
    private HashMap<Long,List<GeneNameModel>> searchDict = new HashMap<Long,List<GeneNameModel>>();
    private HashMap<String,ArrayList<Long>> nameLookupDict = new HashMap<String,ArrayList<Long>>();
    
    
    private HashMap<String,Subject> activeUsers = new HashMap<String,Subject>();
    
    private SessionCheckerTimer sct;
    private Date date = new Date();
    
    @PreDestroy
    public void shutdownTimer() {
    	sct.stop();
    }
    
    @SuppressWarnings("restriction")
	@PostConstruct
    public void loadAllData() throws Exception {
    	//Load properties
    	if (!BiominerProperties.isLoaded()) {
    		BiominerProperties.loadProperties();
    	}
    	
    	//If load on launch, create all interval trees
    	if (BiominerProperties.getProperty("loadDataOnLaunch").equals("true")) {
    		List<OrganismBuild> obList = organismBuildService.getAllOrganismBuilds();
    		
        	AnalysisType chipType = analysisTypeService.getAnalysisTypeByName(AnalysisTypeEnum.valueOf("ChIPSeq"));
        	AnalysisType rnaType = analysisTypeService.getAnalysisTypeByName(AnalysisTypeEnum.valueOf("RNASeq"));
        	AnalysisType methType = analysisTypeService.getAnalysisTypeByName(AnalysisTypeEnum.valueOf("Methylation"));
        	AnalysisType varType = analysisTypeService.getAnalysisTypeByName(AnalysisTypeEnum.valueOf("Variant"));
        	
        	for (OrganismBuild ob: obList) {
        		if (ob.getGenomeFile() != null) {
        			GenomeBuilds.loadGenome(ob);
        			Genome g = GenomeBuilds.getGenome(ob);
        			
        			List<Analysis> analyses = new ArrayList<Analysis>();
        			
        			if (chipType == null) {
        	    		System.out.print("Analysis type ChIPSeq is not present in the database!!");
        	    	} else {
        	    		List<Analysis> chipAnalysis = this.analysisService.getAnalysesToPreload(ob, chipType);
        	    		analyses.addAll(chipAnalysis);
        	    	}	
        			
        			if (rnaType == null) {
        				System.out.println("Analysis Type RNASeq is not present in the database");
        			} else {
        	    		List<Analysis> rnaAnalysis = this.analysisService.getAnalysesToPreload(ob, rnaType);
        	    		analyses.addAll(rnaAnalysis);
        	    	}	
        			
        			if (methType == null) {
        				System.out.println("Analysis Type Methylation is not present in the database");
        			} else {
        	    		List<Analysis> methAnalysis = this.analysisService.getAnalysesToPreload(ob, methType);
        	    		analyses.addAll(methAnalysis);
        	    	}
        			
        			if (varType == null) {
        				System.out.println("Analysis Type Variation is not present in the database");
        			} else {
        	    		List<Analysis> varAnalysis = this.analysisService.getAnalysesToPreload(ob, varType);
        	    		analyses.addAll(varAnalysis);
        	    	}
        			
        			
        			if (analyses.size() != 0) {
        	    		for (Analysis a: analyses) {
        	    			if (!IntervalTrees.doesChipIntervalTreeExist(a)) {
        	    				IntervalTrees.loadChipIntervalTree(a, g);
        	    			}
        	    		}	
        			}
        			
        		}
        		
        		if(!searchDict.containsKey(ob.getIdOrganismBuild())) {
        			loadGeneNames(ob);
        		}
        	}
    	}
    	
    	//Start checking for active sessions
    	sct = new SessionCheckerTimer(date);
    	sct.start();	
    }
    
    
    
    private void loadGeneNames(OrganismBuild ob) {
    	System.out.println("Loading common names for: " + ob.getName());
    	List<ExternalGene> egList = this.externalGeneService.getExternalGenesByOrganismBuild(ob);
    	
    	
    	HashSet<String> uniqueNamesHash = new HashSet<String>();
    	List<String> uniqueNamesSort = new ArrayList<String>();
    	for (ExternalGene eg: egList) {
    		String name = eg.getExternalGeneName();
    		String source = eg.getExternalGeneSource();
    		if (source.equals("hugo")) {
    			uniqueNamesHash.add(name);
    		}
    		if (!nameLookupDict.containsKey(name)) {
    			nameLookupDict.put(name, new ArrayList<Long>());
    		}
    		nameLookupDict.get(name).add(eg.getIdExternalGene());
    		
    	}
    	egList.clear();
    	
    	uniqueNamesSort.addAll(uniqueNamesHash);
    	Collections.sort(uniqueNamesSort);
    	
    	List<GeneNameModel> uniqueNamesList = new ArrayList<GeneNameModel>();
    	for (String name: uniqueNamesSort) {
    		GeneNameModel gnm = new GeneNameModel(name);
    		uniqueNamesList.add(gnm);
    	}
    	uniqueNamesSort.clear();
    	searchDict.put(ob.getIdOrganismBuild(), uniqueNamesList);
    	
  
    }
  
    @RequestMapping(value="/clearNames",method=RequestMethod.POST)
    @ResponseBody
    public void clearNames(@RequestParam("obId") Long obId ) {
    	if (searchDict.containsKey(obId)) {
    		searchDict.remove(obId);
    	}
    }
    
    @RequestMapping("/layout")
    public String getQueryPartialPage(ModelMap modelMap) {
        return "query/layout";
    }
    
    @RequestMapping(value="warnings",method=RequestMethod.GET)
    @ResponseBody
    public String getWarnings() throws Exception{
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	
    	User user = null;
    	String username;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    		
    		this.activeUsers.put(user.getUsername(), currentUser);
    		username = user.getUsername();
    	} else {
    		username = "guest";
    	}
    	return queryWarningsDict.get(username).toString();
    }
    
    @RequestMapping(value="uploadGene",method=RequestMethod.POST)
    @ResponseBody
    public RegionUpload parseGenes(@RequestParam("file") MultipartFile file) {
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	
    	User user = null;
    	String username;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    		
    		this.activeUsers.put(user.getUsername(), currentUser);
    		username = user.getUsername();
    	} else {
    		username = "guest";
    	}
    	
    	
    	RegionUpload regions = new RegionUpload();
    	StringBuilder geneString = new StringBuilder("");
    	if (!file.isEmpty()) {
    		try {
    			String name = file.getOriginalFilename();
    			BufferedReader br = null;
    			if (name.endsWith(".gz")) {
    				GZIPInputStream gzip = new GZIPInputStream(file.getInputStream());
    				br =  new BufferedReader(new InputStreamReader(gzip));
    			} else if (name.endsWith(".zip")) {
    				ZipInputStream zip = new ZipInputStream(file.getInputStream());
    				ZipEntry ze = (ZipEntry) zip.getNextEntry();
    				br = new BufferedReader(new InputStreamReader(zip));
    			} else {
    				br = new BufferedReader(new InputStreamReader(file.getInputStream()));
    				
    				
    			}
    
    			String temp;
    			
    			boolean ok = true;
    			while ((temp = br.readLine()) != null) {
    				if (!isASCII(temp)) {
						regions.setMessage("The file appears to be binary.  Biominer accepts text files or bed files.  Can be gzipped or zipped.");
						ok =false;
    				}
    				
    				
    				String[] parts = temp.split(",\\s*");
    				for (String p: parts) {
    					geneString.append(p.toUpperCase() + "\n");
    				}
    				
    			}
    			
    			if (ok) {
    				regions.setRegions(String.format("[Load genes from file: %s]",name));
        			this.geneDict.put(username,geneString.toString());
    			}
    			
    			br.close();
    			
    		} catch (IOException ioex) {
    			regions.setMessage("Error reading file: " + ioex.getMessage());
    			ioex.printStackTrace();
    		}
    		
    		
    	} else {
    		regions.setMessage("File is empty!");
    	}
    	
    	return regions;
    }
    
    
    @RequestMapping(value="upload",method=RequestMethod.POST)
    @ResponseBody
    public RegionUpload parseRegions(@RequestParam("file") MultipartFile file) {
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	
    	User user = null;
    	String username;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    		
    		this.activeUsers.put(user.getUsername(), currentUser);
    		username = user.getUsername();
    	} else {
    		username = "guest";
    	}
    	
    	
    	RegionUpload regions = new RegionUpload();
    	Pattern pattern1 = Pattern.compile("^(\\w+)(,|:|\\s+)(\\d+)(,|-|\\s+)(\\d+)(,|-|\\s+)*.*");
    	
    	if (!file.isEmpty()) {
    		try {
    			String name = file.getOriginalFilename();
    			BufferedReader br = null;
    			if (name.endsWith(".gz")) {
    				GZIPInputStream gzip = new GZIPInputStream(file.getInputStream());
    				br =  new BufferedReader(new InputStreamReader(gzip));
    			} else if (name.endsWith(".zip")) {
    				ZipInputStream zip = new ZipInputStream(file.getInputStream());
    				ZipEntry ze = (ZipEntry) zip.getNextEntry();
    				br = new BufferedReader(new InputStreamReader(zip));
    				
    		
    			} else {
    				br = new BufferedReader(new InputStreamReader(file.getInputStream()));
    			}
    
    			String temp;
    			StringBuilder regionString = new StringBuilder("");
    			boolean ok = true;
    			while ((temp = br.readLine()) != null) {
    				Matcher m = pattern1.matcher(temp);
    				if (m.matches()) {
    					regionString.append(String.format("%s:%s-%s\n",m.group(1),m.group(3),m.group(5)));
    				} else {
    					if (isASCII(temp)) {
    						regions.setMessage(String.format("Could not parse region line: %s. The first three columns must be chromsome, "
        							+ "start coordinate and stop coordinate.  Can be tab, space, comma delimited or in the format "
        							+ "chr:start-end",temp));
    					} else {
    						regions.setMessage("The file appears to be binary.  Biominer accepts text files or bed files.  Can be gzipped or zipped.");
    					}
    					
    					ok = false;
    					break;
    				}
    			}
    			
    			if (ok) {
    				regions.setRegions(String.format("[Load regions from file: %s]",name));
        			this.regionDict.put(username,regionString.toString());
    			}
    			
    			br.close();
    			
    			
    		} catch (IOException ioex) {
    			regions.setMessage("Error reading file: " + ioex.getMessage());
    			ioex.printStackTrace();
    		}
    		
    		
    	} else {
    		regions.setMessage("File is empty!");
    	}
    	
    	return regions;
    }
    
    
    private boolean isASCII(String test) {
    	byte[] byteArray = test.getBytes();
    	CharsetDecoder decoder = Charset.forName("US-ASCII").newDecoder();
        try {
            CharBuffer buffer = decoder.decode(ByteBuffer.wrap(byteArray));
            return true;
 

        } catch (CharacterCodingException e) {
            return false;
        }
    }
    
    @RequestMapping(value = "run", method=RequestMethod.GET)
    @ResponseBody
    //public List<QueryResult> run(
    public QueryResultContainer run (
        @RequestParam(value="codeResultType") String codeResultType,
        @RequestParam(value="idOrganismBuild") Long idOrganismBuild,
        @RequestParam(value="idAnalysisTypes") List<Long> idAnalysisTypes,
        @RequestParam(value="idLabs") List<Long> idLabs,
        @RequestParam(value="idProjects") List<Long> idProjects,
        @RequestParam(value="idAnalyses") List<Long> idAnalyses,
        @RequestParam(value="idSampleSources") List<Long> idSampleSources,
        @RequestParam(value="regions") String regions,
        @RequestParam(value="regionMargins") Integer regionMargins,
        @RequestParam(value="genes") String genes,
        @RequestParam(value="geneMargins") Integer geneMargins,
        @RequestParam(value="FDR",required=false) Float FDR,
        @RequestParam(value="codeFDRComparison") String codeFDRComparison,
        @RequestParam(value="log2Ratio",required=false) Float log2Ratio,
        @RequestParam(value="codeLog2RatioComparison") String codeLog2RatioComparison,
        @RequestParam(value="resultsPerPage") Integer resultsPerPage,
        @RequestParam(value="sortType") String sortType,
        @RequestParam(value="intersectionTarget") String target,
        @RequestParam(value="isReverse") boolean reverse,
        @RequestParam(value="searchExisting") boolean searchExisting
        ) throws Exception {
      
    	
    	
    	if (genes != null) {
    		genes = genes.toUpperCase();
    	}
    	
    
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	
    	User user = null;
    	String username;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    		
    		this.activeUsers.put(user.getUsername(), currentUser);
    		username = user.getUsername();
    	} else {
    		username = "guest";
    	}
    	
    	//Clear out warnings
    	StringBuilder warnings = new StringBuilder("");
    	queryWarningsDict.put(username, warnings);
    	
    	//Get Organism Build
    	OrganismBuild ob = this.organismBuildService.getOrganismBuildById(idOrganismBuild);   	
    	
    	//Get genome
    	Genome genome = GenomeBuilds.fetchGenome(ob);
    	
    	//Build AnalysisTypeList
    	HashMap<AnalysisTypeEnum,AnalysisType> atMap = new HashMap<AnalysisTypeEnum,AnalysisType>();
    	for (Long id: idAnalysisTypes) {
    		AnalysisType at = this.analysisTypeService.getAnalysisTypeById(id);
    		atMap.put(at.getType(),at);
    	}
    	
    	//Container of mapped gene names, used for gene based searches
    	List<String> mappedNames = null;
    	
    	//Create intervals
    	IntervalParser ip = new IntervalParser();
    	List<LocalInterval> intervalsToCheck = new ArrayList<LocalInterval>();
        	
    	if (target.equals("EVERYTHING")) {
    		intervalsToCheck = ip.parseIntervals("", "", 0, genome);
    	} else if (codeResultType.equals("GENE") || (codeResultType.equals("REGION") && target.equals("GENE"))) {
    		List<List<String>> parsed = null;
    		if (genes.startsWith("[LOAD GENES FROM FILE: ")) {
    			if (geneDict.containsKey(username)) {
    				String loadedGenes = geneDict.get(username);
        			parsed = this.getGeneIntervals(loadedGenes, genome, "TxBoundary",ob);
    			} else {
    				queryWarningsDict.get(username).append("Biominer expects genes loaded from file, but none could be found. Please try reloading your gene file.<br/>");
    			}

    		} else if (genes.equals("[ALL RESULT GENES]")) {
    			if (geneDict.containsKey(username)) {
    				String loadedGenes = geneDict.get(username);
        			parsed = this.getGeneIntervals(loadedGenes, genome, "TxBoundary",ob);
    			} else {
    				queryWarningsDict.get(username).append("Biominer expects genes copied from the previous query, but none could be found. Please submit a bug report.<br/>");
    			}
    		} else {
    			parsed = this.getGeneIntervals(genes, genome, "TxBoundary",ob);
    		}
    	 
    		System.out.println("HEY THE SIZE IS:" + parsed.get(0).size());
    		if (parsed != null && parsed.get(0).size() != 0) {
    			mappedNames = parsed.get(2); //This will be used for gene based filtering, if necessary
        		intervalsToCheck = ip.parseIntervals(this.convertListToString(parsed.get(0)), this.convertListToString(parsed.get(1)), geneMargins, genome);
    		} 
    	} else if (codeResultType.equals("REGION"))  {
    		if (regions.startsWith("[Load regions from file: ")) {
    			if (regionDict.containsKey(username)) {
    				String loadedRegions = regionDict.get(username);
        			intervalsToCheck = ip.parseIntervals(loadedRegions, loadedRegions, regionMargins, genome);
    			} else {
    				queryWarningsDict.get(username).append("Biominer expects regions loaded from file, but none could be found. Please try reloading your gene file.<br/>");
    			}
    		} else if (regions.equals("[All result coordinates]")) {
    			if (regionDict.containsKey(username)) {
    				String loadedRegions = regionDict.get(username);
        			intervalsToCheck = ip.parseIntervals(loadedRegions, loadedRegions, regionMargins, genome);
    			} else {
    				queryWarningsDict.get(username).append("Biominer expects regions copied from the previous query , but none could be found. Please submit a bug report.<br/>");
    			}
    		} else {
    			intervalsToCheck = ip.parseIntervals(regions, regions, regionMargins, genome);
    		}
    	}
    	
    	//Add IP warnings
    	queryWarningsDict.get(username).append(ip.getWarnings());
    	
    	//Get analysis entries for the query
    	List<Analysis> analyses = new ArrayList<Analysis>();
    	if (atMap.containsKey(AnalysisTypeEnum.ChIPSeq)) {
    		System.out.println("Looking for ChIPSeq analyses");
        	List<Analysis> chipAnalyses = this.analysisService.getAnalysesByQuery(idLabs, idProjects, idAnalyses, idSampleSources, atMap.get(AnalysisTypeEnum.ChIPSeq).getIdAnalysisType(), idOrganismBuild, user);
        	System.out.println("Number of analyses: " + chipAnalyses.size());
        	analyses.addAll(chipAnalyses);
    	}
    	if (atMap.containsKey(AnalysisTypeEnum.RNASeq)) {
    		System.out.println("Looking for RNAseq analyses");
    		List<Analysis> rnaseqAnalyses = this.analysisService.getAnalysesByQuery(idLabs, idProjects, idAnalyses, idSampleSources, atMap.get(AnalysisTypeEnum.RNASeq).getIdAnalysisType(), idOrganismBuild, user);
    		System.out.println("Number of analyses: " + rnaseqAnalyses.size());
    		analyses.addAll(rnaseqAnalyses);
    	}
    	if (atMap.containsKey(AnalysisTypeEnum.Methylation)) {
    		System.out.println("Looking for Methylation analyses");
    		List<Analysis> methAnalyses = this.analysisService.getAnalysesByQuery(idLabs, idProjects, idAnalyses, idSampleSources, atMap.get(AnalysisTypeEnum.Methylation).getIdAnalysisType(), idOrganismBuild, user);
    		System.out.println("Number of analyses: " + methAnalyses.size());
    		analyses.addAll(methAnalyses);
    	}
    	if (atMap.containsKey(AnalysisTypeEnum.Variant)) {
    		System.out.println("Looking for Variant analyses");
    		List<Analysis> variantAnalyses = this.analysisService.getAnalysesByQuery(idLabs, idProjects, idAnalyses, idSampleSources, atMap.get(AnalysisTypeEnum.Variant).getIdAnalysisType(), idOrganismBuild, user);
    		System.out.println("Number of analyses: " + variantAnalyses.size());
    		analyses.addAll(variantAnalyses);
    	}
        	
    	//Convert analyses to interval trees
    	ArrayList<HashMap<String,IntervalTree<GenericResult>>> itList = generateIntervalTrees(analyses, genome);
    	System.out.println("Number of interval tree lists " + itList.size());
        	
    	//Run basic search
    	List<QueryResult> results;
    	if (searchExisting) {
    		if (resultsDict.containsKey(username)) {
    			HashMap<String,IntervalTree<QueryResult>> resultTree = this.createQueryResultIntervalTree(resultsDict.get(username).getResultList());
    			results = this.getIntersectingRegionsExisting(resultTree, intervalsToCheck, reverse);
        	} else {
        		results = new ArrayList<QueryResult>();
        		queryWarningsDict.get(username).append("Could not find any existing results, can't query existing results.<br/>");
    		}
    	} else {
    		results = this.getIntersectingRegions(itList, analyses, intervalsToCheck, reverse);
    		System.out.println(results.size());
    	}
    	
    	
    	
 
    	//Run thresholding if necessary
    	List<QueryResult> fullRegionResults = new ArrayList<QueryResult>();
    	if (FDR != null) {
    		results = this.filterFdr(results, FDR, codeFDRComparison);
    	}
    	
    	if (log2Ratio != null) {
    		results = this.filterLog2Ratio(results, log2Ratio, codeLog2RatioComparison);
    	}
    	
    	if (codeResultType.equals("GENE") && mappedNames != null) {
    		results = this.filterGene(results, mappedNames);
    	}
    	
    	fullRegionResults.addAll(results);
    	System.out.println(fullRegionResults.size());
    	
    	
    	
    	//Determine how many analyses were used
    	List<Analysis> usedAnalyses = this.getUsedAnalyses(analyses, fullRegionResults);
    	HashMap<String,String> usedDataTracks = this.getDataTrackList(usedAnalyses);
    	
    	QueryResultContainer qrc = new QueryResultContainer(fullRegionResults, fullRegionResults.size(), usedAnalyses.size(), usedDataTracks.keySet().size(), 0, sortType, true);
    	
    	//Create settings object
    	QuerySettings qs = new QuerySettings(codeResultType, target, idOrganismBuild, idAnalysisTypes, idLabs, idProjects,
    			idAnalyses, idSampleSources, regions, regionMargins, genes, geneMargins, FDR, codeFDRComparison, log2Ratio, 
    			codeLog2RatioComparison,resultsPerPage, sortType, reverse, searchExisting);

    	if (user != null) {
    		this.resultsDict.put(username, qrc);
    		this.settingsDict.put(username, qs);
    	} else {
    		this.resultsDict.put(username, qrc);
    	}
    	
    	System.out.println("Used analyses " + usedAnalyses.size());
    	System.out.println("Used data tracks " + usedDataTracks.keySet().size());
    	
    	//update database
    	Date queryDate = new Date();
    	this.dashboardService.updateQueryDate(queryDate.getTime());
    	this.dashboardService.increaseQuery();
    	
    	//Create result object
    	QueryResultContainer qrcSub = qrc.getQrcSubset(resultsPerPage, 0, sortType);
    	return qrcSub;	
    }
    
    
    @RequestMapping(value="copyAllCoordinates",method=RequestMethod.POST)
    @ResponseBody
    private void copyAllCoordinates(HttpServletResponse response) {
    	Subject currentUser = SecurityUtils.getSubject();
    	String username = "guest";
    	    	
    	//If user isn't authenticated, return nothing
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		User user = userService.getUser(userId);
    		username = user.getUsername();
    	} 
    	
    	if (this.resultsDict.containsKey(username)) {
    		StringBuilder intervals = new StringBuilder("");
    		QueryResultContainer qrc = resultsDict.get(username);
    		for (QueryResult qr: qrc.getResultList()) {
    			intervals.append(qr.getCoordinates() + "\n");
    		}
    		this.regionDict.put(username, intervals.toString());
    	} else {
    		response.setStatus(998);
    		this.regionDict.put(username, "");
    	}
    }
    
    @RequestMapping(value="copyAllGenes",method=RequestMethod.POST)
    @ResponseBody
    private void copyAllGenes(HttpServletResponse response) {
    	Subject currentUser = SecurityUtils.getSubject();
    	String username = "guest";
    	    	
    	//If user isn't authenticated, return nothing
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		User user = userService.getUser(userId);
    		username = user.getUsername();
    	} 
    	
    	if (this.resultsDict.containsKey(username)) {
    		StringBuilder intervals = new StringBuilder("");
    		QueryResultContainer qrc = resultsDict.get(username);
    		for (QueryResult qr: qrc.getResultList()) {
    			intervals.append(qr.getMappedName() + "\n");
    		}
    		this.geneDict.put(username, intervals.toString());
    		
    	} else {
    		response.setStatus(998);
    		this.geneDict.put(username,"");
    	}
    }
    
    
    @RequestMapping(value="loadExistingSettings",method=RequestMethod.GET) 
    @ResponseBody
    public QuerySettings getQuerySettings() throws Exception {
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = null;
    	
    	
    	System.out.println("Subject");
    	
    	//If user isn't authenticated, return nothing
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    	} else {
    		return null;
    	}
    	
    	System.out.println("Authenicate");
    	
    	String username = user.getUsername();
    	
    	//If settings are already loaded, simply return them
    	if (this.settingsDict.containsKey(username)) {
    		return this.settingsDict.get(username);
    	}
    	
    	System.out.println("No Hash");
    	
    	//If settings aren't loaded, check to see if they are serialized.
    	File settingsPath = new File(FileController.getQueryDirectory(),username + ".settings.ser");
    	if (settingsPath.exists()) {
    		FileInputStream fin = new FileInputStream(settingsPath);
    		ObjectInputStream ois = new ObjectInputStream(fin);
    		QuerySettings qs = (QuerySettings)ois.readObject();
    		ois.close();
    		this.settingsDict.put(username, qs);
    		System.out.println("Got Result");

    		return qs;
    	} else {
    		System.out.println("No Result");
    		return null;
    	}	
    }
    
    @RequestMapping(value="loadExistingResults",method=RequestMethod.GET) 
    @ResponseBody
    public QueryResultContainer getQueryResults() throws Exception {
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = null;
    	
    	//If user isn't authenticated, return nothing
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    	} else {
    		return null;
    	}
    	
    	String username = user.getUsername();
    	
    	if (!regionDict.containsKey(username)) {
    		this.loadRegionDict(username);
    	}
    	
    	if (!geneDict.containsKey(username)) {
    		this.loadGeneDict(username);
    	}
    	
    	if (!queryWarningsDict.containsKey(username)) {
    		this.loadWarningsDict(username);
    	}
    	
    	//If settings are already loaded, simply return them
    	if (this.resultsDict.containsKey(username)) {
    		QueryResultContainer qrcSub = this.resultsDict.get(username).getQrcSubset(25, 0, "FDR");
    		return qrcSub;
    	}
    	
    	//If settings aren't loaded, check to see if they are serialized.
    	File resultsPath = new File(FileController.getQueryDirectory(),username + ".results.ser");
    	if (resultsPath.exists()) {
    		FileInputStream fin = new FileInputStream(resultsPath);
    		ObjectInputStream ois = new ObjectInputStream(fin);
    		QueryResultContainer qrc = (QueryResultContainer)ois.readObject();
    		ois.close();
    		this.resultsDict.put(username, qrc);		
    		
    		QueryResultContainer qrcSub = qrc.getQrcSubset(25, 0, "FDR");
    		
    		return qrcSub;
    	} else {
    		return null;
    	}	
    }
    
    private void loadGeneDict(String username) throws Exception {
    	File resultsPath = new File(FileController.getQueryDirectory(),username + ".genes.ser");
    	if (resultsPath.exists()) {
    		FileInputStream fin = new FileInputStream(resultsPath);
    		ObjectInputStream ois = new ObjectInputStream(fin);
    		String genes = (String)ois.readObject();
    		ois.close();
    		this.geneDict.put(username, genes);
    	}
    }
    
    private void loadRegionDict(String username) throws Exception {
    	File resultsPath = new File(FileController.getQueryDirectory(),username + ".regions.ser");
    	if (resultsPath.exists()) {
    		FileInputStream fin = new FileInputStream(resultsPath);
    		ObjectInputStream ois = new ObjectInputStream(fin);
    		String regions = (String)ois.readObject();
    		ois.close();
    		this.regionDict.put(username, regions);
    	}
    }
    
    private void loadWarningsDict(String username) throws Exception {
    	File resultsPath = new File(FileController.getQueryDirectory(),username + ".warnings.ser");
    	if (resultsPath.exists()) {
    		FileInputStream fin = new FileInputStream(resultsPath);
    		ObjectInputStream ois = new ObjectInputStream(fin);
    		StringBuilder warnings = (StringBuilder)ois.readObject();
    		ois.close();
    		this.queryWarningsDict.put(username, warnings);
    	}
    }
    
    
    public IgvSessionResult createIgvSessionFile(String username, File sessionsDirectory, String serverName) throws Exception {
    	//Make sure genome is loaded
    	
    	
    	//Create result 
    	IgvSessionResult igvSR = new IgvSessionResult();
    	
    	StringBuilder warnings = new StringBuilder("");
    	StringBuilder errors = new StringBuilder("");
    	
    	//Create file handle to actual file
    	String fileName = username + "_igv.xml";
    	File localDirectory = FileController.getIgvDirectory();
    	File sessionFile = new File(localDirectory,fileName);
    	igvSR.setSessionFile(sessionFile);
    	
    	
    	if (!sessionsDirectory.exists()) {
    		Process process = Runtime.getRuntime().exec( new String[] { "ln", "-s", localDirectory.getAbsolutePath(), sessionsDirectory.getAbsolutePath() } );
    		process.waitFor();
    	    process.destroy();
    	}
    	
    	//Grab the stored analyses
    	List<Analysis> analyses = this.analysisService.getAllAnalyses();
    	QueryResultContainer results = null;
    	
    	if (this.resultsDict.containsKey(username)) {
    		results = this.resultsDict.get(username);
    	} else {
    		errors.append(String.format("This user %s doesn't appear to have any stored analyses, IGV session can't be created.",username));
    		igvSR.setError(errors.toString());
    		return igvSR;
    	}
    	
    	//Get datatracks list
    	List<Analysis> usedAnalyses = this.getUsedAnalyses(analyses, results.getResultList());
    	HashMap<String,String> datatracks = this.getDataTrackList(usedAnalyses);
    	
    	String igvBuildName = null;
    	if (usedAnalyses.size() != 0) {
    		OrganismBuild ob = usedAnalyses.get(0).getProject().getOrganismBuild();
    		if (!GenomeBuilds.doesGenomeExist(ob)) {
    			GenomeBuilds.loadGenome(ob);
    		}
    		igvBuildName = GenomeBuilds.getGenome(ob).getBuildName();
    		
    	} else {
    		errors.append("The stored analysis list is empty, session can't be created.");
    		igvSR.setError(errors.toString());
    		return igvSR;
    	}
    	
    	
    	
    	List<IGVResource> resources = new ArrayList<IGVResource>();
    	for (String name: datatracks.keySet()) {
    		URL datatrackURL = new URL("http://" + serverName + "/sessions/" + datatracks.get(name));
    		if (!urlExists(datatrackURL)) {
    			warnings.append(String.format("The datatrack %s does not exist or is inaccessable.<br/>", datatracks.get(name)));
    			continue;
    		}
    		
    		IGVResource igvResource = null;
    		String path = datatracks.get(name);
    		if (path.endsWith(".vcf.gz")) {
    			igvResource = new IGVResource(name, datatrackURL, null, false);
    		} else if (path.endsWith(".bw")) {
    			igvResource = new IGVResource(name, datatrackURL, null, true);
    		} else if (path.endsWith(".bb")) {
    			igvResource = new IGVResource(name, datatrackURL, null, false);
    		} else {
    			warnings.append(String.format("The datatrack %s does not have a recognized suffix.<br/>", datatracks.get(name)));
    		}
    		
    		if (igvResource != null) {
    			resources.add(igvResource);
    		}
    	}
    	igvSR.setWarnings(warnings.toString());
    	
    	//Make sure the session has some resources
    	if (resources.size() == 0) {
    		errors.append("None of the datatracks can be viewed in IGV.");
    		igvSR.setError(errors.toString());
    		return igvSR;
    	}
    	
    	//Add resources
    	IGVResource[] resourceArray = new IGVResource[resources.size()];
    	resourceArray = resources.toArray(new IGVResource[resources.size()]);
    	
    	//Create IGV session object
    	//Create session object
    	
    	
    	IGVSession igvSession = new IGVSession(igvBuildName);
    	igvSession.setIgvResources(resourceArray);
    	
    	
    	
    	//Write out session
    	igvSession.writeXMLSession(sessionFile);
    	
    	
    	//Construct the url
    	URL sessionUrl = new URL("http://" + serverName + "/sessions/" + fileName);
    	
    	igvSR.setUrl(igvSession.fetchIGVLaunchURL(sessionUrl).toString());
    	igvSR.setUrl2(sessionUrl.toString());
    	
    	
    	
    	return igvSR;
    }
    
   
    
    @RequestMapping(value="startIgvSession",method=RequestMethod.GET)
    @ResponseBody
    public IgvSessionResult startIgvSession(HttpServletResponse response, HttpServletRequest request) throws Exception {
    	String serverName = request.getLocalName();
    	if (serverName.equals("localhost")) {
    		serverName = "127.0.0.1:8080";
    	}
    	
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = null;
    	String username = "guest";
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    		username = user.getUsername();
    	}
    	
    	//Link hosted directory to local files
    	//Create file handle to hosted files
    	String rootDirectory = request.getSession().getServletContext().getRealPath("/");
    	File sessionsDirectory = new File(rootDirectory,"../sessions");
    
    	IgvSessionResult igr = this.createIgvSessionFile(username, sessionsDirectory, serverName);
    	if (igr.getError() != null) {
    		response.setStatus(405);
    	}
    	
    	this.dashboardService.increaseIgv();
    	
    	return igr;
    	
    	
    }
    
    /* Stolen from http://www.rgagnon.com/javadetails/java-0059.html */
    public boolean urlExists(URL url){
        try {
          HttpURLConnection.setFollowRedirects(false);
          
          HttpURLConnection con = (HttpURLConnection) url.openConnection();
          con.setRequestMethod("HEAD");
          return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
        	System.out.println ("[QueryController] the failing URL: " + url);
           e.printStackTrace();
           return false;
        }
    }
    
    private HashMap<String,String> getDataTrackList(List<Analysis> analyses) {
    	HashMap<String,String> pathDict = new HashMap<String,String>();
    	for (Analysis a: analyses) {
    		List<DataTrack> dts = a.getDataTracks();
			for (DataTrack dt: dts) {
				String dtName = dt.getName();
				String dtPath = a.getProject().getIdProject() + "/" + dt.getPath();
				if (!pathDict.containsKey(dtName)) {
					pathDict.put(dtName, dtPath);
				}
    		}
    	}
    	return pathDict;
    }
    
    private List<Analysis> getUsedAnalyses(List<Analysis> analyses, List<QueryResult> results) {
    	HashSet<Long> usedIds = new HashSet<Long>();
    	for (QueryResult qr: results) {
    		usedIds.add(qr.getIdAnalysis());
    	}
    	
    	List<Analysis> usedAnalyses = new ArrayList<Analysis>();
    	for (Analysis a: analyses) {
    		if (usedIds.contains(a.getIdAnalysis())) {
    			usedAnalyses.add(a);
    		}
    	}
    	
    	return usedAnalyses;
    }
    
    
    
    
    
     @RequestMapping(value = "downloadAnalysis", method = RequestMethod.GET)
	 public void downloadAnalysis(HttpServletRequest request, HttpServletResponse response, @RequestParam(value="codeResultType") String codeResultType) throws Exception{
    	
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = null;
    	String key = "guest";
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    		key = user.getUsername();
    	}
    	
    	if (this.fileDict.containsKey(key)) {
    		File fileToDelete = this.fileDict.get(key);
    		fileToDelete.delete();
    	}
    	
    	if (this.resultsDict.containsKey(key)) {
    		List<QueryResult> results = this.resultsDict.get(key).getResultList();
    		if (results.size() > 0) {
    			//Create results file
    			File localFile = new File(FileController.getDownloadDirectory(),key + ".query.xls.gz");
    			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(localFile))));
                
    			System.out.println(codeResultType);
    			
    			if (results.size() > 0) {
    				bw.write(results.get(0).writeGeneHeader());
    			}
    			for (QueryResult qr: results) {
    				bw.write(qr.writeGene());
    			}

    			bw.close();
    			
    			//Create session file
    			String serverName = request.getLocalName();
    	    	if (serverName.equals("localhost")) {
    	    		serverName = "127.0.0.1:8080";
    	    	}
    			
    	    	String rootDirectory = request.getSession().getServletContext().getRealPath("/");
    	    	File sessionsDirectory = new File(rootDirectory,"../sessions");
    			IgvSessionResult isr = this.createIgvSessionFile(key, sessionsDirectory, serverName);
    			boolean sessionOK = true;
    			if (isr.getError() != null) {
    				sessionOK = false;
    			}
    			
    			
    			//Write out as zip
    			File zipFile = new File(FileController.getDownloadDirectory(),key + ".results.zip");
    			byte[] b = new byte[1024];
    			int count;
    			
    			GZIPInputStream in1 = new GZIPInputStream(new FileInputStream(localFile));
    			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
    			out.putNextEntry(new ZipEntry(key + ".query.xls"));
    		
    			while((count = in1.read(b)) > 0) {
    				out.write(b,0,count);
    			}
    			out.closeEntry();
    			in1.close();
    			
    			if (sessionOK) {
    				FileInputStream in2 = new FileInputStream(isr.getSessionFile());
    				out.putNextEntry(new ZipEntry(key + ".session.xml"));
    				
    				while((count = in2.read(b)) > 0) {
        				out.write(b,0,count);
        			}
        			out.closeEntry();
        			in2.close();
    			}
    			
    			out.close();

    			try {		
    			 	//response.setContentType(getFile.getFileType());
    			 	response.setHeader("Content-disposition", "attachment; filename=\""+ key + ".results.zip"+"\"");
    			 	
    			 	BufferedInputStream bis = new BufferedInputStream(new FileInputStream(zipFile));
    			 	
    		        FileCopyUtils.copy(bis, response.getOutputStream());
    		        
    		        this.fileDict.put(key, zipFile);
    		        
    			 }catch (IOException e) {
    				e.printStackTrace();
    			 }
    			
    		}
    	}
	 }
     
    
    
    @RequestMapping(value = "changeTablePosition",method=RequestMethod.GET)
    @ResponseBody
    public QueryResultContainer changeTablePosition(
    		@RequestParam(value="resultsPerPage") Integer resultsPerPage,
    		@RequestParam(value="pageNum") Integer pageNum,
    		@RequestParam(value="sortType") String sortType
    ) {
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = null;
    	String key = "guest";
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    		key = user.getUsername();
    	}
    	
    	System.out.println(key);

    	QueryResultContainer qrc = null;
    	if (this.resultsDict.containsKey(key)) {
    		
    		

    		QueryResultContainer full = this.resultsDict.get(key);
    		
    		qrc = full.getQrcSubset(resultsPerPage, pageNum, sortType);
    	} else {
    		queryWarningsDict.put(key, new StringBuilder(""));
    		queryWarningsDict.get(key).append("There aren't any available results stored for this user<br/>");
    	}
    	
    	return qrc;
    }
    
    @RequestMapping(value = "getQueryOrganismBuilds", method=RequestMethod.GET)
    @ResponseBody
    public List<OrganismBuild> getQueryOrganismBuilds (
    		@RequestParam(value="idAnalysisTypes") List<Long> idAnalysisTypes,
    		@RequestParam(value="idLabs") List<Long> idLabs,
    		@RequestParam(value="idProjects") List<Long> idProjects,
    		@RequestParam(value="idAnalyses") List<Long> idAnalyses,
    		@RequestParam(value="idSampleSources") List<Long> idSampleSources
    ) throws Exception {
    	
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = null;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    	} 
    	
    	List<OrganismBuild> obList = this.analysisService.getOrgansimBuildByQuery(user, idAnalysisTypes, idLabs, idProjects, idAnalyses, idSampleSources);
    	
    	return obList;
    }
    
    @RequestMapping(value = "getQueryLabs", method=RequestMethod.GET)
    @ResponseBody
    public List<Lab> getQueryLabs (
    		@RequestParam(value="idAnalysisTypes") List<Long> idAnalysisTypes,
    		@RequestParam(value="idProjects") List<Long> idProjects,
    		@RequestParam(value="idAnalyses") List<Long> idAnalyses,
    		@RequestParam(value="idSampleSources") List<Long> idSampleSources,
    		@RequestParam(value="idOrganismBuild") Long idOrganismBuild
    ) throws Exception {
    	
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = null;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    	} 
    	
    	List<Lab> obList = this.analysisService.getLabByQuery(user, idAnalysisTypes, idProjects, idAnalyses, idSampleSources, idOrganismBuild);
    	
    	return obList;
    }
    
    @RequestMapping(value = "getQueryProjects", method=RequestMethod.GET)
    @ResponseBody
    public List<Project> getQueryProjects (
    		@RequestParam(value="idAnalysisTypes") List<Long> idAnalysisTypes,
    		@RequestParam(value="idLabs") List<Long> idLabs,
    		@RequestParam(value="idAnalyses") List<Long> idAnalyses,
    		@RequestParam(value="idSampleSources") List<Long> idSampleSources,
    		@RequestParam(value="idOrganismBuild") Long idOrganismBuild
    ) throws Exception {
    	
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = null;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    	} 
    	
    	List<Project> projectList = this.analysisService.getProjectsByQuery(idLabs, idAnalyses, idSampleSources, idAnalysisTypes, idOrganismBuild, user );
    	
    	return projectList;
    }
    
    @RequestMapping(value = "getQueryAnalyses", method=RequestMethod.GET)
    @ResponseBody
    public List<Analysis> getQueryAnalyses (
    		@RequestParam(value="idAnalysisTypes") List<Long> idAnalysisTypes,
    		@RequestParam(value="idLabs") List<Long> idLabs,
    		@RequestParam(value="idProjects") List<Long> idProjects,
    		@RequestParam(value="idSampleSources") List<Long> idSampleSources,
    		@RequestParam(value="idOrganismBuild") Long idOrganismBuild
    ) throws Exception {
    	
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = null;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    	} 
    	
    	List<Analysis> analysisList = this.analysisService.getAnalysesByQuery(idLabs, idProjects,  idSampleSources, idAnalysisTypes, idOrganismBuild, user );
    	
    	return analysisList;
    }
    
    @RequestMapping(value = "getQueryAnalysisTypes", method=RequestMethod.GET)
    @ResponseBody
    public List<AnalysisType> getQueryAnalysisTypes (
    		@RequestParam(value="idLabs") List<Long> idLabs,
    		@RequestParam(value="idProjects") List<Long> idProjects,
    		@RequestParam(value="idAnalyses") List<Long> idAnalyses,
    		@RequestParam(value="idSampleSources") List<Long> idSampleSources,
    		@RequestParam(value="idOrganismBuild") Long idOrganismBuild
    ) throws Exception {
    	
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = null;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    	} 
    	
    	List<AnalysisType> analysisTypeList = this.analysisService.getAnalysisTypesByQuery(idLabs, idProjects, idAnalyses, idSampleSources, idOrganismBuild, user );
    	
    	
    	return analysisTypeList;
    }
    
    @RequestMapping(value = "getQuerySampleSource", method=RequestMethod.GET)
    @ResponseBody
    public List<SampleSource> getQuerySampleSource (
    		@RequestParam(value="idAnalysisTypes") List<Long> idAnalysisTypes,
    		@RequestParam(value="idLabs") List<Long> idLabs,
    		@RequestParam(value="idProjects") List<Long> idProjects,
    		@RequestParam(value="idAnalyses") List<Long> idAnalyses,
    		@RequestParam(value="idOrganismBuild") Long idOrganismBuild
    ) throws Exception {
    	
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = null;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    	} 
    	
    	List<SampleSource> sampleSourceList = this.analysisService.getSampleSourceByQuery(idLabs, idAnalyses, idProjects, idAnalysisTypes, idOrganismBuild, user );
    	
    	
    	return sampleSourceList;
    }
    
    @RequestMapping(value="getHugoNames",method=RequestMethod.GET)
    @ResponseBody
    public List<GeneNameModel> getHugoNames(@RequestParam(value="idOrganismBuild") Long idOrganismBuild) {
    	List<GeneNameModel> egList = null;
    	if (!this.searchDict.containsKey(idOrganismBuild)) {
    		OrganismBuild ob = this.organismBuildService.getOrganismBuildById(idOrganismBuild);
    		this.loadGeneNames(ob);
    	}
    	egList = this.searchDict.get(idOrganismBuild);
    	return egList;
    }
    
    
    
    private String convertListToString(List<String> listOfStuff) {
    	StringBuilder concatString = new StringBuilder("");
    	
    	//Convert regions and searches into a string
    	for (String r: listOfStuff) {
    		concatString.append("\n" + r);
    	}
    
    	//Clean up leading newline
    	String finalString = concatString.toString();
    	if (finalString.length() > 0) {
    		finalString = finalString.substring(1);
    	}
    	
    	return finalString;
    }
 
    
    private List<List<String>> getGeneIntervals(String names, Genome genome, String searchType, OrganismBuild ob) {
    	List<String> regions = new ArrayList<String>();
    	List<String> searches = new ArrayList<String>();
    	List<String> mapped = new ArrayList<String>();
    	
    	
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	String username;
    	User user = null;
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    		
    		this.activeUsers.put(user.getUsername(), currentUser);
    		username = user.getUsername();
    	} else {
    		username = "guest";
    	}
    	
    	String[] genes = names.split("\n");
    	List<String> cleanedGenes = new ArrayList<String>();
    	if (genes.length == 0) {
    		this.queryWarningsDict.put(username, new StringBuilder("The gene list is empty!<br/>"));
    		return null;
    	}
    	
    	for (String g: genes) {
    		String[] byNewLine = g.split("\n");
    		for (String newLine: byNewLine) {
    			String[] parts = newLine.split(",\\s*");
    			for (String p: parts) {
        			cleanedGenes.add(p.trim());
        		}	
    		}
    	}
    	
    	HashSet<String> missingGenes = new HashSet<String>();
    	
    	for (String name: cleanedGenes) {
    		if (missingGenes.contains(name)) {
    			continue;
    		}
    		//Lookup biominer id in dictionary.
        	HashSet<String> extIdFinalSet = new HashSet<String>();
    		if (nameLookupDict.containsKey(name)) {
    			//For each biominer id, return the ensembl identifer
    			for (Long id: nameLookupDict.get(name)) {
    				List<ExternalGene> eglist = this.externalGeneService.getEnsemblNamesById(id,"ensembl",ob.getIdOrganismBuild());
 
    				for (ExternalGene eg: eglist) {
    					extIdFinalSet.add(eg.getExternalGeneName());
    				}
    			}
    			
    			//Warning if no ensembl names can be found
    			if (extIdFinalSet.size() == 0) {
    				this.queryWarningsDict.get(username).append("Could not find an Ensembl identifier for gene: '" + name + "'<br/>");
					continue;
    			}
    			
    		} else {
    			//Warning if no biominer genes can be found
    			missingGenes.add(name);
    			this.queryWarningsDict.get(username).append("The gene '" + name + "' could not be found in our database<br/>");
    			continue;
    		}
    		
    		
    		
        
        	HashMap<String, Gene> geneNameGene = genome.getTranscriptomes()[0].getGeneNameGene();
       
        	for (String ensemblName: extIdFinalSet) {
        		
        		
        		if (geneNameGene.containsKey(ensemblName)) {
        			Gene gene = geneNameGene.get(ensemblName);
        			String region = null;
        			if (searchType.equals("TxBoundary")) {
        				region = geneByTxBoundary(gene);
        			} else if (searchType.equals("CdsBoundary")) {
        				region = geneByCdsBoundary(gene);
        			} else if (searchType.equals("TxStart")) {
        				region = geneByTxStart(gene);
        			} else if (searchType.equals("TxEnd")) {
        				region = geneByTxEnd(gene);
        			}
        			regions.add(region);
        			searches.add(name);
        			mapped.add(ensemblName);
        			
        		} else {
        			//this.warnings.append("Could not find gene: '" + ensemblName + "' in Genome Object.\n");
        		}
        	}
    	}
    	
    	
    	List<List<String>> returnArray = new ArrayList<List<String>>();
    	returnArray.add(regions);
    	returnArray.add(searches);
    	returnArray.add(mapped);
    	
    	return returnArray;
    	
    }
    
    private String geneByTxBoundary(Gene gene) {
    	Transcript t = gene.getMergedTranscript();
    	int start = t.getTxStart();
    	int end = t.getTxEnd();
    	String chrom = t.getChrom();
    	
    	String region = String.format("%s:%d-%d", chrom, start, end);
    	return region;
    }
    
    private String geneByCdsBoundary(Gene gene) {
    	Transcript t = gene.getMergedTranscript();
    	int start = t.getCdsStart();
    	int end = t.getCdsEnd();
    	String chrom = t.getChrom();
    	
    	String region = String.format("%s:%d-%d", chrom, start, end);
    	return region;
    }
    
    private String geneByTxStart(Gene gene) {
    	Transcript t = gene.getMergedTranscript();
    	int start = t.getTxStart();
    	int end = t.getTxStart();
    	String chrom = t.getChrom();
    	
    	String region = String.format("%s:%d-%d", chrom, start, end);
    	return region;
    }
    
    private String geneByTxEnd(Gene gene) {
    	Transcript t = gene.getMergedTranscript();
    	int start = t.getTxEnd();
    	int end = t.getTxEnd();
    	String chrom = t.getChrom();
    	
    	String region = String.format("%s:%d-%d", chrom, start, end);
    	return region;
    }
    
    
    
    
    	
    private ArrayList<HashMap<String,IntervalTree<GenericResult>>> generateIntervalTrees(List<Analysis> analyses, Genome genome) throws Exception{
    	ArrayList<HashMap<String,IntervalTree<GenericResult>>> itList = new ArrayList<HashMap<String,IntervalTree<GenericResult>>>();
    	for (Analysis a: analyses) {
    		if (!IntervalTrees.doesChipIntervalTreeExist(a)) {
    			IntervalTrees.loadChipIntervalTree(a, genome);
    		}
    		itList.add(IntervalTrees.getChipIntervalTree(a));
    	}	
    	return itList;
    }
    
    private List<QueryResult> filterGene(List<QueryResult> results, List<String> mappedGenes) {
    	HashSet<String> cleanGeneSet = new HashSet<String>();
    	
    	for (String cg: mappedGenes) {
    		cleanGeneSet.add(cg);
    	}
    	
    	List<QueryResult> filteredResults = new ArrayList<QueryResult>();
    	for (QueryResult qr: results) {
    		if (qr.getMappedName() == null) {
    			continue;
    		}
    		if (cleanGeneSet.contains(qr.getMappedName())) {
    			filteredResults.add(qr);
    		}
    	}
    	
    	return filteredResults;
    }
    
   
    private List<QueryResult> filterFdr(List<QueryResult> results, Float fdr, String fdrCode) throws Exception {
    	List<QueryResult> filteredResults = new ArrayList<QueryResult>();
    	NumberFormat formatter = new DecimalFormat("0.##E0");
    	for (QueryResult qr: results) {
  
    		Double readFdr = null;
    		String rawFdr = qr.getFDR();
    		if (rawFdr == null) {
    			filteredResults.add(qr);
    		} else {
    			try {
	    		  readFdr = formatter.parse(rawFdr).doubleValue();
	    		} catch (Exception ex) {
	    			throw new Exception("Could not parse FDR value: " + qr.getFDR());
	    		}
	    		boolean pass = true;
	    		if (fdrCode.equals("GT")) {
	    			if (readFdr <= fdr) {
	    				pass = false;
	    			}
	    		} else {
	    			if (readFdr >= fdr) {
	    				pass = false;
	    			}
	    		}
	    		if (pass) {
	    			filteredResults.add(qr);
	    		}
    		}
    	}
    	return filteredResults;
    }
    
    private List<QueryResult> filterLog2Ratio(List<QueryResult> results, Float log2ratio, String log2ratioCode) {
    	List<QueryResult> filteredResults = new ArrayList<QueryResult>();
    	for (QueryResult qr: results) {
    		boolean pass = true;
    	
    		if (qr.getLog2Ratio() == null) {
    			filteredResults.add(qr);
    			pass = false;
    		} else if (log2ratioCode.equals("GT")) {
    			if (qr.getLog2Ratio() < log2ratio) {
    				pass = false;
    			}
    		} else if (log2ratioCode.equals("LT")) {
    			if (qr.getLog2Ratio() > log2ratio) {
    				pass = false;
    			}
    		} else {
    			if (Math.abs(qr.getLog2Ratio()) < log2ratio) {
    				pass = false;
    			}
    		}
    		if (pass) {
    			filteredResults.add(qr);
    		}
    	}
    	return filteredResults;
    }
    
    
    
    
    private List<QueryResult> getIntersectingRegions(ArrayList<HashMap<String,IntervalTree<GenericResult>>> treeList, List<Analysis> analyses,
    		List<LocalInterval> localIntervals, boolean reverse) throws Exception{
    	
    	//Container for matches
    	HashMap<GenericResult,Integer[]> grMap = new HashMap<GenericResult,Integer[]>();
    	
    	//Generate match of unique matches
    	for (int i=0; i<treeList.size(); i++) {
    		HashMap<String, IntervalTree<GenericResult>> it = treeList.get(i);
    		for (int j=0; j<localIntervals.size(); j++) {
    			LocalInterval inv = localIntervals.get(j); 
    			String chrom = inv.getChrom();
    			if (it.containsKey(chrom)) {
    				List<GenericResult> hits = it.get(chrom).search(inv.getStart(), inv.getEnd());
    				
        			for (GenericResult gr: hits) {
        				if (!grMap.containsKey(gr)) {
        					Integer[] indexes = new Integer[]{i,j};
        					grMap.put(gr,indexes);
        				}	
        			}
    			}	
    		}
    	}
    	
    	
    	List<QueryResult> qrList;
    	if (reverse) {
    		//If reverse, generate does not match list!
        	HashMap<GenericResult,Integer[]> grMapRev = new HashMap<GenericResult,Integer[]>();
    		for (int i=0; i<treeList.size(); i++) {
    			for (IntervalTree<GenericResult> it: treeList.get(i).values()) {
    				for (Interval<GenericResult> inv: it.getInterval()) {
    					if (!grMapRev.containsKey(inv)) {
    						Integer[] indexes = new Integer[]{i,-1};
    						grMapRev.put(inv.getValue(), indexes);
    					}
    				}
    			}
    		}
    		
    		for (GenericResult gr: grMap.keySet()) {
    			if (grMapRev.containsKey(gr)) {
    				grMapRev.remove(gr);
    			}
    		}
    		
    		grMap = grMapRev;
    		localIntervals = null;
    	} 
    	
    	qrList = this.convertGenericToQuery(grMap, analyses, localIntervals);
    	return qrList;
    }
    
    private List<QueryResult> getIntersectingRegionsExisting(HashMap<String,IntervalTree<QueryResult>> tree, 
    		List<LocalInterval> localIntervals, boolean reverse) throws Exception{
    	
    	//Container for matches
    	HashSet<QueryResult> grMap = new HashSet<QueryResult>();
    	
    	for (int j=0;j<localIntervals.size();j++) {
    		LocalInterval inv = localIntervals.get(j);
    		String chrom = inv.getChrom();
    		if (tree.containsKey(chrom)) {
    			List<QueryResult> hits = tree.get(chrom).search(inv.getStart(),inv.getEnd());
    			
    			for (QueryResult hit: hits) {
    				if (!grMap.contains(hit)) {
    					hit.setSearch(hit.getSearch() + ";" + inv.getSearch());
    					grMap.add(hit);
    				}
    			}
    		}
    	}
    	
    	if (reverse) {
    		//If reverse, generate does not match list!
        	HashSet<QueryResult> grMapRev = new HashSet<QueryResult>();
        	
        	for (IntervalTree<QueryResult> it: tree.values()) {
				for (Interval<QueryResult> inv: it.getInterval()) {
					QueryResult qr = inv.getValue();
					if (!grMapRev.contains(qr)) {
						qr.setSearch(qr.getSearch() + ";NA");
						grMapRev.add(qr);
					}
				}
			}
    		
    		for (QueryResult qr: grMap) {
    			if (grMapRev.contains(qr)) {
    				grMapRev.remove(qr);
    			}
    		}
    		
    		grMap = grMapRev;
    	} 
    	
    	List<QueryResult> qrList = new ArrayList<QueryResult>();
    	qrList.addAll(grMap);
    	
    	return qrList;
    }
    
    private HashMap<String,IntervalTree<QueryResult>> createQueryResultIntervalTree(List<QueryResult> results) {
    	HashMap<String,ArrayList<Interval<QueryResult>>> resultIntervalData = new HashMap<String,ArrayList<Interval<QueryResult>>>();
    	
    	for (QueryResult qr: results) {
    		String chrom = qr.getChrom();
    		Interval<QueryResult> newInv = new Interval<QueryResult>(qr.getStart(),qr.getEnd(),qr);
    		if (!resultIntervalData.containsKey(chrom)) {
    			resultIntervalData.put(chrom, new ArrayList<Interval<QueryResult>>());
    		} 
    		resultIntervalData.get(chrom).add(newInv);
    	}
    	
    	HashMap<String,IntervalTree<QueryResult>> resultIntervalTree = new HashMap<String,IntervalTree<QueryResult>>();
    	for (String chrom: resultIntervalData.keySet()) {
    		IntervalTree<QueryResult> newTree = new IntervalTree<QueryResult>(resultIntervalData.get(chrom),false);
    		resultIntervalTree.put(chrom,newTree);
    	}
    	
    	return resultIntervalTree;
    }
 
    
    private List<QueryResult> convertGenericToQuery(HashMap<GenericResult,Integer[]> grHash, List<Analysis> analyses, List<LocalInterval> intervalList) {
    	List<QueryResult> qrList = new ArrayList<QueryResult>();
    	
    	int index = 0;
    	for (GenericResult gr: grHash.keySet()) {
    		Integer[] indexes = grHash.get(gr);
    		Analysis a = analyses.get(indexes[0]);
    		
    		
    		
    		QueryResult result = new QueryResult();
			result.setIndex(index++);
    		result.setProjectName(a.getProject().getName());
    		result.setIdAnalysis(a.getIdAnalysis());
    		result.setAnalysisType(a.getAnalysisType().getType());
    		result.setAnalysisName(a.getName());
    		result.setAnalysisSummary(a.getDescription());
    		HashSet<String> conditions = new HashSet<String>();
    		String conditionString = "";
    		for (Sample sample: a.getSamples()) {
    			String cond = sample.getSampleCondition().getCond();
    			if (!conditions.contains(cond)) {
    				conditions.add(cond);
    				conditionString += cond + ",";
    			}
    		}
    		result.setSampleConditions(conditionString.substring(0,conditionString.length()-1));
    		String coordinate = gr.getChrom() + ":" + String.valueOf(gr.getStart()) + "-" + String.valueOf(gr.getStop());
    		result.setCoordinates(coordinate);
    		result.setFDR(gr.getTransFDR());
    		result.setLog2Ratio(gr.getLog2Rto());
    		result.setMappedName(gr.getMappedName());
    		
    		//If interval list is set ( shouldn't be for does not match), set search parameter
    		if (intervalList == null) {
    			result.setSearch("NA");
    		} else {
    			LocalInterval inv = intervalList.get(indexes[1]);
    			result.setSearch(inv.getSearch());
    		}
    		qrList.add(result);
    	}
    	
    	return qrList;
    }
   
    
    private class IntervalParser {
    	private Pattern pattern1 = Pattern.compile("^(chr)*(.+?)(:|\\s+)(\\d+)(-|\\s+)(\\d+)$");
    	private Pattern pattern2 = Pattern.compile("^(chr)*(.+)$");

  
    	private StringBuilder warnings = new StringBuilder("");
    	
    	
    	
    	public List<LocalInterval> parseIntervals(String region, String search, Integer regionMargin, Genome genome) throws Exception {
    		List<LocalInterval> localIntervals = new ArrayList<LocalInterval>();
    		
  
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
    				if (chrom.startsWith("chr")) {
    					continue;
    				}
    				LocalInterval inv = new LocalInterval(chrom,0,genome.getNameChromosome().get(chrom).getLength(),"anything");
    				localIntervals.add(inv);
    			}
    		} else {
    			String[] regionList = region.split("\n");
    			String[] searchList = search.split("\n");
    			
    			if (regionList.length != searchList.length) {
    				throw new Exception(String.format("Region and search lists don't match: %d vs %d",regionList.length,searchList.length));
    			}
    			
    			for (int i=0;i<regionList.length;i++) {		
    				String r = regionList[i];
    				String s = searchList[i];
    				
    				r = r.replace(",", "");
    				
    				Matcher m1 = pattern1.matcher(r);
    	    		Matcher m2 = pattern2.matcher(r);
    	    		
    	    		
    	    		if (m1.matches()) {
    	    		
    	    			int end = 0;
    	    			int start = 0;
    	    			try {
    	    				start = Integer.parseInt(m1.group(4)) - regionMargin;
    	    			} catch (NumberFormatException nfe) {
    	    				warnings.append(String.format("Start boundary not an integer %s, skipping.<br/>",m1.group(2)));
    	    				continue;
    	    			}
    	    			
    	    			try {
    	    				end = Integer.parseInt(m1.group(6)) + regionMargin;
    	    			} catch (NumberFormatException nfe) {
    	    				warnings.append(String.format("End boundary not an integer %s, skipping.<br/>",m1.group(3)));
    	    				continue;
    	    			}
    	    			
    	    			String chrom = m1.group(2);
    	    			
    	    			if (!genome.getNameChromosome().containsKey(chrom)) {
    	    				warnings.append(String.format("The chromsome %s could not be found in the genome %s.<br/>",chrom,genome.getBuildName()));
    	    				continue;
    	    			}
    	    			if (start >= end) {
    	    				warnings.append(String.format("The start coordinate (%d) is greater or equal to the end coordinate (%d).<br/>",start,end));
    	    				continue;
    	    			}
    	    			
    	    			if (start < 0) {
    	    				warnings.append(String.format("The start coordinate ( %d ) is less than 0.  Setting to zero.<br/>",start));
    	    				start = 0;
    	    			} 
    	    			if (end > genome.getNameChromosome().get(chrom).getLength()) {
    	    				warnings.append(String.format("The end coordinate ( %d) is greater than the chromsome length (%d). Setting to chromsome"
    	    						+ " end.<br/>",end,genome.getNameChromosome().get(chrom).getLength()));
    	    				end = genome.getNameChromosome().get(chrom).getLength();
    	    			}  
    	    			
    	    			LocalInterval inv = new LocalInterval(chrom,start,end,s);
    	    			localIntervals.add(inv);
    	    			
    	    		} else if (m2.matches()) {
    	    			String chrom  = m2.group(2);
    	    			
    	    			if (!genome.getNameChromosome().containsKey(chrom)) {
    	    				warnings.append(String.format("The chromsome %s could not be found in the genome %s.<br/>",chrom,genome.getBuildName()));
    	    				continue;
    	    			}
    	    			
    	    			int start = 0;
    	    			int end = genome.getNameChromosome().get(chrom).getLength();
    	    			
    	    			LocalInterval inv = new LocalInterval(chrom,start,end,s);
    	    			localIntervals.add(inv);

    	    			
    	    		} else {
    	    			warnings.append(String.format("The string %s does not match a region format.<br/>",region));
    	    			
    	    		}
    			}
    		}
    		
    		return localIntervals;
    	}
    	
    	public StringBuilder getWarnings() {
    		return warnings;
    	}
    }
    
    private class LocalInterval {
    	private String chrom;
    	private int start;
    	private int end;
    	private String search; //what was searched to get this interval 

	
    	public LocalInterval(String chrom, int start, int end, String search) {
    		this.chrom = chrom;
    		this.start = start;
    		this.end = end;
    		this.search = search;
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
    	
    	public String getSearch() {
    		return this.search;
    	}
    }
    
    private class SessionCheckerTimer {
    	private long delay = 60000;
    	private Timer timer = new Timer();
    	private SessionChecker sc = new SessionChecker();
    	private Date date;
    	
    	public SessionCheckerTimer(Date date) {
    		this.date = date;
    	}
    	
    	public void start() {
    		timer.cancel();
    		timer = new Timer();
    		Date executionDate = new Date();
    		timer.scheduleAtFixedRate(sc, executionDate, delay);
    	}
    	
    	public void stop() {
    		timer.cancel();
    		
    		for (String key: activeUsers.keySet()) {
    			writeData(key);
    		}
    	}
    	
    	public void writeData(String key) {
    		if (settingsDict.containsKey(key)) {
				//First write out serialized objects
				try {
					File settingsPath = new File(FileController.getQueryDirectory(), key + ".settings.ser");
        			FileOutputStream fos = new FileOutputStream(settingsPath);
        			ObjectOutputStream oos = new ObjectOutputStream(fos);
        			oos.writeObject(settingsDict.get(key));
        			oos.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
    			
    			//Remove settings from memory
				settingsDict.remove(key);
			}
			
			if (resultsDict.containsKey(key)) {
				//First write out serialized objects
				try {
					File resultsPath = new File(FileController.getQueryDirectory(), key + ".results.ser");
        			FileOutputStream fos = new FileOutputStream(resultsPath);
        			ObjectOutputStream oos = new ObjectOutputStream(fos);
        			oos.writeObject(resultsDict.get(key));
        			oos.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
    			
    			//Remove settings from memory
				resultsDict.remove(key);
			}
		
			if (regionDict.containsKey(key)) {
				try {
					File resultsPath = new File(FileController.getQueryDirectory(),key + ".regions.ser");
					FileOutputStream fos = new FileOutputStream(resultsPath);
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(regionDict.get(key));
					oos.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				regionDict.remove(key);
			}
			
			if (geneDict.containsKey(key)) {
				try {
					File resultsPath = new File(FileController.getQueryDirectory(),key + ".genes.ser");
					FileOutputStream fos = new FileOutputStream(resultsPath);
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(geneDict.get(key));
					oos.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				geneDict.remove(key);
			}
			
			if (queryWarningsDict.containsKey(key)) {
				try {
					File resultsPath = new File(FileController.getQueryDirectory(),key + ".warnings.ser");
					FileOutputStream fos = new FileOutputStream(resultsPath);
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(queryWarningsDict.get(key));
					oos.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				queryWarningsDict.remove(key);
			}
    	}
    	
    	private class SessionChecker extends TimerTask {
        	public void run() {
            	HashMap<String,Subject> updatedUsers = new HashMap<String,Subject>();
            	
            	for (String key: activeUsers.keySet()) {
            		Subject s = activeUsers.get(key);
            		
            		try {
            			s.getPrincipal();
            			updatedUsers.put(key, s);
            		} catch (Exception authEx) {
            			writeData(key);	
            		}

            	}
            	activeUsers = updatedUsers;
            }
        }
    }
    
    
}
