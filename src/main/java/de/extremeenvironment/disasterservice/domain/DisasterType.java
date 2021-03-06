package de.extremeenvironment.disasterservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DisasterType.
 */
@Entity
@Table(name = "disaster_type")
public class DisasterType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "disasterType",fetch=FetchType.EAGER,cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<Disaster> disasters = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Disaster> getDisasters() {
        return disasters;
    }

    public void setDisasters(Set<Disaster> disasters) {
        this.disasters = disasters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DisasterType disasterType = (DisasterType) o;
        if(disasterType.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, disasterType.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "DisasterType{" +
            "id=" + id +
            ", name='" + name + "'" +
            '}';
    }
}
