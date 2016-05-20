'use strict';

/**
 * DashboardController
 * @constructor
 */
var dashboard = angular.module('dashboard', ['services','error','chart.js']);

angular.module('dashboard')

.controller('DashboardController', 
[ '$rootScope','$scope','$http','$window',
              
function($rootScope, $scope, $http, $window) {

    $scope.chipData = [[]];
    $scope.chipLabels = [];
    $scope.rnaData = [[]];
    $scope.rnaLabels = [];
    $scope.varData = [[]];
    $scope.varLabels = [];
    $scope.bisData = [[]];
    $scope.bisLabels = [];
  
    $scope.uploadSize = null;
    $scope.parsedSize = null;
    $scope.totalUsers = null;
    $scope.totalAnalyses = null;
    $scope.lastQueryDate = null;
    $scope.lastSubmissionDate = null;
    $scope.lastReportDate = null;
    $scope.daysSinceReport = null;
    $scope.daysSinceCrash = null;
    $scope.lastCrashDate = null;
    $scope.queryCount = null;
    $scope.igvCount = null;
    $scope.loginCount = null;
    $scope.crashCount = null;
    $scope.reportCount = null;
    
    $scope.$on('$locationChangeStart', function(event, next, current) {
    	//nv.render.queue = [];
    	//nv.utils.clearAllListeners();
    });
    
    $scope.getCrashCount = function() {
    	$http({
    		url: "dashboard/getCrashCount",
    		method: "GET",
    	}).success(function(data) {
    		$scope.crashCount = data.long;
    	})
    }
    
    $scope.getReportCount = function() {
    	$http({
    		url: "dashboard/getReportCount",
    		method: "GET",
    	}).success(function(data) {
    		$scope.reportCount = data.long;
    	})
    }
    
    $scope.getAnalysisCount = function() {
    	$http({
    		url: "dashboard/getTotalAnalyses",
    		method: "GET",
    	}).success(function(data) {
    		$scope.totalAnalyses = data.long;
    	})
    }
    
    $scope.getLoginCount = function() {
    	$http({
    		url: "dashboard/getLoginCount",
    		method: "GET",
    	}).success(function(data) {
    		$scope.loginCount = data.long;
    	})
    }
    
    $scope.getLastQueryDate = function() {
    	$http({
    		url: "dashboard/getLastQueryDate",
    		method: "GET",
    	}).success(function(data) {
    		$scope.lastQueryDate = data.long;
    	});
    }
    
    $scope.getLastCrashDate = function() {
    	$http({
    		url: "dashboard/getLastCrashDate",
    		method: "GET",
    	}).success(function(data) {
    		$scope.lastCrashDate = data.long;
    		var d = new Date();
    		var n = d.getTime(); 
    		var since = n - data.long;
    		$scope.daysSinceCrash = since / 8.64e7
    	});
    }
    
    $scope.getLastReportDate = function() {
    	$http({
    		url: "dashboard/getLastReportDate",
    		method: "GET",
    	}).success(function(data) {
    		$scope.lastReportDate = data.long;
    		var d = new Date();
    		var n = d.getTime(); 
    		var since = n - data.long;
    		$scope.daysSinceReport = since / 8.64e7
    	});
    }
    
    $scope.getLastSubmissionDate = function() {
    	$http({
    		url: "dashboard/getLastSubmissionDate",
    		method: "GET",
    	}).success(function(data) {
    		$scope.lastSubmissionDate = data.long;
    	});
    }
    
    $scope.getIgvCount = function() {
    	$http({
    		url: "dashboard/getIgvCount",
    		method: "GET",
    	}).success(function(data) {
    		$scope.igvCount = data.long;
    	});
    }
    
    $scope.getQueryCount = function() {
    	$http({
    		url: "dashboard/getQueryCount",
    		method: "GET",
    	}).success(function(data) {
    		$scope.queryCount = data.long;
    	});
    }
   
    
    $scope.getUploadedSize = function() {
    	$http({
    		url: "dashboard/getUploadedSize",
    		method: "GET",
    	}).success(function(data) {
    		$scope.uploadSize = data.float;
    	})
    }
    
    $scope.getParsedSize = function() {
    	$http({
    		url: "dashboard/getParsedSize",
    		method: "GET",
    	}).success(function(data) {
    		$scope.parsedSize = data.float;
    	})
    }
    
    $scope.getUserCount = function() {
    	$http({
    		url: "dashboard/getTotalUsers",
    		method: "GET",
    	}).success(function(data) {
    		$scope.totalUsers = data.long;
    	})
    }
    
    $scope.getLabCount = function() {
    	$http({
    		url: "dashboard/getTotalLabs",
    		method: "GET",
    	}).success(function(data) {
    		$scope.totalLabs = data.long;
    	})
    }
    
    
    $scope.getChipSeq = function() {
    	$http({
    		url: "dashboard/getCount",
    		method: "POST",
    		params: {type: "ChIPSeq"}
    	}).success(function(data) {
    		$scope.chipseqCount = 0;
    		
			var chipData = [];
			var chipLabels = [];
			for (var i=0; i<data.length;i++) {
    			$scope.chipseqCount += data[i].y;
    			chipData.push(data[i].y);
    			chipLabels.push(data[i].key);
    		}
    		$scope.chipData = []
    		$scope.chipData.push(chipData);
    		$scope.chipLabels = chipLabels;

    	});
    };

    $scope.getRnaSeq = function() {
    	$http({
    		url: "dashboard/getCount",
    		method: "POST",
    		params: {type: "RNASeq"}
    	}).success(function(data) {
    		$scope.rnaseqCount = 0;
    		
			var rnaData = [];
			var rnaLabels = [];
			for (var i=0; i<data.length;i++) {
    			$scope.rnaseqCount += data[i].y;
    			rnaData.push(data[i].y);
    			rnaLabels.push(data[i].key);
    		}
			$scope.rnaData = []
			$scope.rnaData.push(rnaData);
    		$scope.rnaLabels = rnaLabels;
    	});
    };
    
    $scope.getBisSeq = function() {
    	$http({
    		url: "dashboard/getCount",
    		method: "POST",
    		params: {type: "Methylation"}
    	}).success(function(data) {
    		$scope.bisseqCount = 0;
    		
			var bisData = [];
			var bisLabels = [];
			for (var i=0; i<data.length;i++) {
    			$scope.bisseqCount += data[i].y;
    			bisData.push(data[i].y);
    			bisLabels.push(data[i].key);
    		}
			$scope.bisData = []
    		$scope.bisData.push(bisData);
    		$scope.bisLabels = bisLabels;

    	});
    };
    
    $scope.getVariant = function() {
    	$http({
    		url: "dashboard/getCount",
    		method: "POST",
    		params: {type: "Variant"}
    	}).success(function(data) {
    		$scope.variantCount = 0;
    		
			var varData = [];
			var varLabels = [];
			for (var i=0; i<data.length;i++) {
    			$scope.variantCount += data[i].y;
    			varData.push(data[i].y);
    			varLabels.push(data[i].key);
    		}
			$scope.varData = []
    		$scope.varData.push(varData);
    		$scope.varLabels = varLabels;	

    	});
    }
    
    $scope.refreshCharts = function() {
    	for (var i=0;i<$scope.rnaData[0].length;i++) {
			$scope.rnaData[0][i] = 0;
		}
    	for (var i=0;i<$scope.chipData[0].length;i++) {
			$scope.chipData[0][i] = 0;
		}
    	for (var i=0;i<$scope.rnaData[0].length;i++) {
			$scope.bisData[0][i] = 0;
		}
    	for (var i=0;i<$scope.rnaData[0].length;i++) {
			$scope.varData[0][i] = 0;
		}
    	
    	setTimeout(function() {
    		$scope.getRnaSeq();
    	},500);
    	
    	setTimeout(function() {
    		$scope.getChipSeq();
    	},1000);
    	setTimeout(function() {
    		$scope.getBisSeq();
    	},1500);
    	setTimeout(function() {
    		$scope.getVariant();
    	},2000);
    };
    
    $scope.refreshCounts = function() {
    	$scope.getUploadedSize();
    	$scope.getParsedSize();
    	$scope.getUserCount();
    	$scope.getLabCount();
    	$scope.getAnalysisCount();
    	$scope.getQueryCount();
    	$scope.getIgvCount();
    	$scope.getLastQueryDate();
    	$scope.getLoginCount();
    	$scope.getLastSubmissionDate();
    	$scope.getLastCrashDate();
    	$scope.getLastReportDate();
    };
    
    $scope.refreshAmin = function() {
    	$scope.getCrashCount();
    	$scope.getReportCount();
    }
    
    $scope.refreshAll = function() {
    	if ($rootScope.admin) {
    		$scope.refreshAmin();
    	}
    	$scope.refreshCharts();
    	$scope.refreshCounts();
    	//nv.render.queue = [];
    	//nv.utils.clearAllListeners();
    }
    
    
    
    $scope.refreshAll();

    $rootScope.helpMessage = "<h2>Welcome to Biominer!</h2>" 
    	+ "<p>BioMinerQT is a web-based tool that internalizes ChIPseq, RNAseq, "
    	+ "bisulfite, and variant analysis into a searchable database. The goal "
    	+ "of the BioMinerQT is to allow researchers to mine all the data that " 
    	+ "has been uploaded without the help of a bioinformatician. </p>"
    	+ "<p>If you encounter any errors, please submit a <a href='#/reportIssue' style='color: red'>bug report.</a> "
    	+ "If you need a login or have any questions about Biominer, please " 
    	+ "<a href='mailto:BioMinerSupport@hci.utah.edu' style='color: red'>contact</a> the developers. "
    	+ "The source code and war can be downloaded from <a href='https://sourceforge.net/projects/biominerqt/'>sourceforge.</a></p>";
    


 }
]);

