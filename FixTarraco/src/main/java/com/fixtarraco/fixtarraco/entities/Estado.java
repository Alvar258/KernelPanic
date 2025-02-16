package com.fixtarraco.fixtarraco.entities;

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
@NamedQuery(name = "Estado.findState",
        query = "SELECT c FROM Estado c WHERE c.name = :state")
@Table(name = "STATE")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Estado implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "Estado_Gen", sequenceName = "ESTADO_GEN", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Estado_Gen")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "state", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonbTransient
    private List<Incidencia> incidencias = new ArrayList<>();

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

    // Método para añadir incidencia a la lista
    public void addIncidencia(Incidencia incidencia) {
        this.incidencias.add(incidencia);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
