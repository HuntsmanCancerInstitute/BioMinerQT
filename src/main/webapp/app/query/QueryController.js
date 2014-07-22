'use strict';


/**
 * QueryController
 * @constructor
 */
var query     = angular.module('query',     ['filters', 'services', 'directives', 'ui.bootstrap', 'chosen']);


angular.module("query").controller("QueryController", 
[ '$scope', '$http', '$modal','DynamicDictionary','StaticDictionary',
  
function($scope, $http, $filter, DynamicDictionary, StaticDictionary) {
	
	$scope.hasResults = false;
	
	$scope.querySummary = [];
	$scope.codeResultType = "";
	$scope.isGeneBasedQuery = true;
	$scope.idOrganismBuild = "";

	
	$scope.selectedAnalysisTypes = [];
	$scope.selectedLabs = [];
	$scope.projects = [];
	$scope.analyses = [];
	$scope.selectedSampleSources = [];
	
	$scope.isIntersect = "true";
	$scope.intersectionTarget = "";
	$scope.regions = "";
	$scope.regionMargins = "1000";
	$scope.genes = "";
	$scope.geneMargins = "1000";
	$scope.selectedGeneAnnotations = [];
	
	$scope.isThresholdBasedQuery = true;
	$scope.thresholdFDR = "";
	$scope.codeThresholdFDRComparison = ">";
	$scope.thresholdLog2Ratio = "";
	$scope.codeThresholdLog2RatioComparison = "> abs";
	
	$scope.thresholdVariantQual = "";
	$scope.codeThresholdVariantQualComparison = ">";
	$scope.codeVariantFilterType = "";
	$scope.codeVariantFilterType = "";
	$scope.selectedGenotypes = [];

	
	$scope.mapResultType = {
			'GENE' :     'Genes',
			'REGION' :   'Genomic Regions',
			'VARIANT' :  'Variants' };
	
	$scope.analysisTypeList = [
	        {"codeAnalysisType": "RNASeq",  "name": "RNA Seq"},
	        {"codeAnalysisType": "CHIPSEQ", "name": "ChIP Seq"},
	        {"codeAnalysisType": "VARIANT", "name": "Variant"},
	        {"codeAnalysisType": "METHYL",  "name": "Methylation"}
	];
	
	$scope.analysisTypeCheckedList = [
	               	        {"codeAnalysisType": "RNASeq",  "name": "RNA Seq",     "codeResultTypes" : ["GENE", "REGION"],               
	               	        	"selected": false, "show" : true, "class": "grey-out"},
	               	        {"codeAnalysisType": "CHIPSEQ", "name": "ChIP Seq",    "codeResultTypes" : ["REGION"],                       
	               	        	"selected": false, "show" : true, "class": ""},
	               	        {"codeAnalysisType": "VARIANT", "name": "Variant",     "codeResultTypes" : ["GENE", "REGION", "VARIANT"],     
	               	        	"selected": false, "show" : true, "class": ""},
	               	        {"codeAnalysisType": "METHYL",  "name": "Methylation", "codeResultTypes" : ["REGION"],                        
	               	        	"selected": false, "show" : true, "class": ""}
	];

    
    $scope.geneAnnotationList = [
                   {"idGeneAnnotation": 1, "name": "TSS"},
                   {"idGeneAnnotation": 2, "name": "Genic"},
                   {"idGeneAnnotation": 3, "name": "Intronic"},
                   {"idGeneAnnotation": 4, "name": "Exonic"},
                   {"idGeneAnnotation": 5, "name": "Intergenic"}
                     		
    ];
    
    
    $scope.genotypeList = [
                   {"idGenotype": 1, "name": "Homozygous Mutant"},
                   {"idGenotype": 2, "name": "Heterozygous"},
                   {"idGenotype": 3, "name": "Reference"},
                   {"idGenotype": 4, "name": "Carrier Mutant"},
                   {"idGenotype": 5, "name": "Carrier Reference"}
    ];
       
       

	$scope.analysisTypeList = [
	   	          {idAnalysisType: 1, name: "ChIP Seq"},
	              {idAnalysisType: 2, name: "RNA Seq"},
	              {idAnalysisType: 3, name: "Methylation Analysis"},
	              {idAnalysisType: 4, name: "Variant Calling"}
	   	                  
	];
	
	$scope.queryResults = [
	 {
		  projectName: "Cell 2012 Wamstad Alexander",
		  analysisName: "ChIP-seq at four stages during cardiomyocyte differentiation",
		  analysisType: "ChIP Seq",
		  sampleConditions: "Global occupancy for histone modifications and RNA polymerase II",
		  analysisDescription: "Mouse Embryonic - Stage ESC",
		  region: "ch4:23454-23898",
		  fdr: .03,
		  log2Ratio: 1.5
	 },
	 {
		  projectName: "Cell 2012 Wamstad Alexander",
		  analysisName: "ChIP-seq at four stages during cardiomyocyte differentiation",
		  analysisType: "ChIP Seq",
		  sampleConditions: "Global occupancy for histone modifications and RNA polymerase II",
		  analysisDescription: "Mouse Embryonic - Stage ESC",
		  region: "ch4:23454-23898",
		  fdr: .03,
		  log2Ratio: 1.5
	 },
	 {
		  projectName: "Cell 2012 Wamstad Alexander",
		  analysisName: "ChIP-seq at four stages during cardiomyocyte differentiation",
		  analysisType: "ChIP Seq",
		  sampleConditions: "Global occupancy for histone modifications and RNA polymerase II",
		  analysisDescription: "Mouse Embryonic - Stage ESC",
		  region: "ch6:89000-90000",
		  fdr: .02,
		  log2Ratio: 1.5
	 },
	 {
		  projectName: "Cell 2012 Wamstad Alexander",
		  analysisName: "ChIP-seq at four stages during cardiomyocyte differentiation",
		  analysisType: "ChIP Seq",
		  sampleConditions: "Global occupancy for histone modifications and RNA polymerase II",
		  analysisDescription: "Mouse Embryonic - Stage MES",
		  region: "ch3:23454-23898",
		  fdr: .01,
		  log2Ratio: 1.8
	 },
	 {
		  projectName: "Cell 2012 Wamstad Alexander",
		  analysisName: "ChIP-seq at four stages during cardiomyocyte differentiation",
		  analysisType: "ChIP Seq",
		  sampleConditions: "Global occupancy for histone modifications and RNA polymerase II",
		  analysisDescription: "Mouse Embryonic - Stage MES",
		  region: "ch13:123454-123898",
		  fdr: .01,
		  log2Ratio: 1.8
	 }
	                  
	                  
	];
	
	
	//Static dictionaries.
	StaticDictionary.organismBuildList(function(data) {
		$scope.organismBuildList = data;
	});
	
	//Dynamic dictionaries.  These dictionaries can be loaded on-demand.
    $scope.loadLabs = function() {
    	DynamicDictionary.loadLabs().success(function(data) {
    		$scope.labList = data;
    	});
    };
    $scope.loadSampleSources = function() {
    	DynamicDictionary.loadSampleSources().success(function(data) {
    		$scope.sampleSourceList = data;
    	});
    };
    
    //Load up dynamic dictionaries
	$scope.loadLabs();
	$scope.loadSampleSources();


	
	$scope.pickResultType = function() {
		for (var x = 0; x < $scope.analysisTypeCheckedList.length; x++) {
			var allowed = false;
			for (var idx = 0; idx < $scope.analysisTypeCheckedList[x].codeResultTypes.length; idx++) {
				if ($scope.codeResultType == $scope.analysisTypeCheckedList[x].codeResultTypes[idx]) {
					allowed = true;
					break;
				}
			}
			$scope.analysisTypeCheckedList[x].show = allowed;
			$scope.analysisTypeCheckedList[x].class = allowed ? '' : 'grey-out';
		}
		
		if ($scope.codeResultType == 'GENE' || $scope.codeResultType == 'VARIANT') {
			$scope.isGeneBasedQuery = true;
		} else {
			$scope.isGeneBasedQuery = false;
		}
		
		if ($scope.codeResultType == 'VARIANT') {
			$scope.isThresholdBasedQuery = false;
		} else {
			$scope.isThresholdBasedQuery = true;
		}
	};
	
	
	$scope.runQuery = function() {
		$scope.hasResults = false;
		
		// Build a summary of the query that is being performed.  This will display
		// in the results panel
		$scope.buildQuerySummary();
		
		$scope.hasResults = true;
		
	
	};
	
	$scope.clearQuery = function() {
		$scope.hasResults = false;
		$scope.queryForm.$setPristine();
		
		$scope.querySummary = [];
		$scope.codeResultType = "";
		$scope.isGeneBasedQuery = true;
		$scope.idOrganismBuild = "";

		
		$scope.selectedAnalysisTypes.length = 0;
		$scope.selectedLabs.length = 0;;
		$scope.projects.length = 0;
		$scope.analyses.length = 0;
		$scope.selectedSampleSources.length = 0;
		
		$scope.isIntersect = "true";
		$scope.intersectionTarget = "";
		$scope.regions = "";
		$scope.regionMargins = "1000";
		$scope.genes = "";
		$scope.geneMargins = "1000";
		$scope.selectedGeneAnnotations.length = 0;
		
		$scope.isThresholdBasedQuery = true;
		$scope.thresholdFDR = "";
		$scope.codeThresholdFDRComparison = ">";
		$scope.thresholdLog2Ratio = "";
		$scope.codeThresholdLog2RatioComparison = "> abs";
		
		$scope.thresholdVariantQual = "";
		$scope.codeThresholdVariantQualComparison = ">";
		$scope.codeVariantFilterType = "";
		$scope.codeVariantFilterType = "";
		$scope.selectedGenotypes.length = 0;
		
		for (var x =0; x < $scope.analysisTypeCheckedList.length; x++) {
			$scope.analysisTypeCheckedList[x].show = true;
			$scope.analysisTypeCheckedList[x].selected = false;
		}
	};
	
	$scope.someAnalysisTypeChecked = function() {
		var someSelected = false;
		for (var i=0; i < $scope.analysisTypeCheckedList.length; i++) {
			if ($scope.analysisTypeCheckedList[i].selected) {
				someSelected = true;
				break;
			}
		}
		return someSelected;
	};
	
	$scope.lookup = function(array, idAttributeName, id) {
		var element = null;
		for (var x = 0; x < array.length; x++) {
			if (array[x][idAttributeName] == id) {
				element = array[x];
				break;
			}
		}
		return element;
	};
	
	
	$scope.buildQuerySummary = function() {
		$scope.querySummary.length = 0;
		
		// Type of results
		$scope.querySummary.push("FIND  " + $scope.mapResultType[$scope.codeResultType]);
		
		// Genome build
		var ob = $scope.lookup($scope.organismBuildList, 'idOrganismBuild', $scope.idOrganismBuild);
		$scope.querySummary.push("FOR BUILD  " + ob.organism.common + ' ' + ob.name);
		
		// Data sets
		var datasetSummary = "";
		$scope.selectedAnalysisTypes.length = 0;
		for (var i=0; i < $scope.analysisTypeCheckedList.length; i++) {
			if ($scope.analysisTypeCheckedList[i].selected) {
				$scope.selectedAnalysisTypes.push($scope.analysisTypeCheckedList[i]);
			}
		}
		$scope.display = "";
		$scope.selectedAnalysisTypes.forEach($scope.concatDisplayName);
		if ($scope.display.length > 0) {
			datasetSummary = "ON  " + $scope.display + " data sets";
			
			// lab
			var labDisplay = $.map($scope.selectedLabs, function(lab){
			    return lab.first + ' ' + lab.last + ' lab';
			}).join(', ');
			if (labDisplay.length > 0) {
				datasetSummary += "  submitted by  " + labDisplay;
			}
			// sample source
			var sampleSourcesDisplay = $.map($scope.selectedSampleSources, function(ss){
			    return ss.source;
			}).join(', ');
			if (sampleSourcesDisplay.length > 0) {
				datasetSummary += " for samples from " + sampleSourcesDisplay;
			}
			$scope.querySummary.push(datasetSummary);

		}
		

		// intersect
		var intersectSummary = "";
		if ($scope.isIntersect == "true") {
			intersectSummary = "THAT INTERSECT ";
		} else if ($scope.isIntersect == "false") {
			intersectSummary = "THAT DON'T INTERSECT ";
		}		
		if (intersectSummary.length > 0) {
			if ($scope.intersectionTarget == 'REGION') {
				// Region based query
				if ($scope.regions.length > 0) {
					$scope.querySummary.push(intersectSummary + "REGIONS   " + $scope.regions + " +/- " + $scope.regionMargins);					
				}
				
			} else if ($scope.intersectionTarget == 'GENE') {
				// Gene based query
				var geneSummary = "";
				if ($scope.genes.length > 0) {
					geneSummary = $scope.genes;		
				}
				$scope.display = "";
				$scope.selectedGeneAnnotations.forEach($scope.concatDisplayName);
				if ($scope.display.length > 0) {
					geneSummary = geneSummary + " IDENTIFIED AS " + $scope.display + " +/- " + $scope.geneMargins;	
				}
				$scope.querySummary.push(intersectSummary + "GENES   " + geneSummary );
			}
		}
		if ($scope.isThresholdBasedQuery) {
			var thresholdQuery = "";
			if ($scope.thresholdFDR.length > 0) {
				thresholdQuery = "THAT EXCEED THRESHOLD of  " + "FDR " + $scope.codeThresholdFDRComparison + ' ' + $scope.thresholdFDR;
			}
			if ($scope.thresholdLog2Ratio.length > 0) {
				thresholdQuery = thresholdQuery + ($scope.thresholdFDR.length > 0 ? " AND ": "THAT EXCEED THRESHOLD   ");
				$scope.querySummary.push(thresholdQuery + "Log2Ratio " + $scope.codeThresholdLog2RatioComparison + ' ' + $scope.thresholdLog2Ratio);
			}
		} else {
			var variantQuery = "";
			if ($scope.thresholdVariantQual) {
				variantQuery = "QUAL " +  $scope.codeThresholdVariantQualComparison + ' ' + $scope.thresholdVariantQual;
			}
			if ($scope.codeVariantFilterPass) {
				if (variantQuery.length > 0) {
					variantQuery = variantQuery + ", ";
				}
				variantQuery = variantQuery + " " + $scope.codeVariantFilterPass;
			}
			if ($scope.codeVariantFilterType) {
				if (variantQuery.length > 0) {
					variantQuery = variantQuery + ", ";
				}
				variantQuery = variantQuery + "  " + $scope.codeVariantFilterType;
			}
			if ($scope.selectedGenotypes.length > 0) {
				$scope.display = "";
				$scope.selectedGenotypes.forEach($scope.concatDisplayName);
				if (variantQuery.length > 0) {
					variantQuery = variantQuery + ", ";
				}
				variantQuery = variantQuery + " " + $scope.display;
				
			}
			if (variantQuery.length > 0) {
				$scope.querySummary.push("FOR VARIANTS MATCHING " + variantQuery);
			}
		}
		
 	};
	
	$scope.concatDisplayName = function(element, index, array) {
  	  if ($scope.display.length > 0) {
		  $scope.display += ", ";
	  }
  	  $scope.display +=  element.name;
	};



}]);