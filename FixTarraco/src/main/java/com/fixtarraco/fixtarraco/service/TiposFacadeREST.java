package com.fixtarraco.fixtarraco.service;

import com.fixtarraco.fixtarraco.entities.TiposIncidencia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/api/v1/types")
public class TiposFacadeREST {

    @PersistenceContext
    private EntityManager em;

    @GetMapping
    public ResponseEntity<List<TiposIncidencia>> getAllTypes() {
        TypedQuery<TiposIncidencia> query = em.createQuery("SELECT u FROM TiposIncidencia u", TiposIncidencia.class);
        List<TiposIncidencia> tipos = query.getResultList();
        return ResponseEntity.ok(tipos);
    }
}
