'use strict';

/**
 * SubmitController
 * @constructor
 */
 
var submit    = angular.module('submit', ['ui.bootstrap','filters', 'services', 'directives','chosen','error','ngProgress']);

angular.module("submit").controller("SubmitController", [
'$scope', '$http', '$modal','DynamicDictionary','StaticDictionary','$rootScope','$upload','$q','ngProgress',
function($scope, $http, $modal, DynamicDictionary, StaticDictionary,$rootScope,$upload,$q,ngProgress) {
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
    $scope.projectEditMode = false;
    
    $scope.complete = 0;
    
    $rootScope.helpMessage = "<p>Placeholder for data submission help.</p>";
    
	
	//Static dictionaries. These http calls are cached.
    $scope.loadOrganismBuildList = function () {
    	StaticDictionary.getOrganismBuildList().success(function(data) {
    		$scope.organismBuildList = data;
    	});
    };
    
    $scope.loadAnalysisTypeList = function () {
    	StaticDictionary.getAnalysisTypeList().success(function(data) {
    		$scope.analysisTypeList = data;
    	});
    };
    
    $scope.loadSampleTypeList = function () {
    	StaticDictionary.getSampleTypeList().success(function(data) {
    		$scope.sampleTypeList = data;
    	});
    };
    
    $scope.loadOrganismBuildList = function () {
    	StaticDictionary.getOrganismBuildList().success(function(data) {
    		$scope.organismBuildList = data;
    	});
    };
    
     
    //Dynamic dictionaries.  These http calls aren't cached.
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
  
    //Load up all dictionaries
	$scope.loadSampleConditions();
	$scope.loadSampleSources();
	$scope.loadSamplePreps();
	$scope.loadOrganismBuildList();
	$scope.loadAnalysisTypeList();
	$scope.loadSampleTypeList();
	$scope.loadOrganismBuildList();
    
    //Watchers
    $scope.$watch('sample.sampleType',function() {
    	if ($scope.sample.sampleType == null) {
    		$scope.samplePrepList = null;
    	} else {
    		$scope.loadSamplePrepsBySampleType();
    	}
    });
    
    $rootScope.$watch('loggedUser', function() {
    	if ($rootScope.loggedUser != null) {
    		$scope.labList = $rootScope.loggedUser.labs;
        	$scope.instituteList = $rootScope.loggedUser.institutes;
    	}
    	
    });
    
    /**********************
	 * Project management
	 *********************/
    
    $scope.loadProjects = function(projectId) {
		$http({
			url: "project/getProjectsByVisibility",
			method: "GET",
			params: {projectId: projectId},
		}).success(function(data, status, headers, config) {
			$scope.projectId = config.params.projectId;
			$scope.projects = data;
			$scope.setActiveProject();
		});
    	
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
    	
    	var lids = [];
		var iids = [];
		
		for (var idx=0; idx<project.labs.length;idx++) {
			lids.push(project.labs[idx].idLab);
		}
		
		for (var idx=0; idx<project.institutes.length;idx++) {
			iids.push(project.institutes[idx].idInstitute);
		}
		
		var buildId = -1;
		if (project.organismBuild != null) {
			buildId = project.organismBuild.idOrganismBuild;
		}
    	
		$http({
			url: "project/createProject",
			method: "PUT",
			params: {name: project.name, description: project.description, idLab: lids, idOrganismBuild: buildId,
				idInstitute: iids, visibility: "PUBLIC"},
		}).success(function(data) {
			$scope.loadProjects(data);
			$scope.editedProject = {};
		});
	};
	
	$scope.edit = function() {
		$scope.editedProject = angular.copy($scope.project);
		$scope.projectEditMode = true;
	};
	
	$scope.cancel = function() {
		$scope.editedProject = {};
    	$scope.projectEditMode = false;
    };
	
	//Update project
	$scope.save = function() {
		$scope.project = $scope.editedProject;
		var lids = [];
		var iids = [];
		
		for (var idx=0; idx<$scope.project.labs.length;idx++) {
			lids.push($scope.project.labs[idx].idLab);
		}
		
		for (var idx=0; idx<$scope.project.institutes.length;idx++) {
			iids.push($scope.project.institutes[idx].idInstitute);
		}
		
		var buildId = -1;
		if ($scope.project.organismBuild != null) {
			buildId = $scope.project.organismBuild.idOrganismBuild;
		}
		
		$http({
			url: "project/updateProject",
			method: "PUT",
			params: {name: $scope.project.name, description: $scope.project.description,
				idLab: lids, idOrganismBuild: buildId, idProject: $scope.projectId,
				visibility: $scope.project.visibility, idInstitute: iids, dataUrls: $scope.project.dataUrls},
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
		});
		
		$scope.projectEditMode = false;
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
    	if ($scope.results.length > 0 || $scope.samples.length > 0 || $scope.datatracks.length > 0 || $scope.files.uploadedFiles.length > 0 || 
    			$scope.files.importedFiles.length > 0) {
    		$scope.showErrorMessage("Can't Delete Selected Project","You must delete any samples, datatracks or analyses before deleting project");
    	} else if ($scope.idProject != -1) {
    		$http({
    			url: "project/deleteProject",
    			method: "DELETE",
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
	      
	      resolve: {
  			labList: function() {
  				return $scope.labList;
  			},
  			instList: function() {
  				return $scope.instituteList;
  			},
  			organismBuildList: function() {
  				return $scope.organismBuildList;
  			}
  		  }
	      
	      
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
		$scope.sampleEditMode = false;
	};

	$scope.editSample = function(sample) {
		$scope.sample = angular.copy(sample);
		$scope.sampleEditMode = true;
		
		console.log($scope.samplePrepList);
		console.log($scope.samplePrepListAll);
    };
	
	$scope.saveSample = function(sample) {
		$scope.sampleEditMode = false;
		
		$http({
			url: "project/updateSample",
			method: "PUT",
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
			method: "DELETE",
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
			method: "PUT",
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
		$scope.datatrackEditMode = false;
	};
	
	$scope.editDataTrack = function(datatrack) {
		$scope.datatrack = angular.copy(datatrack);
		$scope.datatrackEditMode = true;
    };
	
	$scope.saveDataTrack = function(datatrack) {
		var deferred = $q.defer();
		var promise = deferred.promise;
		
		if ($scope.datatrack.file != null) {
			promise = $scope.uploadDatatrack($scope.datatrack.file, promise);
		}
		
		promise = promise.then(function(data) {
			return $http({
				url : "project/updateDataTrack",
				method: "PUT",
				params: {idProject: $scope.projectId, name: datatrack.name, path: datatrack.path, idDataTrack: datatrack.idDataTrack},
			}).success(function(data) {
				$scope.loadProjects($scope.projectId);
				$scope.datatrackEditMode = false;
				$scope.datatrack = {};
			}).error(function(data) {
				console.log("Could not update datatrack");
			});
		});
		
		deferred.resolve();
	};
	
	
	$scope.addDataTrackFile = function(files) {
		$scope.datatrack.file = files[0];
		$scope.datatrack.path = files[0].name;
	};
	
	$scope.uploadDatatrack = function(file, promise) {
		$scope.complete = 0;
		var max = 100000000;
	
		var fileChunks = [];
		
		if (file.size > max) {
			for (var i=0;i<file.size;i+=max)
			fileChunks.push(file.slice(i,i+max));
		}
		
		
		ngProgress.start();
		
		
		var loaded = 0;
		for (var i=0; i<fileChunks.length; i++) {
			(function(i) {
				promise = promise.then(function() {
					return $upload.upload({
						url: "project/addDataTrackFile",
						file: fileChunks[i],
						params : {index: i, total: fileChunks.length, name: file.name, idProject: $scope.project.idProject},
					}).progress(function(evt) {
						$scope.complete = (loaded + evt.loaded) / file.size * 100;
					}).success(function(data) {
						loaded += fileChunks[i].size;
						if (data.finished) {
							$scope.datatrack.path = data.directory;
						}
						$scope.complete = (loaded) / file.size * 100;
						ngProgress.complete();
					}).error(function(data) {
						$scope.datatrack.message = data.message;
						$scope.complete = 0;
						ngProgress.reset();
					});
				});
			})(i);
		}
		return promise;
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
			method: "DELETE",
			params: {idDataTrack: datatrack.idDataTrack},
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
		}).error(function(data) {
			console.log("Could not delete datatrack");
		});
    };
    
    $scope.showDataTrackError = function(datatrack) {
    	$scope.showErrorMessage("Error uploading datatracks", datatrack.message);
    };
	
	$scope.addDataTrack = function(datatrack) {
		var deferred = $q.defer();
		var promise = deferred.promise;
		
		promise = $scope.uploadDatatrack(datatrack.file, promise);
		
		promise = promise.then(function() {
			return  $http({
				url : "project/createDataTrack",
				method: "PUT",
				params: {idProject: $scope.projectId, name: datatrack.name, path: datatrack.path},
			}).success(function(data) {
				$scope.loadProjects($scope.projectId);
				$scope.datatrack = {};
			}).error(function(data) {
				console.log("Could not create datatrack");
			});
		});
		deferred.resolve();
	}; 
	
	
	
	/**********************
	 * Analysis Management
	 *********************/
	
	$scope.clearResult = function() {
		$scope.result = {};
		$scope.resultEditMode = false;
	};

	$scope.editResult = function(result) {
		$scope.result = angular.copy(result);
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
			method: "PUT",
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
			method: "DELETE",
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
			method: "PUT",
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
			method: "PUT",
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
			method: "PUT",
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
			method: "PUT",
			params: {condition: $scope.newSampleCond.cond},
		}).success(function(data) {
			$scope.newSampleCond.cond = "";
			$scope.loadSampleConditions();
			$scope.sample.sampleCondition = data;
		});
	};
	
	$scope.$watch("result.analysisType",function() {
		if ($scope.result.analysisType != null) {
			for (var i=0; i<$scope.files.importedFiles.length; i++) {
				if ($scope.files.importedFiles[i].analysisType.idAnalysisType == $scope.result.analysisType.idAnalysisType) {
					$scope.files.importedFiles[i].doesAnalysisMatch = true;
				} else {
					$scope.files.importedFiles[i].doesAnalysisMatch = false;
				}
			}
		}
		
		if ($scope.result.file != null &&  $scope.result.analysisType != null) {
			if ($scope.result.file.analysisType.idAnalysisType != $scope.result.analysisType.idAnalysisType) {
				$scope.result.file = null;
			}
		}
	});
	
	/****************
	 * Hide/Show controls
	 */
	
	$scope.hideSampleControls = function(sample) {
		sample.show = !sample.show;
	};
	
	$scope.hideResultControls = function(result) {
		result.show = !result.show;
	};
	
	$scope.hideDatatrackControls = function(datatrack) {
		datatrack.show = !datatrack.show;
	};
	
	$scope.datepicker = {opened: false};
	$scope.open = function($event) {
		$event.preventDefault();
	    $event.stopPropagation();
	    $scope.datepicker.opened = true;
	};

	
}]);





