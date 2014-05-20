/*
 * 
 */

package org.hello.meowthsli;

import com.sun.sgs.app.ManagedReference;
import java.nio.ByteBuffer;
import org.json.JSONObject;

/**
 *
 * @author meowth
 */
public class Messages {

    public static ByteBuffer encodeString(String s) {
        return ByteBuffer.wrap(s.getBytes());
    }

    /**
     * 
     * @param message
     * @return 
     */
    public static String decodeString(ByteBuffer message) {
        final byte[] bytes = new byte[message.remaining()];
        message.get(bytes);
        return new String(bytes);
    }

    static String createPlayerJoined(ZPlayer rp) {
        final JSONObject msg = new JSONObject();
        msg.put("name", rp.getName());
        return msg.toString();
    }
}