'use strict';


/**
 * QueryController
 * @constructor
 */
var query     = angular.module('query',     ['filters', 'services', 'directives', 'ui.bootstrap']);


angular.module("query").controller("QueryController", 
[ '$scope', '$http', '$modal',
  
function($scope, $http, $filter) {
	$scope.querySummary = [];
	$scope.codeResultType = "";
	$scope.isGeneBasedQuery = true;
	$scope.genomeBuild = "";

	
	$scope.selectedAnalysisTypes = [];
	$scope.selectedLabs = [];
	$scope.projects = [];
	$scope.analyses = [];
	$scope.selectedSampleSources = [];
	
	$scope.isIntersect = "";
	$scope.intersectionTarget = "";
	$scope.regions = "";
	$scope.regionMargins = "";
	$scope.genes = "";
	$scope.geneMargins = "";
	$scope.selectedGeneAnnotations = [];
	$scope.geneAnnotationMargins = "";
	
	$scope.isThresholdBasedQuery = true;
	$scope.thresholdFDR = "";
	$scope.codeThresholdFDRComparison = "";
	$scope.thresholdLog2Ratio = "";
	$scope.codeThresholdLog2RatioComparison = "";
	
	$scope.geneAnnotations = [];
	$scope.codeVariantFilterType = "";
	$scope.codeVariantFilterType = "";
	$scope.selectedGenotypes = [];
	
	// Temporary mockup code... these should be in a parent model
	$scope.labList = [
	                  {"idLab": 1,
	               	"name": "Yost lab"                	
	                  },
	                  {"idLab": 2,
	                  	"name": "Bruneau lab"                	
	                  },
	                  {"idLab": 3,
	                  	"name": "Pu lab"                	
	                      }
	                        
    ];
	
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
	               	        	"selected": false, "show" : false, "class": ""},
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
       
       
   $scope.visibilityList = [
                   {"codeVisibility": "MEM", "name": "Lab members"},
                   {"codeVisibility": "INST", "name": "Institution"},
                   {"codeVisibility": "PUBLIC", "name": "Public"}
   		
   ];
       
	$scope.genomeBuildList = [
	                 {"idGenomeBuild": 1, name: "hg17", species: "Human"},
	                 {"idGenomeBuild": 2, name: "hg18", species: "Human"},
	                 {"idGenomeBuild": 3, name: "mm18", species: "Mouse"},
	                 {"idGenomeBuild": 4, name: "mm19", species: "Mouse"},
	                 {"idGenomeBuild": 5, name: "zb 1", species: "Zebrafish"},
	                 {"idGenomeBuild": 6, name: "zb 2", species: "Zebrafish"},
	                 {"idGenomeBuild": 7, name: "zb 3", species: "Zebrafish"}
	];
	$scope.speciesList = [
	     	                 {"idSpecies": 1, name: "Human"},
	     	                 {"idSpecies": 2, name: "Mouse"},
	     	                 {"idSpecies": 3, name: "Chicken"},
	     	                 {"idSpecies": 4, name: "Zebrafish"},
   ];
	
	$scope.sampleTypeList = [
	                  {idSampleType: 1, name: "RNA -> polyA"},
	                  {idSampleType: 2, name: "RNA->RiboZero"},
	                  {idSampleType: 3, name: "ChIP DNA"},
	                  {idSampleType: 4, name: "DNA"}
	                  
	];
	
	$scope.sampleSourceList = [
	 	   	              {idSampleSource: 0, name: "Cell Line", organ: "Cell Line"},
	   	                  {idSampleSource: 1, name: "Heart Left ventricle", organ: "Heart"},
	   	                  {idSampleSource: 2, name: "Heart Right ventricle", organ: "Heart"},
	   	                  {idSampleSource: 3, name: "Heart Aortic valve", organ: "Heart"},
	   	                  {idSampleSource: 4, name: "Lung Left interior lobe", organ: "Lung"},
	   	                  {idSampleSource: 5, name: "Lung Right interior lobe", organ: "Lung"}
	   	                  
	];
	
	
	$scope.sampleGroupList = [
	                      {idSampleGroup: 1, name: "Effect (wildtype)"}, 
	                      {idSampleGroup: 2, name: "Treated"}, 
	                      {idSampleGroup: 3, name: "Tumor"}, 
	                      {idSampleGroup: 4, name: "Normal"}, 
	                      {idSampleGroup: 5, name: "Control"}, 
	                      {idSampleGroup: 6, name: "Other (specify)"} 
   	                      
	];
	$scope.analysisTypeList = [
	   	          {idAnalysisType: 1, name: "ChIP Seq"},
	              {idAnalysisType: 2, name: "RNA Seq"},
	              {idAnalysisType: 3, name: "Methylation Analysis"},
	              {idAnalysisType: 4, name: "Variant Calling"}
	   	                  
	];
	
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
		
		// Build a summary of the query that is being performed.  This will display
		// in the results panel
		$scope.buildQuerySummary();
		
	
	};
	
	
	$scope.buildQuerySummary = function() {
		$scope.querySummary.length = 0;
		
		// type of results
		$scope.querySummary.push("FIND  " + $scope.mapResultType[$scope.codeResultType]);
		
		// genome build
		$scope.querySummary.push("FOR BUILD  " + $scope.genomeBuild.species + ' ' + $scope.genomeBuild.name);
		
		// lab
		$scope.display = "";
		$scope.selectedLabs.forEach($scope.concatDisplayName);
		if ($scope.display.length > 0) {
			$scope.querySummary.push("SUBMITTED BY  " + $scope.display);
		}
		
		// analysis types (data sets)
		$scope.selectedAnalysisTypes.length = 0;
		for (var i=0; i < $scope.analysisTypeCheckedList.length; i++) {
			if ($scope.analysisTypeCheckedList[i].selected) {
				$scope.selectedAnalysisTypes.push($scope.analysisTypeCheckedList[i]);
			}
		}
		$scope.display = "";
		$scope.selectedAnalysisTypes.forEach($scope.concatDisplayName);
		if ($scope.display.length > 0) {
			$scope.querySummary.push("ON  " + $scope.display + " data sets");
		}
		
		// sample source
		$scope.display = "";
		$scope.selectedSampleSources.forEach($scope.concatDisplayName);
		if ($scope.display.length > 0) {
			$scope.querySummary.push("FOR SAMPLES FROM " + $scope.display);
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
					geneSummary = $scope.genes + " +/- " + $scope.geneMargins;		
				}
				$scope.display = "";
				$scope.selectedGeneAnnotations.forEach($scope.concatDisplayName);
				if ($scope.display.length > 0) {
					geneSummary = geneSummary + " IDENTIFIED AS " + $scope.display + " +/- " + $scope.geneAnnotationMargins;	
				}
				$scope.querySummary.push(intersectSummary + "GENES   " + geneSummary );
			}
		}
		if ($scope.isThresholdBasedQuery) {
			var thresholdQuery = "";
			if ($scope.thresholdFDR.length > 0) {
				thresholdQuery = "THAT EXCEED THRESHOLD of  " + "FDR " + $scope.codeThresholdFDRComparison + $scope.thresholdFDR;
			}
			if ($scope.thresholdLog2Ratio.length > 0) {
				thresholdQuery = thresholdQuery + ($scope.thresholdFDR.length > 0 ? " AND ": "THAT EXCEED THRESHOLD   ");
				$scope.querySummary.push(thresholdQuery + "Log2Ratio " + $scope.codeThresholdLog2RatioComparison + $scope.thresholdLog2Ratio);
			}
		} else {
			var variantQuery = "";
			if ($scope.codeVariantFilterPass.length > 0) {
				variantQuery = $scope.codeVariantFilterPass;
			}
			if ($scope.codeVariantFilterType.length > 0) {
				if (variantQuery.length > 0) {
					variantQuery = variantQuery + " OR ";
				}
				variantQuery = variantQuery + $scope.codeVariantFilterType;
			}
			if ($scope.selectedGenotypes.length > 0) {
				$scope.display = "";
				$scope.selectedGenotypes.forEach($scope.concatDisplayName);
				if (variantQuery.length > 0) {
					variantQuery = variantQuery + " OR ";
				}
				variantQuery = variantQuery + $scope.display;
				
			}
			if (variantQuery.length > 0) {
				$scope.querySummary.push("THAT EXCEED THRESHOLD of " + variantQuery);
			}
		}
		
 	};
	
	$scope.concatDisplayName = function(element, index, array) {
  	  if ($scope.display.length > 0) {
		  $scope.display = $scope.display + ", ";
	  }
  	  $scope.display =  $scope.display + element.name;
	};
	


}]);