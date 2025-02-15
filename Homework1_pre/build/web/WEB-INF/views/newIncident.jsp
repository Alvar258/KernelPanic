<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Nueva Incidencia</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
  <script>
    // Función para obtener la dirección desde latitud y longitud
    function reverseGeocode(lat, lon) {
      const url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lon;
      fetch(url)
        .then(response => response.json())
        .then(data => {
          if (data && data.display_name) {
            document.getElementById('street').value = data.display_name;
            document.getElementById('geoMessage').textContent = "Dirección autocompletada.";
          } else {
            document.getElementById('geoMessage').textContent = "No se encontró la dirección.";
          }
        })
        .catch(error => {
          console.error("Error en reverse geocoding:", error);
          document.getElementById('geoMessage').textContent = "Error al obtener la dirección.";
        });
    }

    // Al cargar la página, obtener los parámetros lat y lon
    window.onload = function() {
      const urlParams = new URLSearchParams(window.location.search);
      const lat = urlParams.get('lat');
      const lon = urlParams.get('lon');
      if (lat && lon) {
        document.getElementById('latitude').value = lat;
        document.getElementById('longitude').value = lon;
        reverseGeocode(lat, lon);
      }
    }
  </script>
</head>
<body>
  <jsp:include page="/WEB-INF/jspf/header.jsp" />
  <div class="new-incident-container">
    <h1>Nueva incidencia</h1>
    <form method="post" action="${pageContext.request.contextPath}/IncidentController?action=create" enctype="multipart/form-data">
      <input type="hidden" id="tipo" name="tipo" value="incidencia">
      
      <!-- Dirección (autocompletada) -->
      <div class="form-group">
        <label for="street"><i class="fas fa-map-marker-alt"></i> Calle en Tarragona</label>
        <input type="text" id="street" name="street" placeholder="Dirección autocompletada por el mapa" required>
        <div id="geoMessage"></div>
      </div>
      
      <!-- Campos ocultos de latitud y longitud -->
      <input type="hidden" id="latitude" name="latitude">
      <input type="hidden" id="longitude" name="longitude">
      
      <!-- Descripción -->
      <div class="form-group">
        <label for="description">Descripción</label>
        <textarea id="description" name="description" required></textarea>
      </div>

      <!-- Botón para enviar -->
      <div class="form-group">
        <input type="submit" value="Añadir incidencia">
      </div>
    </form>
  </div>
  <footer>
    <div>Hackathon Cloud Computing</div>
  </footer>
</body>
</html>
