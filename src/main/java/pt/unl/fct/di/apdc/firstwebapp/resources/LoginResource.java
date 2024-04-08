package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.google.cloud.datastore.Transaction;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;

import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangePasswordData;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.UserData;

//import pt.unl.fct.di.apdc.firstwebapp.util.UserData;

import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import com.google.gson.Gson;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	// Settings that must be in the database
	public static final String ADMIN = "Admin";
	public static final String BACKOFFICE = "Backoffice";
	public static final String REGULAR = "Regular";
	private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Gson g = new Gson();

	public class LoginResourse {
	}

	Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		try {
			Key userKey = userKeyFactory.newKey(data.username);
			Entity user = datastore.get(userKey);
			LOG.fine("Login attempt by user: " + data.username);

			if (user == null) {
				LOG.warning("USER NOT FOUNNDDDD:" + data.username);
				return Response.status(Status.FORBIDDEN).entity("User not found").build();
			}
			if (!user.getString("state").equals("ATIVO")) {
				LOG.warning("USER NOT ACTIVE:" + data.username);
				return Response.status(Status.FORBIDDEN).entity("User not ACTIVE").build();
			}
			if (!user.getString("password").equals(DigestUtils.sha512Hex(data.password))) {
				LOG.warning("palavra pass errada do user:" + data.username);

				return Response.status(Status.FORBIDDEN).entity("pass errada").build();
			}
			
			
			
			String id = UUID.randomUUID().toString();
			long currentTime = System.currentTimeMillis();
			String fields = data.username + "." + id + "." + user.getString("user_role") + "." + currentTime + "." + 1000 * 60 * 60 * 2;
			String signature = SignatureUtils.calculateHMac(key, fields);
			
			
			if (signature == null) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error while signing token. See logs.")
						.build();
			}
			String value = fields + "." + signature;
			NewCookie cookie = new NewCookie("session::apdc", value, "/", null, "comment", 1000 * 60 * 60 * 2, false,
					true);
			return Response.ok().cookie(cookie).build();
			
			
			
			
		} catch (Exception e) {
			LOG.severe("Error durting login: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal server error").build();
		}
		
	}
	@POST
	@Path("/changePassword")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePassword(@CookieParam("session::apdc") Cookie cookie, ChangePasswordData data) {
	    if (cookie == null) {
	        return Response.status(Status.UNAUTHORIZED).entity("Authentication cookie is missing.").build();
	    }

	    // Extract the username from the cookie
	    String[] cookieParts = cookie.getValue().split("\\.");
	    if (cookieParts.length < 6) {
	        return Response.status(Status.UNAUTHORIZED).entity("Invalid cookie format.").build();
	    }
	    String usernameFromCookie = cookieParts[0]; // assuming the username is the first part

		Transaction txn = datastore.newTransaction();


	    try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(usernameFromCookie);
			Entity user = txn.get(userKey);

			String pass = DigestUtils.sha512Hex(data.passAtual);
	        if (user == null) {
	            return Response.status(Status.NOT_FOUND).entity("User account not found.").build();
	        }

	        // Verify the current password
	        if (!user.getString("password").equals(DigestUtils.sha512Hex(data.passAtual))) {
				txn.rollback();
	            return Response.status(Status.FORBIDDEN).entity("Current password is incorrect.").build();
	        }

	        // Check if the new passwords match
	        if (!data.passwordNova.equals(data.passwordNova2)) {
				txn.rollback();
	            return Response.status(Status.BAD_REQUEST).entity("The new passwords do not match.").build();
	        }

	        // Proceed to update the password with a hashed version
	        user = Entity.newBuilder(user)
	                .set("password", DigestUtils.sha512Hex(data.passwordNova)) // Replace with actual hash method
	                .build();
			txn.update(user);
			txn.commit();


	        return Response.ok().entity("Password changed successfully.").build();
	    } catch (Exception e) {
	        LOG.severe("Error during password change: " + e.getMessage());
	        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal server error occurred during password change.").build();
	    }
	}



}