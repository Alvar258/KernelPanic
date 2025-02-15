<%@ page import="model.entities.ArticleDTO" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Detalle del Artículo</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/style.css"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css"/>
</head>
<body>
    <!-- Header -->
    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <div class="container">
        <%
            ArticleDTO article = (ArticleDTO) request.getAttribute("article");
            if (article != null) {
        %>
        <div class="article-detail-wrapper">
            <div class="article-detail-header">
                <div class="article-detail-title">
                    <h1><%= article.getTitle() %></h1>
                    <% if (article.isIsPrivate()) { %>
                        <span class="article-private-badge">
                            <i class="fas fa-lock"></i> Artículo Privado
                        </span>
                    <% } %>
                </div>
                
                <div class="article-detail-meta">
                    <div class="meta-item">
                        <i class="fas fa-user"></i>
                        <span><%= article.getAuthorName() %></span>
                    </div>
                    <div class="meta-item">
                        <i class="fas fa-calendar"></i>
                        <span><%= new SimpleDateFormat("dd/MM/yyyy").format(article.getPublicationDate()) %></span>
                    </div>
                    <div class="meta-item">
                        <i class="fas fa-eye"></i>
                        <span><%= article.getViewCount() %> vistas</span>
                    </div>
                </div>
            </div>

            <div class="article-detail-image-container">
                <img src="<%= article.getImageUrl() != null && !article.getImageUrl().isEmpty() ? 
                          article.getImageUrl() : request.getContextPath() + "/resources/images/noimg.jpg" %>" 
                     alt="<%= article.getTitle() %>" class="article-detail-image">
            </div>

            <% if (article.getTopics() != null && !article.getTopics().isEmpty()) { %>
                <div class="article-detail-topics">
                    <% for (String topic : article.getTopics()) { %>
                        <span class="topic-tag">
                            <i class="fas fa-tag"></i>
                            <%= topic %>
                        </span>
                    <% } %>
                </div>
            <% } %>

            <div class="article-detail-content">
                <p><%= article.getContent() %></p>
            </div>

            <% 
                boolean isAdmin = session != null && "ADMIN".equals(session.getAttribute("role"));
                boolean isAuthor = session != null && article.getAuthorName().equals(session.getAttribute("username"));
                if (isAuthor || isAdmin) { 
            %>
                <div class="article-detail-actions">
                    <a href="<%= request.getContextPath() %>/ArticleController?action=edit&id=<%= article.getId() %>" 
                       class="btn btn-edit">
                        <i class="fas fa-edit"></i> Editar
                    </a>
                    <a href="<%= request.getContextPath() %>/ArticleController?action=delete&id=<%= article.getId() %>" 
                       class="btn btn-delete" 
                       onclick="return confirm('¿Estás seguro de eliminar este artículo?')">
                        <i class="fas fa-trash"></i> Eliminar
                    </a>
                </div>
            <% } %>
        </div>
        <% } %>
    </div>
</body>
</html>