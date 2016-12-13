'use strict';

/**
 * UserAdminController
 * @constructor
 */
var useradmin = angular.module('useradmin', ['angularFileUpload','ui.mask','ui.validate','filters','directives','services','ngProgress','dialogs.main','error','cgBusy']);

useradmin.controller("UserAdminController", ['$rootScope','$scope','$http','$location','$window','$uibModal','$timeout','$upload','DynamicDictionary',
                                                               'StaticDictionary','ngProgress','dialogs','$q',
                                                      
function($rootScope, $scope, $http, $location, $window, $uibModal, $timeout, $upload, DynamicDictionary, StaticDictionary, ngProgress, dialogs,$q) {
	
	// approve user variables
	$scope.guid = "";
	$scope.iduser = "";
	$scope.deleteuser = "";
	
	//user table variables
	$scope.userLimit = 10;
	$scope.userCurrentPage = 0;
	$scope.userReverseSort = false;
	$scope.userOrderByField = "last";
	
	//lab table variables
	$scope.labLimit = 10;
	$scope.labCurrentPage = 0;
	$scope.labReverseSort = false;
	$scope.labOrderByField = "last";
	
	//tabset variables
	$scope.currentTab = 0;
	

	//Model data
	$scope.labs = [];
	$scope.organismBuildList = [];
	$scope.organismList = [];
	$scope.selectedUsers = [];
	$scope.selectedLab;
	$scope.liftoverFileList = [];
	$scope.liftoverSupportList = [];
	

	$scope.selectedTf;
	$scope.tfList = [];
	$scope.tfEditMode = false;
	
	//Select all users
	$scope.selectAllUsers = false;
	
	//Annotation promises and deferred objects
	$scope.uploadGenomePromise = null;
	$scope.uploadTranscriptPromise = null;
	$scope.uploadAnnotationPromise = null;
	$scope.parseAnnotationPromise = null;
	$scope.deletePromise = null;
	$scope.uploadTfPromise = null;
	$scope.addTfPromise = null;
	$scope.uploadConvPromise = null;
	$scope.parseConvPromise = null;
	
	$scope.columnDefs = null;
	
	
	/***************************************
	 * *************************************
	 * 
	 *      Interrupts
	 * 
	 * *************************************
	 ***************************************/
    
    $scope.$on('$locationChangeStart', function( event, next, current ) {
    	if ($scope.uploadGenomePromise != null || $scope.uploadTranscriptPromise != null || $scope.uploadAnnotationPromise != null) {
    		event.preventDefault();
    		var dialog = dialogs.confirm("Page Navigation","File upload in progress, are you sure you want to leave this page?  The upload should complete even if you leave.");
        	dialog.result.then(function() {
        		$timeout(function() {
        			$location.path(next.substring($location.absUrl().length - $location.url().length));
                    $scope.$apply();
        		});
        	});
    	}  else if ($scope.parseAnnotationPromise != null) {
    		event.preventDefault();
    		var dialog = dialogs.confirm("Page Navigation","Annotation parsing in progress, are you sure you want to leave the page? The parsing should complete, even if you leave.");
        	dialog.result.then(function() {
        		$timeout(function() {
        			$location.path(next.substring($location.absUrl().length - $location.url().length));
                    $scope.$apply();
        		});
        	}); 
    	} 
    });
    

  
	
    /***************************************
	 * *************************************
	 * 
	 *     Watchers
	 * 
	 * *************************************
	 ***************************************/
    
     $scope.helpPreamble = "" +
        "<h1>Admin Page</h1>" + 
        "<p>The admin page is used to add/edit users, labs and genome builds.  This page is only visible to administrators.</p>";
	
	 $scope.helpUser = "" +
	    "<h3>User Page</h3>" +
	    "<p>The user page can be used to add new users to Biominer or edit existing users.  New users can be added by clicking on the <strong>Add User</strong> link " +
	    "at the top of the page. Users can be deleted by clicking on the checkbox next to the user name and then clicking on the <strong>Delete User</strong> link. " +
	    "Users can be edited by double clicking on the user row in the table. The table can be sorted by clicking on the column names at the top of the table.</p>" +
	    "<ol>" +
	    "<li><strong>First Name </strong><em>required</em>: First name of the user.</li>" +
	    "<li><strong>Last Name</strong><em>required</em>: Last name of the user.</li>" +
	    "<li><strong>Labs </strong><em>required</em>: List of labs associated with the user.</li>" +
	    "<li><strong>Institutes </strong><em>required</em>: List of institutes associated with the user.</li>" +
	    "<li><strong>Username </strong><em>required</em>: Username used to log in.</li>" +
	    "<li><strong>Password </strong><em>required</em>: Password used to log in.</li>" +
	    "<li><strong>Phone </strong><em>required</em>: User phone number.</li>" +
	    "<li><strong>Email </strong><em>required</em>: User email address.</li> " +
	    "<li><strong>Admin </strong>: Check this box if the person is an admin.  Admins have access to all user, lab and genome information.</li>" +
	    "</ol>";
	 
	 $scope.helpLab = "" +
	    "<h3>Lab Page</h3>" +
	    "<p>The lab page can be used to add new labs to Biominer or edit existing labs.  New labs can be added by clicking on the <strong>Add Lab</strong> " +
	    "link on the top of the page.  New users can be added by clicking on the <strong>Delete Lab</strong> link on the top of the page.  Labs can be " +
	    "edited by double clicking on the lab entry in the table.  The table can be sorted by clicking on the column names at the top of the table.</p> " +
	    "<ol>" +
	    "<li><strong>First Name </strong><em>required</em>: First name of the lab PI.</li>" +
	    "<li><strong>Last Name </strong><em>required</em>: Last name of the lab PI.</li>" +
	    "<li><strong>Email </strong><em>required</em>: Email address of the PI. An email is sent to the PI when a user signs up for an account.</li>" +
	    "<li><strong>Phone </strong><em>requierd</em>: Phone number of the PI.</li>" +
	    "</ol>";
	 
	 $scope.helpGenome = "" +
	    "<h3>Genome Page</h3>" +
	    "<p>The Genome page can be used to add new organisms/builds to Biominer or edit existing organisms/builds.</p>" +
	    "<p>New organisms can be added by clicking on the <strong>Add Organism</strong> link above the organism build table. " +
		"Existing organisms can be edited by double clicking on an organism name in the organism build table.  There currently isn't " +
		"a way to delete existing organisms from the Biominer website.</p>" +
	 	"<p>New organism builds can be added by clicking on the <strong>Add Organism Build</strong> link above the organism build table. " +
		"Existing organisms builds can be edited by double clicking on an organism build name in the organism build table.  Organism builds can" +
		"be deleted by selecting the checkbox to the left of the organism build name and then clickin on the <strong>Delete Organism Build</strong> link above " +
		"the organism build table.  There will be a confirmation message before the build is deleted.</p>" +
	 	"<p>Transcript, genome and annotation files are necessary for queries to work. These can be added " +
		"to a organism build by clicking on the blue plus button next to the appropriate table entry. You can replace one of these files by selecting " +
		"the orange refresh button next to the appropriate entry.  You can delete the file by selecting the red minus button next to the appropriate " +
		"entry.</p>" +
	 	"<p>The transcript file and genome files must be loaded for basic queries.  The annotation file must be loaded " +
		"to search by gene. When the transcript and genome file are selected, Biominer will check to see if they are valid. If a genome can be " +
		"used for basic queries, there will be an empty start next to its name.  If a build can be used for basic queries and gene name searches, " +
		"there will be a filled in star next to its name.</p>";
	 
	 $rootScope.helpMessage = 
		 $scope.helpPreamble +
		 $scope.helpUser +
		 $scope.helpLab +
		 $scope.helpGenome; 
		 
		 

	 /***************************************
	 * *************************************
	 * 
	 *      Load Dictionaries
	 * 
	 * *************************************
	 ***************************************/
	//Static dictionaries. These http calls are cached.
    $scope.getInstituteList = function () {
    	StaticDictionary.getInstituteList().success(function(data) {
    		$scope.institutes = data;
    	});
    };
    
    //Dynamic dictionaries
	$scope.loadLabs = function() {
		DynamicDictionary.loadLabs().success(function(data,status) {
	    	$scope.labs = data;
	    	$scope.loadCounts();
	    });
	};
	
	$scope.loadOrganisms = function() {
		DynamicDictionary.loadOrganisms().success(function(data,status) {
			$scope.organismList = data;
		});
	};
	
	$scope.loadOrganismBuilds = function() {
		DynamicDictionary.loadOrganismBuilds().success(function(data,status) {
			$scope.organismBuildList = data;
		});
	};
	
	$scope.loadTfData = function() {
		DynamicDictionary.loadTfs().success(function(data,status) {
			$scope.tfList = data;
		});
	};
	
	$scope.loadConversionData = function() {
		DynamicDictionary.loadConversions().success(function(data,status) {
			$scope.conversionList = data;
		});
	};
	
	$scope.loadLiftoverChainData = function() {
		DynamicDictionary.loadLiftoverChains().success(function(data,status) {
			$scope.liftoverFileList = data;
		});
	}
	
	$scope.loadLiftoverSupportData = function() {
		DynamicDictionary.loadLiftoverSupports().success(function(data,status) {
			$scope.liftoverSupportList = data;
		});
	}
	
	$scope.loadTfData();
	$scope.loadConversionData();
	$scope.loadLiftoverChainData();
	$scope.loadLiftoverSupportData();
	
	
	/***************************************
	 * *************************************
	 * 
	 *     Annotation upload/stop/delete methods
	 * 
	 * *************************************
	 ***************************************/
	
	
	$scope.addGenomeFile = function(files, ob) {
		$scope.uploadGenomeDeferred = $q.defer();
		if (files.length > 0) {
			(function(ob) {
				$scope.uploadGenomePromise = $upload.upload({
		    		url: "genetable/addGenomeFile",
		    		file: files[0],
		    		params: {idOrganismBuild: ob.idOrganismBuild},
		    		
		    	}).success(function(data) {
		    		$scope.refreshOrganisms();
		    		$scope.uploadGenomePromise = null;
		    	}).error(function(data) {
		    		if (data != null) {
		    			dialogs.error("Upload Error","Error reading in genome file", "Removing from database: " + data);
		    		}
		    		$scope.removeGenomeFromBuild(ob);
		    		$scope.uploadGenomePromise = null;
		    	});
			}(ob));
		}
	};
	
	
	
	$scope.addTranscriptFile = function(files,ob) {
		$scope.uploadTranscriptDeferred = $q.defer();
		
		if (files.length > 0) {
			(function(ob) {
				$scope.uploadTranscriptPromise = $upload.upload({
					url: "genetable/addTranscriptFile",
					file: files[0],
					params: {idOrganismBuild: ob.idOrganismBuild},
					
				}).success(function(data) {
		    		$scope.refreshOrganisms();
		    		$scope.uploadTranscriptPromise = null;
		    	}).error(function(data) {
		    		if (data != null) {
		    			dialogs.error("Error reading in transcript file", "Removing from database: " + data);
		    		}
		    		$scope.removeTranscriptsFromBuild(ob);
		    		$scope.uploadTranscriptPromise = null;
				});
			}(ob));
		}
	};

	
	$scope.addAnnotationFile = function(file,ob) {
		$scope.uploadAnnotationDeferred = $q.defer();
		if (file.length > 0) {
			(function(ob) {
				$scope.uploadAnnotationPromise = $upload.upload({
					url: "genetable/addAnnotationFile",
					file: file[0],
					params: {idOrganismBuild: ob.idOrganismBuild},
					
				}).success(function(data) {
			    	var modalInstance = $uibModal.open({
			    		templateUrl: 'app/useradmin/annotationPreviewWindow.html',
			    		controller: 'AnnotationPreviewController',
			    		windowClass: 'preview-dialog',
			    		resolve: {
			    			filename: function() {
			    				return file.name;
			    			},
			    			previewData: function() {
			    				return data.previewData;
			    			}
			    		}
			    	});
			    	
			    	modalInstance.result.then(function (setColumns) {
			    		$scope.columnDefs = setColumns;
			    		
			    		var params = {};
			    		for (var i=0; i<setColumns.length; i++) {
			    			params[setColumns[i].name] = setColumns[i].index;
			    		}
			    		
			    		params["idOrganismBuild"] = ob.idOrganismBuild;
			    		
			    		(function(ob) {
			    			$scope.parseAnnotationPromise = $http({
				    			method: 'PUT',
				    			url: 'genetable/parseAnnotations',
				    			params: params,
				    			
				    		}).success(function(data) {
				    			$scope.refreshOrganisms();
				    			
				    			$http({
				        			method: 'POST',
				        			url: 'query/clearNames',
				        			params: {obId: ob.idOrganismBuild}
				        		});
								$scope.uploadAnnotationPromise = null;
								$scope.parseAnnotationPromise = null;
				    		}).error(function(data) {
				    			if (data != null) {
				    				dialogs.error("Upload Error","Error parsing annotation data", "Removing from database: " + data);
				    			}
				    			
				    			$scope.removeAnnotationsFromBuild(ob,true);
								$scope.uploadAnnotationPromise = null;
								$scope.parseAnnotationPromise = null;
				    		});
			    		}(ob));
			    		
			    		
				    },function() {
				    	$scope.deleteAnnotationUpload();
						$scope.uploadAnnotationPromise = null;
						$scope.parseAnnotationPromise = null;
				    });
					   
			    	
				}).error(function(data) {
					if (data != null) {
						dialogs.error("Upload Error","Error generating annotation preview: " + data);
					}
					$scope.removeAnnotationsFromBuild(ob,true);
					$scope.uploadAnnotationPromise = null;
					$scope.parseAnnotationPromise = null;
				});
			}(ob));
			
		}
		
	};
	
	$scope.removeGenomeFromBuild = function(ob) {
		$scope.deletePromise = $http({
	    	method: 'DELETE',
	    	url: 'genetable/removeGenomeFromBuild',
	    	params: {idOrganismBuild: ob.idOrganismBuild}
	    }).success(function(data) {
    		$scope.refreshOrganisms();
    	}).error(function(data) {
    	});
	};
	
	$scope.removeTranscriptsFromBuild = function(ob) {
		$scope.deletePromise = $http({
	    	method: 'DELETE',
	    	url: 'genetable/removeTranscriptsFromBuild',
	    	params: {idOrganismBuild: ob.idOrganismBuild}
	    }).success(function(data) {
    		$scope.refreshOrganisms();
    	}).error(function(data) {
    	});
	};
	
	$scope.removeAnnotationsFromBuild = function(ob,delay) {
		$scope.deletePromise = $http({
    		method: 'DELETE',
    		url: 'genetable/removeAnnotationsFromBuild',
    		params: {idOrganismBuild: ob.idOrganismBuild, delay: delay}
    	}).success(function(data) {
    		$scope.refreshOrganisms();
    		
    		$http({
    			method: 'POST',
    			url: 'query/clearNames',
    			params: {obId: ob.idOrganismBuild}
    		});
    		
    	}).error(function(data) {
    	
    	});
		
		$http({
			method: 'POST',
			url: 'query/clearNames',
			params: {obId: ob.idOrganismBuild}
		});
	};
	
	$scope.deleteAnnotationUpload = function() {
		$http({
    		method: 'DELETE',
    		url: 'genetable/deleteAnnotationUpload',
    	}).success(function(data) {
    		$scope.refreshOrganisms();
    	});
	};
	
	/***************************************
	 * *************************************
	 * 
	 *     Transcription factor methods
	 * 
	 * *************************************
	 ***************************************/
	$scope.$watch('tfTabOpen', function() {
		if ($scope.tfTabOpen) {
			$scope.loadTfData();
		}
    	
    });
	
	$scope.showTfControls = function(tf) {
		tf.show = true;
	}
	
	$scope.hideTfControls = function(tf) {
		tf.show = false;
	}
	
	$scope.stopParse = function() {
		$scope.tfDeferred.resolve();
		$scope.tfRunning = false;
	}
	
	$scope.deleteTf = function(tf) {
		$http({
			url: "transFactor/deleteTf",
			method: "DELETE",
			params: {idTransFactor : tf.idTransFactor},
		}).success(function(){
			console.log("Deleted tf");
			$scope.loadTfData();
		}).error(function() {
			console.log("Failed to delete tf");
			$scope.loadTfData();
		});
	}
	
	$scope.addTfFile = function(file,ob) {
		var modalInstance = $uibModal.open({
    		templateUrl: 'app/useradmin/tfUploadWindow.html',
    		controller: 'TfUploadWindowController',
    		resolve: {
    			organismBuildList: function() {
    				return $scope.organismBuildList;
    			}
    		}
    	});
    	
    	modalInstance.result.then(function (tf) {
    		$scope.selectedTf = tf;
    		$scope.tfDeferred = $q.defer();
    		$scope.tfRunning = true;
    		$scope.parseTfPromise = null;
    		$scope.uploadTfPromise =  $upload.upload({
				url: "transFactor/parseTfFile",
				file: $scope.selectedTf.path[0],
				params : {idOrganismBuild: $scope.selectedTf.organismBuild.idOrganismBuild, isConverted: $scope.selectedTf.transformed},
				timeout: $scope.tfDeferred.promise,
			}).success(function(data) {

				(function(data) {
					$scope.addTfPromise = $http({
						url: "transFactor/addTf",
						method: "POST",
						params: {name : $scope.selectedTf.name, description: $scope.selectedTf.description, 
							idOrganismBuild : $scope.selectedTf.organismBuild.idOrganismBuild,filename: data.name},
					}).then(function(data) {
						$scope.loadTfData();
						$scope.tfRunning = false;
						$scope.selectedTf = null;
					},function(data) {
						$scope.loadTfData();
						$scope.tfRunning = false;
						dialogs.error("TF Upload Error","Error creating transcription factor database entry");
						(function(data) {
							$http({
								url: "transFactor/deleteTfFile",
								method: "DELETE",
								params: {name : data.name},
							}).success(function(){
								$scope.selectedTf = null;
							}).error(function() {
								$scope.selectedTf = null;
							});
						}(data));
					});
				}(data));
			}).error(function(data) {
				$scope.tfRunning = false;
				dialogs.error("TF Upload Error","Error parsing bed file : " + data.message);
				(function(data) {
					console.log(data);
					$http({
						url: "transFactor/deleteTfFile",
						method: "DELETE",
						params: {name : data.name},
					}).success(function(){
						$scope.selectedTf = null;
					}).error(function() {
						$scope.selectedTf = null;
					});
				}(data));
			});
	    });
		
	};
	
	/***************************************
	 * *************************************
	 * 
	 *     Homology methods
	 * 
	 * *************************************
	 ***************************************/

	$scope.showConversionControls = function(conv) {
		conv.show = true;
	}
	
	$scope.hideConversionControls = function(conv) {
		conv.show = false;
	}
	
	$scope.stopConverionParse = function() {
		$scope.convDeferred.resolve();
		$scope.convRunning = false;
	}
	
	$scope.deleteConversion = function(conv) {
		$http({
			url: "id_conversion/delete_conversion",
			method: "DELETE",
			params: {idGeneIdConversion : conv.idGeneIdConversion},
		}).success(function(){
			console.log("Deleted conversion");
			$scope.loadConversionData();
		}).error(function() {
			console.log("Failed to delete conversion");
			$scope.loadConversionData();
		});
	}
	
	$scope.addConversionFile = function(file,ob) {
		var modalInstance = $uibModal.open({
    		templateUrl: 'app/useradmin/conversionUploadWindow.html',
    		controller: 'ConversionUploadWindowController',
    		resolve: {
    			organismBuildList: function() {
    				return $scope.organismBuildList;
    			},
    			homologyList: function() {
    				return $scope.conversionList;
    			}
    		}
    	});
    	
    	modalInstance.result.then(function (conv) {
    		$scope.selectedConv = conv;
    		$scope.convDeferred = $q.defer();
    		$scope.convRunning = true;
    		$scope.convPromise = null;
    		$scope.uploadConvPromise =  $upload.upload({
				url: "id_conversion/add_conversion",
				file: $scope.selectedConv.path[0],
				params : {idSourceBuild: $scope.selectedConv.sourceBuild.idOrganismBuild, idDestBuild: $scope.selectedConv.destBuild.idOrganismBuild},
				timeout: $scope.convDeferred.promise,
			}).success(function(data) {
				(function(data) {
					$scope.parseConvPromise = $http({
						url: "id_conversion/check_conversion",
						method: "POST",
						params: {idGeneIdConversion : data.idx},
					}).success(function(data) {
						$scope.loadConversionData();
						$scope.convRunning = false;
						dialogs.notify("Homology File Parsing Stats",data.message);
					}).error(function(data) {
						console.log(data);
						$scope.loadConversionData();
						$scope.convRunning = false;
						dialogs.error("Homology File Error","Error parsing homology file: " + data.message);
					});
				}(data));
			}).error(function(data) {
				$scope.convRunning = false;
				dialogs.error("Homology File Upload Error","Error creating homology database entry: " + data.message);
				(function(data) {
					$http({
						url: "id_conversion/delete_conversion",
						method: "DELETE",
						params: {idGeneIdConversion : data.idx},
					}).success(function(){
						$scope.loadConversionData();
						$scope.selectedConv = null;
					}).error(function() {
						$scope.loadConversionData();
						$scope.selectedConv = null;
					});
				}(data));
			});
	    });
	};
	
	/***************************************
	 * *************************************
	 * 
	 *     Liftover methods
	 * 
	 * *************************************
	 ***************************************/

	$scope.showLiftoverSupportControls = function(lift) {
		lift.show = true;
	}
	
	$scope.hideLiftoverSupportControls = function(lift) {
		lift.show = false;
	}
	
	$scope.showLiftoverFileControls = function(lift) {
		lift.show = true;
	}
	
	$scope.hideLiftoverFileControls = function(lift) {
		lift.show = false;
	}
	
	$scope.deleteLiftoverChain = function(lift) {
		if (lift.supportCount > 0) {
			dialogs.error("Chain in Use","Selected chain is currently in use, please delete Supporting Liftover before deleting the chain.");
		} else {
			var dialog = dialogs.confirm("Delete Liftover Chain","Are you sure you want to delete the Liftover Chain entry?");
			
			dialog.result.then(function() {
				$http({
					url: "liftover/delete_liftover_chain",
					method: "DELETE",
					params: {idLiftoverChain : lift.idLiftoverChain},
				}).success(function(){
					console.log("Deleted liftover");
					$scope.loadLiftoverChainData();
					$scope.loadLiftoverSupportData();
				}).error(function() {
					console.log("Failed to delete liftover");
					$scope.loadLiftoverChainData();
					$scope.loadLiftoverSupportData();
				});
			});
		}
	}
	
	$scope.deleteLiftoverSupport = function(lift) {
		var dialog = dialogs.confirm("Delete Liftover Support","Are you sure you want to delete the Liftover Support entry?");
		
    	dialog.result.then(function() {
    		$http({
    			url: "liftover/delete_liftover_support",
    			method: "DELETE",
    			params: {idLiftoverSupport : lift.idLiftoverSupport},
    		}).success(function(){
    			$scope.loadLiftoverSupportData();
    			$scope.loadLiftoverChainData();
    		}).error(function(data) {
    			dialog.error("Error Deleting Liftover Support","Failed to delete Liftover Support, please contact Admins.");
    			$scope.loadLiftoverSupportData();
    			$scope.loadLiftoverChainData();
    		});
    	});
		
		
	}
	
	$scope.addLiftoverChain = function(file,ob) {
		var modalInstance = $uibModal.open({
    		templateUrl: 'app/useradmin/liftoverChainUploadWindow.html',
    		controller: 'LiftoverChainUploadWindowController',
    		resolve: {
    			organismBuildList: function() {
    				return $scope.organismBuildList;
    			},
    			chainList: function() {
    				return $scope.liftoverFileList;
    			}
    		}
    	});
    	
    	modalInstance.result.then(function (lift) {
    		$scope.selectedLiftover = lift;
    		$scope.liftoverDeferred = $q.defer();
    		$scope.liftoverRunning = true;
    		$scope.liftoverPromise = null;
    		$scope.liftoverPromise =  $upload.upload({
				url: "liftover/add_liftover_chain",
				file: $scope.selectedLiftover.path[0],
				params : {idSourceBuild: $scope.selectedLiftover.sourceBuild.idOrganismBuild, idDestBuild: $scope.selectedLiftover.destBuild.idOrganismBuild},
				timeout: $scope.liftoverDeferred.promise,
			}).success(function(data) {
				dialogs.notify("Success","Liftover Chain successfully added to the database.")
				$scope.liftoverPromise = null;
				$scope.loadLiftoverChainData();
				$scope.loadLiftoverSupportData();
			}).error(function(data) {
				$scope.liftoverRunning = false;
				dialogs.error("Liftover Upload Error","Error creating liftover chain database entry: " + data.message);
				$scope.liftoverPromise = null;
				$scope.loadLiftoverChainData();
				$scope.loadLiftoverSupportData();
			});
	    });
	};
	
	$scope.addLiftoverSupport = function(file,ob) {
		var modalInstance = $uibModal.open({
    		templateUrl: 'app/useradmin/liftoverSupportUploadWindow.html',
    		controller: 'LiftoverSupportUploadWindowController',
    		resolve: {
    			organismBuildList: function() {
    				return $scope.organismBuildList;
    			},
    			supportList: function() {
    				return $scope.liftoverSupportList;
    			},
    			chainList: function() {
    				return $scope.liftoverFileList;
    			}
    		}
    	});
    	
    	modalInstance.result.then(function (lift) {
    		$scope.selectedLiftover = lift;
    		$scope.liftoverDeferred = $q.defer();
    		$scope.liftoverRunning = true;
    		$scope.liftoverPromise = null;
    		$scope.liftoverPromise =  $upload.upload({
				url: "liftover/add_liftover_support",
				params : {idSourceBuild: $scope.selectedLiftover.sourceBuild.idOrganismBuild, 
					idDestBuild: $scope.selectedLiftover.destBuild.idOrganismBuild,
					idChainList: $scope.selectedLiftover.idChainList},
				timeout: $scope.liftoverDeferred.promise,
			}).success(function(data) {
				dialogs.notify("Success","Liftover Support successfully added to the database.")
				$scope.liftoverPromise = null;
				$scope.loadLiftoverChainData();
				$scope.loadLiftoverSupportData();
			}).error(function(data) {
				$scope.liftoverRunning = false;
				dialogs.error("Liftover Upload Error","Error creating liftover support database entry: " + data.message);
				$scope.liftoverPromise = null;
				$scope.loadLiftoverChainData();
				$scope.loadLiftoverSupportData();
			});
	    });
	};
	

	
	
	/***************************************
	 * *************************************
	 * 
	 *      Utilities
	 * 
	 * *************************************
	 ***************************************/
	
	
	$scope.setMessage = function(message) {
		$scope.message = message;
		$timeout(function(){$scope.message = "";},5000); 
	};
	
	$scope.isAdmin = function(selectedUsers) {
		for (var i=0; i<selectedUsers.length; i++) {
			var admin = false;
			for (var j=0;j<selectedUsers[i].roles.length;j++) {
				if (selectedUsers[i].roles[j].name == "admin") {
					admin = true;
				}
			}
			
			selectedUsers[i]['admin'] = admin;
		}
		
		return selectedUsers;
	};
	

	$scope.hideOrganismControls = function(ob) {
		ob.show = false;
	};
	$scope.showOrganismControls = function(ob) {
		ob.show = true;
	};
	
	$scope.loadCounts = function() {
		for (var i=0; i<$scope.labs.length; i++) {
			$http({
    	    	method: 'GET',
    	    	url: 'user/bylab',
    	    	params: {idLab:$scope.labs[i].idLab, localIndex: i}
    	    }).success(function(data,status,headers,config) {
    	    	$scope.labs[config.params.localIndex]['count'] = data.length;
    	    });
		}
	};
	
    /**
     * Load user list.  If a lab is specfied, limit the list to a lab
     */
    $scope.loadSelected = function() {
    	if (angular.isUndefined($scope.selectedLab) || $scope.selectedLab == null) {
    		$http({
        		method: 'GET',
        		url: 'user/all'
            }).success(function(data,status) {
            	$scope.selectedUsers = $scope.isAdmin(data);
        	}).error(function(data) {
        		console.log(data);
        	});
    		
    	} else {
    		$http({
    	    	method: 'GET',
    	    	url: 'user/bylab',
    	    	params: {idLab:$scope.selectedLab.idLab}
    	    }).success(function(data,status) {
            	$scope.selectedUsers = $scope.isAdmin(data);
    	    });
    	}
    };
    
    /**
     * Call loadSelected when the selected lab is changed.
     */
    $scope.$watch('selectedLab', function() {
    	$scope.loadSelected();
    });
    
    
    /***************************************
	 * *************************************
	 * 
	 *      Edit exising users, labs, organism, builds
	 * 
	 * *************************************
	 ***************************************/
    $scope.openEditUserWindow = function(e) {
    	var modalInstance = $uibModal.open({
    		templateUrl: 'app/useradmin/userWindow.html',
    		controller: 'UserController',
    		resolve: {
    			labList: function() {
    				return $scope.labs;
    			},
    			instituteList: function() {
    				return $scope.institutes;
    			},
    			userData: function () {
    				e["password"] = "placeholder"; 
    				return e;
    			},
    			title: function() {
    				return "Edit";
    			},
    			bFace: function() {
    				return "Update";
    			},
    			showAll: function() {
    				return true;
    			}
    		}
    	});
    	
    	modalInstance.result.then(function (user) {
    		//Create a list of lab ids
	    	var lids = [];
	    	var iids = [];
	    	
	    	for (var i=0; i<user.labs.length;i++) {
	    		lids.push(user.labs[i].idLab);
	    	}
	    	
	    	for (var i=0;i<user.institutes.length;i++) {
	    		iids.push(user.institutes[i].idInstitute);
	    	}
	    	
	    	$http({
    	    	method: 'POST',
    	    	url: 'user/modifyuser',
    	    	params: {first:user.first,last:user.last,username:user.username,password:user.password,email:user.email,
    	    		phone:user.phone,admin:user.admin,lab:lids,institutes:iids,idUser:user.idUser}
    	    }).success(function(data,status) {
    	    	$scope.loadSelected();
    	    });
	    	
	    });
    };
    
 
    $scope.openEditLabWindow = function(e) {
    	var modalInstance = $uibModal.open({
    		templateUrl: 'app/useradmin/labWindow.html',
    		controller: 'LabController',
    		resolve: {
    			labData: function () {
    				return e;
    			},
    			title: function() {
    				return "Edit";
    			},
    			bFace: function() {
    				return "Update";
    			}
    		}
    	});
    	
    	modalInstance.result.then(function (lab) {
	    	$http({
    	    	method: 'PUT',
    	    	url: 'lab/modifylab',
    	    	params: {first:lab.first, last:lab.last, email:lab.email, phone:lab.phone, idLab:lab.idLab}
    	    }).success(function(data,status) {
    	    	$scope.loadLabs();
    	    });
	    });
    };
    
 
    $scope.openEditOrganismWindow = function (idOrganism) {
    	var organism = null;
    	for (var i=0; i<$scope.organismList.length;i++) {
    		if ($scope.organismList[i].idOrganism == idOrganism) {
    			organism = $scope.organismList[i];
    		}
    	}
    	
    	if (organism == null) {
    		return;
    	}
    	
	    var modalInstance = $uibModal.open({
	      templateUrl: 'app/useradmin/organismWindow.html',
	      controller: 'OrganismController',
    	  resolve: {
    		  	
		        organismData: function () {
		        	return organism;
    			},
    			title: function() {
    				return "Edit";
    			},
    			bFace: function() {
    				return "Update";
    			},
    			organismList: function() {
    				return $scope.organismList;
    			}
		  }  
	    });

	    modalInstance.result.then(function (organism) {
	    	$http({
    	    	method: 'PUT',
    	    	url: 'genetable/modifyOrganism',
    	    	params: {common:organism.common,binomial:organism.binomial, idOrganism: organism.idOrganism}
    	    }).success(function(data) {
    	    	$scope.refreshOrganisms();
    	    });
	    	
	    });
    };
    
    $scope.openEditBuildWindow = function (organismBuild) {	
	    var modalInstance = $uibModal.open({
	      templateUrl: 'app/useradmin/organismBuildWindow.html',
	      controller: 'OrganismBuildController',
    	  resolve: {
		        organismBuildData: function () {
		        	return organismBuild;
    			},
    			title: function() {
    				return "Edit";
    			},
    			bFace: function() {
    				return "Update";
    			},
    			organismList: function() {
    				return $scope.organismList;
    			},
    			organismBuildList: function() {
    				return $scope.organismBuildList;
    			}
		  }  
	    });

	    modalInstance.result.then(function (organismBuild) {
	    	$http({
    	    	method: 'PUT',
    	    	url: 'genetable/modifyOrganismBuild',
    	    	params: {name:organismBuild.name,idOrganism:organismBuild.organism.idOrganism, idOrganismBuild: organismBuild.idOrganismBuild}
    	    }).success(function(data) {
    	    	$scope.refreshOrganisms();
    	    });
	    	
	    });
    };
    
    
    /**************************************
     * Add new users/labs/organism
     **************************************/
    $scope.openNewUserWindow = function () {
	    var modalInstance = $uibModal.open({
	      templateUrl: 'app/useradmin/userWindow.html',
	      controller: 'UserController',
    	  resolve: {
		        labList: function () {
		          return $scope.labs;
		        },
		        instituteList: function() {
    		  		return $scope.institutes;
  				},
		        userData: function () {
		        	var emptyUser = {first: '', last: '', username: '', password: '',
			    			phone: '', email: '', admin: false, lab: []};
    				return emptyUser;
    			},
    			title: function() {
    				return "Add";
    			},
    			bFace: function() {
    				return "Add";
    			},
    			showAll: function() {
    				return true;
    			}
		  }
	    });

	    modalInstance.result.then(function (user) {
	    	//Create a list of lab ids
	    	var lids = [];
	    	var iids = [];
	    	for (var i=0; i<user.labs.length;i++) {
	    		lids.push(user.labs[i].idLab);
	    	}
	    	for (var i=0; i<user.institutes.length;i++) {
	    		iids.push(user.institutes[i].idInstitute);
	    	}
	    	
	    	$http({
    	    	method: 'POST',
    	    	url: 'user/adduser',
    	    	params: {first:user.first,last:user.last,username:user.username,password:user.password,email:user.email,
    	    		phone:user.phone,admin:user.admin,lab:lids,institutes:iids}
    	    }).success(function(data,status) {
    	    	$scope.loadSelected();
    	    	$scope.loadLabs();
    	    });	
	    });
    };
    
    $scope.openNewUserRequestWindow = function () {
	//console.log("In openNewUserRequestWindow");    
	    var modalInstance = $uibModal.open({
	      templateUrl: 'app/useradmin/userWindow.html',
	      controller: 'UserController',
    	  resolve: {
		        labList: function () {
		          return $scope.labs;
		        },
		        instituteList: function() {
    		  		return $scope.institutes;
  				},
		        userData: function () {
		        	var emptyUser = {first: '', last: '', username: '', password: '',
			    			phone: '', email: '', admin: false, lab: []};
    				return emptyUser;
    			},
    			title: function() {
    				return "Sign Up";
    			},
    			bFace: function() {
    				return "Sign Up";
    			},
    			showAll: function() {
    				return true;
    			}
		  }
	    });

	    modalInstance.result.then(function (user) {
	    	//Create a list of lab ids
	    	var lids = [];
	    	var iids = [];
	    	for (var i=0; i<user.labs.length;i++) {
	    		lids.push(user.labs[i].idLab);
	    	}
	    	for (var i=0; i<user.institutes.length;i++) {
	    		iids.push(user.institutes[i].idInstitute);
	    	}
	    	
			//console.log("about to call user/newuser");	    	
	    	$http({
    	    	method: 'POST',
    	    	url: 'user/newuser',
    	    	params: {first:user.first,last:user.last,username:user.username,password:user.password,email:user.email,
    	    		phone:user.phone,admin:user.admin,lab:lids,institutes:iids,theUrl:$location.absUrl()}
    	    }).success(function(data,status) {
    	    	$scope.setMessage(data);
    	    	$scope.loadSelected();
    	    	$scope.loadLabs();
    	    });	
	    });
    };


    $scope.openNewLabWindow = function () {
	    var modalInstance = $uibModal.open({
	      templateUrl: 'app/useradmin/labWindow.html',
	      controller: 'LabController',
    	  resolve: {
    		  	
		        labData: function () {
		        	var emptyLab = {first: '', last: '', institutes: []};
    				return emptyLab;
    			},
    			title: function() {
    				return "Add";
    			},
    			bFace: function() {
    				return "Add";
    			}
		  }  
	    });

	    modalInstance.result.then(function (lab) {
	    	$http({
    	    	method: 'PUT',
    	    	url: 'lab/addlab',
    	    	params: {first:lab.first,last:lab.last,email:lab.email, phone:lab.phone}
    	    }).success(function(data,status) {
    	    	$scope.loadLabs();
    	    });
	    	
	    });
    };


		$scope.approveUser = function() {
			$scope.guid = $location.search()['guid'];
			$scope.iduser = $location.search()['idUser'];
			$scope.deleteuser = $location.search()['deleteuser'];
		
			//console.log("before calling approveuser, guid: ");
			//console.log($scope.guid);
			//console.log($scope.iduser);
			//console.log($scope.deleteuser);

			$http({
	    		method: 'POST',
	    		url: 'user/approveuser',
	    		params: {guid: $scope.guid, iduser: $scope.iduser, deleteuser: $scope.deleteuser, theUrl:$location.absUrl()}
	        }).success(function(data,status) {
	        	$scope.setMessage(data);
	        	$timeout(function(){$window.close();},5000);
	        	//$scope.message = data;

        		//$location.path($rootScope.lastLocation);

	    	});
		};		
		
    
    $scope.openNewOrganismWindow = function () {
	    var modalInstance = $uibModal.open({
	      templateUrl: 'app/useradmin/organismWindow.html',
	      controller: 'OrganismController',
    	  resolve: {
    		  	
		        organismData: function () {
		        	var emptyOrganism = {common: '', binomial: ''};
    				return emptyOrganism;
    			},
    			title: function() {
    				return "Add";
    			},
    			bFace: function() {
    				return "Add";
    			},
    			organismList: function() {
    				return $scope.organismList;
    			}
		  }  
	    });

	    modalInstance.result.then(function (organism) {
	    	$http({
    	    	method: 'PUT',
    	    	url: 'genetable/addOrganism',
    	    	params: {common:organism.common,binomial:organism.binomial}
    	    }).success(function(data,status) {
    	    	$scope.refreshOrganisms();
    	    });
	    	
	    });
    };
    
    $scope.openNewBuildWindow = function () {
	    var modalInstance = $uibModal.open({
	      templateUrl: 'app/useradmin/organismBuildWindow.html',
	      controller: 'OrganismBuildController',
    	  resolve: {
    		  	
		        organismBuildData: function () {
		        	var emptyBuild = {name: '', organism: ''};
    				return emptyBuild;
    			},
    			title: function() {
    				return "Add";
    			},
    			bFace: function() {
    				return "Add";
    			},
    			organismList: function() {
    				return $scope.organismList;
    			},
    			organismBuildList: function() {
    				return $scope.organismBuildList;
    			}
		  }  
	    });

	    modalInstance.result.then(function (organismBuild) {
	    	console.log(organismBuild);
	    	$http({
    	    	method: 'PUT',
    	    	url: 'genetable/addOrganismBuild',
    	    	params: {name:organismBuild.name, idOrganism: organismBuild.organism.idOrganism}
    	    }).success(function(data) {
    	    	$scope.refreshOrganisms();
    	    });
	    });
    };
    
    
    
    /***
     * Select or deselect all users
     */
    $scope.selectAllUsersChanged = function() {
    	$scope.selectAllUsers = !$scope.selectAllUsers;

    	for (var i = 0; i < $scope.selectedUsers.length; i++) {
    		$scope.selectedUsers[i].selected = $scope.selectAllUsers;
    	}
    };
    
    
    /**************************************
     * These methods call the delete methods
     **************************************/
    $scope.deleteSelectedUsers = function(idList) {
    	for(var i=0;i<idList.length;i++) {
    		$http({
        		method: 'DELETE',
        		url: 'user/deleteuser',
        		params: {idUser: idList[i]}
    		}).success(function() {
    			$scope.loadSelected();
    			$scope.loadLabs();
    		});
    	}
    };
    
    $scope.deleteSelectedLabs = function(idList) {
    	for(var i=0;i<idList.length;i++) {
    		$http({
        		method: 'DELETE',
        		url: 'lab/deletelab',
        		params: {idLab: idList[i]}
    		}).success(function() {
    			$scope.loadLabs();
    		});
    	}
    };
    
    $scope.deleteSelectedBuilds = function(idList) { 	
    	for(var i=0;i<idList.length;i++) {
    		$http({
        		method: 'DELETE',
        		url: 'genetable/removeOrganismBuild',
        		params: {idOrganismBuild: idList[i]}
    		}).success(function() {
    			$scope.refreshOrganisms();
    		});
    	}
    };
    
    /**************************************
     * These methods generate a delete confirmation box.  If confirmed, calls the delete methods
     **************************************/
    $scope.confirmOrganismDelete = function() {
    	//load lab ids
    	var selectedBuilds = [];
    	for (var i=0; i<$scope.organismBuildList.length;i++) {
    		if ($scope.organismBuildList[i].selected == true) {
    			selectedBuilds.push($scope.organismBuildList[i].idOrganismBuild);
    		}
    	}
    	
    	if (selectedBuilds.length > 0) {
    		var dialog = dialogs.confirm("Delete Builds","Click 'Yes' to delete selected builds, otherwise click 'No'.");
    		
        	dialog.result.then(function() {
        		$scope.deleteSelectedBuilds(selectedBuilds);
        	});
    	} else {
    		$scope.setMessage("No builds selected");
    	}
    	
    };
    
    $scope.confirmOrganismFileDelete = function(ob, toDelete) {
    	var dialog = null;
    	
    	if (toDelete == "transcripts") {
    		dialog = dialogs.confirm("Delete Transcripts","Click 'Yes' to delete transcripts from " + ob.name + ", click 'No' to bail.");
		} else if (toDelete == "genome") {
			dialog = dialogs.confirm("Delete Genome","Click 'Yes' to delete genomes from " + ob.name + ", click 'No' to bail.");
		} else if (toDelete == "annotations") {
			dialog = dialogs.confirm("Delete Gene Aliases","Click 'Yes' to delete gene aliases from " + ob.name + ", click 'No' to bail.");
		}
    	
    	dialog.result.then(function() {
    		if (toDelete == "transcripts") {
				$scope.removeTranscriptsFromBuild(ob);
			} else if (toDelete == "genome") {
				$scope.removeGenomeFromBuild(ob);
			} else if (toDelete == "annotations") {
				$scope.removeAnnotationsFromBuild(ob, false);
			}
    	});
    };
    
    
    $scope.confirmUserDelete = function() {
    	//load lab ids
    	var selectedIds = [];
    	for (var i=0; i<$scope.selectedUsers.length;i++) {
    		if ($scope.selectedUsers[i].selected) {
    			selectedIds.push($scope.selectedUsers[i].idUser);
    		}
    	}
    	
    	if (selectedIds.length > 0) {
    		var dialog = dialogs.confirm("Delete Users","Click 'Yes' to delete selected users, click 'No' to bail.");
    		
        	dialog.result.then(function() {
        		$scope.deleteSelectedUsers(selectedIds);
        	});
    	} else {
    		$scope.setMessage("No users selected");
    	}
    	
    };
    
    $scope.confirmLabDelete = function() {
    	//load lab ids
    	var selectedIds = [];
    	for (var i=0; i<$scope.labs.length;i++) {
    		if ($scope.labs[i].selected) {
    			selectedIds.push($scope.labs[i].idLab);
    		}
    	}
    	
    	if (selectedIds > 0) {
    		
    		var dialog = dialogs.confirm("Delete Labs","Click 'Yes' to delete selected labs, click 'No' to bail.");
        	
        	dialog.result.then(function(result) {
        		$scope.deleteSelectedLabs(selectedIds);
        	});
    	} else {
    		$scope.setMessage("No labs selected");
    	}
    	
    };
    
    $scope.refreshOrganisms = function() {
		$scope.loadOrganismBuilds();
		$scope.loadOrganisms();
	};
    
    //Load labs and users.
	$scope.loadLabs();
	$scope.loadSelected();
	$scope.getInstituteList();
	$scope.refreshOrganisms();
	

}]);