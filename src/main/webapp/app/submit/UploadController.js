'use strict';


var upload  = angular.module('upload',  ['ui.bootstrap', 'angularFileUpload','filters', 'services', 'directives','fneditor','chosen','ngProgress','dialogs.main','error']);

angular.module("upload").controller("UploadController", ['$scope','$upload','$http','$modal','$q','ngProgress','dialogs',
                                                      
	function($scope, $upload, $http, $modal, $q, ngProgress, dialogs) {

		$scope.selectAllParse = false;
		$scope.selectAllImport = false;
		$scope.columnDefs = null;
		
		
		$scope.selectedFiles = [];
		$scope.selectedAnalysisType = null;
		
		$scope.uploadSelected = false;
		$scope.importedSelected = false;
		
		
		
		
		/********************
		 * Upload files!
		 ********************/
		$scope.onFileSelect = function(files) {
			var validFiles = [];
			var badFiles = [];
			var promises = [];
			
			console.log(files);
			
			for (var i=0;i<files.length;i++) {
				var promise = $http({
					url: "submit/doesRawUploadExist",
					method: "GET",
					params: {idProject : $scope.projectId, fileName: files[i].name, index: i}
				}).success(function(data, status, headers, config) {
					if (data.found) {
						console.log("BAD");
						badFiles.push(files[config.params["index"]]);
					} else {
						console.log("Good");
						validFiles.push(files[config.params["index"]]);
					}
				}).error(function(data,status,headers,config) {
					console.log("error");
				});
				promises.push(promise);
			}
			
			$q.all(promises).then(function(data) {
				if (badFiles.length > 0) {
					var warningMessage = "<p>The following files have already been uploaded and won't be overwritten.</p>";
					warningMessage += "<ul>";
					for (var idx in badFiles) {
						warningMessage += "<li>" + files[idx].name + "</li>";
					}
					warningMessage += "</ul>";
					 
					dialogs.error("Duplicate file names",warningMessage);
				}
				$scope.processValidFiles(validFiles);
				files = null;
			});
			
			
		};
		
		
		$scope.processValidFiles = function($files) {
			
			//Initialize upload status variables.
			$scope.complete = 0;
			$scope.totalGlobalSize = 0;
			$scope.currGlobalSize = 0;
			$scope.currIndSize = [];
			$scope.totalIndSize = [];
			
			//Calculate global size and initialize local sizes
			for (var i=0; i<$files.length; i++) {
				$scope.totalGlobalSize += $files[i].size;
				$scope.totalIndSize.push($files[i].size);
				$scope.currIndSize.push(0);
			}

			var promise = $q.all(null);
			for (var i=0; i<$files.length; i++) {
				//initialize variables
				var file = $files[i];
				var index = -1;
				
				//Check to see if there are any matching files in list (match on name only)
				for (var j=0; j<$scope.files.uploadedFiles.length; j++) {
					if (file.name == $scope.files.uploadedFiles[j].name || (file.name + ".gz") == $scope.files.uploadedFiles[j].name ) {
						index = j;
						$scope.files.uploadedFiles[j].state = "started";
						$scope.files.uploadedFiles[j].complete = 0;
					}
				}
				
				//If no match, create new file object.
				if (index == -1) {
					var f = {name: file.name, state: "started", complete: 0};
					index = $scope.files.uploadedFiles.length;
					$scope.files.uploadedFiles.push(f);
				}
					
				promise = $scope.uploadFile(i,index, file, promise);
			}
		};


	$scope.uploadFile = function(fileIdx,index,file,promise) {
		var max = 10000000;
	
		var fileChunks = [];
		
		if (file.size > max) {
			for (var i=0;i<file.size;i+=max)
			fileChunks.push(file.slice(i,i+max));
		}
		else {
			fileChunks.push(file.slice(0,file.size));
		}
		
		for (var i=0; i<fileChunks.length; i++) {
			(function(i,fileIdx) {
				promise = promise.then(function() {
					return $upload.upload({
						url: "submit/uploadchunk",
						file: fileChunks[i],
						params : {index: i, total: fileChunks.length, name: file.name, idProject: $scope.project.idProject},
					}).progress(function(evt) {
						//$scope.complete = (loaded + evt.loaded) / file.size * 100;
			
						$scope.files.uploadedFiles[index].complete = 100 * (evt.loaded + $scope.currIndSize[fileIdx]) / $scope.totalIndSize[fileIdx];
										        
						$scope.currGlobalSize = 0;
						for (var j = 0; j < $scope.currIndSize.length; j++) {
							$scope.currGlobalSize += $scope.currIndSize[j];
						}
						$scope.complete = 100.0 * (evt.loaded + $scope.currGlobalSize) / $scope.totalGlobalSize;
					
						
					}).success(function(data) {
						if (i==fileChunks.length-1) {
							if (data.state != "SUCCESS") {
								//Only set message on failure
								$scope.files.uploadedFiles[index].message = data.message;
								$scope.files.uploadedFiles[index].state = "FAILURE";
							} else {
								//Set everything on success
								$scope.files.uploadedFiles[index] = data;
								
							}
							$scope.files.uploadedFiles[index].selected = false;
							
							
						}
						$scope.currIndSize[fileIdx] += fileChunks[i].size;
					}).error(function(data) {
					    $scope.files.uploadedFiles[index].status = "FAILURE";
						$scope.uploadedFiles[index].message = data.message;
						$scope.complete = 0;
					});
				});
			})(i,fileIdx);
		}
		return promise;
	};
			
		/********************
		 * Select/Deselect all
		 ********************/
		$scope.clickSelected = function(collection,status) {
			status = !status;
			for (var i=0; i < collection.length;i++) {
				collection[i].selected = status;
			}
		};
		
		/********************
		 * Delete selected files
		 ********************/
		$scope.deleteSelected = function(collection) {
			//Make sure files are unbound
			var fileList = [];
			
			for (var i=0; i < collection.length; i++) {
				var file = collection[i];
				if (file.selected == true && file.isAnalysisSet) {
					fileList.push(file);
				}
			}
			
			if (fileList.length > 0) {
				var message = "";
				message += "<p>The following files are associated with existing analyses and can't be deleted. Please delete the appropriate analyses and try again.</p>";
				message += "<br/>";
				message += "<ul>";
				for (var i=0;i<fileList.length;i++) {
					message += "<li>" + fileList[i].name + "</li>";
				}
				message += "</ul>";
			
				dialogs.error("Can't Delete Selected Files",message,null);
				return;
			}
			
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
							params: {file: file.name, type: file.type, idProject: $scope.$parent.projectId}
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
		 * When import button is pushed, generate a file preview and selection modal
		 ********************/
		$scope.parse = function() {
			$scope.selectedFiles = [];	
			for (var i=0; i<$scope.files.uploadedFiles.length;i++) {
				if ($scope.files.uploadedFiles[i].selected == true) {
					var outname = "";
					var inputname = $scope.files.uploadedFiles[i].name;
					if (inputname.indexOf(".gz",this.length-3) !== -1) {
						outname = inputname.substring(0,inputname.length-3) + ".PARSED.gz";
					} else {
						outname = inputname + ".PARSED";
					}
		
					var sf = {file : $scope.files.uploadedFiles[i], outname: outname};
					$scope.selectedFiles.push(sf);
				}
			}
				
			if ($scope.selectedFiles.length > 0) {
				if ($scope.selectedAnalysisType.type == "Variant") {
					$scope.columnDefs = {"variant": "variant"};
					$scope.setFilenames();
				} else {
					$http({
						url: "submit/parse/preview/",
						method: "GET",
						params: {name: $scope.selectedFiles[0].file.name, idProject: $scope.$parent.projectId}
					}).success(function(data,status,headers,config) {
				    	var modalInstance = $modal.open({
				    		templateUrl: 'app/submit/previewWindow.html',
				    		controller: 'PreviewWindowController',
				    		windowClass: 'preview-dialog',
				    		resolve: {
				    			filename: function() {
									//return $scope.selectedFiles[0].file.name;				    			
				    				return config.params.filename;
				    			},
				    			previewData: function() {
				    				return data.previewData;
				    			},
								analysisType: function() {
									return $scope.selectedAnalysisType;
								}
				    		}
				    	});
				    	
				    	modalInstance.result.then(function (setColumns) {
				    		$scope.columnDefs = setColumns;
				    		$scope.setFilenames();
					    });
					   
					}).error(function(data) {
						console.log("ARG!");
					});
				}
			}	
		};
		
		/********************
		 * When columnDefs is modified (presumably from import), fire this code
		 ********************/
		$scope.setFilenames = function() {
			var modalInstance = $modal.open({
	    		templateUrl: 'app/submit/filenameEditor.html',
	    		controller: 'FilenameEditorController',
	    		windowClass: 'filename-dialog',
	    		resolve: {
	    			selected: function() {
	    				return $scope.selectedFiles;
	    			},
	    			idProject: function() {
	    				return $scope.project.idProject;
	    			}
	    		}
	    	});
	    	
	    	modalInstance.result.then(function (selectedFiles) {
	    		$scope.selectedFiles = selectedFiles;
	    		$scope.callParser();
		    });
		};
		
		
		
		/********************
		 * When selectedFiles is modified (presumably from FilenameEditor), fire this code
		 ********************/
		$scope.callParser = function() {
			var promise = $q.when(null);
			for (var i=0; i<$scope.selectedFiles.length;i++) {
				
				//Create paramter list.  Column defs + file names
				var params = {};
				params.inputFile = $scope.selectedFiles[i].file.name;
				params.outputFile = $scope.selectedFiles[i].outname;
				params.idFileUpload = $scope.selectedFiles[i].file.idFileUpload;
				
				//This will be tied to build going forward!!
				params.build = $scope.project.organismBuild.idOrganismBuild;
				params.analysisID = $scope.$parent.projectId;
				params.idAnalysisType = $scope.selectedAnalysisType.idAnalysisType;
				
				//Check to see if there are any matching files in list (match on name only)
				var index = -1;
				for (var j=0; j<$scope.files.importedFiles.length; j++) {
					if (params.outputFile == $scope.files.importedFiles[j].name) {
						index = j;
						$scope.files.importedFiles[j].state = "started";
						$scope.files.importedFiles[j].size = null;
					}
				}
				
				//If no match, create new file object.
				if (index == -1) {
					var f = {name: params.outputFile, state: "started"};
					index = $scope.files.importedFiles.length;
					$scope.files.importedFiles.push(f);
				}
				
				//add columndefs to parameters.
				for (var k=0; k<$scope.columnDefs.length; k++) {
					params[$scope.columnDefs[k].name] = $scope.columnDefs[k].index;
				}
				
				
				if ($scope.selectedAnalysisType.type == "ChIPSeq" || $scope.selectedAnalysisType.type == "Methylation") {
					(function(params,index) {
						ngProgress.start();					
						promise = promise.then(function() {
							return $http({
								url: "submit/parse/chip",
								method: "POST",
								params: params
							}).success(function(data) {
								data.selected = false;
								$scope.files.importedFiles[index] = data;
								ngProgress.complete();
							}).error(function(data) {
								ngProgress.reset();								
							});
						});
					}(params,index));
				} else if ($scope.selectedAnalysisType.type == "RNASeq") {
					(function(params,index) {
						ngProgress.start();
						
						promise = promise.then(function() {
							return $http({
								url: "submit/parse/rnaseq",
								method: "POST",
								params: params
							}).success(function(data) {
								data.selected = false;
								$scope.files.importedFiles[index] = data;
								ngProgress.complete();
							}).error(function(data) {
								ngProgress.reset();
							});
						});
					}(params,index));
				} else if ($scope.selectedAnalysisType.type == "Variant") {
					(function(params,index) {
						ngProgress.start();
						promise = promise.then(function() {
							return $http({
								url: "submit/parse/variant",
								method: "POST",
								params: params
							}).success(function(data) {
								data.selected = false;
								$scope.files.importedFiles[index] = data;
								ngProgress.complete();
							}).error(function(data) {
								ngProgress.reset();
							});
						});
					}(params,index));
				}
					
			}
			
			//When parsing is finished, clear out objects
			promise.then(function() {
				$scope.selectedFiles = [];
				$scope.columnDefs = null;
			});
		};
		
		
		/********************
		 * Load existing uploaded/parsed files
		 ********************/
		$scope.loadExisting = function(type) {
			var collection = [];
			$http({
				url: "submit/upload/load",
				method: "GET",
				params: {type : type, idProject: $scope.$parent.projectId}
			}).success( function(data) {
        		for (var i = 0; i < data.length; i++) {
        			//Set selected
    				data[i].selected = false;
        			collection.push(data[i]);
            	}
        	});
			return collection;
		};
		
		
		$scope.$watch('files.importedFiles',function() {
			var selected = false;
			for (var i=0; i<$scope.files.importedFiles.length; i++) {
				
				if ($scope.files.importedFiles[i].selected) {
					selected = true;
				}
			}
			if (selected) {
				$scope.importedSelected = true;
			} else {
				$scope.importedSelected = false;
			}
		},true); 
	
		$scope.$watch('files.uploadedFiles',function() {
			var selected = false;
			for (var i=0; i<$scope.files.uploadedFiles.length; i++) {
				if ($scope.files.uploadedFiles[i].selected) {
					selected = true;
				}
			}
			if (selected) {
				$scope.uploadSelected = true;
			} else {
				$scope.uploadSelected = false;
			}
		},true);
		
		
		
		/********************
		 * Load files once a project is selected
		 ********************/
        $scope.$parent.$watch('project',function() {
        	if ($scope.$parent.project.idProject > 0) {
        		$scope.files.uploadedFiles = $scope.loadExisting("UPLOADED");
            	$scope.files.importedFiles = $scope.loadExisting("IMPORTED");	
        	}
        	        	
        });
        
        
        /********************
    	 * Display error message in modal
    	 ********************/
    	$scope.showFileError = function(file) {
    		dialogs.notify("File Warning",file.message,null);
    	};
    	
    	$scope.showFileWarning = function(file) {
    		dialogs.error("File Error",file.message, null);
    	};
        
  
	
}]);