'use strict';

/* Services */


var services = angular.module('services', ['ngResource'])

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
]);


