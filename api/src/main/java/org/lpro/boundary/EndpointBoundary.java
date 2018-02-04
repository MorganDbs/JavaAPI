package org.lpro.boundary;

import org.lpro.provider.Secured;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/endpoint")
public class EndpointBoundary {

    @GET
    @Produces("application/json")
    public Response methodeNonSecurisee() {
        // La méthode n'est pas annotée avec @Secured
        // Le filtre n'est pas exécuté
        String str = "Non sécurisé";
        JsonObject jsonResult = Json.createObjectBuilder().
                add("status", str).build();
        return Response.ok().entity(jsonResult).build();
    }

    @DELETE
    @Secured
    @Path("{id}")
    @Produces("application/json")
    public Response methodeSecurisee(@PathParam("id") Long id) {
        // La méthode est annotée avec @Secured
        // Le filtre est exécuté
        // On doit avoir un token valide
        String str = "Sécurisé";
        JsonObject jsonResult = Json.createObjectBuilder().
                add("status", str).build();
        return Response.ok().entity(jsonResult).build();
    }
}
