'use strict';


/**
 * QueryController
 * @constructor
 */
var query     = angular.module('query',     ['filters', 'services', 'directives', 'ui.bootstrap']);

angular.module("app").controller("QueryController", [
'$scope', '$http', '$modal',                                                      
function($scope, $http) {
	$scope.querySummary = [];
	$scope.codeResultType = "";
	$scope.genomeBuild = "";

	$scope.checkedAnalysisTypes = [false, false, false, false];
	$scope.selectedAnalysisTypes = [];
	$scope.selectedLabs = [];
	$scope.projects = [];
	$scope.analyses = [];
	$scope.sampleSources = [];
	
	$scope.codeIntersect = "";
	$scope.thresholdFDR = "";
	$scope.codeThresholdFDRComparison = "";
	$scope.thresholdLog2Ratio = "";
	$scope.codeThresholdLog2RatioComparison = "";
	
	$scope.geneAnnotations = [];
	$scope.codeVariantPass = "";
	$scope.codeVariantType = "";
	$scope.genotypes = [];
	
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

    
    $scope.geneAnnotationList = [
                   {"idGeneAnnotation": 1, "name": "TSS"},
                   {"idGeneAnnotation": 2, "name": "Genic"},
                   {"idGeneAnnotation": 3, "name": "Intronic"},
                   {"idGeneAnnotation": 4, "name": "Exonic"},
                   {"idGeneAnnotation": 5, "name": "Intergenic"}
                     		
    ];
    
    
    $scope.genotypeList = [
                   {"idGenotype": 1, "name": "Homozygous"},
                   {"idGenotype": 2, "name": "Heterozygous"},
                   {"idGenotype": 3, "name": "Carrier Mutant"},
                   {"idGenotype": 4, "name": "Carrier Reference"},
                   {"idGenotype": 5, "name": "Reference"}
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
	   	                  {idSampleSource: 1, name: "Heart, Left ventricle", organ: "Heart"},
	   	                  {idSampleSource: 2, name: "Heart, Right ventricle", organ: "Heart"},
	   	                  {idSampleSource: 3, name: "Heart, Aortic valve", organ: "Heart"},
	   	                  {idSampleSource: 4, name: "Lung, Left interior lobe", organ: "Lung"},
	   	                  {idSampleSource: 5, name: "Lung, Right interior lobe", organ: "Lung"}
	   	                  
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
	
	$scope.runQuery = function() {
		$scope.selectedAnalysisTypes.length = 0;
		for (var i=0; i < $scope.checkedAnalysisTypes.length; i++) {
			if ($scope.checkedAnalysisTypes[i]) {
				$scope.selectedAnalysisTypes.push($scope.analysisTypeList[i]);
			}
		}
		
		var theAnalysisTypes = "";
		$scope.selectedAnalysisTypes.forEach(function(element, index, array) {
	    	  if (theAnalysisTypes.length > 0) {
	    		  theAnalysisTypes = theAnalysisTypes + ", ";
	    	  }
	    	  theAnalysisTypes = theAnalysisTypes + element.name;
	     });
		if (theAnalysisTypes.length > 0) {
			theAnalysisTypes = "On" + theAnalysisTypes + " data sets";
		}

		
		var theLabs = "";
		$scope.selectedLabs.forEach(function(element, index, array) {
	    	  if (theLabs.length > 0) {
	    		  theLabs = theLabs + ", ";
	    	  }
	    	  theLabs = theLabs + element.name;
	     });
		if (theLabs.length > 0) {
			theLabs = "Submitted by " + theLabs;
		}

		$scope.querySummary.length = 0;
		$scope.querySummary.push("Find " + $scope.mapResultType[$scope.codeResultType]);
		$scope.querySummary.push("For build " + $scope.genomeBuild.species + ' ' + $scope.genomeBuild.name);
		$scope.querySummary.push(theAnalysisTypes);
		$scope.querySummary.push(theLabs);
	
	};
	


}]);