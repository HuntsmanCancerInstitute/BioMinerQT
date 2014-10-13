'use strict';

angular.module("useradmin").controller("OrganismController", [
 '$scope','$http', '$modalInstance','organismData','organismList','title','bFace',
function ($scope, $http, $modalInstance, organismData, organismList, title, bFace) {
 
	$scope.title = title;
	$scope.bFace = bFace;
	$scope.organismData = angular.copy(organismData);
	$scope.organismList = organismList;
	$scope.commonList = [];
	$scope.binomialList = [];
	$scope.originalCommon = organismData.common;
	$scope.originalBinomial = organismData.binomial;
	
	for (var i=0;i<organismList.length;i++) {
		$scope.commonList.push(organismList[i].common);
		$scope.binomialList.push(organismList[i].binomial);
	}
	
	$scope.checkCommon = function(value) {
		if (value == $scope.originalCommon) {
			return true;
		} else {
			return $scope.commonList.indexOf(value) === -1;
		}
	};
	
	$scope.checkBinomial = function(value) {
		if (value == $scope.originalBinomial) {
			return true;
		} else {
			return $scope.binomialList.indexOf(value) === -1;
		}
	};
	
	$scope.ok = function () {
	 $modalInstance.close($scope.organismData);
	};
	
	$scope.cancel = function () {
	 $modalInstance.dismiss('cancel');
	};
}]);