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

//import pt.unl.fct.di.apdc.firstwebapp.util.UserData;

import com.google.gson.Gson;

@Path("/changeState")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeStateResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Gson g = new Gson();



	Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeState(@CookieParam("session::apdc") Cookie cookie, ChangeStateData data) {
	    if (cookie == null) {
	        return Response.status(Status.UNAUTHORIZED).entity("No session cookie found.").build();
	    }

	    try {
	        String cookieValue = cookie.getValue();
	        String[] parts = cookieValue.split("\\.");
	        if (parts.length != 6) {
	            return Response.status(Status.UNAUTHORIZED).entity("Invalid session cookie.").build();
	        }

	        String username = parts[0];
	        String role = parts[2]; // Role is now the third element
	        String signature = parts[5];
	        String fields = String.join(".", Arrays.copyOf(parts, 5));


	        // Fetch the target user entity from the datastore
	        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
	        Entity user = datastore.get(userKey);

	        if (user == null) {
	            return Response.status(Status.NOT_FOUND).entity("User not found.").build();
	        }
			// Verify if new state is either "ATIVO" or "INATIVO"
			if (!data.newState.equals("ATIVO") && !data.newState.equals("INATIVO")) {
				return Response.status(Status.BAD_REQUEST).entity("Só pode alterar para ATIVO ou INATIVO.").build();
			}

	        String currentUserRole = user.getString("user_role");
	        String currentState = user.getString("state");

	        // Verify if already in desired state
	        if (data.newState.equals(currentState)) {
	            return Response.status(Status.BAD_REQUEST).entity("User is already in the desired state.").build();
	        }

	        // Check user role and permissions
	        boolean hasPermission = false;
	        switch (role) {
	            case "SU":
	                hasPermission = true; // SU can change any state
	                break;
	            case "GA":
	                if (currentUserRole.equals("USER") || currentUserRole.equals("GBO")) {
	                    hasPermission = true;
	                }
	                break;
	            case "GBO":
	                if (currentUserRole.equals("USER")) {
	                    hasPermission = true;
	                }
	                break;
	            case "USER":
	                hasPermission = false; // USER cannot change states
	                break;
	            default:
	                hasPermission = false;
	        }

	        if (!hasPermission) {
	            return Response.status(Status.FORBIDDEN).entity("Insufficient permissions to change state.").build();
	        }

	        // Update the user state
	        user = Entity.newBuilder(user)
	                .set("state", data.newState)
	                .build();
	        datastore.update(user);

	        return Response.ok().entity(g.toJson("State updated successfully.")).build();

	    } catch (Exception e) {
	        LOG.severe(e.getMessage());
	        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error while changing state.").build();
	    }
	}


}
