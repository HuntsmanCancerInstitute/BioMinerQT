angular.module("upload").controller("PreviewWindowController", [
 '$scope', '$modalInstance','filename','previewData','analysisType',
function ($scope, $modalInstance, filename, previewData, analysisType) {
	//passed in to the controller
	$scope.filename = filename;
	$scope.previewData = previewData;
	$scope.showList = []; //Indexes of selected columns.  Used for styling
	$scope.allSet = false; //True if all expected columns are set

	//Hide/show instructions
	$scope.alertHover = false;
	 
	$scope.analysisColumns = [];
	$scope.analysisType = analysisType;
	
	if (analysisType.type == "ChIPSeq" || analysisType.type == "Methylation") {
		
		$scope.analysisColumns.push({name: "Ignore", index: null, link: -1});
		$scope.analysisColumns.push({name: "Chromosome", index: -1, link: -1});
		$scope.analysisColumns.push({name: "Start", index: -1, link: -1});
		$scope.analysisColumns.push({name: "End", index: -1, link: -1});
		$scope.analysisColumns.push({name: "Log2Ratio", index: -1, link: -1});
		$scope.analysisColumns.push({name: "FDR", index: -1, link: 6});
		$scope.analysisColumns.push({name: "-10*log10(FDR)", index: -1, link: 5});
	} else if (analysisType.type == "RNASeq") {
		$scope.analysisColumns.push({name: "Ignore", index: null, link: -1});
		$scope.analysisColumns.push({name: "Gene", index: -1, link: -1});
		$scope.analysisColumns.push({name: "Log2Ratio", index: -1, link: -1});
		$scope.analysisColumns.push({name: "FDR", index: -1, link: 4});
		$scope.analysisColumns.push({name: "-10*log10(FDR)", index: -1, link: 3});
	} else if (analysisType.type == "Variant") {
		$scope.analysisColumns.push({name: "Ignore", index: null, link: -1});
		$scope.analysisColumns.push({name: "Sample", index: -1, link: -1});
	} else {
		console.log("Uh-oh");
	}
	
	
	//Create global version of analysisColumns.  This is used to track the global status of the chipSeq columns.
	$scope.globalColumns = angular.copy($scope.analysisColumns);
	
	//create header and dropdown objects (The dropdown menus are generated from copies of the chipColumn objects.  The index
	//paramter isn't used here, but it seems silly to have two version of analysisColumns...)
	$scope.header = [];
	if ($scope.previewData.length > 0) {
		for (var i=0; i<$scope.previewData[0].length; i++) {
			var h = {option: "Ignore", dropdown : angular.copy($scope.analysisColumns)};
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
				if ($scope.globalColumns[i].link == -1 || $scope.globalColumns[$scope.globalColumns[i].link].index == -1) { //If link is set, don't trip allSet
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
				} else if ($scope.header[i].dropdown[j].link == -1 || set.indexOf($scope.header[i].dropdown[$scope.header[i].dropdown[j].link].name) == -1) {
					$scope.header[i].dropdown[j].index = -1;
				} else {
					$scope.header[i].dropdown[j].index = 1;
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