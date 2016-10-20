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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import java.io.StringWriter;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
 

/**
 * REST Web Service
 *
 * @author debian
 */
@Path("version")
public class VersionResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of VersionResource
     */
    public VersionResource() {
    }

    /**
     * Retrieves representation of an instance of greeting.VersionResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson() {
         JsonObject versionObject = Json.createObjectBuilder()
                    .add("Version",  0.0 )
                    .build();
         
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = Json.createWriter(stringWriter);
        writer.writeObject(versionObject);
        writer.close();
        
        String result =  stringWriter.getBuffer().toString();
        System.out.println("getJson calculates and returns "  + result );
        return result;
    }

}

