'use strict';

/* Services */


var services = angular.module('services', ['ngResource','ui.bootstrap'])


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
				
				if (rejection.data == null) {
					return $q.reject(rejection);
			    } else if (rejection.data.errorMessage == null) {
					return $q.reject(rejection);
				} else {
					var modalInstance = $injector.get('$uibModal');
					
					modalInstance.open({
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
		
		dict.getLabList = function() {
			return $http({
				method: 'GET',
				url: 'shared/getAllLabs',
				cache: true,
			});
		};
		
		dict.getGenotypeList = function() {
			return $http({
				method: 'GET',
				url: 'shared/getAllGenotypes',
				cache: true,
			});
		};
		
		dict.getGeneAnnotationList = function() {
			return $http({
				method: 'GET',
				url: 'shared/getAllGeneAnnotations',
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
		    	url: 'project/getProjectsByVisibility'
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
		
		dict.loadQueryAnalyses = function() {
			return $http({
				method: 'GET',
				url: 'project/getAllAnalyses',
			});
		};
		
		dict.loadOrganismBuilds = function() {
			return $http({
				method: 'GET',
				url: 'shared/getAllBuilds',
			});
		};
		
		dict.loadOrganisms = function() {
			return $http({
				method: 'GET',
				url: 'shared/getAllOrganisms',
			});
		};
		
		dict.loadTfs = function() {
			return $http({
				method: 'GET',
				url: 'transFactor/getAllTfs',
			});
		}
		
		dict.loadConversions = function() {
			return $http({
				method: 'GET',
				url: 'id_conversion/get_conversions',
			});
		}
		
		dict.loadLiftoverChains = function() {
			return $http({
				method: 'GET',
				url: "liftover/get_liftover_chains",
			});
		}
		
		dict.loadLiftoverSupports = function() {
			return $http({
				method: 'GET',
				url: "liftover/get_liftover_supports",
			});
		}

		return dict;
	                 
}]);


