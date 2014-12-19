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
.directive('optionsDisabled', function($parse) {
    var disableOptions = function(scope, attr, element, data, 
                                  fnDisableIfTrue) {
        // refresh the disabled options in the select element.
        var options = element.find("option");
        for(var pos= 0,index=0;pos<options.length;pos++){
            var elem = angular.element(options[pos]);
            if(elem.val()!=""){
                var locals = {};
                locals[attr] = data[index];
                elem.attr("disabled", fnDisableIfTrue(scope, locals));
                index++;
            }
        }
    };
    return {
        priority: 0,
        require: 'ngModel',
        link: function(scope, iElement, iAttrs, ctrl) {
            // parse expression and build array of disabled options
            var expElements = iAttrs.optionsDisabled.match(
                /^\s*(.+)\s+for\s+(.+)\s+in\s+(.+)?\s*/);
            var attrToWatch = expElements[3];
            var fnDisableIfTrue = $parse(expElements[1]);
            scope.$watch(attrToWatch, function(newValue, oldValue) {
                if(newValue)
                    disableOptions(scope, expElements[2], iElement, 
                        newValue, fnDisableIfTrue);
            }, true);
            // handle model updates properly
            scope.$watch(iAttrs.ngModel, function(newValue, oldValue) {
                var disOptions = $parse(attrToWatch)(scope);
                if(newValue)
                    disableOptions(scope, expElements[2], iElement, 
                        disOptions, fnDisableIfTrue);
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
