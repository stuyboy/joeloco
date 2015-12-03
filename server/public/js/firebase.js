//Quick file to contain all the firebase code
var FIREBASE_URL='https://joeloco.firebaseio.com/';
var firebaseRef = new Firebase(FIREBASE_URL);

//Anonmymous login
firebaseRef.authAnonymously(function(error, authData) {}, { remember: 'sessionOnly' });

//Various firebase refs
var geoFire = new GeoFire(firebaseRef.child('rtLocations'));
var groupFirebase = firebaseRef.child('groups');
var eventFirebase = firebaseRef.child('events');

var impRef = firebaseRef.child('impressions');

//When a fellow visitor comes along, pop on map.
impRef.on('child_added', function(snapshot) {
    var obj=snapshot.val();
    //displayLiveMarker(obj.latitude, obj.longitude);
});


function getGeoFirebase() {
    return geoFire.ref();
}

function getGroupFirebase(groupId) {
    if (typeof groupId !== 'undefined') {
        return groupFirebase.child(groupId);
    }

    return groupFirebase;
}

function getGroupMembersFirebase(groupId) {
    return getGroupFirebase(groupId).child('members');
}

function getEventFirebase(eventId) {
    if (typeof eventId !== 'undefined') {
        return eventFirebase.child(eventId);
    }

    return eventFirebase;
}

function pushLocation(lat, lng) {
    var timestamp = new Date().getTime();

    impRef.push(
        {
            timestamp: timestamp,
            latitude: lat,
            longitude: lng,
            device: WURFL
        }
    );

    geoFire.set('joloco', [lat, lng]).then(
        function() {
            console.log('added joeloco website access location');
        },
        function() {
            console.log('error adding location');
        }
    );
}
