var login = angular.module("login",['services'])

.controller("LoginController", ['$scope','$http','$rootScope','$location','$interval','$route','DynamicDictionary',
                                                      
	function($scope, $http, $rootScope, $location, $interval, $route, DynamicDictionary) {
		$scope.user = {username : "", password: "", passwordconfirm: "", guid: ""};
		$scope.remember = false;
		$scope.message = null;
		$scope.theUrl = "";

	
	$scope.issueEmail = "";
	if ($scope.loggedUser != null) {
		$scope.issueEmail = $scope.loggedUser.email;
	}
	$scope.issueProblem = "";
	$scope.issueMessage = null;
		
		$rootScope.checkInterval = undefined;

		$scope.close = function () {
            $location.path($rootScope.lastLocation);
		};

	
		$scope.submitIssue = function() {

			$http({
	    		method: 'POST',
	    		url: 'shared/reportissue',
	    		params: {email: $scope.issueEmail, problem: $scope.issueProblem}
	        }).success(function(data,status) {
	        	$scope.issueMessage = data;
	        	if (angular.isDefined($rootScope.checkInterval)) {
        			$interval.cancel($rootScope.checkInterval);
        		}

        		$location.path($rootScope.lastLocation);

	    	});
		};		

		
		$scope.clear = function () {
            $location.path($rootScope.lastLocation);
			$route.reload();

		};
		
		$scope.submitChange = function() {
		$scope.user.guid = $location.search()['guid'];

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

        		$location.path("/dashboard");

	    	});
		};		
		

		$scope.submitReset = function() {
		$scope.theUrl = $location.absUrl();

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

        		$location.path("/dashboard");
        		$route.reload();

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
	        		$rootScope.checkInterval = $interval(function() {$rootScope.isAuthenticated();},data.timeout + 1800000);
	        		
	        		var admin = false;
					for (var i=0;i<$rootScope.loggedUser.roles.length;i++) {
						if ($rootScope.loggedUser.roles[i].name == "admin") {
							admin = true;
						}
					}
					$rootScope.admin = admin;
		        	$location.path($rootScope.lastLocation);
		        	if (angular.equals($rootScope.lastLocation,"/login")) {
		        		$location.path("/dashboard");
		        		}
		        	$route.reload();
	        	} else {
	        		//console.log("no user, no checking");
	        	}
	    	});
		};
		
		$rootScope.helpMessage = "" +
			"<h1>Login Page / Report Issue</h1>" +
			"<p>The login page can be used to gain access to Biominer.  Users that don't log in will be treated as guests and will only have access to public data. " +
			"Guests also aren't allowed to submit new analyses.  The login page can also be used to reset passwords using the <strong>Reset Password</strong> link. New " +
			"users can request account by clicking on the <strong>Sign up</strong> link at the top of the page.  New accounts can be approved by the Biominer team or " +
			"the PI of the lab selected in the sign up form.</p>" +
			"<p>If a user encounters a bug or wishes to request a feature, they can send a message to the Biominer Team using the <strong>Report Issue</strong> page. " +
			"If the user is familiar with SourceForge and has a SourceForge account, they can submit tickets directly and can monitor the progress.</p>";
			
	}
]);