<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.entities.IncidentDTO" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Incidencias</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- Vincular el CSS -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/style.css"/>
    <!-- Iconos -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css"/>
    <!-- Leaflet CSS -->
    <link rel="stylesheet" href="https://unpkg.com/leaflet/dist/leaflet.css" />
    <script src="<%= request.getContextPath() %>/resources/js/app.js" defer></script>
    <script>
        window.contextPath = "<%= request.getContextPath() %>";
    </script>  
    <script>
        // Función para mostrar/ocultar el filtro de tipo de incidencia según la opción seleccionada
        function toggleIncidentTypeFilter(value) {
            var incidentTypeDiv = document.getElementById('incidentTypeFilter');
            if(value === 'incidencia'){
                incidentTypeDiv.style.display = 'block';
            } else {
                incidentTypeDiv.style.display = 'none';
            }
        }
    </script>    
    <style>
        /* Contenedor principal */
        .main-container {
            display: flex;
            gap: 20px;
            padding: 20px 40px;
        }
        .map-container {
            flex: 2;
            height: 500px;
        }
        /* Contenedor lateral que agrupa botones y filtros */
        .side-panel {
            flex: 1;
            display: flex;
            flex-direction: column;
            gap: 20px;
        }
        /* Contenedor de los botones superiores */
        .filter-top-buttons {
            display: flex;
            justify-content: center;
            gap: 20px;
        }
        /* Panel de filtros */
        .filter-container {
            padding: 20px;
            background-color: #ffffff;
            border: 1px solid #ddd;
            border-radius: 8px;
            height: 500px;
            overflow-y: auto;
        }
        /* Botones flotantes reutilizados (ahora en el lateral) */
        .btn-floating {
            background-color: #007aff;
            color: #fff;
            border: none;
            padding: 15px 20px;
            font-size: 20px;
            border-radius: 50%;
            cursor: pointer;
            transition: background-color 0.3s ease, transform 0.3s ease;
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        }
        .btn-floating:hover {
            background-color: #005bb5;
            transform: scale(1.05);
        }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jspf/header.jsp" />
    <div class="main-container">
        <!-- Contenedor del mapa -->
        <div class="map-container">
            <div id="map" style="height: 100%; width: 100%;"></div>
        </div>
        <!-- Contenedor lateral: botones arriba y filtros debajo -->
        <div class="side-panel">
            <!-- Botones en la parte superior del panel lateral -->
            <div class="filter-top-buttons">
                <button class="btn-floating" onclick="window.location.href='<%= request.getContextPath() %>/IncidentController?action=edit'">
                    <i class="fas fa-pencil-alt"></i>
                </button>
                <button class="btn-floating" onclick="window.location.href='<%= request.getContextPath() %>/IncidentController?action=new'">
                    <i class="fas fa-plus"></i>
                </button>
                <button class="btn-floating" onclick="window.location.href='<%= request.getContextPath() %>/IncidentController?action=view'">
                    <i class="fas fa-eye"></i>
                </button>
            </div>
            <!-- Panel de filtros -->
            <div class="filter-container">
                <h2>Filtros</h2>
                <form method="get" action="<%= request.getContextPath() %>/RootController">
                    <div>
                        <label for="dateFrom">Desde:</label>
                        <input type="date" id="dateFrom" name="dateFrom">
                    </div>
                    <div>
                        <label for="dateTo">Hasta:</label>
                        <input type="date" id="dateTo" name="dateTo">
                    </div>
                    <div>
                        <label for="search">Buscar:</label>
                        <input type="text" id="search" name="search" placeholder="Palabra clave">
                    </div>
                    <!-- Filtro para seleccionar sugerencias o incidencias -->
                    <div>
                        <label for="category">Categoría:</label>
                        <select id="category" name="category" onchange="toggleIncidentTypeFilter(this.value)">
                            <option value="">Todas</option>
                            <option value="sugerencia">Sugerencias</option>
                            <option value="incidencia">Incidencias</option>
                        </select>
                    </div>
                    <!-- Filtro para tipo de incidencia, visible solo si se selecciona "incidencia" -->
                    <div id="incidentTypeFilter" style="display: none;">
                        <label for="incidentType">Tipo de Incidencia:</label>
                        <select id="incidentType" name="incidentType">
                            <option value="">Todos</option>
                            <option value="infraestructura">Infraestructura</option>
                            <option value="vial">Vial</option>
                            <option value="limpieza">Limpieza</option>
                        </select>
                    </div>
                    <div>
                        <button type="submit" class="btn-primary">Aplicar Filtros</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    
    <!-- Incluir scripts de Leaflet y el script para el mapa -->
    <script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>
    <script src="<%= request.getContextPath() %>/resources/js/map.js"></script>

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
