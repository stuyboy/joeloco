Installation:
=============
Install all needed dependencies such as bower and gulp. Should run ```bower install``` as well.

```
npm install
```

Run local server:
=================
Compiles application and library javascript and css and runs a local server on http://localhost:4000

The compiled files are placed in build/dev.

```
gulp
```

Stage for production:
=====================
Minifies all javascript and css and copies all the files to www directory. This is the directory we should run from in prod.

```
gulp stage
```
