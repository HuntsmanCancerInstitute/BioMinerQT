'use strict';

angular.module("useradmin").controller("LiftoverChainUploadWindowController", [
 '$scope', '$uibModalInstance','organismBuildList','chainList',                                                     
function ($scope, $uibModalInstance, organismBuildList, chainList) {
	$scope.organismBuildList = organismBuildList;
	$scope.selectedBuilds = [];
	$scope.build_valid = true;
	$scope.chain_valid = true;
	$scope.file_valid = true;
	
	$scope.files = [];
	$scope.chains = [];
	
	for (var i=0; i < chainList.length; i++) {
		$scope.files.push(chainList[i].chainFile);
		var key = chainList[i].sourceBuild.idOrganismBuild + ":" + chainList[i].destBuild.idOrganismBuild;
		$scope.chains.push(key);
	}
	
	$scope.chain = {path: '', sourceBuild: {idOrganismBuild: ''}, destBuild: {idOrganismBuild: ''}};

	$scope.$watch("chain.sourceBuild.idOrganismBuild",function() {
		$scope.checkBuild();
		$scope.checkChain();
	});
	
	$scope.$watch("chain.destBuild.idOrganismBuild",function() {
		$scope.checkBuild();
		$scope.checkChain();
	});
	
	$scope.$watch("chain.path",function() {
		if ($scope.chain.path && $scope.chain.path[0]) {
			if ($scope.files.indexOf($scope.chain.path[0].name) != -1 || $scope.files.indexOf($scope.chain.path[0].name + ".gz") != -1) {
				$scope.file_valid = false;
			} else {
				$scope.file_valid = true;
			}
		} 
	});
	
	$scope.checkChain = function() {
		if ($scope.chain.sourceBuild.idOrganismBuild && $scope.chain.destBuild.idOrganismBuild) {
			var key = $scope.chain.sourceBuild.idOrganismBuild + ":" + $scope.chain.destBuild.idOrganismBuild;
			if ($scope.chains.indexOf(key) != -1) {
				$scope.chain_valid = false;
			} else {
				$scope.chain_valid = true;
			}
		}
	}
	
	$scope.checkBuild = function() {
		if ($scope.chain.sourceBuild.idOrganismBuild && $scope.chain.destBuild.idOrganismBuild) {
			if ($scope.chain.sourceBuild.idOrganismBuild == $scope.chain.destBuild.idOrganismBuild) {
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
	
	$scope.chainOK = function () {
	   $uibModalInstance.close($scope.chain);
	};
		
	$scope.chainCancel = function () {
	  $uibModalInstance.dismiss('cancel');
	};
}]);