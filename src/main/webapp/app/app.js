
(function () {
	'use strict';
	var url = 'submit/upload';
	
	angular.module('app', ['ngRoute', 'ui.bootstrap', 'blueimp.fileupload', 'dashboard', 'submit', 'query', 'useradmin']);


	angular.module('app').config(['$routeProvider', 
	                              
	  function ($routeProvider) {

		$routeProvider.when('/dashboard', {
		    templateUrl: 'app/dashboard/dashboard.html',
		    controller: 'DashboardController'
		});

		$routeProvider.when('/query', {
		      templateUrl: 'app/query/query.html',
		      controller: 'QueryController'
		});
		  
		$routeProvider.when('/submit', {
		      templateUrl: 'app/submit/submit.html',
		          controller: 'SubmitController'
		});
		   
		      
		$routeProvider.when('/useradmin', {
		      templateUrl: 'app/useradmin/useradmin.html',
		      controller: 'UserAdminController'
		});
	
		
		$routeProvider.otherwise({redirectTo: '/dashboard'});
		
			

	}]);
	

angular.module("app")
/*
.config([
            '$httpProvider', 'fileUploadProvider',
            function ($httpProvider, fileUploadProvider) {
                delete $httpProvider.defaults.headers.common['X-Requested-With'];
                
                fileUploadProvider.defaults.redirect = window.location.href.replace(
                    /\/[^\/]*$/,
                    '/cors/result.html?%s'
                );
                
                
            }
        
        ]) 
    */
        
.controller('DemoFileUploadController', [
    '$scope', '$http', '$filter', '$window',
    function ($scope, $http) {
        $scope.options = {
            url: url
        };
        $scope.loadingFiles = true;
        $http.get(url)
            .then(
                function (response) {
                    $scope.loadingFiles = false;
                    $scope.queue = response.data.files || [];
                },
                function () {
                    $scope.loadingFiles = false;
                }
            );
            
    }
])
.controller('FileDestroyController', [
    '$scope', '$http',
    function ($scope, $http) {
        var file = $scope.file,
            state;
        if (file.url) {
            file.$state = function () {
                return state;
            };
            file.$destroy = function () {
                state = 'pending';
                return $http({
                    url: file.deleteUrl,
                    method: file.deleteType
                }).then(
                    function () {
                        state = 'resolved';
                        $scope.clear(file);
                    },
                    function () {
                        state = 'rejected';
                    }
                );
            };
        } else if (!file.$cancel && !file._index) {
            file.$cancel = function () {
                $scope.clear(file);
            };
        }
    }
]);

}());

