package org.lpro.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.NamedQuery;

@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedQuery(name="Taille.findAll",query="SELECT t FROM Taille t")
public class Taille implements Serializable
{

    @Id
    private String id;
    @NotNull
    private String nom;
    @NotNull
    private double prix;
    @ManyToMany
    private Set<Sandwich> sandwich = new HashSet<Sandwich>();

    public Taille() { }

    public Taille(String id, String nom, double prix) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public Set<Sandwich> getSandwich() {
        return sandwich;
    }

    public void setSandwich(Set<Sandwich> sandwich) {
        this.sandwich = sandwich;
    }
}
