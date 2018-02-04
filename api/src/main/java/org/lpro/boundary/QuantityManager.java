package org.lpro.boundary;

import java.util.UUID;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.lpro.entity.Quantity;

@Stateless
public class QuantityManager {

    @PersistenceContext
    EntityManager em;

    public Quantity findById(String id) {
        return this.em.find(Quantity.class, id);
    }
    
   public Quantity findByCommandeAndSandwich(String commande_id, String sandwich_id){
       TypedQuery<Quantity> query = em.createQuery("SELECT q FROM Quantity q WHERE q.commande_id = '" + commande_id +"' and q.sandwich_id = '"+sandwich_id+"'", Quantity.class);
       return query.getSingleResult();
   } 
   
   public Quantity findByCommandeAndSandwichAndTaille(String commande_id, String sandwich_id, String taille_id){
       TypedQuery<Quantity> query = em.createQuery("SELECT q FROM Quantity q WHERE q.commande_id = '" + commande_id +"' and q.sandwich_id = '"+sandwich_id+"' and q.taille_id = '" + taille_id + "'", Quantity.class);
       return query.getSingleResult();
   } 
   
   public void delete(Quantity q){
       try {
            Quantity ref = this.em.getReference(Quantity.class, q.getId());
            this.em.remove(ref);
        } catch (EntityNotFoundException e) { }
   }
   
   public Quantity save(Quantity q) {
        q.setId(UUID.randomUUID().toString());
        return this.em.merge(q);
    }
}
