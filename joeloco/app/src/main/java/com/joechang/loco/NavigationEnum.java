package com.joechang.loco;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Author:  joechang
 * Date:    3/11/15
 * Purpose: Put the navigation bar elements in an enum.  Setup the strings by init(context);
 * Watch out for reordering the enum, as you will change the navbar order as well.
 */
public enum NavigationEnum {
    //Constructor arguments are whether it's enabled in drawer, and whether it is back only
    HOME(false, false),
    PROFILE(true, true),
    GROUPS(true, true),
    NEARBY(true, true),
    BROWSE(true, false),
    FIND(false, true),
    SETTINGS(true, true),
    DEBUG(true, true);

    private boolean enabled;
    private boolean nonDrawerBackOnly;
    private CharSequence name;
    private static List<NavigationEnum> enabledValues = new LinkedList<NavigationEnum>();

    static {
        for (NavigationEnum ne : values()) {
            if (ne.enabled) {
                enabledValues.add(ne);
            }
        }
    }

    NavigationEnum(boolean enabled, boolean isNonDrawerBackOnly) {
        this.enabled = enabled;
        this.nonDrawerBackOnly = isNonDrawerBackOnly;
    }

    public boolean isNonDrawerBackOnly() {
        return this.nonDrawerBackOnly;
    }

    /**
     * This returns the index of the navEnum, but of the ones that are ENABLED.
     * So not to be confused with ordinal, which lists all (enabled or disabled) values.
     * @return -1 if this is not enabled!
     */
    public int getIndex() {
        return enabledValues.indexOf(this);
    }

    /**
     * Returns the enum based on index of the ones that are ENABLED.
     * @param index
     * @return
     */
    public static NavigationEnum fromIndex(int index) {
        if (index > enabledValues.size() - 1 || index < 0) {
            return null;
        }
        return enabledValues.get(index);
    }

    public CharSequence getName() {
        if (this.name == null) {
            return this.toString();
        }
        return this.name;
    }

    public static void init(Context c) {
        HOME.name = c.getString(R.string.title_home);
        PROFILE.name = c.getString(R.string.title_profile);
        GROUPS.name = c.getString(R.string.title_groups);
        NEARBY.name = c.getString(R.string.title_activity_nearby);
        BROWSE.name = c.getString(R.string.title_browse);
        FIND.name = c.getString(R.string.title_find);
        SETTINGS.name = c.getString(R.string.title_settings);
        DEBUG.name = "Debug";
    }

    public static String[] getTitles() {
        int i=0;
        String[] ret = new String[enabledValues.size()];
        for (NavigationEnum ne : enabledValues) {
            ret[i++] = ne.getName().toString();
        }
        return ret;
    }
}
