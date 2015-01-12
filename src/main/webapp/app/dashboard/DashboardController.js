'use strict';

/**
 * DashboardController
 * @constructor
 */
var dashboard = angular.module('dashboard', ['services','error']);

angular.module('dashboard')

.controller('DashboardController', 
[ '$rootScope','$scope', 'dashboardService',
              
function($rootScope, $scope, dashboardService) {

    $scope.rnaseq = {};
    $scope.rnaseq.data = [];
    
    $scope.chipseq = {};
    $scope.chipseq.data = [];

    $scope.bisseq = {};
    $scope.bisseq.data = [];
    
    $scope.getChipSeq = function() {
    	dashboardService.getChipSeqData().success(function(data) {
    		$scope.chipseq.data = data;
    	});
    };
    
    $scope.getRnaSeq = function() {
    	dashboardService.getRNASeqData().success(function(data) {
    		$scope.rnaseq.data = data;
    	});
    };
    
    $scope.getBisSeq = function() {
    	dashboardService.getBisSeqData().success(function(data) {
    		$scope.bisseq.data = data;
    	});
    };
    
    
    $scope.refresh = function() {
    	$scope.getChipSeq();
    	$scope.getRnaSeq();
    	$scope.getBisSeq();
    };
    
    $scope.getChipSeq();
    $scope.getRnaSeq();
    $scope.getBisSeq();
    

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
}
]);

