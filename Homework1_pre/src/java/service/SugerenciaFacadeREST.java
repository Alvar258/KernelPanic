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
import model.entities.Municipio;
import model.entities.Sugerencia;


@Stateless
@Path("/sugerencia")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class SugerenciaFacadeREST {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    // GET /rest/api/v1/sugerencia
    @GET
    public Response getAllSugerencias() {

        // Construir la consulta
        StringBuilder queryBuilder = new StringBuilder("SELECT a FROM Sugerencia a");

        TypedQuery<Sugerencia> query = em.createQuery(queryBuilder.toString(), Sugerencia.class);

        // Consultar todos los artículos
        List<Sugerencia> sugerencia = query.getResultList();

        return Response.ok(sugerencia).build();
    }


    // GET /rest/api/v1/sugerencia/{id}
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getSugerencia(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        try {
            Sugerencia sugerencia = em.find(Sugerencia.class, id);
            if (sugerencia == null) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("Artículo no encontrado.")
                               .type(MediaType.TEXT_PLAIN)
                               .build();
            }
            return Response.ok(sugerencia).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error interno del servidor: " + e.getMessage())
                           .type(MediaType.TEXT_PLAIN)
                           .build();
        }
    }

    // DELETE /rest/api/v1/sugerencia/{id}
    @DELETE
    @Path("{id}")
    @Secured
    public Response deleteSugerencia(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        if (securityContext.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Autenticación requerida").build();
        }

        Sugerencia sugerencia = em.find(Sugerencia.class, id);
        if (sugerencia == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Sugerencia no encontrado").build();
        }

        /*String usernameT = securityContext.getUserPrincipal().getName();
        
        TypedQuery<Usuario> query = em.createNamedQuery("Usuario.findUser", Usuario.class);
        Usuario username = query.setParameter("username", usernameT).getSingleResult();
        
        // Verificar si el usuario autenticado es el autor o un administrador
        if (!username.getCityHall()) {
            return Response.status(Response.Status.FORBIDDEN).entity("No tienes permiso para eliminar esta sugerencia").build();
        }*/

        try {
            em.remove(sugerencia);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al eliminar la sugerencia.").build();
        }
    }



    // POST /rest/api/v1/sugerencia
    @POST
    @Secured
    public Response createSugerencia(Sugerencia sugerencia, @Context UriInfo uriInfo, @Context SecurityContext securityContext) {
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
        
        TypedQuery<Municipio> query2 = em.createNamedQuery("Municipio.findMunicipality", Municipio.class);
        Municipio municipio = query2.setParameter("municipality", sugerencia.getMunicipality().getName()).getSingleResult();
        
        if (municipio == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("Introduce un municipio valido").build();
        }

        try {
            // Persistir el artículo (esto también actualizará las relaciones en la BD)
            em.persist(sugerencia);
            em.flush(); // Asegura que el ID se genere antes de construir la URI

            URI uri = uriInfo.getAbsolutePathBuilder().path(Long.toString(sugerencia.getId())).build();

            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error al crear el sugerencia: " + e.getMessage())
                           .build();
        }

    }


    // PUT /rest/api/v1/sugerencia/{id}
    @PUT
    @Path("{id}")
    @Secured
    public Response updateSugerencia(@PathParam("id") Long id, Sugerencia updatedSugerencia, @Context SecurityContext securityContext) {
        Sugerencia sugerencia2 = em.find(Sugerencia.class, id);
        if (sugerencia2 == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Sugerencia no encontrado.").build();
        }

        /*String usernameT = securityContext.getUserPrincipal().getName();
        
        TypedQuery<Usuario> query = em.createNamedQuery("Usuario.findUser", Usuario.class);
        Usuario username = query.setParameter("username", usernameT).getSingleResult();
        
        // Verificar si el usuario autenticado es el autor o administrador
        if (!username.getCityHall()) {
            return Response.status(Response.Status.FORBIDDEN).entity("No tienes permiso para actualizar esta sugerencia.").build();
        }*/
        
        TypedQuery<Municipio> query2 = em.createNamedQuery("Municipio.findMunicipality", Municipio.class);
        Municipio municipio = query2.setParameter("municipality", updatedSugerencia.getMunicipality().getName()).getSingleResult();
        
        if (municipio == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("Introduce un municipio valido").build();
        }

        // Actualizar los campos permitidos
        sugerencia2.setDescription(updatedSugerencia.getDescription());
        sugerencia2.setX(updatedSugerencia.getX());
        sugerencia2.setY(updatedSugerencia.getY());
        sugerencia2.setLikes(updatedSugerencia.getLikes());
        sugerencia2.setScore_IA(updatedSugerencia.getScore_IA());
        sugerencia2.setScore_final(updatedSugerencia.getScore_final());
        sugerencia2.setStreet(updatedSugerencia.getStreet());
        sugerencia2.setProcessed(updatedSugerencia.getProcessed());
        sugerencia2.setMunicipality(updatedSugerencia.getMunicipality());
        sugerencia2.setDateFinished(updatedSugerencia.getDateFinished());
        sugerencia2.setDateInitial(updatedSugerencia.getDateInitial());

        em.merge(sugerencia2);

        return Response.ok().build();
    }

    
}
