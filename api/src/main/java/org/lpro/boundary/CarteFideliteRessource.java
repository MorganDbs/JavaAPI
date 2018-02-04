package org.lpro.boundary;

import org.lpro.control.KeyManagement;
import org.lpro.control.PasswordManagement;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import org.lpro.entity.CarteFidelite;
import org.mindrot.jbcrypt.BCrypt;

@Path("/fidelite")
@Api(value = "Carte Fidelite")
public class CarteFideliteRessource {

    @Inject
    private KeyManagement keyManagement;
    
    @Inject
    CarteFideliteManager cfm;
    
    @Context
    private UriInfo uriInfo;
    
    @POST
    @Path("/creer")
    @ApiOperation(value = "Crée une nouvelle carte de fidelite", notes = "Crée une carte de fidelite a partir du JSON fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response createCarte(@Valid CarteFidelite carteFidelite) {
        CarteFidelite cf_check = this.cfm.findByNomAndPrenom(carteFidelite);
        if(cf_check != null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }else{
            CarteFidelite cf = this.cfm.createCarte(carteFidelite);
            return Response.ok().entity(buildJsonCarte(cf)).status(Response.Status.CREATED).header(AUTHORIZATION, cf.getCarte()).build();
        }
    }
    
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    @ApiOperation(value = "Récupère une carte de fidélité", notes = "Récupère une carte de fidelite a partir du JSON fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response authentifieUtilisateur(@Valid CarteFidelite carteFidelite) {
        try {
            String carte_num = carteFidelite.getCarte();
            String password = carteFidelite.getPassword();
            
            System.out.println("hash : "+PasswordManagement.digestPassword(password));
            
            String digestPassword = PasswordManagement.digestPassword(password);
            
            CarteFidelite cf = this.cfm.find(carteFidelite);
            
            if(cf == null){
                return Response.status(Response.Status.NOT_FOUND).build();
            }else{
                this.authentifie(carte_num, password, cf);
                
                String token = issueToken(carte_num);
                return Response.ok(buildJsonCarte(cf)).header(AUTHORIZATION, "Bearer " + token).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    private void authentifie(String carte_num, String password, CarteFidelite ct) throws Exception {
        if (carte_num.equals(ct.getCarte()) && BCrypt.checkpw(password, ct.getPassword())) { 
            
        } else {
            throw new NotAuthorizedException("Problème d'authentification");
        }
    }

    private String issueToken(String login) {
        Key key = keyManagement.generateKey();
        String jwtToken = Jwts.builder()
                .setSubject(login)
                .setIssuer(uriInfo.getAbsolutePath().toString())
                .setIssuedAt(new Date())
                .setExpiration(toDate(LocalDateTime.now().plusMinutes(5L)))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
        System.out.println(">>>> token/key : " + jwtToken + " -- " + key);
        return jwtToken;
    }

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    private JsonObject buildJsonCarte(CarteFidelite carteFidelite) {
        return Json.createObjectBuilder()
                .add("num_carte", carteFidelite.getCarte())
                .add("nom", carteFidelite.getNom())
                .add("prenom", carteFidelite.getPrenom())
                .add("montant", carteFidelite.getMontant())
                .add("reduction", carteFidelite.getReduction())
                .build();
    }
}
