package model.entities;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import java.io.Serializable;

@Entity
@Table(name = "INCIDENT_TYPE", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class IncidentType implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public IncidentType() {}

    @Id
    @SequenceGenerator(name="IncidentType_Gen", sequenceName="INCIDENT_TYPE_GEN", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IncidentType_Gen")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
    
    // Getters y setters
    public Long getId() {
        return id;
    }
 
    public void setId(Long id) {
        this.id = id;
    }
 
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }
 
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof IncidentType)) {
            return false;
        }
        IncidentType other = (IncidentType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
 
    @Override
    public String toString() {
        return "model.entities.IncidentType[ id=" + id + ", name=" + name + " ]";
    }
}
