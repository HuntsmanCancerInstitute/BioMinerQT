'use strict';


/**
 * QueryController
 * @constructor
 */
var query     = angular.module('query',     ['filters', 'services', 'directives', 'ui.bootstrap']);

angular.module("app").controller("QueryController", [
'$scope', '$http', '$modal',                                                      
function($scope, $http) {
	
	$scope.selectedLabs = [];
	$scope.selectedSpecies = [];
	$scope.genomeBuild = "";
	$scope.sampleSource = "";
	$scope.geneAnnotations = [];
	$scope.gentypes = [];
	
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
    
    $scope.geneAnnotationList = [
                   {"idGeneAnnotation": 1, "name": "TSS"},
                   {"idGeneAnnotation": 2, "name": "Genic"},
                   {"idGeneAnnotation": 3, "name": "Intronic"},
                   {"idGeneAnnotation": 4, "name": "Exonic"},
                   {"idGeneAnnotation": 5, "name": "Intergenic"}
                     		
    ];
    
    
    $scope.genotypeList = [
                   {"idGenotype": 1, "name": "Homozygous Mutant"},
                   {"idGenotype": 2, "name": "Heterozygous Mutant"},
                   {"idGenotype": 3, "name": "Compound Heterozygous "},
                   {"idGenotype": 4, "name": "Wildtype"}
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

}]);