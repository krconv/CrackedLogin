/**
 * CrackedLogin.java
 * 
 * @author Kodey Converse (krconverse@wpi.edu)
 */
package krconverse.bukkit.crackedlogin;

import java.io.File;
import java.net.URISyntaxException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

/**
 * The CrackedLogin plugin.
 */
public class CrackedLogin extends JavaPlugin {
    private Authenticator authenticator;
    private PacketAdapter packetAdapter;
    private ProtocolManager protocolManager;

    /*
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
	protocolManager = ProtocolLibrary.getProtocolManager();

	// load the configuration
	FileConfiguration config = getConfig();
	config.options().copyDefaults(true);
	if (!new File(getDataFolder(), "config.yml").exists()) {
	    saveDefaultConfig();
	    saveConfig();
	}
	// load up the authenticator
	String authenticationURL = new StringBuilder()
		.append(config.getBoolean("authentication.URL.UseHTTPS") ? "https://" : "http://")
		.append(config.getString("authentication.URL.Host")).append(config.getString("authentication.URL.Path"))
		.toString();
	try {
	    authenticator = new Authenticator(authenticationURL, this);
	} catch (URISyntaxException e) {
	    this.getLogger().log(Level.SEVERE, "The provided authentication URL parameters are incorrect!", e);
	    this.setEnabled(false);
	    return;
	}

	// load up the packet listener
	this.packetAdapter = new PacketAdapter(this);

	// register the login listener
	getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);

    }

    /**
     * Get the tool for authentication.
     * 
     * @return The tool for authentication.
     */
    public Authenticator getAuthenticator() {
	return authenticator;
    }

    /**
     * Get the adapter for packets between players and the server.
     * 
     * @return The adapter for packets.
     */
    public PacketAdapter getPacketAdapter() {
	return packetAdapter;
    }

    /**
     * Get the protocol manager for access to packet handling.
     * 
     * @return the protocol manager.
     */
    public ProtocolManager getProtocolManager() {
	return protocolManager;
    }

}
