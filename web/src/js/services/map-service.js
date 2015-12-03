

var mapService = angular.module('loco.map', ['geolocation', 'loco.firebase', 'duScroll']);

mapService.service('mapService', ['$log', 'firebaseService', function($log, firebaseService) {

    //var map = null;
    //this.defaultCenter = null;
    //
    //map = L.map('map');
    //map.on('load', function(e) {
    //    initGroupsNearby();
    //});
    //map.setView(latLng, 14);
    //var accessToken = 'pk.eyJ1Ijoiam9lY2hhbmciLCJhIjoicEp0Q3NSQSJ9.ws11nLqGvsMzLTJ0U-I_5Q';
    //L.tileLayer('http://{s}.tiles.mapbox.com/v4/joechang.f7451cba/{z}/{x}/{y}.png?access_token=' + accessToken, {
    //    attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
    //    maxZoom: 18
    //}).addTo(map);

}]);
//
//function initLocationAndMap() {
//    prepareGeolocation();
//    doGeolocation();
//}
//
//function doGeolocation() {
//    if (navigator.geolocation) {
//        navigator.geolocation.getCurrentPosition(positionSuccess, positionError);
//    } else {
//        positionError(-1);
//    }
//}

//function positionError(err) {
//    console.log('Unable to find location');
//    defaultCenter = [37.7833, -122.4167]; //sf
//    init(defaultCenter);
//}
//
//function positionSuccess(position) {
//    // Centre the map on the new location
//    var coords = position.coords || position.coordinate || position;
//    var latLng = L.latLng(coords.latitude, coords.longitude);
//    defaultCenter = latLng;
//
//    //Init the map
//    init(defaultCenter);
//}
//
//function init(latLng) {
//
//}

//initLocationAndMap();



//Test variables for development.  Replace.
var radiusInKm = 20;
var markers = {};
var initFinished = false;
var mainEvent;
var popup;

//Debugging purposes
function proximityListener() {
    var geoQuery = geoFire.query({
        center: [defaultCenter.lat, defaultCenter.lng],
        radius: radiusInKm
    });

    geoQuery.on("key_entered", function (key, loc) {
        markers[key] = createMarker(loc);
        if (initFinished) {
            //Only call on NEW markers entering map.
            zoomMoveMapToMarkers();
        }
    });

    geoQuery.on("key_exited", function(key, loc) {
        map.removeLayer(markers[key]);
        removeMarker(key);
    });

    geoQuery.on("key_moved", function (key, loc) {
        var mark = markers[key];

        if (typeof mark !== "undefined" && typeof mark !== "undefined") {
            mark.animatedMoveTo(loc);
        }
    });

    geoQuery.on("ready", function() {
        //panMapToInclude(loc);
        zoomMoveMapToMarkers();
        initFinished = true;
    });
}

function createMarker(loc) {
    var marker = L.marker(loc).addTo(map);
    return marker;
}

function removeMarker(key) {
    if (typeof markers !== 'undefined') {
        var e = markers[key];
        if (typeof e !== 'undefined') {
            map.removeLayer(markers[key]);
            delete markers[key];
        }
    }
}

//Return NOTSTARTED or EXPIRED
function checkEventValidity() {
    if (typeof mainEvent !== 'undefined') {
        if (Date.now() > mainEvent.dateEnd) {
            return 'EXPIRED';
        }
        if (Date.now() < mainEvent.dateStart) {
            return 'PENDING';
        }
        if (Date.now() <= mainEvent.dateEnd && Date.now() >= mainEvent.dateStart) {
            return 'ON';
        }
    }
    return 'EXPIRED';
}

function registerUserListener(userId) {
    var gfUserRef = getGeoFirebase().child(userId);
    gfUserRef.on('value', function(dataSnapshot) {
        if (dataSnapshot.exists()) {
            if (checkEventValidity()) {
                var loc = dataSnapshot.val();
                var mark = markers[userId];
                if (typeof mark === "undefined") {
                    mark = createMarker(loc.l);
                    markers[userId] = mark;
                    popup = mark.bindPopup('Lat: ' + loc.l[0] + ' Long: ' + loc.l[1]).openPopup();
                } else {
                    mark.animatedMoveTo(loc.l);
                }
                map.setView(new L.latLng(loc.l[0], loc.l[1]));
                popup.update();
            } else {
                shutdownMapTracking();
            }
        }
    });
}

function registerGroupUserListeners(groupId) {
    var ref = getGroupMembersFirebase(groupId);
    ref.once('value', function (dataSnapshot) {
        if (dataSnapshot.exists()) {
            var map = dataSnapshot.val();
            for (var key in map) {
                if (map.hasOwnProperty(key)) {
                    registerUserListener(key);
                }
            }
        }
    });
}

function queryEventDetails(eventId) {
    var ref = getEventFirebase(eventId);
    ref.on('value', function (dataSnapshot) {
        if (dataSnapshot.exists()) {
            var evt = dataSnapshot.val();
            mainEvent = evt;
            var eventCheck = checkEventValidity();
            if (eventCheck == 'ON') {
                showOverlay(false);
                registerGroupUserListeners(evt.groupId);
            } else if (eventCheck == 'EXPIRED') {
                shutdownMapTracking("Event has Expired. Realtime Tracking Disabled.");
            } else if (eventCheck == 'PENDING') {
                shutdownMapTracking("Realtime Tracking disabled until event start.");
            }
        }
    });
}

function shutdownMapTracking(message) {
    for (var key in markers) {
        removeMarker(key);
    }
    showOverlay(true);
    var olt = document.getElementById("overlayText");
    if (typeof olt !== 'undefined') {
        olt.innerText = message;
    }
}

function showOverlay(sw) {
    var ol = document.getElementById('overlay');
    if (typeof ol !== 'undefined') {
        if (sw) {
            ol.style.display = 'block';
        } else {
            ol.style.display = 'none';
        }
    }
}

/* Returns true if the two inputted coordinates are approximately equivalent */
function coordinatesAreEquivalent(coord1, coord2) {
    return (Math.abs(coord1 - coord2) < 0.000001);
}

/* Animates the Marker class (based on https://stackoverflow.com/a/10906464) */
L.Marker.prototype.animatedMoveTo = function(newLocation) {
    var toLat = newLocation[0];
    var toLng = newLocation[1];

    var fromLat = this.getLatLng().lat;
    var fromLng = this.getLatLng().lng;

    if (!coordinatesAreEquivalent(fromLat, toLat) || !coordinatesAreEquivalent(fromLng, toLng)) {
        var percent = 0;
        var latDistance = toLat - fromLat;
        var lngDistance = toLng - fromLng;
        var interval = window.setInterval(function () {
            percent += 0.01;
            var curLat = fromLat + (percent * latDistance);
            var curLng = fromLng + (percent * lngDistance);
            var pos = new L.latLng(curLat, curLng);
            this.setLatLng(pos);
            if (percent >= 1) {
                window.clearInterval(interval);
            }
        }.bind(this), 10);
    }
};

function panMapToInclude(latLng) {
    if (!map.getBounds().contains(latLng)) {
        map.panTo(latLng);
    }
}

function zoomMoveMapToMarkers() {
    var latlngs = [];
    for (var key in markers) {
        var ll = markers[key].getLatLng();
        latlngs.push(ll);
    }
    var bounds = L.latLngBounds(latlngs);
    map.fitBounds(bounds);
}

function initGroupsNearby() {
    var qGroupId = qs.groupId;
    var qEventId = qs.eventId;

    if (typeof qEventId !== 'undefined') {
        queryEventDetails(qEventId);
    } else if (typeof qGroupId !== 'undefined') {
        registerGroupUserListeners(qGroupId);
    } else {
        proximityListener();
    }
}