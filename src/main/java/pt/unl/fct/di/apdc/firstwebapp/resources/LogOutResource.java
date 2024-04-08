package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

@Path("/logout")
public class LogOutResource {
	
	@POST
	@Path("/")
	public Response logout(@CookieParam("session::apdc") Cookie cookie) {
	    if (cookie == null) {
	        // Se não há cookie, não há sessão para encerrar.
	        return Response.status(Response.Status.BAD_REQUEST).entity("No session to logout from.").build();
	    }

	    // Cria um cookie de sessão com valor vazio e duração 0 para remover o cookie existente.
	    NewCookie revokedCookie = new NewCookie("session::apdc", "", "/", null, Cookie.DEFAULT_VERSION, "Session revoked", 0, false);

	    // Prepara a resposta informando que a sessão foi encerrada
	    return Response.ok("Session terminated successfully.").cookie(revokedCookie).build();
	}

}
