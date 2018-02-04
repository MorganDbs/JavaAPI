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

public class TestCategorie {

    private Client client;
    private WebTarget target;
    
    @Before
    public void initClient(){
        this.client = ClientBuilder.newClient();
        this.target = this.client.target(Serveur.serveurUrl() + "categories");
    }
    
    @Test
    public void testCategorie() {
        // POST
        JsonObjectBuilder categorie = Json.createObjectBuilder();
        JsonObject categorieJson = categorie
                .add("nom", "bio")
                .add("description","un sandwich bio")
                .build();
        Response categorieResponse = this.target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(categorieJson));
        String location = categorieResponse.getHeaderString("Location");
        assertThat(categorieResponse.getStatus(), is(201));
        
        // GET
        JsonObject jsonRecupere = this.client
                .target(location)
                .request(MediaType.APPLICATION_JSON)
                .get(JsonObject.class);
        assertTrue(jsonRecupere.getJsonObject("categorie").getString("nom").contains("bio"));
        assertTrue(jsonRecupere.getJsonObject("categorie").getString("desc").contains("sandwich bio"));
        
        // PUT
        categorieJson = categorie
                .add("nom", "bio")
                .add("description","un sandwich bio modifier")
                .build();
        categorieResponse = this.client
                .target(location)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(categorieJson));
        assertThat(categorieResponse.getStatus(), is(200));
        
        // GET
        jsonRecupere = this.client
                .target(location)
                .request(MediaType.APPLICATION_JSON)
                .get(JsonObject.class);
        System.out.println(jsonRecupere);
        assertTrue(jsonRecupere.getJsonObject("categorie").getString("nom").contains("bio"));
        assertTrue(jsonRecupere.getJsonObject("categorie").getString("desc").contains("un sandwich bio modifier"));
        
        // DELETE
        Response deleteResponse = this.client
                .target(location)
                .request()
                .delete();
        assertThat(deleteResponse.getStatus(), is(204));
    }
    
    @Test
    public void testCategorieSandwich(){
        
        // Iniatialisation
        JsonObjectBuilder categorie = Json.createObjectBuilder();
        JsonObject categorieJson = categorie
                .add("nom", "bio")
                .add("description","un sandwich bio")
                .build();
        Response categorieResponse = this.target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(categorieJson));
        String location = categorieResponse.getHeaderString("Location");
        assertThat(categorieResponse.getStatus(), is(201));
        
        // POST
        JsonObject jo = null;
        Response response = this.client
                .target(location + "/sandwichs/10")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(jo));
        assertThat(response.getStatus(), is(201));
        
        // GET
        JsonObject jsonRecupere = this.client
                .target(location + "/sandwichs")
                .request(MediaType.APPLICATION_JSON)
                .get(JsonObject.class);
        assertTrue(jsonRecupere.getJsonArray("sandwichs").getJsonObject(0).getJsonObject("sandwich").getString("id").contains("10"));
        assertTrue(jsonRecupere.getJsonArray("sandwichs").getJsonObject(0).getJsonObject("sandwich").getString("nom").contains("ab-fuga"));
        assertTrue(jsonRecupere.getJsonArray("sandwichs").getJsonObject(0).getJsonObject("sandwich").getString("type_pain").contains("tortillas"));
    
        // DELETE
        Response deleteResponse = this.client
                .target(location)
                .request()
                .delete();
        assertThat(deleteResponse.getStatus(), is(204));
    }
   
}
