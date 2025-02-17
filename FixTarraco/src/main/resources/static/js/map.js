document.addEventListener("DOMContentLoaded", function() {

    // Inicializa el mapa centrado en Tarragona (lat: 41.1189, lng: 1.2445)
    const map = L.map('map').setView([41.1189, 1.2445], 13);

    // Añadir capa de OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap'
    }).addTo(map);

    let currentLatLng = null;
    const popupForm = document.getElementById('popupForm');

    // Mapeo para convertir tipo numérico a texto (si fuera necesario)
    const typeMapping = {
        "1": "Infraestructura",
        "2": "Vial",
        "3": "Sugerencia"
    };

    // Función para determinar el color del marcador según el tipo
    function getMarkerColor(typeName) {
        if (typeName === 'Infraestructura') {
            return 'red';
        } else if (typeName === 'Vial') {
            return 'blue';
        } else if (typeName === 'Sugerencia') {
            return 'green';
        } else {
            return 'yellow';
        }
    }

    // Función para obtener el emoji según el tipo
    function getTypeEmoji(typeName) {
        if (typeName === 'Infraestructura') {
            return '🚧';
        } else if (typeName === 'Vial') {
            return '🛣️';
        } else if (typeName === 'Sugerencia') {
            return '💡';
        } else {
            return '';
        }
    }

    // Función para crear un icono personalizado para el marcador
    function createCustomMarkerIcon(typeName) {
        return L.divIcon({
            className: 'custom-marker',
            html: `<div style="background-color: ${getMarkerColor(typeName)}; width:24px; height:24px; border-radius:50%; text-align:center; line-height:24px; font-size:16px; color:white;">${getTypeEmoji(typeName)}</div>`,
            iconSize: [24, 24],
            iconAnchor: [12, 24]
        });
    }

    // Función para cargar incidencias del backend y mostrarlas en el mapa
    function loadIncidencias() {
        fetch('http://localhost:8080/rest/api/v1/incidencia')
            .then(response => response.json())
            .then(incidencias => {
                // Limpiar marcadores existentes (opcional, según tu lógica)
                // Recorremos cada incidencia y agregamos un marcador
                incidencias.forEach(incidencia => {
                    // Determinar el nombre del tipo
                    let typeName = "";
                    if (typeof incidencia.type === 'object' && incidencia.type.name) {
                        typeName = incidencia.type.name;
                    } else if (typeof incidencia.type === 'number' || typeof incidencia.type === 'string') {
                        typeName = typeMapping[incidencia.type] || incidencia.type;
                    } else {
                        typeName = incidencia.type;
                    }

                    const markerOptions = {
                        icon: createCustomMarkerIcon(typeName)
                    };

                    // Asegúrate de usar el orden [lat, lng]
                    const marker = L.marker([incidencia.y, incidencia.x], markerOptions).addTo(map);
                    let popupContent = `<b>${incidencia.description}</b><br>Tipo: ${typeName}`;
                    marker.bindPopup(popupContent);
                });
            })
            .catch(error => console.error("Error al cargar incidencias:", error));
    }

    // Llamar a la función para cargar incidencias al iniciar
    loadIncidencias();

    // Al hacer clic en el mapa, se muestra el formulario
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

    // Guardar marcador: se ejecuta al pulsar "Guardar"
    document.getElementById('saveMarker').addEventListener('click', function() {
        const token = localStorage.getItem('jwtToken');
        if (!token || token.trim() === "") {
            alert("Debe iniciar sesión para guardar incidencias.");
            popupForm.style.display = 'none';
            return;
        }

        const type = document.getElementById('markerType').value; // Asegúrate de incluir "Sugerencia" en el select si corresponde
        const description = document.getElementById('markerText').value.trim();
        if (!description) {
            alert("Por favor, ingrese una descripción.");
            return;
        }

        // Añadir marcador de feedback visual con icono personalizado
        const customIcon = createCustomMarkerIcon(type);
        const marker = L.marker(currentLatLng, { icon: customIcon }).addTo(map);
        marker.bindPopup(`<b>${description}</b><br>Tipo: ${type}`);

        // Determinar el endpoint según el tipo (si es "Sugerencia", se envía a sugerencias)
        let endpoint = 'http://localhost:8080/rest/api/v1/incidencia';
        if(type === "Sugerencia") {
            endpoint = 'http://localhost:8080/rest/api/v1/sugerencia';
        }

        // Usar reverse-geocoding con Nominatim para obtener datos de dirección
        fetch(`https://nominatim.openstreetmap.org/reverse?lat=${currentLatLng.lat}&lon=${currentLatLng.lng}&format=json`)
            .then(response => response.json())
            .then(data => {
                const address = data.address;
                let municipalityName = address.city || address.town || address.village || address.county;
                if (!municipalityName) {
                    alert("No se pudo determinar el municipio desde las coordenadas.");
                    return;
                }
                // Extraer calle, número y código postal
                let street = address.road || address.pedestrian || "Desconocido";
                let houseNumber = address.house_number || "";
                let postalCode = address.postcode || "";
                let fullStreet = street;
                if (houseNumber) {
                    fullStreet += " " + houseNumber;
                }
                if (postalCode) {
                    fullStreet += ", " + postalCode;
                }

                // Realizar la petición al backend para persistir la incidencia o sugerencia
                fetch(endpoint, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + token
                    },
                    body: JSON.stringify({
                        description: description,
                        emoji: getTypeEmoji(type),
                        likes: 0,
                        municipality: { name: municipalityName },
                        score_IA: 0,
                        score_final: 0,
                        x: currentLatLng.lng,
                        y: currentLatLng.lat,
                        state: { name: "Activo" },
                        street: fullStreet,
                        type: { name: type },
                        dateInitial: new Date()
                    })
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error("Error en la creación: " + response.statusText);
                        }
                        return response.text();
                    })
                    .then(data => {
                        console.log("Creado con ID:", data);
                        // Recargar los marcadores para mostrar la nueva incidencia/sugerencia
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
                console.error("Error en reverse-geocoding:", err);
                alert("Error al determinar la dirección.");
            });
    });

    // Botón Cancelar: Oculta el formulario sin añadir marcador
    document.getElementById('cancelMarker').addEventListener('click', function() {
        popupForm.style.display = 'none';
        document.getElementById('markerText').value = '';
        currentLatLng = null;
    });
});
