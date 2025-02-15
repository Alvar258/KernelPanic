/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "SUGERENCIA", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Sugerencia implements Serializable {
    private static final long serialVersionUID = 1L;

    // Constructor sin parámetros
    public Sugerencia() {}

    @Id
    @SequenceGenerator(name="Sugerencia_Gen", sequenceName = "SUGERENCIA_GEN", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Sugerencia_Gen")
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateInitial;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFinished;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String emoji;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id", referencedColumnName = "id")
    private Municipio municipality;
    
    @Column(nullable = false)
    private int likes;

    private boolean processed;
    
    @Column(nullable = false)
    private int score_IA;

    @Column(nullable = false)
    private int score_final;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private int x;

    @Column(nullable = false)
    private int y;

    // Getters y Setters

    public Date getDateFinished() {
        return dateFinished;
    }

    public void setDateFinished(Date d) {
        this.dateFinished = d;
    }
    
    public Date getDateInitial() {
        return dateInitial;
    }

    public void setDateInitial(Date d) {
        this.dateInitial = d;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
    
    public Municipio getMunicipality() {
        return municipality;
    }

    public void setMunicipality(Municipio municipality) {
        this.municipality = municipality;
    }

    public int getScore_IA() {
        return score_IA;
    }

    public void setScore_IA(int score_IA) {
        this.score_IA = score_IA;
    }

    public int getScore_final() {
        return score_final;
    }

    public void setScore_final(int score_final) {
        this.score_final = score_final;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String s) {
        this.street = s;
    }
    
    public boolean getProcessed() {
        return processed;
    }

    public void setProcessed(boolean s) {
        this.processed = s;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Sugerencia)) {
            return false;
        }
        Sugerencia other = (Sugerencia) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.entities.Sugerencia[ id=" + id + " ]";
    }
}