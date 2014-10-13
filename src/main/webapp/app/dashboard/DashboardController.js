'use strict';

/**
 * DashboardController
 * @constructor
 */
var dashboard = angular.module('dashboard', ['services']);

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
    
    $scope.chipseq.data = dashboardService.getChipSeqData();
	$scope.bisseq.data =  dashboardService.getBisSeqData();
    $scope.rnaseq.data =  dashboardService.getRNASeqData();

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

