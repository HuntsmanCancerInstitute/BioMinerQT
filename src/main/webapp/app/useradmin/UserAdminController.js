'use strict';

/**
 * UserAdminController
 * @constructor
 */
var useradmin = angular.module('useradmin', ['angularFileUpload','ui.mask','ui.validate','confirmation','filters','directives','services']);

angular.module("useradmin").controller("UserAdminController", ['$rootScope','$scope','$http','$modal','$timeout','$upload','DynamicDictionary','StaticDictionary',
                                                      
function($rootScope, $scope, $http, $modal, $timeout, $upload, DynamicDictionary, StaticDictionary) {
	
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
	$scope.userTabOpen = true;
	$scope.labTabOpen = false;
	$scope.genomeTabOpen = false;

	//Model data
	$scope.labs = [];
	$scope.organismBuildList = [];
	$scope.organismList = [];
	$scope.selectedUsers = [];
	$scope.selectedLab;
	
	//Select all users
	$scope.selectAllUsers = false;
	
	$scope.columnDefs = null;
	
	/*************************************
	 * Watchers
	 */
	
	 $scope.$watch('userTabOpen', function() {
	    if ($scope.userTabOpen == true) {
	    	$rootScope.helpMessage = "<p>Placeholder for user management help.</p>";
	    }
	 });
	 
	 $scope.$watch('labTabOpen', function() {
	    if ($scope.labTabOpen == true) {
	    	$rootScope.helpMessage = "<p>Placeholder for lab mangement help.</p>";
	    }
	 });
	 
	 $scope.$watch('genomeTabOpen', function() {
		if ($scope.genomeTabOpen == true) {
		   	$rootScope.helpMessage = "<p>The Genome page can be used to add new organisms/builds to Biominer or edit existing organisms/builds.</p>";
		    $rootScope.helpMessage += "<p>New organisms can be added by clicking on the 'Add Organism' link above the organism build table. " +
		    		"Existing organisms can be edited by double clicking on an organism name in the organism build table.  There currently isn't " +
		    		"a way to delete existing organisms from the Biominer website</p>";
		    $rootScope.helpMessage += "<p>New organism builds can be added by clicking on the 'Add Organism Build' link above the organism build table. " +
		    		"Existing organisms builds can be edited by double clicking on an organism build name in the organism build table.  Organism builds can" +
		    		"be deleted by selecting the checkbox to the left of the organism build name and then clickin on the 'Delete Organism Build link above " +
		    		"the organism build table.  There will be a confirmation message before the build is deleted.</p>";
		    $rootScope.helpMessage += "<p>Transcript, genome and annotation files are necessary for queries to work. These can be added " +
		    		"to a organism build by clicking on the blue plus button next to the appropriate table entry. You can replace one of these files by selecting " +
		    		"the orange refresh button next to the appropriate entry.  You can delete the file by selecting the red minus button next to the appropriate " +
		    		"entry.</p>";
		    $rootScope.helpMessage += "<p>The transcript file and genome files must be loaded for basic queries.  The annotation file must be loaded " +
		    		"to search by gene. When the transcript and genome file are selected, Biominer will check to see if they are valid. If a genome can be " +
		    		"used for basic queries, there will be an empty start next to its name.  If a build can be used for basic queries and gene name searches, " +
		    		"there will be a filled in star next to its name.</p>";
		 }
	 });

	/**************************************
     * Load dictionaries
     **************************************/
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
	
	
	/**************************************
     * Organism file control methods
     **************************************/
	$scope.addGenomeFile = function(files, ob) {
		$upload.upload({
    		url: "genetable/addGenomeFile",
    		file: files,
    		params: {idOrganismBuild: ob.idOrganismBuild}
    	}).success(function(data) {
    		$scope.refreshOrganisms();
    	}).error(function(data) {
    		$scope.showErrorMessage("Error reading in gene annotation file",data);
    	});
	};
	
	$scope.addTranscriptFile = function(files,ob) {
		$upload.upload({
			url: "genetable/addTranscriptFile",
			file: files,
			params: {idOrganismBuild: ob.idOrganismBuild}
		}).success(function(data) {
    		$scope.refreshOrganisms();
    	}).error(function(data) {
			$scope.showErrorMessage("Error reading in transcript file", data);
		});
	};
	
	$scope.addAnnotationFile = function(file,ob) {
		$upload.upload({
			url: "genetable/addAnnotationFile",
			file: file,
			params: {idOrganismBuild: ob.idOrganismBuild}
		}).success(function(data) {
	    	var modalInstance = $modal.open({
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
	    		
	    		$http({
	    			method: 'PUT',
	    			url: 'genetable/parseAnnotations',
	    			params: params
	    		}).success(function(data) {
	    			$scope.showErrorMessage("Successfully parsed annotation file.", data);
	    		}).error(function(data) {
	    			$scope.showErrorMessage("Error parsing annotation data", data);
	    			$scope.removeAnnotationFile(ob);
	    		});
	    		
		    },function() {
		    	$scope.removeAnnotationFile(ob);
		    });
			   
	    	$scope.refreshOrganisms();
		}).error(function(data) {
			$scope.showErrorMessage(data.message);
		});
	};
	
	$scope.removeGenomeFile = function(ob) {
		$http({
	    	method: 'DELETE',
	    	url: 'genetable/removeGenomeFile',
	    	params: {idOrganismBuild: ob.idOrganismBuild}
	    }).success(function(data) {
    		$scope.refreshOrganisms();
    	});
	};
	
	$scope.removeTranscriptFile = function(ob) {
		$http({
	    	method: 'DELETE',
	    	url: 'genetable/removeTranscriptFile',
	    	params: {idOrganismBuild: ob.idOrganismBuild}
	    }).success(function(data) {
    		$scope.refreshOrganisms();
    	});
	};
	
	$scope.removeAnnotationFile = function(ob) {
		$http({
    		method: 'DELETE',
    		url: 'genetable/removeAnnotationFile',
    		params: {idOrganismBuild: ob.idOrganismBuild}
    	}).success(function(data) {
    		$scope.refreshOrganisms();
    	});
	};
	
	 
	
	/**************************************
     * Utilities
     **************************************/
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
	
	$scope.setMessage = function(message) {
		$scope.message = message;
		$timeout(function(){$scope.message = "";},3000); 
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
	
	
	
	/**************************************
     * Display
     **************************************/
	$scope.hideOrganismControls = function(ob) {
		ob.show = !ob.show;
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
    
    /**************************************
     * Edit exising users, labs, organism, builds
     **************************************/
    $scope.openEditUserWindow = function(e) {
    	var modalInstance = $modal.open({
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
    	var modalInstance = $modal.open({
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
    	    	params: {first:lab.first, last:lab.last, idLab:lab.idLab}
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
    	
	    var modalInstance = $modal.open({
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
	    var modalInstance = $modal.open({
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
	    var modalInstance = $modal.open({
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
    
    $scope.openNewLabWindow = function () {
	    var modalInstance = $modal.open({
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
    	    	params: {first:lab.first,last:lab.last}
    	    }).success(function(data,status) {
    	    	$scope.loadLabs();
    	    });
	    	
	    });
    };
    
    $scope.openNewOrganismWindow = function () {
	    var modalInstance = $modal.open({
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
	    var modalInstance = $modal.open({
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
    		var modalInstance = $modal.open({
        		templateUrl: 'app/common/confirmation.html',
        		controller: 'ConfirmationController',
        		resolve: {
        			data: function() {
        				return {
        					title: 'Delete Builds',
        					message: "Click OK to delete selected builds, otherwise click cancel."
        				};
        			},	
        		}
        	});
        	
        	modalInstance.result.then(function(result) {
        		$scope.deleteSelectedBuilds(selectedBuilds);
        	});
    	} else {
    		$scope.setMessage("No builds selected");
    	}
    	
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
    		var modalInstance = $modal.open({
        		templateUrl: 'app/common/confirmation.html',
        		controller: 'ConfirmationController',
        		resolve: {
        			data: function() {
        				return {
        					title: 'Delete Users',
        					message: "Click OK to delete selected users, otherwise click cancel."
        				};
        			},	
        		}
        	});
        	
        	modalInstance.result.then(function(result) {
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
    		var modalInstance = $modal.open({
        		templateUrl: 'app/common/confirmation.html',
        		controller: 'ConfirmationController',
        		resolve: {
        			data: function() {
        				return {
        					title: 'Delete Labs',
        					message: "Click OK to delete selected labs, otherwise click cancel."
        				};
        			},	
        		}
        	});
        	
        	modalInstance.result.then(function(result) {
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