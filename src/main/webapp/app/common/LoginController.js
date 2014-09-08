var login = angular.module("login",['services'])

.controller("LoginController", ['$scope','$http','$rootScope','$location','$interval','DynamicDictionary',
                                                      
	function($scope, $http, $rootScope, $location, $interval, DynamicDictionary) {
		$scope.user = {username : "", password: ""};
		$scope.remember = false;
		$scope.message = null;
		
		$rootScope.checkInterval = undefined;

		$scope.submitCreds = function() {
			
			$http({
	    		method: 'POST',
	    		url: 'security/login',
	    		params: {username: $scope.user.username, password: $scope.user.password, remember: $scope.remember}
	        }).success(function(data,status) {
	        	$rootScope.loggedUser = data.user;
	        	$scope.message = data.message;
	        	if (angular.isDefined($rootScope.checkInterval)) {
	        		//console.log("Stopping checking (submit)");
        			$interval.cancel($rootScope.checkInterval);
        		}
	        	if ($rootScope.loggedUser != null) {
	        		//console.log("starting checking");
	        		$rootScope.checkInterval = $interval(function() {$rootScope.isAuthenticated();},data.timeout + 60000);
	        		
	        		var admin = false;
					for (var i=0;i<$rootScope.loggedUser.roles.length;i++) {
						if ($rootScope.loggedUser.roles[i].name == "admin") {
							admin = true;
						}
					}
					$rootScope.admin = admin;
		        	$location.path($rootScope.lastLocation);
	        	} else {
	        		//console.log("no user, no checking");
	        	}
	    	});
		};
	}
]);