<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import = "java.sql.*" %>
<%@ page import = "authn.PasswordUtil" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Database SQL Load</title>
    </head>
    <style>
        .error {
            color: red;
        }
        pre {
            color: green;
        }
    </style>
    <body>
        <h2>Database SQL Load</h2>
        <%
            String dbname = "sob_grup_51";
            String schema = "ROOT";
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/" + dbname, "root", "root");
            Statement stmt = con.createStatement();
           
            // Hash las contraseñas
            String adminPassHash = PasswordUtil.hashPassword("adminpass");
            String user1PassHash = PasswordUtil.hashPassword("user1pass");
            String user2PassHash = PasswordUtil.hashPassword("user2pass");
            String user3PassHash = PasswordUtil.hashPassword("user3pass");
            String user4PassHash = PasswordUtil.hashPassword("user4pass");
            String sobPassHash = PasswordUtil.hashPassword("sob");

            boolean hasError = false;
            // Inserción de datos
            String data[] = new String[]{
                // Insertar Usuarios (CUSTOMER)
                "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'admin', '" + adminPassHash + "', 'ADMIN')",
                "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'user1', '" + user1PassHash + "', 'CUSTOMER')",
                "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'user2', '" + user2PassHash + "', 'CUSTOMER')",
                "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'user3', '" + user3PassHash + "', 'CUSTOMER')",
                "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'user4', '" + user4PassHash + "', 'CUSTOMER')",
                "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'sob', '" + sobPassHash + "', 'CUSTOMER')",

                // Insertar Credenciales (CREDENTIALS) vinculadas a CUSTOMER
                "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'admin', '" + adminPassHash + "', 1)",
                "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'user1', '" + user1PassHash + "', 2)",
                "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'user2', '" + user2PassHash + "', 3)",
                "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'user3', '" + user3PassHash + "', 4)",
                "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'user4', '" + user4PassHash + "', 5)",
                "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'sob', '" + sobPassHash + "', 6)",

                // Insertar Temas (TOPIC)
                "INSERT INTO " + schema + ".TOPIC (id, name) VALUES (NEXT VALUE FOR TOPIC_GEN, 'Technology')",
                "INSERT INTO " + schema + ".TOPIC (id, name) VALUES (NEXT VALUE FOR TOPIC_GEN, 'Science')",
                "INSERT INTO " + schema + ".TOPIC (id, name) VALUES (NEXT VALUE FOR TOPIC_GEN, 'Health')",
                "INSERT INTO " + schema + ".TOPIC (id, name) VALUES (NEXT VALUE FOR TOPIC_GEN, 'Education')",
                "INSERT INTO " + schema + ".TOPIC (id, name) VALUES (NEXT VALUE FOR TOPIC_GEN, 'Business')",

                // Insertar Artículos (ARTICLE)
                "INSERT INTO " + schema + ".ARTICLE (id, title, content, summary, imageUrl, publicationDate, viewCount, isPrivate, author_id) VALUES (NEXT VALUE FOR ARTICLE_GEN, 'Introduction to Java', 'Java is a versatile programming language...', 'A brief introduction to Java.', '/Homework1_pre/images/articles/java-logo-png.png', CURRENT_DATE, 0, 0, 1)",
                "INSERT INTO " + schema + ".ARTICLE (id, title, content, summary, imageUrl, publicationDate, viewCount, isPrivate, author_id) VALUES (NEXT VALUE FOR ARTICLE_GEN, 'Advanced SQL Techniques', 'SQL is essential for database management...', 'Exploring advanced SQL topics.', '/Homework1_pre/images/articles/sql_ejercicios.png', CURRENT_DATE, 0, 1, 1)",
                "INSERT INTO " + schema + ".ARTICLE (id, title, content, summary, imageUrl, publicationDate, viewCount, isPrivate, author_id) VALUES (NEXT VALUE FOR ARTICLE_GEN, 'Health and Wellness', 'Maintaining good health is crucial...', 'Tips for a healthy lifestyle.', '/Homework1_pre/images/articles/hq720.jpg', CURRENT_DATE, 0, 0, 2)",
                "INSERT INTO " + schema + ".ARTICLE (id, title, content, summary, imageUrl, publicationDate, viewCount, isPrivate, author_id) VALUES (NEXT VALUE FOR ARTICLE_GEN, 'Educational Reforms', 'Discussing the latest trends in education...', 'An overview of educational reforms.', '/Homework1_pre/images/articles/1043-978x652-1.jpg', CURRENT_DATE, 0, 0, 4)",
                "INSERT INTO " + schema + ".ARTICLE (id, title, content, summary, imageUrl, publicationDate, viewCount, isPrivate, author_id) VALUES (NEXT VALUE FOR ARTICLE_GEN, 'Business Strategies', 'Understanding modern business strategies...', 'Insights into business world.', '/Homework1_pre/images/articles/Leadership-Skills.png', CURRENT_DATE, 0, 1, 5)",

                // Insertar Relaciones entre Artículos y Temas (ARTICLE_TOPIC)
                "INSERT INTO " + schema + ".ARTICLE_TOPIC (TOPIC_ID, ARTICLE_ID) VALUES (1, 1)",
                "INSERT INTO " + schema + ".ARTICLE_TOPIC (TOPIC_ID, ARTICLE_ID) VALUES (2, 2)",
                "INSERT INTO " + schema + ".ARTICLE_TOPIC (TOPIC_ID, ARTICLE_ID) VALUES (1, 2)",
                "INSERT INTO " + schema + ".ARTICLE_TOPIC (TOPIC_ID, ARTICLE_ID) VALUES (3, 3)",
                "INSERT INTO " + schema + ".ARTICLE_TOPIC (TOPIC_ID, ARTICLE_ID) VALUES (3, 1)",
                "INSERT INTO " + schema + ".ARTICLE_TOPIC (TOPIC_ID, ARTICLE_ID) VALUES (4, 4)",
                "INSERT INTO " + schema + ".ARTICLE_TOPIC (TOPIC_ID, ARTICLE_ID) VALUES (3, 5)",
                "INSERT INTO " + schema + ".ARTICLE_TOPIC (TOPIC_ID, ARTICLE_ID) VALUES (5, 5)"
            };

            for (String datum : data) {
                try {
                    stmt.executeUpdate(datum);
                    out.println("<pre> -> " + datum + "</pre>");
                } catch (SQLException e) {
                    hasError = true;
                    if (e.getSQLState().equals("23505")) { // Clave duplicada
                        out.println("<span class='error'>Clave duplicada, omitiendo: " + datum + "</span><br>");
                    } else {
                        out.println("<span class='error'>SQLException al insertar datos: " + datum + "</span><br>");
                        out.println("<span class='error'>" + e.getMessage() + "</span><br>");
                    }
                }
            }
            
            // Cerrar recursos
            stmt.close();
            con.close();

            if (!hasError) {
                // Si no hubo errores, redirigir al RootController después de 2 segundos
                response.setHeader("Refresh", "2;url=" + request.getContextPath() + "/RootController");
                out.println("<div style='text-align: center; margin-top: 20px;'>");
                out.println("<h3 style='color: green;'>Instalación completada con éxito</h3>");
                out.println("<p>Redirigiendo a la página principal en 2 segundos...</p>");
                out.println("</div>");
            } else {
        %>
                <div style="text-align: center; margin-top: 20px;">
                    <h3 style="color: red;">Se encontraron algunos errores durante la instalación</h3>
                    <button onclick="window.location='<%=request.getContextPath()%>/RootController'" style="margin-top: 10px;">
                        Ir a la página principal
                    </button>
                </div>
        <%
            }
        %>
    </body>
</html>
