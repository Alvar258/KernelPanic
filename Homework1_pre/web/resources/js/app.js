// app.js
document.addEventListener("DOMContentLoaded", function() {
    // Animación de fade-in para la container
    const containers = document.querySelectorAll(".container");
    containers.forEach(container => {
        container.style.opacity = 0;
        container.style.transition = "opacity 1s";
        setTimeout(() => {
            container.style.opacity = 1;
        }, 100);
    });

    // Confirmación antes de eliminar
    const deleteButtons = document.querySelectorAll("a[href*='action=delete'] button");
    deleteButtons.forEach(btn => {
        btn.addEventListener("click", function(e) {
            if (!confirm("¿Estás seguro de que deseas eliminar este artículo?")) {
                e.preventDefault();
            }
        });
    });

    // Manejar la búsqueda de topics
    const topicSearch = document.getElementById('topicSearch');
    if (topicSearch) {
        topicSearch.addEventListener('input', function(e) {
            const query = e.target.value.toLowerCase();
            const topicChips = document.querySelectorAll('.topic-chip');
            topicChips.forEach(chip => {
                const topic = chip.querySelector('span').textContent.toLowerCase();
                if (topic.includes(query)) {
                    chip.style.display = 'flex';
                } else {
                    chip.style.display = 'none';
                }
            });
        });
    }

    // Manejar la selección de topics
    const topicChips = document.querySelectorAll('.topic-chip');
    topicChips.forEach(chip => {
        chip.addEventListener('click', function(e) {
            // Evitar que se active el checkbox al hacer clic en el label
            if (e.target.tagName === 'LABEL' || e.target.tagName === 'SPAN') {
                e.preventDefault();
            }
            
            const checkbox = this.querySelector('input[type="checkbox"]');
            checkbox.checked = !checkbox.checked;
            this.classList.toggle('selected', checkbox.checked);
        });
    });
});

function addTopic(topic) {
    const selectedTopics = document.getElementById('selectedTopics');
    const hiddenTopics = document.getElementById('hiddenTopics');
    
    // Verificar si el topic ya está seleccionado
    if (!document.querySelector(`.topic-badge[data-topic="${topic}"]`)) {
        // Crear el badge visual
        const badge = document.createElement('span');
        badge.className = 'topic-badge';
        badge.setAttribute('data-topic', topic);
        badge.innerHTML = `
            ${topic}
            <button type="button" class="remove-topic" onclick="removeTopic(this)">&times;</button>
        `;
        selectedTopics.appendChild(badge);

        // Crear el input oculto
        const hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = 'topic';
        hiddenInput.value = topic;
        hiddenTopics.appendChild(hiddenInput);
    }
}

function removeTopic(button) {
    const badge = button.parentElement;
    const topic = badge.getAttribute('data-topic');
    
    // Eliminar el badge visual
    badge.remove();
    
    // Eliminar el input oculto
    const hiddenInput = document.querySelector(`input[type="hidden"][value="${topic}"]`);
    if (hiddenInput) {
        hiddenInput.remove();
    }
}

function clearTopics() {
    const selectedTopics = document.getElementById('selectedTopics');
    const hiddenTopics = document.getElementById('hiddenTopics');
    selectedTopics.innerHTML = '';
    hiddenTopics.innerHTML = '';
}

function limitTopicSelection(checkbox, maxAllowed) {
    const checkedBoxes = document.querySelectorAll('input[name="topics"]:checked');
    if (checkedBoxes.length > maxAllowed) {
        checkbox.checked = false;
        checkbox.parentElement.classList.remove('selected');
        alert(`Solo puedes seleccionar ${maxAllowed} tópicos`);
    } else {
        checkbox.parentElement.classList.toggle('selected', checkbox.checked);
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const mobileMenu = document.getElementById('mobile-menu');
    const nav = document.getElementById('mainNav');

    if (mobileMenu) {
        mobileMenu.addEventListener('click', function() {
            nav.classList.toggle('active');
            mobileMenu.classList.toggle('open'); // Para animar el botón de menú
        });
    }
});

