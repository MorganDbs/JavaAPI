/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lpro.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author morgan
 */
@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CarteFidelite {
    
    @Id
    private String id;
    
    private String carte;
    
    private String nom;

    private String prenom;

    private String password;

    private double montant;
    
    private double reduction;
    
    public CarteFidelite() { }
    
    public CarteFidelite(String carte, String password) {
        this.carte = carte;
        this.password = password;
    }
    
    public CarteFidelite(String nom, String prenom, String password) {
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getPassword() {
        return password;
    }

    public String getCarte() {
        return carte;
    }

    public void setCarte(String carte) {
        this.carte = carte;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public double getReduction() {
        return reduction;
    }

    public void setReduction(double reduction) {
        this.reduction = reduction;
    }
}
