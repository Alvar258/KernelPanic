<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Incidencias y Sugerencias</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/style.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
  <!-- Nuevos estilos para el menú en tonos azules y centrado de títulos -->
  <script>
    function showTab(tab) {
      // Oculta ambas secciones
      document.getElementById('incidencias').style.display = 'none';
      document.getElementById('sugerencias').style.display = 'none';
      
      // Quita la clase activa de ambos botones
      document.getElementById('incidenciasButton').classList.remove('active');
      document.getElementById('sugerenciasButton').classList.remove('active');
      
      // Muestra la pestaña seleccionada y marca el botón activo
      document.getElementById(tab).style.display = 'block';
      document.getElementById(tab + 'Button').classList.add('active');
    }
    window.onload = function() {
      showTab('incidencias'); // Por defecto se muestra Incidencias
    };
  </script>
</head>
<body>
  <jsp:include page="/WEB-INF/jspf/header.jsp" />
  <div class="admin-panel">
    <!-- Encabezado interno con pestañas -->
    <div class="tab-header2">
      <!-- Ícono cambiado a fa-map-marker-alt -->
      <button id="incidenciasButton" class="tab-button2 active" onclick="showTab('incidencias')">
        <i class="fas fa-map-marker-alt"></i> Incidencias
      </button>
      <button id="sugerenciasButton" class="tab-button2" onclick="showTab('sugerencias')">
        <i class="fas fa-comment-alt"></i> Sugerencias
      </button>
    </div>
    
    <!-- Contenido de Incidencias (con filtros, tabla y paginación) -->
    <div id="incidencias" class="tab-content2">
      <h2>Listado de Incidencias</h2>
      <div class="form-group">
        <label>Filtrar por categoría:</label>
        <div class="topics-container">
          <c:forEach var="categoria" items="${categorias}">
            <label class="topic-chip">
              <input type="checkbox" name="categorias" value="${categoria}">
              <span>${categoria}</span>
            </label>
          </c:forEach>
        </div>
      </div>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Descripción</th>
            <th>Categoría</th>
            <th>Estado</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="incidencia" items="${incidencias}">
            <tr>
              <td>${incidencia.id}</td>
              <td>${incidencia.descripcion}</td>
              <td>${incidencia.categoria}</td>
              <td>${incidencia.estado}</td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
      <!-- Paginación dinámica para Incidencias -->
      <div id="incidenciasPaginacion" class="paginacion">
        <ul class="pagination">
          <li>
            <a href="?incidenciasPage=${currentIncidenciasPage > 1 ? currentIncidenciasPage - 1 : 1}">&laquo;</a>
          </li>
          <c:forEach begin="1" end="${incidenciasTotalPages}" var="i">
            <li>
              <a href="?incidenciasPage=${i}" class="${i == currentIncidenciasPage ? 'active' : ''}">${i}</a>
            </li>
          </c:forEach>
          <li>
            <a href="?incidenciasPage=${currentIncidenciasPage < incidenciasTotalPages ? currentIncidenciasPage + 1 : incidenciasTotalPages}">&raquo;</a>
          </li>
        </ul>
      </div>
    </div>
    
    <!-- Contenido de Sugerencias (tabla y paginación) -->
    <div id="sugerencias" class="tab-content2" style="display: none;">
      <h2>Listado de Sugerencias</h2>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Descripción</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="sugerencia" items="${sugerencias}">
            <tr>
              <td>${sugerencia.id}</td>
              <td>${sugerencia.descripcion}</td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
      <!-- Paginación dinámica para Sugerencias -->
      <div id="sugerenciasPaginacion" class="paginacion">
        <ul class="pagination">
          <li>
            <a href="?sugerenciasPage=${currentSugerenciasPage > 1 ? currentSugerenciasPage - 1 : 1}">&laquo;</a>
          </li>
          <c:forEach begin="1" end="${sugerenciasTotalPages}" var="i">
            <li>
              <a href="?sugerenciasPage=${i}" class="${i == currentSugerenciasPage ? 'active' : ''}">${i}</a>
            </li>
          </c:forEach>
          <li>
            <a href="?sugerenciasPage=${currentSugerenciasPage < sugerenciasTotalPages ? currentSugerenciasPage + 1 : sugerenciasTotalPages}">&raquo;</a>
          </li>
        </ul>
      </div>
    </div>
  </div>
  
  <!-- Footer (igual al JSP de ejemplo) -->
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
