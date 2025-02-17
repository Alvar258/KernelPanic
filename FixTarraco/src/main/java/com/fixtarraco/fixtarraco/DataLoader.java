package com.fixtarraco.fixtarraco;

import com.fixtarraco.fixtarraco.authn.Credentials;
import com.fixtarraco.fixtarraco.entities.Estado;
import com.fixtarraco.fixtarraco.entities.Incidencia;
import com.fixtarraco.fixtarraco.entities.Municipio;
import com.fixtarraco.fixtarraco.entities.Sugerencia;
import com.fixtarraco.fixtarraco.entities.TiposIncidencia;
import com.fixtarraco.fixtarraco.entities.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@Transactional  // Asegura que las operaciones se realicen en una transacción
public class DataLoader implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void run(String... args) throws Exception {
        // Crear un estado
        Estado estado = new Estado();
        estado.setName("Activo");
        em.persist(estado);
        Estado estado1 = new Estado();
        estado1.setName("Procesando");
        em.persist(estado1);
        Estado estado2 = new Estado();
        estado2.setName("Arreglado");
        em.persist(estado2);

        // Crear los municipios
        Municipio municipio = new Municipio();
        municipio.setName("Tarragona");
        em.persist(municipio);
        Municipio municipio2 = new Municipio();
        municipio2.setName("Altafulla");
        em.persist(municipio2);
        Municipio municipio3 = new Municipio();
        municipio3.setName("Constantí");
        em.persist(municipio3);
        Municipio municipio4 = new Municipio();
        municipio4.setName("Creixell");
        em.persist(municipio4);
        Municipio municipio5 = new Municipio();
        municipio5.setName("Perafort");
        em.persist(municipio5);
        Municipio municipio6 = new Municipio();
        municipio6.setName("Renau");
        em.persist(municipio6);
        Municipio municipio7 = new Municipio();
        municipio7.setName("Rode de Berà");
        em.persist(municipio7);
        Municipio municipio8 = new Municipio();
        municipio8.setName("Salomó");
        em.persist(municipio8);
        Municipio municipio9 = new Municipio();
        municipio9.setName("Salou");
        em.persist(municipio9);
        Municipio municipio10 = new Municipio();
        municipio10.setName("Torredembarra");
        em.persist(municipio10);
        Municipio municipio11 = new Municipio();
        municipio11.setName("Vespella de Gaià");
        em.persist(municipio11);
        Municipio municipio12 = new Municipio();
        municipio12.setName("Vila-seca");
        em.persist(municipio12);
        Municipio municipio13 = new Municipio();
        municipio13.setName("Vilallonga del Camp");
        em.persist(municipio13);

        // Crear tipo de incidencia "Infraestructura"
        TiposIncidencia tipoInfra = new TiposIncidencia();
        tipoInfra.setName("Infraestructura");
        em.persist(tipoInfra);

        // Crear tipo de incidencia "Vial"
        TiposIncidencia tipoVial = new TiposIncidencia();
        tipoVial.setName("Vial");
        em.persist(tipoVial);


        // Crear un usuario y sus credenciales
        Credentials cred = new Credentials();
        cred.setUsername("admin");
        cred.setPassword("admin123");
        em.persist(cred);

        Usuario usuario = new Usuario();
        usuario.setCityHall(true);
        usuario.setCredentials(cred);
        usuario.setImageURL("https://example.com/image.png");
        em.persist(usuario);

        // Crear una incidencia
        Incidencia incidencia = new Incidencia();
        incidencia.setDescription("Incidencia de prueba");
        incidencia.setEmoji(":)");
        incidencia.setLikes(0);
        incidencia.setMunicipality(municipio);
        incidencia.setScore_IA(50);
        incidencia.setScore_final(50);
        incidencia.setX(100);
        incidencia.setY(200);
        incidencia.setState(estado);
        incidencia.setStreet("Calle Principal");
        incidencia.setType(tipoInfra);
        incidencia.setDateInitial(new Date());
        incidencia.setDateFinished(new Date());
        em.persist(incidencia);

        // Crear una sugerencia
        Sugerencia sugerencia = new Sugerencia();
        sugerencia.setDescription("Sugerencia de prueba");
        sugerencia.setEmoji(":D");
        sugerencia.setLikes(0);
        sugerencia.setMunicipality(municipio);
        sugerencia.setScore_IA(30);
        sugerencia.setScore_final(30);
        sugerencia.setStreet("Calle Secundaria");
        sugerencia.setType("General");
        sugerencia.setX(150);
        sugerencia.setY(250);
        sugerencia.setDateInitial(new Date());
        sugerencia.setDateFinished(new Date());
        sugerencia.setProcessed(false);
        em.persist(sugerencia);
    }
}
