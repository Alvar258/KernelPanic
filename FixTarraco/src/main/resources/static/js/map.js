document.addEventListener("DOMContentLoaded", function() {

    // Inicializa el mapa centrado en Tarragona
    const map = L.map('map').setView([41.1189, 1.2445], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap'
    }).addTo(map);

    let currentLatLng = null;
    const popupForm = document.getElementById('popupForm');

    // Se asume que en el HTML se tiene un <select id="markerType"> con opciones "Incidencia" y "Sugerencia"
    // Por ejemplo:
    // <select id="markerType">
    //    <option value="Incidencia">Incidencia</option>
    //    <option value="Sugerencia">Sugerencia</option>
    // </select>

    // Funciones para determinar color y emoji según la categoría
    function getMarkerColor(category) {
        const cat = category.toLowerCase();
        if (cat === 'infraestructura') {
            return 'red';
        } else if (cat === 'sugerencia') {
            return 'green';
        } else {
            return 'yellow';
        }
    }

    function getCategoryEmoji(category) {
        const cat = category.toLowerCase();
        if (cat === 'infraestructura') {
            return '🚧';
        } else if (cat === 'sugerencia') {
            return '💡';
        } else {
            return '';
        }
    }
// Función para parsear la respuesta de clasificación
    function parseClassificationResponse(rawText) {
        try {
            // Intentamos parsear directamente
            return JSON.parse(rawText);
        } catch (e) {
            // Si falla, limpiamos los delimitadores Markdown
            const cleaned = rawText.replace(/```json\s*/i, '').replace(/\s*```/g, '').trim();
            return JSON.parse(cleaned);
        }
    }

    function createCustomMarkerIcon(category) {
        return L.divIcon({
            className: 'custom-marker',
            html: `<div style="background-color: ${getMarkerColor(category)}; width:24px; height:24px; border-radius:50%; text-align:center; line-height:24px; font-size:16px; color:white;">${getCategoryEmoji(category)}</div>`,
            iconSize: [24, 24],
            iconAnchor: [12, 24]
        });
    }

    // Función para cargar incidencias desde el backend y mostrarlas en el mapa
    function loadIncidencias() {
        fetch('http://localhost:8080/rest/api/v1/incidencia')
            .then(response => response.json())
            .then(incidencias => {
                // Aquí se crean los marcadores para cada incidencia (se asume que el backend incluye "category", etc.)
                incidencias.forEach(incidencia => {
                    const category = incidencia.category || (incidencia.type && incidencia.type.name) || "Desconocido";
                    const marker = L.marker([incidencia.y, incidencia.x], {
                        icon: createCustomMarkerIcon(category)
                    }).addTo(map);
                    let popupContent = `<b>${incidencia.description}</b><br>Categoría: ${category}`;
                    if (typeof incidencia.priority_score !== 'undefined') {
                        popupContent += `<br>Puntuación IA: ${incidencia.priority_score}`;
                    }
                    marker.bindPopup(popupContent);
                });
            })
            .catch(error => console.error("Error al cargar incidencias:", error));
    }

    loadIncidencias();

    // Al hacer clic en el mapa, se muestra el formulario para crear un reporte
    map.on('click', function(e) {
        const token = localStorage.getItem('jwtToken');
        if (!token || token.trim() === "") {
            alert("Debe iniciar sesión para añadir incidencias o sugerencias.");
            return;
        }
        currentLatLng = e.latlng;
        popupForm.style.left = e.containerPoint.x + 'px';
        popupForm.style.top = e.containerPoint.y + 'px';
        popupForm.style.display = 'block';
    });

    // Al pulsar "Guardar" en el formulario
    document.getElementById('saveMarker').addEventListener('click', function() {
        const token = localStorage.getItem('jwtToken');
        if (!token || token.trim() === "") {
            alert("Debe iniciar sesión para guardar incidencias.");
            popupForm.style.display = 'none';
            return;
        }
        // Dentro del eventListener de "Guardar":
        const selectedType = document.getElementById('markerType').value;  // "Incidencia" o "Sugerencia"
        const description = document.getElementById('markerText').value.trim();
        if (!description) {
            alert("Por favor, ingrese una descripción.");
            return;
        }

        fetch(`https://nominatim.openstreetmap.org/reverse?lat=${currentLatLng.lat}&lon=${currentLatLng.lng}&format=json`)
            .then(response => response.json())
            .then(data => {
                const address = data.address;
                let municipalityName = address.city || address.town || address.village || address.county;
                if (!municipalityName) {
                    alert("No se pudo determinar el municipio desde las coordenadas.");
                    return;
                }
                let street = address.road || address.pedestrian || "Desconocido";
                let houseNumber = address.house_number || "";
                let postalCode = address.postcode || "";
                let fullStreet = street;
                if (houseNumber) fullStreet += " " + houseNumber;
                if (postalCode) fullStreet += ", " + postalCode;

                // Llamada a la API de clasificación (solo para "Incidencia")
                if (selectedType === "Incidencia") {
                    const classificationPayload = {
                        municipi: municipalityName,
                        incidencia: description
                    };

                    fetch('http://localhost:5001/clasificar', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        mode: 'cors',
                        body: JSON.stringify(classificationPayload)
                    })
                        .then(response => response.text())
                        .then(rawText => {
                            console.log("Raw classification response:", rawText);
                            let result;
                            try {
                                result = parseClassificationResponse(rawText);
                            } catch (e) {
                                console.error("Error al parsear la respuesta de clasificación:", e);
                                alert("Error al interpretar la respuesta de la IA.");
                                return;
                            }
                            console.log("Resultado de clasificación parseado:", result);
                            if (result.is_spam === true) {
                                alert("El reporte ha sido clasificado como spam. No se creará la incidencia.");
                                popupForm.style.display = 'none';
                                document.getElementById('markerText').value = '';
                                currentLatLng = null;
                                return;
                            }
                            const category = result.category || "Desconocido";
                            const priority_score = result.priority_score || 0;
                            if (category === "Desconocido") {
                                alert("La IA no pudo determinar correctamente la categoría. Respuesta: " + rawText);
                                // Puedes optar por cancelar o seguir con un valor por defecto.
                            }
                            // Endpoint para incidencias
                            const endpoint = 'http://localhost:8080/rest/api/v1/incidencia';
                            const incidentPayload = {
                                description: description,
                                emoji: getCategoryEmoji(category),
                                likes: 0,
                                municipality: { name: municipalityName },
                                score_IA: priority_score,
                                score_final: priority_score,
                                x: currentLatLng.lng,
                                y: currentLatLng.lat,
                                state: { name: "Activo" },
                                street: fullStreet,
                                type: { name: category },
                                dateInitial: new Date(),
                                category: category,
                                priority_score: priority_score
                            };
                            fetch(endpoint, {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json',
                                    'Authorization': 'Bearer ' + token
                                },
                                body: JSON.stringify(incidentPayload)
                            })
                                .then(response => {
                                    if (!response.ok) {
                                        throw new Error("Error en la creación: " + response.statusText);
                                    }
                                    return response.text();
                                })
                                .then(data => {
                                    console.log("Incidencia creada con ID:", data);
                                    loadIncidencias();
                                })
                                .catch(error => {
                                    console.error(error);
                                    alert("Error al guardar la incidencia: " + error.message);
                                });
                            popupForm.style.display = 'none';
                            document.getElementById('markerText').value = '';
                            currentLatLng = null;
                        })
                        .catch(err => {
                            console.error("Error al clasificar la incidencia:", err);
                            alert("Error al clasificar la incidencia.");
                        });
                } else {
                    // Rama para "Sugerencia" (sin llamar a la IA)
                    const endpoint = 'http://localhost:8080/rest/api/v1/sugerencia';
                    const incidentPayload = {
                        description: description,
                        emoji: getCategoryEmoji("Sugerencia"),
                        likes: 0,
                        municipality: { name: municipalityName },
                        score_IA: 0,
                        score_final: 0,
                        x: currentLatLng.lng,
                        y: currentLatLng.lat,
                        state: { name: "Activo" },
                        street: fullStreet,
                        type: { name: "Sugerencia" },
                        dateInitial: new Date(),
                        category: "Sugerencia",
                        priority_score: 0
                    };
                    fetch(endpoint, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': 'Bearer ' + token
                        },
                        body: JSON.stringify(incidentPayload)
                    })
                        .then(response => {
                            if (!response.ok) {
                                throw new Error("Error en la creación: " + response.statusText);
                            }
                            return response.text();
                        })
                        .then(data => {
                            console.log("Sugerencia creada con ID:", data);
                            loadIncidencias();
                        })
                        .catch(error => {
                            console.error(error);
                            alert("Error al guardar la sugerencia: " + error.message);
                        });
                    popupForm.style.display = 'none';
                    document.getElementById('markerText').value = '';
                    currentLatLng = null;
                }
            })
            .catch(err => {
                console.error("Error en reverse-geocoding:", err);
                alert("Error al determinar la dirección.");
            });

    });

    // Botón Cancelar: cierra el formulario sin crear nada
    document.getElementById('cancelMarker').addEventListener('click', function() {
        popupForm.style.display = 'none';
        document.getElementById('markerText').value = '';
        currentLatLng = null;
    });
});
