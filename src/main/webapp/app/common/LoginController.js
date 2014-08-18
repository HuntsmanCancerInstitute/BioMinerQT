var login = angular.module("login",['services'])

.controller("LoginController", ['$scope','$http','$rootScope','$location','DynamicDictionary',
                                                      
	function($scope, $http, $rootScope, $location, DynamicDictionary) {
		$scope.user = {username : "", password: ""};
		$scope.remember = false;
		$scope.message = null;
		
		$scope.submitCreds = function() {
			
			$http({
	    		method: 'POST',
	    		url: 'security/login',
	    		params: {username: $scope.user.username, password: $scope.user.password, remember: $scope.remember}
	        }).success(function(data,status) {
	        	$rootScope.loggedUser = data.user;
	        	$scope.message = data.message;
	        	if ($rootScope.loggedUser != null) {
	        		var admin = false;
					for (var i=0;i<$rootScope.loggedUser.roles.length;i++) {
						if ($rootScope.loggedUser.roles[i].name == "admin") {
							admin = true;
						}
					}
					$rootScope.admin = admin;
		        	$location.path($rootScope.lastLocation);
	        	}
	        	
	        	
	    	});
		};
	}
]);