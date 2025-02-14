<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import = "java.sql.*" %>

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
           

            // Inserción de datos
            String data[] = new String[]{
                // Insertar Usuarios (CUSTOMER)
                "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'admin', 'adminpass', 'ADMIN')",
                "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'user1', 'user1pass', 'CUSTOMER')",
                "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'user2', 'user2pass', 'CUSTOMER')",
                "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'user3', 'user3pass', 'CUSTOMER')",
                "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'user4', 'user4pass', 'CUSTOMER')",
                "INSERT INTO " + schema + ".CUSTOMER (id, username, password, role) VALUES (NEXT VALUE FOR CUSTOMER_GEN, 'sob', 'sob', 'CUSTOMER')",

                // Insertar Credenciales (CREDENTIALS) vinculadas a CUSTOMER
                "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'admin', 'adminpass', 1)",
                "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'user1', 'user1pass', 2)",
                "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'user2', 'user2pass', 3)",
                "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'user3', 'user3pass', 4)",
                "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'user4', 'user4pass', 5)",
                "INSERT INTO " + schema + ".CREDENTIALS (id, username, password, customer_id) VALUES (NEXT VALUE FOR CREDENTIALS_GEN, 'sob', 'sob', 6)",

                // Insertar Temas (TOPIC)
                "INSERT INTO " + schema + ".TOPIC (id, name) VALUES (NEXT VALUE FOR TOPIC_GEN, 'Technology')",
                "INSERT INTO " + schema + ".TOPIC (id, name) VALUES (NEXT VALUE FOR TOPIC_GEN, 'Science')",
                "INSERT INTO " + schema + ".TOPIC (id, name) VALUES (NEXT VALUE FOR TOPIC_GEN, 'Health')",
                "INSERT INTO " + schema + ".TOPIC (id, name) VALUES (NEXT VALUE FOR TOPIC_GEN, 'Education')",
                "INSERT INTO " + schema + ".TOPIC (id, name) VALUES (NEXT VALUE FOR TOPIC_GEN, 'Business')",

                // Insertar Artículos (ARTICLE)
                "INSERT INTO " + schema + ".ARTICLE (id, title, content, summary, imageUrl, publicationDate, viewCount, isPrivate, author_id) VALUES (NEXT VALUE FOR ARTICLE_GEN, 'Introduction to Java', 'Java is a versatile programming language...', 'A brief introduction to Java.', 'http://example.com/java.jpg', CURRENT_DATE, 0, 0, 1)",
                "INSERT INTO " + schema + ".ARTICLE (id, title, content, summary, imageUrl, publicationDate, viewCount, isPrivate, author_id) VALUES (NEXT VALUE FOR ARTICLE_GEN, 'Advanced SQL Techniques', 'SQL is essential for database management...', 'Exploring advanced SQL topics.', 'http://example.com/sql.jpg', CURRENT_DATE, 0, 1, 1)",
                "INSERT INTO " + schema + ".ARTICLE (id, title, content, summary, imageUrl, publicationDate, viewCount, isPrivate, author_id) VALUES (NEXT VALUE FOR ARTICLE_GEN, 'Health and Wellness', 'Maintaining good health is crucial...', 'Tips for a healthy lifestyle.', 'http://example.com/health.jpg', CURRENT_DATE, 0, 0, 2)",
                "INSERT INTO " + schema + ".ARTICLE (id, title, content, summary, imageUrl, publicationDate, viewCount, isPrivate, author_id) VALUES (NEXT VALUE FOR ARTICLE_GEN, 'Educational Reforms', 'Discussing the latest trends in education...', 'An overview of educational reforms.', 'http://example.com/education.jpg', CURRENT_DATE, 0, 0, 4)",
                "INSERT INTO " + schema + ".ARTICLE (id, title, content, summary, imageUrl, publicationDate, viewCount, isPrivate, author_id) VALUES (NEXT VALUE FOR ARTICLE_GEN, 'Business Strategies', 'Understanding modern business strategies...', 'Insights into business world.', 'http://example.com/business.jpg', CURRENT_DATE, 0, 1, 5)",

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
                    if (e.getSQLState().equals("23505")) { // Clave duplicada
                        out.println("<span class='error'>Clave duplicada, omitiendo: " + datum + "</span><br>");
                    } else {
                        out.println("<span class='error'>SQLException al insertar datos: " + datum + "</span><br>");
                        out.println("<span class='error'>" + e.getMessage() + "</span><br>");
                        return;
                    }
                }
            }
        %>
        <button onclick="window.location='<%=request.getSession().getServletContext().getContextPath()%>'">Go home</button>
    </body>
</html>
