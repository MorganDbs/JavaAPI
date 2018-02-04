package org.lpro.boundary;

import java.util.Random;
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
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;

public class TestCarteFidelite {

    private Client client;
    private WebTarget target;
    private static final String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int RANDOM_STRING_LENGTH = 64;
    
    @Before
    public void initClient(){
        this.client = ClientBuilder.newClient();
        this.target = this.client.target(Serveur.serveurUrl() + "fidelite");
    }
    
    @Test
    public void TestCarteFidelite() {
        String nom = this.generateRandomString();
        String prenom = this.generateRandomString();
        String password = this.generateRandomString();
        // Post
        JsonObjectBuilder carteFidelite = Json.createObjectBuilder();
        JsonObject carteFideliteJson = carteFidelite
                .add("nom", nom)
                .add("prenom", prenom)
                .add("password", password)
                .build();
        
        WebTarget creer = this.client.target(Serveur.serveurUrl() + "fidelite/creer");
        Response carteFideliteResponse = creer
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(carteFideliteJson));
        assertThat(carteFideliteResponse.getStatus(), is(201));
        
        System.out.println(carteFideliteResponse.getHeaderString(HttpHeaders.AUTHORIZATION));
        String numcarte = carteFideliteResponse.getHeaderString(HttpHeaders.AUTHORIZATION);
        
        carteFidelite = Json.createObjectBuilder();
        carteFideliteJson = carteFidelite
                .add("carte", numcarte)
                .add("password", password)
                .build();
        Response carteFideliteResponse2 = this.target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(carteFideliteJson));
        assertThat(carteFideliteResponse2.getStatus(), is(200));
        assertTrue(carteFideliteJson.getString("carte").contains(numcarte));
    }
    
    public String generateRandomString(){
         
        StringBuffer randStr = new StringBuffer();
        for(int i=0; i<RANDOM_STRING_LENGTH; i++){
            int number = getRandomNumber();
            char ch = CHAR_LIST.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }
    
    private int getRandomNumber() {
        int randomInt = 0;
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(CHAR_LIST.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }
}
