package com.fixtarraco.fixtarraco.authn;

import java.io.Serializable;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;
import com.fixtarraco.fixtarraco.entities.Usuario;

@Entity
@NamedQuery(name="Credentials.findUser",
        query="SELECT c FROM Credentials c WHERE c.username = :username")
@XmlRootElement
public class Credentials implements Serializable {
    @Id
    @SequenceGenerator(name = "Credentials_Gen", sequenceName = "CREDENTIALS_GEN", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Credentials_Gen")
    private Long id;

    @Transient
    private String jwtToken;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String username;

    public Credentials() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String generateJwtToken() {
        this.jwtToken = JWTUtil.generateToken(this.username);
        return this.jwtToken;
    }
}
