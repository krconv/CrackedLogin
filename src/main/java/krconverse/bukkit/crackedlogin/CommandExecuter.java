/**
 * CommandExecuter.java
 * 
 * @author Kodey Converse (krconverse@wpi.edu)
 */
package krconverse.bukkit.crackedlogin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Handles commands for the plugin.
 */
public class CommandExecuter implements CommandExecutor {
    private CrackedLogin plugin;
    
    /**
     * Creates a new executer for plugin commands.
     * 
     * @param plugin
     *            The plugin.
     */
    public CommandExecuter(CrackedLogin plugin) {
	this.plugin = plugin;
    }

    /* (non-Javadoc)
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (command.getName().equalsIgnoreCase("whois")) {
	    if (args.length >= 1) {
		String player = args[0];
		for (int i = 1; i < args.length; i++)
		    player += " " + args[i];
		if (!plugin.getAuthenticator().isRegistered(player)) { // player isn't valid
		    sender.sendMessage(ChatColor.RED + "\"" + player + "\" is not a registered user.");
		    return true;
		}
		String owner = plugin.getAuthenticator().getOwner(player);
		sender.sendMessage("The owner of \"" + player + "\" is " + owner);
		return true;
	    } else {
		sender.sendMessage(ChatColor.RED + "Usage: " + plugin.getCommand("whois").getUsage());
		return true;
	    }
	}
	return false;
    }

}
