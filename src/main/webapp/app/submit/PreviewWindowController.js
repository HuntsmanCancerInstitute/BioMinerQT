angular.module("upload").controller("PreviewWindowController", [
 '$scope', '$modalInstance','filename','previewData',
function ($scope, $modalInstance, filename, previewData) {
	//passed in to the controller
	$scope.filename = filename;
	$scope.previewData = previewData;
	$scope.showList = []; //Indexes of selected columns.  Used for styling
	$scope.allSet = false; //True if all expected columns are set
	
	//Hide/show instructions
	$scope.alertHover = false;
	 
	//I am hard-coding ChIP-seq columns for now.  This should be passed in to the controller once we have
	//the other analysis types ready.
	$scope.chipColumns = [];
	$scope.chipColumns.push({name: "Ignore", index: null});
	$scope.chipColumns.push({name: "Chromosome", index: -1});
	$scope.chipColumns.push({name: "Start", index: -1});
	$scope.chipColumns.push({name: "End", index: -1});
	$scope.chipColumns.push({name: "Log2Ratio", index: -1});
	$scope.chipColumns.push({name: "FDR", index: -1});
	
	//Create global version of chipColumns.  This is used to track the global status of the chipSeq columns.
	$scope.globalColumns = angular.copy($scope.chipColumns);
	
	//create header and dropdown objects (The dropdown menus are generated from copies of the chipColumn objects.  The index
	//paramter isn't used here, but it seems silly to have two version of chipColumns...)
	$scope.header = [];
	if ($scope.previewData.length > 0) {
		for (var i=0; i<$scope.previewData[0].length; i++) {
			var h = {option: "Ignore", dropdown : angular.copy($scope.chipColumns)};
			$scope.header.push(h);
		}
	}
	
	//This is fired whenever a column drop-down is changed
	$scope.$watch('header',function() {
		//inialize arrays.  These contain the list of selcted columns and their indexes
		var set = []; //List of selected columns
		$scope.showList = [];
		
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
				allSet = false;
			}
		}
		$scope.allSet = allSet; //Set $scope allset to local value
		
		//Go through each header select and disable/enable choices
		for (var i=0; i<$scope.header.length; i++) {
			if ($scope.header[i].option == "Ignore") {
				for (var j=0; j<$scope.header[i].dropdown.length; j++) {
					var idx = set.indexOf($scope.header[i].dropdown[j].name);
					if (idx != -1) {
						set.indexOf($scope.header[i].dropdown[j].index = $scope.showList[idx] );
					} else {
						set.indexOf($scope.header[i].dropdown[j].index = -1);
					}
				}
			}
		}
		
	},true);
	
	
		
	//modal dismissed
	$scope.cancel = function () {
	   $modalInstance.dismiss('cancel');
	};
	
	$scope.ok = function() {
	   $modalInstance.close($scope.globalColumns);
	};
	
	
}]);