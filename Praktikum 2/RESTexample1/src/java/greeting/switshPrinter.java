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
import javax.json.JsonWriter;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import panel.Panel;
import panel.druckAuftrag;
import panel.panelManager;

/**
 *
 * @author joe
 */
@Path("/switch")
public class switshPrinter {
   // @Context
    private UriInfo context;
    panelManager pM = null;
    String f = "nix";
   
    //@GET
    //@Path("/status")
    public switshPrinter(@QueryParam("on") boolean value) { 
        if (pM == null && value==true){
            Panel.work = true;
            pM = new panelManager();
            f = "new panelManager()"+value;
            
        }
        if ( value == false){
            Panel.work = false;
            f = "Panel.work = false;"+value;
        }
        if (pM != null && value == true){
            Panel.work = true;
            f = "pM != null"+value;
        }
    }
    
    //@Path("/sendprintjob")
    public void resiveJob(@QueryParam("dateiPfad") String value) { 
        System.out.println("value"+value);
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
        String x;
        if(Panel.work){x="on";}else{x="off";}

         JsonObject versionObject = Json.createObjectBuilder()
                    .add("debug",  f )
                    .add("status",  x )
                    .add("countedJobsInQueue",  Panel.druckAuftraege.size() )
                    .add("jobsInQeueu", getPrinterQeeue())
                    .add("errors", getPrinterErrors())
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
    private JsonArrayBuilder getPrinterErrors(){
        JsonArrayBuilder jArray = Json.createArrayBuilder();
        /* Neuer Versuch!!
        for (UUID key : Panel.errors.keySet()) {
            System.out.println("Key = " + key + " - " + Panel.errors.get(key));
            //jArray.add(Panel.druckAuftraege.get(i));
            JsonObject jo = Json.createObjectBuilder()
                    .add("errorCode", Panel.errors.get(key).getErrors().getErrorNummer())
                    //Hier noch wine For schleife???
                    
                    .add("errorGrund", Panel.errors.get(key).getErrorGrund())
                    .add("errorText", Panel.errors.get(key).getErrorText())
                    .build();
            jArray.add(jo);
        }*/
        
        /*
        for (int i=0; i < Panel.errors.size(); i++){
            //jArray.add(Panel.druckAuftraege.get(i));
            JsonObject jo = Json.createObjectBuilder()
                    Panel.errors.values()
                    .add("errorCode", Panel.errors.get(i).getErrorNummer())
                    .add("errorGrund", Panel.errors.get(i).getErrorGrund())
                    .add("errorText", Panel.errors.get(i).getErrorText())
                    .build();
            jArray.add(jo);
        }
        */
        if(jArray == null){
            JsonObject jo = Json.createObjectBuilder().add("", "").build();
            jArray.add(jo);
        }
        return jArray;
    }
}
