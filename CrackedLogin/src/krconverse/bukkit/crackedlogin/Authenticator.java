/**
 * Authenticator.java
 * 
 * @author Kodey Converse (krconverse@wpi.edu)
 */
package krconverse.bukkit.crackedlogin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.bukkit.entity.Player;

/**
 * An authentication tool for authenticating a player.
 */
public class Authenticator {
    private URI uri; // the authentication url
    private List<Player> authenticatedUsers; // players that have been
					     // authenticated
    private HttpClient httpclient;
    private Logger logger;

    /**
     * Creates a new tool for authentication.
     * 
     * @param uri
     *            The URI to use for authentication.
     * @throws URISyntaxException
     *             Thrown if the given URI couldn't be parsed
     */
    public Authenticator(String uri) throws URISyntaxException {
	this.uri = new URI(uri);
	this.authenticatedUsers = new LinkedList<Player>();
	this.httpclient = HttpClients.createDefault();
	this.logger = Logger.getLogger("Minecraft");
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
	    result = get(new BasicNameValuePair("username", player.getName()));
	} catch (URISyntaxException | IOException e) {
	    logger.log(Level.SEVERE, "Could not reach the authentication server!", e);
	}
	return result;
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
	    result = get(new BasicNameValuePair("username", player.getName()));
	} catch (URISyntaxException | IOException e) {
	    logger.log(Level.SEVERE, "Could not reach the authentication server!", e);
	}
	return result;
    }

    /**
     * Creates and executes a get request which returns a boolean.
     * 
     * @param parameters
     *            The get parameters.
     * @return The response of the request.
     * @throws URISyntaxException
     *             Thrown if the given parameters are of invalid syntax.
     * @throws IOException
     *             Thrown if there is an error while making the request.
     */
    private boolean get(List<NameValuePair> parameters) throws URISyntaxException, IOException {
	// build the request
	HttpGet get = new HttpGet(new URIBuilder(this.uri).setParameters(parameters).build());
	// send the request
	HttpResponse response = httpclient.execute(get);
	// grab and parse the content of the response
	HttpEntity entity = response.getEntity();
	BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
	boolean result = reader.readLine().equals("true");
	reader.close();
	return result;
    }

    /**
     * Creates and executes a get request which returns a boolean.
     * 
     * @param parameter
     *            The get parameter.
     * @return The response of the request.
     * @throws URISyntaxException
     *             Thrown if the given parameters are of invalid syntax.
     * @throws IOException
     *             Thrown if there is an error while making the request.
     */
    private boolean get(NameValuePair parameter) throws URISyntaxException, IOException {
	List<NameValuePair> parameters = new ArrayList<NameValuePair>();
	parameters.add(parameter);
	return get(parameters);
    }

}
