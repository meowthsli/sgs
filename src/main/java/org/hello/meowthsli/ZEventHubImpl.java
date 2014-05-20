/*
 * 
 */

package org.hello.meowthsli;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ObjectNotFoundException;
import com.sun.sgs.app.util.ScalableHashSet;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Set;
import org.json.JSONObject;

/**
 *
 * @author meowth
 */
public class ZEventHubImpl implements ManagedObject, Serializable, ZEventHub
{
    private static final long serialVersionUID = 1L;
    private final HashMap<String, Set<ManagedReference<? extends ManagedObject>>> sinks;
    
    public ZEventHubImpl() {
        sinks = new HashMap<>();
    }
            
    @Override
    public void subscribe(ZEventSink sink, String kind) {
        final DataManager dm = AppContext.getDataManager();
        final BigInteger uid = dm.getObjectId(sink);
        if(!sinks.containsKey(kind)) {
            sinks.put(kind, new ScalableHashSet<>());
            dm.markForUpdate(this);
        } else {
            final Set<ManagedReference<? extends ManagedObject>> xsinks = sinks.get(kind);
            boolean found = false;
            for(ManagedReference<? extends ManagedObject> rs : xsinks) {
                if(rs.getId().equals(uid)) {
                    found = true;
                }
            }
            if(!found) {
                final ManagedReference<ManagedObject> refSink = dm.createReference((ManagedObject)sink);
                xsinks.add(refSink);
                dm.markForUpdate(uid);
            }
        }
    }
    
    @Override
    public void publish(String kind, JSONObject event) {
        final DataManager dm = AppContext.getDataManager();
        if(sinks.containsKey(kind)) {
            sinks.get(kind).stream().forEach(refSink -> {
                try {
                    ((ZEventSink)refSink.get()).consume(kind, event);
                } catch (ObjectNotFoundException e) {
                    sinks.get(kind).remove(refSink);
                    dm.markForUpdate(this);
                }
            });
        }
    }
}
