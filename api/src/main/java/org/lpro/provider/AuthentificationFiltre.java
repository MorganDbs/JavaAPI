package org.lpro.provider;

import org.lpro.control.KeyManagement;
import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import io.jsonwebtoken.Jwts;
import java.security.Key;
import javax.inject.Inject;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthentificationFiltre implements ContainerRequestFilter {

    @Inject
    private KeyManagement keyManagement;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Récupère le header HTTP à partir de la requête
        String authHeader
                = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        // On vérifie que le header Authorization est présent et formatté correctement
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Probleme header autorisation");
        }
        // On extrait le token, et on vérifie qu'il est valide
        String token = authHeader.substring("Bearer".length()).trim();

        try {
            // Valide le token...
            Key key = keyManagement.generateKey();
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            System.out.println(">>>> token valide : " + token);
        } catch (Exception e) {
            // ... ou pas
            System.out.println(">>>> token invalide : " + token);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

}
