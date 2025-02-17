package com.fixtarraco.fixtarraco.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@NamedQuery(name = "Municipio.findMunicipality",
        query = "SELECT c FROM Municipio c WHERE c.name = :municipality")
@Table(name = "MUNICIPIO")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
// Evita serializar la lista de incidencias
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
@JsonIgnoreProperties({"incidencias", "sugerencias", "hibernateLazyInitializer", "handler"})

public class Municipio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "Municipio_Gen", sequenceName = "MUNICIPIO_GEN", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Municipio_Gen")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "municipality", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonbTransient
    private List<Incidencia> incidencias = new ArrayList<>();

    @OneToMany(mappedBy = "municipality", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonbTransient
    private List<Sugerencia> sugerencias = new ArrayList<>();

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Incidencia> getIncidencias() {
        return incidencias;
    }

    // Método para añadir incidencia
    public void addIncidencia(Incidencia incidencia) {
        this.incidencias.add(incidencia);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Sugerencia> getSugerencias() {
        return sugerencias;
    }

    // Método para añadir sugerencia
    public void addSugerencia(Sugerencia sugerencia) {
        this.sugerencias.add(sugerencia);
    }
}
