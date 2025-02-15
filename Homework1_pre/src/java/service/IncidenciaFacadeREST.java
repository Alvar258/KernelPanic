package service;

import authn.Secured;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
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
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import model.entities.Usuario;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import model.entities.Estado;
import model.entities.Incidencia;
import model.entities.Municipio;
import model.entities.TiposIncidencia;


@Stateless
@Path("/incidencia")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class IncidenciaFacadeREST {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    // GET /rest/api/v1/incidencia
    @GET
    public Response getAllIncidencias() {

        // Construir la consulta
        StringBuilder queryBuilder = new StringBuilder("SELECT a FROM Incidencia a");

        TypedQuery<Incidencia> query = em.createQuery(queryBuilder.toString(), Incidencia.class);

        // Consultar todos los artículos
        List<Incidencia> incidencias = query.getResultList();

        return Response.ok(incidencias).build();
    }


    // GET /rest/api/v1/incidencias/{id}
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getIncidencia(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        try {
            Incidencia incidencia = em.find(Incidencia.class, id);
            if (incidencia == null) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("Artículo no encontrado.")
                               .type(MediaType.TEXT_PLAIN)
                               .build();
            }
            return Response.ok(incidencia).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error interno del servidor: " + e.getMessage())
                           .type(MediaType.TEXT_PLAIN)
                           .build();
        }
    }

    // DELETE /rest/api/v1/incidencia/{id}
    @DELETE
    @Path("{id}")
    @Secured
    public Response deleteIncidencia(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        /*if (securityContext.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Autenticación requerida").build();
        }*/

        Incidencia incidencia = em.find(Incidencia.class, id);
        if (incidencia == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Incidencia no encontrado").build();
        }

        /*String username = securityContext.getUserPrincipal().getName().trim();
        
        System.out.println(username);
        
        TypedQuery<Usuario> query = em.createNamedQuery("Usuario.iscityHall", Usuario.class);
        Usuario usuario = query.setParameter("username", username).getSingleResult();
        
        // Verificar si el usuario autenticado es el autor o un administrador
        if (!usuario.getCityHall()) {
            return Response.status(Response.Status.FORBIDDEN).entity("No tienes permiso para eliminar esta incidencia").build();
        }*/

        try {
            em.remove(incidencia);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al eliminar la incidencia.").build();
        }
    }



    // POST /rest/api/v1/incidencia
    @POST
    @Secured
    public Response createIncidencia(Incidencia incidencia, @Context UriInfo uriInfo, @Context SecurityContext securityContext) {
        // Verificar si el usuario está autenticado
        Principal principal = securityContext.getUserPrincipal();
        if (principal == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Autenticación requerida").build();
        }

        /*String usernameT = securityContext.getUserPrincipal().getName();
        
        TypedQuery<Usuario> query = em.createNamedQuery("Usuario.findUser", Usuario.class);
        Usuario username = query.setParameter("username", usernameT).getSingleResult();
        
        // Log para verificar el autor autenticado
        System.out.println("Usuario autenticado: " + username.getCredentials().getUsername());

        // Verificar si el usuario existe en la base de datos
        Usuario user;
        try {
            user = em.createQuery("SELECT u FROM Usuario u WHERE u.username = :username", Usuario.class)
                     .setParameter("username", username)
                     .getSingleResult();
        } catch (NoResultException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Usuario no autentificado").build();
        }*/
        
        TypedQuery<Estado> query2 = em.createNamedQuery("Estado.findState", Estado.class);
        Estado state = query2.setParameter("state", incidencia.getState().getName()).getSingleResult();
        
        if (state == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("Introduce un estado valido").build();
        }
        
        TypedQuery<Municipio> query3 = em.createNamedQuery("Municipio.findMunicipality", Municipio.class);
        Municipio municipio = query3.setParameter("municipality", incidencia.getMunicipality().getName()).getSingleResult();
        
        if (municipio == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("Introduce un municipio valido").build();
        }
        
        TypedQuery<TiposIncidencia> query4 = em.createNamedQuery("TiposIncidencia.findType", TiposIncidencia.class);
        TiposIncidencia type = query4.setParameter("type", incidencia.getType().getName()).getSingleResult();
        
        if (type == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("Introduce un tipo valido").build();
        }
        
        try {
            // Persistir el artículo (esto también actualizará las relaciones en la BD)
            em.persist(incidencia);
            em.flush(); // Asegura que el ID se genere antes de construir la URI

            URI uri = uriInfo.getAbsolutePathBuilder().path(Long.toString(incidencia.getId())).build();

            return Response.created(uri).entity(incidencia.getId().toString()).type(MediaType.TEXT_PLAIN).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error al crear el incidencia: " + e.getMessage())
                           .build();
        }

    }


    // PUT /rest/api/v1/incidencia/{id} 
    @PUT
    @Path("{id}")
    @Secured
    public Response updateIncidencia(@PathParam("id") Long id, Incidencia updatedIncidencia, @Context SecurityContext securityContext) {
        Incidencia incidencia2 = em.find(Incidencia.class, id);
        if (incidencia2 == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Incidencia no encontrado.").build();
        }

        /*String usernameT = securityContext.getUserPrincipal().getName();
        
        TypedQuery<Usuario> query = em.createNamedQuery("Usuario.findUser", Usuario.class);
        Usuario username = query.setParameter("username", usernameT).getSingleResult();
        
        // Verificar si el usuario autenticado es el autor o administrador
        if (!username.getCityHall()) {
            return Response.status(Response.Status.FORBIDDEN).entity("No tienes permiso para actualizar esta incidencia.").build();
        }*/
        
        TypedQuery<Estado> query2 = em.createNamedQuery("Estado.findState", Estado.class);
        Estado state = query2.setParameter("state", updatedIncidencia.getState().getName()).getSingleResult();
        
        if (state == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("Introduce un estado valido").build();
        }
        
        TypedQuery<Municipio> query3 = em.createNamedQuery("Municipio.findMunicipality", Municipio.class);
        Municipio municipio = query3.setParameter("municipality", updatedIncidencia.getMunicipality().getName()).getSingleResult();
        
        if (municipio == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("Introduce un municipio valido").build();
        }
        
        TypedQuery<TiposIncidencia> query4 = em.createNamedQuery("TiposIncidencia.findType", TiposIncidencia.class);
        TiposIncidencia type = query4.setParameter("type", updatedIncidencia.getType().getName()).getSingleResult();
        
        if (type == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("Introduce un tipo valido").build();
        }

        // Actualizar los campos permitidos
        incidencia2.setDescription(updatedIncidencia.getDescription());
        incidencia2.setEmoji(updatedIncidencia.getEmoji());
        incidencia2.setScore_IA(updatedIncidencia.getScore_IA());
        incidencia2.setScore_final(updatedIncidencia.getScore_final());
        incidencia2.setStreet(updatedIncidencia.getStreet());
        incidencia2.setType(updatedIncidencia.getType());
        incidencia2.setLikes(updatedIncidencia.getLikes());
        incidencia2.setX(updatedIncidencia.getX());
        incidencia2.setY(updatedIncidencia.getY());
        incidencia2.setDateFinished(updatedIncidencia.getDateFinished());
        incidencia2.setDateInitial(updatedIncidencia.getDateInitial());
        incidencia2.setState(updatedIncidencia.getState());
        incidencia2.setMunicipality(updatedIncidencia.getMunicipality());


        em.merge(incidencia2);

        return Response.ok().build();
    }

    
}
