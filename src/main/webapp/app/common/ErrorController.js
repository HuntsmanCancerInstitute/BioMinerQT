var error = angular.module("error",[])

.controller("ErrorController", ['$scope','$uibModalInstance','$sce','$rootScope','$http','title','message','stackTrace','errorTime',
                                                      
function($scope, $uibModalInstance, $sce, $rootScope, $http, title, message, stackTrace, errorTime) {
	$scope.title = title;
	$scope.message = $sce.trustAsHtml(message);
	$scope.stackTrace = stackTrace;
	$scope.errorTime = errorTime;
	$scope.userComments = "";
	
	$scope.ok = function() {
		$uibModalInstance.dismiss();
	};
	
	$scope.sendReport = function() {
		var subject = "Error report from BiominerQT ";
		var user = "guest";
		if ($scope.loggedUser != null) {
			user = $scope.loggedUser.username;
		}
		
		if ($scope.userComments == "") {
			$scope.userComments = "No user comments";
		}
		
		var body = "User " + user + " reported an error at " + errorTime + "\n\n" + $scope.userComments + 
			"\n\n" + $scope.message + "\n\n" + $scope.stackTrace + "\n\n" ;
		$http({
			url : "shared/sendMail",
			method : "POST",
			params : {subject: subject, body: body}
		});
		$uibModalInstance.dismiss();
	};
}])
.controller("userErrorController", ['$scope','$uibModalInstance','$sce','title','message',
	 function($scope, $uibModalInstance, $sce, title, message) {
		$scope.title = title;
		$scope.message = $sce.trustAsHtml(message);
		
		$scope.ok = function() {
			$uibModalInstance.dismiss();
		};
}]);