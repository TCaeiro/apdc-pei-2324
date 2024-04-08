package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeAtrData;
import java.util.Arrays;
import java.util.List;

@Path("/ChangeAtr")
public class ChangeAtrResource {

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeUserAttributes(@CookieParam("session::apdc") Cookie cookie, ChangeAtrData data) {
        if (cookie == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("No session cookie found.").build();
        }

        String cookieValue = cookie.getValue();
        String[] parts = cookieValue.split("\\.");
        if (parts.length != 6) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid session cookie.").build();
        }

        String requesterRole = parts[2];
        String requesterUserId = parts[0];

        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity destinyEntity = txn.get(userKey);

            if (destinyEntity == null) {
                txn.rollback();
                return Response.status(Response.Status.NOT_FOUND).entity("User not found.").build();
            }

            if ("USER".equals(requesterRole) && !requesterUserId.equals(destinyEntity.getKey().getName())) {
                txn.rollback();
                return Response.status(Response.Status.FORBIDDEN).entity("USER can only modify their own data.").build();
            }

            if ("SU".equals(requesterRole) && "SU".equals(destinyEntity.getString("user_role"))) {
                txn.rollback();
                return Response.status(Response.Status.FORBIDDEN).entity("SU cannot modify another SU.").build();
            }



            if ("GBO".equals(requesterRole) && !"USER".equals(destinyEntity.getString("user_role"))) {
                txn.rollback();
                return Response.status(Response.Status.FORBIDDEN).entity("GBO only can change USER.").build();
            }

            if ("GA".equals(requesterRole) && (!("USER".equals(destinyEntity.getString("user_role")) || "GBO".equals(destinyEntity.getString("user_role")))))
            {
                txn.rollback();
                return Response.status(Response.Status.FORBIDDEN).entity("GA only can change USER and GBO.").build();
            }

            Entity.Builder builder = Entity.newBuilder(destinyEntity);
            updateUserEntityFromData(builder, data, requesterRole, requesterUserId);
            Entity updatedUserEntity = builder.build();

            txn.put(updatedUserEntity);
            txn.commit();

            return Response.ok().entity("User data updated successfully.").build();
        } catch (Exception e) {
            txn.rollback();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred: " + e.getMessage()).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    private void updateUserEntityFromData(Entity.Builder builder, ChangeAtrData data, String requesterRole, String requesterUserId) {
        if (data.phone != null) {
            builder.set("phone", data.phone);
        }

            if (data.gmail != null && data.gmail.matches("[^@]+@[^@]+\\.[^@]+")) {
                builder.set("gmail", data.gmail);
            }
            if (data.perfil != null) {
                builder.set("perfil", data.perfil);
            }
            if (data.ocupacao != null) {
                builder.set("ocupacao", data.ocupacao);
            }
            if (data.localTrabalho != null) {
                builder.set("localTrabalho", data.localTrabalho);
            }
            if (data.morada != null) {
                builder.set("morada", data.morada);
            }
            if (data.cp != null) {
                builder.set("cp", data.cp);
            }
            if (data.nif != null) {
                builder.set("nif", data.nif);
            }
            if (data.password != null) {
                builder.set("password", data.password);
            }
        if (!"USER".equals(requesterRole)) {
            if (data.gmail != null && data.gmail.matches("[^@]+@[^@]+\\.[^@]+")) {
                builder.set("gmail", data.gmail);
            }
            if (data.state != null) {
                builder.set("state", data.state);
            }
            if (data.role != null && Arrays.asList("USER", "GA", "GBO", "SU").contains(data.role)) {
                builder.set("user_role", data.role);
            }
            if (data.nome != null) {
                builder.set("nome", data.nome);
            }
        }
    }
}
