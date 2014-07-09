'use strict';

angular.module("useradmin").controller("UserController", [
 '$scope','$http', '$modalInstance','labList','userData','title','bFace',
function ($scope, $http, $modalInstance, labList, userData, title,bFace) {
 
	$scope.title = title;
	$scope.bFace = bFace;
	$scope.availLabs = labList;
	$scope.user = angular.copy(userData);
	$scope.usedNames = [];
	
	console.log(userData);
	console.log(labList);
	
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
	
}]);