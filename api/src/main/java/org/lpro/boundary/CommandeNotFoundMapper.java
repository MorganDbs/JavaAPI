package org.lpro.boundary;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class CommandeNotFoundMapper implements ExceptionMapper<CommandeNotFound> {

    public Response toResponse(CommandeNotFound exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorJson(exception))
                .build();
    }

    private JsonObject errorJson(CommandeNotFound exception) {
        return Json.createObjectBuilder()
                .add("error", Response.Status.NOT_FOUND.getStatusCode())
                .add("message", exception.getMessage())
                .build();
    }

}
