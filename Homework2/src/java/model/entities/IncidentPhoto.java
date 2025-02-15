package model.entities;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "INCIDENT_PHOTO")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class IncidentPhoto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @SequenceGenerator(name="IncidentPhoto_Gen", sequenceName="INCIDENT_PHOTO_GEN", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IncidentPhoto_Gen")
    private Long id;
    
    @Column(nullable = false)
    private String photoUrl;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date photoDate;
    
    // Relación con la incidencia
    @ManyToOne
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;
    
    public IncidentPhoto() {
        this.photoDate = new Date();
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
 
    public void setId(Long id) {
        this.id = id;
    }
 
    public String getPhotoUrl() {
        return photoUrl;
    }
 
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
 
    public Date getPhotoDate() {
        return photoDate;
    }
 
    public void setPhotoDate(Date photoDate) {
        this.photoDate = photoDate;
    }
 
    public Incident getIncident() {
        return incident;
    }
 
    public void setIncident(Incident incident) {
        this.incident = incident;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }
 
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof IncidentPhoto)) {
            return false;
        }
        IncidentPhoto other = (IncidentPhoto) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
 
    @Override
    public String toString() {
        return "model.entities.IncidentPhoto[ id=" + id + " ]";
    }
}
