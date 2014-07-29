'use strict';

/**
 * UserAdminController
 * @constructor
 */
var useradmin = angular.module('useradmin', ['ui.mask','ui.validate','confirmation','filters','directives','services']);

angular.module("useradmin").controller("UserAdminController", ['$scope','$http','$modal','$timeout','DynamicDictionary','StaticDictionary',
                                                      
function($scope, $http, $modal, $timeout, DynamicDictionary, StaticDictionary) {
	
	//user table variables
	$scope.userLimit = 5;
	$scope.userCurrentPage = 0;
	$scope.userReverseSort = false;
	$scope.userOrderByField = "last";
	
	//lab table variables
	$scope.labLimit = 5;
	$scope.labCurrentPage = 0;
	$scope.labReverseSort = false;
	$scope.labOrderByField = "last";
	
	//tabset variables
	$scope.userTabOpen = true;
	$scope.labTabOpen = false;

	//Model data
	$scope.labs = [];
	$scope.selectedUsers = [];
	$scope.selectedLab;
	
	//Load static dictionaries
	StaticDictionary.instituteList(function(data) {
		$scope.institutes = data;
	});
	
	
	//Select all users
	$scope.selectAllUsers = false;
	
	/**
	 * Message
	 */
	$scope.setMessage = function(message) {
		$scope.message = message;
		$timeout(function(){$scope.message = "";},3000); 
	};
	
	
	/**
	 * Add 'selected' property to an object.  This can be tied a checkbox
	 */
	
	$scope.addCheckbox = function(selectedUsers) {
		for (var i = 0; i < selectedUsers.length; i++) {
		    selectedUsers[i]['selected'] = false;
		}
		return selectedUsers;
	};
	
	$scope.loadCounts = function() {
		for (var i=0; i<$scope.labs.length; i++) {
			$http({
    	    	method: 'POST',
    	    	url: 'user/bylab',
    	    	params: {idLab:$scope.labs[i].idLab, localIndex: i}
    	    }).success(function(data,status,headers,config) {
    	    	$scope.labs[config.params.localIndex]['count'] = data.length;
    	    });
		}
	};
	
	
	/**
	 * Load all available labs.
	 */
	$scope.loadLabs = function() {
		DynamicDictionary.loadLabs().success(function(data,status) {
	    	$scope.labs = $scope.addCheckbox(data);
	    	$scope.loadCounts();
	    });
	};
	

    
    /**
     * Load user list.  If a lab is specfied, limit the list to a lab
     */
    $scope.loadSelected = function() {
    	if (angular.isUndefined($scope.selectedLab) || $scope.selectedLab == null) {
    		$http({
        		method: 'POST',
        		url: 'user/all'
            }).success(function(data,status) {
            	$scope.selectedUsers = $scope.addCheckbox(data);
        	});
    		
    	} else {
    		$http({
    	    	method: 'POST',
    	    	url: 'user/bylab',
    	    	params: {idLab:$scope.selectedLab.idLab}
    	    }).success(function(data,status) {
    	    	$scope.selectedUsers = $scope.addCheckbox(data);
    	    });
    	}
    };
    
    /**
     * Call loadSelected when the selected lab is changed.
     */

    $scope.$watch('selectedLab', function() {
    	$scope.loadSelected();
    });
    
    /***
     * Open user window with existing data
     */
    $scope.openEditUserWindow = function(e) {
    	var modalInstance = $modal.open({
    		templateUrl: 'app/useradmin/userWindow.html',
    		controller: 'UserController',
    		resolve: {
    			labList: function() {
    				return $scope.labs;
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
	    	var ids = [];
	    	for (var i=0; i<user.labs.length;i++) {
	    		ids.push(user.labs[i].idLab);
	    	}
	    	
	    	$http({
    	    	method: 'POST',
    	    	url: 'user/modifyuser',
    	    	params: {first:user.first,last:user.last,username:user.username,password:user.password,email:user.email,
    	    		phone:user.phone,admin:user.admin,lab:ids,idUser:user.idUser}
    	    }).success(function(data,status) {
    	    	$scope.loadSelected();
    	    });
	    	
	    });
    };
    
    /***
     * Open user window with existing data
     */
    $scope.openEditLabWindow = function(e) {
    	var modalInstance = $modal.open({
    		templateUrl: 'app/useradmin/labWindow.html',
    		controller: 'LabController',
    		resolve: {
    			instituteList: function() {
    				return $scope.institutes;
    			},
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
    		var ids = [];
	    	for (var i=0; i<lab.institutes.length;i++) {
	    		ids.push(lab.institutes[i].idInstitute);
	    	}
    		
	    	$http({
    	    	method: 'POST',
    	    	url: 'lab/modifylab',
    	    	params: {first:lab.first, last:lab.last, idLab:lab.idLab, institutes:ids}
    	    }).success(function(data,status) {
    	    	$scope.loadLabs();
    	    });
	    });
    };
    
    /*****
     * Open userWindow with an empty model
     */
    $scope.openNewUserWindow = function () {
	    var modalInstance = $modal.open({
	      templateUrl: 'app/useradmin/userWindow.html',
	      controller: 'UserController',
    	  resolve: {
		        labList: function () {
		          return $scope.labs;
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
	    	var ids = [];
	    	for (var i=0; i<user.labs.length;i++) {
	    		ids.push(user.labs[i].idLab);
	    	}
	    	
	    	$http({
    	    	method: 'POST',
    	    	url: 'user/adduser',
    	    	params: {first:user.first,last:user.last,username:user.username,password:user.password,email:user.email,
    	    		phone:user.phone,admin:user.admin,lab:ids}
    	    }).success(function(data,status) {
    	    	$scope.loadSelected();
    	    	$scope.loadLabs();
    	    });	
	    });
    };
    
    /*****
     * Open labWindow with an empty model
     */
    $scope.openNewLabWindow = function () {
	    var modalInstance = $modal.open({
	      templateUrl: 'app/useradmin/labWindow.html',
	      controller: 'LabController',
    	  resolve: {
    		  	instituteList: function() {
    		  		return $scope.institutes;
  				},
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
	    	var ids = [];
	    	for (var i=0; i<lab.institutes.length;i++) {
	    		ids.push(lab.institutes[i].idInstitute);
	    	}
	    	
	    	$http({
    	    	method: 'POST',
    	    	url: 'lab/addlab',
    	    	params: {first:lab.first,last:lab.last, institutes: ids}
    	    }).success(function(data,status) {
    	    	$scope.loadLabs();
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
    
    
    
    
    /****
     * Delete users with selected checkboxes
     */
    $scope.deleteSelectedUsers = function() {
    	var toDelete = [];
    	for (var i = 0; i < $scope.selectedUsers.length; i++) {
		    if ($scope.selectedUsers[i]['selected'] == true) {
		    	toDelete.push($scope.selectedUsers[i].idUser);
		    }
		}
   
    	for(var i=0;i<toDelete.length;i++) {
    		$http({
        		method: 'POST',
        		url: 'user/deleteuser',
        		params: {idUser: toDelete[i]}
    		}).success(function() {
    			$scope.loadSelected();
    			$scope.loadLabs();
    		});
    	}
    };
    
    /****
     * Delete users with selected checkboxes
     */
    $scope.deleteSelectedLabs = function() {
    	var toDelete = [];
    	for (var i = 0; i < $scope.labs.length; i++) {
		    if ($scope.labs[i]['selected'] == true) {
		    	toDelete.push($scope.labs[i].idLab);
		    }
		}
   
    	for(var i=0;i<toDelete.length;i++) {
    		$http({
        		method: 'POST',
        		url: 'lab/deletelab',
        		params: {idLab: toDelete[i]}
    		}).success(function() {
    			$scope.loadLabs();
    		});
    	}
    };
    
    /***
     * This method launches a confirmation window.  If a result is returned, the delete user method is selected.
     */
    $scope.confirmUserDelete = function() {
    	//load lab ids
    	var selectedIds = [];
    	for (var i=0; i<$scope.selectedUsers.length;i++) {
    		if ($scope.selectedUsers[i].selected) {
    			selectedIds.push(i);
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
        		$scope.deleteSelectedUsers();
        	});
    	} else {
    		$scope.setMessage("No users selected");
    	}
    	
    };
    
    /***
     * This method launches a confirmation window.  If a result is returned, the delete lab method is selected.
     */
    $scope.confirmLabDelete = function() {
    	//load lab ids
    	var selectedIds = [];
    	for (var i=0; i<$scope.labs.length;i++) {
    		if ($scope.labs[i].selected) {
    			selectedIds.push(i);
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
        		$scope.deleteSelectedLabs();
        	});
    	} else {
    		$scope.setMessage("No labs selected");
    	}
    	
    };
    
    //Load labs and users.
	$scope.loadLabs();
	$scope.loadSelected();
	
	
}]);