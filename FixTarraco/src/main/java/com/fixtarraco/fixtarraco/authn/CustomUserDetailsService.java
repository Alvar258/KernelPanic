package com.fixtarraco.fixtarraco.authn;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @PersistenceContext
    private EntityManager em;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TypedQuery<Credentials> query = em.createNamedQuery("Credentials.findUser", Credentials.class);
        try {
            Credentials credentials = query.setParameter("username", username).getSingleResult();
            // Aquí asignamos ROLE_USER (puedes ajustar según tu lógica)
            return new User(credentials.getUsername(),
                    credentials.getPassword(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        } catch (NoResultException e) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}
