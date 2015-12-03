
var timeoutService = angular.module('loco.timeout', []);

timeoutService.service('timeoutService', ['$timeout', '$interval', function($timeout, $interval) {

    this.timeout = function(fn, time, scope) {
        var promise = $timeout(fn, time);

        if (scope) {
            scope.$on("$destroy", function(event) {
                $timeout.cancel(promise);
            });
        } else {
            console.log('no scope specified.');
        }

        return function() {
            $timeout.cancel(promise);
        };
    };

    this.interval = function(fn, time, scope) {
        var promise = $interval(fn, time);

        scope.$on("$destroy", function(event) {
            $interval.cancel(promise);
        });

        return function() {
            $interval.cancel(promise);
        };
    };
}]);


