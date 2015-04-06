var error = angular.module("fneditor",['ui.validate'])

.controller("FilenameEditorController", ['$scope','$http','$q','$modalInstance','selected','idProject',
                                                      
function($scope, $http, $q, $modal, selected, idProject) {
	$scope.selected = angular.copy(selected);
	$scope.usedNames = [];
	$scope.usedExist = false;
	$scope.usedNew = false;
	
	
	$scope.getNames = function() {
		$http({
			url: "submit/getParsedUploadNames",
			method: "GET",
			params: {idProject: idProject},
		}).success(function(data) {
			$scope.usedNames = data;
			$scope.checkExisting();
		}).error(function(data) {
			console.log("Could not get a list of used filenames");
		});
	};
	
	
	$scope.checkExisting = function() {
		var usedExist= false;
		var usedNew = false;
		
		for (var i=0; i<$scope.selected.length;i++) {
			if ($scope.checkExistingNames($scope.selected[i])) {
				$scope.selected[i].usedExist = true;
				usedExist = true;
			} else {
				$scope.selected[i].usedExist = false;
			}
			if($scope.checkCurrentNames($scope.selected[i])) {
				$scope.selected[i].usedNew = true;
				usedNew = true;
			} else {
				$scope.selected[i].usedNew = false;
			}
		};
		
		$scope.usedExist = usedExist;
		$scope.usedNew = usedNew;
	};
	
	
	$scope.$watch("selected",function() {
		$scope.checkExisting();
	},true);

	
	$scope.checkExistingNames = function(s) {
		var idx = $scope.usedNames.indexOf(s.outname);
		
		if (idx == -1) {
			return false;
		} else {
			return true;
		}
	};
	
	$scope.checkCurrentNames = function(s) {
		var notDuplicate = false;
		for (var i=0; i<$scope.selected.length; i++) {
			if (selected[i].file.idFileUpload != s.file.idFileUpload) {
				if (selected[i].outname == s.outname) {
					notDuplicate = true;
				}
			}
		}
		return notDuplicate;
	};
		
	$scope.getNames();

	$scope.cancel = function() {
		$modal.dismiss();
	};
	
	$scope.ok = function() {
		$modal.close($scope.selected);
	};
}]);
