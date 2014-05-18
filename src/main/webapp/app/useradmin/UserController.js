'use strict';

angular.module("useradmin").controller("UserController", [
 '$scope','$http', '$modalInstance','labList','userData','title','bFace',
function ($scope, $http, $modalInstance, labList, userData, title,bFace) {
 
	$scope.title = title;
	$scope.bFace = bFace;
	$scope.availLabs = labList;
	$scope.user = angular.copy(userData);
	$scope.usedNames = [];

	$http({
		method: 'POST',
		url: 'user/usernames'
    }).success(function(data,status) {
		$scope.usedNames = data;
	});
	
	
	$scope.ok = function () {
	 $modalInstance.close($scope.user);
	};
	
	$scope.cancel = function () {
	 $modalInstance.dismiss('cancel');
	};
	
	
	$scope.checkUsername = function(value) {
		console.log("start");
		console.log(value);
		console.log($scope.usedNames.indexOf(value));
		console.log($scope.usedNames.indexOf($scope.user.username) === -1);
		return $scope.usedNames.indexOf(value) === -1;
	};
	
	$scope.setLab = function() {
		if (!angular.isUndefined($scope.user.lab.id) || $scope.user.lab.id != null) {
			for (var i = 0; i < $scope.availLabs.length; i++) {
				if ($scope.user.lab.id == $scope.availLabs[i].id) {
					$scope.user.lab = $scope.availLabs[i];
				}
			}
		}
	};
	
	$scope.report = function() {
		console.log($scope.user.admin);
	};
	
	$scope.setLab();
	
	
	
	
}]);