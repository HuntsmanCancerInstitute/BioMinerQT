angular.module("submit").controller("SampleUploadController", [
 '$scope', '$modalInstance','previewData',
function ($scope, $modalInstance, previewData, filename) {
	//passed in to the controller
	$scope.previewData = previewData;
	$scope.filename = filename;
	$scope.showList = []; //Indexes of selected columns.  Used for styling
	$scope.allSet = false; //True if all expected columns are set

	//Hide/show instructions
	$scope.alertHover = false;
	 
	$scope.sampleInfoColumns = [];
	
	$scope.sampleInfoColumns.push({name: "Ignore", index: null, infinite: false, set: []});
	$scope.sampleInfoColumns.push({name: "Sample Name", index: -1, infinite: false, set: []});
	$scope.sampleInfoColumns.push({name: "Library Type", index: -1, infinite: false, set: []});
	$scope.sampleInfoColumns.push({name: "Prep Method", index: -1, infinite: false, set: []});
	$scope.sampleInfoColumns.push({name: "Sample Source", index: -1, infinite: false, set: []});
	$scope.sampleInfoColumns.push({name: "Sample Condition", index: -1, infinite: true, set: []});
	

	//Create global version of sampleInfoColumns.  This is used to track the global status of the chipSeq columns.
	$scope.globalColumns = angular.copy($scope.sampleInfoColumns);
	
	//create header and dropdown objects (The dropdown menus are generated from copies of the sampleInfoColumn objects.  The index
	//paramter isn't used here, but it seems silly to have two versions of sampleInfoColumns...)
	$scope.header = [];
	if ($scope.previewData.length > 0) {
		for (var i=0; i<$scope.previewData[0].length; i++) {
			var h = {option: "Ignore", dropdown : angular.copy($scope.sampleInfoColumns)};
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
			
			if ($scope.globalColumns[i].infinite == true) {
				$scope.globalColumns[i].set = [];
				
				for (var j=0;j<set.length;j++) {
					var idx = set.indexOf($scope.globalColumns[i].name,j);
					if (idx != -1) {
						$scope.globalColumns[i].index = 1;
						$scope.globalColumns[i].set.push($scope.showList[idx]);
						j = idx;
					} else {
						break;
					}
				}
				
				if ($scope.globalColumns[i].set.length == 0) {
					$scope.globalColumns[i].index = -1;
					allSet = false;
				}
			}
			else {
				var idx = set.indexOf($scope.globalColumns[i].name);
				
				if (idx != -1) {
					$scope.globalColumns[i].index = $scope.showList[idx];
				} else {
					$scope.globalColumns[i].index = -1;
					allSet = false;
				}
			}
			
		}
		$scope.allSet = allSet; //Set $scope allset to local value
		
		//Go through each header select and disable/enable choices
		for (var i=0; i<$scope.header.length; i++) {
	
			for (var j=0; j<$scope.header[i].dropdown.length; j++) {
				var idx = set.indexOf($scope.header[i].dropdown[j].name);
			
				if (idx != -1 && $scope.header[i].dropdown[j].infinite == false) {
					$scope.header[i].dropdown[j].index = 1;
				} else {
					$scope.header[i].dropdown[j].index = -1;
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