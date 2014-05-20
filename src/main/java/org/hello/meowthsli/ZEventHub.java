/*
 * 
 */

package org.hello.meowthsli;

import org.json.JSONObject;

/**
 *
 * @author meowth
 */
public interface ZEventHub
{
    void publish(String kind, JSONObject event);
    void subscribe(ZEventSink sink, String kind);    
}
