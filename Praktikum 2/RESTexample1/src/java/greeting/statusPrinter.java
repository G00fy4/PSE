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
@Path("/status")
public class statusPrinter {
   // @Context
    private UriInfo context;
    panelManager pM = null;
    String f = "nix";
   public statusPrinter(){}
    //@GET
    //@Path("/status")
   /*
    public void statusPrinter(@QueryParam("on") boolean value) { 
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
    */
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
                    .add("countedJobsWithErrors", Panel.errors.size())
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
                    .add("auftrag", Panel.druckAuftraege.get(i).getTimestamp().toString())
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
    private JsonArrayBuilder getErrorsByAuftrag(druckAuftrag a){
        JsonArrayBuilder jArray = Json.createArrayBuilder();
        for (Integer i = 0; i< a.getErrors().size(); i++){
            JsonObject jo = Json.createObjectBuilder()
                    .add("errorCode", a.getErrors().get(i).getErrorNummer())
                    //Hier noch wine For schleife???
                    
                    .add("errorGrund", a.getErrors().get(i).getErrorGrund())
                    .add("errorText", a.getErrors().get(i).getErrorText())
                    .build();
            jArray.add(jo);
            }
        return jArray;
    }
    private JsonArrayBuilder getPrinterErrors(){
        JsonArrayBuilder jArray = Json.createArrayBuilder();
         
        for (UUID key : Panel.errors.keySet()) {
            String lastError = Panel.errors.get(key).getErrors().get(Panel.errors.get(key).getErrors().size()-1).getErrorNummer()+ ":" +Panel.errors.get(key).getErrors().get(Panel.errors.get(key).getErrors().size()-1).getErrorText()+" - "+Panel.errors.get(key).getStatus();
            //System.out.println("Auftrag '" + key + "' hat " + Panel.errors.get(key).getErrors().size()+" Fehler.");
            JsonObject joAuftrag = Json.createObjectBuilder()
                    .add("auftrag", Panel.errors.get(key).getTimestamp().toString())
                    .add("dateiPfad", Panel.errors.get(key).getPfad())
                    .add("farbe",Panel.errors.get(key).getFarbe())
                    .add("hasError",Panel.errors.get(key).hasError())
                    .add("status",lastError)
                    
                    .add("errors", getErrorsByAuftrag(Panel.errors.get(key)))
                      .build();
            jArray.add(joAuftrag);
        }
        
        if(jArray == null){
            JsonObject jo = Json.createObjectBuilder().add("", "").build();
            jArray.add(jo);
        }
        return jArray;
    }
}
