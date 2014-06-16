'use strict';


var upload  = angular.module('upload',  ['ui.bootstrap', 'angularFileUpload','filters', 'services', 'directives']);

angular.module("upload").controller("UploadController", ['$scope','$upload','$http','$modal','$q',
                                                      
	function($scope, $upload, $http, $modal, $q) {
	
		$scope.selectAllFiles = false;
		$scope.columnDefs = null;
		
		$scope.parsedFiles = [];
		$scope.parsedFiles.type = "parsed";
		$scope.$parent.uploadedFiles.type = "imported";
		
		
		/********************
		 * Upload files!
		 ********************/
		$scope.onFileSelect = function($files) {
			//Initialize upload status variables.
			$scope.complete = 0;
			$scope.totalGlobalSize = 0;
			$scope.currGlobalSize = 0;
			$scope.localTotals = [];
			
			//Calculate global size and initialize local sizes
			for (var i=0; i<$files.length; i++) {
				$scope.totalGlobalSize += $files[i].size;
				$scope.localTotals.push(0);
			}
			
			var promise = $q.all(null);
			for (var i=0; i<$files.length; i++) {
				//initialize variables
				var file = $files[i];
				var index = -1;
				
				
				//Check to see if there are any matching files in list (match on name only)
				for (var j=0; j<$scope.$parent.uploadedFiles.length; j++) {
					if (file.name == $scope.$parent.uploadedFiles[j].name) {
						index = j;
						$scope.$parent.uploadedFiles[j].message = "started";
						$scope.$parent.uploadedFiles[j].complete = 0;
					}
				}
				
				//If no match, create new file object.
				if (index == -1) {
					var f = {name: file.name, message: "started", complete: 0};
					index = $scope.$parent.uploadedFiles.length;
					$scope.$parent.uploadedFiles.push(f);
				}
				
				//upload the file
				(function (i,index,file) {
						return $scope.upload = $upload.upload({
							url: "submit/upload",
							file: file,
							progress: function(evt) {
								$scope.$parent.uploadedFiles[index].complete = 100.0 * evt.loaded / evt.total;
								
								//Set global progress
								$scope.localTotals[i] = evt.loaded;
								$scope.currGlobalSize = 0;
								for (var j = 0; j < $scope.localTotals.length; j++) {
									$scope.currGlobalSize += $scope.localTotals[j];
								}
								$scope.complete = 100.0 * $scope.currGlobalSize / $scope.totalGlobalSize;
							}
						}).success(function(data) {
							console.log(i);
							if (data.message != "success") {
								//Only set message on failure
								$scope.$parent.uploadedFiles[index].message = data.message;
							} else {
								//Set everything on success
								$scope.$parent.uploadedFiles[index] = data;
							}
							$scope.$parent.uploadedFiles[index].selected = false;
							
						}).error(function(data) {
							$scope.$parent.uploadedFiles[index].status = "failure";
						});
				})(i,index,file);
				
			}
		};
		
		/********************
		 * Select/Deselect all
		 ********************/
		$scope.clickSelected = function(collection) {
			$scope.selectAllFiles = !$scope.selectAllFiles;
			for (var i=0; i < collection.length;i++) {
				collection[i].selected = $scope.selectAllFiles;
			}
		};
		
		/********************
		 * Delete selected files
		 ********************/
		$scope.deleteSelected = function(collection) {
			for (var i = 0; i < collection.length; i++) {
				var file = collection[i];
				var name = collection[i].name;
				
				if (file.selected == true) {
					collection.message = "pending";
					
					if (file.size == null) {
						for (var j=0; j<collection.length;j++) {
							if (collection[j].name == name) {
								collection.splice(j,1);
							}
						}
					} else {
						$http({
							url: "submit/upload/delete",
							method: "DELETE",
							params: {file: file.name, type: collection.type}
						}).success((function (name) {
							return function(data) {
								for (var j=0; j<collection.length;j++) {
									if (collection[j].name == name) {
										collection.splice(j,1);
									}
								}
							};
						})(name));
					}
				}
			}
		};
		
		/********************
		 * When parse button is pushed, generate a file preview and selection modal
		 ********************/
		$scope.parse = function() {
			//Iterate through the file list and look for selected
			var paramSet = false;
			
			for (var i=0; i<$scope.$parent.uploadedFiles.length;i++) {
				
				if (paramSet == false && $scope.$parent.uploadedFiles[i].selected == true) {
					$http({
						url: "submit/parse/preview/",
						method: "POST",
						params: {filename: $scope.$parent.uploadedFiles[i].name}
					}).success(function(data,status,headers,config) {
				    	var modalInstance = $modal.open({
				    		templateUrl: 'app/submit/previewWindow.html',
				    		controller: 'PreviewWindowController',
				    		windowClass: 'preview-dialog',
				    		resolve: {
				    			filename: function() {
				    				return config.params.filename;
				    			},
				    			previewData: function() {
				    				return data.previewData;
				    			}
				    		}
				    	});
				    	
				    	modalInstance.result.then(function (setColumns) {
				    		$scope.columnDefs = setColumns;
					    });
					   
					}).error(function(data) {
						console.log("ARG!");
					});
					paramSet =true;
				} 
			}	
		};
		
		/********************
		 * When columnDefs object is modified (presumably from PreviewWindow) call parser.
		 ********************/
		$scope.$watch("columnDefs",function() {
			if ($scope.columnDefs != null) {
				
				var promise = $q.when(null);
				for (var i=0; i<$scope.$parent.uploadedFiles.length;i++) {
					if ($scope.$parent.uploadedFiles[i].selected == true) {
						
						//Create paramter list.  Column defs + file names
						var params = {};
						params.inputFile = $scope.$parent.uploadedFiles[i].name;
						params.inputDirectory = $scope.$parent.uploadedFiles[i].directory;
						params.outputFile = $scope.$parent.uploadedFiles[i].name + ".PARSED.txt";
						
						//Check to see if there are any matching files in list (match on name only)
						var index = -1;
						for (var j=0; j<$scope.parsedFiles.length; j++) {
							if (params.outputFile == $scope.parsedFiles[j].name) {
								index = j;
								$scope.parsedFiles[j].message = "started";
							}
						}
						
						//If no match, create new file object.
						if (index == -1) {
							var f = {name: params.outputFile, message: "started"};
							index = $scope.parsedFiles.length;
							$scope.parsedFiles.push(f);
						}
						
						for (var k=0; k<$scope.columnDefs.length; k++) {
							params[$scope.columnDefs[k].name] = $scope.columnDefs[k].index;
						}
						
						(function(i,params) {
							promise = promise.then(function() {
								return $http({
									url: "submit/parse/chip",
									method: "POST",
									params: params
								}).success(function(data) {
									data.selected = false;
									$scope.parsedFiles[index] = data;
								});
							});
						}(i,params));
						

					}
				}
			}	
		});
		
		
		/********************
		 * Load existing uploaded/parsed files
		 ********************/
		$scope.loadExisting = function(collection) {
			$http({
				url: "submit/upload/load",
				method: "GET",
				params: {type : collection.type}
			}).success( function(data) {
        		for (var i = 0; i < data.files.length; i++) {
        			//Set selected
    				data.files[i].selected = false;
        			
    				//Go through existing model
        			var index = -1;
        			for (var j=0; j < collection.length; j++) {
        				//Check for matches
        				if (data.files[i].name == collection[j].name) {
        					index = j;
        				}
        			}
        			
        			//If new, add, otherwise replace
    				if (index == -1) {
        				collection.push(data.files[i]);
        			} else {
        				collection[index] = data.files[i];
        			}
            	}
        	});
		};
		
		
		/********************
		 * Initialization
		 ********************/
        $scope.onload = function(data) {
        	this.loadExisting($scope.$parent.uploadedFiles);
        	this.loadExisting($scope.parsedFiles);	        	
        };
        
        $scope.onload(); //call initialization code
	
}]);