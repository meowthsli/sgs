/*
 * 
 */

package org.hello.meowthsli;

import org.json.JSONObject;

/**
 *
 * @author meowth
 */
public interface ZEventSink 
{
    void consume(String kind, JSONObject event);
}
