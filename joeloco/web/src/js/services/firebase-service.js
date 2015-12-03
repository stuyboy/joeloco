
var firebaseService = angular.module('loco.firebase', ['firebase']);

firebaseService.service('firebaseService', ['$log', '$q', '$firebaseObject', '$firebaseArray', function($log, $q, $firebaseObject, $firebaseArray) {

    var fb = new Firebase('https://joeloco.firebaseio.com/');
    var geo = new GeoFire(fb.child('rtLocations'));

    var getGroup = function(groupId) {
        return $firebaseObject(fb.child('groups').child(groupId));
    };
    this.getGroup = getGroup;

    var getUser = function(userId) {
        return $firebaseObject(fb.child('users').child(userId));
    };
    this.getUser = getUser;

    var getEvent = function(eventId) {
        return $firebaseObject(fb.child('events').child(eventId));
    };
    this.getEvent = getEvent;

    var getChat = function(eventId) {
        return $firebaseArray(fb.child('chats').child(eventId).child("messages"));
    };
    this.getChat = getChat;

    var getGeo = function(geoId) {
        return $firebaseObject(geo.ref().child(geoId));
    };
    this.getGeo = getGeo;


    var addLocation = function(userId, location) {
        fb.child('rtLocations').child(userId).set({
            g: 1,
            l: [location.lat, location.lng]
        });
    };
    this.addLocation = addLocation;

    this.getEventMemberLocations = function(eventId) {
        var deferred = $q.defer();

        var event = getEvent(eventId);
        event.$loaded()
            .then(function(data) {
                var group = getGroup(data.groupId);
                group.$loaded()
                    .then(function(data) {
                        deferred.resolve(Object.keys(data.members).map(function(userId) {
                            return getGeo(userId);
                        }));
                    })
                    .catch(function(error) {
                        console.error("Error:", error);
                    });
            })
            .catch(function(error) {
                console.error("Error:", error);
            });

        return deferred.promise;
    };
}]);
