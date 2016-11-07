package hci.biominer.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
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

import returnModel.HomologyModel;
import returnModel.IgvSessionResult;
import returnModel.JBrowseReturnData;
import returnModel.QueryResultContainer;
import returnModel.QuerySettings;
import hci.biominer.model.Analysis;
import hci.biominer.model.AnalysisType;
import hci.biominer.model.DataTrack;
import hci.biominer.model.GeneIdConversion;
import hci.biominer.model.GeneNameModel;
import hci.biominer.model.GenericResult;
import hci.biominer.model.LocalInterval;
import hci.biominer.model.OrganismBuild;
import hci.biominer.model.Project;
import hci.biominer.model.QueryResult;
import hci.biominer.model.Sample;
import hci.biominer.model.SampleSource;
import hci.biominer.model.TransFactor;
import hci.biominer.model.access.Lab;
import hci.biominer.model.access.User;
import hci.biominer.model.genome.Gene;
import hci.biominer.model.genome.Genome;
import hci.biominer.model.genome.Transcript;
import hci.biominer.model.intervaltree.Interval;
import hci.biominer.model.intervaltree.IntervalTree;
import hci.biominer.model.ExternalGene;
import hci.biominer.parser.BedLocalIntervalParser;
import hci.biominer.parser.HomologyParser;
import hci.biominer.service.DashboardService;
import hci.biominer.service.GeneIdConversionService;
import hci.biominer.service.OrganismBuildService;
import hci.biominer.service.LabService;
import hci.biominer.service.TransFactorService;
import hci.biominer.service.UserService;
import hci.biominer.service.AnalysisService;
import hci.biominer.service.AnalysisTypeService;
import hci.biominer.service.ExternalGeneService;
import hci.biominer.util.BiominerProperties;
import hci.biominer.util.Enumerated.AnalysisTypeEnum;
import hci.biominer.util.GenomeBuilds;
import hci.biominer.util.IO;
import hci.biominer.util.IntervalTrees;
import hci.biominer.util.JBrowseUtilities;
import hci.biominer.util.RegionUpload;
import hci.biominer.util.SessionData;
import hci.biominer.util.igv.IGVResource;
import hci.biominer.util.igv.IGVSession;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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
    
    @Autowired
    private TransFactorService tfService;
    
    @Autowired
    private GeneIdConversionService geneIdConversionService;
    
    //private StringBuilder warnings = new StringBuilder("");
    
    //private HashMap<String,QueryResultContainer> resultsDict =  new HashMap<String,QueryResultContainer>();
    //private HashMap<String,StringBuilder> queryWarningsDict = new HashMap<String,StringBuilder>();
    //private HashMap<String,QuerySettings> settingsDict = new HashMap<String,QuerySettings>();
    //private HashMap<String,String> regionDict = new HashMap<String,String>();
    //private HashMap<String,String> geneDict = new HashMap<String,String>();
    
    private HashMap<String,HashMap<String,SessionData>> currentSessions = new HashMap<String,HashMap<String,SessionData>>();
    private HashMap<String,Subject> activeUsers = new HashMap<String,Subject>();
    private HashMap<String,ArrayList<QueryResult>> homologyResult = new HashMap<String,ArrayList<QueryResult>>();
    
    
    private HashMap<String,File> fileDict = new HashMap<String,File>();
    private HashMap<Long,List<GeneNameModel>> searchDict = new HashMap<Long,List<GeneNameModel>>();
    private HashMap<String,ArrayList<Long>> nameLookupDict = new HashMap<String,ArrayList<Long>>();
    private HashMap<String,String> ensembl2NameDict = new HashMap<String,String>();
    private HashMap<Long,String> bidToEnsembl = new HashMap<Long,String>();
    
    
    
    
    
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
    	System.out.println("Finished loading names");
    	
    	//Iterate through external genes and extract information on associated gene name and ensembl name
    	HashSet<String> uniqueNamesHash = new HashSet<String>();
    	HashMap<Long,String> associatedLink = new HashMap<Long,String>();
    	for (ExternalGene eg: egList) {
    		String name = eg.getExternalGeneName();
    		String source = eg.getExternalGeneSource();
    		if (source.equals("hugo")) {
    			uniqueNamesHash.add(name);
    			associatedLink.put(eg.getIdBiominerGene(), name);
    		} else if (source.equals("ensembl")) {
    			bidToEnsembl.put(eg.getIdBiominerGene(), name);
    		} 
    		if (!nameLookupDict.containsKey(name)) {
    			nameLookupDict.put(name, new ArrayList<Long>());
    		}
    		nameLookupDict.get(name).add(eg.getIdBiominerGene());	
    	}
    	egList = null;
    	
    	//Sort associated gene names and store in a list for angular.
    	List<String> uniqueNamesSort = new ArrayList<String>();
    	uniqueNamesSort.addAll(uniqueNamesHash);
    	Collections.sort(uniqueNamesSort);
    	uniqueNamesHash = null;
    	
    	List<GeneNameModel> uniqueNamesList = new ArrayList<GeneNameModel>();
    	for (String name: uniqueNamesSort) {
    		GeneNameModel gnm = new GeneNameModel(name);
    		uniqueNamesList.add(gnm);
    	}
    	uniqueNamesSort = null;
    	searchDict.put(ob.getIdOrganismBuild(), uniqueNamesList);
    	
    	//Create lookup table ensembl to hugo
    	for (Long id: bidToEnsembl.keySet()) {
    		if (associatedLink.containsKey(id)) {
    			ensembl2NameDict.put(bidToEnsembl.get(id),associatedLink.get(id));
    		}
    	}
    	
    	

    }
  
    @RequestMapping(value="/clearNames",method=RequestMethod.POST)
    @ResponseBody
    public void clearNames(@RequestParam("obId") Long obId ) {
    	if (searchDict.containsKey(obId)) {
    		searchDict.remove(obId);
    	}
    }
    
    @RequestMapping(value="/layout",produces="text/plain")
    public String getQueryPartialPage(ModelMap modelMap) {
        return "query/layout";
    }
    
    @RequestMapping(value="warnings",method=RequestMethod.GET,produces="text/plain")
    @ResponseBody
    public String getWarnings(@RequestParam("idTab") String idTab) throws Exception{
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
    	
    	String warnings = "";
    	if (currentSessions.containsKey(username)) {
    		if (currentSessions.get(username).containsKey(idTab)) {
    			warnings = currentSessions.get(username).get(idTab).getQueryWarnings().toString();
    		} else {
    			warnings = fetchMostRecentSession(username).getQueryWarnings().toString();
    		}
    	}
    	return warnings;
    }
    
    @RequestMapping(value="uploadGene",method=RequestMethod.POST)
    @ResponseBody
    public RegionUpload parseGenes(HttpServletResponse response, @RequestParam("file") MultipartFile file, @RequestParam("idTab") String idTab) {
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
    	
    	//gene count
    	int geneCounter = 0;
    	
    	//failure flag
    	boolean uploadFailed = false;
    	StringBuilder first10 = new StringBuilder("");
    	
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
    			
    			while ((temp = br.readLine()) != null) {
    				if (!IO.isASCII(temp)) {
						regions.setMessage("The file appears to be binary.  Biominer accepts text files or bed files.  Can be gzipped or zipped.");
						response.setStatus(500);
						uploadFailed = true;
    				}
    				
    				
    				String[] parts = temp.split("\\s+");
    				for (String p: parts) {
    					geneString.append(p.toUpperCase() + "\n");
    					if (geneCounter < 10) {
    						first10.append(p + "<br>");
    					}
    					geneCounter += 1;
    					break;
    				}
    				
    			}
    			
    			if (!uploadFailed) {
    				regions.setRegions(String.format("[Load genes from file: %s]",name));
    				if (!currentSessions.containsKey(username)) {
    					currentSessions.put(username, new HashMap<String,SessionData>());
    					SessionData sd = new SessionData();
    					sd.setGeneString(geneString.toString());
    					currentSessions.get(username).put(idTab, sd);
    				} else if (!currentSessions.get(username).containsKey(idTab)) {
    					SessionData sd = new SessionData();
    					sd.setGeneString(geneString.toString());
    					currentSessions.get(username).put(idTab, sd);
    				} else {
    					currentSessions.get(username).get(idTab).setGeneString(geneString.toString());
    				}
    				
    			}
    			
    			br.close();
    			
    		} catch (IOException ioex) {
    			regions.setMessage("Error reading file: " + ioex.getMessage());
    			ioex.printStackTrace();
    		}
    		
    		
    	} else {
    		regions.setMessage("File is empty!");
    	}
    	
    	if (uploadFailed) {
    		response.setStatus(500);
    	} else {
    		regions.setMessage("Uploaded " + geneCounter + " genes, see below for an example of what was uploaded. <br><br>" + first10.toString());
    	}
    	
    	return regions;
    }
    
    
    @RequestMapping(value="upload",method=RequestMethod.POST)
    @ResponseBody
    public RegionUpload parseRegions(HttpServletResponse response, @RequestParam("file") MultipartFile file, @RequestParam("idTab") String idTab) {
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
    	
    	//Count the number of regions uploaded
    	int regionCounter = 0;
    	StringBuilder first10 = new StringBuilder("");
    	
    	//flag if there was an error
    	boolean uploadFailed = false;
    	
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
    			
    			
    			while ((temp = br.readLine()) != null) {
    				Matcher m = pattern1.matcher(temp);
    				if (m.matches()) {
    					regionString.append(String.format("%s:%s-%s\n",m.group(1),m.group(3),m.group(5)));
    					if (regionCounter < 10) {
    						first10.append(String.format("%s:%s-%s\n",m.group(1),m.group(3),m.group(5)) + "<br>");
    					}
    					regionCounter += 1;
    				} else {
    					if (IO.isASCII(temp)) {
    						regions.setMessage(String.format("Could not parse region line: %s. The first three columns must be chromsome, "
        							+ "start coordinate and stop coordinate.  Can be tab, space, comma delimited or in the format "
        							+ "chr:start-end",temp));
    					} else {
    						regions.setMessage("The file appears to be binary.  Biominer accepts text files or bed files.  Can be gzipped or zipped.");
    					}
    					uploadFailed = true;
    					break;
    					
    				}
    			}
    			
    			if (!uploadFailed) {
    				regions.setRegions(String.format("[Load regions from file: %s]",name));
    				if (!currentSessions.containsKey(username)) {
    					currentSessions.put(username, new HashMap<String,SessionData>());
    					SessionData sd = new SessionData();
    					sd.setRegionString(regionString.toString());
    					currentSessions.get(username).put(idTab, sd);
    				} else if (!currentSessions.get(username).containsKey(idTab)) {
    					SessionData sd = new SessionData();
    					sd.setRegionString(regionString.toString());
    					currentSessions.get(username).put(idTab, sd);
    				} else {
    					currentSessions.get(username).get(idTab).setRegionString(regionString.toString());
    				}
    			}
    			
    			br.close();
    			
    			
    		} catch (IOException ioex) {
    			uploadFailed = true;
    			regions.setMessage("Error reading file: " + ioex.getMessage());
    			ioex.printStackTrace();
    		}
    		
    		
    	} else {
    		uploadFailed = true;
    		regions.setMessage("File is empty!");
    	}
    	
    	if (uploadFailed) {
    		response.setStatus(500);
    	} else {
    		regions.setMessage("Uploaded " + regionCounter + " regions, see below for an example of what was uploaded.<br><br>" + first10.toString());
    	}
    	
    	return regions;
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
        @RequestParam(value="regionMargins",required=false) Integer regionMargins,
        @RequestParam(value="genes") String genes,
        @RequestParam(value="geneMargins",required=false) Integer geneMargins,
        @RequestParam(value="idTransFactor",required=false) Long idTransFactor,
        @RequestParam(value="tfMargins",defaultValue="0") Integer tfMargins,
        @RequestParam(value="FDR",required=false) Float FDR,
        @RequestParam(value="codeFDRComparison") String codeFDRComparison,
        @RequestParam(value="log2Ratio",required=false) Float log2Ratio,
        @RequestParam(value="codeLog2RatioComparison") String codeLog2RatioComparison,
        @RequestParam(value="resultsPerPage") Integer resultsPerPage,
        @RequestParam(value="sortType") String sortType,
        @RequestParam(value="intersectionTarget") String target,
        @RequestParam(value="isReverse") boolean reverse,
        @RequestParam(value="searchExisting") boolean searchExisting,
        @RequestParam(value="idTab") String idTab,
        @RequestParam(value="sortReverse") boolean sortReverse
        ) throws Exception {
      
    	
    	
    	if (genes != null) {
    		genes = genes.toUpperCase();
    	}
    	
    	if (geneMargins == null) {
    		geneMargins = 0;
    	}
    	
    	if (regionMargins == null) {
    		regionMargins = 0;
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
    	SessionData sessionData = null;
    	if (!currentSessions.containsKey(username)) {
			currentSessions.put(username, new HashMap<String,SessionData>());
			sessionData = new SessionData();
		} else if (!currentSessions.get(username).containsKey(idTab)) {
			sessionData = new SessionData();
		} else {
			sessionData = currentSessions.get(username).get(idTab);
			sessionData.setQueryWarnings(new StringBuilder());
		}
    	
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
    			if (sessionData.getGeneString() != null) {
    				String loadedGenes = sessionData.getGeneString();
        			parsed = getGeneIntervals(loadedGenes, genome, "TxBoundary",ob, idTab);
    			}
    		    else {
    				sessionData.getQueryWarnings().append("Biominer expects genes loaded from file, but none could be found. Please try reloading your gene file.<br/>");
    			}

    		} else if (genes.equals("[ALL RESULT GENES]")) {
    			if (sessionData.getGeneString() != null) {
    				String loadedGenes = sessionData.getGeneString();
        			parsed = getGeneIntervals(loadedGenes, genome, "TxBoundary",ob, idTab);
    			} else {
    				sessionData.addWarning("Biominer expects genes copied from the previous query, but none could be found. Please submit a bug report.<br/>");
    			}
    		} else {
    			parsed = getGeneIntervals(genes, genome, "TxBoundary",ob, idTab);
    		}
    	 
    		if (parsed != null && parsed.get(0).size() != 0) {
    			mappedNames = parsed.get(2); //This will be used for gene based filtering, if necessary
        		intervalsToCheck = ip.parseIntervals(this.convertListToString(parsed.get(0)), this.convertListToString(parsed.get(1)), geneMargins, genome);
    		} 
    	} else if (codeResultType.equals("REGION"))  {
    		if (target.equals("TF")) {
    			System.out.println("TRANSCRIPTION FACTOR!!!!");
    			if (tfMargins == null) {
    				tfMargins = 0;
    			}
        		TransFactor tf = tfService.getTransFactorById(idTransFactor);
        		File bedFile = new File(FileController.getTfParseDirectory(),tf.getFilename());
        		BedLocalIntervalParser blip = new BedLocalIntervalParser(bedFile);
        		intervalsToCheck = blip.getLocalIntervals(tf.getName(), tfMargins);
    		} else {
    			if (regions.startsWith("[Load regions from file: ")) {
        			if (sessionData.getRegionString() != null) {
        				String loadedRegions = sessionData.getRegionString();
            			intervalsToCheck = ip.parseIntervals(loadedRegions, loadedRegions, regionMargins, genome);
        			} else {
        				sessionData.addWarning("Biominer expects regions loaded from file, but none could be found. Please try reloading your region file.<br/>");
        			}
        		} else if (regions.equals("[All result coordinates]")) {
        			if (sessionData.getRegionString() != null) {
        				String loadedRegions = sessionData.getRegionString();
            			intervalsToCheck = ip.parseIntervals(loadedRegions, loadedRegions, regionMargins, genome);
        			} else {
        				sessionData.addWarning("Biominer expects regions copied from the previous query , but none could be found. Please submit a bug report.<br/>");
        			}
        		} else {
        			intervalsToCheck = ip.parseIntervals(regions, regions, regionMargins, genome);
        		}
    		}
    	}
    	
    	//Add IP warnings
    	sessionData.addWarning(ip.getWarnings().toString());
    	
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
    		if (sessionData.getResults() != null) {
    			HashMap<String,IntervalTree<QueryResult>> resultTree = this.createQueryResultIntervalTree(currentSessions.get(username).get(idTab).getResults().getResultList());
    			results = this.getIntersectingRegionsExisting(resultTree, intervalsToCheck, reverse);
        	} else {
        		results = new ArrayList<QueryResult>();
        		sessionData.addWarning("Could not find any existing results, can't query existing results.<br/>");
    		}
    	} else {
    		results = this.getIntersectingRegions(itList, analyses, intervalsToCheck, reverse);
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
    		System.out.println("FILTERING");
    		results = this.filterGene(results, mappedNames);
    	}
    	
    	fullRegionResults.addAll(results);
    	    	
    	//Determine how many analyses were used
    	List<Analysis> usedAnalyses = this.getUsedAnalyses(analyses, fullRegionResults);
    	HashMap<String,String> usedDataTracks = this.getDataTrackList(usedAnalyses);
    	
    	QueryResultContainer qrc = new QueryResultContainer(fullRegionResults, fullRegionResults.size(), usedAnalyses.size(), usedDataTracks.keySet().size(), 0, sortType, true, idOrganismBuild, ob.getEnsemblCode(),ob.getOrganism().getBinomial(),sortReverse);
    	
    	//Create settings object
    	QuerySettings qs = new QuerySettings(codeResultType, target, idOrganismBuild, idAnalysisTypes, idLabs, idProjects,
    			idAnalyses, idSampleSources, idTransFactor, tfMargins, regions, regionMargins, genes, geneMargins, FDR, codeFDRComparison, log2Ratio, 
    			codeLog2RatioComparison,resultsPerPage, sortType, reverse, searchExisting);
    	
    	
    	sessionData.setResults(qrc);
    	sessionData.setSettings(qs);
    	sessionData.setLastTouched(new Date());
    	
    	currentSessions.get(username).put(idTab,sessionData);
    	
    	System.out.println("Used analyses " + usedAnalyses.size());
    	System.out.println("Used data tracks " + usedDataTracks.keySet().size());
    	
    	//update database
    	Date queryDate = new Date();
    	this.dashboardService.updateQueryDate(queryDate.getTime());
    	this.dashboardService.increaseQuery();
    	
    	//Create result object
    	QueryResultContainer qrcSub = qrc.getQrcSubset(resultsPerPage, 0, sortType,sortReverse);
    	
    	return qrcSub;	
    }
    
    @RequestMapping(value="homologyGeneNames",method=RequestMethod.POST,produces="text/plain")
    @ResponseBody
    private String homologyGeneNames(HttpServletResponse response, @RequestParam("idTab") String idTab, @RequestParam("idConversion") Long idConversion) throws Exception {
    	Subject currentUser = SecurityUtils.getSubject();
    	String username = "guest";
    	
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		User user = userService.getUser(userId);
    		username = user.getUsername();
    	}
    	
   
    	ArrayList<QueryResult> homologyResult = new ArrayList<QueryResult>();
    	
    	StringBuilder returnMessage = new StringBuilder("");
    	if (!currentSessions.containsKey(username) || !currentSessions.get(username).containsKey(idTab)) {
    		response.setStatus(988);
    		returnMessage.append("Could not find an existing session for homology conversion!");
    	} else {
    		SessionData sd = currentSessions.get(username).get(idTab);
    	
    		//Get external gene information
    		GeneIdConversion gic = this.geneIdConversionService.getGeneIdConversionByID(idConversion);
    		OrganismBuild sourceBuild = gic.getSourceBuild();
    		OrganismBuild destBuild = gic.getDestBuild();
    		List<ExternalGene> sourceList = this.externalGeneService.getExternalGenesByOrganismBuild(sourceBuild);
    		List<ExternalGene> destList = this.externalGeneService.getExternalGenesByOrganismBuild(destBuild);
    		
    		//Get gene names if not done already
    		if(!searchDict.containsKey(destBuild.getIdOrganismBuild())) {
    			loadGeneNames(destBuild);
    		}
    		
    		//get conversion information
    		File conversionFile = new File(FileController.getHomologyDirectory(),gic.getConversionFile());
    		
    		//Create homology stuff
    		HomologyParser hp = new HomologyParser(conversionFile,sourceList,destList,sourceBuild,destBuild);
    		HomologyModel hm = hp.processData();
    		HashMap<String,String> homologyMap = hm.getHomologyMap();
    		
    		//Get coordinate information
    		Genome genome = GenomeBuilds.fetchGenome(destBuild);
    		HashMap<String, Gene> geneNameGene = genome.getTranscriptomes()[0].getGeneNameGene();
    		
    	       
        	//counters    		
    		int totalInput = 0;
    		int totalHomology = 0;
    		int totalCoordinate = 0;
    		int ambiguousHomologyCount = 0;
    		int ambiguousStableCount = 0;
    		
    		
    		for (QueryResult qr: sd.getResults().getResultList()) {
    			String name = qr.getEnsemblName();
    			
    			totalInput++;
    			if (homologyMap.containsKey(name)) {
    				String updatedName = homologyMap.get(name);
    				boolean isHomologyAmbig = false;
    				boolean isStableAmbig = false;
    				if (updatedName.endsWith("*")) {
    					ambiguousHomologyCount += 1;
    					updatedName = updatedName.substring(0,updatedName.length()-1);
    					isHomologyAmbig = true;
    				}
    				if (updatedName.endsWith("+")) {
    					ambiguousStableCount += 1;
    					updatedName = updatedName.substring(0,updatedName.length()-1);
    					isStableAmbig = true;
    				}
    				
    				if (!updatedName.equals("None")) {
    					totalHomology++;
        				if (geneNameGene.containsKey(updatedName)) {
        					totalCoordinate += 1;
        					Gene gene = geneNameGene.get(updatedName);
        					QueryResult newQR = qr.clone();
        					newQR.setCoordinates(geneByTxBoundary(gene));
        					if (isHomologyAmbig) {
        						newQR.setSearch("*" + newQR.getEnsemblName());
        					} else if (isStableAmbig) {
        						newQR.setSearch("+" + newQR.getEnsemblName());
        					} else {
        						newQR.setSearch(newQR.getEnsemblName());
        					}
        					
        					newQR.setEnsemblName(updatedName);
        					if (ensembl2NameDict.containsKey(updatedName)) {
        		    			newQR.setMappedName(ensembl2NameDict.get(updatedName));
        		    		} else {
        		    			newQR.setMappedName(updatedName);
        		    		}
        					homologyResult.add(newQR);
        				}
    				}	
    			}
    		}
    		
    		this.homologyResult.put(idTab, homologyResult);
    		
    		returnMessage.append(String.format("Found %d genes to convert.<br>",totalInput));
    		returnMessage.append(String.format("Successfully found homology for %d genes (%.2f). <br>",totalHomology,(float)totalHomology / totalInput * 100 ));
    		returnMessage.append(String.format("Genes with ambiguous homology: %d (%.2f). <br>",ambiguousHomologyCount,(float)ambiguousHomologyCount / totalHomology * 100));
    		returnMessage.append(String.format("Genes with ambiguous stable IDs: %d  (%.2f). <br>",ambiguousStableCount,(float)ambiguousStableCount / totalHomology * 100 ));
    		returnMessage.append(String.format("Successfully found coordinates for %d genes (%.2f). <br>",totalCoordinate,(float)totalCoordinate / totalInput * 100));
    	}
    	return returnMessage.toString();
    }
    
    @RequestMapping(value="homologyGeneNameClear",method=RequestMethod.GET)
    @ResponseBody
    private void homlogyGeneNameClear(
    		HttpServletResponse response, 
    		@RequestParam(value="idTab") String idTab) {
    	
    	if (!homologyResult.containsKey(idTab)) {
			response.setStatus(988);
		} else {
			homologyResult.remove(idTab);
		}
    	
    }
    
    @RequestMapping(value="homologyGeneNameResult",method=RequestMethod.GET)
    @ResponseBody
    private QueryResultContainer homlogyGeneNameResult(
    		HttpServletResponse response, 
    		@RequestParam(value="idTab") String idTab, 
    		@RequestParam(value="idConversion") Long idConversion, 
            @RequestParam(value="resultsPerPage") Integer resultsPerPage,
            @RequestParam(value="sortType") String sortType,
            @RequestParam(value="isReverse") boolean reverse) {
    	
    	
    	QueryResultContainer qrc = null;
    
    	
		Subject currentUser = SecurityUtils.getSubject();
    	String username = "guest";
    	    	
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		User user = userService.getUser(userId);
    		username = user.getUsername();
    	}
    	
    	if (!currentSessions.containsKey(username) || !currentSessions.get(username).containsKey(idTab)) {
    		response.setStatus(988);
    		
    	} else {
    		if (!homologyResult.containsKey(idTab)) {
    			response.setStatus(988);
    			System.out.println("Could not find homology");
    		} else {
    			//Pull up the existing result
        		SessionData sd = currentSessions.get(username).get(idTab);
        		
        		//Get new organism build information and add to qrc
        		GeneIdConversion gic = geneIdConversionService.getGeneIdConversionByID(idConversion);
        		OrganismBuild ob = gic.getDestBuild();
        		
        		
        		
        		QueryResultContainer oldQrc = sd.getResults();
            	qrc = new QueryResultContainer(homologyResult.get(idTab),homologyResult.get(idTab).size(),oldQrc.getAnalysisNum(),
            			0, 0, oldQrc.getSortType(), true, ob.getIdOrganismBuild(), ob.getEnsemblCode(),ob.getOrganism().getBinomial(), false);
        
      
        		//Stuff back into session
            	sd.setResults(qrc);
            	
            	sd.setLastTouched(new Date());
            	

        		currentSessions.get(username).put(idTab, sd);
        		
    		}
    	}
    	 
    	
    	homologyResult.remove(idTab);
    	QueryResultContainer qrcSub = qrc.getQrcSubset(resultsPerPage, 0, sortType, reverse);
        return qrcSub;
    }
    
    @RequestMapping(value="copyAllCoordinates",method=RequestMethod.POST,produces="text/plain")
    @ResponseBody
    private String copyAllCoordinates(HttpServletResponse response, @RequestParam("idTab") String idTab) {
    	Subject currentUser = SecurityUtils.getSubject();
    	String username = "guest";
    	    	
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		User user = userService.getUser(userId);
    		username = user.getUsername();
    	} 
    	
    	boolean failed = false;
    	int coordinateCount = 0;
    	
    	if (!currentSessions.containsKey(username) || !currentSessions.get(username).containsKey(idTab)) {
    		response.setStatus(988);
    		failed = true;
    	} else {
    		SessionData sd = currentSessions.get(username).get(idTab);
    		
    		
    		
    		StringBuilder intervals = new StringBuilder("");
    		for (QueryResult qr: sd.getResults().getResultList()) {
    			intervals.append(qr.getCoordinates() + "\n");
    			coordinateCount += 1;
    		}
    		sd.setRegionString(intervals.toString());
    	}
    	
    	String message = null;
    	if (!failed) {
    		message = coordinateCount + " coordinates were copied.";
    	}
    	return message;
    }
    
    @RequestMapping(value="copyAllGenes",method=RequestMethod.POST,produces="text/plain")
    @ResponseBody
    private String copyAllGenes(HttpServletResponse response, @RequestParam("idTab") String idTab) {
    	Subject currentUser = SecurityUtils.getSubject();
    	String username = "guest";
    	    	
    	//If user isn't authenticated, return nothing
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		User user = userService.getUser(userId);
    		username = user.getUsername();
    	} 
    	
    	boolean failed = false;
    	int geneCount = 0;
    	
    	if (!currentSessions.containsKey(username) || !currentSessions.get(username).containsKey(idTab)) {
    		response.setStatus(988);
    		failed  = true;
    	} else {
    		SessionData sd = currentSessions.get(username).get(idTab);
    		
    		StringBuilder intervals = new StringBuilder("");
    		for (QueryResult qr: sd.getResults().getResultList()) {
    			intervals.append(qr.getMappedName() + "\n");
    			geneCount += 1;
    		}
    		sd.setGeneString(intervals.toString());
    	}
    	
    	String message = null;
    	if (!failed) {
    		message = geneCount + " genes were copied.  ";
    	} 
    	return message;
    }
    
    
    @RequestMapping(value="loadExistingSettings",method=RequestMethod.GET) 
    @ResponseBody
    public QuerySettings getQuerySettings(@RequestParam("idTab") String idTab) throws Exception {
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
    	
    	if (!currentSessions.containsKey(username)) {
    		loadSessionDict(username);
    	}
    	
    	if (currentSessions.containsKey(username)) {
    		QuerySettings qs = null;
    		if (!currentSessions.get(username).containsKey(idTab)) {
    			currentSessions.get(username).put(idTab, fetchMostRecentSession(username));
    			System.out.println("Loading session info");
    		}
    		if (!currentSessions.get(username).containsKey(idTab)) {
    			System.out.println("NOPE!");
    		}
    		System.out.println(currentSessions.get(username).get(idTab).getSettings());
    		qs = currentSessions.get(username).get(idTab).getSettings();
    		return qs;
    		
    	} else {
    		return null;
    	}
    }
    
    @RequestMapping(value="loadExistingResults",method=RequestMethod.GET) 
    @ResponseBody
    public QueryResultContainer getQueryResults(@RequestParam("idTab") String idTab) throws Exception {
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
    	
    	if (!currentSessions.containsKey(username)) {
    		loadSessionDict(username);
    	}
    	
    	if (currentSessions.containsKey(username)) {
    		QueryResultContainer qrcSub;
    		if (!currentSessions.get(username).containsKey(idTab)) {
    			currentSessions.get(username).put(idTab,fetchMostRecentSession(username));
    			System.out.println("Can't find idTab");
    		} 
    		
    		for (String tab: currentSessions.get(username).keySet()) {
    			System.out.println(tab);
    		}
    		
    		qrcSub = currentSessions.get(username).get(idTab).getResults().getQrcSubset(25, 0, "FDR",true);
    		return qrcSub;
    		
    	} else {
    		return null;
    	}
    }
    
    
    private void loadSessionDict(String username) throws Exception {
    	File resultsPath = new File(FileController.getQueryDirectory(),username + ".session.ser");
    	if (resultsPath.exists()) {
    		FileInputStream fin = new FileInputStream(resultsPath);
    		ObjectInputStream ois = new ObjectInputStream(fin);
    		SessionData sd = (SessionData)ois.readObject();
    		ois.close();
    		HashMap<String,SessionData> sdHash = new HashMap<String,SessionData>();
    		sdHash.put(username, sd);
    		currentSessions.put(username,sdHash);
    	}
    }
    
    
    public IgvSessionResult createIgvSessionFile(String username, File sessionsDirectory, String serverName, String idTab) throws Exception {
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
    	QueryResultContainer results = null;
    	
    	if (!currentSessions.containsKey(username) || !currentSessions.get(username).containsKey(idTab)) {
    		errors.append(String.format("This user %s doesn't appear to have any stored analyses, IGV session can't be created.",username));
    		igvSR.setError(errors.toString());
    		return igvSR;
    	} else {
    		results = currentSessions.get(username).get(idTab).getResults();
    	}
    	
    	//Get datatracks list
    	List<Analysis> usedAnalyses = getUsedAnalyses(results.getResultList());
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
    public IgvSessionResult startIgvSession(HttpServletResponse response, HttpServletRequest request, @RequestParam("idTab") String idTab) throws Exception {
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
    
    	IgvSessionResult igr = this.createIgvSessionFile(username, sessionsDirectory, serverName, idTab);
    	if (igr.getError() != null) {
    		response.setStatus(405);
    	}
    	
    	this.dashboardService.increaseIgv();
    	
    	return igr;
    }
    
    @RequestMapping(value="startJBrowseSession",method=RequestMethod.GET)
    @ResponseBody
    public JBrowseReturnData startJbrowseSession(HttpServletResponse response, HttpServletRequest request, @RequestParam("idTab") String idTab) throws Exception {
    	try {
	    	String serverName = request.getServerName();
	    	System.out.println(serverName);
	    	
	    	if (serverName.equals("localhost")) {
	    		serverName = "localhost:8080";
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
	    	
	   
	    	StringBuilder errors = new StringBuilder("");
	    	
	    	//Grab the stored analyses
	    	QueryResultContainer results = null;
	    	Long idTransFactor = null;
	    	if (!currentSessions.containsKey(username) || !currentSessions.get(username).containsKey(idTab)) {
	    		errors.append(String.format("This user %s doesn't appear to have any stored analyses, JBrowse session can't be created.",username));
	    		JBrowseReturnData dbrd = new JBrowseReturnData(errors.toString(),"",false,"");
	    		return dbrd;
	    	} else {
	    		results = currentSessions.get(username).get(idTab).getResults();
	    		idTransFactor = currentSessions.get(username).get(idTab).getSettings().getIdTransFactor();
	    	}
	    	
	    	//Get transfactor
	    	TransFactor tf = null;
	    	if (idTransFactor != null) {
	    		tf = tfService.getTransFactorById(idTransFactor);
	    	}
	    	
	    	//Get datatracks list
	    	List<Analysis> usedAnalyses = getUsedAnalyses(results.getResultList());
	    	List<DataTrack> dtList = new ArrayList<DataTrack>();
	    	for (Analysis a: usedAnalyses) {
	    		dtList.addAll(a.getDataTracks());
	    	}
	    	
	    	
	    	
	    	String buildName = null;
	    	if (usedAnalyses.size() != 0) {
	    		OrganismBuild ob = usedAnalyses.get(0).getProject().getOrganismBuild();
	    		if (!GenomeBuilds.doesGenomeExist(ob)) {
	    			GenomeBuilds.loadGenome(ob);
	    		}
	    		buildName = GenomeBuilds.getGenome(ob).getBuildName();
	    		
	    	} 
	    	
	    	String pathToJBrowse = BiominerProperties.getProperty("jbrowsePath");
	    	String pathToRepo = "";
	    	boolean repoCreated = JBrowseUtilities.createJbrowseRepo(username, buildName, dtList, tf, pathToJBrowse);
	    	
	    	if (repoCreated) {
	    		//http://localhost:8080/jbrowse/JBrowse-1.12.1/index.html?data=data/u0855942
	    		pathToRepo = "http://" + serverName + "/jbrowse/JBrowse-1.12.1/index.html?data=data/" + username;
	    	}
	    	JBrowseReturnData dbrd = new JBrowseReturnData(errors.toString(),"",repoCreated,pathToRepo);
	    	return dbrd;
    	} catch (Exception ex) {
    		JBrowseReturnData dbrd = new JBrowseReturnData("Error creating JBrowseRepo: " + ex.getMessage(),"",false,"");
    		return dbrd;
    	}
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
    
    private List<Analysis> getUsedAnalyses(List<QueryResult> results) {
    	HashSet<Long> usedIds = new HashSet<Long>();
    	for (QueryResult qr: results) {
    		usedIds.add(qr.getIdAnalysis());
    	}
    	
    	List<Analysis> usedAnalyses = new ArrayList<Analysis>();
    	for (Long id: usedIds) {
    		usedAnalyses.add(analysisService.getAnalysisById(id));
    	}
    	
    	return usedAnalyses;
    }
    
    
    
    
     @RequestMapping(value = "downloadAnalysis", method = RequestMethod.GET)
     @ResponseBody
	 public void downloadAnalysis(HttpServletRequest request, HttpServletResponse response, @RequestParam(value="codeResultType") String codeResultType, @RequestParam(value="idTab") String idTab) throws Exception{
    	
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
    	
    	if (currentSessions.containsKey(key) && currentSessions.get(key).containsKey(idTab)) {
    		List<QueryResult> results = currentSessions.get(key).get(idTab).getResults().getResultList();
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
    			IgvSessionResult isr = createIgvSessionFile(key, sessionsDirectory, serverName, idTab);
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
    				response.setContentType("application/x-download");
    			 	response.setHeader("Content-Disposition", "attachment; filename=\""+ key + ".results.zip"+"\"");
    			 	
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
    		@RequestParam(value="sortType") String sortType,
    		@RequestParam(value="idTab") String idTab,
    		@RequestParam(value="sortReverse") boolean sortReverse
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
    	

    	QueryResultContainer qrc = null;
    	if (currentSessions.containsKey(key) && currentSessions.get(key).containsKey(idTab)) {
    		QueryResultContainer full = currentSessions.get(key).get(idTab).getResults();
    		
    		qrc = full.getQrcSubset(resultsPerPage, pageNum, sortType, sortReverse);
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
    	
    	//Not sure it is smart to restrict organism list by anything but user.  Going to comment out this fancy version for now...
    	//List<OrganismBuild> obList = this.analysisService.getOrgansimBuildByQuery(user, idAnalysisTypes, idLabs, idProjects, idAnalyses, idSampleSources);
    	ArrayList<Long> empty = new ArrayList<Long>();
    	List<OrganismBuild> obList = this.analysisService.getOrgansimBuildByQuery(user, empty, empty, empty, empty, empty);
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
 
    
    private List<List<String>> getGeneIntervals(String names, Genome genome, String searchType, OrganismBuild ob, String idTab) {
    	List<String> regions = new ArrayList<String>();
    	List<String> searches = new ArrayList<String>();
    	List<String> mapped = new ArrayList<String>();
    	
    	//Load gene names if not already done
    	if(!searchDict.containsKey(ob.getIdOrganismBuild())) {
    		System.out.println("Loading gene names");
			loadGeneNames(ob);
		} else {
			System.out.println("nope already loaded");
		}
    	
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
    		currentSessions.get(username).get(idTab).addWarning("The gene list is empty!<br/>");
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
    				if (bidToEnsembl.containsKey(id)) {
    					extIdFinalSet.add(bidToEnsembl.get(id));
    				}
    			}
    			
    			//Warning if no ensembl names can be found
    			if (extIdFinalSet.size() == 0) {
    				currentSessions.get(username).get(idTab).addWarning("Could not find an Ensembl identifier for gene: '" + name + "'<br/>");
					continue;
    			}
    			
    		} else {
    			//Warning if no biominer genes can be found
    			missingGenes.add(name);
    			currentSessions.get(username).get(idTab).addWarning("The gene '" + name + "' could not be found in our database<br/>");
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
    		if (qr.getEnsemblName() == null) {
    			continue;
    		}
    		
    		if (cleanGeneSet.contains(qr.getEnsemblName())) {
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
    		String mappedName = gr.getMappedName();
    		result.setEnsemblName(mappedName);
    		if (ensembl2NameDict.containsKey(mappedName)) {
    			result.setMappedName(ensembl2NameDict.get(mappedName));
    		} else {
    			result.setMappedName(mappedName);
    		}
    		
    		
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
   
    private SessionData fetchMostRecentSession(String username) {
    	SessionData mostRecent = null;
    	Date newestDate = new Date(0);
    	for (SessionData sess : currentSessions.get(username).values()) {
    		if (sess.getLastTouched().after(newestDate)) {
    			mostRecent = sess;
    			newestDate = sess.getLastTouched();
    		}
    	}
    	
    	return mostRecent;
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
    	    			String chrom = m1.group(2);
    	    			
    	    			try {
    	    				start = Integer.parseInt(m1.group(4));
    	    			} catch (NumberFormatException nfe) {
    	    				warnings.append(String.format("Start boundary not an integer %s, skipping.<br/>",m1.group(2)));
    	    				continue;
    	    			}
    	    			
    	    			try {
    	    				end = Integer.parseInt(m1.group(6));
    	    			} catch (NumberFormatException nfe) {
    	    				warnings.append(String.format("End boundary not an integer %s, skipping.<br/>",m1.group(3)));
    	    				continue;
    	    			}
    	    			
    	    			if (!genome.getNameChromosome().containsKey(chrom)) {
    	    				warnings.append(String.format("The chromsome %s could not be found in the genome %s.<br/>",chrom,genome.getBuildName()));
    	    				continue;
    	    			}
    	    			if (start >= end) {
    	    				warnings.append(String.format("The start coordinate (%d) is greater or equal to the end coordinate before padding (%d).<br/>",start,end));
    	    				continue;
    	    			}
    	    			
    	    			if (start < 0) {
    	    				warnings.append(String.format("The start coordinate ( %d ) is less than 0 before padding. Setting to zero.<br/>",start));
    	    				start = 0;
    	    			} 
    	    			if (end > genome.getNameChromosome().get(chrom).getLength()) {
    	    				warnings.append(String.format("The end coordinate ( %d) is greater than the chromsome length (%d) before padding. Setting to chromsome"
    	    						+ " end.<br/>",end,genome.getNameChromosome().get(chrom).getLength()));
    	    				end = genome.getNameChromosome().get(chrom).getLength();
    	    			}  
    	    			
    	    			start = Math.max(start-regionMargin,0);
    	    			end = Math.min(end+regionMargin,genome.getNameChromosome().get(chrom).getLength());
    	    			
    	    			
   	    		
    	    			
    	    			LocalInterval inv = new LocalInterval(chrom,start,end,s);
    	    			localIntervals.add(inv);
    	    			
    	    		} else if (m2.matches()) {
    	    			String chrom  = m2.group(2);
    	    			
    	    			if (!genome.getNameChromosome().containsKey(chrom)) {
    	    			
    	    				warnings.append(String.format("The chromosome %s could not be found in the genome %s.<br/>",chrom,genome.getBuildName()));
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
    		if (currentSessions.containsKey(key)) {
    			SessionData sd = fetchMostRecentSession(key);
    			try {
    				File sessionPath = new File(FileController.getQueryDirectory(), key + ".session.ser");
    				FileOutputStream fos = new FileOutputStream(sessionPath);
    				ObjectOutputStream oos = new ObjectOutputStream(fos);
    				oos.writeObject(sd);
    				oos.close();
    			} catch (Exception ex) {
    				ex.printStackTrace();
    			}
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
