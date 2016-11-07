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
.directive('resize', function ($window) {
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
})
.directive('resize2', function ($window) {
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
                    'height': (newValue.h * attrs.per / 130) + 'px',
                };
            };

        }, true);

        w.bind('resize', function () {
            scope.$apply();
        });
    };
})
.directive('convertToNumber', function() {
  return {
    require: 'ngModel',
    link: function(scope, element, attrs, ngModel) {
      ngModel.$parsers.push(function(val) {
        return val != null ? parseInt(val, 10) : null;
      });
      ngModel.$formatters.push(function(val) {
        return val != null ? '' + val : null;
      });
    }
  };
})
.directive('showOnRowHover', function () {
	    return {
	        link: function (scope, element, attrs) {

	            element.closest('tr').bind('mouseenter', function () {
	                element.show();
	            });
	            element.closest('tr').bind('mouseleave', function () {
	                element.hide();

	                var contextmenu = element.find('#contextmenu');
	                contextmenu.click();

	                element.parent().removeClass('open');

	            });

	        }
	    };
});
