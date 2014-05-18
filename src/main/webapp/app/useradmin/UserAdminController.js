'use strict';

/**
 * UserAdminController
 * @constructor
 */
var useradmin = angular.module('useradmin', ['ui.mask','ui.validate','confirmation','filters']);

angular.module("useradmin").controller("UserAdminController", ['$scope','$http','$modal',
                                                      
function($scope, $http, $modal) {
	
	/**********************
	 * Temporary password validation fun!
	 * 
	 */
	
	$scope.username = "";
	$scope.password = "";
	$scope.goodCreds = false;
	$scope.credSubmitted = false;
	
	$scope.submitCreds = function() {
		$http({
    		method: 'POST',
    		url: 'user/checkpass',
    		params: {username: $scope.username, password: $scope.password}
        }).success(function(data,status) {
        	if (data == "true") {
        		$scope.goodCreds = true;
        	} else {
        		$scope.goodCreds = false;
        	}
        	$scope.credSubmitted = true;
    	});
	};
	
	
	/**
	 * End of temporary password fun
	 * 
	 ************************/
	
	
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
    	    	params: {id:$scope.labs[i].id, localIndex: i}
    	    }).success(function(data,status,headers,config) {
    	    	$scope.labs[config.params.localIndex]['count'] = data.length;
    	    });
		}
	};
	
	
	/**
	 * Load all available labs.
	 */
	$scope.loadLabs = function() {
		$http({
	    	method: 'POST',
	    	url: 'lab/all'
	    }).success(function(data,status) {
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
    	    	params: {id:$scope.selectedLab.id}
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
	    	$http({
    	    	method: 'POST',
    	    	url: 'user/modifyuser',
    	    	params: {first:user.first,last:user.last,username:user.username,password:user.password,email:user.email,
    	    		phone:user.phone,admin:user.admin,lab:user.lab.id,userid:user.idx}
    	    }).success(function(data,status) {
    	    	$scope.loadSelected();
    	    	$scope.loadUsers();
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
    	    	method: 'POST',
    	    	url: 'lab/modifylab',
    	    	params: {first:lab.first,last:lab.last,id:lab.id}
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
			    			phone: '', email: '', admin: false, lab: ''};
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
	    	$http({
    	    	method: 'POST',
    	    	url: 'user/adduser',
    	    	params: {first:user.first,last:user.last,username:user.username,password:user.password,email:user.email,
    	    		phone:user.phone,admin:user.admin,lab:user.lab.id}
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
		        labData: function () {
		        	var emptyLab = {first: '', last: ''};
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
    	    	method: 'POST',
    	    	url: 'lab/addlab',
    	    	params: {first:lab.first,last:lab.last}
    	    }).success(function(data,status) {
    	    	$scope.loadLabs();
    	    });
	    	
	    });
    };
    
    
    
    /***
     * Select or deselect all users
     */
    $scope.selectAllUsers = function() {
    	var $checked = false;
    	if ($scope.selectAllUsersCheckbox) {
    		$checked = false;
    	} else {
    		$checked = true;
    	}
    	
    	for (var i = 0; i < $scope.selectedUsers.length; i++) {
    		$scope.selectedUsers[i].selected = $checked;
    	}
    };
    
    
    
    
    /****
     * Delete users with selected checkboxes
     */
    $scope.deleteSelectedUsers = function() {
    	var toDelete = [];
    	for (var i = 0; i < $scope.selectedUsers.length; i++) {
		    if ($scope.selectedUsers[i]['selected'] == true) {
		    	toDelete.push($scope.selectedUsers[i].idx);
		    }
		}
   
    	for(var i=0;i<toDelete.length;i++) {
    		$http({
        		method: 'POST',
        		url: 'user/deleteuser',
        		params: {id: toDelete[i]}
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
		    	toDelete.push($scope.labs[i].id);
		    }
		}
   
    	for(var i=0;i<toDelete.length;i++) {
    		$http({
        		method: 'POST',
        		url: 'lab/deletelab',
        		params: {id: toDelete[i]}
    		}).success(function() {
    			$scope.loadLabs();
    		});
    	}
    };
    
    /***
     * This method launches a confirmation window.  If a result is returned, the delete user method is selected.
     */
    $scope.confirmUserDelete = function() {
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
    };
    
    /***
     * This method launches a confirmation window.  If a result is returned, the delete lab method is selected.
     */
    $scope.confirmLabDelete = function() {
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
    };
    
    //Load labs and users.
	$scope.loadLabs();
	$scope.loadSelected();
    
}]);