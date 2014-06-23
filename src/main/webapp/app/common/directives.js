'use strict';

/* Directives */

var directives = angular.module('directives', [])
		
.directive('appVersion', ['version', 
    function (version) {
		return function (scope, elm, attrs) {
	        elm.text(version);
	    };
	}
])
.directive('flotChart', [ 
	function() {
	    return {
	        restrict: 'EA',
	        link: function(scope, element, attr) {
	            scope.$watch(attr.myModel, function(x) {
	                if ((!x) || (!x.data) || x.data.length<2) {
	                    return;
	                }
	                $.plot(element, x.data, x.options);
	            }, true);
	        }
	    };
	}
])
.directive('chosen',[ 
    function() {
		var linker = function(scope,element,attrs) {
			scope.$watch(attrs.chosen,function(){
				element.chosen();
				element.trigger('chosen:updated');
			});
			
			
		};
		
		return {
			restrict: 'A',
			link: linker
		};
    }
	
]).directive('resize', function ($window) {
    return function (scope, element, attrs) {
        var w = angular.element($window);
        scope.getWindowDimensions = function () {
            return {
                'h': w.height(),
                'w': w.width()
            };
        };
        scope.$watch(scope.getWindowDimensions, function (newValue, oldValue) {
            scope.style = function () {
                return {
                    'max-height': (newValue.h * attrs.per / 100) + 'px',
                };
            };

        }, true);

        w.bind('resize', function () {
            scope.$apply();
        });
    };
});