package controller;

import client.IncidentRestClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import model.entities.IncidentDTO;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;

@WebServlet("/IncidentController")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 15
)
public class IncidentController extends HttpServlet {

    private IncidentRestClient incidentClient = new IncidentRestClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }
        switch (action) {
            case "edit":
                showEditForm(request, response);
                break;
            case "detail":
                showDetail(request, response);
                break;
            case "new":
                showNewForm(request, response);
                break;
            case "view":
                showViewForm(request, response);
                break;
            case "getMapData":
                getMapData(request, response);
                break;
            case "delete":
                deleteIncident(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/RootController");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/RootController");
            return;
        }
        switch (action) {
            case "create":
                createIncident(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/RootController");
        }
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String incidentIdStr = request.getParameter("id");
        if (incidentIdStr == null || incidentIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/RootController");
            return;
        }
        try {
            Long incidentId = Long.parseLong(incidentIdStr);
            HttpSession session = request.getSession(false);
            String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;
            String rawJson = incidentClient.getIncidentById(incidentId, jwtToken);
            try (JsonReader reader = Json.createReader(new StringReader(rawJson))) {
                JsonObject obj = reader.readObject();
                IncidentDTO incident = IncidentControllerHelper.parseIncidentFromJson(obj);
                if (incident != null) {
                    request.setAttribute("incident", incident);
                    request.getRequestDispatcher("/WEB-INF/views/detailIncident.jsp").forward(request, response);
                } else {
                    throw new Exception("No se pudo cargar la incidencia.");
                }
            }
        } catch (Exception e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/newIncident.jsp").forward(request, response);
    }

    private void showViewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;
        try {
            String incidentsJson = incidentClient.getIncidents(jwtToken);
            List<IncidentDTO> incidents = IncidentControllerHelper.parseIncidentsJson(incidentsJson);
            request.setAttribute("incidencias", incidents);
            request.setAttribute("currentIncidenciasPage", 1);
            request.setAttribute("incidenciasTotalPages", 1);
            request.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error obteniendo incidencias: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
    
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/editIncident.jsp").forward(request, response);
    }   

    private void getMapData(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;
        try {
            String incidentsJson = incidentClient.getIncidents(jwtToken);
            response.setContentType("application/json");
            response.getWriter().write(incidentsJson);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void createIncident(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    try {
        // Obtener parámetros
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String imageUrl = request.getParameter("imageUrl"); // No obligatorio
        String latStr = request.getParameter("latitude");
        String lngStr = request.getParameter("longitude");

        if (title == null || title.trim().isEmpty()) {
            throw new Exception("El título es requerido");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new Exception("La descripción es requerida");
        }

        // Coordenadas
        if (latStr == null || lngStr == null) {
            throw new Exception("Las coordenadas son requeridas");
        }

        double latitude, longitude;
        try {
            latitude = Double.parseDouble(latStr);
            longitude = Double.parseDouble(lngStr);
        } catch (NumberFormatException nfe) {
            throw new Exception("Coordenadas inválidas");
        }

        // Obtener token JWT de sesión
        HttpSession session = request.getSession(false);
        String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;

        // Llamada al cliente REST para crear la incidencia
        String responseJson = incidentClient.createIncident(title.trim(), description.trim(), imageUrl, latitude, longitude, jwtToken);

        // Comprobar si la creación fue exitosa
        if (responseJson != null && !responseJson.isEmpty()) {
            // Redirigir al usuario a la página principal después de crear la incidencia
            response.sendRedirect(request.getContextPath() + "/RootController");
        } else {
            throw new Exception("Error al crear la incidencia: no se recibió respuesta válida del servidor");
        }
    } catch (Exception e) {
        // Si hubo un error, mostrar mensaje y volver a la página de creación
        request.setAttribute("errorMessage", "Error creando la incidencia: " + e.getMessage());
        request.getRequestDispatcher("/WEB-INF/views/newIncident.jsp").forward(request, response);
    }
}

    
    // Método auxiliar para escapar caracteres especiales en JSON
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }

    private void deleteIncident(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/RootController");
            return;
        }
        try {
            Long incidentId = Long.valueOf(idStr);
            HttpSession session = request.getSession(false);
            String jwtToken = (session != null) ? (String) session.getAttribute("jwtToken") : null;
            incidentClient.deleteIncident(incidentId, jwtToken);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error al eliminar la incidencia: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }
        doGet(request, response);
    }
}
