package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.UserData;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;

import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.gson.Gson;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	KeyFactory keyFactory = datastore.newKeyFactory().setKind("User");

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Gson g = new Gson();

	public RegisterResource() {
	}

	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegister1(UserData data) {
	    // Inicia a transação
	    Transaction txn = datastore.newTransaction();
	    
	    try {
	        // Verificações dos campos obrigatórios
	        
	        Query<Entity> query = Query.newEntityQueryBuilder().setKind("User")
	                 .setFilter(PropertyFilter.eq("__key__", datastore.newKeyFactory().setKind("User").newKey(data.username))).build();

	        QueryResults<Entity> results = txn.run(query);
	         
	        if(results.hasNext()) {
	            return Response.status(Response.Status.BAD_REQUEST).entity("Username já registrado").build();
	        }
	        
	        if (data.username == null || data.username.trim().isEmpty()) {
	            return Response.status(Response.Status.BAD_REQUEST).entity("Username is required").build();
	        }
	        if (data.password == null || data.password.trim().isEmpty()) {
	            return Response.status(Response.Status.BAD_REQUEST).entity("Password is required").build();
	        }
	        if (data.password2 == null || !data.password.equals(data.password2)) {
	            return Response.status(Response.Status.BAD_REQUEST).entity("Passwords do not match").build();
	        }
	        if (data.gmail == null || !data.gmail.matches("[^@]+@[^@]+\\.[^@]+")) {
	            return Response.status(Response.Status.BAD_REQUEST).entity("Valid gmail is required").build();
	        }
	        if (data.phone == null) {
	            return Response.status(Response.Status.BAD_REQUEST).entity("Phone is required").build();
	        }
	        if (data.nome == null || data.nome.trim().isEmpty()) {
	            return Response.status(Response.Status.BAD_REQUEST).entity("Nome is required").build();
	        }

	        // Cria a chave para a nova entidade do usuário
	        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);

	        // Inicia a construção da entidade do usuário
	        Entity.Builder userBuilder = Entity.newBuilder(userKey)
	            .set("nome", data.nome)
	            .set("password", DigestUtils.sha512Hex(data.password))
	            .set("gmail", data.gmail)
	            .set("phone", data.phone); // O telefone agora é obrigatório
	        
	        // Adiciona os campos opcionais à entidade se fornecidos
	        if (data.perfil != null && !data.perfil.trim().isEmpty()){
				if(!data.perfil.equals("Público") && !data.perfil.equals("Privado") ){
					return Response.status(Response.Status.BAD_REQUEST).entity("Perfil só pode ser Privado ou Público").build();
				}
	            userBuilder.set("perfil", data.perfil);
	        }
	        if (data.ocupacao != null && !data.ocupacao.trim().isEmpty()) {
	            userBuilder.set("ocupacao", data.ocupacao);
	        }
	        if (data.localTrabalho != null && !data.localTrabalho.trim().isEmpty()) {
	            userBuilder.set("localTrabalho", data.localTrabalho);
	        }
	        if (data.morada != null && !data.morada.trim().isEmpty()) {
	            userBuilder.set("morada", data.morada);
	        }
	        if (data.cp != null && !data.cp.trim().isEmpty()) {
	            userBuilder.set("cp", data.cp);
	        }
	        if (data.nif != null ) {
	            userBuilder.set("nif", data.nif);
	        }
	        
	        userBuilder.set("user_role", "USER");
	        userBuilder.set("state", "INATIVO");

	        // Constrói a entidade e a armazena no Datastore
	        Entity user = userBuilder.build();
	        txn.put(user);
	        
	        // Finaliza a transação
	        txn.commit();
	        
	        return Response.ok("User added!").build();
	    } catch (Exception e) {
	        txn.rollback();
	        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred").build();
	    } finally {
	        if (txn.isActive()) {
	            txn.rollback();
	        }
	    }
	}


}
