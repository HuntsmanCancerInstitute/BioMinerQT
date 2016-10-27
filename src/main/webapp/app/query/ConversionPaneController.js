'use strict';

angular.module("query").controller("ConversionPaneController", [
 '$scope', '$modalInstance','conversionList',                                                     
function ($scope, $modalInstance, conversionList) {
	$scope.conversionList = conversionList;
	$scope.conversion = null;
	
	$scope.conversionOK = function () {
	   $modalInstance.close($scope.conversion);
	};
		
	$scope.conversionCancel = function () {
	  $modalInstance.dismiss('cancel');
	};
}]);