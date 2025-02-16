package com.fixtarraco.fixtarraco.service;

import com.fixtarraco.fixtarraco.authn.Credentials;
import com.fixtarraco.fixtarraco.authn.Secured;
import com.fixtarraco.fixtarraco.entities.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/rest/api/v1/usuario")
public class UsuarioFacadeREST {

    @PersistenceContext
    private EntityManager em;

    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsers() {
        TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u", Usuario.class);
        List<Usuario> users = query.getResultList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("{id}")
    @Secured
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Usuario updatedUser, Principal principal) {
        Usuario user = em.find(Usuario.class, id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }
        // Actualizar campos permitidos
        user.setCredentials(updatedUser.getCredentials());
        if (user.getCityHall()) {
            user.setCityHall(updatedUser.getCityHall());
        } else {
            updatedUser.setCityHall(user.getCityHall());
        }
        user.setImageURL(updatedUser.getImageURL());

        try {
            em.merge(user);
            return ResponseEntity.ok("Usuario actualizado correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el usuario.");
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Usuario user) {
        if (user.getCredentials().getUsername() == null || user.getCredentials().getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de usuario no puede estar vacío.");
        }
        if (user.getCredentials().getPassword() == null || user.getCredentials().getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("La contraseña no puede estar vacía.");
        }
        boolean cityHall = user.getCityHall();
        if (!cityHall) {
            return ResponseEntity.badRequest().body("El usuario no pertenece al ayuntamiento");
        }
        List<Credentials> existingCredentials = em.createQuery("SELECT c FROM Credentials c WHERE c.username = :username", Credentials.class)
                .setParameter("username", user.getCredentials().getUsername())
                .getResultList();
        if (!existingCredentials.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya existe");
        }
        try {
            em.persist(user);
            em.flush();
            // Crear y persistir las credenciales asociadas
            Credentials credentials = new Credentials();
            credentials.setUsername(user.getCredentials().getUsername());
            credentials.setPassword(user.getCredentials().getPassword());
            em.persist(credentials);

            URI uri = URI.create("/rest/api/v1/usuario/" + user.getId());
            return ResponseEntity.created(uri).body(user.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el usuario: " + e.getMessage());
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Autenticación requerida");
        }
        Usuario customer = em.find(Usuario.class, id);
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente no encontrado");
        }
        try {
            TypedQuery<Credentials> query = em.createQuery("SELECT c FROM Credentials c WHERE c.id = :customerId", Credentials.class);
            List<Credentials> credentials = query.setParameter("customerId", id).getResultList();
            for (Credentials cred : credentials) {
                em.remove(cred);
            }
            em.remove(customer);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el cliente: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credentials credentials) {
        try {
            TypedQuery<Credentials> query = em.createNamedQuery("Credentials.findUser", Credentials.class);
            Credentials storedCredentials = query.setParameter("username", credentials.getUsername()).getSingleResult();
            if (storedCredentials.getPassword().equals(credentials.getPassword())) {
                String token = com.fixtarraco.fixtarraco.authn.JWTUtil.generateToken(credentials.getUsername());
                return ResponseEntity.ok(token);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } catch (NoResultException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}
