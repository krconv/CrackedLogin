/**
 * Authenticator.java
 * 
 * @author Kodey Converse (krconverse@wpi.edu)
 */
package krconverse.bukkit.crackedlogin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.bukkit.entity.Player;

/**
 * An authentication tool for authenticating a player.
 */
public class Authenticator {
    private CrackedLogin plugin;
    private URI uri; // the authentication url
    private HashSet<Player> authenticatedUsers; // players that have been
					     // authenticated

    /**
     * Creates a new tool for authentication.
     * 
     * @param uri
     *            The URI to use for authentication.
     * @throws URISyntaxException
     *             Thrown if the given URI couldn't be parsed
     */
    public Authenticator(String uri, CrackedLogin plugin) throws URISyntaxException {
	this.uri = new URI(uri);
	this.authenticatedUsers = new HashSet<Player>();
	this.plugin = plugin;
    }

    /**
     * Determines whether the given player is allowed to join the server.
     * 
     * @param player
     *            The player to check.
     * @return Whether the user is allowed to join.
     */
    public boolean canJoin(Player player) {
	boolean result = false;
	try {
	    result = get(new BasicNameValuePair("username", player.getName()), "canJoin");
	} catch (URISyntaxException | IOException e) {
	    plugin.getLogger().log(Level.SEVERE, "Could not reach the authentication server!", e);
	}
	return result;
    }

    /**
     * Bans a player on the authentication server.
     * 
     * @param player
     *            The player to ban.
     */
    public void ban(Player player) {
	// TODO add implementation for banning
    }

    /**
     * Unbans a player on the authentication server.
     * 
     * @param player
     *            The player to unban.
     */
    public void unban(Player player) {
	// TODO add implementation for unbanning
    }

    /**
     * Determines whether the given player is banned from the server.
     * 
     * @param player
     *            The player to check.
     * @return Whether the user is banned.
     */
    public boolean isBanned(Player player) {
	boolean result = false;
	try {
	    result = get(new BasicNameValuePair("username", player.getName()), "isBanned");
	} catch (URISyntaxException | IOException e) {
	    plugin.getLogger().log(Level.SEVERE, "Could not reach the authentication server!", e);
	}
	return result;
    }

    /**
     * Determines whether the given player is registered on the server.
     * 
     * @param player
     *            The player to check.
     * @return Whether the user is registered.
     */
    public boolean isRegistered(Player player) {
	boolean result = false;
	try {
	    result = get(new BasicNameValuePair("username", player.getName()), "isRegistered");
	} catch (URISyntaxException | IOException e) {
	    plugin.getLogger().log(Level.SEVERE, "Could not reach the authentication server!", e);
	}
	return result;
    }

    /**
     * Attempts to authenticate a player.
     * 
     * @param player
     *            The player to authenticate.
     * @param password
     *            The password to authenticate the player with.
     * @return Whether the player was authenticated with given password.
     */
    public boolean authenticate(Player player, String password) {
	boolean result = false;
	try {
	    result = get(new NameValuePair[] { new BasicNameValuePair("username", player.getName()),
		    new BasicNameValuePair("password", password) }, "canAuthenticate");
	} catch (URISyntaxException | IOException e) {
	    plugin.getLogger().log(Level.SEVERE, "Could not reach the authentication server!", e);
	}
	if (result) {
	    authenticatedUsers.add(player);
	}
	return result;
    }

    /**
     * Deauthenticates a player.
     * 
     * @param player
     *            The player to deauthenticate.
     */
    public void deauthenticate(Player player) {
	if (authenticatedUsers.contains(player)) {
	    authenticatedUsers.remove(player);
	}
    }

    /**
     * Determines whether a player is currently authenticated.
     * 
     * @param player
     *            The player to check.
     * @return Whether the player is currently authenticated.
     */
    public boolean isAuthenticated(Player player) {
	return authenticatedUsers.contains(player);
    }

    /**
     * Determines whether there are any unauthenticated users in the given list
     * of players.
     * 
     * @param players
     *            The players to check.
     * @return Whether any of the given players are not authenticated.
     */
    public boolean anyUnauthenticated(Collection<? extends Player> players) {
	for (Player player : players) {
	    if (!isAuthenticated(player)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Determines whether there are any unauthenticated users on the server.
     * 
     * @return Whether any joined players are not authenticated.
     */
    public boolean anyUnauthenticated() {
	return anyUnauthenticated(plugin.getServer().getOnlinePlayers());
    }

    /**
     * Creates and executes a get request which returns a boolean.
     * 
     * @param parameters
     *            The get parameters.
     * @param query
     *            The query string.
     * @return The response of the request.
     * @throws URISyntaxException
     *             Thrown if the given parameters are of invalid syntax.
     * @throws IOException
     *             Thrown if there is an error while making the request.
     */
    private boolean get(NameValuePair[] parameters, String query) throws IOException, URISyntaxException {

	CloseableHttpClient httpclient = HttpClients.createMinimal();
	boolean result = false;
	try {
	    // build the request
	    HttpGet get = new HttpGet(
		    new URIBuilder(this.uri).setParameters(parameters).setParameter("q", query).build());

	    // create a custom response handler
	    ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

		public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
		    int status = response.getStatusLine().getStatusCode();
		    if (status >= 200 && status < 300) {
			HttpEntity entity = response.getEntity();
			return entity != null ? EntityUtils.toString(entity) : null;
		    } else {
			throw new ClientProtocolException("Unexpected response status: " + status);
		    }
		}

	    };
	    // send the request and parse the response to a boolean
	    String response = httpclient.execute(get, responseHandler);
	    result = Boolean.parseBoolean(response);
	} finally {
	    httpclient.close();
	}
	return result;
    }

    /**
     * Creates and executes a get request which returns a boolean.
     * 
     * @param parameter
     *            The get parameter.
     * @param query
     *            The query string.
     * @return The response of the request.
     * @throws URISyntaxException
     *             Thrown if the given parameters are of invalid syntax.
     * @throws IOException
     *             Thrown if there is an error while making the request.
     */
    private boolean get(NameValuePair parameter, String query) throws URISyntaxException, IOException {
	return get(new NameValuePair[] { parameter }, query);
    }

}