
/* Filters */
var locoFilters = angular.module('loco.filters', []);

locoFilters.filter('startFrom', function() {
    return function(input, start) {
        start = +start; //parse to int
        return input.slice(start);
    };
});
