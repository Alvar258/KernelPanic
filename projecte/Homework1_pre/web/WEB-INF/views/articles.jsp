<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.entities.ArticleDTO" %>
<html>
<head>
    <title>Articles - Medium-like</title>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/style.css"/>
    <script src="<%=request.getContextPath()%>/js/app.js"></script>
</head>
<body>
    <jsp:include page="/WEB-INF/jspf/header.jsp" />
    <main>
        <h1>Llista d'Articles</h1>

        <!-- Filtre -->
        <form method="get" action="<%= request.getContextPath() %>/ArticleController" class="filter-form">
            <input type="hidden" name="action" value="list"/>
            <input type="text" name="topic" placeholder="Topic"/>
            <input type="text" name="author" placeholder="Author"/>
            <button type="submit">Filtrar</button>
        </form>

        <%
            List<ArticleDTO> articles = (List<ArticleDTO>) request.getAttribute("articles");
            if (articles != null) {
                for (ArticleDTO art : articles) {
        %>
            <article class="article-card">
                <div class="card mb-4">
                    <a href="<%= request.getContextPath() %>/ArticleController?action=detail&id=<%=art.getId()%>">
                        <img src="${art.imageUrl}" class="card-img-top" alt="${art.title}" style="height: 200px; object-fit: cover;">
                    </a>
                    <div class="card-body">
                        <h2>
                          <a href="<%=request.getContextPath()%>/ArticleController?action=detail&id=<%=art.getId()%>">
                            <%= art.getTitle() %>
                          </a>
                        </h2>
                        <p class="summary"><%= art.getSummary() %></p>
                        <div class="meta">
                            <span>Autor: <%= art.getAuthorName() %></span> | 
                            <span>Data: <%= art.getPublicationDate() %></span> |
                            <span>Views: <%= art.getViewCount() %></span>
                            <% if (art.isIsPrivate()) { %>
                                <span class="private">Privat</span>
                            <% } %>
                        </div>
                    </div>
                </div>
            </article>
        <%
                }
            }
        %>
    </main>
</body>
</html>
