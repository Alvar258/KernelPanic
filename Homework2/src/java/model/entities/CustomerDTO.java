package model.entities;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomerDTO {
    private Long id;
    private String username;
    private String role;
    private Map<String, String> links = new HashMap<>();

    public CustomerDTO(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }
    
    public CustomerDTO() {}

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
 
    public String getRole() {
        return role;
    }
 
    public void setRole(String role) {
        this.role = role;
    }
 
    public Map<String, String> getLinks() {
        return links;
    }
 
    public void setLinks(Map<String, String> links) {
        this.links = links;
    }
 
    public void addLink(String rel, String href) {
        this.links.put(rel, href);
    }
}
