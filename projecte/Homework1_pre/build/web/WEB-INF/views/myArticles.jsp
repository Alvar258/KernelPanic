<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Crear Nuevo Artículo - Medium-like</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/style.css"/>
    <script src="<%= request.getContextPath() %>/resources/js/app.js" defer></script>
</head>
<body>
    <!-- Header -->
    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <!-- Contenedor Principal -->
    <div class="container">
        <h1>Crear Nuevo Artículo</h1>
        <%
            String errorMessage = (String) request.getAttribute("errorMessage");
            if (errorMessage != null) {
        %>
            <p class="error"><%= errorMessage %></p>
        <%
            }
        %>
        <form method="post" action="<%= request.getContextPath()%>/ArticleController?action=create">
            <label for="title">Título</label>
            <input type="text" id="title" name="title" placeholder="Introduce el título" required/>

            <label for="summary">Resumen</label>
            <input type="text" id="summary" name="summary" placeholder="Introduce un breve resumen" required/>

            <label for="content">Contenido</label>
            <textarea id="content" name="content" rows="8" placeholder="Escribe el contenido del artículo" required></textarea>

            <label for="imageUrl">URL de la imagen</label>
            <input type="text" id="imageUrl" name="imageUrl" placeholder="Introduce la URL de la imagen"/>

            <label for="topics">Temas (máximo 2, separados por coma)</label>
            <input type="text" id="topics" name="topics" placeholder="Ej: Tecnología, Educación" required/>

            <label for="isPrivate">
                <input type="checkbox" id="isPrivate" name="isPrivate" value="true"/>
                ¿Artículo privado?
            </label>

            <button type="submit">Crear Artículo</button>
        </form>
    </div>
</body>
</html>
