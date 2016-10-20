/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greeting;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

/**
 * REST Web Service
 *
 * @author debian
 */
@Path("greeting")
public class greetingResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of greetingResource
     */
    public greetingResource() {
    }

    /**
     * Retrieves representation of an instance of greeting.greetingResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("text/html")
    public String getHtml() {
        return("<html><body><h1>Hello World!</body></h1></html>");
    }

    
    
}
