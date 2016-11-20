/**
 * CrackedLogin.java
 * 
 * @author Kodey Converse (krconverse@wpi.edu)
 */
package krconverse.bukkit.crackedlogin;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.apihelper.APIManager;
import org.inventivetalent.packetlistener.PacketListenerAPI;

/**
 * The CrackedLogin plugin.
 */
public class CrackedLogin extends JavaPlugin {
    

    /* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onLoad()
     */
    @Override
    public void onLoad() {
	APIManager.require(PacketListenerAPI.class, this);
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
	//getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);
	
	// load the configuration
	FileConfiguration config = getConfig();
	config.options().copyDefaults(true);
	if (!new File(getDataFolder(), "config.yml").exists()) {
		saveDefaultConfig();
		saveConfig();
	}
    }
    
}
