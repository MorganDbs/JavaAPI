/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lpro.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import static org.eclipse.persistence.sessions.remote.corba.sun.TransporterHelper.id;

/**
 *
 * @author morgan
 */

@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NamedQuery(name="Quantity.findAll",query="SELECT q FROM Quantity q")
public class Quantity implements Serializable{
    @Id
    private String id;
    
    private int quantity;
    
    private String commande_id;
    
    private String sandwich_id;
    
    private String taille_id;
    
    public Quantity(){}
    
    public Quantity(int quantity, String commande_id, String sandwich_id, String taille_id){
        this.quantity= quantity;
        this.commande_id = commande_id;
        this.sandwich_id = sandwich_id;
        this.taille_id = taille_id;
    }

    public String getTaille_id() {
        return taille_id;
    }

    public void setTaille_id(String taille_id) {
        this.taille_id = taille_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCommande_id() {
        return commande_id;
    }

    public void setCommande_id(String commande_id) {
        this.commande_id = commande_id;
    }

    public String getSandwich_id() {
        return sandwich_id;
    }

    public void setSandwich_id(String sandwich_id) {
        this.sandwich_id = sandwich_id;
    }
    
}
