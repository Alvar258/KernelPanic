package model.entities;

import java.util.Date;

public class IncidentDTO {
    
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private Date incidentDate;
    private double latitude;
    private double longitude;
    
    // Constructor sin parámetros
    public IncidentDTO() {
    }
    
    // Getters y setters
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
    
    @Override
    public String toString() {
        return "IncidentDTO [id=" + id + ", title=" + title + ", description=" + description 
                + ", imageUrl=" + imageUrl + ", incidentDate=" + incidentDate 
                + ", latitude=" + latitude + ", longitude=" + longitude + "]";
    }
}
