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
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
//import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Entity
@NamedQueries({
    @NamedQuery(name="Usuario.findUser", 
                query="SELECT c FROM Usuario c WHERE c.credentials.username = :username"),
    @NamedQuery(name="Usuario.iscityHall", 
                query="SELECT c.cityHall FROM Usuario c WHERE c.credentials.username = :username")
})

@Table(name = "USUARIO")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD) // Esto evita que tengas que anotar cada getter
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "Usuario_Gen", sequenceName = "USUARIO_GEN", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Usuario_Gen")
    private Long id;

    @Column(nullable = false)
    private boolean cityHall;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "credentials_id", referencedColumnName = "id")
    private Credentials credentials;
    
    private String ImageURL;

    
    // Getters y Setters
    public boolean getCityHall() {
        return cityHall;
    }

    public void setCityHall(boolean c) {
        this.cityHall = c;
    }

    public Credentials getCredentials() {
        return credentials;
    }
    
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String image) {
        this.ImageURL = image;
    }
    
    // Constructor sin parámetros
    public Usuario() {}


    // Métodos hashCode, equals y toString

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Usuario)) {
            return false;
        }
        Usuario other = (Usuario) object;
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
