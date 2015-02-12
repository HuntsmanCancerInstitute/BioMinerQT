/**
 * 
 */

var useradmin = angular.module('navbar', ['login','services','error']);

angular.module('navbar').controller("NavbarController",['$modal','$scope','$http','$rootScope','$location','$interval','$route','DynamicDictionary',
	function($modal,$scope,$http,$rootScope,$location,$interval,$route,DynamicDictionary) {
	    $rootScope.loggedUser = null;
	    $rootScope.lastLocation = "dashboard";
		$rootScope.admin = false;
		$rootScope.helpMessage = "";
	
		$scope.logout = function() {
			$http({
				url: "security/logout",
				method: "POST",
			}).success(function(data) {
				$rootScope.isAuthenticated();
				$location.path("/dashboard");
				$route.reload();
			});
		};
		
		$rootScope.isAuthenticated = function() {
			//console.log("Calling isAuth");
			var wasAuth = false;
			if ($rootScope.loggedUser != null) {
				wasAuth = true;
			}
			
	    	DynamicDictionary.isAuthenticated().success(function(data) {
	    		$rootScope.loggedUser = data.user;
	    		if ($rootScope.loggedUser == null) {
	    			if (angular.isDefined($rootScope.checkInterval)) {
		        		//console.log("Stopping checking (isAuth)");
	        			$interval.cancel($rootScope.checkInterval);
	        		}
	    			$rootScope.admin = false;
	    			if (wasAuth) {
						console.log("Reloading");
						$route.reload();
					} 
	    		} else {
					var admin = false;
					for (var i=0;i<$rootScope.loggedUser.roles.length;i++) {
						if ($rootScope.loggedUser.roles[i].name == "admin") {
							admin = true;
						}
					}
					$rootScope.admin = admin;
				}
	    	});
		};
		
		
		                 		
		$rootScope.$on('$routeChangeStart',function(event, next, prev) {
			var url = "/dashboard";
			if (prev != undefined) {
				url = prev.originalPath;
			}
			
			if (next.restrict == "authorized" && !$scope.admin && $rootScope.loggedUser == null) {
				$scope.isAuthenticated();
				$rootScope.lastLocation = url;
				$location.path("/login");
			} else if (next.restrict == "authenticated" && $rootScope.loggedUser == null) {
				$scope.isAuthenticated();
				$rootScope.lastLocation = url;
				$location.path("/login");
			} else {
				$rootScope.lastLocation = url;
			}
		
		});  
		
		$scope.displayHelp = function() {
			$modal.open({
	    		templateUrl: 'app/common/userError.html',
	    		controller: 'userErrorController',
	    		resolve: {
	    			title: function() {
	    				return "Help";
	    			},
	    			message: function() {
	    				return $rootScope.helpMessage;
	    			}
	    		}
	    	});
		};
		
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
    	    		phone:user.phone,admin:$rootScope.admin,lab:lids,institutes:iids,idUser:user.idUser}
    	    }).success(function(data,status) {
    	    });
	    	
	    });
    };
		
		                          	
}]);