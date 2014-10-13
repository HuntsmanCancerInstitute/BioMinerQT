'use strict';

angular.module("useradmin").controller("OrganismBuildController", [
 '$scope','$http', '$modalInstance','organismBuildData','organismList','title','bFace','organismBuildList',
function ($scope, $http, $modalInstance, organismBuildData, organismList, title, bFace,organismBuildList) {
 
	$scope.title = title;
	$scope.bFace = bFace;
	$scope.organismBuildData = angular.copy(organismBuildData);
	$scope.organismList = organismList;
	$scope.organismBuildList = organismBuildList;
	$scope.buildList = [];
	$scope.originalBuild = organismBuildData.name;
	
	
	for (var i=0;i<organismBuildList.length;i++) {
		$scope.buildList.push(organismBuildList[i].name);
	}
	
	$scope.checkBuild = function(value) {
		if (value == $scope.originalBuild) {
			return true;
		} else {
			return $scope.buildList.indexOf(value) === -1;
		}
	};
	
	$scope.ok = function () {
	 $modalInstance.close($scope.organismBuildData);
	};
	
	$scope.cancel = function () {
	 $modalInstance.dismiss('cancel');
	};
}]);