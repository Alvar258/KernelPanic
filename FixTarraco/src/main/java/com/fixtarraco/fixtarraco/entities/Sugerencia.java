package com.fixtarraco.fixtarraco.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "SUGERENCIA", uniqueConstraints = @UniqueConstraint(columnNames = "description"))
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Sugerencia implements Serializable {

    private static final long serialVersionUID = 1L;

    public Sugerencia() {}

    @Id
    @SequenceGenerator(name = "Sugerencia_Gen", sequenceName = "SUGERENCIA_GEN", allocationSize = 1)
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
    private double x;

    @Column(nullable = false)
    private double y;

    // Getters y setters

    public Date getDateFinished() {
        return dateFinished;
    }

    public void setDateFinished(Date dateFinished) {
        this.dateFinished = dateFinished;
    }

    public Date getDateInitial() {
        return dateInitial;
    }

    public void setDateInitial(Date dateInitial) {
        this.dateInitial = dateInitial;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void setStreet(String street) {
        this.street = street;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    // equals y hashCode

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Sugerencia)) {
            return false;
        }
        Sugerencia other = (Sugerencia) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Sugerencia[ id=" + id + " ]";
    }
}
