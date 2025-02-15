package model.entities;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import java.util.Date;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class IncidentDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private Date incidentDate;
    private double latitude;
    private double longitude;
    
    // Nuevos campos
    private int likes;
    private String incidentType; // Nombre del tipo
    private List<String> photoUrls; // Lista de URLs de las fotos

    public IncidentDTO() {}

    public IncidentDTO(Long id, String title, String description, String imageUrl, Date incidentDate,
                       double latitude, double longitude, int likes, String incidentType, List<String> photoUrls) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.incidentDate = incidentDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.likes = likes;
        this.incidentType = incidentType;
        this.photoUrls = photoUrls;
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
 
    public int getLikes() {
        return likes;
    }
 
    public void setLikes(int likes) {
        this.likes = likes;
    }
 
    public String getIncidentType() {
        return incidentType;
    }
 
    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }
 
    public List<String> getPhotoUrls() {
        return photoUrls;
    }
 
    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }
}
