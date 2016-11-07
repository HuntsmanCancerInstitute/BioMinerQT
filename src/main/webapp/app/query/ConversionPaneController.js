'use strict';

angular.module("query").controller("ConversionPaneController", [
 '$scope', '$uibModalInstance','conversionList',                                                     
function ($scope, $uibModalInstance, conversionList) {
	$scope.conversionList = conversionList;
	$scope.conversion = null;
	
	$scope.conversionOK = function () {
		$uibModalInstance.close($scope.conversion);
	};
		
	$scope.conversionCancel = function () {
		$uibModalInstance.dismiss('cancel');
	};
}]);