/**
 * 
 */

var useradmin = angular.module('navbar', ['login','services']);

angular.module('navbar').controller("NavbarController",['$scope','$http','$rootScope','$location','$interval','$route','DynamicDictionary',
	function($scope,$http,$rootScope,$location,$interval,$route,DynamicDictionary) {
	    $rootScope.loggedUser = null;
	    $rootScope.lastLocation = "dashboard";
		$rootScope.admin = false;	
	    
		$scope.logout = function() {
			$http({
				url: "security/logout",
				method: "POST",
			}).success(function(data) {
				$rootScope.loggedUser = null;
				$scope.isAuthenticated();
			});
		};
		
		$interval(function() {$scope.isAuthenticated();},2000000);

				
		$scope.isAuthenticated = function() {
			var wasAuth = false;
			if ($rootScope.loggedUser != null) {
				wasAuth = true;
			}
			
	    	DynamicDictionary.isAuthenticated().success(function(data) {
	    		$rootScope.loggedUser = data.user;
	    		if ($rootScope.loggedUser == null) {
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
			
			if (next.restrict == "authorized" && !$scope.admin) {
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
		
		$scope.isAuthenticated();
		                          	
}]);