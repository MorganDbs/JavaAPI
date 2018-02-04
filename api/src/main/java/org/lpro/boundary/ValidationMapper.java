package org.lpro.boundary;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ValidationMapper implements ExceptionMapper<ConstraintViolationException> {
    
    @Override
    public Response toResponse(ConstraintViolationException ex) {
        Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
        return builder.build();
    }
}
