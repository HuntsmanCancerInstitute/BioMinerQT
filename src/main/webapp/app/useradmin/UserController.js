 'use strict';

angular.module("useradmin").controller("UserController", [
 '$scope','$http', '$modalInstance','labList','instituteList','userData','title','bFace',
function ($scope, $http, $modalInstance, labList, instituteList, userData, title,bFace) {
 
	$scope.title = title;
	$scope.bFace = bFace;
	$scope.availLabs = labList;
	$scope.availInst = instituteList;
	$scope.user = angular.copy(userData);
	$scope.usedNames = [];
	$scope.originalUsername = userData.username;
	
	$http({
		method: 'GET',
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
		if ($scope.originalUsername == value) {
			return true;
		} else {
			return $scope.usedNames.indexOf(value) === -1;
		}
		
	};
	
}]);