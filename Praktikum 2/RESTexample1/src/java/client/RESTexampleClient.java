/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package client;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
 
/**
 * Jersey REST client generated for REST resource:greetingResource
 * [greeting]<br>
 * USAGE:
 * <pre>
 *        RESTexampleClient client = new RESTexampleClient();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author debian
 */
public class RESTexampleClient {
    private WebTarget webTarget;
    private Client client;
    private static final String BASE_URI = "http://localhost:8080/WEB-INF/webresources";

    public RESTexampleClient() {
        client = javax.ws.rs.client.ClientBuilder.newClient();
        webTarget = client.target(BASE_URI).path("greeting");
    }

    public String getHtml() throws ClientErrorException {
        WebTarget resource = webTarget;
        return resource.request(javax.ws.rs.core.MediaType.TEXT_HTML).get(String.class);
    }

    public void close() {
        client.close();
    } 
    
    public double getVersion()  throws ClientErrorException {
        WebTarget versionResource = client.target(BASE_URI).path("version");
        String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        
        JsonReader reader = Json.createReader(new StringReader( jsonString ));
        JsonObject versionObject = reader.readObject();
        reader.close();
        
        double version = versionObject.getJsonNumber( "Version" ).doubleValue();
        return version;
    }
    
       public boolean sendPrintjob(){
        
        //String datei = "/home/tomatenjoe/Dokumente/h-da/Verteilte Systeme/Praktikum 1/BeispielSocketsInJava.tar.gz";
        String datei = "/home/joe/Dokumente/h-da/Verteilte Systeme/Praktikum 1/BeispielSocketsInJava.tar.gz";
        
        WebTarget versionResource = client.target(BASE_URI).path("sendprintjob").queryParam("dateiPfad", datei);
        String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
           
        JsonReader reader = Json.createReader(new StringReader( jsonString ));
        JsonObject versionObject = reader.readObject();
        reader.close();
          
        String data = versionObject.getJsonString("pintData").getString();
        if (data.equalsIgnoreCase(datei)){
            return true;
        }
        return false;
       }
    public static void main(String[] args) {
        RESTexampleClient client = new RESTexampleClient();
        String html = client.getHtml();
        System.out.println( "GET returns: " + html);
        System.out.println( "Version == " + client.getVersion() );
        if (client.sendPrintjob()){
            System.out.println( "Durckauftrag erfolgreich gesendet. ");
        } else {
            System.out.println( "Durckauftrag Error. ");
        }
        client.close();
    }
 
}

