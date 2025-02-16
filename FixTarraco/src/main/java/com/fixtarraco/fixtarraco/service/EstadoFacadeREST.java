package com.fixtarraco.fixtarraco.service;

import com.fixtarraco.fixtarraco.entities.Estado;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/api/v1/state")
public class EstadoFacadeREST {

    @PersistenceContext
    private EntityManager em;

    @GetMapping
    public ResponseEntity<List<Estado>> getAllState() {
        TypedQuery<Estado> query = em.createQuery("SELECT u FROM Estado u", Estado.class);
        List<Estado> estados = query.getResultList();
        return ResponseEntity.ok(estados);
    }
}
