var error = angular.module("fneditor",[])

.controller("FilenameEditorController", ['$scope','$modalInstance','selected',
                                                      
function($scope, $modal, selected) {
	$scope.selected = angular.copy(selected);
	
	$scope.cancel = function() {
		$modal.dismiss();
	};
	
	$scope.ok = function() {
		$modal.close($scope.selected);
	};
}]);
