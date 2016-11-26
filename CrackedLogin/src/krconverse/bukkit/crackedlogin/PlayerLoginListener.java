/**
 * PlayerLoginListener.java
 * 
 * @author Kodey Converse (krconverse@wpi.edu)
 */
package krconverse.bukkit.crackedlogin;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * A listener to keep track of players joining and leaving the game.
 */
public class PlayerLoginListener implements Listener {
    private CrackedLogin plugin;

    /**
     * Creates a new listener to keep track of players leaving and joining the
     * game.
     * 
     * @param plugin
     *            The plugin.
     */
    public PlayerLoginListener(CrackedLogin plugin) {
	this.plugin = plugin;
    }

    /**
     * Make sure a player is allowed to join the server.
     * 
     * @param event
     *            The event arguments
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent event) {
	// see if the played is allowed to join
	Player player = event.getPlayer();
	if (!plugin.getAuthenticator().canJoin(player)) {
	    // the player isn't allowed to join
	    if (plugin.getAuthenticator().isBanned(player)) {
		event.disallow(Result.KICK_BANNED, plugin.getConfig().getString("messages.BannedLoginKick"));
	    } else if (!plugin.getAuthenticator().isRegistered(player)) {
		event.disallow(Result.KICK_WHITELIST, plugin.getConfig().getString("messages.NotRegisteredLoginKick"));
	    } else {
		event.disallow(Result.KICK_OTHER, plugin.getConfig().getString("messages.OtherLoginKick"));
	    }
	    return;
	}

	// need to pause the player's communication to server until it
	// authenticates
	plugin.getPacketListener().startListening();

	// timeout the player after configured timeout
	Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
	    public void run() {
		timeoutAuthentication(player);
	    }
	}, plugin.getConfig().getInt("authentication.Timeout"), TimeUnit.SECONDS);
    }

    /**
     * Deauthenticate players when they leave the game.
     * 
     * @param event
     *            The event arguments
     */
    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
	// deauthenticate the player that quit
	if (plugin.getAuthenticator().isAuthenticated(event.getPlayer())) {
	    plugin.getAuthenticator().deauthenticate(event.getPlayer());
	} else {
	    // stop the packet listener if there are no unauthenticated players
	    Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
	    players.remove(event.getPlayer());
	    if (!plugin.getAuthenticator().anyUnauthenticated(players)) {
		plugin.getPacketListener().stopListening();
	    }
	}
    }

    /**
     * Causes a player to be kicked if not authenticated yet.
     * 
     * @param player
     *            Player to timeout.
     */
    private void timeoutAuthentication(Player player) {
	if (!plugin.getAuthenticator().isAuthenticated(player)) {
	    player.kickPlayer(plugin.getConfig().getString("messages.TimeoutKick"));
	}
    }

}
