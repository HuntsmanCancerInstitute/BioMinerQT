'use strict';

angular.module("useradmin").controller("LabController", [
 '$scope','$http', '$modalInstance','instituteList','labData','title','bFace',
function ($scope, $http, $modalInstance, instituteList, labData, title, bFace) {
 
	$scope.title = title;
	$scope.bFace = bFace;
	$scope.lab = angular.copy(labData);
	$scope.usedNames = [];
	
	$scope.availInst = instituteList;

	
	$scope.ok = function () {
	 $modalInstance.close($scope.lab);
	};
	
	$scope.cancel = function () {
	 $modalInstance.dismiss('cancel');
	};
	
	
	//The institute objects in 'lab' object aren't the same objects in the full institute list.  This method
	//replaces the 'lab' institute objects with the list versions. This allows the select box to be pre-loaded
	//on edits.
	$scope.setInst = function() {
		if(!angular.isUndefined($scope.lab.institutes) || $scope.lab.institutes != null) {
			var idList = [];
			for (var i = 0; i < $scope.lab.institutes.length; i++) {
				idList.push($scope.lab.institutes[i].idInstitute);
			}
			
			var instList = [];
			for (var i=0; i < $scope.availInst.length; i++) {
				if (idList.indexOf($scope.availInst[i].idInstitute) != -1) {
					instList.push($scope.availInst[i]);
				}
			}
			
			$scope.lab.institutes = instList;
		}
	};
	
	$scope.setInst();
	
}]);