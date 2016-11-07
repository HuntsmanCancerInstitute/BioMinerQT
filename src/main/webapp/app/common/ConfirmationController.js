'use strict';

/**
 * Confirmation
 * @constructor
 */
var confirmation = angular.module("confirmation",[])

.controller("ConfirmationController", ['$scope','$uibModalInstance','data',
                                                      
function($scope, $uibModalInstance, data) {
	$scope.data = data;
	
	$scope.confirm = function() {
		$uibModalInstance.close();
	};
	
	$scope.cancel = function() {
		$uibModalInstance.dismiss();
	};
}]);