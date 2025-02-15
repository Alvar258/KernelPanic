<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="model.entities.ArticleDTO" %>
<%@ page import="model.entities.Customer" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel de Administración</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/resources/css/style.css"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css"/>
</head>
<body>
    <!-- Header -->
    <jsp:include page="/WEB-INF/jspf/header.jsp" />

    <div class="admin-container">
        <div class="admin-header">
            <h1>Panel de Administración</h1>
            <div class="admin-stats">
                <div class="stat-card">
                    <i class="fas fa-newspaper"></i>
                    <span>Total Artículos: ${totalArticles}</span>
                </div>
                <div class="stat-card">
                    <i class="fas fa-users"></i>
                    <span>Total Usuarios: ${totalUsers}</span>
                </div>
            </div>
        </div>

        <!-- Mensajes -->
        <% if (request.getAttribute("errorMessage") != null) { %>
            <div class="error-message">
                <%= request.getAttribute("errorMessage") %>
            </div>
        <% } %>
        <% if (request.getAttribute("successMessage") != null) { %>
            <div class="success-message">
                <%= request.getAttribute("successMessage") %>
            </div>
        <% } %>

        <!-- Pestañas -->
        <div class="admin-tabs">
            <button class="tab-button active" onclick="showTab('articles')">
                <i class="fas fa-newspaper"></i> Artículos
            </button>
            <button class="tab-button" onclick="showTab('users')">
                <i class="fas fa-users"></i> Usuarios
            </button>
        </div>

        <!-- Pestaña de Artículos -->
        <div id="articles-tab" class="tab-content active">
            <div class="admin-create-form">
                <h2><i class="fas fa-plus-circle"></i> Crear Nuevo Artículo</h2>
                <form action="${pageContext.request.contextPath}/admin" method="post" enctype="multipart/form-data" id="articleForm" onsubmit="return validateAndSubmitForm(event)">
                    <input type="hidden" name="action" value="createArticle">
                    
                    <div class="form-group">
                        <label for="title"><i class="fas fa-heading"></i> Título</label>
                        <input type="text" id="title" name="title" placeholder="Título del artículo" required>
                    </div>
                    
                    <div class="form-group">
                        <label for="content"><i class="fas fa-book-open"></i> Contenido</label>
                        <textarea id="content" name="content" placeholder="Contenido del artículo" required></textarea>
                    </div>
                    
                    <div class="form-group">
                        <label for="summary"><i class="fas fa-align-left"></i> Resumen</label>
                        <textarea id="summary" name="summary" placeholder="Resumen del artículo" required></textarea>
                    </div>
                    
                    <div class="form-group">
                        <label for="imageUpload">
                            <i class="fas fa-image"></i> Imagen del artículo
                        </label>
                        <input type="file" class="form-control" id="imageUpload" name="image" accept="image/*" required>
                        <div class="image-preview-container">
                            <img id="preview" src="#" alt="Vista previa" class="image-preview" style="display: none;">
                        </div>
                        <input type="hidden" id="imageUrl" name="imageUrl">
                    </div>

                    <div class="form-group">
                        <label><i class="fas fa-tags"></i> Tópicos:</label>
                        <div class="topics-container">
                            <% 
                                Set<String> availableTopics = (Set<String>)request.getAttribute("availableTopics");
                                if (availableTopics != null && !availableTopics.isEmpty()) {
                                    for (String topic : availableTopics) { 
                                        if (topic != null && !topic.trim().isEmpty()) { 
                            %>
                                <label class="topic-chip">
                                    <input type="checkbox" name="topics" value="<%= topic %>">
                                    <i class="fas fa-tag"></i>
                                    <span><%= topic %></span>
                                </label>
                            <% 
                                        }
                                    }
                                } else { 
                            %>
                                <p>No hay tópicos disponibles.</p>
                            <% } %>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="checkbox-label">
                            <input type="checkbox" name="isPrivate">
                            <span><i class="fas fa-lock"></i> Es Privado</span>
                        </label>
                    </div>

                    <button type="submit" class="admin-btn admin-btn-create">
                        <i class="fas fa-plus"></i> Crear Artículo
                    </button>
                </form>
            </div>

            <script>
            let imageUploaded = false;
            let selectedTopicsCount = 0;

            document.querySelectorAll('.topic-chip').forEach(chip => {
                chip.addEventListener('click', function(e) {
                    // Evitar que se active el checkbox al hacer clic en el label
                    if (e.target.tagName === 'LABEL' || e.target.tagName === 'SPAN') {
                        e.preventDefault();
                    }
                    
                    const checkbox = this.querySelector('input[type="checkbox"]');
                    const selectedTopics = document.querySelectorAll('.topic-chip input[type="checkbox"]:checked');
                    
                    if (!checkbox.checked && selectedTopics.length >= 2) {
                        alert('No puedes seleccionar más de 2 tópicos');
                        return;
                    }
                    
                    checkbox.checked = !checkbox.checked;
                    this.classList.toggle('selected', checkbox.checked);
                });
            });

            async function validateAndSubmitForm(event) {
                event.preventDefault();
                
                const title = document.getElementById('title').value.trim();
                const content = document.getElementById('content').value.trim();
                const summary = document.getElementById('summary').value.trim();
                const imageUrl = document.getElementById('imageUrl').value.trim();
                const selectedTopics = document.querySelectorAll('input[name="topics"]:checked');
                
                // Debug log
                console.log('Form data before submission:');
                console.log('Title:', title);
                console.log('Content length:', content.length);
                console.log('Summary:', summary);
                console.log('Image URL:', imageUrl);
                console.log('Selected topics:', Array.from(selectedTopics).map(t => t.value));
                
                const form = document.getElementById('articleForm');
                console.log('Form action:', form.action);
                console.log('Form method:', form.method);
                console.log('Form enctype:', form.enctype);
                
                // Log all form data
                const formData = new FormData(form);
                for (let [key, value] of formData.entries()) {
                    console.log('Form field -', key + ':', value);
                }
                
                let errors = [];

                if (!title) errors.push('El título es requerido');
                if (!content) errors.push('El contenido es requerido');
                if (!summary) errors.push('El resumen es requerido');
                if (!imageUrl) errors.push('La imagen es requerida');
                if (selectedTopics.length === 0) errors.push('Debes seleccionar al menos un tópico');
                if (selectedTopics.length > 2) errors.push('No puedes seleccionar más de dos tópicos');

                if (errors.length > 0) {
                    alert(errors.join('\n'));
                    return false;
                }

                form.submit();
                return true;
            }

            document.getElementById('imageUpload').addEventListener('change', async function(e) {
                const preview = document.getElementById('preview');
                const file = e.target.files[0];
                imageUploaded = false;
                
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
                        
                        if (!response.ok) {
                            throw new Error('Error en la respuesta del servidor');
                        }
                        
                        const data = await response.json();
                        
                        if (data.url) {
                            document.getElementById('imageUrl').value = data.url;
                            imageUploaded = true;
                        } else {
                            throw new Error(data.error || 'Error desconocido al subir la imagen');
                        }
                    } catch (error) {
                        console.error('Error:', error);
                        alert('Error al subir la imagen: ' + error.message);
                        e.target.value = '';
                        preview.style.display = 'none';
                        document.getElementById('imageUrl').value = '';
                    }
                }
            });
            </script>

            <!-- Lista de Artículos -->
            <div class="admin-articles">
                <div class="admin-table">
                    <div class="admin-table-header">
                        <div class="col-title">Título</div>
                        <div class="col-author">Autor</div>
                        <div class="col-date">Fecha</div>
                        <div class="col-actions">Acciones</div>
                    </div>
                    <div class="admin-table-body">
                        <% 
                            List<ArticleDTO> articles = (List<ArticleDTO>)request.getAttribute("articles");
                            if (articles != null && !articles.isEmpty()) {
                                for (ArticleDTO article : articles) {
                        %>
                        <div class="admin-table-row">
                            <div class="col-title"><%= article.getTitle() %></div>
                            <div class="col-author"><%= article.getAuthorName() %></div>
                            <div class="col-date">
                                <%= article.getPublicationDate() != null ? 
                                    new java.text.SimpleDateFormat("dd/MM/yyyy").format(article.getPublicationDate()) : 
                                    "No disponible" %>
                            </div>
                            <div class="col-actions">
                                <form action="admin" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="deleteArticle">
                                    <input type="hidden" name="id" value="<%= article.getId() %>">
                                    <button type="submit" class="admin-btn admin-btn-delete" 
                                            title="Eliminar"
                                            onclick="return confirm('¿Estás seguro de que deseas eliminar este artículo?')">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </form>
                            </div>
                        </div>
                        <% 
                                }
                            } else {
                        %>
                            <div class="admin-table-row">
                                <div class="col-title">No hay artículos disponibles.</div>
                            </div>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>

        <!-- Pestaña de Usuarios -->
        <div id="users-tab" class="tab-content">
            <div class="admin-create-form">
                <h2><i class="fas fa-user-plus"></i> Crear Nuevo Usuario</h2>
                <form action="${pageContext.request.contextPath}/admin" method="post">
                    <input type="hidden" name="action" value="createUser">
                    
                    <div class="form-group">
                        <input type="text" name="username" placeholder="Nombre de usuario" required>
                    </div>
                    
                    <div class="form-group">
                        <input type="password" name="password" placeholder="Contraseña" required>
                    </div>

                    <div class="form-group">
                        <label class="checkbox-label">
                            <input type="checkbox" name="isAdmin" value="true">
                            <span>Es Administrador</span>
                        </label>
                    </div>

                    <button type="submit" class="admin-btn admin-btn-create">
                        <i class="fas fa-plus"></i> Crear Usuario
                    </button>
                </form>
            </div>

            <!-- Lista de Usuarios -->
            <div class="admin-users">
                <div class="admin-table">
                    <div class="admin-table-header">
                        <div class="col-username">Usuario</div>
                        <div class="col-role">Rol</div>
                        <div class="col-actions">Acciones</div>
                    </div>
                    <div class="admin-table-body">
                        <% 
                            List<Customer> users = (List<Customer>)request.getAttribute("users");
                            if (users != null && !users.isEmpty()) {
                                for (Customer user : users) {
                        %>
                        <div class="admin-table-row">
                            <div class="col-username"><%= user.getUsername() %></div>
                            <div class="col-role"><%= user.getRole() %></div>
                            <div class="col-actions">
                                <form action="${pageContext.request.contextPath}/admin" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="deleteUser">
                                    <input type="hidden" name="id" value="<%= user.getId() %>">
                                    <button type="submit" class="admin-btn admin-btn-delete" 
                                            title="Eliminar"
                                            onclick="return confirm('¿Estás seguro de que deseas eliminar este usuario?')">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </form>
                            </div>
                        </div>
                        <% 
                                }
                            } else {
                        %>
                            <div class="admin-table-row">
                                <div class="col-username">No hay usuarios disponibles.</div>
                            </div>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        function showTab(tabName) {
            // Ocultar todas las pestañas
            document.querySelectorAll('.tab-content').forEach(tab => {
                tab.classList.remove('active');
            });
            
            // Desactivar todos los botones
            document.querySelectorAll('.tab-button').forEach(button => {
                button.classList.remove('active');
            });
            
            // Mostrar la pestaña seleccionada
            document.getElementById(tabName + '-tab').classList.add('active');
            
            // Activar el botón correspondiente
            const activeButton = Array.from(document.querySelectorAll('.tab-button')).find(
                button => button.onclick.toString().includes(tabName)
            );
            if (activeButton) {
                activeButton.classList.add('active');
            }
        }
    </script>
</body>
</html>
