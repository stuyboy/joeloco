package com.joechang.loco.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by joechang on 5/19/15.
 * <p/>
 * Quick little something to generate unique Ids when we don't want to depend on Firebase, etc.
 */
public class IdUtils {

    /**
     * Generates unique 22 character base64 encoded UUID.
     * @return
     */
    public static String uniqueId() {
        UUID uuid = UUID.randomUUID();

        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return encodeBase64URLSafeString(bb.array());
    }

    /**
     * Encodes binary data using a URL-safe variation of the base64 algorithm but does not chunk the output. The
     * url-safe variation emits - and _ instead of + and / characters.
     * <b>Note: no padding is added.</b>
     *
     * @param binaryData binary data to encode
     * @return String containing Base64 characters
     */
    public static String encodeBase64URLSafeString(final byte[] binaryData) {
        Base64 base64 = new Base64();
        byte[] encoded = base64.encodeBase64(binaryData, false);
        String ret = new String(encoded);

        try {
            ret = new String(encoded, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            //eat
        }

        //Ugly, but with libraries available, get rid of + and / and padding '='
        ret = ret.replace('+', '-').replace('/', '_').replace("=","");

        return ret;
    }


}
