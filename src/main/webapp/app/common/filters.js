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
					display = sampleTypeList[x].name;
					break;
				}
			}
			return display;
		}
	}
])

.filter('lookupSampleType', [
    function() {
		return function(input,sampleTypeList) {
			var display = input;
			for (var x in sampleTypeList) {
				if (sampleTypeList[x].idSampleType == input) {
					display = sampleTypeList[x].name;
					break;
				}
			}
			return display;
		};
	}
])

.filter('lookupSampleGroup', [
	function() {
		return function(input,sampleGroupList) {
			var display = input;
			for (var x in sampleGroupList) {
				if (sampleGroupList[x].idSampleGroup == input) {
					display = sampleGroupList[x].name;
					break;
				}
			}
			return display;
		};
	}
])

.filter('lookupSite', [
    function() {
		return function(input,SiteList) {
			var display = input;
			for (var x in SiteList) {
				if (SiteList[x].idSite == input) {
					display = SiteList[x].organ + " - " + SiteList[x].name;
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
				if (analysisTypeList[x].idAnalysisType == input) {
					display = analysisTypeList[x].name;
					break;
				}
			}
			return display;
		};
	}
]);