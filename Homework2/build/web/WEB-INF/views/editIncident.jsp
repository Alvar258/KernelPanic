<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Incidència - Vigilante Tarragona</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/style.css"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css"/>
    <style>
        /* Estilos para el formulario de edición */
        .incident-form-container {
            max-width: 800px;
            margin: 20px auto;
            padding: 2rem;
            background: #fff;
            border-radius: 10px;
            box-shadow: 0 0 20px rgba(0,0,0,0.1);
        }
        .form-title {
            color: #2c3e50;
            margin-bottom: 1.5rem;
            text-align: center;
        }
        .incident-form .form-group {
            margin-bottom: 15px;
        }
        .incident-form label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #555;
        }
        .incident-form input[type="text"],
        .incident-form textarea {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        .incident-form .form-actions {
            display: flex;
            justify-content: space-between;
            margin-top: 20px;
        }
        .btn-primary, .btn-secondary {
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            font-weight: bold;
            cursor: pointer;
            text-decoration: none;
        }
        .btn-primary {
            background-color: #007aff;
            color: #fff;
        }
        .btn-secondary {
            background-color: #6c757d;
            color: #fff;
        }
        
        /* Estilos para el menú de actualización de estados */
        .state-update-container {
            max-width: 1000px;
            margin: 40px auto;
            padding: 1rem 2rem;
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .state-update-container h2 {
            text-align: center;
            color: #007aff;
            margin-bottom: 20px;
        }
        .state-table {
            width: 100%;
            border-collapse: collapse;
        }
        .state-table th, .state-table td {
            padding: 10px;
            border: 1px solid #ddd;
            text-align: center;
        }
        .state-table th {
            background-color: #e0e0e0;
            color: #333;
        }
        .state-table td input[type="radio"] {
            cursor: pointer;
        }
        .form-actions {
            text-align: center;
            margin-top: 20px;
        }
        
        /* Footer (se asume que el CSS global ya tiene estilos para .footer, .footer-content, etc.) */
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jspf/header.jsp" />
    
    <div class="container">
        <!-- Formulario para editar incidencia -->
        <div class="incident-form-container">
            <h1 class="form-title"><i class="fas fa-edit"></i> Editar Incidencia</h1>
            <%
                model.entities.IncidentDTO incident = (model.entities.IncidentDTO) request.getAttribute("incident");
            %>
            <form id="incidentForm" method="post" action="<%= request.getContextPath() %>/IncidentController?action=update" class="incident-form">
                <input type="hidden" name="id" value="<%= incident.getId() %>">
                <div class="form-group">
                    <label for="title"><i class="fas fa-heading"></i> Título</label>
                    <input type="text" id="title" name="title" value="<%= incident.getTitle() %>" required>
                </div>
                <div class="form-group">
                    <label for="description"><i class="fas fa-align-left"></i> Descripción</label>
                    <textarea id="description" name="description" rows="10" required><%= incident.getDescription() %></textarea>
                </div>
                <div class="form-group">
                    <label for="imageUrl"><i class="fas fa-image"></i> URL de la Imagen</label>
                    <input type="text" id="imageUrl" name="imageUrl" value="<%= incident.getImageUrl() %>" required>
                </div>
                <div class="form-group">
                    <label for="latitude"><i class="fas fa-map-marker-alt"></i> Latitud</label>
                    <input type="text" id="latitude" name="latitude" value="<%= incident.getLatitude() %>" required>
                </div>
                <div class="form-group">
                    <label for="longitude"><i class="fas fa-map-marker-alt"></i> Longitud</label>
                    <input type="text" id="longitude" name="longitude" value="<%= incident.getLongitude() %>" required>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn-primary" id="submitButton">
                        <i class="fas fa-save"></i> Guardar Cambios
                    </button>
                    <a href="<%= request.getContextPath() %>/IncidentController?action=detail&id=<%= incident.getId() %>" class="btn-secondary">
                        <i class="fas fa-times"></i> Cancelar
                    </a>
                </div>
            </form>
        </div>
        
        <!-- Menú de actualización de estados por Municipio e Importancia -->
        <div class="state-update-container">
            <h2>Actualizar Estado de Incidencias</h2>
            <form id="stateForm" method="post" action="<%= request.getContextPath() %>/IncidentController?action=updateStatus">
                <table class="state-table">
                    <thead>
                        <tr>
                            <th>Municipio</th>
                            <th>Importancia</th>
                            <th>Reporte</th>
                            <th>En Proceso</th>
                            <th>Finalizado</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="inc" items="${incidents}">
                            <tr>
                                <td>${inc.municipio}</td>
                                <td>${inc.importancia}</td>
                                <td>
                                    <input type="radio" name="estado_${inc.id}" value="reporte" 
                                    <c:if test="${inc.estado == 'reporte'}">checked</c:if>>
                                </td>
                                <td>
                                    <input type="radio" name="estado_${inc.id}" value="proceso" 
                                    <c:if test="${inc.estado == 'proceso'}">checked</c:if>>
                                </td>
                                <td>
                                    <input type="radio" name="estado_${inc.id}" value="finalizado" 
                                    <c:if test="${inc.estado == 'finalizado'}">checked</c:if>>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                <div class="form-actions">
                    <button type="submit" class="btn-primary">
                        <i class="fas fa-save"></i> Guardar Estados
                    </button>
                </div>
            </form>
        </div>
    </div>
    
    <!-- Footer -->
    <footer class="footer">
        <div class="footer-content">
            <p>Hackathon Cloud Computing</p>
            <ul class="footer-team">
                <li>Elena Díez, Álvaro Lucas, Àitor Olivares, Marina Oteiza</li>
            </ul>
        </div>
    </footer>
</body>
</html>
