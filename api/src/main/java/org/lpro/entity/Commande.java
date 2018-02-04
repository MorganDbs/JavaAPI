/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lpro.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedQuery(name="Commande.findAll",query="SELECT c FROM Commande c")
public class Commande implements Serializable{
    
    @Id
    private String id;
    @NotNull
    private String nom_client;
    
    private String prenom_client;
    @NotNull
    private String mail_client;
    @NotNull
    private Timestamp date;
    
    private Timestamp created_at;
    private String etat;
    private String token;
    
    @ManyToMany
    private Set<Sandwich> sandwich = new HashSet<Sandwich>();
    @OneToMany
    private Set<Quantity> quantity = new HashSet<Quantity>();

    
    public Commande(){}
    
    public Commande(String nom_client, String prenom_client, String mail_client, Timestamp date){
        this.nom_client = nom_client;
        this.prenom_client = prenom_client;
        this.mail_client = mail_client;
        this.date = date;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getDefault());

        Date today = Date.from(LocalDateTime.now()
            .atZone(ZoneId.systemDefault())
            .toInstant());               
                    
        Timestamp today_stamp = new Timestamp(today.getTime());
        
        this.created_at = today_stamp;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom_client() {
        return nom_client;
    }

    public void setNom_client(String nom_client) {
        this.nom_client = nom_client;
    }
    
    public String getPrenom_client() {
        return prenom_client;
    }

    public void setPrenom_client(String prenom_client) {
        this.prenom_client = prenom_client;
    }
    public String getMail_client() {
        return mail_client;
    }

    public void setMail_client(String mail_client) {
        this.mail_client = mail_client;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }
    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Set<Sandwich> getSandwich() {
        return sandwich;
    }

    public void setSandwich(Set<Sandwich> sandwich) {
        this.sandwich = sandwich;
    }
    public Set<Quantity> getQuantity() {
        return quantity;
    }

    public void setQuantity(Set<Quantity> quantity) {
        this.quantity = quantity;
    }
}