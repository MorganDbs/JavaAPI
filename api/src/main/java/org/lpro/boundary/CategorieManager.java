package org.lpro.boundary;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.lpro.entity.Categorie;
import org.lpro.entity.Sandwich;

@Stateless
public class CategorieManager {

    @PersistenceContext
    EntityManager em;

    public Categorie findById(String id) {
        return this.em.find(Categorie.class, id);
    }

    public List<Categorie> findAll() {
        Query q = this.em.createNamedQuery("Categorie.findAll", Categorie.class);
        q.setHint("javax.persistance.cache.storeMode", CacheStoreMode.REFRESH);
        return q.getResultList();
    }

    public Categorie save(Categorie c) {
        c.setId(UUID.randomUUID().toString());
        c.setSandwich(new HashSet<Sandwich>());
        return this.em.merge(c);
    }
    
    public Categorie update(Categorie c) {
        return this.em.merge(c);
    }

    public void delete(String id) {
        try {
            Categorie ref = this.em.getReference(Categorie.class, id);
            this.em.remove(ref);
        } catch (EntityNotFoundException e) { }
    }
}
