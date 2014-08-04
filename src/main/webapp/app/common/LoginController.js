var login = angular.module("login",['services'])

.controller("LoginController", ['$scope','$http','$rootScope','DynamicDictionary',
                                                      
	function($scope, $http, $rootScope, DynamicDictionary) {
		$scope.user = {username : "", password: ""};
		$scope.remember = false;
		$scope.message = null;
		
		
		$scope.submitCreds = function() {
			
			$http({
	    		method: 'POST',
	    		url: 'security/login',
	    		params: {username: $scope.user.username, password: $scope.user.password, remember: $scope.remember}
	        }).success(function(data,status) {
	        	console.log(data.message);
	        	$rootScope.loggedUser = data.username;
	        	$scope.message = data.message;	
	    	});
		};
	}
]);