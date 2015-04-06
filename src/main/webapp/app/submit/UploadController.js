'use strict';


var upload  = angular.module('upload',  ['ui.bootstrap', 'angularFileUpload','filters', 'services', 'directives','fneditor','chosen','ngProgress','dialogs.main','error','cgBusy']);

angular.module("upload").controller("UploadController", ['$scope','$upload','$http','$modal','$q','ngProgress','dialogs','$timeout','$location',
                                                      
	function($scope, $upload, $http, $modal, $q, ngProgress, dialogs, $timeout, $location) {

		$scope.selectAllImport = false;
		$scope.columnDefs = null;
		
		
		$scope.selectedFiles = [];
		$scope.selectedAnalysisType = null;
		
		$scope.uploadSelected = false;
		
		$scope.uploadPromise = null;
		$scope.uploadDeferred = null;
		$scope.uploadingFiles = false;
		
		$scope.importPromise = null;
		$scope.importDeferred = null;
		$scope.importingFiles = false;
		
		$scope.uploadOrderByField = "idFileUpload";
		$scope.uploadReverseSort = true;
		
		$scope.importStarted = false;
		$scope.importFinished = false;
		
		$scope.uploadSelectedIds = [];
		
		
		/***************************************
		 * *************************************
		 * 
		 *      Interrupts
		 * 
		 * *************************************
		 ***************************************/
	    
	    $scope.$on('$locationChangeStart', function( event, next, current ) {
	    	if ($scope.uploadingFiles) {
	    		event.preventDefault();
	    		var dialog = dialogs.confirm("Page Navigation","File upload in progress, are you sure you want to leave this page?  All incomplete uploads will be removed from the database.");
	        	dialog.result.then(function() {
	        		$timeout(function() {
	        			$scope.cancelUpload();
	        			$scope.cleanUploadedFiles();
	        			$location.path(next.substring($location.absUrl().length - $location.url().length));
	                    $scope.$apply();
	        		});
	        		$scope.navigationOk = true;
	        	});
	    	} else if ($scope.importingFiles) {
	    		event.preventDefault();
	    		var dialog = dialogs.confirm("Page Navigation","File import in progress, are you sure you want to leave this page?  All incomplete imports will be removed from the database.");
	        	dialog.result.then(function() {
	        		$timeout(function() {
	        			$scope.cancelImport();
	        			$scope.cleanUploadedFiles();
	        			$location.path(next.substring($location.absUrl().length - $location.url().length));
	                    $scope.$apply();
	        		});
	        		$scope.navigationOk = true;
	        	}); 
	    	} else {
	    		$scope.cancelUpload();
	    		$scope.cancelImport();
    			$scope.cleanUploadedFiles();
	    	}
	    });
	    
	
	    
	    window.onbeforeunload = function() {
	    	$scope.cancelUpload();
	    	$scope.cancelImport();
			$scope.cleanUploadedFiles();
	    }
		
		
	    /***************************************
		 * *************************************
		 * 
		 *      File uploading
		 * 
		 * *************************************
		 ***************************************/
		$scope.onFileSelect = function(files) {
			if (files.length == 0) {
				return 0;
			}
			
			console.log("Starting");
			
			//Turn on uploading flag
			$scope.uploadingFiles = true;
			
			//create deferred promise
			var deferred = $q.defer();
			var promise = deferred.promise;
			
			promise = $scope.cleanUploadedFilesPromise(promise);
			promise = $scope.checkUploadedFileNames(files, promise);
			promise = $scope.createFileUploads(files, promise);
			promise = $scope.processAllFiles(files, promise);
			promise = promise.then(function() {
				console.log("GOOD");
				$scope.complete = 100;
				cleanupAfterUpload();
			},function() {
				console.log("BAD");
				$scope.complete = 0;
				cleanupAfterUpload();
			});
			
			deferred.resolve();
		};
		
		var cleanupAfterUpload = function() {
			$scope.uploadingFiles = false;
			$scope.loadExisting("IMPORTED");
			$scope.uploadPromise = null;
			$scope.uploadDeferred = null;
		}
		
		$scope.cancelUpload = function() {
			if ($scope.uploadDeferred != null) {
				$scope.uploadDeferred.resolve();
			}
		}
		
		$scope.checkUploadedFileNames = function(files, promise) {
			var badFiles = [];
			var existPromises = [];
			
			promise = promise.then(function() {
				for (var i=0;i<files.length;i++) {
					var existPromise = $http({
						url: "submit/doesRawUploadExist",
						method: "GET",
						params: {idProject : $scope.projectId, fileName: files[i].name, index: i}
					}).success(function(data, status, headers, config) {
						if (data.found) {
							badFiles.push(files[config.params["index"]]);
						} 
					});
					existPromises.push(existPromise);
				}
				
				return $q.all(existPromises).then(function(data) {
					var fileDeferred = $q.defer();
				
					if (badFiles.length > 0) {
						fileDeferred.reject("Duplicate File Names");
						var warningMessage = "<p>The following files have already been uploaded and won't be overwritten.</p>";
						warningMessage += "<ul>";
						for (var idx in badFiles) {
							warningMessage += "<li>" + files[idx].name + "</li>";
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
		
		$scope.processAllFiles = function(files, promise) {
			
			//Initialize upload status variables.
			$scope.complete = 0;
			$scope.totalGlobalSize = 0;
			$scope.currGlobalSize = 0;
			$scope.currIndSize = [];
			$scope.totalIndSize = [];
			
			//Calculate global size and initialize local sizes
			for (var i=0; i<files.length; i++) {
				$scope.totalGlobalSize += files[i].size;
				$scope.totalIndSize.push(files[i].size);
				$scope.currIndSize.push(0);
			}

			promise = promise.then(function() {
				var deferred = $q.defer();
				var uploadPromise = deferred.promise;
				$scope.uploadDeferred = $q.defer();
				
				for (var i=0; i<files.length; i++) {
					//initialize variables
					var file = files[i];
					var index = -1;
					
					//Check to see if there are any matching files in list (match on name only)
					for (var j=0; j<$scope.files.uploadedFiles.length; j++) {
						if (file.name == $scope.files.uploadedFiles[j].name || (file.name + ".gz") == $scope.files.uploadedFiles[j].name ) {
							uploadPromise = $scope.uploadFile(i, j, file, uploadPromise);	
						}
					}
				}
				
				$scope.uploadPromise = uploadPromise;
				deferred.resolve();
				return uploadPromise;
			});
			
			return promise;
		};

		
	$scope.createFileUploads = function(files, promise) {
		for (var i=0; i<files.length; i++) {
			var file = files[i];
			(function(file) {
				promise = promise.then(function() {
					return $http({
						url: "submit/createUploadFile",
						method: "POST",
						params : {name: file.name, size: file.size, idProject: $scope.projectId }
					}).success(function(data) {
						$scope.files.uploadedFiles.push(data);
					}).error(function(data) {
						console.log("Could not create upload file");
					});
				})
			}(file))
		}
		return promise;
	}

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
			(function(i,fileIdx, index) {
				promise = promise.then(function() {
					return $upload.upload({
						url: "submit/uploadchunk",
						file: fileChunks[i],
						params : {index: i, total: fileChunks.length, idProject: $scope.project.idProject, idFileUpload: $scope.files.uploadedFiles[index].idFileUpload, name: $scope.files.uploadedFiles[index].name},
						timeout : $scope.uploadDeferred.promise,
					}).progress(function(evt) {
						//$scope.complete = (loaded + evt.loaded) / file.size * 100;
						if ($scope.files.uploadedFiles)
						$scope.files.uploadedFiles[index].complete = 100 * (evt.loaded + $scope.currIndSize[fileIdx]) / $scope.totalIndSize[fileIdx];
										        
						$scope.currGlobalSize = 0;
						for (var j = 0; j < $scope.currIndSize.length; j++) {
							$scope.currGlobalSize += $scope.currIndSize[j];
						}
						$scope.complete = 100.0 * (evt.loaded + $scope.currGlobalSize) / $scope.totalGlobalSize;
						
						
					}).success(function(data, status, headers, config) {
						if (data.finished) {
							$scope.files.uploadedFiles[index].complete = 100;
							$scope.files.uploadedFiles[index].state = data.state;
							$scope.files.uploadedFiles[index].message = data.message;
							$scope.files.uploadedFiles[index].selected = false;
							$http({
								url : "submit/finalizeFileUpload",
								method: "PUT",
								params: {idFileUpload: config.params["idFileUpload"], state: data.state},
							})
						}
						$scope.currIndSize[fileIdx] += fileChunks[i].size;
					}).error(function(data, status, headers, config) {
						if (data != null) {
							$scope.files.uploadedFiles[index].state = "FAILURE";
							$scope.files.uploadedFiles[index].message = data.message;
							$http({
								url : "submit/finalizeFileUpload",
								method: "PUT",
								params: {idFileUpload: config.params["idFileUpload"], state: data.state},
							})
						}  else {
							$scope.files.importedFiles[index].message = "Unknown failure, please contact the Biominer team";
							$scope.files.importedFiles[index].state = "FAILURE";
							$http({
								url : "submit/finalizeFileUpload",
								method: "PUT",
								params: {idFileUpload: config.params["idFileUpload"], state: "FAILURE"},
							})
						}
						$scope.complete = 0;
					    
					});
				});
			})(i,fileIdx, index);
		}
		return promise;
	};
			
		
		
		
		
		/***************************************
		 * *************************************
		 * 
		 *       File Parsing methods
		 * 
		 * *************************************
		 ***************************************/
		
		/** 
		 * This is the first method called when the 'import' button is pushed.  It creates a scope variable that
		 * contains all of the uploaded files selected when import was pushed.  It also creates temporary output
		 * file names that will be displayed in the file editor.  The method then calls the file preview
		 * object that allows the user to select the columns necessary for import.
		 */
		$scope.parse = function() {
			//check to see if any of the files are invalid
			var badFiles = [];
			for (var i=0; i<$scope.files.uploadedFiles.length; i++) {
				if ($scope.files.uploadedFiles[i].selected == true && ($scope.files.uploadedFiles[i].state == "FAILURE" || $scope.files.uploadedFiles[i].state == "INCOMPLETE")) {
					badFiles.push($scope.files.uploadedFiles[i].name);
					$scope.files.uploadedFiles[i].selected = false;
				}
			}
			
			if (badFiles.length > 0) {
				var message = "";
				message += "<p>The following selected files are incomplete or failed to upload properly, so they cannot be imported into Biominer. " +
						" The files have been delesected automatically.</p>";
				message += "<br/>";
				message += "<ul>";
				for (var i=0;i<badFiles.length;i++) {
					message += "<li>" + badFiles[i] + "</li>";
				}
				message += "</ul>";
			
				dialogs.error("Cannot Parse Bad Upload",message,null);
				
				return;
			}
			
			var deferred = $q.defer();
			var promise = deferred.promise;
			promise = $scope.cleanUploadedFiles(promise);
			
			promise = promise.then(function() {
				$scope.selectedFiles = [];
				for (var i=0; i<$scope.files.uploadedFiles.length;i++) {
					if ($scope.files.uploadedFiles[i].selected == true) {
						
						var outname = "";
						var inputname = $scope.files.uploadedFiles[i].name;
						if (inputname.indexOf(".gz",inputname.length-3) !== -1) {
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
					    				return config.params.name;
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
			});
			deferred.resolve();
		};
		
		/**
		 * This method is called once the file definitions are set in the parse method. 
		 * This method presents the user with a list of the files to be parsed and allows
		 * them to change the names.  Once the user is satisfied with the names, the
		 * method calls the parseDriver() method.
		 */
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
	    		$scope.parseDriver();
		    });
		};
		
		
		
		/**
		 * This method drives the file parsing.  Much like the upload methods, this method creates
		 * a long set of chained promises and then starts them.  the chain includes: file cleanup,
		 * database creation and the actual parsing. When all of this finishes, a cleanup method 
		 * is called.
		 **/
		$scope.parseDriver = function() {
			var deferred = $q.defer();
			var promise = deferred.promise;
			
			$scope.importingFiles = true;
			
			promise = $scope.createFileImports(promise);
			promise = $scope.callParse(promise);
			
			//When parsing is finished, clear out objects
			promise.then(function() {
				$scope.importFinished = true;
				$scope.cleanupAfterParse();
			},function() {
				$scope.cleanupAfterParse();
			});
			
			deferred.resolve();
		};
		
		
		/**
		 * This method calls the spring parse method 
		 */
		$scope.callParse = function(promise) {
			promise = promise.then(function() {
				var parseDeferred = $q.defer();
				var parsePromise = parseDeferred.promise;
				
				$scope.importDeferred = $q.defer();
				$scope.importStarted = true;
				$scope.importFinished = false;
				for (var i=0; i<$scope.selectedFiles.length;i++) {
					
					//Create paramter list.  Column defs + file names
					var params = {};
					
					//add columndefs to parameters.
					for (var k=0; k<$scope.columnDefs.length; k++) {
						params[$scope.columnDefs[k].name] = $scope.columnDefs[k].index;
					}
					
					//Add other information
					params.inputFile = $scope.selectedFiles[i].file.name;
					params.outputFile = $scope.selectedFiles[i].outname;
					params.idParent = $scope.selectedFiles[i].file.idFileUpload;
					params.build = $scope.project.organismBuild.idOrganismBuild;
					params.idProject = $scope.projectId;
					params.idAnalysisType = $scope.selectedAnalysisType.idAnalysisType;
					
					var index = -1;
					for (var j=0; j<$scope.files.importedFiles.length; j++) {
						if (params.outputFile == $scope.files.importedFiles[j].name) {
							params.idFileUpload = $scope.files.importedFiles[j].idFileUpload;
							index = j;
						}
					}
					
					
					parsePromise = parsePromise.then(function() {
						var missingDeferred = $q.defer();
						//Throw error if imported file can't be found
						if (index == -1) {
							dialogs.error("Missing Import","Can't find file in list: " + params.outputFile);
							missingDeferred.reject();
						} else {
							missingDeferred.resolve();
						}
						return missingDeferred.promise;
					});
					
					
					if ($scope.selectedAnalysisType.type == "ChIPSeq" || $scope.selectedAnalysisType.type == "Methylation") {
						(function(params,index) {				
							parsePromise = parsePromise.then(function() {
								$scope.files.importedFiles[index].complete = 50;
								return $http({
									url: "submit/parse/chip",
									method: "POST",
									params: params,
									timeout: $scope.importDeferred.promise,
								}).success(function(data,status,headers,config) {
									$scope.files.importedFiles[index] = data;
									if (data.state == "FAILURE") {
										$scope.files.importedFiles[index].complete = 0;
									} else {
										$scope.files.importedFiles[index].complete = 100;
									}
									$http({
										url : "submit/finalizeFileUpload",
										method: "PUT",
										params: {idFileUpload: config.params["idFileUpload"], state: data.state},
									})
								}).error(function(data,status,headers,config) {
									if (data != null) {
										$scope.files.importedFiles[index].message = data.message;
										$scope.files.importedFiles[index].state = data.state;
										$http({
											url : "submit/finalizeFileUpload",
											method: "PUT",
											params: {idFileUpload: config.params["idFileUpload"], state: data.state},
										})
									} else {
										$scope.files.importedFiles[index].message = "Unknown failure, please contact the Biominer team";
										$scope.files.importedFiles[index].state = "FAILURE";
										$http({
											url : "submit/finalizeFileUpload",
											method: "PUT",
											params: {idFileUpload: config.params["idFileUpload"], state: "FAILURE"},
										})
									}
								});
							});
						}(params,index));
					} else if ($scope.selectedAnalysisType.type == "RNASeq") {
						(function(params,index) {
							parsePromise = parsePromise.then(function() {
								$scope.files.importedFiles[index].complete = 50;
								return $http({
									url: "submit/parse/rnaseq",
									method: "POST",
									params: params,
									timeout: $scope.importDeferred.promise,
								}).success(function(data,status,headers,config) {
									$scope.files.importedFiles[index] = data;
									if (data.state == "FAILURE") {
										$scope.files.importedFiles[index].complete = 0;
									} else {
										$scope.files.importedFiles[index].complete = 100;
									}
									$http({
										url : "submit/finalizeFileUpload",
										method: "PUT",
										params: {idFileUpload: config.params["idFileUpload"], state: data.state},
									})
								}).error(function(data,status,headers,config) {
									if (data != null) {
										$scope.files.importedFiles[index].message = data.message;
										$scope.files.importedFiles[index].state = data.state;
										$http({
											url : "submit/finalizeFileUpload",
											method: "PUT",
											params: {idFileUpload: config.params["idFileUpload"], state: data.state},
										})
									} else {
										$scope.files.importedFiles[index].message = "Unknown failure, please contact the Biominer team";
										$scope.files.importedFiles[index].state = "FAILURE";
										$http({
											url : "submit/finalizeFileUpload",
											method: "PUT",
											params: {idFileUpload: config.params["idFileUpload"], state: "FAILURE"},
										})
									}
									$scope.files.importedFiles[index].complete = 0;
								});
							});
						}(params,index));
					} else if ($scope.selectedAnalysisType.type == "Variant") {
						(function(params,index) {
							parsePromise = parsePromise.then(function() {
								$scope.files.importedFiles[index].complete = 50;
								return $http({
									url: "submit/parse/variant",
									method: "POST",
									params: params,
									timeout: $scope.importDeferred.promise,
								}).success(function(data,status,headers,config) {
									$scope.files.importedFiles[index] = data;
									if (data.state == "FAILURE") {
										$scope.files.importedFiles[index].complete = 0;
									} else {
										$scope.files.importedFiles[index].complete = 100;
									}
									$http({
										url : "submit/finalizeFileUpload",
										method: "PUT",
										params: {idFileUpload: config.params["idFileUpload"], state: data.state},
									})
								}).error(function(data,status,headers,config) {
									if (data != null) {
										$scope.files.importedFiles[index].message = data.message;
										$scope.files.importedFiles[index].state = data.state;
										$http({
											url : "submit/finalizeFileUpload",
											method: "PUT",
											params: {idFileUpload: config.params["idFileUpload"], state: data.state},
										})
									} else {
										$scope.files.importedFiles[index].message = "Unknown failure, please contact the Biominer team";
										$scope.files.importedFiles[index].state = "FAILURE";
										$http({
											url : "submit/finalizeFileUpload",
											method: "PUT",
											params: {idFileUpload: config.params["idFileUpload"], state: "FAILURE"},
										})
									}
									$scope.files.importedFiles[index].complete = 0;
								});
							});
						}(params,index));
					}
				}
				
				parseDeferred.resolve();
				$scope.importPromise = parsePromise;
				return parsePromise;
			});
			
			return promise;
		}
		
		/** 
		 * This method is called after parsing is complete.
		 */
		$scope.cleanupAfterParse = function() {
			$scope.importPromise = null;
			$scope.importDeferred = null;
			$scope.selectedFiles = [];
			$scope.columnDefs = null;
			$scope.importingFiles = false;
			for (var i=0;i<$scope.files.importedFiles.length;i++) {
				$scope.files.importedFiles[i].complete = null;
			}
		}
		
		/**
		 * This method stops a parse job.
		 * 
		 */
		$scope.cancelImport = function() {
			if ($scope.importDeferred != null) {
				$scope.importDeferred.resolve();
			}
		}
		
		/**
		 * This method creates the import file objects.
		 */
		$scope.createFileImports = function(promise) {
			for (var i=0; i<$scope.selectedFiles.length; i++) {
				var name = $scope.selectedFiles[i].outname;
				var idParent = $scope.selectedFiles[i].file.idFileUpload;
				(function(name,idParent) {
					promise = promise.then(function() {
						return $http({
							url: "submit/createImportFile",
							method: "POST",
							params : {name: name, idProject: $scope.projectId, idParent: idParent }
						}).success(function(data) {
							$scope.files.importedFiles.push(data);
						}).error(function() {
							console.log("Could not create import file");
						});
					})
				}(name,idParent))
			}
			return promise;
		}
		
		
		 /***************************************
		 * *************************************
		 * 
		 *     Fetchers
		 * 
		 * *************************************
		 ***************************************/
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
		
	
		
		 /***************************************
		 * *************************************
		 * 
		 *      Watchers
		 * 
		 * *************************************
		 ***************************************/
		
		
		/********************
		 * Load files once a project is selected
		 ********************/
        $scope.$parent.$watch('project',function(newValue, oldValue) {
        	if ($scope.projectId > 0 && newValue.idProject != oldValue.idProject) {
        		$scope.files.uploadedFiles = $scope.loadExisting("UPLOADED");
            	$scope.files.importedFiles = $scope.loadExisting("IMPORTED");	
        	}
        	        	
        });
        
        $scope.$watch('files.uploadedFiles',function() {
			var selected = false;
			var newSelectedIds = [];
			
			for (var i=0; i<$scope.files.uploadedFiles.length; i++) {
				if ($scope.files.uploadedFiles[i].selected) {
					newSelectedIds.push($scope.files.uploadedFiles[i].idFileUpload);
					selected = true;
				}
			}
			
			if (selected) {
				$scope.uploadSelected = true;
			} else {
				$scope.uploadSelected = false;
			}
			
			if(newSelectedIds.sort().join(',') != $scope.uploadSelectedIds.sort().join(',')){
			    $scope.uploadSelectedIds = newSelectedIds;
				$scope.importStarted = false;
				$scope.importFinished = false;
				$scope.selectedAnalysisType = null;
			}
			
			
		},true);
        
        
        
    	
        /***************************************
		 * *************************************
		 * 
		 *      Inner table display
		 * 
		 * *************************************
		 ***************************************/
        $scope.displayTable = function(file) {
        	var found = false;
        	if (file.state == "FAILURE") {
        		return false;
        	}
        	for (var i=0; i<$scope.files.importedFiles.length;i++) {
        		if ($scope.files.importedFiles[i].idParent == file.idFileUpload) {
        			found = true;
        			break
        		}
        	}
        	return found;
        	
        }
        
        $scope.filterImports = function(file) {
        	return function(ifile) {
        		if (file.idFileUpload == ifile.idParent) {
        			return true;
        		} else {
        			return false;
        		}
        	}
        }
        
        
		
		/***************************************
		 * *************************************
		 * 
		 *      Delete methods
		 * 
		 * *************************************
		 ***************************************/
		
		/*******************
		 * Delete all failed and complete on the back end, with promise.
		 */
		$scope.cleanUploadedFilesPromise = function(promise) {
			promise = promise.then(function() {
				return $http({
					url: "submit/cleanUploadedFiles",
					method: "DELETE",
					params: {idProject: $scope.projectId}
				}).success(function(data) {
					updateFileList(data);
				});
			})
			
			return promise;
		}
		
		/******************
		 *  Delete all failed/incomplete on back end
		 */
		$scope.cleanUploadedFiles = function() {
			return $http({
				url: "submit/cleanUploadedFiles",
				method: "DELETE",
				params: {idProject: $scope.projectId}
			}).success(function(data) {
				updateFileList(data);
			});
		}
		
		/****************
		 * Update file lists
		 */
		var updateFileList = function(deletedIds) {
			var newUploaded = [];
			var newImported = [];
			for (var i=0; i<$scope.files.uploadedFiles.length; i++) {
				if (deletedIds.indexOf($scope.files.uploadedFiles[i].idFileUpload) == -1) {
					newUploaded.push($scope.files.uploadedFiles[i]);
				}
			}
			for (var i=0; i<$scope.files.importedFiles.length; i++) {
				if (deletedIds.indexOf($scope.files.importedFiles[i].idFileUpload) == -1) {
					newImported.push($scope.files.importedFiles[i]);
				}
			}
			$scope.files.importedFiles = newImported;
			$scope.files.uploadedFiles = newUploaded;
		}
		
		/*******************
		 * Delete files
		 */
		$scope.deleteFileUpload = function(file) {
			if (file.type == "IMPORTED" && file.isAnalysisSet) {
				var message = "";
				message += "<p>The file: <strong>" + file.name + "</strong> is associated with an analyses and can't be deleted. The analysis must be deleted before removing the imported file.</p>";
				dialogs.error("Can't Delete Selected Files",message,null);
				return;
			}
			
			var fileList = [];
			for (var i=0; i<$scope.files.importedFiles.length;i++) {
				if (file.idFileUpload == $scope.files.importedFiles[i].idParent) {
					fileList.push($scope.files.importedFiles[i].name);
				}
			}
			
			if (fileList.length > 0) {
				var message = "";
				message += "<p>The file: <strong>" + file.name + "</strong> is associated with imported files. These files must be deleted before deleting the original data.</p>";
				message += "<br/>";
				message += "<ul>";
				for (var i=0;i<fileList.length;i++) {
					message += "<li>" + fileList[i] + "</li>";
				}
				message += "</ul>";
			
				dialogs.error("Can't Delete File",message,null);
				return;
			}
			
			$http({
				url: "submit/upload/deleteFileUpload",
				method: "DELETE",
				params: {idFileUpload: file.idFileUpload}
			}).success(function (data, status, headers, config) {
				updateFileList([config.params["idFileUpload"]]);
			});
		}
        
        
		/***************************************
		 * *************************************
		 * 
		 *      UI controls
		 * 
		 * *************************************
		 ***************************************/
        
        $scope.hideUploadControls = function(file) {
    		file.show = false;
    	};
    	
    	$scope.showUploadControls = function(file) {
    		file.show = true;
    	};
    	
    	$scope.hideImportControls = function(file) {
    	    file.show = false;
    	};
    	
    	$scope.showImportControls = function(file) {
    		file.show = true;
    	};
    	
    	$scope.showFileError = function(file) {
    		dialogs.notify("File Warning",file.message,null);
    	};
    	
    	$scope.showFileWarning = function(file) {
    		dialogs.error("File Error",file.message, null);
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
  
	
}]);