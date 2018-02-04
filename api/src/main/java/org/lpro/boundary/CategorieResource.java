package org.lpro.boundary;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.lpro.entity.Categorie;
import org.lpro.entity.Sandwich;

@Stateless
@Path("categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Categorie")
public class CategorieResource {
    
    @Inject
    SandwichManager sm;
    
    @Inject
    CategorieManager cm;
    
    @Context
    UriInfo uriInfo;
    
    @GET
    @ApiOperation(value = "Récupère toutes les catégories", notes = "Renvoie le JSON associé à la collection de catégories")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response getCategories() {
        JsonObject json = Json.createObjectBuilder()
                .add("type", "collection")
                .add("categories", this.getCategorieList())
                .build();
        return Response.ok(json).build();
    }
    
    @GET
    @Path("{id}")
    @ApiOperation(value = "Récupère une catégorie", notes = "Renvoie le JSON associé à la catégorie")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response getOneCategorie(@PathParam("id") String id, @Context UriInfo uriInfo) {
        return Optional.ofNullable(cm.findById(id))
                .map(c -> Response.ok(categorie2Json(c)).build())
                .orElseThrow(() -> new CategorieNotFound("Ressource non disponible "+ uriInfo.getPath()));
    }
    
    @GET
    @Path("{categId}/sandwichs")
    @ApiOperation(value = "Récupère les sandwichs d'une catégorie", notes = "Renvoie le JSON associé a la collection de sandwich de la categorie")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response getCategorieSandwichs(@PathParam("categId") String categId, @Context UriInfo uriInfo) {
        Categorie c = this.cm.findById(categId);
        Set<Sandwich> sandwichs = c.getSandwich();

        JsonArrayBuilder ja = Json.createArrayBuilder();
        sandwichs.forEach((sand) -> {
            ja.add(SandwichResource.buildJson(sand));
        });

        JsonObject json = Json.createObjectBuilder()
                .add("sandwichs", ja.build())
                .build();

        return Response.ok(json).build();
    }
    
    @POST
    @Path("{categId}/sandwichs/{sandId}")
    @ApiOperation(value = "Ajoute un sandwich a une categorie", notes = "Ajoute un sandwich a une categorie à partir des id fournis")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response addSandToCategorie(@PathParam("categId") String categId, @PathParam("sandId") String sandId, @Context UriInfo uriInfo) {
        Sandwich s = this.sm.addSandwich(categId, sandId);
        URI uri = uriInfo.getAbsolutePathBuilder()
                .path("/")
                .path(sandId)
                .build();
        return Response.created(uri).build();
    }
     
    @POST
    @ApiOperation(value = "Crée une catégorie", notes = "Crée une catégorie à partir du JSON fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response newCategorie(@Valid Categorie c, @Context UriInfo uriInfo) {
        Categorie newOne = this.cm.save(c);
        String id = newOne.getId();
        URI uri = uriInfo.getAbsolutePathBuilder().path("/"+id).build();
        return Response.created(uri).build();
    }
            
    @DELETE
    @Path("{id}")
    @ApiOperation(value = "Supprime une catégorie", notes = "Supprime la catégorie dont l'ID est fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No content"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response suppression(@PathParam("id") String id) {
        this.cm.delete(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    @PUT
    @Path("{id}")
    @ApiOperation(value = "Change les informations d'une categorie", notes = "Change les informations d'une catégorie à partir du JSON fourni")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Internal server error")})
    public Response update(@PathParam("id") String id, Categorie c) {
        Categorie categorie = this.cm.findById(id);
        if(categorie == null){
            return Response.status(Response.Status.NOT_FOUND).entity(
                    Json.createObjectBuilder()
                            .add("error", "La categorie n'existe pas")
                            .build()
            ).build();
        }else{
            if(c.getNom() != null && !c.getNom().equals("")){
                categorie.setNom(c.getNom());
            }
            
            if(c.getDescription() != null && !c.getDescription().equals("")){
                categorie.setDescription(c.getDescription());
            }
            
            this.cm.update(categorie);
            
            return Response.ok(buildJson(categorie)).build();
        }
    }

    private JsonObject categorie2Json(Categorie c) {
        JsonObject json = Json.createObjectBuilder()
                .add("type", "resource")
                .add("categorie", buildJson(c))
                .build();
        return json;
    }
    
    private JsonArray getCategorieList() {
        JsonArrayBuilder jab = Json.createArrayBuilder();
        this.cm.findAll().forEach((c) -> {
            jab.add(buildJson(c));
            });
        return jab.build();
    }
    
    private JsonObject buildJson(Categorie c) {
        return Json.createObjectBuilder()
                .add("id",c.getId())
                .add("nom", c.getNom())
                .add("desc", c.getDescription())
                .build();
    }
}
