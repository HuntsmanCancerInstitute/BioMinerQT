angular.module("useradmin").controller("AnnotationPreviewController", [
 '$scope', '$uibModalInstance','filename','previewData',
function ($scope, $uibModalInstance, filename, previewData) {
	//passed in to the controller
	$scope.filename = filename;
	$scope.previewData = previewData;
	$scope.showList = []; //Indexes of selected columns.  Used for styling
	$scope.allSet = false; //True if all expected columns are set
	
	//Hide/show instructions
	$scope.alertHover = false;
	 
	//I am hard-coding ChIP-seq columns for now.  This should be passed in to the controller once we have
	//the other analysis types ready.
	$scope.annColumns = [];
	$scope.annColumns.push({name: "Ignore", index: null, required: false});
	$scope.annColumns.push({name: "Ensembl", index: -1, required: true});
	$scope.annColumns.push({name: "Common", index: -1, required: true});
	$scope.annColumns.push({name: "RefSeq", index: -1, required: false});
	$scope.annColumns.push({name: "UCSC", index: -1, required: false});
	
	//Create global version of annColumns.  This is used to track the global status of the chipSeq columns.
	$scope.globalColumns = angular.copy($scope.annColumns);
	
	//create header and dropdown objects (The dropdown menus are generated from copies of the annColumn objects.  The index
	//paramter isn't used here, but it seems silly to have two version of annColumns...)
	$scope.header = [];
	if ($scope.previewData.length > 0) {
		for (var i=0; i<$scope.previewData[0].length; i++) {
			var h = {option: "Ignore", dropdown : angular.copy($scope.annColumns)};
			$scope.header.push(h);
		}
	}
	
	//This is fired whenever a column drop-down is changed
	$scope.$watch('header',function() {
		//inialize arrays.  These contain the list of selcted columns and their indexes
		var set = []; //List of selected column names
		$scope.showList = []; //List of selected column indexes
		
		//Go through head header select and find 'set' columns.  Store information 
		// in the arrays.
		for (var i=0; i<$scope.header.length; i++) {
			var selectedOption = $scope.header[i].option;
			
			if (selectedOption != "Ignore") {
				set.push(selectedOption);
				$scope.showList.push(i);
			} 
		}
		
		//update global chip object based on 'set' columns
		var allSet = true; //if this stays true, all necessary columns have been set and the parse button will become active
		for (var i=1; i<$scope.globalColumns.length; i++) {
			var idx = set.indexOf($scope.globalColumns[i].name);
			if (idx != -1) {
				$scope.globalColumns[i].index = $scope.showList[idx];
			} else {
				$scope.globalColumns[i].index = -1;
				if ($scope.globalColumns[i].required) {
					allSet = false;
				}
			}
		}
		$scope.allSet = allSet; //Set $scope allset to local value
		
		//Go through each header select and disable/enable choices
		for (var i=0; i<$scope.header.length; i++) {
	
			for (var j=0; j<$scope.header[i].dropdown.length; j++) {
				var idx = set.indexOf($scope.header[i].dropdown[j].name);
			
				if (idx != -1) {
					$scope.header[i].dropdown[j].index = 1;
				}  else {
					$scope.header[i].dropdown[j].index = -1;
				}
			}
		
		}
		
	},true);
	
	
		
	//modal dismissed
	$scope.cancel = function () {
		$uibModalInstance.dismiss('cancel');
	};
	
	$scope.ok = function() {
		$uibModalInstance.close($scope.globalColumns);
	};
	
	
}]);