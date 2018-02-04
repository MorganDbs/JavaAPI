package org.lpro.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedQuery(name="Sandwich.findAll",query="SELECT c FROM Sandwich c")
public class Sandwich implements Serializable{

    @Id
    private String id;
    @NotNull
    private String nom;
    @NotNull
    private String description;
    @NotNull
    private String type_pain;
    @NotNull
    private String img;
    
    @ManyToMany(mappedBy = "sandwich")
    private Set<Categorie> categ = new HashSet<Categorie>();
    
    @ManyToMany
    private Set<Taille> taille = new HashSet<Taille>();
    
    public Sandwich() {
    }

    public Sandwich(String nom, String description, String type_pain, String img) {
        this.nom = nom;
        this.description = description;
        this.type_pain =  type_pain;
        this.img = img;
    }
    
    public Set<Categorie> getCategorie() {
        return categ;
    }

    public void setCategorie(Set<Categorie> categorie) {
        this.categ = categorie;
    }
    
    public Set<Taille> getTaille() {
        return taille;
    }

    public void setTaille(Set<Taille> taille) {
        this.taille = taille;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getType_pain(){
        return type_pain;
    }
    
    public void setType_pain(String type_pain){
        this.type_pain = type_pain;
    }
    
    public String getImg(){
        return img;
    }
    
    public void setImg(String img){
        this.img = img;
    }
}
