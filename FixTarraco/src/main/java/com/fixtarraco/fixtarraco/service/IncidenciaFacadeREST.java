package com.fixtarraco.fixtarraco.service;

import com.fixtarraco.fixtarraco.authn.Secured;
import com.fixtarraco.fixtarraco.entities.Estado;
import com.fixtarraco.fixtarraco.entities.Incidencia;
import com.fixtarraco.fixtarraco.entities.Municipio;
import com.fixtarraco.fixtarraco.entities.TiposIncidencia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/rest/api/v1/incidencia")
public class IncidenciaFacadeREST {

    @PersistenceContext
    private EntityManager em;

    @GetMapping
    public ResponseEntity<List<Incidencia>> getAllIncidencias() {
        TypedQuery<Incidencia> query = em.createQuery("SELECT a FROM Incidencia a", Incidencia.class);
        List<Incidencia> incidencias = query.getResultList();
        return ResponseEntity.ok(incidencias);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getIncidencia(@PathVariable Long id, Principal principal) {
        try {
            Incidencia incidencia = em.find(Incidencia.class, id);
            if (incidencia == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Artículo no encontrado.");
            }
            return ResponseEntity.ok(incidencia);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    @DeleteMapping("{id}")
    // Puedes aplicar aquí anotaciones de seguridad de Spring (por ejemplo, @PreAuthorize)
    public ResponseEntity<?> deleteIncidencia(@PathVariable Long id, Principal principal) {
        Incidencia incidencia = em.find(Incidencia.class, id);
        if (incidencia == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Incidencia no encontrada");
        }
        try {
            em.remove(incidencia);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la incidencia.");
        }
    }

    @PostMapping
    @Secured
    @Transactional
    public ResponseEntity<?> createIncidencia(@RequestBody Incidencia incidencia, Principal principal) {
        // Validar y obtener el Estado mediante una consulta segura
        Estado state;
        try {
            TypedQuery<Estado> query2 = em.createNamedQuery("Estado.findState", Estado.class);
            state = query2.setParameter("state", incidencia.getState().getName()).getSingleResult();
        } catch (jakarta.persistence.NoResultException ex) {
            return ResponseEntity.badRequest().body("Estado no encontrado. Introduce un estado válido.");
        }

        // Validar y obtener el Municipio
        Municipio municipio;
        try {
            TypedQuery<Municipio> query3 = em.createNamedQuery("Municipio.findMunicipality", Municipio.class);
            municipio = query3.setParameter("municipality", incidencia.getMunicipality().getName()).getSingleResult();
        } catch (jakarta.persistence.NoResultException ex) {
            return ResponseEntity.badRequest().body("Municipio no encontrado. Introduce un municipio válido.");
        }

        // Validar y obtener el Tipo de incidencia
        TiposIncidencia type;
        try {
            TypedQuery<TiposIncidencia> query4 = em.createNamedQuery("TiposIncidencia.findType", TiposIncidencia.class);
            type = query4.setParameter("type", incidencia.getType().getName()).getSingleResult();
        } catch (jakarta.persistence.NoResultException ex) {
            return ResponseEntity.badRequest().body("Tipo de incidencia no encontrado. Introduce un tipo válido.");
        }

        // Opcionalmente, puedes asignar los objetos obtenidos a la incidencia para asegurar la integridad:
        incidencia.setState(state);
        incidencia.setMunicipality(municipio);
        incidencia.setType(type);

        try {
            em.persist(incidencia);
            em.flush(); // Para que se genere el ID y se garantice la persistencia
            URI uri = URI.create("/rest/api/v1/incidencia/" + incidencia.getId());
            return ResponseEntity.created(uri).body(incidencia.getId().toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la incidencia: " + e.getMessage());
        }
    }


    @PutMapping("{id}")
    // @Secured
    public ResponseEntity<?> updateIncidencia(@PathVariable Long id, @RequestBody Incidencia updatedIncidencia, Principal principal) {
        Incidencia incidencia2 = em.find(Incidencia.class, id);
        if (incidencia2 == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Incidencia no encontrada.");
        }

        TypedQuery<Estado> query2 = em.createNamedQuery("Estado.findState", Estado.class);
        Estado state = query2.setParameter("state", updatedIncidencia.getState().getName()).getSingleResult();
        if (state == null) {
            return ResponseEntity.badRequest().body("Introduce un estado valido");
        }
        TypedQuery<Municipio> query3 = em.createNamedQuery("Municipio.findMunicipality", Municipio.class);
        Municipio municipio = query3.setParameter("municipality", updatedIncidencia.getMunicipality().getName()).getSingleResult();
        if (municipio == null) {
            return ResponseEntity.badRequest().body("Introduce un municipio valido");
        }
        TypedQuery<TiposIncidencia> query4 = em.createNamedQuery("TiposIncidencia.findType", TiposIncidencia.class);
        TiposIncidencia type = query4.setParameter("type", updatedIncidencia.getType().getName()).getSingleResult();
        if (type == null) {
            return ResponseEntity.badRequest().body("Introduce un tipo valido");
        }

        // Actualizar los campos permitidos
        incidencia2.setDescription(updatedIncidencia.getDescription());
        incidencia2.setEmoji(updatedIncidencia.getEmoji());
        incidencia2.setScore_IA(updatedIncidencia.getScore_IA());
        incidencia2.setScore_final(updatedIncidencia.getScore_final());
        incidencia2.setStreet(updatedIncidencia.getStreet());
        incidencia2.setType(updatedIncidencia.getType());
        incidencia2.setLikes(updatedIncidencia.getLikes());
        incidencia2.setX(updatedIncidencia.getX());
        incidencia2.setY(updatedIncidencia.getY());
        incidencia2.setDateFinished(updatedIncidencia.getDateFinished());
        incidencia2.setDateInitial(updatedIncidencia.getDateInitial());
        incidencia2.setState(updatedIncidencia.getState());
        incidencia2.setMunicipality(updatedIncidencia.getMunicipality());

        em.merge(incidencia2);
        return ResponseEntity.ok().build();
    }
}
