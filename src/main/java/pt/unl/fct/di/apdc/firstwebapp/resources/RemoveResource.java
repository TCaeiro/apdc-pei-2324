package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.Arrays;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import javax.ws.rs.Produces;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;

import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeStateData;
import pt.unl.fct.di.apdc.firstwebapp.util.RemoveData;

//import pt.unl.fct.di.apdc.firstwebapp.util.UserData;

import com.google.gson.Gson;

@Path("/removeUser")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Gson g = new Gson();

	

	Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeUser(@CookieParam("session::apdc") Cookie cookie, RemoveData data) {
	    if (cookie == null) {
	        return Response.status(Status.UNAUTHORIZED).entity("Authentication cookie is missing.").build();
	    }
	    
	    try {
	        String cookieValue = cookie.getValue();
	        String[] parts = cookieValue.split("\\.");
	        if (parts.length != 6) {
	            return Response.status(Status.UNAUTHORIZED).entity("Session cookie is invalid or has been tampered with.").build();
	        }

	        String requestorUsername = parts[0];
	        String requestorRole = parts[2];

	        // Fetch the target user entity from the datastore
	        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
	        Entity targetUser = datastore.get(userKey);

	        if (targetUser == null) {
	            return Response.status(Status.NOT_FOUND).entity("The user account you are trying to remove does not exist.").build();
	        }

	        String targetUserRole = targetUser.getString("user_role");

	        // Check user role and permissions to remove the account
	        switch (requestorRole) {
	            case "SU":
	                // SU can remove any account
	                break;
	            case "GA":
	                if (!(targetUserRole.equals("GBO") || targetUserRole.equals("USER"))) {
	                    return Response.status(Status.FORBIDDEN).entity("As GA, you can only remove GBO or USER accounts.").build();
	                }
	                break;
	            case "GBO":
	                // GBO cannot remove accounts
	                return Response.status(Status.FORBIDDEN).entity("As GBO, you are not permitted to remove accounts.").build();
	            case "USER":
	                // USER can only remove their own account
	                if (!requestorUsername.equals(data.username)) {
	                    return Response.status(Status.FORBIDDEN).entity("As a USER, you can only remove your own account.").build();
	                }
	                break;
	            default:
	                return Response.status(Status.FORBIDDEN).entity("Your user role is not recognized or is invalid.").build();
	        }

	        // Proceed to remove the user account
	        datastore.delete(userKey);
	        return Response.ok().entity(g.toJson("User account removed successfully.")).build();

	    } catch (Exception e) {
	        LOG.severe(e.getMessage());
	        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred while attempting to remove the user account: " + e.getMessage()).build();
	    }
	}



}
