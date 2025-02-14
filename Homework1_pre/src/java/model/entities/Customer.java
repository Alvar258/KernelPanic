package model.entities;

import authn.Credentials;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToOne;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
//import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "CUSTOMER")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD) // Esto evita que tengas que anotar cada getter
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "Customer_Gen", sequenceName = "CUSTOMER_GEN", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Customer_Gen")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String role;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "customer", orphanRemoval = true)
    private Credentials credentials;

    // Relación con Artículos (si aplica)
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonbTransient
    private List<Article> articles;

    // Constructor sin parámetros
    public Customer() {}
    
    // Getters y Setters

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
        credentials.setCustomer(this);
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    // Es recomendable almacenar contraseñas en forma de hash
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public Optional<Article> getLatestArticle() {
        return articles.stream()
                .max((a1, a2) -> a1.getPublicationDate().compareTo(a2.getPublicationDate()));
    }

    // Métodos hashCode, equals y toString

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Customer)) {
            return false;
        }
        Customer other = (Customer) object;
        if ((this.id == null && other.id != null) || 
            (this.id != null && !this.id.equals(other.id))) {
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.entities.Customer[ id=" + id + " ]";
    }
}
