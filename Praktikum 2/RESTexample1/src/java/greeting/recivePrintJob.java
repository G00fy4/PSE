/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greeting;

import java.io.StringWriter;
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



@Path("sendprintjob")
public class recivePrintJob {

    @Context
    private UriInfo context;
    
    String reciveData = null;
    /**
     * Creates a new instance of VersionResource
     */
    public recivePrintJob(@QueryParam("dateiPfad") String value) { 
        reciveData = value;
        druckAuftrag a = new druckAuftrag(value,"blue");
        Panel.druckAuftraege.add(a);
        
    }

    /**
     * Retrieves representation of an instance of greeting.VersionResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson() {
       JsonObject versionObject = Json.createObjectBuilder()
                    //.add("status",  systemStatus )
                    .add("countedJobsInQueue",  Panel.druckAuftraege.size() )
                    .add("jobsInQeueu", getPrinterQeeue())
                    //.add("errors", getPrinterErrors())
                    .build();
         
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = Json.createWriter(stringWriter);
        writer.writeObject(versionObject);
        writer.close();
        
        String result =  stringWriter.getBuffer().toString();
        System.out.println("getJson calculates and returns "  + result );
        return result;
    }
    
     private JsonArrayBuilder getPrinterQeeue(){
        JsonArrayBuilder jArray = Json.createArrayBuilder();
        for (int i=0; i < Panel.druckAuftraege.size(); i++){
            //jArray.add(Panel.druckAuftraege.get(i));
            JsonObject jo = Json.createObjectBuilder()
                    .add("dateiPfad", Panel.druckAuftraege.get(i).getPfad())
                    .add("farbe",Panel.druckAuftraege.get(i).getFarbe())
                    .add("hasError",Panel.druckAuftraege.get(i).hasError())
                    .add("status",Panel.druckAuftraege.get(i).getStatus())
                    .build();
            jArray.add(jo);
        }
        
        if(jArray == null){
            JsonObject jo = Json.createObjectBuilder().add("", "").build();
            jArray.add(jo);
        }
        return jArray;
    }

}

