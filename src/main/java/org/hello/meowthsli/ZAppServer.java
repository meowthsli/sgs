/*
 * 
 */

package org.hello.meowthsli;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.util.ScalableHashMap;
import com.sun.sgs.app.util.ScalableHashSet;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author meowth
 */
public class ZAppServer implements AppListener, ManagedObject, Serializable, ZEventHub
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ZAppServer.class.getName());

    private ManagedReference<ScalableHashMap<String, ZPlayer>> connectedPlayers;
    private Set<ManagedReference<ZBattleRoom>> rooms;
    private ManagedReference<ZEventHubImpl> eventHub;
            
    @Override
    public void initialize(Properties props) {
        connectedPlayers = AppContext.getDataManager().createReference(new ScalableHashMap<String, ZPlayer>());
        rooms = new HashSet<>();
        eventHub = AppContext.getDataManager().createReference(new ZEventHubImpl());
        log.info("AppServer initialized");
    }

    @Override
    public ClientSessionListener loggedIn(ClientSession session) {
        log.log(Level.INFO, "Player connected, name = {0}", session.getName());
        final ZPlayer player = loadOrRegister(session);        
        connectedPlayers.getForUpdate().put(player.getName(), player);
        AppContext.getDataManager().markForUpdate(this);
        return player;        
    }
    
    public void onDisconnect(ZPlayer who) {        
        connectedPlayers.getForUpdate().remove(who.getName());
        log.log(Level.INFO, "Player disconnected, name = {0}", who.getName());        
    }
    
    public void beginRoom() {
        
    }
    
    public void onEndRoom(ZBattleRoom room) {
        
    }
    
    private ZPlayer loadOrRegister(ClientSession session) {
        log.log(Level.INFO, "Looking for player for session = {0}", session.getName());        
        final String binding = "player." + session.getName();
        // Попытка загрузить объект и перехват исключения - 
        //   единственный способ узнать, есть ли такой объект в базе            
        try {
            ZPlayer zp = (ZPlayer) AppContext.getDataManager().getBindingForUpdate(binding);
            log.log(Level.INFO, "Player exists; name = {0}", zp.getName());
            return zp;        
        } catch (NameNotBoundException e) {            
            final ZPlayer zp = new ZPlayer(session.getName(), session, this);
            log.log(Level.INFO, "Player not found, creating; name = {0}", zp);
            AppContext.getDataManager().setBinding(binding, zp);
            return zp;
        }
    }
    
    public void zapiGetRooms(ZPlayer whoAsks) {
        
    }

    void zapiJoinRoom(ZPlayer whoAsks, BigInteger roomId) {
        for(ManagedReference<ZBattleRoom> rbr : rooms) {
            if(rbr.getId().equals(roomId)) {
                final ZBattleRoom room = rbr.get();
                room.add(whoAsks);
                whoAsks.setRoom(room);
                room.sayJoined(whoAsks);
            }
        }
    }

    void zapiLeaveRoom(ZPlayer whoAsks) {
        final ZBattleRoom room = whoAsks.getRoom();
        whoAsks.setRoom(null);
        room.remove(whoAsks);        
    }

    @Override
    public void publish(String kind, JSONObject event) {
        eventHub.get().publish(kind, event);
    }

    @Override
    public void subscribe(ZEventSink sink, String kind) {
        eventHub.getForUpdate().subscribe(sink, kind);
    }
}
