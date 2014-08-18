
(function () {
	'use strict';

	angular.module('app', ['ngRoute', 'ui.bootstrap', 'dashboard', 'submit', 'query', 'useradmin', 'upload','navbar']);


	angular.module('app').config(['$routeProvider', 
	                              
	  function ($routeProvider) {

		$routeProvider.when('/dashboard', {
		    templateUrl: 'app/dashboard/dashboard.html',
		    controller: 'DashboardController',
		    restrict: 'none'
		});

		$routeProvider.when('/query', {
		      templateUrl: 'app/query/query.html',
		      controller: 'QueryController',
		      restrict: 'none'
		});
		  
		$routeProvider.when('/submit', {
		      templateUrl: 'app/submit/submit.html',
		      controller: 'SubmitController',
		      restrict: 'authenticated'
		});
		   
		      
		$routeProvider.when('/useradmin', {
		      templateUrl: 'app/useradmin/useradmin.html',
		      controller: 'UserAdminController',
		      restrict: 'authorized'
		});
		
		$routeProvider.when('/login',{
			templateUrl: 'app/common/login.html',
			controller: 'LoginController',
			restrict: 'none'
			
		});
	
		
		$routeProvider.otherwise({redirectTo: '/dashboard'});
	}]);
	
	
	

}());








