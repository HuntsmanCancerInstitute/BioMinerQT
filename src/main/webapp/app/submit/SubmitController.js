'use strict';

/**
 * SubmitController
 * @constructor
 */
 
var submit    = angular.module('submit', ['ui.bootstrap','filters', 'services', 'directives','chosen']);

angular.module("submit").controller("SubmitController", [
'$scope', '$http', '$modal','DynamicDictionary','StaticDictionary','$rootScope',
function($scope, $http, $modal, DynamicDictionary, StaticDictionary,$rootScope) {
	/**********************
	 * Initialization!
	 *********************/
	
	//enums!
	$scope.projectVisibilities = [{enum: "LAB",name: "Lab"},{enum: "INSTITUTE", name: "Institute"},{enum: "PUBLIC", name: "Public"}];
	
	//current project
	$scope.projectId = -1;
	
	
	//containers
	$scope.files = {uploadedFiles: [], importedFiles: [], projectFiles: []};
    $scope.samples = [];
    $scope.datatracks = [];
    $scope.results = [];
    $scope.samplePrepList = [];
    $scope.samplePrepListAll = [];

    //new dictionary entries
    
    $scope.newSamplePrep = { description : "" };
    $scope.newSampleCond = { condition : "" };
    $scope.newSampleSource = { source : "" };
    
    //active
    $scope.sample = {sampleType: null};
    $scope.datatrack = {};
    $scope.result = {};
    $scope.project = {};
    
    //flags
    $scope.sampleEditMode = false;
    $scope.datatrackEditMode = false;
    $scope.resultEditMode = false;
	
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
	
//	StaticDictionary.samplePrepList(function(data) {
//		$scope.samplePrepListAll = data;
//	});
	
	
    
    //Dynamic dictionaries.  These dictionaries can be loaded on-demand.
    $scope.loadLabs = function() {
    	DynamicDictionary.loadLabs().success(function(data) {
    		$scope.labList = data;
    	});
    };
    
    $scope.loadSampleSources = function() {
    	DynamicDictionary.loadSampleSources().success(function(data) {
    		$scope.sampleSourceList = data;
    		var addNew = {source: "Add New", idSampleSource: -1};
    		$scope.sampleSourceList.unshift(addNew);
    	});
    };
    
    $scope.loadSampleConditions = function() {
    	DynamicDictionary.loadSampleConditions().success(function(data) {
    		$scope.sampleConditionList = data;
    		var addNew = {cond: "Add New", idSampleCondition: -1};
    		$scope.sampleConditionList.unshift(addNew);
    	});
    };
    
    $scope.loadSamplePreps = function() {
    	DynamicDictionary.loadSamplePreps().success(function(data) {
    		$scope.samplePrepListAll = data;
    	});
    };
    
    $scope.loadSamplePrepsBySampleType = function() {
    	DynamicDictionary.loadSamplePrepsBySampleType($scope.sample.sampleType.idSampleType).success(function(data) {
			$scope.samplePrepList = data;
			var addNew = {description : "Add New", idSamplePrep: -1};
			$scope.samplePrepList.unshift(addNew);
		});
    };
  
    //Load up dynamic dictionaries
	$scope.loadLabs();
	$scope.loadSampleConditions();
	$scope.loadSampleSources();
	$scope.loadSamplePreps();
    
    //Watchers
    $scope.$watch('sample.sampleType',function() {
    	if ($scope.sample.sampleType == null) {
    		$scope.samplePrepList = null;
    	} else {
    		$scope.loadSamplePrepsBySampleType();
    	}
    });
    
    /**********************
	 * Project management
	 *********************/
    
    //load projects (currently all, will be tied to user going forward)
//    $scope.loadProjects = function(projectId) {
//    	$http({
//			url: "project/getAllProjects",
//			method: "POST",
//			params: {projectId: projectId},
//		}).success(function(data, status, headers, config) {
//			$scope.projectId = config.params.projectId;
//			$scope.projects = data;
//			
//			$scope.setActiveProject();
//			
//		});
//    };
    
    $scope.loadProjects = function(projectId) {
    	if ($rootScope.loggedUser == null) {
    		$http({
    			url: "project/getPublicProjects",
    			method: "POST",
    			params: {projectId: projectId},
    		}).success(function(data, status, headers, config) {
    			$scope.projectId = config.params.projectId;
    			$scope.projects = data;
    			$scope.setActiveProject();
    		});
    	} else {
    		$http({
    			url: "project/getProjectsByVisibility",
    			method: "POST",
    			params: {projectId: projectId, idUser: $rootScope.loggedUser.idUser},
    		}).success(function(data, status, headers, config) {
    			$scope.projectId = config.params.projectId;
    			$scope.projects = data;
    			$scope.setActiveProject();
    		});
    	}
    };
    
    
    $scope.setActiveProject = function() {
    	$scope.project = {};
    	for (var i in $scope.projects) {
    		if($scope.projects[i].idProject == $scope.projectId) {
            	$scope.projects[i].cssClass = "current-project";
            	$scope.project = $scope.projects[i];
            	$scope.samples = $scope.projects[i].samples;
            	$scope.datatracks = $scope.projects[i].dataTracks;
            	$scope.results = $scope.projects[i].analyses;
            	$scope.files.importedFiles = [];
            	$scope.files.uploadedFiles = [];
            	for (var x in $scope.projects[i].files) {
            		if ($scope.projects[i].files[x].type == "IMPORTED") {
            			$scope.files.importedFiles.push($scope.projects[i].files[x]);
            		} else if ($scope.projects[i].files[x].type == "UPLOADED") {
            			$scope.files.uploadedFiles.push($scope.projects[i].files[x]);
            		}
            	}	
            } else {
            	$scope.projects[i].cssClass = "";
            }
    	}
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
    	$scope.datatrack = {};
    	$scope.setActiveProject();
    };
    
 
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
	
	$scope.clearSample = function() {
		$scope.sample = {};
	};

	$scope.editSample = function(sample) {
		$scope.sample = sample;
		$scope.sampleEditMode = true;
		
		console.log($scope.samplePrepList);
		console.log($scope.samplePrepListAll);
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
		$scope.sample = {};
	};
	
	$scope.removeSample = function(sample) {
		//Don't remove samples that are associated with analyses.
		
		//Keeping it a list in case we move over to checkboxes
		var fileList = [];
		fileList.push(sample);
		
		if (sample.analysisSet) {
			var message = "";
			message += "<p>The following samples are associated with existing analyses and can't be deleted. Please delete the appropriate analyses and try again.</p>";
			message += "<br/>";
			message += "<ul>";
			for (var i=0;i<fileList.length;i++) {
				message += "<li>" + sample.name + "</li>";
			}
			message += "</ul>";
		
			$scope.showErrorMessage("Can't Delete Selected Samples",message);
			return;
		}
		
		
		$http({
			url: "project/deleteSample",
			method: "POST",
			params: {idSample: sample.idSample},
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
		}).error(function(data, status, headers, config) {
			console.log("Could not delete sample.");
		});
		$scope.sample = {};
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
		$scope.sample = {};
		
	};
	
	
	/**********************
	 * Datatrack management
	 *********************/
	$scope.clearDataTrack = function() {
		$scope.datatrack = {};
	};
	
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
		//Keeping it a list in case we move over to checkboxes
		var fileList = [];
		fileList.push(datatrack);
		
		
		if (datatrack.analysisSet) {
			var message = "";
			message += "<p>The following Datatracks are associated with existing analyses and can't be deleted. Please delete the appropriate analyses and try again.</p>";
			message += "<br/>";
			message += "<ul>";
			for (var i=0;i<fileList.length;i++) {
				message += "<li>" + datatrack.name + "</li>";
			}
			message += "</ul>";
		
			$scope.showErrorMessage("Can't Delete Selected Datatracks",message);
			return;
		}
		
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
	
	
	
	/**********************
	 * Analysis Management
	 *********************/
	
	$scope.clearResult = function() {
		$scope.result = {};
	};

	$scope.editResult = function(result) {
		$scope.result = result;
		$scope.resultEditMode = true;
    };
	
	$scope.saveResult = function(result) {
		$scope.resultEditMode = false;
		
		var sampleList = [];
		for (var x in result.samples) {
			sampleList.push(result.samples[x].idSample);
		}
		
		var dataTrackList = [];
		for (var x in result.dataTracks) {
			dataTrackList.push(result.dataTracks[x].idDataTrack);
		}
		
		$http({
			url: "project/updateAnalysis",
			method: "POST",
			params: {idAnalysis: result.idAnalysis, name: result.name, description: result.description, date: result.date, idProject: $scope.projectId, 
				idSampleList: sampleList, idDataTrackList: dataTrackList, idFileUpload: result.file.idFileUpload, idAnalysisType: result.analysisType.idAnalysisType}
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
			$scope.result = {};
		}).error(function(data) {
			console.log("Could not update analysis.");
		});
		$scope.result = {};
	};
	
	$scope.removeResult = function(result) {
		$http({
			url: "project/deleteAnalysis",
			method: "POST",
			params: {idAnalysis: result.idAnalysis}
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
		}).error(function(data) {
			console.log("Error deleting analysis");
		});
		$scope.result = {};
    };
	
	$scope.addResult = function(result) {
		var sampleList = [];
		for (var x in result.samples) {
			sampleList.push(result.samples[x].idSample);
		}
		
		var dataTrackList = [];
		for (var x in result.dataTracks) {
			dataTrackList.push(result.dataTracks[x].idDataTrack);
		}
		
		$http({
			url: "project/createAnalysis",
			method: "POST",
			params: {name: result.name, description: result.description, date: result.date.getTime(), idProject: $scope.projectId, 
				idSampleList: sampleList, idDataTrackList: dataTrackList, idFileUpload: result.file.idFileUpload, idAnalysisType: result.analysisType.idAnalysisType}
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
			$scope.result = {};
		}).error(function(data) {
			console.log("Could not create analysis.");
		});
	};
	
	
	$scope.showErrorMessage = function(title,message) {
		$modal.open({
    		templateUrl: 'app/common/error.html',
    		controller: 'ErrorController',
    		resolve: {
    			title: function() {
    				return title;
    			},
    			message: function() {
    				return message;
    			}
    		}
    	});
	};
	
	
	$scope.testError = function() {
		$http({
			url : "project/testError",
			method : "POST",
		}).success(function(data) {
			
		});
	};
	
	$scope.addNewPrep = function() {
		if ($scope.newSamplePrep.description == null || $scope.newSamplePrep.description == "") {
			return;
		}
		
		var found = false;
		for (var i=0; i < $scope.samplePrepList.length; i++) {
			if ($scope.samplePrepList[i].description == $scope.newSamplePrep.description) {
				found = true;
			}
		}
		
		if (found) {
			var message = "<p>The specified sample prep " + $scope.newSamplePrep.description + " already exists!</p>";
			$scope.showErrorMessage("Duplicate sample prep",message);
			return;
		}
		
		$http({
			url: "project/addSamplePrep",
			method: "POST",
			params: {description: $scope.newSamplePrep.description, idSampleType: $scope.sample.sampleType.idSampleType},
		}).success(function(data) {
			$scope.newSamplePrep.description = "";
			$scope.loadSamplePreps();
			$scope.loadSamplePrepsBySampleType();
			$scope.sample.samplePrep = data;
		});
	};
	
	$scope.addNewSource = function() {
		if ($scope.newSampleSource.source == null || $scope.newSampleSource.source == "") {
			return;
		}
		
		var found = false;
		for (var i=0; i < $scope.sampleSourceList.length; i++) {
			if ($scope.sampleSourceList[i].source == $scope.newSampleSource.source) {
				found = true;
			}
		}
		
		if (found) {
			var message = "<p>The specified sample souce " + $scope.newSampleSource.source + " already exists!</p>";
			$scope.showErrorMessage("Duplicate sample source",message);
			return;
		}
		
		
		$http({
			url: "project/addSampleSource",
			method: "POST",
			params: {source: $scope.newSampleSource.source},
		}).success(function(data) {
			$scope.newSampleSource.source = "";
			$scope.loadSampleSources();
			$scope.sample.sampleSource = data;
		});
	};
	
	$scope.addNewCondition = function() {
		if ($scope.newSampleCond.cond == null || $scope.newSampleCond.cond == "") {
			return;
		}
		
		var found = false;
		for (var i=0; i < $scope.sampleConditionList.length; i++) {
			if ($scope.sampleConditionList[i].cond == $scope.newSampleCond.cond) {
				found = true;
			}
		}
		
		if (found) {
			var message = "<p>The specified sample condition " + $scope.newSampleCond.cond + " already exists!</p>";
			$scope.showErrorMessage("Duplicate sample condition",message);
			return;
		}
		
		$http({
			url: "project/addSampleCondition",
			method: "POST",
			params: {condition: $scope.newSampleCond.cond},
		}).success(function(data) {
			$scope.newSampleCond.cond = "";
			$scope.loadSampleConditions();
			$scope.sample.sampleCondition = data;
		});
	};
	

	
}]);





