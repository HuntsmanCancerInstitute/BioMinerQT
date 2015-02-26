'use strict';

/**
 * DashboardController
 * @constructor
 */
var dashboard = angular.module('dashboard', ['services','error','nvd3']);

angular.module('dashboard')

.controller('DashboardController', 
[ '$rootScope','$scope','$http','$window',
              
function($rootScope, $scope, $http,$window) {

    $scope.rnaseq = [];
    $scope.chipseq = [];
    $scope.bisseq = [];
    $scope.variant = [];
    
   
      
    $scope.options = {
        chart: {
            type: 'pieChart',
            height: 400,
            donut: true,
            x: function(d){return d.key;},
            y: function(d){return d.y;},
            showLabels: false,
            donutLabelsOutside: false,
            tooltips: true,
            
            transitionDuration: 500,
            labelThreshold: 0.01,
            legend: {
            	rightAlign: false,
                margin: {
                    top: 5,
                    right: 35,
                    bottom: 5,
                    left: 0
                }
            }
        }
    }
    
    //Setup rnaSeq options
    $scope.rnaOptions = angular.copy($scope.options);
    $scope.rnaOptions.chart.tooltipContent = function(key, y, e, graph) {
    	var total = 0;
    	$scope.rnaseq.forEach(function (d) {
    	    total = total + d.y;
    	});
       	return '<h3 style="background-color: '
            + e.color + '">' + key + '</h3>'
            + '<p>' +  Math.trunc(y) + ' ( ' + Math.round(y/total*100,2) + '% )</p>';
   	};
   	
   	//Setup chip options
    $scope.chipOptions = angular.copy($scope.options);
   	$scope.chipOptions.chart.tooltipContent = function(key, y, e, graph) {
    	var total = 0;
    	$scope.chipseq.forEach(function (d) {
    	    total = total + d.y;
    	});
       	return '<h3 style="background-color: '
            + e.color + '">' + key + '</h3>'
            + '<p>' +  Math.trunc(y) + ' ( ' + Math.round(y/total*100,2) + '% )</p>';
   	};
   	
   	//setup bisseq options
    $scope.bisOptions = angular.copy($scope.options);
    $scope.bisOptions.chart.tooltipContent = function(key, y, e, graph) {
    	var total = 0;
    	$scope.bisseq.forEach(function (d) {
    	    total = total + d.y;
    	});
       	return '<h3 style="background-color: '
            + e.color + '">' + key + '</h3>'
            + '<p>' +  Math.trunc(y) + ' ( ' + Math.round(y/total*100,2) + '% )</p>';
   	};
   	
   	//setup var options
    $scope.varOptions = angular.copy($scope.options);
    $scope.varOptions.chart.tooltipContent = function(key, y, e, graph) {
    	var total = 0;
    	$scope.variant.forEach(function (d) {
    	    total = total + d.y;
    	});
       	return '<h3 style="background-color: '
            + e.color + '">' + key + '</h3>'
            + '<p>' +  Math.trunc(y) + ' ( ' + Math.round(y/total*100,2) + '% )</p>';
   	};
    
   
    
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
    		$scope.chipseq = data;
    	});
    };

    $scope.getRnaSeq = function() {
    	$http({
    		url: "dashboard/getCount",
    		method: "POST",
    		params: {type: "RNASeq"}
    	}).success(function(data) {
    		$scope.rnaseq = data;
    	});
    };
    
    $scope.getBisSeq = function() {
    	$http({
    		url: "dashboard/getCount",
    		method: "POST",
    		params: {type: "Methylation"}
    	}).success(function(data) {
    		$scope.bisseq = data;
    	});
    };
    
    $scope.getVariant = function() {
    	$http({
    		url: "dashboard/getCount",
    		method: "POST",
    		params: {type: "Variant"}
    	}).success(function(data) {
    		$scope.variant = data;
    	});
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

    $rootScope.helpMessage = "<h1>Welcome to the Biominer beta!</h1>" 
    	+ "<p>BioMinerQT is a web-based tool that internalizes ChIPseq, RNAseq, "
    	+ "bisulfite, and variant analysis into a searchable database. The goal "
    	+ "of the BioMinerQT is to allow researchers to mine all the data that " 
    	+ "has been uploaded without the help of a bioinformatician. </p>"
    	+ "<p>If you encounter any errors, please submit a <a href='#/reportIssue' style='color: red'>bug report.</a> "
    	+ "If you need a login or have any questions about Biominer, please " 
    	+ "<a href='mailto:BioMinerSupport@hci.utah.edu' style='color: red'>contact</a> the developers "
    	+ "The source code and war can be downloaded from <a href='https://sourceforge.net/projects/biominerqt/'>sourceforge.</a>";
    

//    $scope.rnaseq.options = {
//        series: {
//            pie: {
//                show: true,
//                radius: 1,
//                label: {
//                    radius: 2 / 3,
//                    formatter: function(label, series) {
//                        return '<div class="pie">' + label + ': ' + series.data[0][1] + '<br>(' + Math.round(series.percent) + '%)</div>';
//                    }
//                }
//            }
//        },
//        legend: {
//            show: false
//        }
//    };
//    $scope.chipseq.options = {
//            series: {
//                pie: {
//                    show: true,
//                    radius: 1,
//                    label: {
//                        radius: 2 / 3,
//                        formatter: function(label, series) {
//                            return '<div class="pie">' + label + ': ' + series.data[0][1] + '<br>(' + Math.round(series.percent) + '%)</div>';
//                        }
//                    }
//                }
//            },
//            legend: {
//                show: false
//            }
//        };
//    $scope.bisseq.options = {
//            series: {
//                pie: {
//                    show: true,
//                    radius: 1,
//                    label: {
//                        radius: 2 / 3,
//                        formatter: function(label, series) {
//                            return '<div class="pie">' + label + ': ' + series.data[0][1] + '<br>(' + Math.round(series.percent) + '%)</div>';
//                        }
//                    }
//                }
//            },
//            legend: {
//                show: false
//            }
//        };
//    $scope.variant.options = {
//            series: {
//                pie: {
//                    show: true,
//                    radius: 1,
//                    label: {
//                        radius: 2 / 3,
//                        formatter: function(label, series) {
//                            return '<div class="pie">' + label + ': ' + series.data[0][1] + '<br>(' + Math.round(series.percent) + '%)</div>';
//                        }
//                    }
//                }
//            },
//            legend: {
//                show: false
//            }
//        };
 }
]);

