/**
 * 
 */

var useradmin = angular.module('navbar', ['login']);

angular.module('navbar').controller("NavbarController",['$scope','$http','$modal','$rootScope',
	function($scope,$http,$modal,$rootScope) {
	    $rootScope.loggedUser = null;
		$scope.getLogin = function() {
			var modalInstance = $modal.open({
			      templateUrl: 'app/common/login.html',
			      controller: 'LoginController',
			});
	
			modalInstance.result.then(function (user) {
				  $rootScope.loggedUser = user;
			});
		};
			
		$scope.logout = function() {
			$rootScope.loggedUser = null;
		};
}]);