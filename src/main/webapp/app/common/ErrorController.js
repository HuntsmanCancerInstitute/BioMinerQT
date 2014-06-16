var error = angular.module("error",[])

.controller("ErrorController", ['$scope','$modalInstance','file',
                                                      
function($scope, $modal, file) {
	$scope.file = file;
	
	$scope.ok = function() {
		$modal.dismiss();
	};
}]);