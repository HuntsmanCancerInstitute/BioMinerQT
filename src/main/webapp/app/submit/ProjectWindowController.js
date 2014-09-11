'use strict';

angular.module("submit").controller("ProjectWindowController", [
 '$scope', '$modalInstance','labList','instList','organismBuildList',                                                     
function ($scope, $modalInstance, labList, instList, organismBuildList) {
	$scope.instituteList = instList;
	$scope.labList = labList;
	$scope.organismBuildList = organismBuildList;

	$scope.project = {name: '', description: '', labs: '', institutes: '', organismBuild: {idOrganismBuild: ''}};

	$scope.ok = function () {
	 $modalInstance.close($scope.project);
	};
	
	$scope.cancel = function () {
	 $modalInstance.dismiss('cancel');
	};
}]);
