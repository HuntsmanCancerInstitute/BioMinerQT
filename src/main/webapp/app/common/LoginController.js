var login = angular.module("login",['services'])

.controller("LoginController", ['$scope','$http','$rootScope','$location','$interval','DynamicDictionary',
                                                      
	function($scope, $http, $rootScope, $location, $interval, DynamicDictionary) {
		$scope.user = {username : "", password: "", passwordconfirm: "", guid: ""};
		$scope.remember = false;
		$scope.message = null;
		$scope.theUrl = "";
		
		$rootScope.checkInterval = undefined;
		
		$scope.clear = function () {
            $location.path($rootScope.lastLocation);
		};
		
		$scope.submitChange = function() {
		$scope.user.guid = $location.search()['guid'];

		//console.log("before calling changepassword, username:, guid: ");
		//console.log($scope.user.username);
		//console.log($scope.user.guid);

			$http({
	    		method: 'POST',
	    		url: 'security/changepassword',
	    		params: {username: $scope.user.username, password: $scope.user.password, passwordconfirm: $scope.user.passwordconfirm, guid: $scope.user.guid, remember: $scope.remember}
	        }).success(function(data,status) {
	        	$scope.message = data;
	        	if (angular.isDefined($rootScope.checkInterval)) {
	        		//console.log("Stopping checking (submit)");
        			$interval.cancel($rootScope.checkInterval);
        		}

        		//$location.path($rootScope.lastLocation);

	    	});
		};		
		

		$scope.submitReset = function() {
		$scope.theUrl = $location.absUrl();

		//console.log("before calling resetpassword, username: ");
		//console.log($scope.user.username);
		//console.log($scope.theUrl);
			$http({
	    		method: 'POST',
	    		url: 'security/resetpassword',
	    		params: {username: $scope.user.username, theUrl: $scope.theUrl, remember: $scope.remember}
	        }).success(function(data,status) {
	        	$scope.message = data;
	        	if (angular.isDefined($rootScope.checkInterval)) {
	        		//console.log("Stopping checking (submit)");
        			$interval.cancel($rootScope.checkInterval);
        		}

        		//$location.path($rootScope.lastLocation);

	    	});
		};		

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
	        		$rootScope.checkInterval = $interval(function() {$rootScope.isAuthenticated();},data.timeout + 180000);
	        		
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