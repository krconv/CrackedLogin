/**
 * PacketSender.java
 * 
 * @author Kodey Converse (kodey@krconv.com)
 */
package krconverse.bukkit.crackedlogin;

import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_11_R1.Packet;

/**
 * A tool for sending packets to players.
 */
public class PacketSender {
    /**
     * Sends a packet to the given player.
     * 
     * @param packet
     *            The packet to send.
     * @param player
     *            The player to send it to.
     */
    public void send(Object packet, Player player) {
	/*
	 * // capture the full version and strip it down to its base version
	 * number String version = Bukkit.getVersion(); version =
	 * version.substring(version.indexOf("(MC: ") + 5); version =
	 * version.substring(0, version.indexOf(")")); version =
	 * version.replace(".", "_"); String packageVersion = ".v" + version;
	 * try { Object entityPlayer = Class.forName("org.bukkit.craftbukkit" +
	 * packageVersion + ".entity.CraftPlayer")
	 * .getMethod("getHandle").invoke(player);
	 * 
	 * Object playerConnection = Class.forName("net.minecraft.server" +
	 * packageVersion + ".EntityPlayer")
	 * .getDeclaredField("playerConnection").get(entityPlayer);
	 * 
	 * playerConnection.getClass() .getDeclaredMethod("sendPacket",
	 * Class.forName("net.minecraft.server.v" + version.replace(".", "_") +
	 * ".PlayerConnection")) .invoke(packet); } catch
	 * (IllegalAccessException | IllegalArgumentException |
	 * InvocationTargetException | NoSuchMethodException | SecurityException
	 * | ClassNotFoundException | NoSuchFieldException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 */
	// send the packet
	((CraftPlayer) player).getHandle().playerConnection.sendPacket((Packet<?>) packet);
    }
}
