document.addEventListener('DOMContentLoaded', function() {
    // Inicializa el mapa centrado en Tarragona usando MapTiler
    var map = L.map('map').setView([41.1189, 1.2445], 13);

    // Añade la capa base de MapTiler (streets-v2)
    L.tileLayer('https://api.maptiler.com/maps/streets-v2/{z}/{x}/{y}.png?key=peT7RgP9SKfoxuVPlrVr', {
        attribution: '<a href="https://www.maptiler.com/copyright/" target="_blank">MapTiler</a>',
        tileSize: 512,
        zoomOffset: -1,
        maxZoom: 19
    }).addTo(map);

    // Cargar incidencias desde el endpoint REST
    fetch(window.contextPath + '/IncidentController?action=getMapData')
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Error al cargar incidencias desde el servidor");
            }
            return response.json();
        })
        .then(function(incidencias) {
            incidencias.forEach(function(incidencia) {
                // Selecciona el icono según el estado (por defecto, icono estándar)
                let iconUrl = 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-icon.png';
                if (incidencia.status === 'pending') {
                    // Ejemplo: si la incidencia está pendiente, podrías usar otro icono (URL que tú proveas)
                    iconUrl = 'https://example.com/path/to/red-marker.png';
                } else if (incidencia.status === 'in process') {
                    // Ejemplo: si está en proceso, usa un icono amarillo
                    iconUrl = 'https://example.com/path/to/yellow-marker.png';
                }
                
                let customIcon = L.icon({
                    iconUrl: iconUrl,
                    iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-icon-2x.png',
                    shadowUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-shadow.png',
                    iconSize: [25, 41],
                    iconAnchor: [12, 41],
                    popupAnchor: [1, -34],
                    shadowSize: [41, 41]
                });
                
                let marker = L.marker([incidencia.latitude, incidencia.longitude], { icon: customIcon }).addTo(map);
                
                // Contenido del popup con información y botones interactivos
                let popupContent = `
                    <div style="text-align:center;">
                        <h3>${incidencia.title}</h3>
                        <p>${incidencia.description}</p>
                        <p><small>${incidencia.incidentDate}</small></p>
                        <p><strong>Likes:</strong> <span id="likes-${incidencia.id}">${incidencia.likes || 0}</span></p>
                        <button class="popup-btn" style="background-color:#007aff; color:#fff; border:none; border-radius:5px; padding:8px 12px; cursor:pointer; margin:5px;"
                            onclick="likeIncident(${incidencia.id})">
                            <i class="fas fa-thumbs-up"></i>
                        </button>
                        <button class="popup-btn" style="background-color:#34c759; color:#fff; border:none; border-radius:5px; padding:8px 12px; cursor:pointer; margin:5px;"
                            onclick="showValidationForm(${incidencia.id})">
                            <i class="fas fa-check"></i>
                        </button>
                        <div id="validationForm-${incidencia.id}" style="display:none; margin-top:10px;">
                            <input type="file" id="photo-${incidencia.id}" accept="image/*"><br>
                            <input type="date" id="photoDate-${incidencia.id}"><br>
                            <button class="popup-btn" style="background-color:#007aff; color:#fff; border:none; border-radius:5px; padding:5px 10px; cursor:pointer;"
                                onclick="submitValidation(${incidencia.id})">
                                Enviar Validación
                            </button>
                        </div>
                    </div>
                `;
                
                marker.bindPopup(popupContent);
            });
        })
        .catch(function(error) {
            console.error('Error al cargar incidencias:', error);
        });

    // Captura el evento click en el mapa para crear una nueva incidencia
    map.on('click', function(e) {
        var lat = e.latlng.lat;
        var lon = e.latlng.lng;
        var popupContent = `
            <div style="text-align:center;">
                <p style="margin-bottom:10px;">¿Crear incidencia en esta ubicación?</p>
                <button style="background-color:#007aff; color:#fff; border:none; border-radius:5px; padding:8px 12px; cursor:pointer; transition: background-color 0.3s ease, transform 0.3s ease;" 
                    onclick="redirectToNewIncident(${lat}, ${lon})">
                    Sí
                </button>
            </div>`;
        L.popup()
         .setLatLng(e.latlng)
         .setContent(popupContent)
         .openOn(map);
    });
});

// Función para redirigir al formulario de nueva incidencia con las coordenadas
function redirectToNewIncident(lat, lon) {
    var context = window.contextPath || "";
    window.location.href = context + "/IncidentController?action=new&lat=" + lat + "&lon=" + lon;
}
