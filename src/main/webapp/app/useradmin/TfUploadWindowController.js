'use strict';

angular.module("useradmin").controller("TfUploadWindowController", [
 '$scope', '$uibModalInstance','organismBuildList',                                                     
function ($scope, $uibModalInstance, organismBuildList) {
	$scope.organismBuildList = organismBuildList;
	 
	$scope.tf = {name: '', description: '', path: '', transformed: false, organismBuild: {idOrganismBuild: ''}};

	$scope.tfOK = function () {
	   $uibModalInstance.close($scope.tf);
	};
	
	$scope.tfCancel = function () {
	  $uibModalInstance.dismiss('cancel');
	};
}]);