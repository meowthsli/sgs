/*
 * 
 */

package org.hello.meowthsli;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.util.ScalableHashSet;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author meowth
 */
public class ZPlayer implements ManagedObject, ClientSessionListener, Serializable
{
    private static final long serialVersionUID = 1L; 
    
    private static final Logger log = Logger.getLogger(ZPlayer.class.getName());
    
    private final ManagedReference<ClientSession> session;
    private final String name;
    private final ManagedReference<ZAppServer> server;
    private ManagedReference<ZBattleRoom> room;
    
    public ZPlayer(String name, ClientSession session, ZAppServer server) {
        this.name = name;
        this.server = AppContext.getDataManager().createReference(server);
        this.session = AppContext.getDataManager().createReference(session);
    }

    public String getName() {
        return name;
    }
    
    public void say(String what) {
        session.get().send(Messages.encodeString(what));
    }

    @Override
    public void receivedMessage(ByteBuffer bb) {
        final JSONObject msg = new JSONObject(Messages.decodeString(bb));
        final String command = msg.get("cmd").toString();
        switch(command) {
            case "getRooms": {
                server.get().zapiGetRooms(this);
                break;
            }
            case "joinRoom": {
                final BigInteger roomId = new BigInteger(msg.get("id").toString());
                server.get().zapiJoinRoom(this, roomId);
                break;
            }
            case "leaveRoom": {
                server.get().zapiLeaveRoom(this);
                break;
            }
        }        
    }

    @Override
    public void disconnected(boolean gracefully) {
        log.info("Disconnect accepted");
        server.get().onDisconnect(this);
    }
    
    public void settle() {
        this.session.get().send(Messages.encodeString("CONNECTED"));
    }
    
    public void setRoom(ZBattleRoom room) {
        if(room == null) {
            this.room = null;
        } else {
            this.room = AppContext.getDataManager().createReference(room);
        }
    }

    public ZBattleRoom getRoom() {
        if(room == null) {
            return null;
        }
        return room.get();
    }
}
