package client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonObjectBuilder;

public class IncidentRestClient {

    private static final String BASE_URL = "http://localhost:8080/Homework1_pre/rest/api/v1/incident";

    // Obtener lista de incidencias
    public String getIncidents(String jwtToken) throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (jwtToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }
        int status = conn.getResponseCode();
        if (status == 200) {
            InputStream is = conn.getInputStream();
            JsonReader reader = Json.createReader(is);
            JsonArray jsonArray = reader.readArray();
            StringWriter sw = new StringWriter();
            JsonWriter jsonWriter = Json.createWriter(sw);
            jsonWriter.writeArray(jsonArray);
            jsonWriter.close();
            reader.close();
            is.close();
            return sw.toString();
        } else {
            throw new Exception("HTTP " + status + " - " + conn.getResponseMessage());
        }
    }

    // Obtener incidencia por ID
    public String getIncidentById(Long id, String jwtToken) throws Exception {
        URL url = new URL(BASE_URL + "/" + id);
        System.out.println("GET Incident by ID URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (jwtToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }
        int status = conn.getResponseCode();
        System.out.println("GET Incident by ID Status: " + status);
        if (status == 200) {
            InputStream is = conn.getInputStream();
            JsonReader reader = Json.createReader(is);
            JsonObject jsonObject = reader.readObject();
            StringWriter sw = new StringWriter();
            JsonWriter jsonWriter = Json.createWriter(sw);
            jsonWriter.writeObject(jsonObject);
            jsonWriter.close();
            reader.close();
            is.close();
            String jsonResponse = sw.toString();
            System.out.println("GET Incident by ID Response: " + jsonResponse);
            return jsonResponse;
        } else {
            InputStream is = conn.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sbErr = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sbErr.append(line);
            }
            br.close();
            is.close();
            throw new Exception("HTTP " + status + " - " + sbErr.toString());
        }
    }

    // Crear incidencia
    public Long createIncident(String title, String description, String imageUrl, 
                               double latitude, double longitude, String jwtToken) throws Exception {
        URL url = new URL(BASE_URL);
        System.out.println("POST Create Incident URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        if (jwtToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }

        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
                .add("title", title)
                .add("description", description)
                .add("imageUrl", imageUrl)
                .add("latitude", latitude)
                .add("longitude", longitude);
        JsonObject json = jsonBuilder.build();
        System.out.println("POST Create Incident JSON: " + json.toString());

        OutputStream os = null;
        JsonWriter writer = null;
        try {
            os = conn.getOutputStream();
            writer = Json.createWriter(os);
            writer.writeObject(json);
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (os != null) {
                os.close();
            }
        }

        int status = conn.getResponseCode();
        System.out.println("POST Create Incident Status: " + status);
        if (status == 201) {
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String idStr = br.readLine();
            br.close();
            is.close();
            System.out.println("POST Create Incident ID: " + idStr);
            return Long.valueOf(idStr);
        } else {
            InputStream is = conn.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sbErr = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sbErr.append(line);
            }
            br.close();
            is.close();
            throw new Exception("HTTP " + status + " - " + sbErr.toString());
        }
    }

    // Eliminar incidencia
    public void deleteIncident(Long id, String jwtToken) throws Exception {
        URL url = new URL(BASE_URL + "/" + id);
        System.out.println("DELETE Incident URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        if (jwtToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }
        int status = conn.getResponseCode();
        System.out.println("DELETE Incident Status: " + status);
        if (status == 204) {
            return;
        } else {
            InputStream is = conn.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sbErr = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sbErr.append(line);
            }
            br.close();
            is.close();
            throw new Exception("HTTP " + status + " - " + sbErr.toString());
        }
    }

    // Actualizar incidencia
    public void updateIncident(Long id, String title, String description,
                               String imageUrl, double latitude, double longitude,
                               String jwtToken) throws Exception {
        URL url = new URL(BASE_URL + "/" + id);
        System.out.println("PUT Update Incident URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        if (jwtToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder()
                .add("title", title)
                .add("description", description)
                .add("imageUrl", imageUrl)
                .add("latitude", latitude)
                .add("longitude", longitude);
        JsonObject json = jsonBuilder.build();
        System.out.println("PUT Update Incident JSON: " + json.toString());
        OutputStream os = null;
        JsonWriter writer = null;
        try {
            os = conn.getOutputStream();
            writer = Json.createWriter(os);
            writer.writeObject(json);
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (os != null) {
                os.close();
            }
        }
        int status = conn.getResponseCode();
        System.out.println("PUT Update Incident Status: " + status);
        if (status != 200) {
            InputStream is = conn.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sbErr = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sbErr.append(line);
            }
            br.close();
            is.close();
            throw new Exception("HTTP " + status + " - " + sbErr.toString());
        }
    }

    // Enviar like para una incidencia
    public String likeIncident(Long id, String jwtToken) throws Exception {
        URL url = new URL(BASE_URL + "/" + id + "/like");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        if (jwtToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }
        int status = conn.getResponseCode();
        if (status == 200) {
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String result = br.readLine();
            br.close();
            is.close();
            return result; // Se asume que devuelve el nuevo total de likes en JSON
        } else {
            throw new Exception("HTTP " + status + " - " + conn.getResponseMessage());
        }
    }

    // Enviar validación (foto y fecha) para una incidencia
    public String validateIncident(Long id, byte[] photoBytes, String photoDate, String jwtToken) throws Exception {
        URL url = new URL(BASE_URL + "/" + id + "/validate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        if (jwtToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }
        String photoBase64 = java.util.Base64.getEncoder().encodeToString(photoBytes);
        JsonObject json = Json.createObjectBuilder()
                .add("photo", photoBase64)
                .add("photoDate", photoDate)
                .build();
        OutputStream os = null;
        JsonWriter writer = null;
        try {
            os = conn.getOutputStream();
            writer = Json.createWriter(os);
            writer.writeObject(json);
        } finally {
            if (writer != null) { writer.close(); }
            if (os != null) { os.close(); }
        }
        int status = conn.getResponseCode();
        if (status == 200) {
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String result = br.readLine();
            br.close();
            is.close();
            return result;
        } else {
            throw new Exception("HTTP " + status + " - " + conn.getResponseMessage());
        }
    }

    // Utilidad para codificar valores URL
    private String encodeValue(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            return value;
        }
    }
}
