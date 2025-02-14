<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.Set" %>
<%@ page import="model.entities.ArticleDTO" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Artículo</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/resources/css/style.css"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css"/>
</head>
<body>
    <!-- Header -->
    <jsp:include page="/WEB-INF/jspf/header.jsp" />
    
    <div class="container">
        <div class="article-form-container">
            <h1 class="form-title"><i class="fas fa-edit"></i> Editar Artículo</h1>
            
            <%
                ArticleDTO article = (ArticleDTO) request.getAttribute("article");
                Set<String> availableTopics = (Set<String>) request.getAttribute("availableTopics");
            %>
            
            <form id="articleForm" method="post" action="<%= request.getContextPath() %>/ArticleController?action=update" 
                  class="article-form" enctype="multipart/form-data">
                <input type="hidden" name="id" value="<%= article.getId() %>">
                
                <div class="form-group">
                    <label for="title">
                        <i class="fas fa-heading"></i> Título
                    </label>
                    <input type="text" id="title" name="title" 
                           value="<%= article.getTitle() %>" required>
                </div>

                <div class="form-group">
                    <label for="summary">
                        <i class="fas fa-align-left"></i> Resumen
                    </label>
                    <textarea id="summary" name="summary" required><%= article.getSummary() %></textarea>
                </div>

                <div class="form-group">
                    <label for="content">
                        <i class="fas fa-book-open"></i> Contenido
                    </label>
                    <textarea id="content" name="content" rows="10" required><%= article.getContent() %></textarea>
                </div>

                <div class="form-group">
                    <label for="imageUpload">
                        <i class="fas fa-image"></i> Imagen del artículo
                    </label>
                    <input type="file" class="form-control" id="imageUpload" name="image" accept="image/*">
                    <div class="image-preview-container">
                        <img id="preview" src="<%= article.getImageUrl() %>" alt="Vista previa" class="image-preview">
                    </div>
                    <input type="hidden" id="imageUrl" name="imageUrl" value="<%= article.getImageUrl() %>">
                </div>

                <script>
                    document.getElementById('imageUpload').addEventListener('change', async function(e) {
                        const preview = document.getElementById('preview');
                        const file = e.target.files[0];
                        
                        if (file) {
                            // Mostrar preview
                            const reader = new FileReader();
                            reader.onload = function(e) {
                                preview.src = e.target.result;
                                preview.style.display = 'block';
                            }
                            reader.readAsDataURL(file);

                            // Subir imagen
                            const formData = new FormData();
                            formData.append('image', file);

                            try {
                                const response = await fetch('<%= request.getContextPath() %>/ImageUploadController', {
                                    method: 'POST',
                                    body: formData
                                });
                                const data = await response.json();
                                if (data.url) {
                                    document.getElementById('imageUrl').value = data.url;
                                } else {
                                    alert('Error al subir la imagen: ' + (data.error || 'Error desconocido'));
                                    // Opcional: Reiniciar el input de archivo y la vista previa
                                    e.target.value = '';
                                    preview.src = '<%= article.getImageUrl() %>';
                                }
                            } catch (error) {
                                console.error('Error:', error);
                                alert('Error al subir la imagen');
                                // Opcional: Reiniciar el input de archivo y la vista previa
                                e.target.value = '';
                                preview.src = '<%= article.getImageUrl() %>';
                            }
                        }
                    });
                </script>

                <div class="form-group">
                    <label>
                        <i class="fas fa-tags"></i> Tópicos
                    </label>
                    <div class="topics-container">
                        <% 
                            if (availableTopics != null) {
                                for (String topic : availableTopics) { 
                        %>
                            <label class="topic-chip <%= article.getTopics().contains(topic) ? "selected" : "" %>">
                                <input type="checkbox" name="topics" value="<%= topic %>"
                                       <%= article.getTopics().contains(topic) ? "checked" : "" %>>
                                <i class="fas fa-tag"></i>
                                <span><%= topic %></span>
                            </label>
                        <%      }
                            } 
                        %>
                    </div>
                </div>

                <div class="form-group checkbox-group">
                    <label for="isPrivate" class="checkbox-label">
                        <input type="checkbox" id="isPrivate" name="isPrivate" 
                               <%= article.isIsPrivate() ? "checked" : "" %>>
                        <span class="checkbox-text">
                            <i class="fas fa-lock"></i> Artículo Privado
                        </span>
                    </label>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn-primary" id="submitButton">
                        <i class="fas fa-save"></i> Guardar Cambios
                    </button>
                    <a href="<%= request.getContextPath() %>/ArticleController?action=detail&id=<%= article.getId() %>" 
                       class="btn-secondary">
                        <i class="fas fa-times"></i> Cancelar
                    </a>
                </div>
            </form>
        </div>
    </div>

    <script>
        // Prevenir envío del formulario si se está subiendo una imagen
        document.getElementById('articleForm').addEventListener('submit', function(e) {
            const imageUpload = document.getElementById('imageUpload');
            const imageUrl = document.getElementById('imageUrl').value;
            
            if (imageUpload.files.length > 0 && !imageUrl) {
                e.preventDefault();
                alert('Por favor, espera a que la imagen termine de subir.');
                return;
            }
        });

        // Manejar la selección de topics
        document.querySelectorAll('.topic-chip').forEach(chip => {
            chip.addEventListener('click', function(e) {
                e.preventDefault();
                
                const checkbox = this.querySelector('input[type="checkbox"]');
                const selectedTopics = document.querySelectorAll('.topic-chip input[type="checkbox"]:checked');
                
                if (!checkbox.checked && selectedTopics.length >= 2) {
                    alert('No puedes seleccionar más de 2 tópicos');
                    return;
                }
                
                this.classList.toggle('selected');
                checkbox.checked = !checkbox.checked;
            });
        });

        // Preview de la imagen cuando se ingresa la URL
        document.getElementById('imageUrl').addEventListener('input', function(e) {
            const url = e.target.value;
            if (url) {
                console.log('Nueva URL de imagen:', url);
            }
        });
    </script>
</body>
</html>
