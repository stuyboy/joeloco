require('jshint-stylish');
var gulp        = require('gulp');
var path        = require('path');
var $           = require('gulp-load-plugins')();
var runSequence = require('run-sequence');
var es          = require('event-stream');
var parallelize = require('concurrent-transform');

var JS_LIBRARIES = [
    'bower_components/jquery/dist/jquery.js',
    'bower_components/angular/angular.js',
    'bower_components/angular-cookies/angular-cookies.js',
    'bower_components/angular-resource/angular-resource.js',
    'bower_components/angular-ui-router/release/angular-ui-router.js',
    'bower_components/angularfire/dist/angularfire.js',
    'bower_components/angulartics/src/angulartics.js',
    'bower_components/angularjs-geolocation/src/geolocation.js',
    'bower_components/angular-leaflet-directive/dist/angular-leaflet-directive.js',
    'bower_components/angular-uuid4/angular-uuid4.js',
    'bower_components/bootstrap/dist/js/bootstrap.js',
    'bower_components/firebase/firebase-debug.js',
    'bower_components/geofire/dist/geofire.js',
    'bower_components/leaflet/dist/leaflet-src.js',
    'bower_components/lodash/lodash.js',
    'bower_components/moment/moment.js',
    'bower_components/angular-moment/angular-moment.js',
    'bower_components/rsvp/rsvp.js',
    'bower_components/SHA-1/sha1.js',
    'bower_components/waypoints/waypoints.js'
];

var CSS_LIBRARIES = [
    'bower_components/bootstrap/dist/css/bootstrap.css',
    'bower_components/leaflet/dist/leaflet.css'
];

var APP_JS = 'app.js';
var APP_CSS = 'app.css';
var APP_TEMPLATES = 'app.templates.js';
var LIB_JS = 'lib.js';
var LIB_CSS = 'lib.css';

var APP_JS_MIN = 'app.min.js';
var APP_CSS_MIN = 'app.min.css';
var LIB_JS_MIN = 'lib.min.js';
var LIB_CSS_MIN = 'lib.min.css';

var SRC  = './src';

var SRC_CSS_BASE  = path.join(SRC, 'css');
var SRC_JAVASCRIPT_BASE  = path.join(SRC, 'js');
var SRC_TEMPLATES_BASE  = path.join(SRC, 'templates');
var SRC_ASSETS_BASE  = path.join(SRC, 'assets');

var SRC_ALL  = path.join(SRC, '**');
var SRC_INDEX_HTML  = path.join(SRC, 'index.html');
var SRC_CSS_ALL  = path.join(SRC_CSS_BASE, '**', '*.scss');
var SRC_JAVASCRIPT_ALL  = path.join(SRC_JAVASCRIPT_BASE, '**', '*.js');
var SRC_TEMPLATES_ALL  = path.join(SRC_TEMPLATES_BASE, '**', '*.html');
var SRC_ASSETS_ALL  = path.join(SRC_ASSETS_BASE, '**', '*');

var DEST = './build';
var DEST_DEV = path.join(DEST, 'dev');
var DEST_DEV_ALL = path.join(DEST_DEV, '**');
var DEST_DEV_LIB = path.join(DEST_DEV, 'lib*');
var DEST_DEV_APP = path.join(DEST_DEV, 'app*');
var DEST_DEV_INDEX = path.join(DEST_DEV, 'index.html');

var DEST_PROD = path.join(DEST, 'prod');
var DEST_PROD_ALL = path.join(DEST_PROD, '**');
var DEST_PROD_LIB = path.join(DEST_PROD, 'lib*');
var DEST_PROD_APP = path.join(DEST_PROD, 'app*');
var DEST_PROD_INDEX = path.join(DEST_PROD, 'index.html');

var DEST_PROD_APP_JS = path.join(DEST_PROD, APP_JS);
var DEST_PROD_APP_CSS = path.join(DEST_PROD, APP_CSS);
var DEST_PROD_LIB_JS = path.join(DEST_PROD, LIB_JS);
var DEST_PROD_LIB_CSS = path.join(DEST_PROD, LIB_CSS);

var WWW = './www';

var PORT_DEV = 4000;

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

var appJs = function() {
    return gulp.src(SRC_JAVASCRIPT_ALL)
        .pipe($.concat(APP_JS));
};


gulp.task('app-js', ['lint'], function() {
    return appJs()
        .pipe(gulp.dest(DEST_DEV));
});

gulp.task('app-js-prod', ['lint'], function() {
    return appJs()
        .pipe($.stripDebug())
        .pipe($.ngmin())
        .pipe($.uglify())
        .pipe($.rename({suffix: '.min'}))
        .pipe(gulp.dest(DEST_PROD));
});

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

var appSass = function() {
    return gulp.src(SRC_CSS_ALL)
        .pipe($.concat(APP_CSS))
        .pipe($.sass())
        .pipe($.autoprefixer('last 2 versions'));
};

gulp.task('app-css', function() {
    return appSass()
        .pipe(gulp.dest(DEST_DEV));
});

gulp.task('app-css-prod', function() {
    return appSass()
        .pipe($.minifyCss())
        .pipe($.rename({suffix: '.min'}))
        .pipe(gulp.dest(DEST_PROD));
});

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

gulp.task('app-templates', function() {
    return gulp.src(SRC_TEMPLATES_ALL)
        .pipe($.angularTemplatecache(APP_TEMPLATES,{standalone:true}))
        .pipe(gulp.dest(DEST_DEV));
});


gulp.task('app-templates-prod', function() {
    return gulp.src(SRC_TEMPLATES_ALL)
        .pipe($.minifyHtml({quotes: true, empty: true, spare: true}))
        .pipe($.angularTemplatecache(APP_TEMPLATES,{standalone:true}))
        .pipe(gulp.dest(DEST_PROD));
});

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

gulp.task('app-assets', function() {
    return gulp.src(SRC_ASSETS_ALL)
        .pipe(gulp.dest(DEST_DEV));
});


gulp.task('app-assets-prod', function() {
    return gulp.src(SRC_ASSETS_ALL)
        .pipe(gulp.dest(DEST_PROD));
});

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

var appIndex = function(lib, app, dir) {
    var libStream = gulp.src(lib, {read: false});
    var appStream = gulp.src(app, {read: false});

    return gulp.src(SRC_INDEX_HTML)
            .pipe($.inject(es.merge(libStream, appStream), {ignorePath: dir, addRootSlash: false}));
};

gulp.task('app-index', function() {
    return appIndex(DEST_DEV_LIB, DEST_DEV_APP, DEST_DEV)
        .pipe(gulp.dest(DEST_DEV));
});

gulp.task('app-index-prod', function() {
    return appIndex(DEST_PROD_LIB, DEST_PROD_APP, DEST_PROD)
        .pipe($.minifyHtml({quotes: true, empty: true, spare: true}))
        .pipe(gulp.dest(DEST_PROD));
});

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

var libJs = function() {
    return gulp.src(JS_LIBRARIES)
        .pipe($.expectFile(JS_LIBRARIES))
        .pipe($.concat(LIB_JS));
};

gulp.task('lib-js', function() {
    return libJs()
        .pipe(gulp.dest(DEST_DEV));
});

gulp.task('lib-js-prod', function() {
    return libJs()
        .pipe($.stripDebug())
        .pipe($.uglify())
        .pipe($.rename({suffix: '.min'}))
        .pipe(gulp.dest(DEST_PROD));
});

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

var libCss = function() {
    return gulp.src(CSS_LIBRARIES)
        .pipe($.expectFile(CSS_LIBRARIES))
        .pipe($.concat(LIB_CSS));
};

gulp.task('lib-css', function() {
    return libCss()
        .pipe(gulp.dest(DEST_DEV));
});

gulp.task('lib-css-prod', function() {
    return libCss()
        .pipe($.minifyCss())
        .pipe($.rename({suffix: '.min'}))
        .pipe(gulp.dest(DEST_PROD));
});

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

gulp.task('watch',function() {

    // reload connect server on built file change
    gulp.watch(DEST_DEV_ALL, function(event) {
        return gulp.src(event.path)
            .pipe($.connect.reload());
    });

    // watch files to build
    gulp.watch(SRC_JAVASCRIPT_ALL, ['app-js']);
    gulp.watch(SRC_CSS_ALL, ['app-css']);
    gulp.watch(SRC_TEMPLATES_ALL, ['app-templates']);
    gulp.watch(SRC_ASSETS_ALL, ['app-assets']);
    gulp.watch(SRC_INDEX_HTML, ['app-index']);
});

gulp.task('connect', ['watch'], function() {
    $.connect.server({
        root: DEST_DEV,
        port: PORT_DEV,
        livereload: true
    });
});

gulp.task('connect-prod', function() {
    $.connect.server({
        root: DEST_PROD,
        port: PORT_DEV,
        livereload: false
    });
});

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

gulp.task('beep', function() {
    return $.util.beep();
});

gulp.task('size', function() {
    return gulp.src(DEST_DEV_ALL)
        .pipe($.size({showFiles: true, title: 'Full'}));
});

gulp.task('compile', function(callback) {
    return runSequence(['app-js', 'app-css', 'app-templates', 'app-assets', 'lib-js', 'lib-css'], 'app-index', 'size', 'beep', callback);
});

gulp.task('default', ['compile', 'connect']);

gulp.task('size-prod', function() {
    return gulp.src(DEST_PROD_ALL)
        .pipe($.size({showFiles: true, title: 'Minified'}));
});

gulp.task('compile-prod', function(callback) {
    return runSequence(['app-js-prod', 'app-css-prod', 'app-templates-prod', 'app-assets-prod', 'lib-js-prod', 'lib-css-prod'], 'app-index-prod', 'size-prod', 'beep', callback);
});

gulp.task('clean', function () {
    return gulp.src(DEST_DEV, {read: false})
        .pipe($.clean());
});

gulp.task('clean-prod', function () {
    return gulp.src(DEST_PROD, {read: false})
        .pipe($.clean());
});

gulp.task('clean-stage', function () {
    return gulp.src(WWW, {read: false})
        .pipe($.clean());
});

gulp.task('clean-all', function(callback){
    runSequence(['clean', 'clean-prod', 'clean-stage'], callback);
});

gulp.task('compile-all', function(callback){
    runSequence(['compile', 'compile-prod'], callback);
});

gulp.task('clean-compile', function(callback){
    runSequence(['clean-all', 'compile-all'], callback);
});


gulp.task('lint', function() {
    gulp.src(SRC_JAVASCRIPT_ALL)
        .pipe($.jshint())
        .pipe($.jshint.reporter('jshint-stylish'));
});

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

gulp.task('stage-files', function() {
    return gulp.src(DEST_PROD_ALL)
        .pipe(gulp.dest(WWW));
});

gulp.task('stage', function(callback) {
    return runSequence(['clean-stage', 'clean-prod'], 'compile-prod', 'stage-files', callback);
});
