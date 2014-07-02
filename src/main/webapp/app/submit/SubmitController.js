'use strict';

/**
 * SubmitController
 * @constructor
 */
var submit    = angular.module('submit', ['localytics.directives','ui.bootstrap', 'blueimp.fileupload','filters', 'services', 'directives']);

angular.module("submit").controller("SubmitController", [
'$scope', '$http', '$modal','DynamicDictionary','StaticDictionary',
function($scope, $http, $modal, DynamicDictionary, StaticDictionary) {
	/**********************
	 * Initialization!
	 *********************/
	
	//enums!
	$scope.projectVisibilities = [{enum: "LAB",name: "Lab"},{enum: "INSTITUTE", name: "Institute"},{enum: "PUBLIC", name: "Public"}];
	
	//current project
	$scope.projectId = -1;
	
	//containers
	$scope.uploadedFiles = [];
    $scope.samples = [];
    $scope.datatracks = [];
    $scope.results = [];
    $scope.samplePrepList = [];
    
    //active
    $scope.sample = {sampleType: null};
    $scope.datatrack = {};
    $scope.result = {};
    
    //flags
    $scope.sampleEditMode = false;
    $scope.datatrackEditMode = false;
	
	//Static dictionaries.
	StaticDictionary.organismBuildList(function(data) {
		$scope.organismBuildList = data;
	});
	
	StaticDictionary.analysisTypeList(function(data) {
		$scope.analysisTypeList = data;
	});
	
	StaticDictionary.sampleTypeList(function(data) {
		$scope.sampleTypeList = data;
	});
	
	StaticDictionary.samplePrepList(function(data) {
		$scope.samplePrepListAll = data;
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
    
    $scope.loadSampleConditions = function() {
    	DynamicDictionary.loadSampleConditions().success(function(data) {
    		$scope.sampleConditionList = data;
    	});
    };
  
    //Load up dynamic dictionaries
	$scope.loadLabs();
	$scope.loadSampleConditions();
	$scope.loadSampleSources();
    
    //Watchers
    $scope.$watch('sample.sampleType',function() {
    	if ($scope.sample.sampleType == null) {
    		$scope.samplePrepList = null;
    	} else {
    		DynamicDictionary.loadSamplePrepsBySampleType($scope.sample.sampleType.idSampleType).success(function(data) {
    			$scope.samplePrepList = data;
    		});
    	}
    });
    
    /**********************
	 * Project management
	 *********************/
    
    //load projects (currently all, will be tied to user going forward)
    $scope.loadProjects = function(projectId) {
    	$http({
			url: "project/getAllProjects",
			method: "POST",
			params: {projectId: projectId},
		}).success(function(data, status, headers, config) {
			$scope.projectId = config.params.projectId;
			$scope.projects = data;
			for (var i=0; i<$scope.projects.length; i++) {
				if ($scope.projects[i].idProject == $scope.projectId) {
					$scope.samples = $scope.projects[i].samples;
					$scope.datatracks = $scope.projects[i].dataTracks;
				}
			}
		});
    };
    
    $scope.loadProjects($scope.projectId);
    
    //Create project
    $scope.add = function(project) {
		$http({
			url: "project/createProject",
			method: "POST",
			params: {name: project.name, description: project.description, visibility: "PUBLIC"},
		}).success(function(data) {
			$scope.loadProjects(data);			
		});
	};
	
	//Update project
	$scope.edit = function() {
		var ids = [];
		
		for (var idx=0; idx<$scope.project.labs.length;idx++) {
			ids.push($scope.project.labs[idx].idLab);
		}
		
		var buildId = -1;
		if ($scope.project.organismBuild != null) {
			buildId = $scope.project.organismBuild.idOrganismBuild;
		}
		
		$http({
			url: "project/updateProject",
			method: "POST",
			params: {name: $scope.project.name, description: $scope.project.description,
				idLab: ids, idOrganismBuild: buildId, idProject: $scope.projectId,
				visibility: $scope.project.visibility},
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
		});
		
    };
    
    //Select project
    $scope.select = function(id) {
    	$scope.projectId = id;
    	$scope.sample = {sampleType: null};
    };
    
    //set active project when projectId changes.
    $scope.$watch("projectId",function() {
    	$scope.project = {};
    	for (var i in $scope.projects) {
    		if($scope.projects[i].idProject == $scope.projectId) {
            	$scope.projects[i].cssClass = "current-project";
            	$scope.project = $scope.projects[i];
            	$scope.samples = $scope.projects[i].samples;
            	$scope.datatracks = $scope.projects[i].datatracks;
            	
            	//Replace project labs with objects in labList.  Track-by doesn't appear to work in chosen widgets...
            	var ids = [];
            	for (var idx=0; idx< $scope.project.labs.length; idx++) {
            		ids.push($scope.project.labs[idx].idLab);
            	}
            	
            	var labs = [];
            	for (var idx=0; idx< $scope.labList.length; idx++) {
            		if (ids.indexOf($scope.labList[idx].idLab) != -1) {
            			labs.push($scope.labList[idx]);
            		}
            	}

            	$scope.project.labs = labs;
            	
            } else {
            	$scope.projects[i].cssClass = "";
            }
    	}
    });
    
    //delete project
    $scope.deleteProject = function() {
    	if ($scope.idProject != -1) {
    		$http({
    			url: "project/deleteProject",
    			method: "POST",
    			params: {idProject: $scope.projectId},
        	}).success(function(data) {
        		$scope.loadProjects(-1);
        	});
    	}
    	
    };
    
    //clear out selected project
    $scope.refresh = function() {
    	$scope.projectId = -1;
    	$scope.sample = {sampleType: null};
    };
    
    
	//New Analysis Project Dialog
	$scope.openNewProjectWindow = function () {
	    var modalInstance = $modal.open({
	      templateUrl: 'app/submit/newProjectWindow.html',
	      controller: 'ProjectWindowController',
	    });

	    modalInstance.result.then(function (project) {
	    	$scope.add(project);
	    }, function () {
	    	// When dialog dismissed
	    });
	};


	/**********************
	* Sample management
	*********************/

	$scope.editSample = function(sample) {
		$scope.sample = sample;
		$scope.sampleEditMode = true;
    };
	
	$scope.saveSample = function(sample) {
		$scope.sampleEditMode = false;
		
		$http({
			url: "project/updateSample",
			method: "POST",
			params: {idProject: $scope.projectId, name: sample.name, idSampleType: sample.sampleType.idSampleType,
				idSamplePrep: sample.samplePrep.idSamplePrep, idSampleSource: sample.sampleSource.idSampleSource, 
				idSampleCondition: sample.sampleCondition.idSampleCondition, idSample: sample.idSample},
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
			$scope.sample = {};
		}).error(function(data, status, headers, config) {
			console.log("Could not update sample.");
		});
	};
	
	$scope.removeSample = function(sample) {
		$http({
			url: "project/deleteSample",
			method: "POST",
			params: {idSample: sample.idSample},
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
		}).error(function(data, status, headers, config) {
			console.log("Could not delete sample.");
		});
    };
	
	$scope.addSample = function(sample) {
		$http({
			url: "project/createSample",
			method: "POST",
			params: {idProject: $scope.projectId, name: sample.name, idSampleType: sample.sampleType.idSampleType,
				idSamplePrep: sample.samplePrep.idSamplePrep, idSampleSource: sample.sampleSource.idSampleSource, 
				idSampleCondition: sample.sampleCondition.idSampleCondition},
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
		}).error(function(data, status, headers, config) {
			console.log("Could not create sample.");
		});
		
		
	};
	
	
//	$scope.duplicateSample = function(sample) {
//		var newSample = {};
//		newSample.idSample = ++$scope.nextIdSample;
//		newSample.name = sample.name;
//		newSample.idSampleType = sample.idSampleType;
//		newSample.idSite = sample.idSite;
//		newSample.idSampleGroup = sample.idSampleGroup;
//
//		$scope.samples.push(newSample);
//	};
	
	
	/**********************
	 * Datatrack management
	 *********************/
	
	$scope.editDataTrack = function(datatrack) {
		$scope.datatrack = datatrack;
		$scope.datatrackEditMode = true;
    };
	
	$scope.saveDataTrack = function(datatrack) {
		$http({
			url : "project/updateDataTrack",
			method: "POST",
			params: {idProject: $scope.projectId, name: datatrack.name, url: datatrack.url, idDataTrack: datatrack.idDataTrack},
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
		}).error(function(data) {
			console.log("Could not update datatrack");
		});
		
		$scope.datatrackEditMode = false;
		$scope.datatrack = {};
	};
	
	$scope.removeDataTrack = function(datatrack) {
		$http({
			url : "project/deleteDataTrack",
			method: "POST",
			params: {idDataTrack: datatrack.idDataTrack},
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
		}).error(function(data) {
			console.log("Could not delete datatrack");
		});
    };
	
	$scope.addDataTrack = function(datatrack) {
		$http({
			url : "project/createDataTrack",
			method: "POST",
			params: {idProject: $scope.projectId, name: datatrack.name, url: datatrack.url},
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
		}).error(function(data) {
			console.log("Could not create datatrack");
		});
		
		$scope.datatrack = {};
	}; 
	
//	$scope.duplicateDataTrack = function(datatrack) {
//		var newDataTrack = {};
//		newDataTrack.idDataTrack = ++$scope.nextIdDataTrack;
//		newDataTrack.name = datatrack.name;
//		newDataTrack.url = datatrack.url;
//		
//		
//		$scope.addDataTrack(datatrack);
//	};
	
	
	//
	// Results
	//

	$scope.editResult = function(result) {
		$scope.result = result;
		$scope.resultEditMode = true;
		
		for (var x in $scope.samples) {
			var isFound = false;
			for (var y in $scope.result.samples) {
				if ($scope.samples[x].idSample == $scope.result.samples[y].idSample) {
					isFound = true;
					break;
				}
			}
			$scope.samples[x].isChecked = isFound;
		}
		for (var x in $scope.datatracks) {
			var isFound = false;
			for (var y in $scope.result.datatracks) {
				if ($scope.datatracks[x].idDataTrack == $scope.result.datatracks[y].idDataTrack) {
					isFound = true;
					break;
				}
			}
			$scope.datatracks[x].isChecked = isFound;
		}
		
    };
	
	$scope.saveResult = function(result) {
		$scope.resultEditMode = false;
		
		var newResult = {};
		$scope.result = newResult;
		$scope.clearResult();
	};
	
	$scope.removeResult = function(id) {
        for(var i in $scope.results) {
            if($scope.results[i].idResult == id) {
            	$scope.results.splice(i, 1);
            	break;
            }
        }
       
    };
	
	$scope.addResult = function(result) {
		var newResult = {};
		newResult.idResult = ++$scope.nextIdResult;
		newResult.name = result.name;
		newResult.description = result.description;
		newResult.idAnalysisType = result.idAnalysisType;
		newResult.date = result.date;
		newResult.samples = [];
		for (var x in $scope.samples) {
			if ($scope.samples[x].isChecked) {
				var newSample = {};
				newSample.idSample = $scope.samples[x].idSample;
				newSample.name = $scope.samples[x].name;
				newResult.samples.push(newSample);
			}
		}
		newResult.datatracks = [];
		for (var x in $scope.datatracks) {
			if ($scope.datatracks[x].isChecked) {
				var dt = {};
				dt.idDataTrack = $scope.datatracks[x].idDataTrack;
				dt.name = $scope.datatracks[x].name;
				newResult.datatracks.push(dt);
			}
		}
		
		$scope.results.push(newResult);
		
		$scope.clearResult();
	};
	
	$scope.clearResult = function() {
		$scope.result.idResult = -1;
		$scope.result.name = "";
		$scope.result.description = "";
		$scope.result.idAnalysisType = "";
		$scope.result.date = "";
		$scope.result.samples = [];
		$scope.result.datatracks = [];
		
		for (var x in $scope.samples) {
			$scope.samples[x].isChecked = false;
		}
		for (var x in $scope.datatracks) {
			$scope.datatracks[x].isChecked = false;
		}

		
	};
	
	$scope.duplicateResult = function(result) {
		var newResult = {};
		newResult.idResult = ++$scope.nextIdResult;
		newResult.name = result.name;
		newResult.description = result.description;
		newResult.idAnalysisType = result.idAnalysisType;
		newResult.date = result.date;
		
		$scope.results.push(newResult);
	};
	
	
	
	
	
	
}]);





