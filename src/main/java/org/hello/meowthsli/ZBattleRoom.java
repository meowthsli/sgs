/*
 * 
 */

package org.hello.meowthsli;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONObject;

/**
 * A room or table or other zone where players are taking battle
 * @author meowth
 */
public class ZBattleRoom implements ManagedObject, Serializable, ZEventSink {
    private final Set<ManagedReference<ZPlayer>> players;
    private final ManagedReference<ZAppServer> server;
    
    public ZBattleRoom(ZAppServer server) {
        players = new HashSet<>();
        this.server = AppContext.getDataManager().createReference(server);

        this.server.get().subscribe(this, "playerJoined");
    }
    
    public void add(ZPlayer player) {
        AppContext.getDataManager().markForUpdate(this);
        players.add(AppContext.getDataManager().createReference(player)); 
        server.get().publish("playerJoined", new JSONObject("{player: '" + player.getName() + "'}"));
    }
    
    public void sayJoined(ZPlayer player) {
        players.stream().map(rp -> rp.get()).forEach(p -> {
            final String msg = Messages.createPlayerJoined(p);
            if (p != player) {
                p.say(msg);
            }
        });
    }

    public void remove(ZPlayer whoAsks) {
        AppContext.getDataManager().markForUpdate(this);
        players.remove(AppContext.getDataManager().createReference(whoAsks));
    }

    @Override
    public void consume(String kind, JSONObject event) {
        
    }
}
