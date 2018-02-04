/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lpro.boundary;

import java.util.UUID;
import javax.ejb.Stateless;
import javax.mail.internet.HeaderTokenizer.Token;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.lpro.control.PasswordManagement;
import org.lpro.entity.CarteFidelite;

/**
 *
 * @author morgan
 */
@Stateless
public class CarteFideliteManager {
    @PersistenceContext
    EntityManager em;

    public CarteFidelite find(CarteFidelite carteFidelite) {
        CarteFidelite c;

        TypedQuery<CarteFidelite> query = this.em.createQuery("SELECT c FROM CarteFidelite c WHERE c.carte = '"+carteFidelite.getCarte()+"'", CarteFidelite.class);

        try {
            c = query.getSingleResult();
        } catch (NoResultException e) {
            c = null;
        }
        return c;
    }
    
    public CarteFidelite find(String carteFidelite) {
        CarteFidelite c;

        TypedQuery<CarteFidelite> query = this.em.createQuery("SELECT c FROM CarteFidelite c WHERE c.carte = '"+carteFidelite+"'", CarteFidelite.class);

        try {
            c = query.getSingleResult();
        } catch (NoResultException e) {
            c = null;
        }
        return c;
    }
    
    public void updateMontant(CarteFidelite cf, Double montant){
        cf.setMontant(cf.getMontant() + montant);
        this.em.merge(cf);
    }
    
    public CarteFidelite findByNomAndPrenom(CarteFidelite carteFidelite) {
        CarteFidelite c;

        TypedQuery<CarteFidelite> query = this.em.createQuery("SELECT c FROM CarteFidelite c WHERE c.nom = '"+carteFidelite.getNom()+"' AND c.prenom ='"+carteFidelite.getPrenom()+"'", CarteFidelite.class);

        try {
            c = query.getSingleResult();
        } catch (NoResultException e) {
            c = null;
        }
        return c;
    }

    public CarteFidelite createCarte(CarteFidelite carteFidelite) {
        carteFidelite.setCarte(new security.Token().generateRandomString());
        carteFidelite.setPassword(PasswordManagement.digestPassword(carteFidelite.getPassword()));
        carteFidelite.setId(UUID.randomUUID().toString());
        this.em.merge(carteFidelite);
        return carteFidelite;
    }
}
