//The global map, using Leaflet + Mapbox
var map;
var defaultCenter;

function initLocationAndMap() {
    prepareGeolocation();
    doGeolocation();
}

function doGeolocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(positionSuccess, positionError);
    } else {
        positionError(-1);
    }
}

function positionError(err) {
    console.log('Unable to find location');
    defaultCenter = [37.7833, -122.4167]; //sf
    init(defaultCenter);
}

function positionSuccess(position) {
    // Centre the map on the new location
    var coords = position.coords || position.coordinate || position;
    var latLng = L.latLng(coords.latitude, coords.longitude);
    defaultCenter = latLng;

    //Init the map
    init(defaultCenter);
}

function init(latLng) {
    map = L.map('map');
    map.on('load', function(e) {
        initEventMap();
    });
    map.setView(latLng, 14);
    var accessToken = 'pk.eyJ1Ijoiam9lY2hhbmciLCJhIjoicEp0Q3NSQSJ9.ws11nLqGvsMzLTJ0U-I_5Q';
    L.tileLayer('http://{s}.tiles.mapbox.com/v4/joechang.f7451cba/{z}/{x}/{y}.png?access_token=' + accessToken, {
        attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
        maxZoom: 18
    }).addTo(map);
}

initLocationAndMap();