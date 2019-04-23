package br.com.systagramrest.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import br.com.systagramrest.http.Person;
import br.com.systagramrest.http.Users;
import br.com.systagramrest.repository.entity.PersonEntity;
import br.com.systagramrest.repository.entity.UsersEntity;
import br.com.systagramrest.service.UsersService;
import br.com.systagramrest.utils.GeradorSenha;
import br.com.systagramrest.utils.Message;


/**
 * Users Controller
 * @author armandosoaressousa
 *
 */
@Component
@Path("/users")
public class UserController {
	UsersService userService = new UsersService();
			
	/**
	 * Contrutor of UserController
	 * @param userService
	 */
	public UserController() {	   
	}

	 /**
     * Retorna em um JSON todos os usuarios cadastrados
     * @return código http
     */
    @GET
    @Produces("application/json")
    public List<Users> getAllUsers() {
    	
		List<Users> users = new ArrayList<Users>();
		
		List<UsersEntity> listaEntityUsers = userService.getListAll();
		
		for (UsersEntity entity : listaEntityUsers) {
			Users user = new Users();
			user.setId(entity.getId());
			user.setEnabled(true);
			user.setEmail(entity.getEmail());
			user.setPassword(entity.getPassword());
			user.setUsername(entity.getUsername());
			
			if (entity.getPerson() != null) {
				Person person = new Person();
				person.setId(entity.getPerson().getId());
				user.setPerson(person);				
			}
			
			users.add(user);
		}

		return users;
    }
    
    /**
     * Dado um id retorna o JSON dos dados do usuario
     * @param id
     * @return código http
     */
    @GET
    @Produces("application/json")
    @Path("/{id}")
    public Users getUser(@PathParam("id") String id){    	
    	Users user = new Users();
    	Message message = new Message();
    	
    	UsersEntity aux = new UsersEntity(); 
    	aux = userService.get(Long.parseLong(id));
    	
    	if (aux==null) {
    		message.setConteudo("Usuario não existe!");
    		message.setId(404);
    		return null; 
    	}
    	
    	user.setId(aux.getId());
    	user.setEmail(aux.getEmail());
    	user.setEnabled(true);
    	user.setPassword(aux.getPassword());
    	user.setUsername(aux.getUsername());
    	
    	if (aux.getPerson() != null) {
        	Person person = new Person();
        	person.setId(aux.getPerson().getId());
        	user.setPerson(person);    		
    	}
    	
    	return (user);
    }
    
    /**
     * Dados os dados de um usuario adiciona um usuario no repositorio
     * @param user
     * @return código http
     * 
     * curl -v --header "Content-Type: application/json" --request POST --data '{"username":"novousuario", "password":"novousuario"}' http://localhost:8080/webservicesystagram/rest/users  
     * 
     */
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response addUser(Users user){
    	Message message = new Message();
    	String senhaCriptografada;
    	List<Users> userAux = userService.getUserByUserName(user.getUsername()); 
    	
    	//checa se o usuário já existe
    	if (userAux.size() > 0) {
    		message.setConteudo("Usuario já existe!");
    		message.setId(404);
    		return null; 
    	}
    	
    	if (user.getPassword().length() > 1){
        	senhaCriptografada = new GeradorSenha().criptografa(user.getPassword());
        	user.setPassword(senhaCriptografada);
        	
        	UsersEntity aux = new UsersEntity();
        	aux.setEmail(user.getEmail());
        	aux.setEnabled(true);
        	aux.setId(user.getId());
        	aux.setPassword(user.getPassword());
        	aux.setUsername(user.getUsername());
        	
        	PersonEntity person = null; 
        	aux.setPerson(person);
        	
            userService.save(aux);
            
            URI uri = URI.create("/" + String.valueOf(user.getId()));
            return Response.created(uri).build();
    	}else{
    		//throw new ServiceException(400, "Informe uma senha para o usuário", 1);
    		message.setConteudo("Informe uma senha para o usuário");
    		message.setId(400);
    		return null;
    	}
    }
    
    /**
     * Dado um id e os dados do user faz sua atualizacao
     * @param id
     * @param user
     * @return código http
     */
    @PUT
    @Consumes("application/json")
    @Path("/{id}")
    public Response updateUser(@PathParam("id") String id, Users user){
    	Message message = new Message();
    	
    	Users userAux = null; 
    	UsersEntity aux = userService.get(Long.parseLong(id));

    	if (aux==null) {
    		//throw new ServiceException(404, "Usuário não existe!", 1);
    		message.setConteudo("Usuário não existe!");
    		message.setId(404);
    		return null;
    	}
    	
    	aux.setEmail(user.getEmail());
    	aux.setEnabled(true);
    	aux.setId(user.getId());
    	aux.setPassword(user.getPassword());
    	aux.setUsername(user.getUsername());
    	
    	if (user.getPerson() != null) {
        	PersonEntity person = new PersonEntity();
        	person.setId(user.getPerson().getId());
        	aux.setPerson(person);    		
    	}

    	userService.update(aux);
    	return Response.noContent().build();
    }

    /**
     * Dado um id de um usuario faz sua remocao do repositorio
     * @param id
     * @return código http
     */
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") String id){
    	Message message = new Message(); 
    	UsersEntity user = userService.get(Long.parseLong(id));
    	
    	if (user == null){
    		//throw new ServiceException(404, "Usuário " + id + " não existe!",1);
    		message.setConteudo("Usuário não existe!");
    		message.setId(404);
    		return null;
    	}

    	userService.delete(Long.parseLong(id));
        return Response.ok().build();
    }
    
    /**
     * Dado email e senha retorna o JSON dos dados do usuario
     * @param 
     * @return código http
     * @throws ServiceException 
     */
    @GET
    @Produces("application/json")
    @Path("/{email}/{senha}")
    public Users getUserAutenticado(@PathParam("email") String email,@PathParam("senha") String senha){
    	List<Users> user = userService.getUserByEmail(email);
    	Message message = new Message();
    	
    	//consulta o usuário por email e se existe retorna os dados do usuário
    	if (user.size() > 0) { //usuário existe    		
    		Iterator iter = user.iterator();
    		Object first = iter.next();
        		
    		String senhaTeste = ((UsersEntity) first).getPassword(); 
    		boolean checaSenha = new GeradorSenha().comparaSenhas(senha, senhaTeste);
        	if (senha.length() >0 && checaSenha){
        		Users aux = new Users();
        		aux.setEmail(((UsersEntity) first).getEmail());
        		aux.setEnabled(true);
        		aux.setId(((UsersEntity) first).getId());
        		aux.setPassword(((UsersEntity) first).getPassword());
        		aux.setUsername(((UsersEntity) first).getUsername());
                return (aux) ;	
        	}else{        		
        		message.setId(1);
        		message.setConteudo("Senha incorreta!");
                return null;	    		
        	}    	    		
    	}else {
    		//throw new ServiceException(404, "Usuário não existe!",1);
    		message.setId(404);
    		message.setConteudo("Usuario não existe!");
            return null;	    		
    	}    	
    }
            
}