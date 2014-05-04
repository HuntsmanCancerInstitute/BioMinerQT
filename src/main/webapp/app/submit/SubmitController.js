'use strict';

/**
 * SubmitController
 * @constructor
 */
var submit    = angular.module('submit',    ['ui.bootstrap', 'blueimp.fileupload','filters', 'services', 'directives']);

angular.module("submit").controller("SubmitController", [
'$scope', '$http', '$modal',
function($scope, $http, $modal) {
	
    $scope.project = {};
    $scope.nextIdProject = 100;
    $scope.projectName = "";
    $scope.projectDescription = "";
    

	
    $scope.uploadedFiles = [];

    $scope.sample = {};
    $scope.nextIdSample = 100;
    $scope.sampleEditMode  = false;
	$scope.samples = [];
	
    $scope.datatrack = {};
    $scope.nextIdDataTrack = 100;
    $scope.datatrackEditMode  = false;
	$scope.datatracks = [];
	
	
    $scope.result = {};
    $scope.nextIdResult = 100;
    $scope.resultEditMode  = false;
	$scope.results = [];

	
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
	
	$scope.sampleTypeList = [
	                  {idSampleType: 1, name: "RNA -> polyA"},
	                  {idSampleType: 2, name: "RNA->RiboZero"},
	                  {idSampleType: 3, name: "ChIP DNA"},
	                  {idSampleType: 4, name: "DNA"}
	                  
	];
	$scope.siteList = [
	   	                  {idSite: 1, name: "Left ventricle", organ: "Heart"},
	   	                  {idSite: 2, name: "Right ventricle", organ: "Heart"},
	   	                  {idSite: 3, name: "Aortic valve", organ: "Heart"},
	   	                  {idSite: 4, name: "Left interior lobe", organ: "Lung"},
	   	                  {idSite: 5, name: "Right interior lobe", organ: "Lung"}
	   	                  
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


	$scope.projects = [
	                 {"id": 1,
	                  "name": "Project ABC",
	                  "description": "Fast just got faster with Nexus S.",
	                  "idLab": 1,
	                  "idGenomeBuild": 3,
	                  "codeVisibility": "MEM"},
	                 {"id": 2,
	                  "name": "My Analysis Project",
	                  "description": "The Next, Next Generation tablet.",
	                  "idLab": 2,
	                  "idGenomeBuild": 4,
	                  "codeVisibility": "PUBLIC"},
	                 {"id": 3,
	                  "name": "ChIP SEQ",
	                  "description": "Here is a good description.",
	                  "idLab": 3,
	                  "idGenomeBuild": 7,
	                  "codeVisibility": "INST"}
	               ];
	
	
	
	$scope.edit = function(id) {
		//search project with given id and update it
        for(var i in $scope.projects) {
            if($scope.projects[i].id == id) {
            	$scope.projects[i].cssClass = "current-project";
            	$scope.project = $scope.projects[i];
            } else {
            	$scope.projects[i].cssClass = "";
            }
        }
    };
	
	$scope.add = function(project) {
		$scope.projects.push( {"id": ++$scope.nextIdProject,
							   "name": project.name,
			                   "description": project.description,
			                   "cssClass": "current-project"}
							);
		
		$scope.project = $scope.projects[$scope.projects.length - 1];

		for(var i in $scope.projects) {
            if($scope.projects[i].id == $scope.project.id) {
            	$scope.projects[i].cssClass = "current-project";
            } else {
            	$scope.projects[i].cssClass = "";
            }
        }
		
	};
	
	//
	//  New Analysis Project Dialog
	//
	$scope.openNewProjectWindow = function () {

		    var modalInstance = $modal.open({
		      templateUrl: 'app/submit/newProjectWindow.html',
		      controller: 'ProjectWindowController',
		      resolve: {
		        projectName: function () {
		          return $scope.projectName;
		        }, 
		        projectDescription: function() {
		          return $scope.projectDescription;  
		        }
		      }
		    });

		    modalInstance.result.then(function (project) {
		    	$scope.add(project);
				
				$scope.projectName = "";
				$scope.projectDescription = "";
		    	
		    }, function () {
		    	// When dialog dismissed
		    });
	};
	
	
	
	//
	// Samples
	//

	$scope.editSample = function(sample) {
		$scope.sample = sample;
		$scope.sampleEditMode = true;
    };
	
	$scope.saveSample = function(sample) {
		$scope.sampleEditMode = false;
		
		var newSample = {};
		$scope.sample = newSample;
		$scope.clearSample();
	};
	
	$scope.removeSample = function(id) {
        for(var i in $scope.samples) {
            if($scope.samples[i].idSample == id) {
            	$scope.samples.splice(i, 1);
            	break;
            }
        }
       
    };
	
	$scope.addSample = function(sample) {
		var newSample = {};
		newSample.idSample = ++$scope.nextIdSample;
		newSample.name = sample.name;
		newSample.idSampleType = sample.idSampleType;
		newSample.idSite = sample.idSite;
		newSample.idSampleGroup = sample.idSampleGroup;
		
		$scope.samples.push(newSample);
		
		$scope.clearSample();
	};
	
	$scope.clearSample = function() {
		$scope.sample.idSample = -1;
		$scope.sample.name = "";
		$scope.sample.idSampleType = "";
		$scope.sample.idSite = "";
		$scope.sample.idSampleGroup = "";

		
	};
	
	$scope.duplicateSample = function(sample) {
		var newSample = {};
		newSample.idSample = ++$scope.nextIdSample;
		newSample.name = sample.name;
		newSample.idSampleType = sample.idSampleType;
		newSample.idSite = sample.idSite;
		newSample.idSampleGroup = sample.idSampleGroup;
		
		$scope.samples.push(newSample);
	};
	
	
	//
	// Data Tracks
	//
	$scope.editDataTrack = function(datatrack) {
		$scope.datatrack = datatrack;
		$scope.datatrackEditMode = true;
    };
	
	$scope.saveDataTrack = function(datatrack) {
		$scope.datatrackEditMode = false;
		
		var newDataTrack = {};
		$scope.datatrack = newDataTrack;
		$scope.clearDataTrack();
	};
	
	$scope.removeDataTrack = function(id) {
        for(var i in $scope.datatracks) {
            if($scope.datatracks[i].idDataTrack == id) {
            	$scope.datatracks.splice(i, 1);
            	break;
            }
        }
       
    };
	
	$scope.addDataTrack = function(datatrack) {
		var newDataTrack = {};
		newDataTrack.idDataTrack = ++$scope.nextIdDataTrack;
		newDataTrack.name = datatrack.name;
		newDataTrack.url = datatrack.url;
		
		$scope.datatracks.push(newDataTrack);
		
		$scope.clearDataTrack();
	}; 
	
	$scope.clearDataTrack = function() {
		
		$scope.datatrack.idDataTrack = -1;
		$scope.datatrack.name = "";
		$scope.datatrack.url  = "";
		
	};
	
	$scope.duplicateDataTrack = function(datatrack) {
		var newDataTrack = {};
		newDataTrack.idDataTrack = ++$scope.nextIdDataTrack;
		newDataTrack.name = datatrack.name;
		newDataTrack.url = datatrack.url;
		
		
		$scope.addDataTrack(datatrack);
	};
	
	
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





