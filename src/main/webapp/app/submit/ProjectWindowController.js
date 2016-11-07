'use strict';

angular.module("submit").controller("ProjectWindowController", [
 '$scope', '$uibModalInstance','labList','instList','organismBuildList','ownerList',                                                     
function ($scope, $uibModalInstance, labList, instList, organismBuildList, ownerList) {
	$scope.instituteList = instList;
	$scope.labList = labList;
	$scope.organismBuildList = organismBuildList;
	$scope.ownerList = ownerList;

	console.log(ownerList);
	console.log($scope.ownerList);
	 
	$scope.project = {name: '', description: '', labs: '', institutes: '', owners: '', organismBuild: {idOrganismBuild: ''}};

	$scope.ok = function () {
		$uibModalInstance.close($scope.project);
	};
	
	$scope.cancel = function () {
		$uibModalInstance.dismiss('cancel');
	};
}]);
