'use strict';


var upload  = angular.module('upload',  ['ui.bootstrap', 'angularFileUpload','filters', 'services', 'directives','error','fneditor']);

angular.module("upload").controller("UploadController", ['$scope','$upload','$http','$modal','$q',
                                                      
	function($scope, $upload, $http, $modal, $q) {

		$scope.selectAllParse = false;
		$scope.selectAllImport = false;
		$scope.columnDefs = null;
		
		$scope.parsedFiles = [];
		$scope.selectedFiles = [];
		
		
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
						$scope.$parent.uploadedFiles[j].state = "started";
						$scope.$parent.uploadedFiles[j].complete = 0;
					}
				}
				
				//If no match, create new file object.
				if (index == -1) {
					var f = {name: file.name, state: "started", complete: 0};
					index = $scope.$parent.uploadedFiles.length;
					$scope.$parent.uploadedFiles.push(f);
				}
				
				//upload the file
				(function (i,index,file) {
						return $scope.upload = $upload.upload({
							url: "submit/upload",
							file: file,
							data: {analysisID: $scope.$parent.project.id},
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
							if (data.state != "success") {
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
		$scope.clickSelected = function(collection,status) {
			status = !status;
			for (var i=0; i < collection.length;i++) {
				collection[i].selected = status;
			}
			console.log($scope.$parent.project.id);
		};
		
		/********************
		 * Delete selected files
		 ********************/
		$scope.deleteSelected = function(collection,type) {
			for (var i = 0; i < collection.length; i++) {
				var file = collection[i];
				var name = collection[i].name;
				
				if (file.selected == true) {
					collection.state = "started";
					
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
							params: {file: file.name, type: type}
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
			$scope.selectedFiles = [];	
			for (var i=0; i<$scope.$parent.uploadedFiles.length;i++) {
				if ($scope.$parent.uploadedFiles[i].selected == true) {
					var outname = "";
					var inputname = $scope.$parent.uploadedFiles[i].name;
					if (inputname.indexOf(".gz",this.length-3) !== -1) {
						outname = inputname.substring(0,inputname.length-3) + ".PARSED.gz";
					} else {
						outname = inputname + ".PARSED";
					}
		
					var sf = {file : $scope.$parent.uploadedFiles[i], outname: outname};
					$scope.selectedFiles.push(sf);
				}
			}
				
			if ($scope.selectedFiles.length > 0) {
				$http({
					url: "submit/parse/preview/",
					method: "POST",
					params: {filename: $scope.selectedFiles[0].file.name, analysisID: $scope.$parent.project.id}
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
			}
		};
		
		/********************
		 * When columnDefs is modified (presumabaly from parse), fire this code
		 ********************/
		$scope.$watch("columnDefs",function() {
			if ($scope.columnDefs != null) {
				var modalInstance = $modal.open({
		    		templateUrl: 'app/submit/FilenameEditor.html',
		    		controller: 'FilenameEditorController',
		    		windowClass: 'filename-dialog',
		    		resolve: {
		    			selected: function() {
		    				return $scope.selectedFiles;
		    			}
		    		}
		    	});
		    	
		    	modalInstance.result.then(function (selectedFiles) {
		    		$scope.selectedFiles = selectedFiles;
			    });
		    	
			}
			
		});
		
		
		/********************
		 * When selectedFiles is modified (presumabaly from FilenameEditor), fire this code
		 ********************/
		$scope.$watch("selectedFiles",function() {
			if ($scope.selectedFiles.length > 0 && $scope.columnDefs != null) {
				
				var promise = $q.when(null);
				for (var i=0; i<$scope.selectedFiles.length;i++) {
					
						//Create paramter list.  Column defs + file names
						var params = {};
						params.inputFile = $scope.selectedFiles[i].file.name;
						params.outputFile = $scope.selectedFiles[i].outname;
						
						//This will be tied to build going forward!!
						params.genome = "hg19";
						params.analysisID = $scope.$parent.project.id;
						
						//Check to see if there are any matching files in list (match on name only)
						var index = -1;
						for (var j=0; j<$scope.parsedFiles.length; j++) {
							if (params.outputFile == $scope.parsedFiles[j].name) {
								index = j;
								$scope.parsedFiles[j].state = "started";
								$scope.parsedFiles[j].size = null;
							}
						}
						
						//If no match, create new file object.
						if (index == -1) {
							var f = {name: params.outputFile, state: "started"};
							index = $scope.parsedFiles.length;
							$scope.parsedFiles.push(f);
						}
						
						//add columndefs to parameters.
						for (var k=0; k<$scope.columnDefs.length; k++) {
							params[$scope.columnDefs[k].name] = $scope.columnDefs[k].index;
						}
						
						(function(params,index) {
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
						}(params,index));
				}
				
				//When parsing is finished, clear out objects
				promise.then(function() {
					$scope.selectedFiles = [];
					$scope.columnDefs = null;
				});
			}	
		});
		
		
		/********************
		 * Load existing uploaded/parsed files
		 ********************/
		$scope.loadExisting = function(type) {
			var collection = [];
			$http({
				url: "submit/upload/load",
				method: "GET",
				params: {type : type, analysisID: $scope.$parent.project.id}
			}).success( function(data) {
        		for (var i = 0; i < data.files.length; i++) {
        			//Set selected
    				data.files[i].selected = false;
        			collection.push(data.files[i]);
            	}
        	});
			return collection;
		};
		
		/********************
		 * Display error message in modal
		 ********************/
		$scope.showError = function(file) {
			$modal.open({
	    		templateUrl: 'app/common/error.html',
	    		controller: 'ErrorController',
	    		resolve: {
	    			file: function() {
	    				return file;
	    			}
	    		}
	    	});
		};
		
		
		
		/********************
		 * Load files once a project is selected
		 ********************/
        $scope.$parent.$watch('project',function() {
        	if ($scope.$parent.project.id > 0) {
        		$scope.$parent.uploadedFiles = $scope.loadExisting("imported");
            	$scope.parsedFiles = $scope.loadExisting("parsed");	
        	}
        	        	
        },true);
        
  
	
}]);