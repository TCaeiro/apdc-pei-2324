package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.CookieParam;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;


@Path("/tokenData")
public class showCookieResource {

	private static final Logger LOG = Logger.getLogger(showCookieResource.class.getName());

	@GET
	@Path("/")
	public Response showCookie(@CookieParam("session::apdc") Cookie cookie) {
	    if (cookie == null) {
	        return Response.status(Response.Status.UNAUTHORIZED).entity("No session token found.").build();
	    }
	    try {
	        String cookieValue = cookie.getValue();
	        String[] parts = cookieValue.split("\\.");
	        if (parts.length != 6) {
	            return Response.status(Response.Status.BAD_REQUEST).entity("Session token format is invalid.").build();
	        }

	        String username = parts[0];
	        String tokenId = parts[1];
	        String userRole = parts[2];
	        long issuedTime = Long.parseLong(parts[3]);
	        long duration = Long.parseLong(parts[4]);
	        String signature = parts[5];

	        long expiryTime = issuedTime + duration;

	        // Converte milissegundos para Instant e depois para ZonedDateTime
	        ZonedDateTime issuedDateTime = Instant.ofEpochMilli(issuedTime).atZone(ZoneId.systemDefault());
	        ZonedDateTime expiryDateTime = Instant.ofEpochMilli(expiryTime).atZone(ZoneId.systemDefault());

	        // Prepara a resposta
	        String output = "Username: " + username + "\n" +
	                        "Token ID: " + tokenId + "\n" +
	                        "User Role: " + userRole + "\n" +
	                        "Issued Time: " + issuedDateTime.toString() + "\n" +
	                        "Expiry Time: " + expiryDateTime.toString() + "\n" +
	                        "Signature: " + signature;

	        return Response.ok(output).build();

		} catch (Exception e) {
			LOG.severe("Error showing token data: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("Internal server error occurred while showing token data.").build();
		}
	}
}
