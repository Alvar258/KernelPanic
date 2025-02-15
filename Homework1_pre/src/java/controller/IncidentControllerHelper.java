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

    public static IncidentDTO parseIncidentFromJson(JsonObject obj) {
        IncidentDTO dto = new IncidentDTO();
        try {
            if (obj.containsKey("id") && !obj.isNull("id")) {
                dto.setId(obj.getJsonNumber("id").longValue());
            }
            // Si no existe "title", usamos "street" como título
            String title = obj.containsKey("title") && !obj.isNull("title") 
                    ? obj.getString("title") 
                    : (obj.containsKey("street") && !obj.isNull("street") ? obj.getString("street") : "");
            dto.setTitle(title);
            
            dto.setDescription(obj.getString("description", ""));
            dto.setImageUrl("");
            
            // Usamos "dateInitial" en lugar de "incidentDate"
            if (obj.containsKey("dateInitial") && !obj.isNull("dateInitial")) {
                try {
                    String dateStr = obj.getString("dateInitial");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    dto.setIncidentDate(sdf.parse(dateStr));
                } catch (Exception e) {
                    System.err.println("Error parseando dateInitial: " + e.getMessage());
                    dto.setIncidentDate(new Date());
                }
            } else {
                dto.setIncidentDate(new Date());
            }
            
            // Usamos los campos "x" y "y" para latitude y longitude
            if (obj.containsKey("x") && !obj.isNull("x")) {
                dto.setLatitude(obj.getJsonNumber("x").doubleValue());
            }
            if (obj.containsKey("y") && !obj.isNull("y")) {
                dto.setLongitude(obj.getJsonNumber("y").doubleValue());
            }
        } catch (Exception e) {
            System.err.println("Error en parseIncidentFromJson: " + e.getMessage());
        }
        return dto;
    }

    public static List<IncidentDTO> parseIncidentsJson(String rawJson) {
        List<IncidentDTO> incidents = new ArrayList<>();
        try (JsonReader reader = Json.createReader(new StringReader(rawJson))) {
            JsonArray array = reader.readArray();
            for (int i = 0; i < array.size(); i++) {
                JsonObject obj = array.getJsonObject(i);
                IncidentDTO dto = parseIncidentFromJson(obj);
                incidents.add(dto);
            }
        } catch (Exception e) {
            System.err.println("Error al parsear JSON de incidencias: " + e.getMessage());
            e.printStackTrace();
        }
        return incidents;
    }
}
