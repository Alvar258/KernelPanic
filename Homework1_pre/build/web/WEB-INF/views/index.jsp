<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.entities.IncidentDTO" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Incidencias - Portal</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css"/>
    <link rel="stylesheet" href="https://unpkg.com/leaflet/dist/leaflet.css" />
    <script src="${pageContext.request.contextPath}/resources/js/app.js" defer></script>
    <script>
        window.contextPath = "${pageContext.request.contextPath}";
    </script>
    <script>
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
        .main-container { display: flex; gap: 20px; padding: 20px 40px; }
        .map-container { flex: 2; height: 500px; }
        .side-panel { flex: 1; display: flex; flex-direction: column; gap: 20px; }
        .filter-top-buttons { display: flex; justify-content: center; gap: 20px; }
        .filter-container { padding: 20px; background-color: #fff; border: 1px solid #ddd; border-radius: 8px; height: 500px; overflow-y: auto; }
        .btn-floating { background-color: #007aff; color: #fff; border: none; padding: 15px 20px; font-size: 20px; border-radius: 50%; cursor: pointer; transition: background-color 0.3s ease, transform 0.3s ease; box-shadow: 0 4px 8px rgba(0,0,0,0.2); }
        .btn-floating:hover { background-color: #005bb5; transform: scale(1.05); }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jspf/header.jsp" />
    <div class="main-container">
        <div class="map-container">
            <div id="map" style="height: 100%; width: 100%;"></div>
        </div>
        <div class="side-panel">
            <div class="filter-top-buttons">
                <button class="btn-floating" onclick="window.location.href='${pageContext.request.contextPath}/IncidentController?action=edit'">
                    <i class="fas fa-pencil-alt"></i>
                </button>
                <button class="btn-floating" onclick="window.location.href='${pageContext.request.contextPath}/IncidentController?action=new'">
                    <i class="fas fa-plus"></i>
                </button>
                <button class="btn-floating" onclick="window.location.href='${pageContext.request.contextPath}/IncidentController?action=view'">
                    <i class="fas fa-eye"></i>
                </button>
            </div>
            <div class="filter-container">
                <h2>Filtros</h2>
                <form method="get" action="${pageContext.request.contextPath}/RootController">
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
                    <div>
                        <label for="category">Categoría:</label>
                        <select id="category" name="category" onchange="toggleIncidentTypeFilter(this.value)">
                            <option value="">Todas</option>
                            <option value="sugerencia">Sugerencias</option>
                            <option value="incidencia">Incidencias</option>
                        </select>
                    </div>
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
    <script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>
    <script src="${pageContext.request.contextPath}/resources/js/map.js"></script>
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
