'use strict';

/**
 * DashboardController
 * @constructor
 */
var dashboard = angular.module('dashboard', ['services','error']);

angular.module('dashboard')

.controller('DashboardController', 
[ '$rootScope','$scope','$http',
              
function($rootScope, $scope, $http) {

    $scope.rnaseq = {};
    $scope.rnaseq.data = [];
    
    $scope.chipseq = {};
    $scope.chipseq.data = [];

    $scope.bisseq = {};
    $scope.bisseq.data = [];
    
    $scope.variant = {};
    $scope.variant.data = [];
    
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
    		$scope.chipseq.data = data;
    	});
    };

    $scope.getRnaSeq = function() {
    	$http({
    		url: "dashboard/getCount",
    		method: "POST",
    		params: {type: "RNASeq"}
    	}).success(function(data) {
    		$scope.rnaseq.data = data;
    	});
    };
    
    $scope.getBisSeq = function() {
    	$http({
    		url: "dashboard/getCount",
    		method: "POST",
    		params: {type: "Methylation"}
    	}).success(function(data) {
    		$scope.bisseq.data = data;
    	});
    };
    
    $scope.getVariant = function() {
    	$http({
    		url: "dashboard/getCount",
    		method: "POST",
    		params: {type: "Variant"}
    	}).success(function(data) {
    		$scope.variant.data = data;
    	});
    }
    
    $scope.getQueryDate = function() {
    	
    }
    
  
    $scope.refreshStandard = function() {
    	$scope.getChipSeq();
    	$scope.getRnaSeq();
    	$scope.getBisSeq();
    	$scope.getVariant();
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
    	$scope.refreshStandard();
    }
    
    $scope.refreshAll();

    $rootScope.helpMessage = "<p>Placeholder for dashboard help.</p>";
    

    $scope.rnaseq.options = {
        series: {
            pie: {
                show: true,
                radius: 1,
                label: {
                    radius: 2 / 3,
                    formatter: function(label, series) {
                        return '<div class="pie">' + label + ': ' + series.data[0][1] + '<br>(' + Math.round(series.percent) + '%)</div>';
                    }
                }
            }
        },
        legend: {
            show: false
        }
    };
    $scope.chipseq.options = {
            series: {
                pie: {
                    show: true,
                    radius: 1,
                    label: {
                        radius: 2 / 3,
                        formatter: function(label, series) {
                            return '<div class="pie">' + label + ': ' + series.data[0][1] + '<br>(' + Math.round(series.percent) + '%)</div>';
                        }
                    }
                }
            },
            legend: {
                show: false
            }
        };
    $scope.bisseq.options = {
            series: {
                pie: {
                    show: true,
                    radius: 1,
                    label: {
                        radius: 2 / 3,
                        formatter: function(label, series) {
                            return '<div class="pie">' + label + ': ' + series.data[0][1] + '<br>(' + Math.round(series.percent) + '%)</div>';
                        }
                    }
                }
            },
            legend: {
                show: false
            }
        };
    $scope.variant.options = {
            series: {
                pie: {
                    show: true,
                    radius: 1,
                    label: {
                        radius: 2 / 3,
                        formatter: function(label, series) {
                            return '<div class="pie">' + label + ': ' + series.data[0][1] + '<br>(' + Math.round(series.percent) + '%)</div>';
                        }
                    }
                }
            },
            legend: {
                show: false
            }
        };
}
]);

