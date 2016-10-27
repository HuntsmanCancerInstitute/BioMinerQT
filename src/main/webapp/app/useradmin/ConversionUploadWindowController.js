'use strict';

angular.module("useradmin").controller("ConversionUploadWindowController", [
 '$scope', '$modalInstance','organismBuildList','homologyList',                                                     
function ($scope, $modalInstance, organismBuildList, homologyList) {
	$scope.organismBuildList = organismBuildList;
	$scope.selectedBuilds = [];
	$scope.build_valid = true;
	$scope.conversion_valid = true;
	$scope.file_valid = true;
	
	$scope.files = [];
	$scope.conversions = [];
	for (var i=0; i < homologyList.length; i++) {
		$scope.files.push(homologyList[i].conversionFile);
		var key = homologyList[i].sourceBuild.idOrganismBuild + ":" + homologyList[i].destBuild.idOrganismBuild;
		$scope.conversions.push(key);
	}
	
	$scope.conversion = {path: '', sourceBuild: {idOrganismBuild: ''}, destBuild: {idOrganismBuild: ''}};

	$scope.$watch("conversion.sourceBuild.idOrganismBuild",function() {
		$scope.checkBuild();
		$scope.checkConversion();
	});
	
	$scope.$watch("conversion.destBuild.idOrganismBuild",function() {
		$scope.checkBuild();
		$scope.checkConversion();
	});
	
	$scope.$watch("conversion.path",function() {
		if ($scope.conversion.path && $scope.conversion.path[0]) {
			if ($scope.files.indexOf($scope.conversion.path[0].name) != -1 || $scope.files.indexOf($scope.conversion.path[0].name + ".gz") != -1) {
				$scope.file_valid = false;
			} else {
				$scope.file_valid = true;
			}
		} 
	});
	
	$scope.checkConversion = function() {
		if ($scope.conversion.sourceBuild.idOrganismBuild && $scope.conversion.destBuild.idOrganismBuild) {
			var key = $scope.conversion.sourceBuild.idOrganismBuild + ":" + $scope.conversion.destBuild.idOrganismBuild;
			console.log(key);
			if ($scope.conversions.indexOf(key) != -1) {
				$scope.conversion_valid = false;
			} else {
				$scope.conversion_valid = true;
			}
		}
	}
	
	
	$scope.checkBuild = function() {
		if ($scope.conversion.sourceBuild.idOrganismBuild && $scope.conversion.destBuild.idOrganismBuild) {
			if ($scope.conversion.sourceBuild.idOrganismBuild == $scope.conversion.destBuild.idOrganismBuild) {
				$scope.build_valid = false;
			} else {
				$scope.build_valid = true;
			}
		} else{
			$scope.build_valid = true;
		}
	}
	
	$scope.checkValid = function(value) {
		if (value == true) {
			return true;
		} else{
			return false;
		}
	}
	
	$scope.conversionOK = function () {
	   $modalInstance.close($scope.conversion);
	};
		
	$scope.conversionCancel = function () {
	  $modalInstance.dismiss('cancel');
	};
}]);