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
	$scope.selectedProjects = [];
	$scope.selectedAnalyses = [];
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
	$scope.codeThresholdFDRComparison = "GT";
	$scope.thresholdLog2Ratio = "";
	$scope.codeThresholdLog2RatioComparison = "GTABS";
	
	$scope.thresholdVariantQual = "";
	$scope.codeThresholdVariantQualComparison = ">";
	$scope.codeVariantFilterType = "";
	$scope.codeVariantFilterType = "";
	$scope.selectedGenotypes = [];

	
	$scope.mapResultType = {
			'GENE' :     'Genes',
			'REGION' :   'Genomic Regions',
			'VARIANT' :  'Variants' };
	
	$scope.mapComparison = {
		'GT':    '>',
		'GTABS': '> abs',
		'LT':    '<'
	};

   
	
	$scope.queryResults = [];
	
	
	//Static dictionaries.
	
	$scope.loadOrganismBuildList = function () {
    	StaticDictionary.getOrganismBuildList().success(function(data) {
    		$scope.organismBuildList = data;
    	});
    };
    $scope.loadGenotypeList = function () {
    	StaticDictionary.getGenotypeList().success(function(data) {
    		$scope.genotypeList = data;
    	});
    };
    $scope.loadGeneAnnotationList = function () {
    	StaticDictionary.getGeneAnnotationList().success(function(data) {
    		$scope.geneAnnotationList = data;
    	});
    };
	$scope.loadAnalysisTypeList = function () {
    	StaticDictionary.getAnalysisTypeList().success(function(data) {
    		$scope.analysisTypeCheckedList = data;
    		for (var idx = 0; idx < $scope.analysisTypeCheckedList.length; idx++) {
    			$scope.analysisTypeCheckedList[idx].selected  = false;
    			$scope.analysisTypeCheckedList[idx].show      = true;
    			$scope.analysisTypeCheckedList[idx].codeResultTypes = $scope.analysisTypeCheckedList[idx].codeResultTypes.split(",");
    		}
    	});
    };
    

	
	//Dynamic dictionaries.  These dictionaries can be loaded on-demand.
    $scope.loadLabs = function() {
    	DynamicDictionary.loadQueryLabs().success(function(data) {
    		$scope.labList = data;
    	});
    };
    $scope.loadProjects = function() {
    	DynamicDictionary.loadQueryProjects().success(function(data) {
    		$scope.projectList = data;
    	});
    };
    $scope.loadAnalyses = function() {
    	DynamicDictionary.loadQueryAnalyses().success(function(data) {
    		$scope.analysisList = data;
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
	$scope.loadOrganismBuildList();
	$scope.loadAnalysisTypeList();
	$scope.loadProjects();
	$scope.loadAnalyses();
	$scope.loadGenotypeList();
	$scope.loadGeneAnnotationList();



	
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
		
	$scope.clearQuery = function() {
		$scope.hasResults = false;
		$scope.queryForm.$setPristine();
		
		$scope.querySummary = [];
		$scope.codeResultType = "";
		$scope.isGeneBasedQuery = true;
		$scope.idOrganismBuild = "";

		
		$scope.selectedAnalysisTypes.length = 0;
		$scope.selectedLabs.length = 0;
		$scope.selectedProjects.length = 0;
		$scope.selectedAalyses.length = 0;
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
		
		var atDisplay = $.map($scope.selectedAnalysisTypes, function(analysisType){
		    return analysisType.type;
		}).join(', ');
		if (atDisplay.length > 0) {
			datasetSummary = "ON  " + atDisplay + " data sets";
			
			// lab
			var labDisplay = $.map($scope.selectedLabs, function(lab){
			    return lab.first + ' ' + lab.last + ' lab';
			}).join(', ');
			if (labDisplay.length > 0) {
				datasetSummary += "  submitted by  " + labDisplay;
			}
			
			// project
			var projectDisplay = $.map($scope.selectedProjects, function(project){
			    return project.name;
			}).join(', ');
			if (projectDisplay.length > 0) {
				datasetSummary += "  for projects  " + projectDisplay;
			}

			// analysis
			var analysisDisplay = $.map($scope.selectedAnalyses, function(analysis){
			    return analysis.name;
			}).join(', ');
			if (analysisDisplay.length > 0) {
				datasetSummary += "  for analysis  " + analysisDisplay;
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
				thresholdQuery = "THAT EXCEED THRESHOLD of  " + "FDR " + $scope.mapComparison[$scope.codeThresholdFDRComparison] + ' ' + $scope.thresholdFDR;
			}
			if ($scope.thresholdLog2Ratio.length > 0) {
				thresholdQuery = thresholdQuery + ($scope.thresholdFDR.length > 0 ? " AND ": "THAT EXCEED THRESHOLD   ");
				$scope.querySummary.push(thresholdQuery + "Log2Ratio " + $scope.mapComparison[$scope.codeThresholdLog2RatioComparison] + ' ' + $scope.thresholdLog2Ratio);
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
	

	
	$scope.runQuery = function() {
		$scope.hasResults = false;
		
		// Build a summary of the query that is being performed.  This will display
		// in the results panel
		$scope.buildQuerySummary();
		
		var idAnalysisTypeParams = $.map($scope.selectedAnalysisTypes, function(analysisType){
		    return analysisType.idAnalysisType;
		}).join(',');
		
		var idLabParams = $.map($scope.selectedLabs, function(lab){
		    return lab.idLab;
		}).join(',');
		
		var idProjectParams = $.map($scope.selectedProjects, function(project){
		    return project.idProject;
		}).join(',');
		
		var idAnalysisParams = $.map($scope.selectedAnalyses, function(analysis){
		    return analysis.idAnalysis;
		}).join(',');
		
		var idSampleSourceParams = $.map($scope.selectedSampleSources, function(ss){
		    return ss.idSampleSource;
		}).join(',');
		
	
		var idGeneAnnotationParams = $.map($scope.selectedGeneAnnotations, function(ga){
		    return ga.idGeneAnnotation;
		}).join(',');

		
		// Run the query on the server.
		$http({
			url: "query/run",
			method: "GET",
			params: {codeResultType:          $scope.codeResultType,
				     idOrganismBuild:         $scope.idOrganismBuild,
				     idAnalysisTypes:         idAnalysisTypeParams,
				     idLabs:                  idLabParams,
				     idProjects:              idProjectParams,
				     idAnalyses:              idAnalysisParams,
				     idSampleSources:         idSampleSourceParams,
				     isIntersect:             $scope.isIntersect,
				     regions:                 $scope.regions,
				     regionMargins:           $scope.regionMargins,
				     genes:                   $scope.genes,
				     geneMargins:             $scope.geneMargins,
				     idGeneAnnotations:       idGeneAnnotationParams,
				     isThresholdBasedQuery:   $scope.isThresholdBasedQuery,
				     FDR:                     $scope.thresholdFDR,
				     codeFDRComparison:       $scope.codeThresholdFDRComparison,
				     log2Ratio:               $scope.thresholdLog2Ratio,
				     codeLog2RatioComparison: $scope.codeThresholdLog2RatioComparison},
				     
		}).success(function(data) {
			$scope.queryResults = data;
			$scope.hasResults = true;
		}).error(function(data, status, headers, config) {
			console.log("Could not run query.");
			$scope.hasResults = true;
		});

		
		
	
	};




}]);