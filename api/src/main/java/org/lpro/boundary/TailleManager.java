package org.lpro.boundary;

import java.util.UUID;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.lpro.entity.Quantity;
import org.lpro.entity.Taille;

@Stateless
public class TailleManager {

    @PersistenceContext
    EntityManager em;

    public Taille findById(String id) {
        return this.em.find(Taille.class, id);
    }
    
   public Taille findByName(String name){
       TypedQuery<Taille> query = em.createQuery("SELECT t FROM Taille t WHERE t.nom = '" + name + "'", Taille.class);
       return query.getSingleResult();
   } 
   
   public Taille save(Taille t) {
        t.setId(UUID.randomUUID().toString());
        return this.em.merge(t);
    }
}
