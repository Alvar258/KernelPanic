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
import model.entities.TiposIncidencia;
import authn.JWTUtil;
import jakarta.persistence.NoResultException;
import java.lang.Thread.State;
import model.entities.Estado;
import model.entities.Municipio;

@Stateless
@Path("/state")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class EstadoFacadeREST {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    // GET /rest/api/v1/state
    @GET
    public Response getAllState(@Context UriInfo uriInfo) {
        // Consulta para obtener todos los usuarios sin filtros
        TypedQuery<Estado> query = em.createQuery("SELECT u FROM Estado u", Estado.class);
        List<Estado> tipo = query.getResultList();

        return Response.ok(tipo).build();
    }
}
