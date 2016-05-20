'use strict';

angular.module("useradmin").controller("TfUploadWindowController", [
 '$scope', '$modalInstance','organismBuildList',                                                     
function ($scope, $modalInstance, organismBuildList) {
	$scope.organismBuildList = organismBuildList;
	 
	$scope.tf = {name: '', description: '', path: '', transformed: false, organismBuild: {idOrganismBuild: ''}};

	$scope.tfOK = function () {
	   $modalInstance.close($scope.tf);
	};
	
	$scope.tfCancel = function () {
	  $modalInstance.dismiss('cancel');
	};
}]);