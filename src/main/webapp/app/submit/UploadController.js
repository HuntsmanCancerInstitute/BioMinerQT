'use strict';


var upload  = angular.module('upload',  ['ui.bootstrap', 'angularFileUpload','filters', 'services', 'directives']);

angular.module("upload").controller("UploadController", ['$scope','$upload','$http',
                                                      
	function($scope, $upload, $http) {
	
		$scope.selectAllFiles = false;
		
		$scope.onFileSelect = function($files) {
			//Initialize upload status variables.
			$scope.complete = 0;
			$scope.totalGlobalSize = 0;
			$scope.currGlobalSize = 0;
			
			//Calculate global size
			for (var i=0; i<$files.length; i++) {
				$scope.totalGlobalSize += $files[i].size;
			}
			
			
			for (var i=0; i<$files.length; i++) {
				//initialize variables
				var file = $files[i];
				var index = -1;
				
				//Check to see if there are any matching files in list (match on name only)
				for (var j=0; j<$scope.$parent.uploadedFiles.length; j++) {
					if (file.name == $scope.$parent.uploadedFiles[j].name) {
						index = j;
						$scope.$parent.uploadedFiles[j].message = "started";
						$scope.$parent.uploadedFiles[j].complete = 0;
					}
				}
				
				//If no match, create new file object.
				if (index == -1) {
					var f = {name: file.name, message: "started", complete: 0};
					index = $scope.$parent.uploadedFiles.length;
					$scope.$parent.uploadedFiles.push(f);
				}
				
				//upload the file
				$scope.upload = $upload.upload({
					url: "submit/upload",
					file: file,
				}).success((function(index) {
					return function(data) {
						if (data.message != "success") {
							//Only set message on failure
							$scope.$parent.uploadedFiles[index].message = data.message;
						} else {
							//Set everything on success
							$scope.$parent.uploadedFiles[index] = data;
						}
						
						$scope.$parent.uploadedFiles[index].selected = false;
						$scope.currGlobalSize += parseInt($scope.$parent.uploadedFiles[index].size);
						$scope.complete = 100.0 * $scope.currGlobalSize / $scope.totalGlobalSize;
					};
				})(index)).progress((function (index) {
					return function(evt) {
						$scope.$parent.uploadedFiles[index].complete = 100.0 * evt.loaded / evt.total;
						if (evt.loaded != evt.total) {
							$scope.complete = 100.0 * ( evt.loaded + $scope.currGlobalSize) / $scope.totalGlobalSize;
						}
					};						
				})(index)).error((function (index) {
					return function(data) {
						$scope.$parent.uploadedFiles[index].status = "failure";
					};
				})(index));
			}
		};
		
		
		$scope.clickSelected = function() {
			$scope.selectAllFiles = !$scope.selectAllFiles;
			for (var i=0; i < $scope.$parent.uploadedFiles.length;i++) {
				$scope.$parent.uploadedFiles[i].selected = $scope.selectAllFiles;
			}
		};
		
		$scope.deleteSelected = function() {
			for (var i = 0; i < $scope.$parent.uploadedFiles.length; i++) {
				var file = $scope.$parent.uploadedFiles[i];
				var name = $scope.$parent.uploadedFiles[i].name;
				
				if (file.selected == true) {
					$scope.$parent.uploadedFiles.message = "pending";
					
					if (file.deleteUrl == null) {
						for (var j=0; j<$scope.$parent.uploadedFiles.length;j++) {
							if ($scope.$parent.uploadedFiles[j].name == name) {
								$scope.$parent.uploadedFiles.splice(j,1);
							}
						}
					} else {
						$http({
							url: file.deleteUrl,
							method: file.deleteType,
						}).success((function (name) {
							return function(data) {
								for (var j=0; j<$scope.$parent.uploadedFiles.length;j++) {
									if ($scope.$parent.uploadedFiles[j].name == name) {
										$scope.$parent.uploadedFiles.splice(j,1);
									}
								}
							};
						})(name));
					}
				}
			}
		};
		
        $scope.onload = function(data) {
        	$http.get("submit/upload")
            	.success( function(data) {
            		for (var i = 0; i < data.files.length; i++) {
            			//Set selected
        				data.files[i].selected = false;
            			
        				//Go through existing model
            			var index = -1;
            			for (var j=0; j < $scope.$parent.uploadedFiles.length; j++) {
            				//Check for matches
            				if (data.files[i].name == $scope.$parent.uploadedFiles[j].name) {
            					index = j;
            				}
            			}
            			
            			//If new, add, otherwise replace
        				if (index == -1) {
            				$scope.$parent.uploadedFiles.push(data.files[i]);
            			} else {
            				$scope.$parent.uploadedFiles[index] = data.files[i];
            			}
                	}
            	});
        };
        
        $scope.onload();
	
}]);