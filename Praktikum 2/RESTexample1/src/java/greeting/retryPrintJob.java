/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greeting;

import java.io.StringWriter;
import java.util.UUID;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import panel.Panel;
import panel.druckAuftrag;



@Path("/retryprintjob")
public class retryPrintJob {

    @Context
    private UriInfo context;
    
    String systemStatus = "Auftrag nicht gefunden.";
    /**
     * Creates a new instance of VersionResource
     * @param value
     */
    public retryPrintJob(@QueryParam("auftragID") String value) { 
        System.out.println("value:"+value+":");
        UUID a = UUID.fromString(value);
        
            for (Integer i = 0; i < Panel.errors.size(); i++){
                if (Panel.errors.get(i).getTimestamp().equals(a)){
                    Panel.errors.get(i).retry(); 
                    Panel.druckAuftraege.add(Panel.errors.get(i));
                    Panel.errors.remove(i);
                    systemStatus = "Auftrag erfolgreich in Druckerwarteschlange hinzugefügt.";
                    break;
                }
            }
        
        //systemStatus = systemStatus + " " +a;
    }

    /**
     * Retrieves representation of an instance of greeting.VersionResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson() {
       JsonObject versionObject = Json.createObjectBuilder()
                    .add("status",  systemStatus )
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

