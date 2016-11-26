/**
 * PacketListener.java
 * 
 * @author Kodey Converse (kodey@krconv.com)
 */
package krconverse.bukkit.crackedlogin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.entity.Player;
import org.inventivetalent.packetlistener.PacketListenerAPI;
import org.inventivetalent.packetlistener.handler.PacketHandler;
import org.inventivetalent.packetlistener.handler.ReceivedPacket;
import org.inventivetalent.packetlistener.handler.SentPacket;

/**
 * A listener which controls the flow of packets between players and the server.
 */
public class PacketListener extends PacketHandler {
    private CrackedLogin plugin;
    private boolean isListening;
    private HashMap<Player, Queue<SentPacket>> queuedPackets;
    private PacketSender sender;
    
    /**
     * Creates a new listener which will hold packets being sent to or
     * from an unauthenticated player.
     * @param plugin
     */
    @SuppressWarnings("deprecation")
    public PacketListener(CrackedLogin plugin) {
	this.plugin = plugin;
	this.isListening = false;
	this.sender = new PacketSender();
    }
    
    /**
     * Turns packet listening on.
     */
    public void startListening() {
	if (!isListening) {
	    PacketListenerAPI.addPacketHandler(this);
	    isListening = true;
	}
    }
    
    /**
     * Turns packet listening off and clears the queued packets.
     */
    public void stopListening() {
	if (isListening) {
	    PacketListenerAPI.removePacketHandler(this);
	    isListening = false;
	    queuedPackets.clear();
	}
    }

    /* (non-Javadoc)
     * @see org.inventivetalent.packetlistener.handler.PacketHandler#onReceive(org.inventivetalent.packetlistener.handler.ReceivedPacket)
     */
    @Override
    public void onReceive(ReceivedPacket packet) {
	if (!plugin.getAuthenticator().isAuthenticated(packet.getPlayer())) {
	    // the player isn't authenticated
	    if (packet.getPacketName().equalsIgnoreCase("PacketPlayInChat")) {
		// the player sent a message, consider it the password
		String password = (String) packet.getPacketValue("a");
		if (plugin.getAuthenticator().authenticate(packet.getPlayer(), password)) {
		    // the password login was successful, so send all of the queued passwords
		    for (SentPacket sent : queuedPackets.get(packet.getPlayer())) {
			sender.send(sent.getPacket(), sent.getPlayer());
		    }
		    queuedPackets.remove(packet.getPlayer());
		    if (!plugin.getAuthenticator().anyUnauthenticated()) {
			// don't need to listen to packets anymore
			stopListening();
		    }
		}
	    }
	}
    }

    /* (non-Javadoc)
     * @see org.inventivetalent.packetlistener.handler.PacketHandler#onSend(org.inventivetalent.packetlistener.handler.ReceivedPacket)
     */
    @Override
    public void onSend(SentPacket packet) {
	if (!plugin.getAuthenticator().isAuthenticated(packet.getPlayer())) {
	    if (!queuedPackets.containsKey(packet.getPlayer())) {
		queuedPackets.put(packet.getPlayer(), new LinkedList<SentPacket>());
	    }
	    queuedPackets.get(packet.getPlayer()).add(packet);
	    packet.setCancelled(true);
	}
    }
}
