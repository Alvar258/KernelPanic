/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.entities;

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
import java.util.List;


@Entity
@NamedQuery(name="Estado.findState", 
            query="SELECT c FROM Estado c WHERE c.name = :state")
@Table(name = "STATE")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Estado implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name="Estado_Gen", sequenceName = "ESTADO_GEN", allocationSize=1) // Els identificadors creixeran un a un
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Estado_Gen") 
    private Long id;
    private String name;
    
    @OneToMany(mappedBy = "state", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonbTransient
    private List<Incidencia> incidencias;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public List<Incidencia> getIncidencias() {
        return incidencias;
    }

    public void setIncidencias(Incidencia incidencia) {
        incidencias.add(incidencia);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
