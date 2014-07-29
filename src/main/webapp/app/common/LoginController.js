var login = angular.module("login",[])

.controller("LoginController", ['$scope','$modalInstance','$http',
                                                      
	function($scope, $modal, $http) {
		$scope.user = {username : "", password: ""};
		$scope.goodCreds = false;
		$scope.currAttempts = 0;
		$scope.maxAttempts = 3;
		
		$scope.submitCreds = function() {
			if ($scope.currAttempts >= $scope.maxAttempts) {
				return;
			}
			
			
			$http({
	    		method: 'POST',
	    		url: 'user/checkpass',
	    		params: {username: $scope.user.username, password: $scope.user.password}
	        }).success(function(data,status) {
	        	if (data == null || data == "") {
	        		$scope.goodCreds = false;
	        		$scope.currAttempts += 1;
	        	} else {
	        		$scope.goodCreds = true;
	        		$modal.close(data);
	        	}
	    	});
		};
	
		$scope.cancel = function() {
			$modal.dismiss();
		};
	}
]);