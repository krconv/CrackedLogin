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
import org.inventivetalent.apihelper.APIManager;
import org.inventivetalent.packetlistener.PacketListenerAPI;

/**
 * The CrackedLogin plugin.
 */
public class CrackedLogin extends JavaPlugin {
    private Authenticator authenticator;
    private PacketListener packetListener;

    /*
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.java.JavaPlugin#onLoad()
     */
    @Override
    public void onLoad() {
	APIManager.require(PacketListenerAPI.class, this);

	// load the configuration
	FileConfiguration config = getConfig();
	config.options().copyDefaults(true);
	if (!new File(getDataFolder(), "config.yml").exists()) {
	    saveDefaultConfig();
	    saveConfig();
	}

	// load up the authenticator
	String authenticationURL = config.getBoolean("authentication.URL.UseHTTPS") ? "https://"
		: "http://" + config.getString("authentication.URL.Host") + config.getString("authentication.URL.Path");
	try {
	    authenticator = new Authenticator(authenticationURL, this);
	} catch (URISyntaxException e) {
	    this.getLogger().log(Level.SEVERE, "The provided authentication URL parameters are incorrect!", e);
	    this.setEnabled(false);
	    return;
	}
	
	// load up the packet listener
	this.packetListener = new PacketListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
	APIManager.initAPI(PacketListenerAPI.class);
	
	// register the login listener
	getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);

    }

    /**
     * Get the tool for authentication.
     * @return The tool for authentication.
     */
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Get the listener for packets between players and the server.
     * @return The listener for packets.
     */
    public PacketListener getPacketListener() {
        return packetListener;
    }
}
