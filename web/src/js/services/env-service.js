

var envService = angular.module('loco.env', []);

envService.config(['$sceDelegateProvider', 'locoServiceHost', function($sceDelegateProvider, locoServiceHost) {
    $sceDelegateProvider.resourceUrlWhitelist(['self', locoServiceHost]);
}]);

envService.constant('locoServiceHost', 'REPLACE_LOCO_SERVICE_HOST');
envService.constant('locoJsHost', 'REPLACE_JS_HOST');
envService.constant('locoResourceHost', 'REPLACE_RESOURCE_HOST');

