 'use strict';

angular.module("useradmin").controller("UserController", [
 '$scope','$http', '$uibModalInstance','labList','instituteList','userData','title','bFace','showAll',
function ($scope, $http, $uibModalInstance, labList, instituteList, userData, title, bFace, showAll) {
 
	$scope.title = title;
	$scope.bFace = bFace;
	$scope.showAll = showAll;
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
	  $uibModalInstance.close($scope.user);
	};
	
	$scope.cancel = function () {
	  $uibModalInstance.dismiss('cancel');
	};
	
	
	$scope.checkUsername = function(value) {
		if ($scope.originalUsername == value) {
			return true;
		} else {
			return $scope.usedNames.indexOf(value) === -1;
		}
		
	};
	
}]);