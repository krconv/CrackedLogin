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

import org.bukkit.entity.Player;

import com.comphenix.packetwrapper.WrapperPlayClientChat;
import com.comphenix.packetwrapper.WrapperPlayServerChat;
import com.comphenix.packetwrapper.WrapperPlayServerPosition;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import net.md_5.bungee.api.ChatColor;

/**
 * A listener which controls the flow of packets between players and the server.
 */
public class PacketAdapter extends com.comphenix.protocol.events.PacketAdapter {
    private CrackedLogin plugin;

    private HashSet<Player> filteredPlayers;
    private HashMap<Player, Queue<PacketContainer>> queuedPackets;
    private HashSet<String> allowedPackets;

    public static final String WHITELIST_STRING = "\u0000";
    private static final String WHITELIST_STRING_ESCAPED = "\\u0000";

    /**
     * Creates a new listener which will hold packets being sent to or from an
     * unauthenticated player.
     * 
     * @param plugin
     */
    public PacketAdapter(CrackedLogin plugin) {
	super(plugin, ListenerPriority.NORMAL, PacketType.values());
	this.plugin = plugin;
	this.filteredPlayers = new HashSet<Player>();
	this.queuedPackets = new HashMap<Player, Queue<PacketContainer>>();
	this.allowedPackets = new HashSet<String>();

	for (String packet : plugin.getConfig().getStringList("authentication.AllowedPackets")) {
	    allowedPackets.add(packet);
	}
    }

    /**
     * Starts listening for packets to the given player and blocking not-allowed
     * packets.
     * 
     * @param player
     *            The player to filter packets to/from.
     */
    public void startFilteringPlayer(Player player) {
	if (filteredPlayers.isEmpty()) // need to start listening for all
				       // packets
	    plugin.getProtocolManager().addPacketListener(this);

	filteredPlayers.add(player);
	if (!queuedPackets.containsKey(player))
	    queuedPackets.put(player, new LinkedList<PacketContainer>());
    }

    /**
     * Stops listening for packets to the given player.
     * 
     * @param player
     *            The player to stop filtering packets to/from.
     * @param sendPackets
     *            Whether to send the queued packets for this player.
     */
    public void stopFilteringPlayer(Player player, boolean sendPackets) {
	if (filteredPlayers.contains(player))
	    filteredPlayers.remove(player);

	if (filteredPlayers.isEmpty())
	    plugin.getProtocolManager().removePacketListener(this);

	if (sendPackets)
	    sendPackets(player, queuedPackets.get(player));
	queuedPackets.remove(player);
    }

    /**
     * Sends all of the given packets.
     * 
     * @param player
     *            The player to send packets to.
     * @param packets
     *            The packets to send.
     */
    private void sendPackets(Player player, Queue<PacketContainer> packets) {
	for (PacketContainer sent : packets) {
	    try {
		plugin.getProtocolManager().sendServerPacket(player, sent);
	    } catch (InvocationTargetException e) {
		plugin.getLogger().log(Level.WARNING, "Failed to send a packet!", e);
	    }
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
	    if (filteredPlayers.contains(event.getPlayer())) {
		if (!allowedPackets.contains(event.getPacketType().name())) {
		    // the player is being filtered
		    event.setCancelled(true);
		    if (event.getPacketType() == PacketType.Play.Client.CHAT) {
			// the player sent a message, consider it the password
			String password = (String) new WrapperPlayClientChat(event.getPacket()).getMessage().toString();
			boolean authenticated = plugin.getAuthenticator().authenticate(event.getPlayer(), password);
			if (!authenticated) {
			    plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
				public void run() {
				    event.getPlayer().kickPlayer(plugin.getConfig().getString("messages.InvalidLogin"));
				}
			    });
			} else {
			    event.getPlayer().setInvulnerable(false);
			    plugin.getServer().broadcastMessage(
				    ChatColor.YELLOW + event.getPlayer().getName() + " has joined the game.");
			}
			stopFilteringPlayer(event.getPlayer(), authenticated);
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
	    if (filteredPlayers.contains(event.getPlayer())) {
		if (!allowedPackets.contains(event.getPacketType().name())) {
		    boolean isAllowed = false;

		    // allow the blocked packet if we sent it with special
		    // character
		    if (event.getPacketType() == PacketType.Play.Server.CHAT) {
			WrappedChatComponent message = new WrapperPlayServerChat(event.getPacket()).getMessage();
			if (message.getJson().contains(WHITELIST_STRING_ESCAPED)) {
			    isAllowed = true;
			}
		    }
		    if (!isAllowed) {
			// we can't send this packet to a filtered player
			event.setCancelled(true);
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
}
