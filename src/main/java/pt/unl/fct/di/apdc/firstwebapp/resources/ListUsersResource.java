package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;

@Path("/listUsers")
public class ListUsersResource {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	@GET
	@Path("/")
	public Response listUsers(@CookieParam("session::apdc") Cookie cookie) {
		if (cookie == null) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("No session cookie found.").build();
		}

		String cookieValue = cookie.getValue();
		String[] parts = cookieValue.split("\\.");
		if (parts.length != 6) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid session cookie.").build();
		}

		String requesterRole = parts[2];
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").build();
		QueryResults<Entity> results = datastore.run(query);

		StringBuilder userDetailsBuilder = new StringBuilder();

		while (results.hasNext()) {
			Entity user = results.next();
			if (shouldIncludeUser(user, requesterRole)) {
				userDetailsBuilder.append(formatUserDetails(user, requesterRole)).append("\n\n");
			}
		}

		String userDetails = userDetailsBuilder.toString().trim();
		return Response.ok(userDetails).build();
	}

	private boolean shouldIncludeUser(Entity user, String requesterRole) {
		String userRole = user.contains("user_role") ? user.getString("user_role") : "";
		String profile = user.contains("perfil") ? user.getString("perfil") : "";
		String state = user.contains("state") ? user.getString("state") : "";

		switch (requesterRole) {
			case "USER":
				return "USER".equals(userRole) && "Público".equals(profile) && "ATIVO".equals(state);
			case "GBO":
				return "USER".equals(userRole);
			case "GA":
				return "USER".equals(userRole) || "GBO".equals(userRole) || "GA".equals(userRole);
			case "SU":
				return true;
			default:
				return false;
		}
	}

	private String formatUserDetails(Entity user, String requesterRole) {
		StringBuilder userDetails = new StringBuilder();
		Key userKey = user.getKey();
		userDetails.append("Username: ").append(userKey.getName()).append("\n");

		if (!"USER".equals(requesterRole)) {
			user.getProperties().forEach((key, value) -> {
				if (value.get() != null) {
					userDetails.append(key).append(": ").append(value.get()).append("\n");
				}
			});
		}

		return userDetails.toString().trim();
	}
}
