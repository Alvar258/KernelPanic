package service;

import jakarta.ejb.Stateless;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import model.entities.Incident;
import model.entities.IncidentDTO;
import model.entities.IncidentType;
import model.entities.IncidentPhoto;
import java.util.Date;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;

@Stateless
@Path("/incident")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class IncidentFacadeREST {

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    @GET
    public Response getIncidents() {
        try {
            String queryStr = "SELECT i FROM Incident i ORDER BY i.incidentDate DESC";
            TypedQuery<Incident> query = em.createQuery(queryStr, Incident.class);
            List<Incident> incidents = query.getResultList();

            List<IncidentDTO> incidentDTOs = incidents.stream().map(incident -> {
                IncidentDTO dto = new IncidentDTO();
                dto.setId(incident.getId());
                dto.setTitle(incident.getTitle());
                dto.setDescription(incident.getDescription());
                dto.setImageUrl(incident.getImageUrl());
                dto.setIncidentDate(incident.getIncidentDate());
                dto.setLatitude(incident.getLatitude());
                dto.setLongitude(incident.getLongitude());
                dto.setLikes(incident.getLikes());
                dto.setIncidentType(incident.getIncidentType() != null ? incident.getIncidentType().getName() : null);
                if (incident.getPhotos() != null) {
                    List<String> photoUrls = incident.getPhotos().stream()
                            .map(IncidentPhoto::getPhotoUrl)
                            .collect(Collectors.toList());
                    dto.setPhotoUrls(photoUrls);
                }
                return dto;
            }).collect(Collectors.toList());

            return Response.ok(incidentDTOs).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error interno del servidor: " + e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("{id}")
    public Response getIncident(@PathParam("id") Long id) {
        try {
            Incident incident = em.find(Incident.class, id);
            if (incident == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Incidència no trobada.")
                        .type(MediaType.TEXT_PLAIN)
                        .build();
            }
            IncidentDTO dto = new IncidentDTO();
            dto.setId(incident.getId());
            dto.setTitle(incident.getTitle());
            dto.setDescription(incident.getDescription());
            dto.setImageUrl(incident.getImageUrl());
            dto.setIncidentDate(incident.getIncidentDate());
            dto.setLatitude(incident.getLatitude());
            dto.setLongitude(incident.getLongitude());
            dto.setLikes(incident.getLikes());
            dto.setIncidentType(incident.getIncidentType() != null ? incident.getIncidentType().getName() : null);
            if (incident.getPhotos() != null) {
                List<String> photoUrls = incident.getPhotos().stream()
                        .map(IncidentPhoto::getPhotoUrl)
                        .collect(Collectors.toList());
                dto.setPhotoUrls(photoUrls);
            }
            return Response.ok(dto).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error interno del servidor: " + e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response deleteIncident(@PathParam("id") Long id) {
        try {
            Incident incident = em.find(Incident.class, id);
            if (incident == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Incidència no trobada.")
                        .build();
            }
            em.remove(incident);
            return Response.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en eliminar la incidència: " + e.getMessage())
                    .build();
        }
    }

    @POST
    public Response createIncident(Incident incident, @Context UriInfo uriInfo) {
        try {
            // Si no se proporcionó fecha, usar la fecha actual
            if (incident.getIncidentDate() == null) {
                incident.setIncidentDate(new Date());
            }
            // Inicializa likes en 0 si no se indica
            if (incident.getLikes() < 0) {
                incident.setLikes(0);
            }
            em.persist(incident);
            em.flush();
            URI uri = uriInfo.getAbsolutePathBuilder().path(Long.toString(incident.getId())).build();
            return Response.created(uri)
                    .entity(incident.getId().toString())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en crear la incidència: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("{id}")
    public Response updateIncident(@PathParam("id") Long id, Incident updatedIncident) {
        try {
            Incident incident = em.find(Incident.class, id);
            if (incident == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Incidència no trobada.")
                        .build();
            }
            incident.setTitle(updatedIncident.getTitle());
            incident.setDescription(updatedIncident.getDescription());
            incident.setImageUrl(updatedIncident.getImageUrl());
            incident.setLatitude(updatedIncident.getLatitude());
            incident.setLongitude(updatedIncident.getLongitude());
            incident.setIncidentDate(updatedIncident.getIncidentDate());
            // Opcional: actualizar likes, tipo y fotos si se envían
            incident.setLikes(updatedIncident.getLikes());
            incident.setIncidentType(updatedIncident.getIncidentType());
            incident.setPhotos(updatedIncident.getPhotos());
            em.merge(incident);

            IncidentDTO dto = new IncidentDTO();
            dto.setId(incident.getId());
            dto.setTitle(incident.getTitle());
            dto.setDescription(incident.getDescription());
            dto.setImageUrl(incident.getImageUrl());
            dto.setIncidentDate(incident.getIncidentDate());
            dto.setLatitude(incident.getLatitude());
            dto.setLongitude(incident.getLongitude());
            dto.setLikes(incident.getLikes());
            dto.setIncidentType(incident.getIncidentType() != null ? incident.getIncidentType().getName() : null);
            if (incident.getPhotos() != null) {
                List<String> photoUrls = incident.getPhotos().stream()
                        .map(photo -> photo.getPhotoUrl())
                        .collect(Collectors.toList());
                dto.setPhotoUrls(photoUrls);
            }
            return Response.ok(dto).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en actualitzar la incidència: " + e.getMessage())
                    .build();
        }
    }

    // Endpoint para incrementar likes (ejemplo de acción "like")
    @POST
    @Path("{id}/like")
    public Response likeIncident(@PathParam("id") Long id) {
        try {
            Incident incident = em.find(Incident.class, id);
            if (incident == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Incidència no trobada.")
                        .build();
            }
            incident.setLikes(incident.getLikes() + 1);
            em.merge(incident);
            // Devolver el nuevo total de likes en JSON
            JsonObject json = Json.createObjectBuilder()
                    .add("likes", incident.getLikes())
                    .build();
            return Response.ok(json).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en incrementar likes: " + e.getMessage())
                    .build();
        }
    }

    // Endpoint para validar la incidencia (recibe foto y fecha de validación)
    @POST
    @Path("{id}/validate")
    public Response validateIncident(@PathParam("id") Long id) {
        try {
            // Aquí deberías implementar la lógica para recibir el FormData (foto y fecha)
            // y asociarla a la incidencia. Por simplicidad, devolveremos un mensaje.
            // La implementación real requerirá procesar el InputStream del Part.
            return Response.ok(Json.createObjectBuilder()
                    .add("message", "Validación recibida correctamente")
                    .build()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en validar la incidencia: " + e.getMessage())
                    .build();
        }
    }
}
