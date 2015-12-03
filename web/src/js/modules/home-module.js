
var homeModule = angular.module('loco.homeModule', ['ui.router']);

homeModule.config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider) {

    $stateProvider.state('home', {
        url: '/home',
        templateUrl: 'home.html',
        controller: 'HomeController'
    });

    $urlRouterProvider.otherwise('/home');
}]);

homeModule.controller('HomeController', ['$scope', '$log',
    function($scope, $log) {
        $log.log('home');
    }]);


