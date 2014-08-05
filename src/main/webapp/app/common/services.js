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
					
				if (rejection.data == "") {
					return $q.reject(rejection);
				} else {
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
			    			},
			    			stackTrace: function() {
			    				return rejection.data.errorStackTrace;
			    			},
			    			errorTime: function() {
			    				return rejection.data.errorTime;
			    			}
			    			
			    		}
			    	});
			       
			        return $q.reject(rejection);
				}
				
		     }
		};
}])
 
.config(function($httpProvider) {
	$httpProvider.interceptors.push('BiominerHttpInterceptor');
})

.factory('StaticDictionary',['$http',
    function($http) {
		var dict = {};
		
		dict.getOrganismBuildList = function() {
			return $http({
				url: "shared/getAllBuilds",
				method: "GET",
				cache: true,
			});
		};
		
		dict.getAnalysisTypeList = function() {
			return $http({
				url: "shared/getAllAnalysisTypes",
	    		method: "GET",
	    		cache: true,
			});
		};
		
		dict.getSampleTypeList = function() {
			return $http({
				url: "shared/getAllSampleTypes",
	    		method: "GET",
	    		cache: true,
			});
		};
		
		dict.getProjectVisibilityList = function() {
			return $http({
				method: 'GET',
				url: 'shared/getAllProjectVisibilities',
				cache: true,
			});
		};
		
		dict.getInstituteList = function() {
			return $http({
				method: 'GET',
				url: 'shared/getAllInstitutes',
				cache: true,
			});
		};
		
		return dict;
	}
])


.factory('DynamicDictionary',['$http',
    function($http) {
		var dict = {};
		
		dict.loadLabs = function() {
			return $http({
		    	method: 'GET',
		    	url: 'lab/all'
		    });
		};
		
		dict.loadQueryProjects = function() {
			return $http({
		    	method: 'GET',
		    	url: 'project/getAllProjects'
		    });
		};

	
		dict.loadSamplePrepsBySampleType = function(idSampleType) {
			return $http({
				method: 'GET',
				url: 'shared/getSamplePrepsBySampleType',
				params : {idSampleType: idSampleType},
			});
		};
		
		dict.loadSampleSources = function() {
			return $http({
				method: 'GET',
				url: 'shared/getAllSampleSources',
			});
		};
		
		dict.loadSampleConditions = function() {
			return $http({
				method: 'GET',
				url: 'shared/getAllSampleConditions',
			});
		};
		
		dict.loadSamplePreps = function() {
			return $http({
				method: 'GET',
				url: 'shared/getAllSamplePreps',
			});
		};
		
		dict.isAuthenticated = function() {
			return $http({
				method: 'GET',
				url: 'security/auth',
			});
		};
		
		dict.loadQueryLabs = function() {
			return $http({
				method: 'GET',
				url: 'lab/getQueryLabs',
			});
		};

		return dict;
		
		
                   
}]);


