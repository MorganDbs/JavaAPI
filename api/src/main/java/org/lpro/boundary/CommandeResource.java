package org.lpro.boundary;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.URI;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.lpro.entity.CarteFidelite;
import org.lpro.entity.Commande;
import org.lpro.entity.Quantity;
import org.lpro.entity.Sandwich;
import org.lpro.entity.Taille;
import org.mindrot.jbcrypt.BCrypt;


@Stateless
@Path("commandes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Commande")
public class CommandeResource {

    @Inject
    CommandeManager cm;

    @Inject
    SandwichManager sm;
    
    @Inject
    TailleManager tm;
    
    @Inject
    QuantityManager qm;
    
    @Inject
    CarteFideliteManager cfm;
    
    @Context
    UriInfo uriInfo;
    
    
    @GET
    @ApiOperation(value = "Récupère toutes les commandes en fonctions de l'état", notes = "Renvoie le JSON associé a la collection de commande")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response getCommandes(@QueryParam("etat") String etat){
        if (etat == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "L'état ne peut pas etre null")
                            .build()
            ).build();
        }
        
        List<Commande> commandes = this.cm.findAll(etat);
        return Response.ok(buildCommandes(commandes)).build();
    }
    
    @GET
    @Path("{id}")
    @ApiOperation(value = "Récupère une commande", notes = "Renvoie le JSON associé a la commande")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response getOneCommande(@PathParam("id") String id, @DefaultValue("0") @QueryParam("details") String details, @QueryParam("token") String token, @HeaderParam("X-lbs-token") String header, @Context UriInfo uriInfo) {
        Commande c;
        try{
            c = this.cm.findById(id);
        }catch(NullPointerException e){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "La commande n'existe pas")
                            .build()
            ).build();
        }

        if (token == null && header == null) {
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Il faut renseigner un token")
                            .build()
            ).build();
        }

        String tokenCommande = (token != null) ? token : header;
        
        if(c.getToken().equals(tokenCommande)){
            if(details.equals("1")){
                return Response.ok(commandeDetails2Json(c)).build();
            }
            else if(details.equals("0")){
                return Response.ok(commande2Json(c)).build();
            }else{
                return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Valeur details non valide")
                            .build()
                ).build();
            }
        }else{
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Token non valide")
                            .build()
            ).build();
        }
    }

    @POST
    @ApiOperation(value = "Crée une commande", notes = "Crée une commande à partir du JSON fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 417, message = "EXPECTATION_FAILED"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response addCommande(JsonObject jsonCommande, @Context UriInfo uriInfo) throws java.text.ParseException {

        JsonObjectBuilder errors = Json.createObjectBuilder();
        String errorsList = "";
        JsonObject livraison;
        Boolean flag_errors = false;

        String nom_client = "";
        if (!jsonCommande.containsKey("nom_client") || jsonCommande.isNull("nom_client") || jsonCommande.getString("nom_client").isEmpty()) {
            errorsList += "Il faut renseigner le nom du client. ";
            flag_errors = true;
        } else {
            nom_client = jsonCommande.getString("nom_client");
            if (!Pattern.matches("([a-zA-Z\\\\s-]+)", nom_client)) {
                errorsList += "Il faut respecter la casse du nom du client. ";
                flag_errors = true;
            }
        }

        String prenom_client = (jsonCommande.containsKey("prenom_client")) ? jsonCommande.getString("prenom_client") : "";
        if (!Pattern.matches("([a-zA-Z\\\\s-]+)", prenom_client)) {
            errorsList += "Il faut respecter la casse du prenom du client. ";
            flag_errors = true;
        }

        String mail_client = "";
        if (!jsonCommande.containsKey("mail_client") || jsonCommande.isNull("mail_client") || jsonCommande.getString("mail_client").isEmpty()) {
            errorsList += "Il faut renseigner le mail du client. ";
            flag_errors = true;
        } else {
            mail_client = jsonCommande.getString("mail_client");
            if (!Pattern.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])", mail_client)) {
                errorsList += "Il faut respecter la casse du mail du client. ";
                flag_errors = true;
            }
        }

        String heure = "";
        String date = "";
        if (!jsonCommande.containsKey("livraison") || jsonCommande.isNull("livraison")) {
            errorsList += "Il faut renseigner la livraison. ";
            flag_errors = true;
        } else {
            livraison = jsonCommande.getJsonObject("livraison");
            if (!livraison.containsKey("date") || livraison.isNull("date") || livraison.getString("date").isEmpty()) {
                errorsList += "Il faut renseigner la date de livraison. ";
                flag_errors = true;
            } else {
                date = livraison.getString("date");
            }
            if (!livraison.containsKey("heure") || livraison.isNull("heure") || livraison.getString("heure").isEmpty()) {
                errorsList += "Il faut renseigner l'heure de livraison.";
                flag_errors = true;
            } else {
                heure = livraison.getString("heure");
            }
        }
        
        Date dateCommande = null;
        Timestamp dateCommande_stamp = null;
        if (!date.isEmpty() && !heure.isEmpty()) {
            if (Pattern.matches("^(0?[1-9]|[12][0-9]|3[01])[\\-](0?[1-9]|1[012])[\\-]\\d{4}$", date) && Pattern.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", heure)) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    sdf.setTimeZone(TimeZone.getDefault());

                    Date today = Date.from(LocalDateTime.now()
                            .atZone(ZoneId.systemDefault())
                            .toInstant());
                    
                    dateCommande = sdf.parse(date + " " + heure);
                    
                    Timestamp today_stamp = new Timestamp(today.getTime());
                    dateCommande_stamp = new Timestamp(dateCommande.getTime());
                    
                    if (dateCommande_stamp.before(today_stamp)) {
                        errorsList += "La date de livraison est antérieur à la date actuelle.";
                        flag_errors = true;
                    }
                } catch (ParseException pe) {
                }
            } else {
                errorsList += "La date ou l'heure de livraison ne respecte pas la casse.";
                flag_errors = true;
            }

        }

        

        
        errors.add("errors", errorsList);
        JsonObject json_errors = errors.build();

        if (flag_errors) {
            return Response.status(Response.Status.EXPECTATION_FAILED).entity(json_errors).build();
        }
        
        Commande commande = new Commande(nom_client, prenom_client, mail_client, dateCommande_stamp);
        Commande newCommande = this.cm.save(commande);
        URI uri = uriInfo.getAbsolutePathBuilder().path("/"+newCommande.getId() + "?token="+newCommande.getToken()).build();
        return Response.created(uri).entity(commande2Json(newCommande)).build();
       
    }
    
    @PUT
    @Path("{id}/livraison")
    @ApiOperation(value = "Change les informations de livraison d'une commande", notes = "Change les informations de livraison d'une commande à partir du JSON fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 417, message = "EXPECTATION_FAILED"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response updateLivraisonCommande(JsonObject jsonLivraison, @QueryParam("token") String token, @PathParam("id") String id, @HeaderParam("X-lbs-token") String header, @Context UriInfo uriInfo) {
        JsonObjectBuilder errors = Json.createObjectBuilder();
        String errorsList = "";
        JsonObject livraison;
        Boolean flag_errors = false;
        Commande c = this.cm.findById(id);

        if (c == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "La commande n'existe pas")
                            .build()
            ).build();
        }
        
        if (!c.getEtat().equals("attente")){
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "La commande ne peut plus etre modifié")
                            .build()
            ).build();
        }
        
        if (token == null && header == null) {
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Il faut renseigner un token")
                            .build()
            ).build();
        }

        String tokenCommande = (token != null) ? token : header;
        
        if(c.getToken().equals(tokenCommande)){
        
            String heure = "";
            String date = "";
            if (!jsonLivraison.containsKey("livraison") || jsonLivraison.isNull("livraison")) {
                errorsList += "Il faut renseigner la livraison. ";
                flag_errors = true;
            } else {
                livraison = jsonLivraison.getJsonObject("livraison");
                if (!livraison.containsKey("date") || livraison.isNull("date") || livraison.getString("date").isEmpty()) {
                    errorsList += "Il faut renseigner la date de livraison. ";
                    flag_errors = true;
                } else {
                    date = livraison.getString("date");
                }
                if (!livraison.containsKey("heure") || livraison.isNull("heure") || livraison.getString("heure").isEmpty()) {
                    errorsList += "Il faut renseigner l'heure de livraison.";
                    flag_errors = true;
                } else {
                    heure = livraison.getString("heure");
                }
            }

            Date dateCommande = null;
            Timestamp dateCommande_stamp = null;
            if (!date.isEmpty() && !heure.isEmpty()) {
                if (Pattern.matches("^(0?[1-9]|[12][0-9]|3[01])[\\-](0?[1-9]|1[012])[\\-]\\d{4}$", date) && Pattern.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", heure)) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                        sdf.setTimeZone(TimeZone.getDefault());

                        Date today = Date.from(LocalDateTime.now()
                                .atZone(ZoneId.systemDefault())
                                .toInstant());

                        dateCommande = sdf.parse(date + " " + heure);

                        Timestamp today_stamp = new Timestamp(today.getTime());
                        dateCommande_stamp = new Timestamp(dateCommande.getTime());

                        if (dateCommande_stamp.before(today_stamp)) {
                            errorsList += "La date de livraison est antérieur à la date actuelle.";
                            flag_errors = true;
                        }
                    } catch (ParseException pe) {
                    }
                } else {
                    errorsList += "La date ou l'heure de livraison ne respecte pas la casse.";
                    flag_errors = true;
                }

            }

            errors.add("errors", errorsList);
            JsonObject json_errors = errors.build();

            if (flag_errors) {
                return Response.status(Response.Status.EXPECTATION_FAILED).entity(json_errors).build();
            }

            c = this.cm.updateLivraison(c, dateCommande_stamp);

            return Response.ok().entity(commande2Json(c)).build();
            
        }else{
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Token non valide")
                            .build()
            ).build();
        }        
    }
    
    @PUT
    @Path("{id}/payer")
    @ApiOperation(value = "Change l'état d'une commande à 'payer'", notes = "Change l'état d'une commande à 'payer'")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 417, message = "EXPECTATION_FAILED"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response payerCommande(JsonObject carte, @QueryParam("token") String token, @PathParam("id") String id, @HeaderParam("X-lbs-token") String header, @Context UriInfo uriInfo) {
        JsonObjectBuilder errors = Json.createObjectBuilder();
        String errorsList = "";
        Boolean flag_errors = false;
        
        Commande c = this.cm.findById(id);

        if (c == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "La commande n'existe pas")
                            .build()
            ).build();
        }
        
        if (!c.getEtat().equals("attente")){
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "La commande est deja payé")
                            .build()
            ).build();
        }
        
        if (token == null && header == null) {
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Il faut renseigner un token")
                            .build()
            ).build();
        }

        String tokenCommande = (token != null) ? token : header;
        
        if(c.getToken().equals(tokenCommande)){
            if(!carte.containsKey("numero") || carte.isNull("numero") || carte.getString("numero").isEmpty()){
                errorsList += "Il faut renseigner le numero de carte. ";
                flag_errors = true;
            }else if (!Pattern.matches("^[0-9]{4} {0,1}[0-9]{4} {0,1}[0-9]{4} {0,1}[0-9]{4}$",  carte.getString("numero"))) {
                errorsList += "Il faut respecter la casse du numero de carte. (Visa, MasterCard, American Express, Diners Club, Discover, et JCB) ";
                flag_errors = true;
            }
            
            if(!carte.containsKey("date") || carte.isNull("date") || carte.getString("date").isEmpty()){
                errorsList += "Il faut renseigner la date. ";
                flag_errors = true;
            }
            
            if(!carte.containsKey("cvv") || carte.isNull("cvv") || carte.getString("cvv").isEmpty()){
                errorsList += "Il faut renseigner le numero de cvv. ";
                flag_errors = true;
            }else if (!Pattern.matches("^[0-9]{3,4}$",  carte.getString("cvv"))) {
                errorsList += "Il faut respecter la casse du numero de cvv. ";
                flag_errors = true;
            }
            
            
            
            Date dateCommande = null;
            Timestamp dateCommande_stamp = null;
            if (!carte.getString("date").isEmpty()) {
                if (Pattern.matches("^([0-1]?[0-9]|2[0-3])-[0-5][0-9]$", carte.getString("date"))) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM-yy");
                        sdf.setTimeZone(TimeZone.getDefault());

                        Date today = Date.from(LocalDateTime.now()
                                .atZone(ZoneId.systemDefault())
                                .toInstant());

                        dateCommande = sdf.parse(carte.getString("date"));

                        Timestamp today_stamp = new Timestamp(today.getTime());
                        dateCommande_stamp = new Timestamp(dateCommande.getTime());

                        if (dateCommande_stamp.before(today_stamp)) {
                            errorsList += "La date de la carte est antérieur à la date actuelle.";
                            flag_errors = true;
                        }
                    } catch (ParseException pe) {
                    }
                } else {
                    errorsList += "La date ne respecte pas la casse.";
                    flag_errors = true;
                }
            }
            
            if(carte.containsKey("carte_fidelite") && !carte.isNull("carte_fidelite") && !carte.getString("carte_fidelite").isEmpty() && carte.containsKey("password") && !carte.isNull("password") && !carte.getString("password").isEmpty()){
                String carteFidelite = carte.getString("carte_fidelite");
                String password = carte.getString("password");
                
                CarteFidelite cf = this.cfm.find(carteFidelite);

                if(cf == null && !BCrypt.checkpw(password, cf.getPassword())){
                    return Response.status(Response.Status.NOT_FOUND).build();
                }else{
                    
                    double montant = c.getQuantity().stream().mapToDouble(cq -> (cq.getQuantity() * this.tm.findById(cq.getTaille_id()).getPrix())).sum();
                    this.cfm.updateMontant(cf, montant);
                }
            }
            
            errors.add("errors", errorsList);
            JsonObject json_errors = errors.build();

            if (flag_errors) {
                return Response.status(Response.Status.EXPECTATION_FAILED).entity(json_errors).build();
            }
            
            
            c = this.cm.payerCommande(c);

            return Response.ok().entity(buildJsonPay(c)).status(Response.Status.OK).build();
            
        }else{
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Token non valide")
                            .build()
            ).build();
        }   
    }
    
    @PUT
    @Path("{id}/etat")
    @ApiOperation(value = "Change l'état d'une commande", notes = "Change l'état d'une commande en fonction du JSON fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 417, message = "EXPECTATION_FAILED"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response setEtat(JsonObject jsonEtat, @QueryParam("token") String token, @PathParam("id") String id, @HeaderParam("X-lbs-token") String header, @Context UriInfo uriInfo) {
        JsonObjectBuilder errors = Json.createObjectBuilder();
        
        Commande c = this.cm.findById(id);

        if (c == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "La commande n'existe pas")
                            .build()
            ).build();
        }
        
        if (token == null && header == null) {
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Il faut renseigner un token")
                            .build()
            ).build();
        }

        String tokenCommande = (token != null) ? token : header;
        
        if(c.getToken().equals(tokenCommande)){
            if (!jsonEtat.containsKey("etat") || jsonEtat.isNull("etat") || jsonEtat.getString("etat").isEmpty()) {
                return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Il faut renseigner un etat")
                            .build()
                ).build();
            }
            if (!Pattern.matches("(\\w+( +\\w+)*)", jsonEtat.getString("etat"))) {
                return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Il faut respecter la casse d'un etat.")
                            .build()
                ).build();
            }
            c = this.cm.updateEtat(c, jsonEtat.getString("etat"));
            return Response.ok().entity(buildJsonPay(c)).status(Response.Status.OK).build();
        }else{
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Token non valide")
                            .build()
            ).build();
        }  
    }

    @GET
    @Path("{id}/facture")
    @ApiOperation(value = "Récupère la facture d'une commande", notes = "Renvoie le JSON associé a la facture de la commande")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response getOneFacture( @QueryParam("token") String token, @PathParam("id") String id, @HeaderParam("X-lbs-token") String header, @Context UriInfo uriInfo) {
        Commande c = this.cm.findById(id);

        if (c == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "La commande n'existe pas")
                            .build()
            ).build();
        }
        
        if (token == null && header == null) {
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Il faut renseigner un token")
                            .build()
            ).build();
        }

        String tokenCommande = (token != null) ? token : header;
        
        if(c.getToken().equals(tokenCommande)){
            if(!c.getEtat().equals("attente")){
                return Response.ok().entity(buildJsonPay(c)).status(Response.Status.CREATED).build();
            }else{
                return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Vous ne pouvez pas obtenir une facture pour une commande en attente")
                            .build()
                ).build();
            }
        }else{
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Token non valide")
                            .build()
            ).build();
        }  
    }

    @POST
    @Path("{id}/sandwichs")
    @ApiOperation(value = "Ajoute un sandwich à une commande", notes = "Ajoute un sandwich à une commande à partir du JSON fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response addSandToCommand(@PathParam("id") String id, JsonObject jsonSandwich, @QueryParam("token") String token, @HeaderParam("X-lbs-token") String header, @Context UriInfo uriInfo) {
        Commande c = this.cm.findById(id);
        Sandwich sandwich = this.sm.findByName(jsonSandwich.getString("sandwich"));
        Taille taille = this.tm.findByName(jsonSandwich.getString("taille"));
        boolean flag = false;
        
        if(c == null){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "La commande n'existe pas")
                            .build()
            ).build();
        }

        if(sandwich == null){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "Le sandwich n'existe pas")
                            .build()
            ).build();
        }
        
        if(taille == null){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "La taille n'existe pas")
                            .build()
            ).build();
        }

        if (token == null && header == null) {
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Il faut renseigner un token")
                            .build()
            ).build();
        }

        if(!c.getEtat().equals("attente")){
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "La commande ne peut plus être modifié")
                            .build()
            ).build();
        }
        
        String tokenCommande = (token != null) ? token : header;
        
        flag = sandwich.getTaille().stream().anyMatch(t -> { return t.getId().equals(taille.getId()); });
        
        if(flag){
            if(jsonSandwich.containsKey("quantity") && !jsonSandwich.isNull("quantity") && !jsonSandwich.getString("quantity").isEmpty()){
                if(c.getToken().equals(tokenCommande)){
                    cm.addSandwichToCommande(c.getId(), sandwich, taille, Integer.parseInt(jsonSandwich.getString("quantity")));
                    URI uri = uriInfo.getAbsolutePathBuilder()
                        .path("/")
                        .path(id)
                        .build();
                return Response.created(uri).build();
                }else{
                    return Response.status(Response.Status.FORBIDDEN).entity(
                            Json.createObjectBuilder()
                                    .add("error", "Token non valide")
                                    .build()
                    ).build();
                }
            }else{
                return Response.status(Response.Status.FORBIDDEN).entity(
                            Json.createObjectBuilder()
                                    .add("error", "Merci de rentrer le nom du sandwich")
                                    .build()
                    ).build();
            }
        }else{
            return Response.status(Response.Status.FORBIDDEN).entity(
                            Json.createObjectBuilder()
                                    .add("error", "Le sandwich n'est pas disponible dans cette taille")
                                    .build()
                    ).build();
        }
    }
    
    @DELETE
    @Path("{id}/sandwichs")
    @ApiOperation(value = "Supprime le sandwich d'une commande", notes = "Supprime le sandwich d'une commande dont l'ID est fourni et le JSON est fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response deleteSandwichFromCommande(@PathParam("id") String id, JsonObject jsonSandwich, @QueryParam("token") String token, @HeaderParam("X-lbs-token") String header, @Context UriInfo uriInfo) {
        Commande c = this.cm.findById(id);
        Sandwich sandwich = this.sm.findByName(jsonSandwich.getString("sandwich"));
        Taille taille = this.tm.findByName(jsonSandwich.getString("taille"));
        boolean flag = false;
        
        if(c == null){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "La commande n'existe pas")
                            .build()
            ).build();
        }
        
        if(sandwich == null){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "Le sandwich n'existe pas")
                            .build()
            ).build();
        }
        
        if(taille == null){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "La taille n'existe pas")
                            .build()
            ).build();
        }
        
        if (token == null && header == null) {
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Il faut renseigner un token")
                            .build()
            ).build();
        }
        
        if(!c.getEtat().equals("attente")){
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "La commande ne peut plus être modifié")
                            .build()
            ).build();
        }
        
        String tokenCommande = (token != null) ? token : header;
        
        flag = sandwich.getTaille().stream().anyMatch(t -> { return t.getId().equals(taille.getId()); });
        
        if(flag){
                if(c.getToken().equals(tokenCommande)){
                    cm.deleteSandwichFromCommande(c.getId(), sandwich, taille);
                    URI uri = uriInfo.getAbsolutePathBuilder()
                        .path("/")
                        .path(id)
                        .build();
                return Response.status(Response.Status.OK).entity(uri).build();
                }else{
                    return Response.status(Response.Status.FORBIDDEN).entity(
                            Json.createObjectBuilder()
                                    .add("error", "Token non valide")
                                    .build()
                    ).build();
                }
        }else{
            return Response.status(Response.Status.FORBIDDEN).entity(
                            Json.createObjectBuilder()
                                    .add("error", "Le sandwich n'est pas disponible dans cette taille")
                                    .build()
                    ).build();
        }
    }
    
    @PUT
    @Path("{id}/sandwichs")
    @ApiOperation(value = "Modifie un sandwich d'une commande", notes = "Modifie un sandwich d'une commande à partir du JSON fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response putSandToCommand(@PathParam("id") String id, JsonObject jsonChoix, @QueryParam("token") String token, @HeaderParam("X-lbs-token") String header, @Context UriInfo uriInfo) {
        Commande c = this.cm.findById(id);
        Quantity q = this.qm.findById(jsonChoix.getString("id"));
        
        if(c == null){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "La commande n'existe pas")
                            .build()
            ).build();
        }
        
        if(q == null){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "L'id du choix n'existe pas")
                            .build()
            ).build();
        }
        
        if (token == null && header == null) {
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "Il faut renseigner un token")
                            .build()
            ).build();
        }
        
        if(!c.getEtat().equals("attente")){
            return Response.status(Response.Status.FORBIDDEN).entity(
                    Json.createObjectBuilder()
                            .add("error", "La commande ne peut plus être modifié")
                            .build()
            ).build();
        }
        
        String tokenCommande = (token != null) ? token : header;
       
        
        if(jsonChoix.containsKey("taille") && !jsonChoix.isNull("taille") && !jsonChoix.getString("taille").isEmpty()){
            Taille taille = this.tm.findByName(jsonChoix.getString("taille"));
            if(taille == null){
                return Response.status(Response.Status.NOT_FOUND).entity(
                        Json.createObjectBuilder()
                                .add("error", "La taille n'existe pas")
                                .build()
                ).build();
            }else{
                if(!this.sm.findById(q.getSandwich_id()).getTaille().stream().anyMatch(t -> { return t.getId().equals(taille.getId()); })){
                    return Response.status(Response.Status.FORBIDDEN).entity(
                            Json.createObjectBuilder()
                                    .add("error", "Le sandwich n'est pas disponible dans cette taille")
                                    .build()
                    ).build();
                }else{
                    if(jsonChoix.containsKey("quantity") && !jsonChoix.isNull("quantity") && !jsonChoix.getString("quantity").isEmpty()){
                        if(Integer.parseInt(jsonChoix.getString("quantity")) > 0){
                            this.cm.updateQuantityTaille(q, taille, Integer.parseInt(jsonChoix.getString("quantity")));
                            URI uri = uriInfo.getAbsolutePathBuilder()
                                .path("/")
                                .path(c.getId())
                                .build();
                            return Response.ok(uri).build();
                        }else{
                            return Response.status(Response.Status.FORBIDDEN).entity(
                                Json.createObjectBuilder()
                                    .add("error", "La quantite ne peut pas etre nulle ou negative")
                                    .build()
                                ).build();
                        }                      
                    }else{
                        this.cm.updateTaille(q, taille);
                        URI uri = uriInfo.getAbsolutePathBuilder()
                                .path("/")
                                .path(c.getId())
                                .build();
                        return Response.ok(uri).status(Response.Status.OK).build();
                    }
                }
            }
        }else{
            if(jsonChoix.containsKey("quantity") && !jsonChoix.isNull("quantity") && !jsonChoix.getString("quantity").isEmpty()){
                if(Integer.parseInt(jsonChoix.getString("quantity")) > 0){
                    this.cm.updateQuantity(q, Integer.parseInt(jsonChoix.getString("quantity")));
                    URI uri = uriInfo.getAbsolutePathBuilder()
                                .path("/")
                                .path(c.getId())
                                .build();
                    return Response.ok(uri).status(Response.Status.OK).build();
                }else{
                    return Response.status(Response.Status.FORBIDDEN).entity(
                        Json.createObjectBuilder()
                            .add("error", "La quantite ne peut pas etre nulle ou negative")
                            .build()
                        ).build();
                    }                      
            }else{
                return Response.status(Response.Status.FORBIDDEN).entity(
                        Json.createObjectBuilder()
                            .add("error", "La quantite ne peut pas etre nulle ou negative")
                            .build()
                        ).build();
            } 
       }
    }

    private JsonObject commande2Json(Commande c) {
        return Json.createObjectBuilder()
                .add("commande", buildJson(c))
                .build();
    }
    
    private JsonObject buildCommandes(List<Commande> commandes) {
        JsonArrayBuilder jab = Json.createArrayBuilder();

        commandes.forEach(c -> {
            jab.add(buildJson(c));
        });

        return Json.createObjectBuilder()
                .add("commandes", jab.build())
                .build();
    }
   
    private JsonObject buildJsonPay(Commande c) {
         JsonObject facture = Json.createObjectBuilder()
                .add("facture", buildJson(c))
                .build();
         return facture;
    }
    private JsonObject buildJson(Commande c) {
        JsonObject livraison = Json.createObjectBuilder()
                .add("date", c.getDate().toString())
                .build();    
        
        double total = c.getQuantity().stream().mapToDouble(cq -> (cq.getQuantity() * this.tm.findById(cq.getTaille_id()).getPrix())).sum();
        
        return Json.createObjectBuilder()
                .add("nom_client", c.getNom_client())
                .add("prenom_client", c.getPrenom_client())
                .add("mail_client", c.getMail_client())
                .add("livraison", livraison)
                .add("etat", c.getEtat())
                .add("id", c.getId())
                .add("total", total)
                .add("token", c.getToken())
                .build();
    }
    
    private JsonObject commandeDetails2Json(Commande c) {
        return Json.createObjectBuilder()
                .add("commande", commandeDetails(c))
                .build();
    }
    
    private JsonObject commandeDetails(Commande c){
        JsonObject livraison = Json.createObjectBuilder()
                .add("date", c.getDate().toString())
                .build();
        
        double total = c.getQuantity().stream().mapToDouble(cq -> (cq.getQuantity() * this.tm.findById(cq.getTaille_id()).getPrix())).sum();
        
        JsonArrayBuilder sandwichs = Json.createArrayBuilder();
        
        c.getQuantity().forEach((q)->{
            JsonObject sandwich = Json.createObjectBuilder()
                    .add("nom", this.sm.findById(q.getSandwich_id()).getNom())
                    .add("taille", this.tm.findById(q.getTaille_id()).getNom())
                    .add("quantite", q.getQuantity())
                    .add("id_choix", q.getId())
                    .build();
            sandwichs.add(sandwich);
        });

        return Json.createObjectBuilder()
                .add("nom_client", c.getNom_client())
                .add("prenom_client", c.getPrenom_client())
                .add("mail_client", c.getMail_client())
                .add("livraison", livraison)
                .add("etat", c.getEtat())
                .add("id", c.getId())
                .add("sandwichs", sandwichs)
                .add("total", total)
                .add("token", c.getToken())
                .build();
    }
}
