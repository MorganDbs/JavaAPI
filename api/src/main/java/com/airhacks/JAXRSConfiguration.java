package com.airhacks;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Configures a JAX-RS endpoint. Delete this class, if you are not exposing
 * JAX-RS resources in your application.
 *
 * @author airhacks.com
 */
@ApplicationPath("api")
public class JAXRSConfiguration extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(org.lpro.boundary.CategorieResource.class);
        classes.add(org.lpro.boundary.CategorieNotFound.class);
        classes.add(org.lpro.boundary.CategorieNotFoundMapper.class);
        classes.add(org.lpro.boundary.AuthentificationBoundary.class);
        classes.add(org.lpro.boundary.CarteFideliteRessource.class);
        classes.add(org.lpro.boundary.CarteFideliteManager.class);
        classes.add(org.lpro.boundary.CommandeManager.class);
        classes.add(org.lpro.boundary.CommandeNotFound.class);
        classes.add(org.lpro.boundary.CommandeNotFoundMapper.class);
        classes.add(org.lpro.boundary.CommandeResource.class);
        classes.add(org.lpro.boundary.EndpointBoundary.class);
        classes.add(org.lpro.boundary.QuantityManager.class);
        classes.add(org.lpro.boundary.SandwichManager.class);
        classes.add(org.lpro.boundary.SandwichNotFound.class);
        classes.add(org.lpro.boundary.SandwichNotFoundMapper.class);
        classes.add(org.lpro.boundary.SandwichResource.class);
        classes.add(org.lpro.boundary.TailleManager.class);
        classes.add(org.lpro.boundary.ValidationMapper.class);
        classes.add(com.github.phillipkruger.apiee.ApieeService.class);
        return classes;
    }

}
