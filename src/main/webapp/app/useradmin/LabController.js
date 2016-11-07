'use strict';

angular.module("useradmin").controller("LabController", [
 '$scope','$http', '$uibModalInstance','labData','title','bFace',
function ($scope, $http, $uibModalInstance, labData, title, bFace) {
 
	$scope.title = title;
	$scope.bFace = bFace;
	$scope.lab = angular.copy(labData);
	$scope.usedNames = [];
	
	

	
	$scope.ok = function () {
	 $uibModalInstance.close($scope.lab);
	};
	
	$scope.cancel = function () {
	 $uibModalInstance.dismiss('cancel');
	};
	
	
}]);