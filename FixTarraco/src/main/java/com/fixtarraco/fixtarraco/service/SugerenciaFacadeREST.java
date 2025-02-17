package com.fixtarraco.fixtarraco.service;

import com.fixtarraco.fixtarraco.authn.Secured;
import com.fixtarraco.fixtarraco.entities.Municipio;
import com.fixtarraco.fixtarraco.entities.Sugerencia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/rest/api/v1/sugerencia")
public class SugerenciaFacadeREST {

    @PersistenceContext
    private EntityManager em;

    @GetMapping
    public ResponseEntity<List<Sugerencia>> getAllSugerencias() {
        TypedQuery<Sugerencia> query = em.createQuery("SELECT a FROM Sugerencia a", Sugerencia.class);
        List<Sugerencia> sugerencias = query.getResultList();
        return ResponseEntity.ok(sugerencias);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getSugerencia(@PathVariable Long id, Principal principal) {
        try {
            Sugerencia sugerencia = em.find(Sugerencia.class, id);
            if (sugerencia == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Artículo no encontrado.");
            }
            return ResponseEntity.ok(sugerencia);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    @DeleteMapping("{id}")
    @Secured
    public ResponseEntity<?> deleteSugerencia(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Autenticación requerida");
        }
        Sugerencia sugerencia = em.find(Sugerencia.class, id);
        if (sugerencia == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Sugerencia no encontrada");
        }
        try {
            em.remove(sugerencia);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la sugerencia.");
        }
    }

    @PostMapping
    @Secured
    public ResponseEntity<?> createSugerencia(@RequestBody Sugerencia sugerencia, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Autenticación requerida");
        }
        // Buscar el Municipio a partir del nombre enviado en la sugerencia
        TypedQuery<Municipio> query2 = em.createNamedQuery("Municipio.findMunicipality", Municipio.class);
        Municipio municipio = query2.setParameter("municipality", sugerencia.getMunicipality().getName()).getSingleResult();
        if (municipio == null) {
            return ResponseEntity.badRequest().body("Introduce un municipio válido");
        }
        // Asignar el Municipio obtenido a la sugerencia para que esté gestionado por JPA
        sugerencia.setMunicipality(municipio);
        try {
            em.persist(sugerencia);
            em.flush();
            URI uri = URI.create("/rest/api/v1/sugerencia/" + sugerencia.getId());
            return ResponseEntity.created(uri).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la sugerencia: " + e.getMessage());
        }
    }



    @PutMapping("{id}")
    @Secured
    public ResponseEntity<?> updateSugerencia(@PathVariable Long id, @RequestBody Sugerencia updatedSugerencia, Principal principal) {
        Sugerencia sugerencia2 = em.find(Sugerencia.class, id);
        if (sugerencia2 == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sugerencia no encontrada.");
        }
        TypedQuery<Municipio> query2 = em.createNamedQuery("Municipio.findMunicipality", Municipio.class);
        Municipio municipio = query2.setParameter("municipality", updatedSugerencia.getMunicipality().getName()).getSingleResult();
        if (municipio == null) {
            return ResponseEntity.badRequest().body("Introduce un municipio valido");
        }
        // Actualizar los campos permitidos
        sugerencia2.setDescription(updatedSugerencia.getDescription());
        sugerencia2.setX(updatedSugerencia.getX());
        sugerencia2.setY(updatedSugerencia.getY());
        sugerencia2.setLikes(updatedSugerencia.getLikes());
        sugerencia2.setScore_IA(updatedSugerencia.getScore_IA());
        sugerencia2.setScore_final(updatedSugerencia.getScore_final());
        sugerencia2.setStreet(updatedSugerencia.getStreet());
        // Usamos isProcessed() en lugar de getProcessed()
        sugerencia2.setProcessed(updatedSugerencia.isProcessed());
        sugerencia2.setMunicipality(updatedSugerencia.getMunicipality());
        sugerencia2.setDateFinished(updatedSugerencia.getDateFinished());
        sugerencia2.setDateInitial(updatedSugerencia.getDateInitial());

        em.merge(sugerencia2);
        return ResponseEntity.ok().build();
    }
}

