package org.lpro.boundary;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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
import org.lpro.entity.Sandwich;

@Stateless
@Path("sandwichs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Sandwich")
public class SandwichResource {
    
    @Inject
    SandwichManager sm;
    
    @GET
    @Produces("application/json")
    @ApiOperation(value = "Récupère tous les sandwichs", notes = "Renvoie le JSON associé a la collection sandwich")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response getSandwichs(@QueryParam("pain") String ptype, @DefaultValue("0") @QueryParam("img") String img, @DefaultValue("1") @QueryParam("page") String page, @DefaultValue("10") @QueryParam("size") String size) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
       
        
        List<Sandwich> sandwichs = this.sm.findParams(ptype, img, page, size);
        
        
        JsonObject meta = Json.createObjectBuilder()
                .add("count", sandwichs.size())
                .add("date",  dateFormat.format(date))
                .build();
        
        JsonObject json = Json.createObjectBuilder()
            .add("type", "collection")
            .add("meta",  meta)
            .add("sandwichs", this.getSandwichList(sandwichs))
            .build();
        
        return Response.ok(json).build();
    }

    
    @GET
    @Path("{id}")
    @ApiOperation(value = "Récupère un sandwich", notes = "Renvoie le JSON associé au sandwich")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response getOneSandwich(@PathParam("id") String id, @DefaultValue("0") @QueryParam("complet") String complet, @Context UriInfo uriInfo) {
        return Optional.ofNullable(sm.findById(id))
                .map(s -> Response.ok((Integer.parseInt(complet) == 1) ? sandwichCompletJson(s) : sandwich2Json(s)).build())
                .orElseThrow(() -> new CategorieNotFound("Ressource non disponible "+ uriInfo.getPath()));
    }
    
    @POST
    @ApiOperation(value = "Crée un sandwich", notes = "Crée un sandwich à partir du JSON fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response newSandwich(@Valid Sandwich s, @Context UriInfo uriInfo) {
        Sandwich newOne = this.sm.save(s);
        String id = newOne.getId();
        URI uri = uriInfo.getAbsolutePathBuilder().path("/"+id).build();
        return Response.created(uri).build();
    }
            
    @DELETE
    @Path("{id}")
    @ApiOperation(value = "Supprime un sandwich", notes = "Supprime le sandwich dont l'ID est fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No content"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response suppression(@PathParam("id") String id) {
        this.sm.delete(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    @PUT
    @Path("{id}")
    @ApiOperation(value = "Change les informations d'un sandwich", notes = "Change les informations d'un sandwich à partir du JSON fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response update(@PathParam("id") String id, Sandwich s) {
        Sandwich sandwich = this.sm.findById(id);
        if(sandwich == null){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "Le sandwich n'existe pas")
                            .build()
            ).build();
        }else{
            if(s.getDescription() != null && !s.getDescription().equals("")){
                sandwich.setDescription(s.getDescription());
            }
            if(s.getType_pain() != null && !s.getType_pain().equals("")){
                sandwich.setType_pain(s.getType_pain());
            }
            if(s.getNom() != null && !s.getNom().equals("")){
                sandwich.setNom(s.getNom());
            }if(s.getImg() != null){
                sandwich.setImg(s.getImg());
            }
            
            this.sm.update(sandwich);
            
            return Response.ok(buildJson(sandwich)).build();
        }
    }

    private JsonObject sandwich2Json(Sandwich s) {
        return Json.createObjectBuilder()
                .add("type", "resource")
                .add("sandwich", buildJson(s))
                .build();
    }
    
    private JsonArray getSandwichList(List<Sandwich> sandwichs) {
        JsonArrayBuilder jab = Json.createArrayBuilder();
        sandwichs.forEach((s) -> {
            jab.add(buildJson(s));
        });
        return jab.build();
    }
    
    public static JsonObject buildJson(Sandwich s) {
        JsonObject sandwich = Json.createObjectBuilder()
                .add("id", s.getId())
                .add("nom", s.getNom())
                .add("type_pain", s.getType_pain())
                .build();

        JsonObject href = Json.createObjectBuilder()
                .add("href", ((s.getImg() == null) ? "" : s.getImg()))
                .build();

        JsonObject self = Json.createObjectBuilder()
                .add("self", href)
                .build();

        return Json.createObjectBuilder()
                .add("sandwich", sandwich)
                .add("links", self)
                .build();
    }
    
    public static JsonObject sandwichCompletJson(Sandwich s) {
        
        JsonArrayBuilder categories = Json.createArrayBuilder();
        s.getCategorie().forEach((c)->{
            JsonObject categorie = Json.createObjectBuilder()
                    .add("id", c.getId())
                    .add("nom", c.getNom())
                    .build();
            categories.add(categorie);
        });
        
        JsonArrayBuilder tailles = Json.createArrayBuilder();
        s.getTaille().forEach((t)->{
            JsonObject taille = Json.createObjectBuilder()
                    .add("id", t.getId())
                    .add("nom", t.getNom())
                    .add("prix", t.getPrix())
                    .build();
            tailles.add(taille);
        });
                
                
        JsonObject sandwich = Json.createObjectBuilder()
                .add("id", s.getId())
                .add("nom", s.getNom())
                .add("description", s.getDescription())
                .add("type_pain", s.getType_pain())
                .add("img", ((s.getImg() == null) ? "null" : s.getImg()))
                .add("categories", categories)
                .add("tailles", tailles)
                .build();
        
        JsonObject hrefcateg = Json.createObjectBuilder()
                .add("href", "/sandwichs/" + s.getId() + "/categories")
                .build();
        
        JsonObject hreftaille = Json.createObjectBuilder()
                .add("href", "/sandwichs/" + s.getId() + "/tailles")
                .build();
        
        JsonObject self = Json.createObjectBuilder()
                .add("categories", hrefcateg)
                .add("tailles", hreftaille)
                .build();

        return Json.createObjectBuilder()
                .add("sandwich", sandwich)
                .add("links", self)
                .build();
    }
}
