'use strict';

/**
 * SubmitController
 * @constructor
 */
 
var submit    = angular.module('submit', ['ui.bootstrap','ui.validate','filters', 'services', 'directives','chosen','ngProgress','dialogs.main','error','cgBusy']);

angular.module("submit").controller("SubmitController", [
'$scope', '$http', '$modal','DynamicDictionary','StaticDictionary','$rootScope','$upload','$q','ngProgress','dialogs','$anchorScroll','$location','$timeout',
function($scope, $http, $modal, DynamicDictionary, StaticDictionary,$rootScope,$upload,$q,ngProgress, dialogs, $anchorScroll, $location, $timeout) {
	/**********************
	 * Initialization!
	 *********************/
    $scope.disableElement = false;
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
    $scope.result.analysisType = null;
    $scope.result.date = new Date();
    $scope.project = {};
    $scope.lastSample = null;
    $scope.lastResult = null;
    
    //name checks
    $scope.originalResultName;
    $scope.originalSampleName;
       
    //flags
    $scope.sampleEditMode = true;
    $scope.datatrackEditMode = false;
    $scope.resultEditMode = false;
    $scope.projectEditMode = false;
    
    $scope.samplePrepUsed = true;
    $scope.sampleConditionUsed = true;
    $scope.sampleSourceUsed = true;
    
    $scope.dtcomplete = 0;
    $scope.totalGlobalSize = 0;
	$scope.currGlobalSize = 0;
	
	//datatracks
	$scope.dataTracksUploading = false;
	$scope.dataTrackUploadPromise = null;
	$scope.dataTrackUploadDefer = null;
	
	$scope.navigationOk = false;
	
	//holds valid analyses
	$scope.validFiles = [];
    
	//Static dictionaries. These http calls are cached.
   
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

    //Dynamic dictionaries.  These http calls aren't cached.
    $scope.loadOrganismBuildList = function() {
    	DynamicDictionary.loadOrganismBuilds().success(function(data) {
    		$scope.organismBuildList = data;
    	});
    }
    
    $scope.loadSampleSources = function() {
    	DynamicDictionary.loadSampleSources().success(function(data) {
    		
    		$scope.sampleSourceList = data;
    		var addNew = {source: "Add New", idSampleSource: -1, idOrganismBuild: null, first: true};
    		$scope.sampleSourceList.unshift(addNew);
    	});
    };
    
    $scope.loadSampleConditions = function() {
    	DynamicDictionary.loadSampleConditions().success(function(data) {
    		$scope.sampleConditionList = data;
    		var addNew = {cond: "Add New", idSampleCondition: -1, idOrganismBuild: null, first: true};
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
    
    $scope.organismCanChange = function() {
    	if ($scope.files.importedFiles.length > 0) {
    		return false;
    	}
    }
  
    //Load up all dictionaries
	$scope.loadSampleConditions();
	$scope.loadSampleSources();
	$scope.loadSamplePreps();
	$scope.loadOrganismBuildList();
	$scope.loadAnalysisTypeList();
	$scope.loadSampleTypeList();
	$scope.loadOrganismBuildList();
	
	/********************* 
	 * 
	 * Filters
	 * 
	 ********************/
	
	$scope.filterSampleSource = function() {
		return function(source) {
			if (Object.keys($scope.project).length != 0) {
				return source.idOrganismBuild == null || source.idOrganismBuild == $scope.project.organismBuild.idOrganismBuild;
			} else {
				return true;
			}	
		}
	}
	
	$scope.filterSampleCondition = function() {
		return function(cond) {
			if (Object.keys($scope.project).length != 0) {
				return cond.idOrganismBuild == null || cond.idOrganismBuild == $scope.project.organismBuild.idOrganismBuild;
			} else {
				return true;
			}
		}
	}
    
    
	/********************* 
	 * 
	 * Watchers
	 * 
	 ********************/
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
    
    $scope.$watch("files.importedFiles",function() {
    	$scope.checkFileStatus();
    });
    
    $scope.$watch("result.analysisType",function() {
		if ($scope.result.analysisType != null) {
			for (var i=0; i<$scope.files.importedFiles.length; i++) {
				if ($scope.files.importedFiles[i].analysisType != null && $scope.files.importedFiles[i].analysisType.idAnalysisType == $scope.result.analysisType.idAnalysisType) {
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
		
		$scope.checkFileStatus();
	});
    

    $scope.checkFileStatus = function() {
    	$scope.validFiles = [];
    	for (var i=0; i<$scope.files.importedFiles.length;i++) {
    		var file = $scope.files.importedFiles[i];
    		if (file.isAnalysisSet || !file.doesAnalysisMatch) {
    			if ($scope.result.file != undefined && file.idFileUpload == $scope.result.file.idFileUpload) {
    				$scope.validFiles.push(file);
    			} else {
    				
    			}
    		} else {
    			$scope.validFiles.push(file);
    		}
    	}
    };
    
    /*********************
     * 
     * 
     * Catch Interrupts
     * 
     * 
     *********************/
    
    $scope.$on('$locationChangeStart', function( event, next, current ) {
    	if ($scope.dataTracksUploading) {
    		event.preventDefault();
    		var dialog = dialogs.confirm("Page Navigation","Datatrack upload in progress, are you sure you want to leave this page?  All incomplete uploads will be removed from the database.");
        	dialog.result.then(function() {
        		$timeout(function() {
        			$scope.stopDataTrackUpload();
        			$scope.cleanDataTracks();
        			$location.path(next.substring($location.absUrl().length - $location.url().length));
                    $scope.$apply();
        		});
        		$scope.navigationOk = true;
        	});
    	} else {
    		$scope.stopDataTrackUpload();
    		$scope.cleanDataTracks();
    	}
    });
    
    $scope.$on('$routeChangeStart', function (event, next, current) {
    	
    });
    
    window.onbeforeunload = function() {
    	$scope.stopDataTrackUpload();
    	$scope.cleanDataTracks();
    }
    
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
    	$scope.loadProjects($scope.projectId);
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
    		dialogs.error("Can't Delete Selected Project","All samples, datatracks, files and analyses associated with project must be deleted first.",null);
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
		$scope.originalSampleName = null;
	};

	$scope.editSample = function(sample) {
		$scope.sample = angular.copy(sample);
		$scope.originalSampleName = angular.copy(sample.name);
		$scope.sampleEditMode = true;
    };
    
    $scope.duplicateSample = function() {
    	$scope.sample = angular.copy($scope.lastSample);
    	$scope.originalSampleName = null;
    };
    
    $scope.copySample = function(sample) {
    	$scope.sample = angular.copy(sample);
		$scope.sampleEditMode = false;
		$scope.originalSampleName = null;
	}
	
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
			$scope.originalSampleName = null;
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
			dialogs.error("Can't delete selected samples.",message,null);
			return;
		}
		
		
		$http({
			url: "project/deleteSample",
			method: "DELETE",
			params: {idSample: sample.idSample},
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
			$scope.originalSampleName = null;
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
			$scope.lastSample = sample;
			$scope.samplePrepUsed = true;
			$scope.sampleConditionUsed = true;
			$scope.sampleSourceUsed = true;
			$scope.originalSampleName = null;
		}).error(function(data, status, headers, config) {
			console.log("Could not create sample.");
		});
		$scope.sample = {};
		
	};
	
	$scope.checkSampleName = function(name) {
		if ($scope.sampleEditMode && $scope.originalSampleName == name) {
			return true;
		}
		var retVal = true;
		for (var i=0; i<$scope.samples.length;i++) {
			if (name == $scope.samples[i].name) {
				retVal = false;
			}
		}
		return retVal;
	};
	
	
	$scope.checkSamplePrep = function(samplePrep) {
		if (samplePrep != null) {
			$http({
				url: "project/isSamplePrepUsed",
				method: "GET",
				params: {"description": samplePrep.description}
			}).success(function(data) {
				$scope.samplePrepUsed = data.found;
				
			});
		} else {
			$scope.samplePrepUsed = true;
		}
	};
	
	$scope.checkSampleSource = function(sampleSource) {
		if (sampleSource != null) {
			$http({
				url: "project/isSampleSourceUsed",
				method: "GET",
				params: {"source": sampleSource.source, idOrganismBuild: $scope.project.organismBuild.idOrganismBuild}
			}).success(function(data) {
				$scope.sampleSourceUsed = data.found;
				
			});
		} else {
			$scope.sampleSourceUsed = true;
		}
	};
	
	$scope.checkSampleCondition = function(sampleCondition) {
		if (sampleCondition != null) {
			$http({
				url: "project/isSampleConditionUsed",
				method: "GET",
				params: {"cond": sampleCondition.cond , idOrganismBuild: $scope.project.organismBuild.idOrganismBuild}
			}).success(function(data) {
				$scope.sampleConditionUsed = data.found;
				
			});
		} else {
			$scope.sampleConditionUsed = true;
		}
	};
	
	$scope.deleteSampleCondition = function(idSampleCondition) {
		$http({
			url: "project/deleteSampleCondition",
			method: "DELETE",
			params: {"idSampleCondition" : idSampleCondition},
		}).success(function(data) {
			$scope.loadSampleConditions();
			$scope.sample.sampleCondition = null;
			$scope.sampleConditionUsed = true;
		});
	};
	
	$scope.deleteSampleSource = function(idSampleSource) {
		$http({
			url: "project/deleteSampleSource",
			method: "DELETE",
			params: {"idSampleSource" : idSampleSource},
		}).success(function(data) {
			$scope.loadSampleSources();
			$scope.sample.sampleSource = null;
			$scope.sampleSourceUsed = true;
			
		});
	};
	
	$scope.deleteSamplePrep = function(idSamplePrep) {
		$http({
			url: "project/deleteSamplePrep",
			method: "DELETE",
			params: {"idSamplePrep": idSamplePrep},
		}).success(function(data) {
			$scope.loadSamplePreps();
			$scope.loadSamplePrepsBySampleType();
			$scope.sample.samplePrep = null;
			$scope.samplePrepUsed = true;
		});
	};
	

	
	
	/**********************
	 * 
	 * 
	 * Datatrack management
	 * 
	 * 
	 * 
	 * 
	 *********************/
	
	$scope.clearDataTrack = function() {
		$scope.datatrack = {};
		$scope.datatrackEditMode = false;
	};
	
	$scope.editDataTrack = function(datatrack) {
		$scope.datatrack = angular.copy(datatrack);
		$scope.datatrackEditMode = true;
		if ($scope.datatrack.state != "SUCCESS") {
			$scope.datatrack.path = null;
		}
    };
	
    /** 
     *  This method updates a datatrack. It first calls the method uploadDataTrack()
     *  and then updates the information in the database
     */
   
	$scope.saveDataTrack = function() {
		
		
		//Turn on uploading flag
		$scope.dataTracksUploading = true;
		$scope.datatrackEditMode = false;
		
		//Create promise
		var deferred = $q.defer();
		var promise = deferred.promise;
		
		
		//Process datatracks
		if ($scope.datatrack.file != null) {
			var files = [];
			files.push($scope.datatrack.file);
			promise = $scope.updateDataTrack($scope.datatrack, true, promise);
			promise = $scope.checkDataTrackNames(files, promise);
			promise = $scope.uploadAllFiles(files, promise);
		} else {
			promise = $scope.updateDataTrack($scope.datatrack, false, promise);
		}
		
		//Cleanup
		promise = promise.then(function() {
			console.log("GLOBAL OK");
			
			$scope.datatrack = null;
			$scope.dtcomplete = 100;
			cleanupAfterDatatrackUpload();
		}, function() {
			console.log("GLOBAL FAIL");
			$scope.datatrack = null;
			cleanupAfterDatatrackUpload();
			$scope.dtcomplete = 0;
		})
		
		//Launch
		deferred.resolve();
		
		
	};
	
	$scope.addDataTracks = function(files) {
		if (files.length == 0) {
			return;
		}
		
		//Turn on uploading flag
		$scope.dataTracksUploading = true;
		
		var deferred = $q.defer();
		var promise = deferred.promise;
		
		//Clean up datatracks
		promise = promise.then(function() {
			var cleanPromise = 	$http({
				url: "project/cleanDataTracks",
				method: "DELETE",
				params: {idProject: $scope.projectId},
			}).success(function(data) {
				var newDt = [];
				for (var i=0;i<$scope.datatracks.length;i++) {
					if (data.indexOf($scope.datatracks[i].idDataTrack) == -1) {
						newDt.push($scope.datatracks[i]);
					}
				}
				$scope.datatracks = newDt;
			});
			
			
			return $q.all(cleanPromise);
		});
		
		//Process datatracks
		promise = $scope.checkDataTrackNames(files, promise); //Make sure there are no duplicates
		promise = $scope.createDataTracks(files, promise); //create datatrack entries
		promise = $scope.uploadAllFiles(files, promise); //upload files
		
		
		//Cleanup
		promise = promise.then(function() { 
			$scope.dtcomplete = 100;
			cleanupAfterDatatrackUpload();
		},function() {
			cleanupAfterDatatrackUpload();
			$scope.dtcomplete = 0;
			
		})
		
		//Launch!
		deferred.resolve()
	}
	
	
	var cleanupAfterDatatrackUpload = function() {
		$scope.dataTracksUploading = false;
		$scope.loadProjects($scope.projectId);
		$scope.dataTrackUploadPromise = null;
		$scope.dataTrackUploadDeferred = null;
	}
	
	/** 
	 * Check if any of the filenames exist.  If they do, display a message and break the promise chain!!
	 */
	$scope.checkDataTrackNames = function(files, promise) {
		var badFiles = []
		var existPromiseList = []
		
		promise = promise.then(function() {
			for (var i=0;i<files.length;i++) {
				var existPromise = $http({
					url: "submit/doesDatatrackExist",
					method: "GET",
					params: {idProject : $scope.projectId, fileName: files[i].name, index: i}
				}).success(function(data, status, headers, config) {
					if (data.found) {
						badFiles.push(files[config.params["index"]]);
					} 
				});
				existPromiseList.push(existPromise);
			}
			return $q.all(existPromiseList).then(function() {
				var fileDeferred = $q.defer();
				
				if (badFiles.length > 0) {
					fileDeferred.reject("Duplicate File Names");
					var warningMessage = "<p>The following files have already been uploaded and won't be overwritten, please re-select your datatracks.</p>";
					warningMessage += "<ul>";
					for (var i=0;i<badFiles.length;i++) {
						
						warningMessage += "<li>" + badFiles[i].name + "</li>";
					}
					warningMessage += "</ul>";
					 
					dialogs.error("Duplicate file names",warningMessage);
				} else {
					fileDeferred.resolve();
				}
				return fileDeferred.promise;
			});
		});
		return promise;
	}
	

	
	/**
	 * This method generates progress statistics and calls upload function 
	 * for each file
	 */
	$scope.uploadAllFiles = function(files, promise) {
		$scope.dtcomplete = 0;
		$scope.totalGlobalSize = 0;
		$scope.currGlobalSize = 0;	
		$scope.currIndSize = [];
		$scope.totalIndSize = [];
			
		//Calculate global size
		for (var i=0; i<files.length; i++) {
			$scope.totalGlobalSize += files[i].size;
			$scope.totalIndSize.push(files[i].size);
			$scope.currIndSize.push(0);			
		}
		
		promise = promise.then(function() {
			var deferred = $q.defer();
			$scope.dataTrackUploadDeferred = $q.defer();
			var uploadPromise = deferred.promise;
			for (var dtIdx=0; dtIdx<$scope.datatracks.length;dtIdx++) {
				for (var fIdx=0;fIdx<files.length;fIdx++) {
					if ($scope.datatracks[dtIdx].path == files[fIdx].name) {
						uploadPromise = $scope.uploadSingleDataTrack(dtIdx,fIdx,files[fIdx],uploadPromise);
						break;
					}
				}
			}
			
			$scope.dataTrackUploadPromise = uploadPromise;
			deferred.resolve();
			return uploadPromise;
		});
		
		
		return promise;
	};
	
	$scope.stopDataTrackUpload = function() {
		if ($scope.dataTrackUploadDeferred != null) {
			$scope.dataTrackUploadDeferred.resolve();
		}
	};
	
	$scope.cleanDataTracks = function() {
		if ($scope.projectId != -1 && $scope.projectId != null) {
			$http({
				url: "project/cleanDataTracks",
				method: "DELETE",
				params: {idProject: $scope.projectId},
			}).success(function() {
				$scope.loadProjects($scope.projectId);
			});
		} 
	};

	
	/**
	 * Returns the basename of the file
	 */
	function baseName(str)
	{
	   var base = new String(str).substring(str.lastIndexOf('/') + 1); 
	    if(base.lastIndexOf(".") != -1)       
	        base = base.substring(0, base.lastIndexOf("."));
	   return base;
	}
	
	/**  
	 * This function uploads a single file to the server.  It chunks the file if necessary and then
	 * returns the upload promise. 
	 */
	$scope.uploadSingleDataTrack = function(datatrackIdx, fileIdx, file, promise) {
		var max = 10000000;
	
		var fileChunks = [];
		
		if (file.size > max) {
			for (var i=0;i<file.size;i+=max) {
				fileChunks.push(file.slice(i,i+max));
			}
		} else {
			fileChunks.push(file);
		}
		
		var loaded = 0;
		for (var i=0; i<fileChunks.length; i++) {
			(function(i, fileIdx, datatrackIdx) {
				promise = promise.then(function() {
					if ($scope.datatracks[datatrackIdx].state != "FAILURE") {
						return $upload.upload({
							url: "project/uploadDataTrack",
							file: fileChunks[i],
							params : {index: i, total: fileChunks.length, name: file.name, idProject: $scope.project.idProject, dtname: $scope.datatracks[datatrackIdx].name},
							timeout: $scope.dataTrackUploadDeferred.promise,
						}).progress(function(evt) {
							$scope.datatracks[datatrackIdx].complete = 100 * (evt.loaded + $scope.currIndSize[fileIdx]) / $scope.totalIndSize[fileIdx];
							$scope.currGlobalSize = 0;
							for (var j = 0; j < $scope.currIndSize.length; j++) {
								$scope.currGlobalSize += $scope.currIndSize[j];
							}
							$scope.dtcomplete = 100.0 * (evt.loaded + $scope.currGlobalSize) / $scope.totalGlobalSize;
													
						}).success(function(data) {
							$scope.currIndSize[fileIdx] += fileChunks[i].size;
							$scope.datatracks[datatrackIdx].state = data.state;
							$scope.datatracks[datatrackIdx].message = data.message;
							if (data.finished) {
								
								if (data.state == "SUCCESS") {
									$scope.datatracks[datatrackIdx].complete = 100;
									$http({
										url: "project/finalizeDataTrack",
										method: "PUT",
										params: {uploadStatus: data.state, idDataTrack: $scope.datatracks[datatrackIdx].idDataTrack, message: data.message},
									});
								} else {
									$scope.datatracks[datatrackIdx].complete = 100;
									$http({
										url: "project/finalizeDataTrack",
										method: "PUT",
										params: {uploadStatus: data.state, idDataTrack: $scope.datatracks[datatrackIdx].idDataTrack, message: data.message},
									});
								}
							}
						})
					}
					
				});
			})(i,fileIdx,datatrackIdx);
		}
		return promise;
	};
		
	
	/** This function removes a datatrack from the database.  It clears the datatrack from the database and clears the
	 * file from the server.  This function only works if the datatrack is not already associated with an analysis.
	 */
	$scope.removeDataTrack = function(datatrack) {
		//Keeping it a list in case we move over to checkboxes
		var fileList = [];
		fileList.push(datatrack);
		$scope.clearDataTrack();
		
		
		
		if (datatrack.analysisSet) {
			var message = "";
			message += "<p>The datatrack you are trying to delete is associated with at least one existing analyses and can't be deleted. Please delete the appropriate analyses and try again.</p>";
			message += "<br/>";
			message += "<ul>";
			for (var i=0;i<fileList.length;i++) {
				message += "<li>" + datatrack.name + "</li>";
			}
			message += "</ul>";
		
			dialogs.error("Can't delete selected datatracks", message, null);
			
			return;
		}
		
		$http({
			url : "project/deleteDataTrack",
			method: "DELETE",
			params: {idDataTrack: datatrack.idDataTrack}
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
		}).error(function(data) {
			console.log("Could not delete datatrack");
		});
    };
    
    /** 
     * Displays errors assocated with the datatrack upload
     */
    $scope.showDataTrackError = function(datatrack) {
    	$scope.showErrorMessage("Error uploading datatracks", datatrack.message);
    };
	
    /** 
     * Creates new datatrack entries in the database.
     */
	$scope.createDataTracks = function(files, promise) {
		for (var i=0; i<files.length;i++) {
			var name = baseName(files[i].name);
			var path = files[i].name;
			(function(name,path) {
				promise = promise.then(function() {
					return  $http({
						url : "project/createDataTrack",
						method: "PUT",
						params: {idProject: $scope.projectId, name: name, path: path},
					}).success(function(data) {
						$scope.datatracks.push(data);
					}).error(function(data) {
						console.log("Could not create datatrack");
					});
				})
			})(name,path);
				
		}
		return promise;
	};
	
	/**
	 *  Updates a datatrack inthe database
	 */
	$scope.updateDataTrack = function(datatrack, toDelete, promise) {
		promise = promise.then(function() {
			return $http({
				url: "project/updateDataTrack",
				method: "PUT",
				params: {idProject: $scope.projectId, name: datatrack.name, path: datatrack.path, idDataTrack: datatrack.idDataTrack, toDelete: toDelete},
			}).success(function(data) {
				for (var i=0;i<$scope.datatracks.length;i++) {
					if ($scope.datatracks[i].idDataTrack == data.idDataTrack) {
						$scope.datatracks[i] = data;
						break;
					}
				}
			}).error(function(data) {
				console.log("Could not update datatrack");
			})
		})
		return promise;
	};
	
	
	/**
	 * This function sets the scope object datatrack to the first selected file
	 * This file is used when the file is updated.
	 */
	$scope.addDataTrackFile = function(files) {
		if (files.length > 0) {
			$scope.datatrack.file = files[0];
			$scope.datatrack.path = files[0].name;
		}
		
	};
	
	
	
	
	/**********************
	 * Analysis Management
	 *********************/
	
	$scope.clearResult = function() {
		$scope.result = {};
		$scope.result.date = new Date();
		$scope.resultEditMode = false;
		$scope.originalResultName = null;
	};

	$scope.editResult = function(result) {
		$scope.result = angular.copy(result);
		$scope.result.date = new Date(result.date);
		$scope.resultEditMode = true;
		$scope.originalResultName = $scope.result.name;
		$anchorScroll();
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
			params: {idAnalysis: result.idAnalysis, name: result.name, description: result.description, date: result.date.getTime(), idProject: $scope.projectId, 
				idSampleList: sampleList, idDataTrackList: dataTrackList, idFileUpload: result.file.idFileUpload, idAnalysisType: result.analysisType.idAnalysisType}
		}).success(function(data) {
			$scope.loadProjects($scope.projectId);
			$scope.resetResult();
			$scope.originalResultName = null;
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
			$scope.lastResult = $scope.result;
			$scope.lastResult.file = null;
			$scope.originalResultName = null;
			$scope.resetResult();
		}).error(function(data) {
			console.log("Could not create analysis.");
		});
	};
	
	$scope.duplicateResult = function() {
		$scope.originalResultName = null;
		$scope.result = $scope.lastResult;
		$scope.result.date = new Date($scope.lastResult.date);
		$anchorScroll();
	};
	
	$scope.copyResult = function(result) {
		$scope.resultEditMode = false;
		$scope.originalResultName = null;
		$scope.result = angular.copy(result);
		$scope.result.file = null;
		$scope.result.date = new Date(result.date);
		$anchorScroll();
	};
	
	$scope.checkResultName = function(name) {
		if ($scope.resultEditMode && $scope.originalResultName == name) {
			return true;
		}
		var retVal = true;
		for (var i=0; i<$scope.results.length;i++) {
			if (name == $scope.results[i].name) {
				retVal = false;
			}
		}
		return retVal;
	};
	
	$scope.resetResult = function() {
		$scope.result = {};
		$scope.result.date = new Date();
	}
	
	
	//Messaging
	
	
	
	$scope.showErrorMessage = function(title,message) {
		dialogs.error(title, message, null);
	};
	
	$scope.showWarningMessage = function(title,message) { 
		dialogs.notify(title,message,null);
	};
	
	
	$scope.addNewPrep = function() {
		if ($scope.newSamplePrep.description == null || $scope.newSamplePrep.description == "") {
			return;
		}
		
		$http({
			url: "project/isSamplePrepNameUsed",
			method: "GET",
			params: {prep: $scope.newSamplePrep.description}
		}).success(function(data) {
			if (data.found) {
				var message = "<p>The specified sample prep " + $scope.newSamplePrep.description + " already exists!</p>";
				$scope.showErrorMessage("Duplicate sample prep",message);
			} else {
				$http({
					url: "project/addSamplePrep",
					method: "PUT",
					params: {description: $scope.newSamplePrep.description, idSampleType: $scope.sample.sampleType.idSampleType},
				}).success(function(data) {
					$scope.newSamplePrep.description = "";
					$scope.loadSamplePreps();
					$scope.loadSamplePrepsBySampleType();
					$scope.sample.samplePrep = data;
					$scope.checkSamplePrep($scope.sample.samplePrep);
				});
			}
			
		});
	};
	
	$scope.addNewSource = function() {
		if ($scope.newSampleSource.source == null || $scope.newSampleSource.source == "") {
			return;
		}
		
		$http({
			url: "project/isSampleSourceNameUsed",
			method: "GET",
			params: {source: $scope.newSampleSource.source, idOrganismBuild: $scope.project.organismBuild.idOrganismBuild}
		}).success(function(data) {
			if (data.found) {
				var message = "<p>The specified sample souce " + $scope.newSampleSource.source + " already exists!</p>";
				$scope.showErrorMessage("Duplicate sample source",message);
			} else {
				$http({
					url: "project/addSampleSource",
					method: "PUT",
					params: {source: $scope.newSampleSource.source, idOrganismBuild: $scope.project.organismBuild.idOrganismBuild},
				}).success(function(data) {
					$scope.newSampleSource.source = "";
					$scope.loadSampleSources();
					$scope.sample.sampleSource = data;
					$scope.checkSampleSource($scope.sample.sampleSource);
				});
			}
		});
	};
	
	$scope.addNewCondition = function() {
		if ($scope.newSampleCond.cond == null || $scope.newSampleCond.cond == "") {
			return;
		}
		
		$http({
			url: "project/isSampleConditionNameUsed",
			method: "GET",
			params: {cond: $scope.newSampleCond.cond, idOrganismBuild: $scope.project.organismBuild.idOrganismBuild}
		}).success(function(data) {
			if (data.found) {
				var message = "<p>The specified sample condition " + $scope.newSampleCond.cond + " already exists!</p>";
				$scope.showErrorMessage("Duplicate sample condition",message);
			} else {
				$http({
					url: "project/addSampleCondition",
					method: "PUT",
					params: {condition: $scope.newSampleCond.cond, idOrganismBuild: $scope.project.organismBuild.idOrganismBuild},
				}).success(function(data) {
					$scope.newSampleCond.cond = "";
					$scope.loadSampleConditions();
					$scope.sample.sampleCondition = data;
					$scope.checkSampleCondition($scope.sample.sampleCondition);
				});
			}
		});
	};
	
	
	
	/****************
	 * Hide/Show controls
	 */
	
	$scope.hideSampleControls = function(sample) {
		sample.show = false;
	};
	
	$scope.showSampleControls = function(sample) {
		sample.show = true;
	};
	
	$scope.hideResultControls = function(result) {
		result.show = false;
	};
	
	$scope.showResultControls = function(result) {
		result.show = true;
	}
	
	$scope.hideDatatrackControls = function(datatrack) {
		datatrack.show = false;
	};
	
	$scope.showDatatrackControls = function(datatrack) {
		datatrack.show = true;
	};
	
	$scope.datepicker = {opened: false};
	$scope.open = function($event) {
		$event.preventDefault();
	    $event.stopPropagation();
	    $scope.datepicker.opened = true;
	};
	
	/***********
	 * styling
	 */
	
	$scope.resultPanelStyle = {};
	$scope.$watch("resultEditMode", function() {
		if ($scope.resultEditMode) {
			$scope.resultPanelStyle = {'background-color':'LightYellow'};
		} else {
			$scope.resultPanelStyle = {'background-color':'white'};
		}
		
	});
	
	$scope.projectPanelStyle = {}
	$scope.$watch("projectEditMode", function() {
		if ($scope.projectEditMode) {
			$scope.projectPanelStyle = {'background-color':'LightYellow'};
		} else {
			$scope.projectPanelStyle = {'background-color':'white'};
		}
	})
	
	$scope.samplePanelStyle = {}
	$scope.$watch("sampleEditMode", function() {
		if ($scope.sampleEditMode) {
			$scope.samplePanelStyle = {'background-color':'LightYellow'};
		} else {
			$scope.samplePanelStyle = {'background-color':'white'};
		}
	})
	
	$scope.datatrackPanelStyle = {}
	$scope.$watch("datatrackEditMode", function() {
		if ($scope.datatrackEditMode) {
			$scope.datatrackPanelStyle = {'background-color':'LightYellow'};
		} else {
			$scope.datatrackPanelStyle = {'background-color':'white'};
		}
	})
	
	
	$scope.sampleEditMode = false;

	$timeout(function() {
		$scope.disableElement = true;
	},1000);
	
	/***************************************
	 * *************************************
	 * 
	 *     Help!!
	 * 
	 * *************************************
	 ***************************************/
	
	
	$scope.showHelpProject = function() {
		var title = "Help: Edit Project Metadata";
		
		dialogs.notify(title, $scope.helpProject);
	}
	
	$scope.helpPreamble = 
		"<p>This page allows users to create new <strong>Projects</strong> or modify exising ones.  Once a user creates a new <strong>Project</strong>, they can " +
		"enter information about samples used in the project, upload datatracks that can be used to visualize data, and upload outputs " +
		"from differential expression, differential methylation, variant detection and ChIP-Seq peak detection analyses.  Once " +
		"all of these components are entered, they can be assembled into an <strong>Analysis</strong>. Each <strong>Analysis</strong> is " +
		"defined by a single differential expression, differential methylation, variant detection or ChIP-Seq peak detection result and any number " +
		"of samples and datatracks.  Once an <strong>Analysis</strong> is created, it can be searched in the query page.</p> " +
		"<h3>Basic Controls</h3> " +
		"<p><strong>Submissions Panel</strong>: The submissions panel can be found on the left of the page and lists all of the projects that are visible to the user. " +
		"Clicking on a project name will load the project information in the panel on the right side of the page.</p> " +
		"<p><strong>New Submission Button</strong>: The new submission button is the first step in creating a new project. Clicking this link will bring up a window " +
		"that asks for basic information about the project:</p>" +
		"<ol>" +
		"<li><strong>Name</strong> <em>required</em>: A descriptive name for the project. Projects can be selected by name in the query page, so it is helpful if the name " +
		"clearly identifies the project.</li>" +
		"<li><strong>Description</strong>: An optional description of the project.</li> " +
		"<li><strong>Genome Build </strong><em>required</em>: The genome build used when analyzing the data (i.e hg19, mm10, zv9).  Only genome builds with loaded annotations will be included " +
		"in this list.  If your favorite build is missing, please contact the Biominer Team.</li> " +
		"<li><strong>Lab </strong><em>required</em>: The list of labs associated with the project. If the project visiblity is set to <strong>Lab</strong>, only " +
		"users belonging to the labs set in this field can view or search the data. The dropdown will only contain labs affiliated with the user.</li>" +
		"<li><strong>Institute </strong><em>required</em>: The list of institutes associated with the project.  If the project visibility is set to <strong>Institute</strong>, " +
		"only users belonging to the institutes set in this field will be able to view or search the data.  The drowpdown will only contain institutes affiliated with the user.</li> " +
		"</ol>" +
		"<p>Once the user fills out the required fields, the <strong>Add</strong> button becomes active.  Pressing this button will add the project to the biominer database.</p> " +
		"<p><strong>Refresh</strong>: Refresh will relead the project list contained in the <strong>Submissions Panel</strong></p> " +
		"<p><strong>Delete</strong>: Delete will remove the project from the database. The project can only be deleted once all samples, datatracks, files and analyses are deleted " +
		"from the project.</p>";
	
	$scope.showHelpProject = function() {
		var title = "Help: Edit Project Metadata";
		
		dialogs.notify(title, $scope.helpProject);
	}
	
	$scope.helpProject = 
		"<h3>Edit Project Metadata</h3>" +
		"The project metatdata screen can be used to view or edit the basic project information.  By default, the information is read only, but can be edited if the user presses the " +
		"<strong>Edit</strong> button.  Once the user is happy with the changes, they can click the <strong>Save</strong> button to commit the changes. Most of the data is identical " +
		"to the entry form, but there are a few additions:</p> " +
		"<ol>" +
		"<li><strong>Name</strong> <em>required</em>: A descriptive name for the project. Projects can be selected by name in the query page, so it is helpful if the name " +
		"clearly identifies the project.</li>" +
		"<li><strong>Description</strong>: An optional description of the project.</li> " +
		"<li><strong>Data URLs</strong>: If the original data is stored in a LIMS system like GNomEx, it might be helpful to include the location of the data.</li>" +
		"<li><strong>Genome Build </strong><em>required</em>: The genome build used when analyzing the data (i.e hg19, mm10, zv9).  Only genome builds with loaded annotations will be included " +
		"in this list.  If your favorite build is missing, please contact the Biominer Team.</li> " +
		"<li><strong>Lab </strong><em>required</em>: The list of labs associated with the project. If the project visiblity is set to <strong>Lab</strong>, only " +
		"users belonging to the labs set in this field can view or search the data. The dropdown will only contain labs affiliated with the user.</li>" +
		"<li><strong>Institute </strong><em>required</em>: The list of institutes associated with the project.  If the project visibility is set to <strong>Institute</strong>, " +
		"only users belonging to the institutes set in this field will be able to view or search the data.  The drowpdown will only contain institutes affiliated with the user.</li> " +
		"<li><strong>Visibility</strong>: Project visibility.  The visibility is set to <strong>Public</strong> by default.  The user can also set the visibility to members of " +
		"their <strong>Lab</strong> or members of their <strong>Institution.</strong></li>" +
		"</ol>";
	
	$scope.showHelpSample = function() {
		var title = "Help: Import Samples into Project";
		dialogs.notify(title, $scope.helpSample);
	}
	
	$scope.helpSample = 
		"<h3>Import Samples into Project</h3>" +
		"<p>The samples page can be used to view or edit the samples used in the project. Once all of the required fields are filled out, the user " +
		"can press the <strong>Add</strong> button to save the sample to the database. If the user is submitting multiple samples, they can use the " +
		"<strong>Duplicate</strong> button to replicate the last entered sample. The sample name must be unique to the project, so the <strong>Add</strong> button will " +
		"be disabled until the name is unique. If the user wants to start over with the sample entry, the user can click the <strong>Clear</strong> button. " +
		"The entry fields are: <p>" +
		"<ol>" +
		"<li><strong>Name </strong><em>required</em>: The name of the sample</li>" +
		"<li><strong>Type </strong><em>required</em>: Sample type (i.e. RNA, DNA).</li>" +
		"<li><strong>Prep Method </strong><em>required</em>: Kit used when prepping the sample for sequencing. Users can add new kits by selecting the <strong> " +
		"Add New</strong> option from the dropdown. Once the user is finished typing out the name, they must click on the <strong>+</strong> button " +
		"to add it to the database.  If the prep method has not been used by anyone, it can be removed from the database using the <strong>-</strong> button " +
		". The prep method dropdown is disabled until the user selects a sample type.</li>" +
		"<li><strong>Source </strong><em>required</em>: The source of the sample, which is often the tissue or cell type. The entries in this dropdown tied to the genome build. " +
		"Users can add new sample sources by selecting the <strong>Add New</strong> option from the dropdown. Once the user is finished typing out " +
		"the source, they must click on the <strong>+</strong> button to add it to the database.  If the sample source has not been used by anyone, it " +
		"can be removed from the database using the <strong>-</strong> button.</li>" +
		"<li><strong>Condition </strong><em>required</em>: The sample condition (i.e. Treatment, Control, H3K27me3, mutant, normal). The entries in this dropdown tied to the genome build. " +
		"Users can add new sample conditions by selecting the <strong>Add New</strong> option from the dropdown. Once the user is finished typing out " +
		"the condition, they must click on the <strong>+</strong> button to add it to the database.  If the sample condition has not been used by anyone, it " +
		"can be removed from the database using the <strong>-</strong> button.</li>" +
		"</ol>" +
		"<p>Samples in the database are listed in the table below the entry form. The table can't be sorted. Users can use the <strong>Controls</strong> column to delete samples, " +
		"edit samples, or copy sample information to the entry form. Samples cannot be deleted if they are included in an <strong>Analysis</strong>.</p>" +
		"<p>If the <strong>Edit</strong> button is pushed, the sample information is displayed in the entry form.  Once the user is happy with the " +
		"changes, the <strong>Save</strong> button can be pushed to commit the changes to the database.  If the user decides against the edits, the " +
		"<strong>Cancel</strong> button can be pushed.</p>";
	
	$rootScope.helpMessage = 
		
	    "<h1>Submit Data Page</h1>" +
	    $scope.helpPreamble +
	    $scope.helpProject +
	    $scope.helpSample;
	    
}]);





