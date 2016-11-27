/**
 * PacketAdapter.java
 * 
 * @author Kodey Converse (kodey@krconv.com)
 */
package krconverse.bukkit.crackedlogin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.comphenix.packetwrapper.WrapperPlayClientChat;
import com.comphenix.packetwrapper.WrapperPlayServerPosition;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

/**
 * A listener which controls the flow of packets between players and the server.
 */
public class PacketAdapter extends com.comphenix.protocol.events.PacketAdapter {
    private CrackedLogin plugin;
    private boolean isListening;
    private HashMap<Player, Queue<PacketContainer>> queuedPackets;
    private HashSet<String> allowedPackets;

    /**
     * Creates a new listener which will hold packets being sent to or from an
     * unauthenticated player.
     * 
     * @param plugin
     */
    public PacketAdapter(CrackedLogin plugin) {
	super(plugin, ListenerPriority.NORMAL, PacketType.values());
	this.plugin = plugin;
	this.isListening = false;
	this.queuedPackets = new HashMap<Player, Queue<PacketContainer>>();
	this.allowedPackets = new HashSet<String>();

	for (String packet : plugin.getConfig().getStringList("authentication.AllowedPackets")) {
	    allowedPackets.add(packet);
	}
    }

    /**
     * Turns packet listening on.
     */
    public void startListening() {
	if (!isListening) {
	    plugin.getProtocolManager().addPacketListener(this);
	    isListening = true;
	}
    }

    /**
     * Turns packet listening off and clears the queued packets.
     */
    public void stopListening() {
	if (isListening) {
	    plugin.getProtocolManager().removePacketListener(this);
	    isListening = false;
	    queuedPackets.clear();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.comphenix.protocol.events.PacketAdapter#onPacketReceiving(com.
     * comphenix.protocol.events.PacketEvent)
     */
    @Override
    public void onPacketReceiving(PacketEvent event) {
	if (event.getPacketType().getProtocol() == Protocol.PLAY) {
	    if (event.getPlayer() != null && !plugin.getAuthenticator().isAuthenticated(event.getPlayer())) {
		// the player isn't authenticated
		event.setCancelled(true);
		if (event.getPacketType().name().equals("CHAT")) {
		    // the player sent a message, consider it the password
		    String password = (String) new WrapperPlayClientChat(event.getPacket()).getMessage().toString();
		    if (plugin.getAuthenticator().authenticate(event.getPlayer(), password)) {
			// the password login was successful, so send all of the
			// queued passwords
			for (PacketContainer sent : queuedPackets.get(event.getPlayer())) {
			    try {
				plugin.getProtocolManager().sendServerPacket(event.getPlayer(), sent);
			    } catch (InvocationTargetException e) {
				plugin.getLogger().log(Level.WARNING, "Failed to send a packet!", e);
			    }
			}
			// stop listening to packets if all players are
			// authenticated
			if (!plugin.getAuthenticator().anyUnauthenticated()) {
			    // don't need to listen to packets anymore
			    stopListening();
			}
			queuedPackets.remove(event.getPlayer());
			// broadcast the login message
			plugin.getServer().broadcastMessage(
				ChatColor.YELLOW + event.getPlayer().getName() + " has joined the game");
			event.getPlayer().setInvulnerable(false);
		    }
		}
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.comphenix.protocol.events.PacketAdapter#onPacketSending(com.comphenix
     * .protocol.events.PacketEvent)
     */
    @Override
    public void onPacketSending(PacketEvent event) {
	if (event.getPacketType().getProtocol() == Protocol.PLAY) {
	    if (event.getPlayer() != null && !plugin.getAuthenticator().isAuthenticated(event.getPlayer())) {
		if (!allowedPackets.contains(event.getPacketType().name())) {
		    // we can't send this packet to an unauthenticated player
		    event.setCancelled(true);
		    // add the packet to the queue to send the player after
		    // authentication
		    if (!queuedPackets.containsKey(event.getPlayer())) {
			queuedPackets.put(event.getPlayer(), new LinkedList<PacketContainer>());
		    }
		    queuedPackets.get(event.getPlayer()).add(event.getPacket());
		    // send fake needed packets
		    if (event.getPacketType() == PacketType.Play.Server.POSITION) {
			WrapperPlayServerPosition position = new WrapperPlayServerPosition();
			position.setX(0);
			position.setY(0);
			position.setZ(0);
			position.setPitch(0);
			position.setYaw(0);
			try {
			    plugin.getProtocolManager().sendServerPacket(event.getPlayer(), position.getHandle(),
				    false);
			} catch (InvocationTargetException e) {
			    plugin.getLogger().log(Level.WARNING,
				    "Couldn't send a fake position packet to " + event.getPlayer(), e);
			}
		    }
		}
	    }
	}
    }
}
