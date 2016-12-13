'use strict';

angular.module("query").controller("LiftoverPaneController", [
 '$scope', '$uibModalInstance','liftoverList',                                                     
function ($scope, $uibModalInstance, liftoverList) {
	$scope.liftoverList = liftoverList;
	$scope.liftover = null;
	
	$scope.liftoverOK = function () {
		$uibModalInstance.close($scope.liftover);
	};
		
	$scope.liftoverCancel = function () {
		$uibModalInstance.dismiss('cancel');
	};
}]);