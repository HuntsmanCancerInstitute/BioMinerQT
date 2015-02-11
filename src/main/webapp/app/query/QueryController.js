'use strict';


/**
 * QueryController
 * @constructor
 */
var query = angular.module('query', ['angularFileUpload','filters', 'services', 'directives', 'ui.bootstrap', 'chosen','angucomplete-alt','dialogs.main','ngProgress','error']);


angular.module("query").controller("QueryController", 
['$interval', '$window','$rootScope','$scope', '$http', '$modal','$anchorScroll','$upload','$location','$timeout','$q','DynamicDictionary','StaticDictionary','dialogs','ngProgress',
  
function($interval, $window, $rootScope, $scope, $http, $modal, $anchorScroll, $upload, $location, $timeout, $q, DynamicDictionary, StaticDictionary, dialogs, ngProgress) {
	
	$scope.hasResults = false;
	$scope.warnings = "";
	$scope.igvWarnings = "";
	
	$scope.querySummary = [];
	$scope.codeResultType = "";
	$scope.returedResultType = "";
	$scope.isGeneBasedQuery = false;
	$scope.idOrganismBuild = "";
	
	$scope.selectedAnalysisType = "";
	$scope.selectedAnalysisTypes = [];
	$scope.selectedLabs = [];
	$scope.selectedProjects = [];
	$scope.selectedAnalyses = [];
	$scope.selectedSampleSources = [];
	
	$scope.analysisList = [];
	
	$scope.intersectionTarget = "EVERYTHING";
	$scope.regions = "";
	$scope.regionMargins = "1000";
	$scope.genes = "";
	$scope.searchGenes = null;
	$scope.geneMargins = "1000";
	
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
	$scope.igvLoaded = false;
	
	$scope.hugoList = [];
	
	//Pagination
	$scope.queryCurrentPage = 0;
	$scope.resultPages = 0;
	$scope.resultsPerPage = 25;
	$scope.totalResults = 0;
	$scope.totalAnalyses = 0;
	$scope.totalDatatracks = 0;
	
	$scope.navigationOk = false; //When this is true, you can navigate away from the page
	$scope.queryDeferred = null;
	
	$scope.isReverse = false;
	$scope.searchExisting = false;
	
	//Sorting
	$scope.sortType = "FDR";
	
	//Copy and pase
	$scope.selectAll = false;
	
	$scope.showValidation = false;
	
	$rootScope.helpMessage = "<p>Placeholder for query help</p>";

	
	$scope.mapResultType = {
			'GENE' :     'Genes',
			'REGION' :   'Genomic Regions'};
//			'VARIANT' :  'Variants' };
	
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
    			$scope.analysisTypeCheckedList[idx].show      = true;
    			$scope.analysisTypeCheckedList[idx].codeResultTypes = $scope.analysisTypeCheckedList[idx].codeResultTypes.split(",");
    			$scope.analysisTypeCheckedList[idx].possible = true;
    			$scope.analysisTypeCheckedList[idx].class = '';
    		}
    		$scope.selectedAnalysisType = "";
    		$scope.loadAnalysisTypes();
    	});
    };
    
    
    $scope.$watch("idOrganismBuild",function() {
    	if ($scope.idOrganismBuild != null && $scope.idOrganismBuild != "") {
    		$scope.hugoList = [];
    		$http({
    			url: "query/getHugoNames",
    			method: "GET",
    			params: {idOrganismBuild: $scope.idOrganismBuild}
        	}).success(function(data) {
        		$scope.hugoList = data;
        	});
    	}
    	
    });
    
    $scope.$watch("selectAll",function() {
		for (var i=0;i<$scope.queryResults.length;i++) {
			$scope.queryResults[i].selected = $scope.selectAll;
		}
    });
    
    $scope.$on('$locationChangeStart', function( event, next, current ) {
    	if ($scope.navigationOk == false) {
    		event.preventDefault();
    		if ($scope.queryStarted) {
        		var dialog = dialogs.confirm("Page Navigation","Query isn't complete, are you sure you want to leave this page");
            	dialog.result.then(function() {
            		$timeout(function() {
            			$location.path(next.substring($location.absUrl().length - $location.url().length));
                        $scope.$apply();
            		});
            		$scope.navigationOk = true;
            	});
        	} else {
        		$timeout(function() {
        			$location.path(next.substring($location.absUrl().length - $location.url().length));
                    $scope.$apply();
        		});
        		$scope.navigationOk = true;
        	}
    	} else {
    		$scope.stopPing();
    	} 	
    });
    
    $scope.$on('$routeChangeStart', function (event, next, current) {
    	if (next.originalPath == current.originalPath) {
    		$scope.stopPing();
    	}
    	
    });
    
    $scope.copyCoordinates = function() {
    	var coordinateList = [];
    	for (var i=0; i<$scope.queryResults.length; i++) {
    		if ($scope.queryResults[i].selected) {
    			var coord = $scope.queryResults[i].coordinates;
    			if (coordinateList.indexOf(coord) == -1) {
    				coordinateList.push(coord);
    			}
    		}
    	}
    	var coordinateEntry = coordinateList.join("\n");
    	$scope.regions = coordinateEntry;
    	$scope.intersectionTarget = "REGION";
    };
    
    $scope.copyGenes = function() {
    	var geneList = [];
    	for (var i=0; i<$scope.queryResults.length; i++) {
    		if ($scope.queryResults[i].selected) {
    			var gene = $scope.queryResults[i].mappedName;
    			if (geneList.indexOf(gene) == -1) {
    				geneList.push(gene);
    			}
    		}
    	}
    	var geneEntry = geneList.join("\n");
    	$scope.genes = geneEntry;
    	$scope.intersectionTarget = "GENE";
    };
    
    $scope.loadRegions = function(files) {
    	ngProgress.start();
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
    			
    			dialogs.error(title,message,null);
        		
    		}
    		ngProgress.complete();
    	}).error(function(data) {
    		console.log("Error running upload");
    		ngProgress.reset();
    	});
	};
	
	$scope.loadGenes = function(files) {
		ngProgress.start();
    	$upload.upload({
    		url: "query/uploadGene",
    		file: files,
    	}).success(function(data) {
    		$scope.genes = data.regions;
    		if (data.message == null) {
    			$scope.genes = data.regions;
    		} else {
    			var message = data.message;
    			var title = "Error Processing Gene File";
    			
    			dialogs.error(title,message,null);
        		
    		}
    		ngProgress.complete();
    	}).error(function(data) {
    		console.log("Error running upload");
    		ngProgress.reset();
    	});
	};
	
	$scope.loadIgvSession = function(files) {
		
		$http({
			url: "query/startIgvSession",
			method: "GET"
		}).success(function(data) {
			if (data.warnings == "") {
				$scope.igvWarnings = "";
			} else {
				$scope.igvWarnings = data.warnings;
			}
			
			var urlPass = "http://127.0.0.1:60151/load?file=" + data.url2;
			var urlFail = data.url;
			$scope.pingIgvUrl(urlPass, urlFail);
			
			$scope.startPing();
			
		}).error(function(data) {
			dialogs.error("IGV Session Error",data.error,null);
			
			if (data.warnings == "") {
				$scope.igvWarnings = "";
			} else {
				$scope.igvWarnings = data.warnings;
			}
		});
	};
    
	$scope.loadLocus = function(queryResult) {
		var coord1 = queryResult.coordinates.split(":");
		var coord2 = coord1[1].split("-");
		var start = parseInt(coord2[0]);
		var end = parseInt(coord2[1]);
		
		if (queryResult.analysisType == "Variant") {
			start = start - 50;
			end = end + 50;
		} else {
			start = start - 1000;
			end = end + 1000;
		}
		
		var finalCoord = coord1[0] + ":" + start.toString() + "-" + end.toString();
		var url = "http://127.0.0.1:60151/goto?locus=" + finalCoord;
		
		$http({
			method: "GET",
			url: url
		}).success(function(data) {
			$scope.igvLoaded = true;
		}).error(function(data) {
			$scope.igvLoaded = false;
		});
		
		
	};
	
	
	$scope.startPing = function() {
		$scope.stopPing();
		$scope.checkIgv = $interval(function() {$scope.pingIGV();}, 10000);
	};
	
	$scope.stopPing = function() {
		$interval.cancel($scope.checkIgv);
		$scope.igvLoaded = false;
	};
	
	$scope.pingIgvUrl = function(urlPass,urlFail) {
		var url = "http://127.0.0.1:60151/execute?command=echo";
		
		$http({
			method: "GET",
			url: url
		}).success(function(data) {
			$scope.igvLoaded = true;
			$window.open(urlPass,"IGV");
		}).error(function(data) {
			$scope.igvLoaded = false;
			$window.open(urlFail,"IGV");
		});
	};
	
	$scope.pingIGV = function() {
		var url = "http://127.0.0.1:60151/execute?command=echo";
		
		$http({
			method: "GET",
			url: url
		}).success(function(data) {
			$scope.igvLoaded = true;
		}).error(function(data) {
			$scope.igvLoaded = false;
			$scope.stopPing();
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
				if ($scope.analysisTypeCheckedList[x].idAnalysisType == $scope.selectedAnalysisType.idAnalysisType) {
					$scope.selectedAnalysisType = "";
				}
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
		console.log("Clearing");
		$scope.hasResults = false;
		$scope.showValidation = false;
		$scope.queryForm.$setPristine();
		
		$scope.querySummary = [];
		$scope.codeResultType = "";
		$scope.isGeneBasedQuery = true;
		$scope.idOrganismBuild = "";

		$scope.selectedLabs.length = 0;
		$scope.selectedProjects.length = 0;
		$scope.selectedAnalyses.length = 0;
		$scope.selectedSampleSources.length = 0;
		
		$scope.intersectionTarget = "";
		$scope.regions = "";
		$scope.regionMargins = "1000";
		$scope.genes = "";
		$scope.geneMargins = "1000";
		
		$scope.isThresholdBasedQuery = true;
		$scope.thresholdFDR = "";
		$scope.codeThresholdFDRComparison = "LT";
		$scope.thresholdLog2Ratio = "";
		$scope.codeThresholdLog2RatioComparison = "GTABS";
		$scope.intersectionTarget = "EVERYTHING";
		
		$scope.thresholdVariantQual = "";
		$scope.codeThresholdVariantQualComparison = ">";
		$scope.codeVariantFilterType = "";
		$scope.codeVariantFilterType = "";
		$scope.selectedGenotypes.length = 0;
		
		$scope.totalResults = 0;
		$scope.totalAnalyses = 0;
		$scope.totalDatatracks = 0;
		
		$scope.selectedAnalysisType = "";
		$scope.searchExisting = false;
		$scope.isReverse = false;
		
		$scope.stopPing();
		
		for (var x =0; x < $scope.analysisTypeCheckedList.length; x++) {
			$scope.analysisTypeCheckedList[x].show = true;
			$scope.analysisTypeCheckedList[x].possible = true;
			$scope.analysisTypeCheckedList[x].class = '';
		}
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
	
	$scope.$watch("searchGenes",function() {
		if ($scope.searchGenes != null) {
			if ($scope.genes == "") {
				$scope.genes = $scope.searchGenes.title;
			} else {
				$scope.genes = $scope.genes + "," + $scope.searchGenes.title;
			}
		}
		
	});
	
	$scope.addGene = function() {
		if ($scope.searchGenes != null) {
			
			$scope.searchGenes = null;
		}
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
		
		
		var atDisplay = $scope.selectedAnalysisType.type;
		
		if (atDisplay.length > 0) {
			datasetSummary = "ON  " 
				
				
			if ($scope.searchExisting) {
				datasetSummary += " previous query "
			} else {
				datasetSummary += atDisplay + " data sets";
				
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
			}
				
			
			$scope.querySummary.push(datasetSummary);

		}
		
		console.log($scope.isThresholdBasedQuery);
		

		// intersect
		var intersectSummary = "";
		if ($scope.isReverse == false) {
			intersectSummary = "THAT INTERSECT ";
		} else if ($scope.isReverse == true) {
			intersectSummary = "THAT DON'T INTERSECT ";
		}		
		if (intersectSummary.length > 0) {
			if ($scope.intersectionTarget == 'REGION') {
				// Region based query
				if ($scope.regions.length > 0 && $scope.regions.length < 100) {
					$scope.querySummary.push(intersectSummary + "REGIONS   " + $scope.regions + " +/- " + $scope.regionMargins);					
				}
				
			} else if ($scope.intersectionTarget == 'GENE') {
				// Gene based query
				if ($scope.genes.length > 0 && $scope.genes.length < 100) {
					$scope.querySummary.push(intersectSummary + "GENES   " + $scope.genes + " +/- " + $scope.geneMargins);	
				}
				
			} else {
				$scope.querySummary.push(intersectSummary + " anything ");
			}
		}
		if ($scope.isThresholdBasedQuery) {
			
			var thresholdQuery = "";
			
			if ($scope.thresholdFDR != null && $scope.thresholdFDR != undefined && $scope.thresholdFDR != "") {
				thresholdQuery = "THAT EXCEED THRESHOLD of  " + "FDR " + $scope.mapComparison[$scope.codeThresholdFDRComparison] + ' ' + $scope.thresholdFDR;
			}
			if ($scope.thresholdLog2Ratio != null && $scope.thresholdLog2Ratio != undefined && $scope.thresholdLog2Ratio != "") {
				thresholdQuery = thresholdQuery + (thresholdQuery.length > 0 ? " AND ": " THAT EXCEED THRESHOLD   ");
				thresholdQuery = thresholdQuery + "Log2Ratio " + $scope.mapComparison[$scope.codeThresholdLog2RatioComparison] + ' ' + $scope.thresholdLog2Ratio;
				
			}
			
			if (thresholdQuery.length > 0) {
				$scope.querySummary.push(thresholdQuery);
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
	
	
	$scope.displayWarnings = function(type){
		var warnings = $scope.warnings;
		var title = "Query Warnings";
		if (type == "igv") {
			warnings = $scope.igvWarnings;
			title = "IGV Session Warnings";
		} 
		
		dialogs.error(title,warnings,null);
		
	};

	
	$scope.runQuery = function(isInvalid) {
		if (isInvalid) {
			$scope.showValidation = true;
			return;
		}
		
		$scope.warnings = "";
		$scope.showValidation = false;
		ngProgress.start();
		$scope.hasResults = false;
		$scope.queryCurrentPage = 0;
		
		//Turn query state to on
		$scope.stopPing();
		$scope.queryStarted = true;
		$scope.queryDeferred = $q.defer();
		
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
		
		var fdr = "";
		if ($scope.thresholdFDR != "" && $scope.thresholdFDR != null) {
			fdr = $scope.thresholdFDR;
		}
		
		var log2ratio = "";
		if ($scope.thresholdLog2Ratio != "" && $scope.thresholdLog2Ratio != null) {
			log2ratio = $scope.thresholdLog2Ratio;
		}
		
		var regions = "";
		if ($scope.regions != null && $scope.regions != "") {
			regions = $scope.regions;
		}
		
		var regionMargins = "";
		if ($scope.regionMargins != null && $scope.regionMargins != "") {
			regionMargins = $scope.regionMargins;
		}
		
		var genes = "";
		if ($scope.genes != null && $scope.genes != "") {
			genes = $scope.genes;
		}
		
		var geneMargins = "";
		if ($scope.geneMargins != null && $scope.geneMargins != "") {
			geneMargins = $scope.geneMargins;
		}
		
		
		$scope.returnedResultType = $scope.codeResultType;
		$scope.totalResults = 0;
		
		
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
				     regions:                 regions,
				     regionMargins:           regionMargins,
				     genes:                   genes,
				     geneMargins:             geneMargins,
				     FDR:                     fdr,
				     codeFDRComparison:       $scope.codeThresholdFDRComparison,
				     log2Ratio:               log2ratio,
				     codeLog2RatioComparison: $scope.codeThresholdLog2RatioComparison,
				     resultsPerPage:          $scope.resultsPerPage,
				     sortType:                $scope.sortType,
				     intersectionTarget:	  $scope.intersectionTarget,
				     isReverse:               $scope.isReverse,
				     searchExisting: 		  $scope.searchExisting
				     },
		    timeout: $scope.queryDeferred.promise,
				     
		}).success(function(data) {
			if (data != null) {
				$scope.queryResults = data.resultList;
				$scope.resultPages = data.pages;
				$scope.totalResults = data.resultNum;
				$scope.totalAnalyses = data.analysisNum;
				$scope.totalDatatracks = data.dataTrackNum;
				$scope.hasResults = true;
			}
			
			
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
			$scope.queryStarted = false;
			$scope.queryDeferred = null;
			ngProgress.complete();
		}).error(function(data, status, headers, config) {
			$scope.hasResults = false;
			$scope.resultPages = 0;
			$scope.totalResults = 0;
			$scope.totalAnalyses = 0;
			$scope.totalDatatracks = 0;
			$scope.returnedResultType = null;
			$scope.queryStarted = false;
			$scope.queryDeferred = null;
			ngProgress.reset();
		});
		
		$anchorScroll();
	};
	
	$scope.abortQuery = function() {
		if ($scope.queryDeferred != null) {
			$scope.queryDeferred.resolve("Query aborted by user");
			$scope.queryDeferred = null;
			console.log("Stop");
		}
		
		
	};
	
	$scope.downloadAnalysis = function() {
		$http({
			url: "query/downloadAnalysis",
			method: "GET"
		});
	};
	
	$scope.changeTablePosition = function() {
		$http({
			url: "query/changeTablePosition",
			method: "GET",
			params: {resultsPerPage:          $scope.resultsPerPage,
				     pageNum:                 $scope.queryCurrentPage,
				     sortType:                $scope.sortType},
				     
		}).success(function(data) {
			if (data != null) {
				$scope.queryResults = data.resultList;
				$scope.resultPages = data.pages;
				$scope.totalResults = data.resultNum;
				$scope.hasResults = true;
			}
			
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
			console.log("Could not change page!");
			$scope.hasResults = false;
		});
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
		var deferred = $q.defer();
		
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
			deferred.resolve();
		}).error(function(data, status, headers, config) {
			console.log("Could not get labList");
			deferred.reject();
		});
		
		return deferred.promise;
	};
	
	$scope.loadProjects = function() {
		var deferred = $q.defer();
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
			deferred.resolve();
		}).error(function(data, status, headers, config) {
			console.log("Could not get project list");
			deferred.reject();
		});
		return deferred.promise;
	};
	
	$scope.loadAnalyses = function() {
		var deferred = $q.defer();
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
			deferred.resolve();
		}).error(function(data, status, headers, config) {
			console.log("Could not get analysis list");
			deferred.reject();
		});
		return deferred.promise;
	};
	
	$scope.loadSampleSources = function() {
		var deferred = $q.defer();
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
			deferred.resolve();
		}).error(function(data, status, headers, config) {
			console.log("Could not get sample source list");
			deferred.reject();
		});
		return deferred.promise;
	};
	
	$scope.loadAnalysisTypes = function() {		
		var deferred = $q.defer();
		
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
					if ($scope.selectedAnalysisType.idAnalysisType == $scope.analysisTypeCheckedList[idx].idAnalysisType) {
						$scope.selectedAnalysisType = "";
					}
				} else {
					$scope.analysisTypeCheckedList[idx].possible = true;
					if ($scope.analysisTypeCheckedList[idx].show) {
						$scope.analysisTypeCheckedList[idx].class = '';
					}
				}
    		}
			deferred.resolve();
		}).error(function(data, status, headers, config) {
			console.log("Could not get analysis type list");
			deferred.reject();
		});
		return deferred.promise;
	};
	
	//Create watchers
	$scope.$watch("selectedAnalysisType",function(newValue, oldValue) {
		if (newValue != oldValue) {
			$scope.selectedAnalysisTypes = [];
			$scope.selectedAnalysisTypes.push($scope.selectedAnalysisType);
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
		if (newValue != undefined && newValue != oldValue) {
			$scope.loadLabs();
			$scope.loadProjects();
			$scope.loadAnalyses();
			$scope.loadSampleSources();
			$scope.loadAnalysisTypes();
		}
	});
	
	$scope.loadExistingResults = function() {
		$http({
			method: "GET",
			url: "query/loadExistingResults"
		}).success(function(data) {
			
			if (data != null && data != "") {
				$scope.queryResults = data.resultList;
				$scope.resultPages = data.pages;
				$scope.totalResults = data.resultNum;
				$scope.totalAnalyses = data.analysisNum;
				$scope.totalDatatracks = data.dataTrackNum;
				$scope.hasResults = true;
			}
		}).error(function(data) {
			$scope.hasResults = false;
			$scope.resultPages = 0;
			$scope.totalResults = 0;
			$scope.totalAnalyses = 0;
			$scope.totalDatatracks = 0;
			
		});
	};
	
	$scope.loadExistingSettings = function() {
		$http({
			method: "GET",
			url: "query/loadExistingSettings"
		}).success(function(data) {
			if (data != null && data != "") {
				$scope.codeResultType = data.codeResultType;
				$scope.returnedResultType = data.codeResultType;
				$scope.intersectionTarget = data.target;
				
				$scope.idOrganismBuild = data.idOrganismBuild;

				for (var i=0;i<$scope.analysisTypeCheckedList.length;i++) {
					for (var j=0;j<data.idAnalysisTypes.length;j++) {
						if ($scope.analysisTypeCheckedList[i].idAnalysisType == data.idAnalysisTypes[j]) {
							$scope.selectedAnalysisType = $scope.analysisTypeCheckedList[i];
						}
					}
				}
				
				for (var i=0; i<$scope.analysisList.length;i++) {
					for (var j=0; j < data.idAnalyses; j++) {
						if ($scope.analysisList[i].idAnalysis == data.idAnalyses[j]) {
							$scope.selectedAnalyses.push($scope.analysisList[i]);
						}
					}
				}
				
				for (var i=0; i<$scope.projectList.length;i++) {
					for (var j=0; j < data.idProjects.length; j++) {
						if ($scope.projectList[i].idProject == data.idProjects[j]) {
							$scope.selectedProjects.push($scope.projectList[i]);
						}
					}
				}
				
				for (var i=0; i<$scope.labList.length;i++) {
					for (var j=0; j < data.idLabs.length; j++) {
						if ($scope.labList[i].idLab == data.idLabs[j]) {
							$scope.selectedLabs.push($scope.labList[i]);
						}
					}
				}
			
				for (var i=0; i<$scope.sampleSourceList.length;i++) {
					for (var j=0; j < data.idSampleSources.length; j++) {
						if ($scope.sampleSourceList[i].idSampleSource == data.idSampleSources[j]) {
							$scope.selectedSampleSources.push($scope.sampleSourceList[i]);
						}
					}
				}
				
				$scope.regions = data.regions;
				$scope.regionMargins = data.regionMargins;
				$scope.genes = data.genes;
				$scope.geneMargins = data.geneMargins;
				
				if (data.fdr == null) {
					$scope.thresholdFDR = "";
				} else {
					$scope.thresholdFDR  = data.fdr;
				}
				
				if (data.log2Ratio == null) {
					$scope.thresholdLog2Ratio = "";
				} else {
					$scope.thresholdLog2Ratio = data.log2Ratio;
				}
				
				$scope.codeThresholdFDRComparison = data.codeFDRComparison;
				$scope.codeThresholdLog2RatioComparison = data.codeLog2RatioComparison;
				$scope.resultsPerPage = data.resultsPerPage;
				$scope.sortType = data.sortType;
				$scope.isReverse = data.reverse;
				
				
				$scope.buildQuerySummary();
			
				setTimeout(function () {
			        $scope.$apply(function() {
			        	$scope.searchExisting = data.searchExisting;
			        });
			    }, 1000);
				
			}  
		}).error(function(data) {
			console.log("calling clearQuery from error");
			$scope.clearQuery();
		});
	};

	//Load up dynamic dictionaries, which return promises
	var prepList = [];
	prepList.push($scope.loadLabs());
	prepList.push($scope.loadOrganismBuildList());
	prepList.push($scope.loadAnalysisTypeList());
	prepList.push($scope.loadProjects());
	prepList.push($scope.loadAnalyses());
	prepList.push($scope.loadSampleSources());
	
	//When all dictionaries are loaded, load settings
	$q.all(prepList).then(function() {
		$scope.loadExistingSettings();
		$scope.loadExistingResults();
	});
	

	$scope.loadGenotypeList();
	$scope.loadGeneAnnotationList();
	
}]);