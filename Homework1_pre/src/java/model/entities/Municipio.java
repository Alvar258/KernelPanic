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
import jakarta.persistence.ManyToMany;
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
@NamedQuery(name="Municipio.findMunicipality", 
            query="SELECT c FROM Municipio c WHERE c.name = :municipality")
@Table(name = "MUNICIPIO")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Municipio implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name="Municipio_Gen", sequenceName = "MUNICIPIO_GEN", allocationSize=1) // Els identificadors creixeran un a un
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Municipio_Gen") 
    private Long id;
    private String name;

    @OneToMany(mappedBy = "municipality", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)    
    @JsonbTransient
    private List<Incidencia> incidencias;
    
    @OneToMany(mappedBy = "municipality", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY) // "topics" es el nombre del atributo en Article
    @JsonbTransient
    private List<Sugerencia> sugerencias;
    
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
    
    public List<Sugerencia> getSugerencias() {
        return sugerencias;
    }

    public void setIncidencias(Sugerencia sugerencia) {
        sugerencias.add(sugerencia);
    }
    
}
