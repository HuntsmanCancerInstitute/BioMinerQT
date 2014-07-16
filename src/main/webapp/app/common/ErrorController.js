var error = angular.module("error",[])

.controller("ErrorController", ['$scope','$modalInstance','$sce','title','message',
                                                      
function($scope, $modal, $sce, title, message) {
	$scope.title = title;
	$scope.message = $sce.trustAsHtml(message);
	
	$scope.ok = function() {
		$modal.dismiss();
	};
}]);