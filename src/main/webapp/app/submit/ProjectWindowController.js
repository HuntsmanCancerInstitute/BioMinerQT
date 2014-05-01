'use strict';

var ProjectWindowController = angular.module("submit").controller("ProjectWindowController", [
                                                      
function ($scope, $modalInstance, projectName, projectDescription) {

	$scope.project = {name: '', description: ''};
	$scope.project.name = projectName;
	$scope.project.description = projectDescription;
	
	
	$scope.ok = function () {
	 $modalInstance.close($scope.project);
	};
	
	$scope.cancel = function () {
	 $modalInstance.dismiss('cancel');
	};
}]);
