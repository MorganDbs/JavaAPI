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

public class TestSandwich {

    private Client client;
    private WebTarget target;
    
    @Before
    public void initClient(){
        this.client = ClientBuilder.newClient();
        this.target = this.client.target(Serveur.serveurUrl() + "sandwichs");
    }
    
    @Test
    public void testSandwich() {
        // POST
        JsonObjectBuilder sandwich = Json.createObjectBuilder();
        JsonObject sandwichJson = sandwich
                .add("nom", "test")
                .add("description", "test")
                .add("type_pain", "test")
                .add("img", "")
                .build();
        Response sandwichResponse = this.target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(sandwichJson));
        String location = sandwichResponse.getHeaderString("Location");
        assertThat(sandwichResponse.getStatus(), is(201));
        
        // GET
        JsonObject jsonRecupere = this.client
                .target(location)
                .request(MediaType.APPLICATION_JSON)
                .get(JsonObject.class);
        assertTrue(jsonRecupere.getJsonObject("sandwich").getJsonObject("sandwich").getString("nom").contains("test"));
        assertTrue(jsonRecupere.getJsonObject("sandwich").getJsonObject("sandwich").getString("type_pain").contains("test"));
        
        // PUT
        System.out.println(location);
        sandwichJson = sandwich
                .add("nom", "bio")
                .add("description", "un sandwich bio")
                .add("type_pain", "bio")
                .add("img", "")
                .build();
        sandwichResponse = this.client
                .target(location)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(sandwichJson));
        assertThat(sandwichResponse.getStatus(), is(200));
        
        // GET
        jsonRecupere = this.client
                .target(location)
                .request(MediaType.APPLICATION_JSON)
                .get(JsonObject.class);
        System.out.println(jsonRecupere);
        assertTrue(jsonRecupere.getJsonObject("sandwich").getJsonObject("sandwich").getString("nom").contains("bio"));
        assertTrue(jsonRecupere.getJsonObject("sandwich").getJsonObject("sandwich").getString("type_pain").contains("bio"));
        
        // DELETE
        Response deleteResponse = this.client
                .target(location)
                .request()
                .delete();
        assertThat(deleteResponse.getStatus(), is(204));
    }
}
