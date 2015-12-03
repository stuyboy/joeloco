package com.joechang.loco.config;

/**
 * Author:    joechang
 * Created:   9/12/15 10:25 AM
 * Purpose:   What query params are we passing around?
 * Use the generic WHAT, WHERE, WHO, etc when the third party search api has a facility to parse that stuff out itself.
 */
public class Params {

    public static final String ID = "id";

    //What
    public static final String WHAT = "what";
    public static final String NAME = "name";
    public static final String CUISINE = "cuisine";

    //When
    public static final String WHEN = "when";


    //Where
    public static final String WHERE = "where";
    public static final String CITY = "city";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";

    //Who
    public static final String PARTY_SIZE = "numPeople";

    //How


    //Misc
    public static final String RESULT_SIZE = "size";

}
