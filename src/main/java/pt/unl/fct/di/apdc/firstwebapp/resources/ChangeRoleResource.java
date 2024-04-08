package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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

import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;

//import pt.unl.fct.di.apdc.firstwebapp.util.UserData;

import com.google.gson.Gson;

@Path("/changeRole")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeRoleResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Gson g = new Gson();
	
	public class LoginResourse {
	}

	
	Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeRole(@CookieParam("session::apdc") Cookie cookie, ChangeRoleData data) {
	    if (cookie == null) {
	        return Response.status(Status.UNAUTHORIZED).entity("Authentication cookie is missing.").build();
	    }
	    
	    // Valid roles list
	    Set<String> validRoles = new HashSet<>(Arrays.asList("SU", "GA", "GBO", "USER"));

	    try {
	        String cookieValue = cookie.getValue();
	        String[] parts = cookieValue.split("\\.");
	        if (parts.length != 6) {
	            return Response.status(Status.UNAUTHORIZED).entity("Session cookie is invalid or has been tampered with.").build();
	        }

	        String requestorUsername = parts[0];
	        String requestorRole = parts[2];
	        String signature = parts[5];
	        String fields = String.join(".", Arrays.copyOf(parts, 5));

	        // Check if the new role is valid
	        if (!validRoles.contains(data.newRole)) {
	            return Response.status(Status.FORBIDDEN).entity("The role you are trying to assign is not recognized or is invalid.").build();
	        }

	        // Fetch the target user entity from the datastore
	        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
	        Entity targetUser = datastore.get(userKey);

	        if (targetUser == null) {
	            return Response.status(Status.NOT_FOUND).entity("The user account you are trying to change does not exist.").build();
	        }

	        String targetUserRole = targetUser.getString("user_role");

	        // Verify signature
	        // ...

	        // Assume no permission initially
	        boolean hasPermission = false;

	        // Check user role and permissions
	        switch (requestorRole) {
	            case "SU":
	                hasPermission = true; // SU can change any role to any role, no further checks needed
	                break;
	            case "GA":
	                // GA pode passar contas USER para GBO (ou vice-versa)
	                if ((targetUserRole.equals("USER") && data.newRole.equals("GBO")) ||
	                    (targetUserRole.equals("GBO") && data.newRole.equals("USER"))) {
	                    hasPermission = true;
	                } else {
	                    return Response.status(Status.FORBIDDEN).entity("You do not have permission to change roles for this user.").build();
	                }
	                break;

	            // ... existing cases for GBO and USER
	            default:
	                return Response.status(Status.FORBIDDEN).entity("Your user role is not recognized or is invalid.").build();
	        }

	        if (!hasPermission) {
	            return Response.status(Status.FORBIDDEN).entity("You do not have permission to change this user's role.").build();
	        }

	        // Update the user role
	        targetUser = Entity.newBuilder(targetUser).set("user_role", data.newRole).build();
	        datastore.update(targetUser);

	        return Response.ok().entity(g.toJson("User role updated successfully to " + data.newRole + ".")).build();

	    } catch (Exception e) {
	        LOG.severe(e.getMessage());
	        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred while attempting to change the user role: " + e.getMessage()).build();
	    }
	}


}
