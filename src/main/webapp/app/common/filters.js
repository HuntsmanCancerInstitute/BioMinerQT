'use strict';

/* Filters */

var filters = angular.module('filters', [])

.filter('interpolate', ['version', 
    function (version) {
    	return function (text) {
    		return String(text).replace(/\%VERSION\%/mg, version);
    	};
    }
])


.filter('checkmark', [
    function() {
    	return function(input) {
    		return input ? '\u2713' : '\u2718';
    	};
    }
])

.filter('lookupSampleType', [
	function() {
		return function(input,sampleTypeList) {
			var display = input;
			for (var x in sampleTypeList) {
				if (sampleTypeList[x].idSampleType == input) {
					display = sampleTypeList[x].type;
					break;
				}
			}
			return display;
		};
	}
])

.filter('lookupSampleCondition', [
	function() {
		return function(input,sampleConditionList) {
			var display = input;
			for (var x in sampleConditionList) {
				if (sampleConditionList[x].idSampleCondition == input) {
					display = sampleConditionList[x].condition;
					break;
				}
			}
			return display;
		};
	}
])

.filter('lookupSampleSource', [
    function() {
		return function(input,sampleSourceList) {
			var display = input;
			for (var x in sampleSourceList) {
				if (sampleSourceList[x].idSampleSource == input) {
					display = sampleSourceList[x].source;
					break;
				}
			}
			return display;
		};
	}
])

.filter('lookupSamplePrep',[
    function() {
    	return function(input,samplePrepList) {
    		var display = input;
    		for (var x in samplePrepList) {
    			if (samplePrepList[x].idSamplePrep == input) {
	    			display = samplePrepList[x].description;
	    			break;
    			}
    		}
    		return display;
    	};
    }
])

.filter('lookupAnalysisType', [
	function() {
		return function(input,analysisTypeList) {
			var display = input;
			for (var x in analysisTypeList) {
				if (analysisTypeList[x].idx == input) {
					display = analysisTypeList[x].type;
					break;
				}
			}
			return display;
		};
	}
])

.filter('startFrom', [
   function() {
	   return function(input, start) {
		   start = +start; //parse to int
		   return input.slice(start);
	   };
   }
])

.filter('ceil', [
  function() {
	  return function(number) {
		  return Math.ceil(number);
	  };
  }                           
])

.filter('displayPhone', [
    function() {
    	return function(number) {
    		var numberString = number.toString();
    		var section1 = numberString.slice(0,3);
    		var section2 = numberString.slice(3,6);
    		var section3 = numberString.slice(6);
    		
    		return "( " + section1 + " ) " + section2 + "-" + section3;
    	};
    }
])

.filter('bytes', [
  function() {
	return function(bytes, precision) {
		if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) return '-';
		if (typeof precision === 'undefined') precision = 1;
		var units = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'];
		var	number = Math.floor(Math.log(bytes) / Math.log(1024));
		return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) +  ' ' + units[number];
	};
  }
])

// This filter is a way to partition elements into a row.  For example, it is used
// on the query results panel to show three query filters per row.
.filter('partition', [
 function() 
 {
	  var cache = {};
	  var filter = function(arr, size) {
	    if (!arr) { return; }
	    var newArr = [];
	    for (var i=0; i<arr.length; i+=size) {
	      newArr.push(arr.slice(i, i+size));
	    }
	    var arrString = JSON.stringify(arr);
	    var fromCache = cache[arrString+size];
	    if (JSON.stringify(fromCache) === JSON.stringify(newArr)) {
	      return fromCache;
	    }
	    cache[arrString+size] = newArr;
	    return newArr;
	  };
	  return filter;
	}
 ]);

