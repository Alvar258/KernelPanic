package model.entities;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "INCIDENT")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Incident implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @SequenceGenerator(name = "Incident_Gen", sequenceName = "INCIDENT_GEN", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Incident_Gen")
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String description;
    
    private String imageUrl;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date incidentDate;
    
    @Column(nullable = false)
    private double latitude;
    
    @Column(nullable = false)
    private double longitude;
    
    // Relación con el autor (Customer)
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer author;
    
    // Nuevo: contador de likes
    @Column(nullable = false)
    private int likes;
    
    // Nuevo: tipo de incidencia
    @ManyToOne
    @JoinColumn(name = "incident_type_id")
    private IncidentType incidentType;
    
    // Nuevo: historial de fotos (validaciones)
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncidentPhoto> photos;
    
    public Incident() {
        // Inicializamos la fecha actual y los likes en 0
        this.incidentDate = new Date();
        this.likes = 0;
    }
    
    // Getters y Setters
    
    public Long getId() {
        return id;
    }
 
    public void setId(Long id) {
        this.id = id;
    }
 
    public String getTitle() {
        return title;
    }
 
    public void setTitle(String title) {
        this.title = title;
    }
 
    public String getDescription() {
        return description;
    }
 
    public void setDescription(String description) {
        this.description = description;
    }
 
    public String getImageUrl() {
        return imageUrl;
    }
 
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
 
    public Date getIncidentDate() {
        return incidentDate;
    }
 
    public void setIncidentDate(Date incidentDate) {
        this.incidentDate = incidentDate;
    }
 
    public double getLatitude() {
        return latitude;
    }
 
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
 
    public double getLongitude() {
        return longitude;
    }
 
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public Customer getAuthor() {
        return author;
    }
    
    public void setAuthor(Customer author) {
        this.author = author;
    }
    
    public int getLikes() {
        return likes;
    }
    
    public void setLikes(int likes) {
        this.likes = likes;
    }
    
    public IncidentType getIncidentType() {
        return incidentType;
    }
    
    public void setIncidentType(IncidentType incidentType) {
        this.incidentType = incidentType;
    }
    
    public List<IncidentPhoto> getPhotos() {
        return photos;
    }
    
    public void setPhotos(List<IncidentPhoto> photos) {
        this.photos = photos;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }
 
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Incident)) {
            return false;
        }
        Incident other = (Incident) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
 
    @Override
    public String toString() {
        return "model.entities.Incident[ id=" + id + " ]";
    }
}
