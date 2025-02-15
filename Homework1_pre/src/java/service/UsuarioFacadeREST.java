package service;

import authn.Credentials;
import authn.Secured;
import jakarta.ejb.Stateless;
import jakarta.jms.Connection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.sql.DatabaseMetaData;
import java.util.List;
import model.entities.Usuario;
import authn.JWTUtil;
import jakarta.persistence.NoResultException;

@Stateless
@Path("/usuario")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UsuarioFacadeREST {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    // GET /rest/api/v1/usuario
    @GET
    public Response getAllUsers(@Context UriInfo uriInfo) {
        // Consulta para obtener todos los usuarios sin filtros
        TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u", Usuario.class);
        List<Usuario> users = query.getResultList();

        return Response.ok(users).build();
    }

    // PUT /rest/api/v1/customer/{id} -  Actualizar un usuario
    @PUT
    @Path("{id}")
    @Secured
    public Response updateUser(@PathParam("id") Long id, Usuario updatedUser, @Context SecurityContext securityContext) {
        Usuario user = em.find(Usuario.class, id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado.").build();
        }

        /*String username = securityContext.getUserPrincipal().getName();

        // Verificar si el usuario autenticado es el mismo o si es un administrador
        if (!username.getCityHall()) {
            return Response.status(Response.Status.FORBIDDEN).entity("No tienes permiso para actualizar este usuario.").build();
        }*/

        // Actualizar los campos permitidos
        user.setCredentials(updatedUser.getCredentials()); 
     
        if (user.getCityHall()) {
            user.setCityHall(updatedUser.getCityHall()); // Solo los administradores pueden cambiar roles
        } else {
            // Asegurarse de que los usuarios no cambien su propio rol
            updatedUser.setCityHall(user.getCityHall());
        }
        
        user.setImageURL(updatedUser.getImageURL());

        try {
            em.merge(user);
            return Response.ok("Usuario actualizado correctamente.").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al actualizar el usuario.").build();
        }
    }

    // POST /rest/api/v1/customer - Crear un nuevo usuario
    @POST
    public Response createUser(Usuario user, @Context UriInfo uriInfo) {
        // Validar campos obligatorios
        if (user.getCredentials().getUsername() == null || user.getCredentials().getUsername().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El nombre de usuario no puede estar vacío.").build();
        }

        if (user.getCredentials().getPassword() == null || user.getCredentials().getPassword().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("La contraseña no puede estar vacía.").build();
        }

        // Validar el rol
        boolean cityHall= user.getCityHall();
        if (!cityHall) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El usuario no pertenece al ayuntamiento").build();
        }

        // Verificar si el username ya existe
        List<Credentials> existingCredentials = em.createQuery("SELECT c FROM Credentials c WHERE c.username = :username", Credentials.class)
                                                  .setParameter("username", user.getCredentials().getUsername())
                                                  .getResultList();
        if (!existingCredentials.isEmpty()) {
            return Response.status(Response.Status.CONFLICT).entity("El nombre de usuario ya existe").build();
        }

        try {
            // Crear el cliente
            em.persist(user);
            em.flush(); // Asegurar que el cliente tiene un ID asignado

            // Crear las credenciales asociadas
            Credentials credentials = new Credentials();
            credentials.setUsername(user.getCredentials().getUsername());
            credentials.setPassword(user.getCredentials().getPassword());
            em.persist(credentials);

            URI uri = uriInfo.getAbsolutePathBuilder().path(Long.toString(user.getId())).build();
            return Response.created(uri).entity(user.getId()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al crear el usuario: " + e.getMessage()).build();
        }
    }


    // DELETE /rest/api/v1/customer/{id} - Opcional: Eliminar un usuario
    @DELETE
    @Path("{id}")
    public Response deleteUser(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        if (securityContext.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Autenticación requerida").build();
        }

        Usuario customer = em.find(Usuario.class, id);
        if (customer == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Cliente no encontrado").build();
        }

        /*String username = securityContext.getUserPrincipal().getName();

        // Verificar permisos: el cliente puede eliminarse a sí mismo o debe ser un administrador
        if (!username.getCityHall()) {
            return Response.status(Response.Status.FORBIDDEN).entity("No tienes permiso para eliminar este cliente").build();
        }*/

        try {
            // Eliminar credenciales asociadas
            TypedQuery<Credentials> query = em.createQuery("SELECT c.credentials FROM Usuario c WHERE c.credentials.id = :customerId", Credentials.class);
            List<Credentials> credentials = query.setParameter("customerId", id).getResultList();
            for (Credentials cred : credentials) {
                em.remove(cred);
            }

            // Finalmente, eliminar el cliente
            em.remove(customer);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al eliminar el cliente: " + e.getMessage()).build();
        }
    }

    // POST /rest/api/v1/customer/login - Endpoint to login and generate JWT token
    @POST
    @Path("/login")
    public Response login(Credentials credentials) {
        try {
            TypedQuery<Credentials> query = em.createNamedQuery("Credentials.findUser", Credentials.class);
            Credentials storedCredentials = query.setParameter("username", credentials.getUsername()).getSingleResult();

            if (storedCredentials.getPassword().equals(credentials.getPassword())) {
                String token = JWTUtil.generateToken(credentials.getUsername());
                return Response.ok().entity(token).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
            }
        } catch (NoResultException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }
    }
}
