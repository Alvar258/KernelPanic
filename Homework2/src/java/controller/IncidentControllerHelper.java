package controller;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.entities.IncidentDTO;

public class IncidentControllerHelper {

    public static List<IncidentDTO> parseIncidentsJson(String rawJson) {
        List<IncidentDTO> incidents = new ArrayList<>();
        try (JsonReader reader = Json.createReader(new StringReader(rawJson))) {
            JsonArray array = reader.readArray();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            for (int i = 0; i < array.size(); i++) {
                JsonObject obj = array.getJsonObject(i);
                IncidentDTO dto = new IncidentDTO();
                dto.setId(obj.getJsonNumber("id").longValue());
                dto.setTitle(obj.getString("title", ""));
                dto.setDescription(obj.getString("description", ""));
                dto.setImageUrl(obj.getString("imageUrl", ""));
                if (obj.containsKey("incidentDate") && !obj.isNull("incidentDate")) {
                    try {
                        String dateStr = obj.getString("incidentDate");
                        dto.setIncidentDate(sdf.parse(dateStr));
                    } catch (Exception e) {
                        System.err.println("Error parseando fecha: " + e.getMessage());
                        dto.setIncidentDate(new Date());
                    }
                } else {
                    dto.setIncidentDate(new Date());
                }
                dto.setLatitude(obj.getJsonNumber("latitude").doubleValue());
                dto.setLongitude(obj.getJsonNumber("longitude").doubleValue());
                incidents.add(dto);
            }
        } catch (Exception e) {
            System.err.println("Error al parsear JSON de incidencias: " + e.getMessage());
            e.printStackTrace();
        }
        return incidents;
    }
}
