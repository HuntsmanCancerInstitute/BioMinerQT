'use strict';


/**
 * QueryController
 * @constructor
 */
var query     = angular.module('query',     ['angularFileUpload','filters', 'services', 'directives', 'ui.bootstrap', 'chosen']);


angular.module("query").controller("QueryController", 
[ '$scope', '$http', '$modal','$anchorScroll','$upload','DynamicDictionary','StaticDictionary',
  
function($scope, $http, $modal, $anchorScroll, $upload, DynamicDictionary, StaticDictionary) {
	
	$scope.hasResults = false;
	$scope.warnings = "";
	
	$scope.querySummary = [];
	$scope.codeResultType = "";
	$scope.isGeneBasedQuery = false;
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
	$scope.codeThresholdFDRComparison = "LT";
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
    			$scope.analysisTypeCheckedList[idx].possible = true;
    		}
    		$scope.loadAnalysisTypes();
    	});
    };
    
    $scope.loadRegions = function(files) {
    	$upload.upload({
    		url: "query/upload",
    		file: files,
    	}).success(function(data) {
    		$scope.regions = data.regions;
    		if (data.message == null) {
    			$scope.regions = data.regions;
    		} else {
    			var message = data.message;
    			var title = "Error Processing Region File";
        		$modal.open({
            		templateUrl: 'app/common/userError.html',
            		controller: 'userErrorController',
            		resolve: {
            			title: function() {
            				return title;
            			},
            			message: function() {
            				return message;
            			}
            		}
            	});
    		}
    	}).error(function(data) {
    		console.log("Error running upload");
    	});
	};
    
    
	
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
			if ($scope.analysisTypeCheckedList[x].show && $scope.analysisTypeCheckedList[x].possible) {
				$scope.analysisTypeCheckedList[x].class = '';
			} else {
				$scope.analysisTypeCheckedList[x].class = 'grey-out';
			}
			
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
		$scope.selectedAnalyses.length = 0;
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
	
	
	$scope.displayWarnings = function(){
		$modal.open({
			templateUrl: 'app/common/userError.html',
			controller: 'userErrorController',
			resolve: {
				title: function() {
					var title = "Query Warnings";
					return title;
				},
				message: function() {
					return $scope.warnings;
				}
			}
		});
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

		var fdr = null;
		if ($scope.thresholdFDR != "") {
			fdr = $scope.thresholdFDR;
		}
		
		var log2ratio = null;
		if ($scope.thresholdLog2Ratio != "") {
			log2ratio = $scope.thresholdLog2Ratio;
		}
		
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
				     FDR:                     fdr,
				     codeFDRComparison:       $scope.codeThresholdFDRComparison,
				     log2Ratio:               log2ratio,
				     codeLog2RatioComparison: $scope.codeThresholdLog2RatioComparison},
				     
		}).success(function(data) {
			$scope.queryResults = data;
			$scope.hasResults = true;
			
			$http({
				url: "query/warnings",
				method: "GET",
			}).success(function(data) {
				if (data == "") {
					$scope.warnings = "";
				} else {
					$scope.warnings = data;
				}
			});
			
		}).error(function(data, status, headers, config) {
			console.log("Could not run query.");
			$scope.hasResults = true;
		});
		
		$anchorScroll();
	};
	
	$scope.loadOrganismBuildList = function() {		
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
	
		// Run the query on the server.
		$http({
			url: "query/getQueryOrganismBuilds",
			method: "GET",
			params: {
				     idAnalysisTypes:         idAnalysisTypeParams,
				     idLabs:                  idLabParams,
				     idProjects:              idProjectParams,
				     idAnalyses:              idAnalysisParams,
				     idSampleSources:         idSampleSourceParams},
		}).success(function(data) {
			$scope.organismBuildList = data;
		}).error(function(data, status, headers, config) {
			console.log("Could not get organismBuildList");
		});
	};
	
	
	$scope.loadLabs = function() {		
		var idAnalysisTypeParams = $.map($scope.selectedAnalysisTypes, function(analysisType){
		    return analysisType.idAnalysisType;
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
	
		// Run the query on the server.
		$http({
			url: "query/getQueryLabs",
			method: "GET",
			params: {
				     idAnalysisTypes:         idAnalysisTypeParams,
				     idProjects:              idProjectParams,
				     idAnalyses:              idAnalysisParams,
				     idSampleSources:         idSampleSourceParams,
				     idOrganismBuild:         $scope.idOrganismBuild},
		}).success(function(data) {
			$scope.labList = data;
		}).error(function(data, status, headers, config) {
			console.log("Could not get labList");
		});
	};
	
	$scope.loadProjects = function() {		
		var idAnalysisTypeParams = $.map($scope.selectedAnalysisTypes, function(analysisType){
		    return analysisType.idAnalysisType;
		}).join(',');
		
		var idLabParams = $.map($scope.selectedLabs, function(lab){
		    return lab.idLab;
		}).join(',');
		
		var idAnalysisParams = $.map($scope.selectedAnalyses, function(analysis){
		    return analysis.idAnalysis;
		}).join(',');
		
		var idSampleSourceParams = $.map($scope.selectedSampleSources, function(ss){
		    return ss.idSampleSource;
		}).join(',');
	
		// Run the query on the server.
		$http({
			url: "query/getQueryProjects",
			method: "GET",
			params: {
				     idAnalysisTypes:         idAnalysisTypeParams,
				     idLabs:                  idLabParams,
				     idAnalyses:              idAnalysisParams,
				     idSampleSources:         idSampleSourceParams,
				     idOrganismBuild:         $scope.idOrganismBuild},
		}).success(function(data) {
			$scope.projectList = data;
		}).error(function(data, status, headers, config) {
			console.log("Could not get project list");
		});
	};
	
	$scope.loadAnalyses = function() {		
		var idAnalysisTypeParams = $.map($scope.selectedAnalysisTypes, function(analysisType){
		    return analysisType.idAnalysisType;
		}).join(',');
		
		var idLabParams = $.map($scope.selectedLabs, function(lab){
		    return lab.idLab;
		}).join(',');
		
		var idProjectParams = $.map($scope.selectedProjects, function(project){
		    return project.idProject;
		}).join(',');
		
		var idSampleSourceParams = $.map($scope.selectedSampleSources, function(ss){
		    return ss.idSampleSource;
		}).join(',');
	
		// Run the query on the server.
		$http({
			url: "query/getQueryAnalyses",
			method: "GET",
			params: {
				     idAnalysisTypes:         idAnalysisTypeParams,
				     idLabs:                  idLabParams,
				     idProjects:              idProjectParams,
				     idSampleSources:         idSampleSourceParams,
				     idOrganismBuild:         $scope.idOrganismBuild},
		}).success(function(data) {
			$scope.analysisList = data;
		}).error(function(data, status, headers, config) {
			console.log("Could not get analysis list");
		});
	};
	
	$scope.loadSampleSources = function() {		
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
		
	
		// Run the query on the server.
		$http({
			url: "query/getQuerySampleSource",
			method: "GET",
			params: {
				     idAnalysisTypes:         idAnalysisTypeParams,
				     idLabs:                  idLabParams,
				     idProjects:              idProjectParams,
				     idAnalyses:              idAnalysisParams,
				     idOrganismBuild:         $scope.idOrganismBuild},
		}).success(function(data) {
			$scope.sampleSourceList = data;
		}).error(function(data, status, headers, config) {
			console.log("Could not get sample source list");
		});
	};
	
	$scope.loadAnalysisTypes = function() {		
		
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
		
	
		// Run the query on the server.
		$http({
			url: "query/getQueryAnalysisTypes",
			method: "GET",
			params: {
				     idSampleSources:         idSampleSourceParams,
				     idLabs:                  idLabParams,
				     idProjects:              idProjectParams,
				     idAnalyses:              idAnalysisParams,
				     idOrganismBuild:         $scope.idOrganismBuild},
		}).success(function(data) {
			for (var idx = 0; idx < $scope.analysisTypeCheckedList.length; idx++) {
				var found  = false;
				for (var idx2 = 0; idx2 < data.length; idx2++) {
					if ($scope.analysisTypeCheckedList[idx].idAnalysisType == data[idx2].idAnalysisType) {
						found = true;
					}
				}
				if (!found) {
					$scope.analysisTypeCheckedList[idx].possible = false;
					$scope.analysisTypeCheckedList[idx].class = 'grey-out';
				} else {
					$scope.analysisTypeCheckedList[idx].possible = true;
					$scope.analysisTypeCheckedList[idx].class  = '';
				}
    		}
		}).error(function(data, status, headers, config) {
			console.log("Could not get analysis type list");
		});
	};
	
	//Create watchers
	$scope.$watch("selectedAnalysisTypes",function(newValue, oldValue) {
		if (newValue != oldValue) {
			$scope.loadOrganismBuildList();
			$scope.loadLabs();
			$scope.loadProjects();
			$scope.loadAnalyses();
			$scope.loadSampleSources();
		}
	});
	
	$scope.$watch("selectedLabs",function(newValue, oldValue) {
		if (newValue != oldValue) {
			$scope.loadOrganismBuildList();
			$scope.loadProjects();
			$scope.loadAnalyses();
			$scope.loadSampleSources();
			$scope.loadAnalysisTypes();
			
		}
	});
	
	$scope.$watch("selectedAnalyses",function(newValue, oldValue) {
		if (newValue != oldValue) {
			$scope.loadOrganismBuildList();
			$scope.loadLabs();
			$scope.loadProjects();
			$scope.loadSampleSources();
			$scope.loadAnalysisTypes();
		}
	});
	
	$scope.$watch("selectedSampleSources",function(newValue, oldValue) {
		if (newValue != oldValue) {
			$scope.loadOrganismBuildList();
			$scope.loadLabs();
			$scope.loadProjects();
			$scope.loadAnalyses();
			$scope.loadAnalysisTypes();
		}
	});
	
	$scope.$watch("selectedProjects",function(newValue, oldValue) {
		if (newValue != oldValue) {
			$scope.loadOrganismBuildList();
			$scope.loadLabs();
			$scope.loadAnalyses();
			$scope.loadSampleSources();
			$scope.loadAnalysisTypes();
		}
	});
	
	$scope.$watch("idOrganismBuild",function(newValue, oldValue) {
		if (newValue != oldValue) {
			$scope.loadLabs();
			$scope.loadProjects();
			$scope.loadAnalyses();
			$scope.loadSampleSources();
			$scope.loadAnalysisTypes();
		}
	});

	//Load up dynamic dictionaries
	$scope.loadLabs();
	$scope.loadSampleSources();
	$scope.loadOrganismBuildList();
	$scope.loadAnalysisTypeList();
	$scope.loadProjects();
	$scope.loadAnalyses();
	$scope.loadGenotypeList();
	$scope.loadGeneAnnotationList();


}]);