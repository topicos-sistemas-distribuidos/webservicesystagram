package br.com.systagramrest.controller;

import java.net.URI;
import java.util.LinkedList;
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

import br.com.systagramrest.repository.entity.Users;
import br.com.systagramrest.service.UsersService;
import br.com.systagramrest.utils.GeradorSenha;
import br.com.systagramrest.utils.Message;
import br.com.systagramrest.utils.ServiceException;


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
       	List<Users> listUsers = new LinkedList<Users>();
    	listUsers = userService.getListAll();
    	return listUsers;
    }
    
    /**
     * Dado um id retorna o JSON dos dados do usuario
     * @param id
     * @return código http
     * @throws ServiceException 
     */
    @GET
    @Produces("application/json")
    @Path("/{id}")
    public Users getUser(@PathParam("id") String id) throws ServiceException{    	
    	Users user = null; 

    	user = userService.get(Long.parseLong(id));
    	
    	if (user==null) {
    		throw new ServiceException(404, "Usuário não existe!", 1);
    	}

    	return (user);
    }
    
    /**
     * Dados os dados de um usuario adiciona um usuario no repositorio
     * @param user
     * @return código http
     * 
     * curl -v --header "Content-Type: application/json" --request POST --data '{"username":"novousuario", "password":"novousuario"}' http://localhost:8083/demo/users
     * @throws ServiceException 
     * 
     */
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response addUser(Users user) throws ServiceException {
    	
    	String senhaCriptografada;
    	Users userAux = userService.getUserByUserName(user.getUsername()); 
    	
    	//checa se o usuário já existe
    	if (userAux != null) {
    		throw new ServiceException(400, "Usuário já existe!", 1);
    	}
    	
    	if (user.getPassword().length() > 1){
        	senhaCriptografada = new GeradorSenha().criptografa(user.getPassword());
        	user.setPassword(senhaCriptografada);
            userService.save(user);
            URI uri = URI.create("/" + String.valueOf(user.getId()));
            return Response.created(uri).build();
    	}else{
    		throw new ServiceException(400, "Informe uma senha para o usuário", 1);
    	}
    }
    
    /**
     * Dado um id e os dados do user faz sua atualizacao
     * @param id
     * @param user
     * @return código http
     * @throws ServiceException 
     */
    @PUT
    @Consumes("application/json")
    @Path("/{id}")
    public Response updateUser(@PathParam("id") String id, Users user) throws ServiceException {
    	Users userAux = null; 
    	userAux = userService.get(Long.parseLong(id));

    	if (userAux==null) {
    		throw new ServiceException(404, "Usuário não existe!", 1);
    	}	

    	userService.save(user);
    	return Response.noContent().build();
    }

    /**
     * Dado um id de um usuario faz sua remocao do repositorio
     * @param id
     * @return código http
     * @throws ServiceException 
     */
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") String id) throws ServiceException{
    	Users user = userService.get(Long.parseLong(id));
    	
    	if (user == null){
    		throw new ServiceException(404, "Usuário " + id + " não existe!",1);
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
    public Object getUserAutenticado(@PathParam("email") String email,@PathParam("senha") String senha) throws ServiceException {
    	Users user = userService.getUserByEmail(email);
    	Message message = new Message();
    	
    	//consulta o usuário por email e se existe retorna os dados do usuário
    	if (user != null) { //usuário existe
    		boolean checaSenha = new GeradorSenha().comparaSenhas(senha, user.getPassword());
        	if (senha.length() >0 && checaSenha){        		
                return user;	
        	}else{        		
        		message.setId(1);
        		message.setConteudo("Senha incorreta!");
                return message;	    		
        	}    	    		
    	}else {
    		throw new ServiceException(404, "Usuário não existe!",1);
    	}    	
    }
            
}