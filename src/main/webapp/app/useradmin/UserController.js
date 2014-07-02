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
		return $scope.usedNames.indexOf(value) === -1;
	};
	
	$scope.setLab = function() {
		if (!angular.isUndefined($scope.user.lab) || $scope.user.lab != null) {
			//Create an array of ids.
			var ids = [];
			for (var i = 0; i < $scope.user.lab.length; i++) {
				ids.push($scope.user.lab[i].idLab);
			}
			
			var labList = [];
			for (var i = 0; i < $scope.availLabs.length; i++) {
				if (ids.indexOf($scope.availLabs[i].idLab) != -1) {
					labList.push($scope.availLabs[i]);
				}
			}
			
			$scope.user.lab = labList;
		}
	};
	
	$scope.report = function() {
		console.log($scope.user.admin);
	};
	
	$scope.setLab();
	
}]);