/**
 * 
 */

var useradmin = angular.module('navbar', ['login','services']);

angular.module('navbar').controller("NavbarController",['$scope','$http','$rootScope','DynamicDictionary',
	function($scope,$http,$rootScope,DynamicDictionary) {
	    $rootScope.loggedUser = null;
			
		$scope.logout = function() {
			$http({
				url: "security/logout",
				method: "POST",
			}).success(function(data) {
				$rootScope.loggedUser = null;
			});
		};
		
		$scope.isAuthenticated = function() {
	    	DynamicDictionary.isAuthenticated().success(function(data) {
	    		$rootScope.loggedUser = data.username;
	    	});
	
		};
		
		$scope.isAuthenticated();
}]);