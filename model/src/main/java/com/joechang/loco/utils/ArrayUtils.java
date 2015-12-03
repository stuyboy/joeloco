package com.joechang.loco.utils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Author:  joechang
 * Date:    2/11/15
 * Purpose: Because we so often deal with strings and such in Android
 */
public class ArrayUtils {

    public static boolean[] toPrimitiveArray(Collection<Boolean> bb) {
        if (bb == null) {
            return new boolean[]{};
        }

        boolean[] ret = new boolean[bb.size()];
        Iterator<Boolean> i = bb.iterator();
        int j=0;
        while (i.hasNext()) {
            ret[j++] = i.next();
        }
        return ret;
    }

    public static String[] toStringArray(Collection<? extends Object> oo) {
        if (oo == null) {
            return new String[]{};
        }

        String[] ret = new String[oo.size()];
        Iterator<? extends Object> i = oo.iterator();
        int j=0;
        while (i.hasNext()) {
            ret[j++] = i.next().toString();
        }
        return ret;
    }

    //STOLEN FROM APACHE COMMONS LANG
    public static <T> T[] addAll(final T[] array1, final T... array2) {
        if (array1 == null) {
            return array2 == null ? null : array2.clone();
        } else if (array2 == null) {
            return array1 == null ? null : array1.clone();
        }
        final Class<?> type1 = array1.getClass().getComponentType();
        @SuppressWarnings("unchecked") // OK, because array is of type T
        final
        T[] joinedArray = (T[]) Array.newInstance(type1, array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        try {
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        } catch (final ArrayStoreException ase) {
            // Check if problem was due to incompatible types
            /*
             * We do this here, rather than before the copy because:
             * - it would be a wasted check most of the time
             * - safer, in case check turns out to be too strict
             */
            final Class<?> type2 = array2.getClass().getComponentType();
            if (!type1.isAssignableFrom(type2)){
                throw new IllegalArgumentException("Cannot store "+type2.getName()+" in an array of "
                        +type1.getName(), ase);
            }
            throw ase; // No, so rethrow original
        }
        return joinedArray;
    }
}
