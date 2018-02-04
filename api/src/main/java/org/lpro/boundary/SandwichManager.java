package org.lpro.boundary;

import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.lpro.entity.Categorie;
import org.lpro.entity.Sandwich;

@Stateless
public class SandwichManager {

    @PersistenceContext
    EntityManager em;

    public Sandwich findById(String id) {
        return this.em.find(Sandwich.class, id);
    }
    
   public Sandwich findByName(String name){
       TypedQuery<Sandwich> query = em.createQuery("SELECT s FROM Sandwich s WHERE s.nom = '" + name +"'", Sandwich.class);
       return query.getSingleResult();
   } 
    
    public List<Sandwich> findParams(String pain, String img, String page, String size) {
        int page_int = Integer.parseInt(page);
        int size_int = Integer.parseInt(size);
        int img_int = Integer.parseInt(img);
        
        String queryString = "SELECT s FROM Sandwich s";
        
        if(pain==null){
            if (img_int == 1) {
                queryString += " WHERE s.img != ''";
            }
        }else{
             if (img_int == 1) {
                queryString += " WHERE s.type_pain = '" + pain + "' AND s.img != ''";
            } else {
                queryString += " WHERE s.type_pain = '" + pain + "'";
            }
        }
        
        Query query = this.em.createQuery(queryString);
        
        double total = query.getResultList().size();

        if (page_int <= 0) {
            page_int = 1;
        }
        else if (page_int > Math.ceil(total / (double) size_int)) {
            page_int = (int) Math.ceil(total / (double) size_int);
        }

        query.setFirstResult((page_int-1) * size_int);
        query.setMaxResults(size_int);
        
        return query.getResultList();
    }
    
    public Sandwich addSandwich(String catId, String sandId) {
        Sandwich s = this.findById(sandId);
        Categorie c = this.em.find(Categorie.class, catId);
        c.getSandwich().add(s);
        this.em.persist(c);
        return s;
    }
     
    public List<Sandwich> findAll() {
        Query q = this.em.createNamedQuery("Sandwich.findAll", Sandwich.class);
        q.setHint("javax.persistence.cache.storeMode", CacheStoreMode.REFRESH);
        return q.getResultList();
    }
    

    public Sandwich save(Sandwich s) {
        s.setId(UUID.randomUUID().toString());
        return this.em.merge(s);
    }
    
    public Sandwich update(Sandwich s) {
        return this.em.merge(s);
    }
    
    public void delete(String id) {
        try {
            Sandwich ref = this.em.getReference(Sandwich.class, id);
            this.em.remove(ref);
        } catch (EntityNotFoundException enfe) {
            // rien à faire   
        }
    }
}
