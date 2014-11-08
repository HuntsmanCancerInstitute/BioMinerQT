package hci.biominer.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
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
import hci.biominer.model.intervaltree.IntervalTree;
import hci.biominer.model.ExternalGene;
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
import hci.biominer.util.igv.IGVResource;
import hci.biominer.util.igv.IGVSession;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.GZIPInputStream;
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
    
    private StringBuilder warnings = new StringBuilder("");
    
    private HashMap<String,QueryResultContainer> resultsDict =  new HashMap<String,QueryResultContainer>();
    private HashMap<String,File> fileDict = new HashMap<String,File>();
    private HashMap<String,List<Analysis>> analysisDict = new HashMap<String,List<Analysis>>();
    private HashMap<Long,List<GeneNameModel>> searchDict = new HashMap<Long,List<GeneNameModel>>();
    
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
        		
        		if(!searchDict.containsKey(ob.getIdOrganismBuild())) {
        			loadGeneNames(ob);
        		}
        	}
    	}
    }
    
    private void loadGeneNames(OrganismBuild ob) {
    	System.out.println("Loading common names for: " + ob.getName());
    	List<ExternalGene> egList = this.externalGeneService.getHugoNamesGenesByOrganismBuild(ob);
    	
    	HashSet<String> uniqueNamesHash = new HashSet<String>();
    	List<String> uniqueNamesSort = new ArrayList<String>();
    	for (ExternalGene eg: egList) {
    		uniqueNamesHash.add(eg.getExternalGeneName());
    	}
    	
    	uniqueNamesSort.addAll(uniqueNamesHash);
    	Collections.sort(uniqueNamesSort);
    	
    	List<GeneNameModel> uniqueNamesList = new ArrayList<GeneNameModel>();
    	for (String name: uniqueNamesSort) {
    		GeneNameModel gnm = new GeneNameModel(name);
    		uniqueNamesList.add(gnm);
    	}
    	
		searchDict.put(ob.getIdOrganismBuild(), uniqueNamesList);
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
    
    @RequestMapping(value="uploadGene",method=RequestMethod.POST)
    @ResponseBody
    public RegionUpload parseGenes(@RequestParam("file") MultipartFile file) {
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
    				String[] parts = temp.split("(,|\\s+)");
    				for (String p: parts) {
    					geneString.append(p + "\n");
    				}
    				
    			}
    			
    			regions.setRegions(geneString.toString());
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
    	RegionUpload regions = new RegionUpload();
    	Pattern pattern1 = Pattern.compile("^(\\w+)(,|:|\\s+)(\\d+)(,|-|\\s+)(\\d+)");
    	
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
    					regions.setMessage(String.format("Could not parse region line: %s. The first three columns must be chromsome, "
    							+ "start coordinate and stop coordinate.  Can be tab, space, comma delimited or in the format "
    							+ "chr:start-end",temp));
    					ok = false;
    					break;
    				}
    			}
    			
    			if (ok) {
    				regions.setRegions(regionString.toString());
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
        @RequestParam(value="isIntersect") boolean isIntersect,
        @RequestParam(value="regions") String regions,
        @RequestParam(value="regionMargins") Integer regionMargins,
        @RequestParam(value="genes") String genes,
        @RequestParam(value="geneMargins") Integer geneMargins,
        @RequestParam(value="idGeneAnnotations") List<Long> idGeneAnnotations,
        @RequestParam(value="isThresholdBasedQuery") boolean isThresholdBasedQuery,
        @RequestParam(value="FDR",required=false) Float FDR,
        @RequestParam(value="codeFDRComparison") String codeFDRComparison,
        @RequestParam(value="log2Ratio",required=false) Float log2Ratio,
        @RequestParam(value="codeLog2RatioComparison") String codeLog2RatioComparison,
        @RequestParam(value="resultsPerPage") Integer resultsPerPage,
        @RequestParam(value="sortType") String sortType,
        @RequestParam(value="intersectionTarget") String target
        ) throws Exception {
      
    	//Clear out warnings
    	this.warnings = new StringBuilder("");
    	
    	if (genes != null) {
    		genes = genes.toUpperCase();
    	}
    	
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
    	HashMap<AnalysisTypeEnum,AnalysisType> atMap = new HashMap<AnalysisTypeEnum,AnalysisType>();
    	for (Long id: idAnalysisTypes) {
    		AnalysisType at = this.analysisTypeService.getAnalysisTypeById(id);
    		atMap.put(at.getType(),at);
    	}
    	
    	//Container of mapped gene names, used for gene based searches
    	List<String> mappedNames = null;
    	
    	//Create intervals
    	IntervalParser ip = new IntervalParser();
    	List<Interval> intervalsToCheck = null;
        	
    	if (codeResultType.equals("GENE") || (codeResultType.equals("REGION") && target.equals("GENE"))) {
    		List<List<String>> parsed = this.getGeneIntervals(genes, genome, "TxBoundary",ob);
    		mappedNames = parsed.get(2); //This will be used for gene based filtering, if necessary
    		intervalsToCheck = ip.parseIntervals(this.convertListToString(parsed.get(0)), this.convertListToString(parsed.get(1)), geneMargins, genome);
    	} else if (codeResultType.equals("REGION"))  {
    		intervalsToCheck = ip.parseIntervals(regions, regions, regionMargins, genome);
    	}
    	
    	//Add IP warnings
    	this.warnings.append(ip.getWarnings());
    	
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
    	List<QueryResult> results = this.getIntersectingRegions(itList, analyses, genome, intervalsToCheck);
 
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
    	
    	//Determine how many analyses were used
    	List<Analysis> usedAnalyses = this.getUsedAnalyses(analyses, fullRegionResults);
    	HashMap<String,String> usedDataTracks = this.getDataTrackList(usedAnalyses);
    	
    	QueryResultContainer qrc = new QueryResultContainer(fullRegionResults, fullRegionResults.size(), usedAnalyses.size(), usedDataTracks.keySet().size(), 0, sortType, true);
    	

    	if (user != null) {
    		this.resultsDict.put(user.getUsername(), qrc);
    		this.analysisDict.put(user.getUsername(), analyses);
    	} else {
    		this.resultsDict.put("guest", qrc);
    		this.analysisDict.put("guest", analyses);
    	}
    	
    	System.out.println("Used analyses " + usedAnalyses.size());
    	System.out.println("Used data tracks " + usedDataTracks.keySet().size());
    	
    	QueryResultContainer qrcSub = qrc.getQrcSubset(resultsPerPage, 0, sortType);
    	
    	return qrcSub;
    	
    }
    
    @RequestMapping(value="startIgvSession",method=RequestMethod.GET)
    @ResponseBody
    public IgvSessionResult startIgvSession(HttpServletResponse response, HttpServletRequest request) throws Exception {
    	//Create result 
    	IgvSessionResult igvSR = new IgvSessionResult();
    	
    	//Get current active user
    	Subject currentUser = SecurityUtils.getSubject();
    	User user = null;
    	String key = "guest";
    	if (currentUser.isAuthenticated()) {
    		Long userId = (Long) currentUser.getPrincipal();
    		user = userService.getUser(userId);
    		key = user.getUsername();
    	}
    	
    	StringBuilder warnings = new StringBuilder("");
    	StringBuilder errors = new StringBuilder("");
    	
    	//Grab the stored analyses
    	List<Analysis> analyses = null;
    	QueryResultContainer results = null;
    	if (this.analysisDict.containsKey(key)) {
    		analyses = this.analysisDict.get(key);
    	} else {
    		errors.append(String.format("The user %s doesn't appear to have any stored analyses, IGV session can't be created.",key));
    		igvSR.setError(errors.toString());
    		response.setStatus(405);
    		return igvSR;
    	}
    	
    	if (this.resultsDict.containsKey(key)) {
    		results = this.resultsDict.get(key);
    	} else {
    		errors.append(String.format("This user %s doesn't appear to have any stored analyses, IGV session can't be created.",key));
    		igvSR.setError(errors.toString());
    		response.setStatus(405);
    		return igvSR;
    	}
    	
    	//Get datatracks list
    	List<Analysis> usedAnalyses = this.getUsedAnalyses(analyses, results.getResultList());
    	HashMap<String,String> datatracks = this.getDataTrackList(usedAnalyses);
    	
    	String genomeBuild = null;
    	if (usedAnalyses.size() != 0) {
    		genomeBuild = usedAnalyses.get(0).getProject().getOrganismBuild().getName();
    	} else {
    		errors.append("The stored analysis list is empty, session can't be created.");
    		igvSR.setError(errors.toString());
    		response.setStatus(405);
    		return igvSR;
    	}
    	
    	//Create session object
    	IGVSession igvSession = new IGVSession(genomeBuild);
    	List<IGVResource> resources = new ArrayList<IGVResource>();
    	for (String name: datatracks.keySet()) {
    		URL datatrackURL = new URL(datatracks.get(name));
    		if (!urlExists(datatrackURL)) {
    			warnings.append(String.format("The datatrack %s does not exist or is inaccessable.<br/>", datatracks.get(name)));
    			continue;
    		}
    		
    		IGVResource igvResource = null;
    		if (name.endsWith(".vcf.gz")) {
    			igvResource = new IGVResource(name, datatrackURL, null, false);
    		} else if (name.endsWith(".bw")) {
    			igvResource = new IGVResource(name, datatrackURL, null, true);
    		} else if (name.endsWith(".bed.gz")) {
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
    		response.setStatus(405);
    		return igvSR;
    	}
    	
    	//Add resources
    	IGVResource[] resourceArray = new IGVResource[resources.size()];
    	resourceArray = resources.toArray(new IGVResource[resources.size()]);
    	igvSession.setIgvResources(resourceArray);
    	
    	//Write out file
    	String rootDirectory = request.getSession().getServletContext().getRealPath("/");
    	File resourceDirectory = new File(rootDirectory, "resources");
    	File sessionsDirectory = new File(resourceDirectory,"sessions");
    	if (!sessionsDirectory.exists()) {
    		sessionsDirectory.mkdir();
    	}
    	String fileName = key + "_igv.xml";
    	File finalPath = new File(sessionsDirectory,fileName);
    	igvSession.writeXMLSession(finalPath);
    	
    	String serverName = request.getLocalName();
    	if (serverName.equals("localhost")) {
    		serverName = "127.0.0.1";
    	}
    	
    	//Construct the url
    	URL sessionUrl = new URL("http://" + serverName + ":8080/biominer/resources/sessions/" + fileName);
    	
    	
    	igvSR.setUrl(igvSession.fetchIGVLaunchURL(sessionUrl).toString());
    	igvSR.setUrl2(sessionUrl.toString());
    	
    	return igvSR;
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
           e.printStackTrace();
           return false;
        }
      }
    
    private HashMap<String,String> getDataTrackList(List<Analysis> analyses) {
    	HashMap<String,String> urlDict = new HashMap<String,String>();
    	for (Analysis a: analyses) {
    		List<DataTrack> dts = a.getDataTracks();
			for (DataTrack dt: dts) {
				String dtName = dt.getName();
				String dtUrl = dt.getUrl();
				if (!urlDict.containsKey(dtName)) {
					urlDict.put(dtName, dtUrl);
				}
    		}
    	}
    	return urlDict;
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
	 public void downloadAnalysis(HttpServletResponse response, @RequestParam(value="codeResultType") String codeResultType) throws Exception{
    	
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
    			File localFile = new File(FileController.getDownloadDirectory(),key + ".query.txt.gz");
    			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(localFile))));
                        
    			if (codeResultType.equals("REGION")) {
    				//Write header
        			if (results.size() > 0) {
        				bw.write(results.get(0).writeRegionHeader());
        			}
        			
        			for (QueryResult qr: results) {
        				bw.write(qr.writeRegion());
        			}
    			} else if (codeResultType.equals("GENE")) {
    				//Write header
        			if (results.size() > 0) {
        				bw.write(results.get(0).writeGeneHeader());
        			}
        			
        			for (QueryResult qr: results) {
        				bw.write(qr.writeGene());
        			}
    			}
    			
    			bw.close();
    			
    			try {		
    			 	//response.setContentType(getFile.getFileType());
    			 	response.setHeader("Content-disposition", "attachment; filename=\""+ key + ".query.txt.gz"+"\"");
    			 	
    			 	BufferedInputStream bis = new BufferedInputStream(new FileInputStream(localFile));
    			 	
    		        FileCopyUtils.copy(bis, response.getOutputStream());
    		        
    		        
    		        this.fileDict.put(key, localFile);
    		        
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
    	

    	QueryResultContainer qrc = null;
    	if (this.resultsDict.containsKey(key)) {
    		
    		
    		QueryResultContainer full = this.resultsDict.get(key);
    		
    		qrc = full.getQrcSubset(resultsPerPage, pageNum, sortType);
    	} else {
    		this.warnings = new StringBuilder("");
    		this.warnings.append("There aren't any available results stored for this user");
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
    	
    	
    	String[] genes = names.split("\n");
    	List<String> cleanedGenes = new ArrayList<String>();
    	if (genes.length == 0) {
    		this.warnings = new StringBuilder("The gene list is empty!");
    		return null;
    	}
    	
    	for (String g: genes) {
    		String[] parts = g.split("[\\s+,\\n]+");
    		for (String p: parts) {
    			cleanedGenes.add(p.trim());
    		}	
    	}
    	
    	this.warnings = new StringBuilder("");
    	for (String name: cleanedGenes) {
    		//Grab external ids matching names
        	List<ExternalGene> extIds = this.externalGeneService.getBiominerIdByExternalName(name,ob.getIdOrganismBuild() );
        	if (extIds.size() == 0) {
        		this.warnings.append("The genes '" + name + "' could not be found in our database");
        		continue;
        	}
        	
        	//Create biominerGene index list
        	HashSet<Long> bIdSet = new HashSet<Long>();
        	for (ExternalGene eg: extIds) {
        		bIdSet.add(eg.getBiominerGene().getIdBiominerGene());
        	}
        	
        	//Search for matches
        	List<Long> bIdList = new ArrayList<Long>();
        	bIdList.addAll(bIdSet);
        	List<ExternalGene> extIdFinal = this.externalGeneService.getExternalGeneByBiominerId(bIdList,"ensembl", ob.getIdOrganismBuild());
        	if (extIdFinal.size() == 0) {
        		this.warnings.append("Could not find an Ensembl identifier for gene '" + name + "'.");
        		continue;
        	}
        	
        	HashSet<String> extIdFinalSet = new HashSet<String>();
        	for (ExternalGene eg: extIdFinal) {
        		extIdFinalSet.add(eg.getExternalGeneName());
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
        			this.warnings.append("Could not find gene: '" + ensemblName + "' in Genome Object.\n");
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
    
    private List<QueryResult> getIntersectingRegions(ArrayList<HashMap<String,IntervalTree<GenericResult>>> treeList, List<Analysis> analyses, Genome genome, 
    		List<Interval> intervals) throws Exception{
    	List<QueryResult> queryResults = new ArrayList<QueryResult>();
    	int index = 1;
    	
    	for (int i=0; i<treeList.size(); i++) {
    		
    		HashMap<String, IntervalTree<GenericResult>> it = treeList.get(i);
    		for (Interval inv: intervals) {
    			if (it.containsKey(inv.getChrom())) {
    				List<GenericResult> hits = it.get(inv.getChrom()).search(inv.getStart(), inv.getEnd());
        			Analysis a = analyses.get(i);
        			for (GenericResult c: hits) {
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
        	    		String coordinate = inv.getChrom() + ":" + String.valueOf(c.getStart()) + "-" + String.valueOf(c.getStop());
        	    		result.setCoordinates(coordinate);
        	    		result.setFDR(c.getTransFDR());
        	    		result.setLog2Ratio(c.getLog2Rto());
        	    		result.setMappedName(c.getMappedName());
        	    		result.setSearch(inv.getSearch());
        	    		queryResults.add(result);
        			}
    			}	
    		}
    	}
    	
    	return queryResults;
    }
   
    
    private class IntervalParser {
    	private Pattern pattern1 = Pattern.compile("^(.+?)(:|\\s+)(\\d+)(-|\\s+)(\\d+)$");
    	private Pattern pattern2 = Pattern.compile("^(.+)$");

  
    	private StringBuilder warnings = new StringBuilder("");
    	
    	
    	
    	public List<Interval> parseIntervals(String region, String search, Integer regionMargin, Genome genome) throws Exception {
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
    				if (chrom.startsWith("chr")) {
    					continue;
    				}
    				Interval inv = new Interval(chrom,0,genome.getNameChromosome().get(chrom).getLength(),"none");
    				intervals.add(inv);
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
    				
    				Matcher m1 = pattern1.matcher(r);
    	    		Matcher m2 = pattern2.matcher(r);
    	    		
    	    		
    	    		if (m1.matches()) {
    	    		
    	    			int end = 0;
    	    			int start = 0;
    	    			try {
    	    				start = Integer.parseInt(m1.group(3)) - regionMargin;
    	    			} catch (NumberFormatException nfe) {
    	    				warnings.append(String.format("Start boundary not an integer %s, skipping.",m1.group(2)));
    	    				continue;
    	    			}
    	    			
    	    			try {
    	    				end = Integer.parseInt(m1.group(5)) + regionMargin;
    	    			} catch (NumberFormatException nfe) {
    	    				warnings.append(String.format("End boundary not an integer %s, skipping.",m1.group(3)));
    	    				continue;
    	    			}
    	    			
    	    			String chrom = m1.group(1);
    	    			System.out.println("X" + chrom + "X");
    	    			System.out.println(region);
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
    	    			
    	    			Interval inv = new Interval(chrom,start,end,s);
    	    			intervals.add(inv);
    	    			
    	    		} else if (m2.matches()) {
    	    			String chrom  = m2.group(1);
    	    			
    	    			if (!genome.getNameChromosome().containsKey(chrom)) {
    	    				warnings.append(String.format("The chromsome %s could not be found in the genome %s.\n",chrom,genome.getBuildName()));
    	    				continue;
    	    			}
    	    			
    	    			int start = 0;
    	    			int end = genome.getNameChromosome().get(chrom).getLength();
    	    			
    	    			Interval inv = new Interval(chrom,start,end,s);
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
    	private String search; //what was searched to get this interval 

	
    	public Interval(String chrom, int start, int end, String search) {
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
    
}
