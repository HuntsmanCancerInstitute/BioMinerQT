'use strict';

angular.module("useradmin").controller("LiftoverSupportUploadWindowController", [
 '$scope', '$uibModalInstance','organismBuildList','supportList','chainList',                                                     
function ($scope, $uibModalInstance, organismBuildList, supportList, chainList) {
	$scope.organismBuildList = organismBuildList;
	$scope.chainList = chainList;
	$scope.selectedBuilds = [];
	$scope.build_valid = true;
	$scope.support_valid = true;
	$scope.chains_valid = true;

	$scope.chain1 = null;
	$scope.chain2 = null;
	$scope.supports = [];
	

	
	for (var i=0; i < supportList.length; i++) {
		var key = supportList[i].sourceBuild.idOrganismBuild + ":" + supportList[i].destBuild.idOrganismBuild;
		$scope.supports.push(key);
	}
	
	$scope.support = {path: '', sourceBuild: {idOrganismBuild: ''}, destBuild: {idOrganismBuild: ''}, idChainList: []};

	$scope.$watch("chain.sourceBuild.idOrganismBuild",function() {
		$scope.checkBuild();
		$scope.checkSupport();
	});
	
	$scope.$watch("chain.destBuild.idOrganismBuild",function() {
		$scope.checkBuild();
		$scope.checkSupport();
	});
	
	$scope.$watch("chain1",function() {
		$scope.buildChain();
	});
	
	$scope.$watch("chain2",function() {
		$scope.buildChain();
	});
	
	$scope.buildChain = function() {
		$scope.support.idChainList = [];
		if ($scope.chain1 != null) {
			$scope.support.idChainList.push($scope.chain1.idLiftoverChain);
		} else {
			$scope.chain2 = null;
		}
		
		if ($scope.chain2 != null) {
			if ($scope.support.idChainList.indexOf($scope.chain2.idLiftoverChain) != -1) {
				$scope.chains_valid = false;
			} else {
				$scope.chains_valid = true;
				$scope.support.idChainList.push($scope.chain2.idLiftoverChain);
			}
		} else {
			$scope.chains_valid = true;
		}
	}
	
	
	//Make sure the conversion doesn't exist already
	$scope.checkSupport = function() {
		if ($scope.support.sourceBuild.idOrganismBuild && $scope.support.destBuild.idOrganismBuild) {
			var key = $scope.support.sourceBuild.idOrganismBuild + ":" + $scope.support.destBuild.idOrganismBuild;
			if ($scope.supports.indexOf(key) != -1) {
				$scope.support_valid = false;
			} else {
				$scope.support_valid = true;
			}
		}
	}
	
	//Make sure the conversion makes sense
	$scope.checkBuild = function() {
		if ($scope.support.sourceBuild.idOrganismBuild && $scope.support.destBuild.idOrganismBuild) {
			if ($scope.support.sourceBuild.idOrganismBuild == $scope.support.destBuild.idOrganismBuild) {
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
	
	$scope.supportOK = function () {
	   $uibModalInstance.close($scope.support);
	};
		
	$scope.supportCancel = function () {
	  $uibModalInstance.dismiss('cancel');
	};
}]);