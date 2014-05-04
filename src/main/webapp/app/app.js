
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
		
			

	}])
	
	

	
	
	.controller('DemoFileUploadController', [
	    '$scope', '$http', '$filter', '$window',
	    function ($scope, $http) {
	    	$scope.options = {
                    url: url,
                    sequentialUploads: true
                    
                };
	    	
		        // When all uploads are finished, add all uploaded files to SubmitController's model
		        $scope.$on('fileuploadstop', function(){
		        	var uploadedFiles = $scope.$parent.$parent.$parent.uploadedFiles;
		        	for (var x in $scope.queue) {
		        		
		        		// Ignore if we have already registered this file
		        		if (uploadedFiles.indexOf($scope.queue[x]) >= 0) {
		        			continue;
		        		}
		        		// Make sure we have actually uploaded the file
		        		if ($scope.queue[x].hasOwnProperty("url")) {
		        			 uploadedFiles.push($scope.queue[x]);
		        		}
		        	}
		           
		        }); 
		        
		        $scope.$on('fileuploadadd', function() {
		        	 $scope.idAnalysisProject = $scope.$parent.$parent.$parent.project.id;
		        });
		        
                if (true) {
                    $scope.loadingFiles = true;
                    $http.get(url)
                        .then(
                            function (response) {
                                $scope.loadingFiles = false;
                                $scope.queue = response.data.files || [];
                                
                                // Add the files to SubmitController's model
                                var uploadedFiles = $scope.$parent.$parent.$parent.uploadedFiles;
                               
            		        	for (var x in $scope.queue) {
            		        		// Ignore if we have already registered this file
            		        		if (uploadedFiles.indexOf($scope.queue[x]) >= 0) {
            		        			continue;
            		        		}

            		        		if ($scope.queue[x].hasOwnProperty("url")) {
            		        			 uploadedFiles.push($scope.queue[x]);
            		        		}
            		        	}
                            },
                            function () {
                                $scope.loadingFiles = false;
                            }
                        );
                }
                
	            
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
	                    	// Get rid of uploaded file in the SubmitController's model
	                    	var uploadedFiles = $scope.$parent.$parent.$parent.uploadedFiles;
	                    	for(var i in uploadedFiles) {
	                            if(uploadedFiles[i].name == file.name) {
	                            	uploadedFiles.splice(i, 1);
	                            	break;
	                            }
	                        }
	                    	
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
	    }]);

}());






