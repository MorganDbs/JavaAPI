package org.lpro.boundary;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;
import org.junit.Before;

public class TestAuthentification {

    private Client client;
    private WebTarget target;
    
    @Before
    public void initClient(){
        this.client = ClientBuilder.newClient();
        this.target = this.client.target(Serveur.serveurUrl() + "authentification");
    }
    
    @Test
    public void TestAuthentification() {
        // Post
        JsonObjectBuilder authentification = Json.createObjectBuilder();
        JsonObject authentificationJson = authentification
                .add("username", "olivier")
                .add("password", "olivier")
                .build();
        Response authResponse = this.target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(authentificationJson));
        String authorization = authResponse.getHeaderString(HttpHeaders.AUTHORIZATION);
        assertThat(authResponse.getStatus(), is(200));
    }
   
}
