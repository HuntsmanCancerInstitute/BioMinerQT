/**
 * 
 */

var useradmin = angular.module('navbar', ['login','services','error','dialogs.main']);

angular.module('navbar').controller("NavbarController",['$uibModal','$scope','$http','$rootScope','$location','$interval','$route','DynamicDictionary','dialogs',
	function($uibModal,$scope,$http,$rootScope,$location,$interval,$route,DynamicDictionary,dialogs) {
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
			
	    	return DynamicDictionary.isAuthenticated().success(function(data) {
	    		$rootScope.loggedUser = data.user;
	    		if ($rootScope.loggedUser == null) {
	    			if (angular.isDefined($rootScope.checkInterval)) {
	        			$interval.cancel($rootScope.checkInterval);
	        		}
	    			$rootScope.admin = false;
	    			if (wasAuth) {
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
			$rootScope.isAuthenticated().then(function() {
				var url = "/dashboard";
				if (prev != undefined) {
					url = prev.originalPath;
					
				}
			
				if (next.restrict == "authorized" && !$scope.admin && $rootScope.loggedUser == null) {
					$rootScope.lastLocation = url;
					$location.path("/login");
				} else if (next.restrict == "authenticated" && $rootScope.loggedUser == null) {
					$rootScope.lastLocation = url;
					$location.path("/login");
				} else {
					$rootScope.lastLocation = url;
				}
			});
		});  
		
		$scope.displayHelp = function() {
			dialogs.notify("Help",$rootScope.helpMessage);
		};
		
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
	    				return false;
	    			}
    		}
    	});
    	
    	modalInstance.result.then(function (user) {
    		//Create a list of lab ids
	    	
	    	$http({
    	    	method: 'POST',
    	    	url: 'user/selfmodify',
    	    	params: {first:user.first,last:user.last,username:user.username,password:user.password,email:user.email,
    	    		phone:user.phone,idUser:user.idUser}
    	    }).success(function(data,status) {
    	    });
	    	
	    });
    };
		
		                          	
}]);