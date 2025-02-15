<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Nueva Incidencia / Sugerencia</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/style.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
  <script>
    // Función para geocodificación inversa: obtener la dirección a partir de lat, lon
    function reverseGeocode(lat, lon) {
      const url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" 
                + lat + "&lon=" + lon;
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
          console.error("Error en geocodificación inversa:", error);
          document.getElementById('geoMessage').textContent = "Error al obtener la dirección.";
        });
    }

    // Función para geocodificar manualmente la dirección ingresada
    function geocodeAddress() {
      const street = document.getElementById('street').value;
      if (street.trim() === "") return;
      const url = "https://nominatim.openstreetmap.org/search?format=json&q=" 
                + encodeURIComponent(street + ", Tarragona, España");
      fetch(url)
        .then(response => response.json())
        .then(data => {
          if (data && data.length > 0) {
            document.getElementById('latitude').value = data[0].lat;
            document.getElementById('longitude').value = data[0].lon;
            document.getElementById('geoMessage').textContent = "Ubicación encontrada: " + data[0].display_name;
          } else {
            document.getElementById('geoMessage').textContent = "No se encontró la ubicación.";
          }
        })
        .catch(error => {
          console.error("Error al geocodificar:", error);
          document.getElementById('geoMessage').textContent = "Error al buscar la ubicación.";
        });
    }

    // Función para manejar el cambio de pestaña
    function selectTab(tipo) {
      // Actualiza el campo oculto
      document.getElementById('tipo').value = tipo;
      
      // Actualiza la apariencia de los botones
      if(tipo === 'incidencia') {
        document.getElementById('incidenciasTab').classList.add('active');
        document.getElementById('sugerenciasTab').classList.remove('active');
        // Mostrar el filtro de categoría (se permite seleccionar solo una opción)
        document.getElementById('newCategoriaContainer').style.display = 'block';
        // Actualizar el texto del botón de envío
        document.getElementById('submitButton').value = "Añadir incidencia";
        // Reiniciar la selección de radio buttons si fuese necesario
        const radios = document.getElementsByName('categoria');
        radios.forEach(radio => radio.checked = false);
      } else {
        document.getElementById('sugerenciasTab').classList.add('active');
        document.getElementById('incidenciasTab').classList.remove('active');
        // Ocultar el filtro de categoría
        document.getElementById('newCategoriaContainer').style.display = 'none';
        // Actualizar el texto del botón de envío
        document.getElementById('submitButton').value = "Añadir sugerencia";
      }
    }

    // Al cargar la página, si hay parámetros lat y lon, se rellenan y se ejecuta la geocodificación inversa
    window.onload = function() {
      const urlParams = new URLSearchParams(window.location.search);
      const lat = urlParams.get('lat');
      const lon = urlParams.get('lon');
      if (lat && lon) {
          document.getElementById('latitude').value = lat;
          document.getElementById('longitude').value = lon;
          reverseGeocode(lat, lon);
      }
      // Seleccionar por defecto "Incidencia"
      selectTab('incidencia');
    }
  </script>
</head>
<body>
  <jsp:include page="/WEB-INF/jspf/header.jsp" />
  <div class="new-incident-container">
    <h1>Nueva incidencia / sugerencia</h1>
    
    <!-- Menú de pestañas específico para este formulario -->
    <div class="new-form-tab-header">
      <button id="incidenciasTab" class="new-form-tab-button active" type="button" onclick="selectTab('incidencia')">
        <i class="fas fa-map-marker-alt"></i> Incidencias
      </button>
      <button id="sugerenciasTab" class="new-form-tab-button" type="button" onclick="selectTab('sugerencia')">
        <i class="fas fa-comment-alt"></i> Sugerencias
      </button>
    </div>
    
    <form method="post" action="<%= request.getContextPath() %>/IncidentController?action=create" enctype="multipart/form-data">
      <!-- Campo oculto para enviar el tipo (incidencia o sugerencia) -->
      <input type="hidden" id="tipo" name="tipo" value="incidencia">
      
      <!-- Campo para ingresar la dirección -->
      <div class="form-group">
        <label for="street"><i class="fas fa-map-marker-alt"></i> Calle en Tarragona</label>
        <input type="text" id="street" name="street" placeholder="Ingresa la calle (ej. Calle Mayor)" required>
        <button type="button" class="geo-btn" onclick="geocodeAddress()">Buscar Ubicación</button>
        <div id="geoMessage"></div>
      </div>
      
      <!-- Campos ocultos para latitud y longitud -->
      <input type="hidden" id="latitude" name="latitude">
      <input type="hidden" id="longitude" name="longitude">
      
      <!-- Filtro de categoría (solo para incidencias) -->
      <div id="newCategoriaContainer" class="form-group">
        <label>Filtrar por categoría (selecciona una):</label>
        <div class="new-form-topics-container">
          <!-- Se asume que 'categorias' es una lista enviada desde el backend -->
          <c:forEach var="categoria" items="${categorias}">
            <label class="topic-chip">
              <input type="radio" name="categoria" value="${categoria}">
              <span>${categoria}</span>
            </label>
          </c:forEach>
        </div>
      </div>
      
      <!-- Campo para la descripción -->
      <div class="form-group">
        <label for="description"><i class="fas fa-align-left"></i> Resumen</label>
        <textarea id="description" name="description" placeholder="Describe la incidencia o sugerencia" required></textarea>
      </div>
      
      <!-- Campo para la imagen -->
      <div class="form-group">
        <label for="image"><i class="fas fa-image"></i> Fotografía</label>
        <input type="file" id="image" name="image" accept="image/*" required>
      </div>
      
      <!-- Botón de envío: el texto se actualizará dinámicamente -->
      <div class="form-group">
        <input type="submit" id="submitButton" value="Añadir incidencia">
      </div>
    </form>
  </div>
  
  <!-- Footer añadido -->
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
