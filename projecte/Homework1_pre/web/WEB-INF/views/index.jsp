<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="model.entities.ArticleDTO" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Inicio - Medium-like</title>
    <!-- IMPORTANTE: Asegúrate de tener viewport para responsividad -->
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
    <!-- Hoja de estilos -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/style.css"/>
    <!-- Iconos -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css"/>
    <!-- Script principal -->
    <script src="<%= request.getContextPath() %>/resources/js/app.js" defer></script>
</head>
<body>
    <!-- Incluir el header con tu código responsivo -->
    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <!-- Hero Section -->
    <section class="hero">
        <h1>Explora Historias Increíbles</h1>
        <p>Descubre, comparte y conecta con una comunidad apasionada por las historias.</p>
    </section>

    <!-- Filter Section -->
    <div class="filter-section">
        <h2>Filtrar Artículos</h2>

        <!-- Botón para mostrar/ocultar filtros en móviles (por defecto oculto en escritorio) -->
        <button class="btn-primary" id="toggle-filters" style="display: none; margin-bottom: 15px;">
            <i class="fas fa-filter"></i> Mostrar Filtros
        </button>

        <!-- Formulario de filtros -->
        <form action="<%= request.getContextPath() %>/RootController" method="get" class="filter-form" id="filter-form">
            <div class="form-group">
                <label for="topicSearch">Tópicos</label>
                <input type="text" id="topicSearch" placeholder="Buscar temas..." class="topic-search">
                <div class="topics-container">
                    <% 
                        Set<String> availableTopics = (Set<String>) request.getAttribute("availableTopics");
                        String[] selectedTopics = request.getParameterValues("topic");
                        Set<String> selectedTopicsSet = new HashSet<>();
                        if (selectedTopics != null) {
                            selectedTopicsSet.addAll(Arrays.asList(selectedTopics));
                        }

                        if (availableTopics != null) {
                            for (String topic : availableTopics) { 
                    %>
                        <label class="topic-chip <%= selectedTopicsSet.contains(topic) ? "selected" : "" %>">
                            <input type="checkbox" name="topic" value="<%= topic %>" 
                                   <%= selectedTopicsSet.contains(topic) ? "checked" : "" %>>
                            <span><%= topic %></span>
                        </label>
                    <% 
                            }
                        } 
                    %>
                </div>
            </div>
            <div class="form-group">
                <label for="author">Autor</label>
                <input type="text" id="author" name="author" 
                       value="<%= request.getParameter("author") != null ? request.getParameter("author") : "" %>"
                       placeholder="Escribe el autor">
            </div>
            <div class="filter-actions">
                <button type="submit" class="btn-primary btn-fixed-size">Aplicar Filtros</button>
                <button type="reset" class="btn-secondary btn-fixed-size" onclick="window.location.href='<%= request.getContextPath() %>/RootController'">Resetear Filtros</button>
            </div>
        </form>
    </div>

    <!-- Article Section -->
    <section class="articles">
        <%
            String errorMessage = (String) request.getAttribute("errorMessage");
            String successMessage = (String) request.getAttribute("successMessage");
            if (errorMessage != null) {
        %>
            <p class="error"><%= errorMessage %></p>
        <%
            }
            if (successMessage != null) {
        %>
            <p class="success"><%= successMessage %></p>
        <%
            }

            List<ArticleDTO> articles = (List<ArticleDTO>) request.getAttribute("articles");
            if (articles != null && !articles.isEmpty()) {
        %>
            <div class="article-grid">
                <%
                    for (ArticleDTO article : articles) {
                %>
                <div class="article-card">
                    <% if (article.isIsPrivate()) { %>
                        <div class="private-indicator">
                            <i class="fas fa-lock"></i> Privado
                        </div>
                    <% } %>
                    <a href="<%= request.getContextPath()%>/ArticleController?action=detail&id=<%=article.getId()%>">
                        <img src="<%= article.getImageUrl() %>" alt="<%= article.getTitle() %>" class="article-image">
                    </a>
                    <div class="article-content">
                        <h2 class="article-title">
                            <a href="<%= request.getContextPath()%>/ArticleController?action=detail&id=<%=article.getId()%>">
                                <%= article.getTitle() %>
                            </a>
                        </h2>
                        <p class="article-summary"><%= article.getSummary() %></p>
                        <ul class="article-topics">
                            <%
                                for (String topic : article.getTopics()) {
                            %>
                                <li><%= topic %></li>
                            <%
                                }
                            %>
                        </ul>
                        <div class="article-meta">
                            <span>Por <%= article.getAuthorName() %></span> |
                            <span><%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(article.getPublicationDate()) %></span>
                        </div>
                    </div>
                </div>
                <%
                    }
                %>
            </div>
        <%
            } else {
        %>
            <p class="no-articles">No hay artículos para mostrar.</p>
        <%
            }
        %>
    </section>

    <!-- Script para la lógica de toggles en móvil -->
    <script>
        document.addEventListener('DOMContentLoaded', function() {
          // 1. Toggle de filtros
          const toggleFiltersBtn = document.getElementById('toggle-filters');
          const filterForm = document.getElementById('filter-form');

          // Función para revisar el tamaño de la ventana
          function checkWindowSize() {
            if (window.innerWidth <= 768) {
              // En pantallas pequeñas, mostramos el botón y ocultamos el form
              toggleFiltersBtn.style.display = 'inline-block';
              filterForm.style.display = 'none';
              toggleFiltersBtn.innerHTML = '<i class="fas fa-filter"></i> Mostrar Filtros';
            } else {
              // En escritorio, mostramos el formulario y ocultamos el botón
              toggleFiltersBtn.style.display = 'none';
              filterForm.style.display = 'flex';
            }
          }

          // Al cargar y al cambiar tamaño de ventana
          checkWindowSize();
          window.addEventListener('resize', checkWindowSize);

          // Evento de clic para mostrar/ocultar los filtros
          toggleFiltersBtn.addEventListener('click', function() {
            if (filterForm.style.display === 'none' || filterForm.style.display === '') {
              filterForm.style.display = 'flex';
              toggleFiltersBtn.innerHTML = '<i class="fas fa-filter"></i> Ocultar Filtros';
            } else {
              filterForm.style.display = 'none';
              toggleFiltersBtn.innerHTML = '<i class="fas fa-filter"></i> Mostrar Filtros';
            }
          });
        });
      </script>
    </body>
</html>
