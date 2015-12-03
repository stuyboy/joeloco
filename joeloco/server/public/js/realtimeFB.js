//Test variables for development.  Replace.
var markers = {};
var eventValid = true;
var bgEventCheck;

function createMarker(loc, userId) {
    var userIcon = L.icon({
        iconUrl: '/users/' + userId + '/mapPointer',
        iconSize:  [40, 50],
        iconAnchor:[20, 50]
    });

    var marker = L.marker(loc, {icon: userIcon}).addTo(map);

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

function queryEventDetailsApi(eventId, successCallback, failureCallback) {
    $(document).ready(function() {
        $.ajax({
            type: 'GET',
            beforeSend: function (req) {
                req.setRequestHeader('Authorization', 'Basic dXNlcjpwYXNzd29yZA==');
            },
            url: '/events/' + eventId + '/users',
            success: function(data) {
                if (successCallback) {
                    data.map(function (item) {
                        successCallback(item);
                    });
                }
            },
            error: function(data) {
                var err = eval("(" + data.responseText + ")");
                if (failureCallback) {
                    failureCallback(err);
                }
            }
        });
    });
}

function registerUserListenerApi(userId) {
    var gfUserRef = getGeoFirebase().child(userId);
    gfUserRef.on('value', function(dataSnapshot) {
        if (eventValid) {
            if (dataSnapshot.exists()) {
                var loc = dataSnapshot.val();
                var mark = markers[userId];
                if (typeof mark === "undefined") {
                    mark = createMarker(loc.l, userId);
                    markers[userId] = mark;
                } else {
                    mark.animatedMoveTo(loc.l);
                }
                map.setView(new L.latLng(loc.l[0], loc.l[1]));
            }
        } else {
            shutdownMapTracking("Event has Expired");
        }
    });
}

function handleEventError(errObject) {
    var msg = "Unknown Error";
    switch (errObject.errorCode) {
        case 0:
            msg = "Event could not be located.";
            break;
        case 50:
            msg = "Tracking is not available yet.";
            break;
        case 60:
            msg = "Tracking has expired.";
            break;
    }
    shutdownMapTracking(msg);
    eventValid = false;
}

function checkEventValidity(eventId) {
    if (eventValid) {
        queryEventDetailsApi(eventId, null, handleEventError);
    } else {
        clearInterval(bgEventCheck);
    }
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

function initEventMap() {
    var qEventId = qs["eventId"];

    if (typeof qEventId !== 'undefined') {
        queryEventDetailsApi(qEventId, registerUserListenerApi, handleEventError);
        bgEventCheck = setInterval(function() { checkEventValidity(qEventId) }, 10000);
    }
}
