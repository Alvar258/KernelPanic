package controller;

import client.IncidentRestClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.entities.IncidentDTO;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RootController extends HttpServlet {

    private IncidentRestClient incidentClient = new IncidentRestClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;

        try {
            String incidentsJson = incidentClient.getIncidents(jwtToken);
            System.out.println("=== Incidents Request Debug ===");
            System.out.println("JWT Token present: " + (jwtToken != null));
            System.out.println("Raw JSON received: " + incidentsJson);

            List<IncidentDTO> incidents = parseIncidentsJson(incidentsJson);
            System.out.println("Número de incidencias obtenidas: " + incidents.size());

            request.setAttribute("incidents", incidents);
            request.getRequestDispatcher("/WEB-INF/views/incidents.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("Error en RootController: " + e.getMessage());
            request.setAttribute("errorMessage", "Error obteniendo incidencias: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private List<IncidentDTO> parseIncidentsJson(String rawJson) {
        Map<Long, IncidentDTO> uniqueIncidents = new LinkedHashMap<>();
        try (JsonReader reader = Json.createReader(new StringReader(rawJson))) {
            JsonArray jsonArray = reader.readArray();
            System.out.println("Parsing " + jsonArray.size() + " incidencias from JSON");
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject obj = jsonArray.getJsonObject(i);
                Long id = obj.getJsonNumber("id").longValue();
                if (!uniqueIncidents.containsKey(id)) {
                    IncidentDTO dto = new IncidentDTO();
                    dto.setId(id);
                    dto.setTitle(obj.getString("title", ""));
                    dto.setDescription(obj.getString("description", ""));
                    dto.setImageUrl(obj.getString("imageUrl", ""));
                    if (obj.containsKey("incidentDate") && !obj.isNull("incidentDate")) {
                        try {
                            String dateStr = obj.getString("incidentDate");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            dto.setIncidentDate(sdf.parse(dateStr));
                        } catch (Exception e) {
                            System.err.println("Error parsing incidentDate: " + e.getMessage());
                            dto.setIncidentDate(new Date());
                        }
                    } else {
                        dto.setIncidentDate(new Date());
                    }
                    dto.setLatitude(obj.getJsonNumber("latitude").doubleValue());
                    dto.setLongitude(obj.getJsonNumber("longitude").doubleValue());
                    uniqueIncidents.put(id, dto);
                    System.out.println("Added incident: " + id + " - " + dto.getTitle());
                } else {
                    System.out.println("Skipping duplicate incident with ID: " + id);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing incidents JSON: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>(uniqueIncidents.values());
    }
}
