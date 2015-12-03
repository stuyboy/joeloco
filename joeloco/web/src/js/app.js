
// Declare app level module which depends on filters, and services
var loco = angular.module('loco', [
    'templates',

    'firebase',

    'loco.homeModule',
    'loco.mapModule'
]);

loco.config([function() {
    //googleAnalyticsCordovaProvider.trackingId = 'UA-52706146-1';
}]);

loco.run(['$rootScope',
    function($rootScope) {
        //googleAnalyticsCordova.init();
    }]);
