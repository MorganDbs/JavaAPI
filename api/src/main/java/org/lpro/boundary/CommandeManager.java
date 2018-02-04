package org.lpro.boundary;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.lpro.entity.Commande;
import org.lpro.entity.Quantity;
import org.lpro.entity.Sandwich;
import org.lpro.entity.Taille;
import security.Token;

@Stateless
public class CommandeManager {

    @PersistenceContext
    EntityManager em;
    
    @Inject
    SandwichManager sm;
    
    @Inject
    QuantityManager qm;
    
    @Inject
    TailleManager tm;
    
    public List<Commande> findAll(String etat) {
        String query = "SELECT c FROM Commande c WHERE c.etat = '" + etat + "' ORDER BY c.date, c.created_at";
        Query q = this.em.createQuery(query);
        q.setHint("javax.persistence.cache.storeMode", CacheStoreMode.REFRESH);

        return q.getResultList();
    }


    public Commande findById(String id) {
        return this.em.find(Commande.class, id);
    }

    public List<Commande> findAll() {
        Query q = this.em.createNamedQuery("Commande.findAll", Commande.class);
        q.setHint("javax.persistance.cache.storeMode", CacheStoreMode.REFRESH);
        return q.getResultList();
    }

    public Commande save(Commande c) {
        c.setToken(new Token().generateRandomString());
        c.setId(UUID.randomUUID().toString());
        c.setEtat("attente");
        return this.em.merge(c);
    }
    public void update(Commande c){
     this.em.merge(c);
    }
    
    public Commande addSandwichToCommande(String commandeId, Sandwich s, Taille t, int quantity){
        Commande c = this.findById(commandeId);
        boolean flag;
        
        if(!c.getQuantity().isEmpty()){
            flag = c.getQuantity().stream().anyMatch(r -> {
                if(r.getSandwich_id().equals(s.getId()) && r.getTaille_id().equals(t.getId())){
                    return true;
                }else{
                    return false;
                }
            });
            
            if(!flag){
                Quantity q = this.qm.save(new Quantity(quantity, commandeId, s.getId(), t.getId()));
                c.getQuantity().add(q);
                this.em.persist(q);  
            }else{
                Quantity q = this.qm.findByCommandeAndSandwichAndTaille(commandeId, s.getId(), t.getId());
                q.setQuantity(q.getQuantity() + quantity);
                this.em.merge(q);
            }
        }else{
            Quantity q = this.qm.save(new Quantity(quantity, commandeId, s.getId(), t.getId()));
            c.getQuantity().add(q);
            this.em.persist(q);
        }
        
        
        return c;
    }
    
    public Commande deleteSandwichFromCommande(String commandeId, Sandwich s, Taille t){
        Commande c = this.findById(commandeId);
        Quantity q = this.qm.findByCommandeAndSandwichAndTaille(commandeId, s.getId(), t.getId());
        c.getQuantity().remove(q);
        this.em.merge(q);
        qm.delete(q);
        return c;
    }
    
    public Commande updateTaille(Quantity q, Taille t){
        Commande c = this.findById(q.getCommande_id());

        boolean flag;
        
        flag = c.getQuantity().stream().anyMatch(r -> {
                if(r.getTaille_id().equals(t.getId()) && !r.getId().equals(q.getId())){
                    return true;
                }else{
                    return false;
                }
            });
        
        if(flag){
            Quantity qq = this.qm.findByCommandeAndSandwichAndTaille(c.getId(), q.getSandwich_id(), t.getId());
            qq.setQuantity(qq.getQuantity() + q.getQuantity());
            this.em.merge(qq);  
            c.getQuantity().remove(q);
            this.em.merge(q);
            this.qm.delete(q);       

        }else{
            q.setTaille_id(t.getId());
            this.em.merge(q);
        }

        return c;
    }
    
    public Commande updateQuantity(Quantity q, int quantity){
        Commande c = this.findById(q.getCommande_id());
        q.setQuantity(quantity);
  
        this.em.merge(q);
        return c;
    }
    
    public Commande updateLivraison(Commande c, Timestamp ts){
        c.setDate(ts);
        this.em.merge(c);
        return c;
    }
    
    public Commande updateEtat(Commande c, String etat){
        c.setEtat(etat);
        this.em.merge(c);
        return c;
    }
    
    public Commande payerCommande(Commande c){
        c.setEtat("payer");
        this.em.merge(c);
        return c;
    }
    
    public Commande updateQuantityTaille(Quantity q,Taille t,int quantity ){
        Commande c = this.findById(q.getCommande_id());

        boolean flag;
        
        flag = c.getQuantity().stream().anyMatch(r -> {
                if(r.getTaille_id().equals(t.getId()) && !r.getId().equals(q.getId())){
                    return true;
                }else{
                    return false;
                }
            });
        
        if(flag){
            Quantity qq = this.qm.findByCommandeAndSandwichAndTaille(c.getId(), q.getSandwich_id(), t.getId());
            qq.setQuantity(qq.getQuantity() + quantity);
            this.em.merge(qq);  
            c.getQuantity().remove(q);
            this.em.merge(q);
            this.qm.delete(q);       

        }else{
            if(q.getTaille_id().equals(t.getId())){
                q.setQuantity(quantity);
                this.em.merge(q);
            }else{
                q.setTaille_id(t.getId());
                q.setQuantity(quantity);
                this.em.merge(q);
            }
        }

        return c;
    }

    public void delete(String id) {
        try {
            Commande ref = this.em.getReference(Commande.class, id);
            this.em.remove(ref);
        } catch (EntityNotFoundException e) { }
    }
}
