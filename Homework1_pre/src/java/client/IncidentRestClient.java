package client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class IncidentRestClient {
    
    // Ajusta BASE_URL según el contexto de tu aplicación
    private static final String BASE_URL = "http://localhost:8080/MyApp/rest/api/v1/incidencia";
    
    private Client client;
    
    public IncidentRestClient() {
        client = ClientBuilder.newClient();
    }
    
    public String getIncidents(String jwtToken) {
        WebTarget target = client.target(BASE_URL);
        Response response = target.request(MediaType.APPLICATION_JSON)
                .header("Authorization", jwtToken != null ? "Bearer " + jwtToken : "")
                .get();
        String json = response.readEntity(String.class);
        response.close();
        return json;
    }
    
    public String getIncidentById(Long id, String jwtToken) {
        WebTarget target = client.target(BASE_URL).path(String.valueOf(id));
        Response response = target.request(MediaType.APPLICATION_JSON)
                .header("Authorization", jwtToken != null ? "Bearer " + jwtToken : "")
                .get();
        String json = response.readEntity(String.class);
        response.close();
        return json;
    }
    
    // Método para enviar el JSON completo (payload)
   public String createIncident(String title, String description, String imageUrl, double latitude, double longitude, String jwtToken) {
    // Crear el cuerpo del POST para la creación de la incidencia
    String payload = "{\"title\":\"" + title + "\","
            + "\"description\":\"" + description + "\","
            + "\"imageUrl\":\"" + imageUrl + "\","
            + "\"latitude\":" + latitude + ","
            + "\"longitude\":" + longitude + "}";

    // Hacer la solicitud POST al backend
    WebTarget target = client.target(BASE_URL);
    Response response = target.request(MediaType.APPLICATION_JSON)
            .header("Authorization", jwtToken != null ? "Bearer " + jwtToken : "")
            .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

    // Leer la respuesta del servidor
    String jsonResponse = response.readEntity(String.class);
    response.close();

    // Se devuelve el JSON de la respuesta, que puede incluir el ID o algún mensaje
    return jsonResponse;
}

    
    public void deleteIncident(Long id, String jwtToken) {
        WebTarget target = client.target(BASE_URL).path(String.valueOf(id));
        Response response = target.request(MediaType.APPLICATION_JSON)
                .header("Authorization", jwtToken != null ? "Bearer " + jwtToken : "")
                .delete();
        response.close();
    }
}
