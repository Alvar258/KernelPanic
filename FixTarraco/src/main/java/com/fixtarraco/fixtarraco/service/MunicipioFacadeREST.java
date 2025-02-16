package com.fixtarraco.fixtarraco.service;

import com.fixtarraco.fixtarraco.entities.Municipio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/api/v1/municipality")
public class MunicipioFacadeREST {

    @PersistenceContext
    private EntityManager em;

    @GetMapping
    public ResponseEntity<List<Municipio>> getAllMunicipality() {
        TypedQuery<Municipio> query = em.createQuery("SELECT u FROM Municipio u", Municipio.class);
        List<Municipio> municipios = query.getResultList();
        return ResponseEntity.ok(municipios);
    }
}
