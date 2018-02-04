package org.lpro.boundary;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;

public class TestCommande {

    private Client client;
    private WebTarget target;
    private WebTarget targetSand;
    
    @Before
    public void initClient(){
        this.client = ClientBuilder.newClient();
        this.target = this.client.target(Serveur.serveurUrl() + "commandes");
    }
    
    @Test
    public void TestCommande() {
        // POST
        JsonObjectBuilder commande = Json.createObjectBuilder();
        JsonObject livraison = Json.createObjectBuilder()
                .add("date", "17-8-2018")
                .add("heure", "15:05")
                .build();
        JsonObject commandeJson = commande
                .add("nom_client", "dubois")
                .add("prenom_client","morgan")
                .add("mail_client","morgan@dubois.fr")
                .add("livraison", livraison)
                .build();
        Response categorieResponse = this.target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(commandeJson));
        String location = categorieResponse.getHeaderString("Location");
        assertThat(categorieResponse.getStatus(), is(201));
        location = location.replace("%3F", "?");
        String[] l = location.split("\\?");
        location = l[0]+"?details=1&"+l[1];
        String locationLivraison = l[0]+"/livraison?"+l[1];
        String locationSandwich = l[0]+"/sandwichs?"+l[1];
        String locationPayer = l[0]+"/payer?"+l[1];
        String locationEtat = l[0]+"/etat?"+l[1];
        String locationFacture = l[0]+"/facture?"+l[1];
        
        // PUT
        livraison = Json.createObjectBuilder()
                .add("date", "20-8-2018")
                .add("heure", "15:05")
                .build();
        
        commandeJson = commande
                .add("livraison", livraison)
                .build();
        categorieResponse = this.client
                .target(locationLivraison)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(commandeJson));
        assertThat(categorieResponse.getStatus(), is(200));
        
        // POST
        JsonObject commandeSandwichJson = commande
                .add("sandwich", "ab-fuga")
                .add("quantity", "4")
                .add("taille", "complet")
                .build();
        categorieResponse = this.client
                .target(locationSandwich)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(commandeSandwichJson));
        assertThat(categorieResponse.getStatus(), is(201));
        
        // GET
        JsonObject jsonRecupere = this.client
                .target(location)
                .request(MediaType.APPLICATION_JSON)
                .get(JsonObject.class);
        assertTrue(jsonRecupere.getJsonObject("commande").getString("nom_client").contains("dubois"));
        assertTrue(jsonRecupere.getJsonObject("commande").getString("prenom_client").contains("morgan"));
        assertTrue(jsonRecupere.getJsonObject("commande").getString("mail_client").contains("morgan@dubois.fr"));
        assertTrue(jsonRecupere.getJsonObject("commande").getJsonObject("livraison").getString("date").contains("2018-08-20"));
        assertTrue(jsonRecupere.getJsonObject("commande").getJsonArray("sandwichs").getJsonObject(0).getString("nom").contains("ab-fuga"));
        assertTrue(jsonRecupere.getJsonObject("commande").getJsonArray("sandwichs").getJsonObject(0).getString("taille").contains("complet"));
        assertTrue(jsonRecupere.getJsonObject("commande").containsKey("token"));
        
        // PUT
        JsonObject commandeSandwichJson2 = commande
                .add("id", jsonRecupere.getJsonObject("commande").getJsonArray("sandwichs").getJsonObject(0).getString("id_choix"))
                .add("quantity", "8")
                .add("taille", "ogre")
                .build();
        categorieResponse = this.client
                .target(locationSandwich)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(commandeSandwichJson2));
        assertThat(categorieResponse.getStatus(), is(200));
        
        // GET
        JsonObject jsonRecupere2 = this.client
                .target(location)
                .request(MediaType.APPLICATION_JSON)
                .get(JsonObject.class);
        assertTrue(jsonRecupere2.getJsonObject("commande").getString("nom_client").contains("dubois"));
        assertTrue(jsonRecupere2.getJsonObject("commande").getString("prenom_client").contains("morgan"));
        assertTrue(jsonRecupere2.getJsonObject("commande").getString("mail_client").contains("morgan@dubois.fr"));
        assertTrue(jsonRecupere2.getJsonObject("commande").getJsonObject("livraison").getString("date").contains("2018-08-20"));
        assertTrue(jsonRecupere2.getJsonObject("commande").getJsonArray("sandwichs").getJsonObject(0).getString("nom").contains("ab-fuga"));
        assertTrue(jsonRecupere2.getJsonObject("commande").getJsonArray("sandwichs").getJsonObject(0).getString("taille").contains("ogre"));
        assertTrue(jsonRecupere2.getJsonObject("commande").containsKey("token"));
        
        // DELETE on ne peut pas tester car on ne peut pas passer de body dans un delete en java
        JsonObject commandeSandwichJson3 = commande
                .add("sandwich", "ab-fuga")
                .add("taille", "ogre")
                .build();
        // PUT
        JsonObject commandeSandwichJson4 = commande
                .add("numero", "4641 1517 4230 3736")
                .add("date", "05-19")
                .add("cvv", "445")
                .build();
        categorieResponse = this.client
                .target(locationPayer)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(commandeSandwichJson4));
        assertThat(categorieResponse.getStatus(), is(200));
        
        // GET
        JsonObject jsonRecupere3 = this.client
                .target(location)
                .request(MediaType.APPLICATION_JSON)
                .get(JsonObject.class);
        assertTrue(jsonRecupere3.getJsonObject("commande").getString("etat").contains("payer"));
        
        // PUT
        JsonObject commandeSandwichJson5 = commande
                .add("etat", "livraison")
                .build();
        categorieResponse = this.client
                .target(locationEtat)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(commandeSandwichJson5));
        assertThat(categorieResponse.getStatus(), is(200));
        
        // GET
        JsonObject jsonRecupere4 = this.client
                .target(location)
                .request(MediaType.APPLICATION_JSON)
                .get(JsonObject.class);
        assertTrue(jsonRecupere4.getJsonObject("commande").getString("etat").contains("livraison"));
       
        // GET
        JsonObject jsonRecupere5 = this.client
                .target(locationFacture)
                .request(MediaType.APPLICATION_JSON)
                .get(JsonObject.class);
        assertTrue(jsonRecupere5.getJsonObject("facture").getString("nom_client").contains("dubois"));
    }
   
}
