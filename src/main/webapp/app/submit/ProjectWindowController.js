'use strict';

angular.module("submit").controller("ProjectWindowController", [
 '$scope', '$modalInstance',                                                     
function ($scope, $modalInstance) {

	$scope.project = {name: '', description: ''};

	$scope.ok = function () {
	 $modalInstance.close($scope.project);
	};
	
	$scope.cancel = function () {
	 $modalInstance.dismiss('cancel');
	};
}]);
