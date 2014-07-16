'use strict';

/* Services */


var services = angular.module('services', ['ngResource','ui.bootstrap'])

.service('dashboardService', function() {
	
    this.getChipSeqData = function() {
    	var  chipseq_data = 
    	 [
			{ label: "Mouse",    data: [33]},
			{ label: "Zebrafish",data: [23]},
			{ label: "Chicken",  data: [9]},
			{ label: "Human  ",  data: [18]}
		];
    	return chipseq_data;
    }; 
    
    this.getRNASeqData = function() {
    	var  rnaseq_data = 
    	  [
		    { label: "Mouse",    data: [[1,30]]},
			{ label: "Zebrafish",data: [[1,40]]},
			{ label: "Chicken",  data: [[1,9]]},
			{ label: "Human  ",  data: [[1,8]]}
          ];
    	return rnaseq_data;
    };  
    this.getBisSeqData = function( ){
    	var  bisseq_data = 
    	  [
  			{ label: "Mouse",    data: [[1,3]]},
  			{ label: "Zebrafish",data: [[1,4]]},
  			{ label: "Chicken",  data: [[1,2]]}
     	 ];
    	return bisseq_data;
    };  
})

.value('version', '1.0')

.factory('DepositCount', ['$resource',
    function($resource) {
        return $resource('resources/json/count.json', null, {
            query: {
                method: 'GET',
                isArray: true
            }
        });

    }
])
.factory('DepositCountGET', ['$resource',
    function($resource) {
        return $resource('resources/json/countget.json', null, {
            query: {
                method: 'GET',
                isArray: true
            }
        });
    }
])
.factory('BiominerHttpInterceptor',['$q','$injector', 
    function($q,$injector) {
	 
		return {
			'responseError': function(rejection) {
					
				var $modal = $injector.get('$modal');
				
				$modal.open({
		    		templateUrl: 'app/common/error.html',
		    		controller: 'ErrorController',
		    		resolve: {
		    			title: function() {
		    				return rejection.data.errorName;
		    			},
		    			message: function() {
		    				return rejection.data.errorMessage;
		    			}
		    		}
		    	});
		       
		        return $q.reject(rejection);
		     }
		};
}])
 
.config(function($httpProvider) {
	$httpProvider.interceptors.push('BiominerHttpInterceptor');
})

.factory('StaticDictionary',['$http','$q',
    function($http) {
		var organismBuildList;
		var analysisTypeList;
		var sampleTypeList;
		var samplePrepList;
		var projectVisibilityList;
		var instituteList;
		
		return {
			organismBuildList: function(callback) {
				if (organismBuildList) {
					return organismBuildList;
				} else {
					organismBuildList = $http({
						url: "shared/getAllBuilds",
						method: "POST",
					}).success(callback);
					return organismBuildList;
				};
			},
			analysisTypeList: function(callback) {
				if(analysisTypeList) {
					return analysisTypeList;
				} else {
					analysisTypeList = $http({
						url: "shared/getAllAnalysisTypes",
			    		method: "POST",
					}).success(callback);
					return analysisTypeList;
				};
			},
			sampleTypeList: function(callback) {
				if (sampleTypeList) {
					return sampleTypeList;
				} else {
					sampleTypeList = $http({
						url: "shared/getAllSampleTypes",
			    		method: "POST",
					}).success(callback);
					return sampleTypeList;
				};
			},
			samplePrepList: function(callback) {
				if(samplePrepList) {
					return samplePrepList;
				} else {
					samplePrepList = $http({
						method: 'POST',
						url: 'shared/getAllSamplePreps',
					}).success(callback);
					return samplePrepList;
				};
			},
			projectVisibilityList: function(callback) {
				if(projectVisibilityList) {
					return projectVisibilityList;
				} else {
					projectVisibilityList = $http({
						method: 'POST',
						url: 'shared/getAllProjectVisibilities',
					}).success(callback);
					return projectVisibilityList;
				};
			},
			instituteList: function(callback) {
				if(instituteList) {
					return instituteList;
				}  else {
					$http({
						method: 'POST',
						url: 'shared/getAllInstitutes',
					}).success(callback);
					return instituteList;
				}
			}
		};
	}
])


.factory('DynamicDictionary',['$http',
    function($http) {
		var dict = {};
		
		dict.loadLabs = function() {
			return $http({
		    	method: 'POST',
		    	url: 'lab/all'
		    });
		};
	
		dict.loadSamplePrepsBySampleType = function(idSampleType) {
			return $http({
				method: 'POST',
				url: 'shared/getSamplePrepsBySampleType',
				params : {idSampleType: idSampleType},
			});
		};
		
		dict.loadSampleSources = function() {
			return $http({
				method: 'POST',
				url: 'shared/getAllSampleSources',
			});
		};
		
		dict.loadSampleConditions = function() {
			return $http({
				method: 'POST',
				url: 'shared/getAllSampleConditions',
			});
		};
		
		dict.loadSamplePreps = function() {
			return $http({
				method: 'POST',
				url: 'shared/getAllSamplePreps',
			});
		};

		return dict;
                   
}]);


