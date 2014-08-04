
(function () {
	'use strict';
	

	var url = 'submit/upload';

	angular.module('app', ['ngRoute', 'ui.bootstrap', 'dashboard', 'submit', 'query', 'useradmin', 'upload','navbar']);


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
		
		$routeProvider.when('/login',{
			templateUrl: 'app/common/login.html',
			controller: 'LoginController'
		});
	
		
		$routeProvider.otherwise({redirectTo: '/dashboard'});
		
			

	}]);
	
	
	
	
	


	

}());








