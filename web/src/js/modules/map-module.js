
var mapModule = angular.module('loco.mapModule', ['ui.router', 'ngResource', 'ngCookies', 'geolocation',
    'leaflet-directive', 'angularMoment', 'loco.firebase', 'loco.timeout', 'uuid4']);

mapModule.config(['$stateProvider', function($stateProvider) {

    $stateProvider.state('map', {
        url: '/map/:eventId',
        templateUrl: 'map/map.html',
        controller: 'MapController'
    });
}]);


mapModule.run(['$rootScope', function($rootScope) {
}]);

mapModule.controller('MapController', ['$scope', '$document', '$stateParams', '$cookies', 'firebaseService', 'geolocation', 'timeoutService', 'uuid4',
    function($scope, $document, $stateParams, $cookies, firebaseService, geolocation, timeoutService, uuid4) {

        var localUserId = $cookies.get('WEB_USER_ID');
        if (!localUserId) {
            localUserId = uuid4.generate();
            $cookies.put('WEB_USER_ID', localUserId);
        }

        var accessToken = 'pk.eyJ1Ijoiam9lY2hhbmciLCJhIjoicEp0Q3NSQSJ9.ws11nLqGvsMzLTJ0U-I_5Q';
        angular.extend($scope, {
            center: {
                lat: 37.7833,
                lng: -122.4167,
                zoom: 13
            },
            markers: {

            },
            defaults: {
                tileLayer: "http://{s}.tiles.mapbox.com/v4/joechang.f7451cba/{z}/{x}/{y}.png?access_token=" + accessToken,
                tileLayerOptions: {
                    opacity: 0.9,
                    detectRetina: true,
                    reuseTiles: true,
                    attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
                },
                maxZoom: 18,
            }
        });

        var myIcon = {
            iconUrl: 'img/marker-icon-red.png',
            iconSize: [25, 41],
            iconAnchor: [13, 41],
            popupAnchor: [0, -35],
        };

        var setMyLocation = function() {
            geolocation.getLocation().then(function(geo){
                if (!$scope.markers.web) {
                    $scope.markers.web = {
                        lat: geo.coords.latitude,
                        lng: geo.coords.longitude,
                        message: 'me',
                        focus: true,
                        draggable: false,
                        icon: myIcon
                    };
                } else {
                    $scope.markers.web.lat = geo.coords.latitude;
                    $scope.markers.web.lng = geo.coords.longitude;
                }

                firebaseService.addLocation(localUserId, $scope.markers.web);
            });
        };

        var shareLocationPrompt = function(userId) {
            firebaseService.getUser(userId).$loaded().then(function(user) {
                $scope.markers[userId].message = user.fullname;
                if (confirm("Share your location with " + user.fullname + "?")) {
                    setMyLocation();
                    timeoutService.interval(setMyLocation, 1000, $scope);
                }
            });
        };

        var updateLocation = function(locationId) {
            firebaseService.getGeo(locationId).$loaded().then(function(locationData) {
                if (!$scope.markers[locationId]) {
                    $scope.markers[locationId] = {
                        lat: locationData.l[0],
                        lng: locationData.l[1],
                        focus: true,
                        draggable: false,
                    };
                    shareLocationPrompt(locationId);
                } else {
                    $scope.markers[locationId].lat = locationData.l[0];
                    $scope.markers[locationId].lng = locationData.l[1];
                }
                $scope.center.lat = locationData.l[0];
                $scope.center.lng = locationData.l[1];
            });
        };

        var promise = firebaseService.getEventMemberLocations($stateParams.eventId);

        promise.then(function(locations) {
            locations.map(function(location) {
                location.$watch(function() {
                    updateLocation(location.$ref().key());
                });
            });
        })
        .catch(function(error) {
            console.error("Error:", error);
        });

        $scope.me = function(message) {
            return message.userid === localUserId;
        };

        $scope.chat = {
            text: "",
            messages: firebaseService.getChat($stateParams.eventId)
        };

        $scope.sendMessage = function() {
            if ($scope.chat.text) {
                var message = {
                    message: $scope.chat.text,
                    name: "Web User",
                    userid: localUserId,
                    time: Date.now(),
                };
                $scope.chat.messages.$add(message);
                $scope.chat.text = "";
            }
        };

}]);

mapModule.directive('ngEnter', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 13) {
                scope.$apply(function (){
                    scope.$eval(attrs.ngEnter);
                });

                event.preventDefault();
            }
        });
    };
});

mapModule.directive('chatScroll', function () {
    return {
        scope: {
            chatScroll: "="
        },
        link: function (scope, element) {
            scope.$watchCollection('chatScroll', function (newValue) {
                if (newValue)
                {
                    $(element).scrollTop($(element)[0].scrollHeight);
                }
            });
        }
    };
});
