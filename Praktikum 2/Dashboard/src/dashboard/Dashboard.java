/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;


/**
 *
 * @author joe
 */
public class Dashboard {


    private void init(){
        //Drucker 
        printerMap.put(printerMap.size()+1, new printerServer("p1","http://localhost:8080/WEB-INF/webresources/"));
        printerMap.put(printerMap.size()+1, new printerServer("p2","http://141.100.42.133:8080/WEB-INF/webresources/"));
        printerMap.put(printerMap.size()+1, new printerServer("p3","http://141.100.42.138:8080/WEB-INF/webresources/"));
        printerMap.put(printerMap.size()+1, new printerServer("p4","http://localhost:8081/RESTexample1/webresources/"));
        
        //Dateien zum Drucken
        druckbareDateien.add("/home/joe/Dokumente/h-da/Verteilte Systeme/Praktikum 2/BeispielSocketsInJava.tar.gz");
        druckbareDateien.add("/home/Tomatenjoe/Dokumente/h-da/Verteilte Systeme/Praktikum 2/BeispielSocketsInJava.tar.gz");
        druckbareDateien.add("/home/Debian/Dokumente/h-da/Verteilte Systeme/Praktikum 2/BeispielSocketsInJava.tar.gz");
        druckbareDateien.add("/home/Debian/Dokumente/Praktikum 2/BeispielSocketsInJava.tar.gz");
        
        //MainMenu (Adapter pattern)
        mainMenu.put(mainMenu.size()+1, new menuItem("Druckauftrag einreichen", new MoveAction() { public void run() { sendPrintjob(); } }));
        mainMenu.put(mainMenu.size()+1, new menuItem("Übersicht aller Drucker", new MoveAction() { public void run() { getUebersichtDruckaufträge(); } }));
        mainMenu.put(mainMenu.size()+1, new menuItem("Druckaufträge ansehen", new MoveAction() { public void run() { showDruckaufträge(); } }));
        mainMenu.put(mainMenu.size()+1, new menuItem("Druckaufträge verwalten", new MoveAction() { public void run() { editDruckaufträge(); } }));
        mainMenu.put(mainMenu.size()+1, new menuItem("Einen Drucker einschalten", new MoveAction() { public void run() { putOnPrinter(); } }));
        mainMenu.put(mainMenu.size()+1, new menuItem("Einen Drucker ausschalten", new MoveAction() { public void run() { putOffPrinter(); } }));
        mainMenu.put(mainMenu.size()+1, new menuItem("Alle Drucker einschalten", new MoveAction() { public void run() { putOnAllPrinters(); } }));
        
        //Druck auftrag Verwaltung Menu (Adapter pattern)
        auftragVerwaltungMenu.put(auftragVerwaltungMenu.size()+1, new jobMenuItem("Druckauftrag löschen",  new MoveJobAction() { public void run(printerServer p, JsonObject x) { removeJob(p,x); } }));
        auftragVerwaltungMenu.put(auftragVerwaltungMenu.size()+1, new jobMenuItem("Druckauftrag priorisieren",  new MoveJobAction() { public void run(printerServer p, JsonObject x) { prioJob(p,x); } }));
        auftragVerwaltungMenu.put(auftragVerwaltungMenu.size()+1, new jobMenuItem("[wirft Error] fehlgeschlagener Druckauftrag wiederholen",  new MoveJobAction() { public void run(printerServer p, JsonObject x) { retryJob(p,x); } }));
        
        //wähle Drucker Menu (Adapter pattern)
        waehleDruckerMenu.put(waehleDruckerMenu.size()+1, new printerMenuItem("Drucker mit der kürzesten Warteschlange", new chosePrinterAction() { public printerServer run() { return getPrinterWhitSmalestJopList(); } }));
        waehleDruckerMenu.put(waehleDruckerMenu.size()+1, new printerMenuItem("Irgend einen Drucker", new chosePrinterAction() { public printerServer run() { return getPrinterWhitSmalestJopList(); } }));
    }
    
    private List<String> druckbareDateien = new ArrayList<String>();
    private Map<Integer,printerServer> printerMap = new HashMap<Integer,printerServer>();
    private Map<Integer,menuItem> mainMenu = new HashMap<Integer,menuItem>();
    private Map<Integer,jobMenuItem> auftragVerwaltungMenu = new HashMap<Integer,jobMenuItem>();
    private Map<Integer,printerMenuItem> waehleDruckerMenu = new HashMap<Integer,printerMenuItem>();
    private Client client;
    
    
    public Dashboard() {
        init();
        client = javax.ws.rs.client.ClientBuilder.newClient();
        
        System.out.println("Willkommen auf dem Dashboad!");
        System.out.println("Es sind "+printerMap.size()+" DruckerServer bekannt. Davon sind " +onlineServer()+ " Server erreichbar.");
        
        getUebersichtDruckaufträge();
        // Menue zum Drucken
        Integer eingabe = -1;
        
        while (eingabe != 0){
            
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isr);
            System.out.println("\nWas möchten Sie machen? ");
            for (Integer i = 1; i <= mainMenu.size(); i++) {
                System.out.println("["+i+"] "+mainMenu.get(i).getText());
            }
            System.out.println("[0] Alle Systeme auschalten \n");
            
            try {
                eingabe = Integer.parseInt(br.readLine());
            } catch (IOException ex) {
                Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (eingabe > mainMenu.size() || eingabe <= 0){
                if(eingabe != 0){
                    System.out.println("Geben Sie ein Zahl zwischen 0 und "+mainMenu.size()+" an.");
                }
            } else {
                
                mainMenu.get(eingabe).runFunk();
            }
        }
        for (Integer key : printerMap.keySet()) {
        WebTarget versionResource = client.target(printerMap.get(key).getURL()).path("switch").queryParam("on", false);
        String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }
        System.out.println("Auf Wiedersehen.");
    }
    private void retryJob(printerServer p,JsonObject auftrag){
        System.out.println("value:"+auftrag.getJsonString("auftrag").getString()+":");
        WebTarget versionResource = client.target(p.getURL()).path("/retryprintjob").queryParam("auftragID", auftrag.getJsonString("auftrag").getString());
        String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        JsonReader reader = Json.createReader(new StringReader( jsonString ));
        JsonObject statusObject = reader.readObject();
        reader.close();
        
        System.out.println(statusObject.getJsonString("status").toString());
    }
    private void prioJob(printerServer p,JsonObject auftrag){ 
        WebTarget versionResource = client.target(p.getURL()).path("/prioprintjob").queryParam("auftragID", auftrag.getJsonString("auftrag").getString());
        String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        JsonReader reader = Json.createReader(new StringReader( jsonString ));
        JsonObject statusObject = reader.readObject();
        reader.close();
        
        System.out.println(statusObject.getJsonString("status").toString());
    }
    private void removeJob(printerServer p,JsonObject auftrag){
        //System.out.println("auftrag:"+auftrag.getJsonString("auftrag").getString()+":"); 
        WebTarget versionResource = client.target(p.getURL()).path("/removeprintjob").queryParam("auftragID", auftrag.getJsonString("auftrag").getString());
        String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        JsonReader reader = Json.createReader(new StringReader( jsonString ));
        JsonObject statusObject = reader.readObject();
        reader.close();
        
        System.out.println(statusObject.getJsonString("status").toString());
    }
    
    private void showDruckaufträge(){
        printerServer printer = waehleDrucker("Aufträge ansehen");
        if (printer != null){
            JsonObject jsonObj = getStatusJsonObjekt(printer);
            System.out.println("Drucker '"+printer.getName()+"' Status: "+jsonObj.getJsonString("status").getString()+" hat "+jsonObj.getInt("countedJobsInQueue")+" Aufträge. Davon "+jsonObj.getInt("countedJobsWithErrors")+" Aufträge mit Fehlern"); 
            System.out.println("In der Druckerwarteschlange:");
            for (Integer i = 0; i < jsonObj.getInt("countedJobsInQueue"); i++){
                JsonObject auftraeg = jsonObj.getJsonArray("jobsInQeueu").getJsonObject(i);
                //System.out.println("  "+auftraeg.getString("auftrag")+ " Status:'"+auftraeg.getString("status")+"' Fehler: "+auftraeg.getString("hasError")+" Datei: "+auftraeg.getString("dateiPfad"));
                System.out.println("  "+auftraeg.getString("auftrag")+ " Status:'"+auftraeg.getString("status")+"' Datei: "+auftraeg.getString("dateiPfad"));
            
            }
            System.out.println("In der Error-Liste:");
            for (Integer i = 0; i < jsonObj.getInt("countedJobsWithErrors"); i++){
                JsonObject auftraeg =  jsonObj.getJsonArray("errors").getJsonObject(i);
                JsonArray error = auftraeg.getJsonArray("errors"); 
                System.out.println("  "+auftraeg.getString("auftrag")+ ":");
                for (Integer l = 0; l < error.size(); l++){
                    System.out.println("     "+error.getJsonObject(l).getJsonNumber("errorCode")+ " : " +error.getJsonObject(l).getJsonString("errorText").toString()+" - "+error.getJsonObject(l).getJsonString("errorGrund").toString());                
                }
                //System.out.println("  "+auftraeg.getString("auftrag")+ " Status:'"+auftraeg.getString("status")+"' Fehler: "+auftraeg.getString("hasError")+" Datei: "+auftraeg.getString("dateiPfad"));
                //System.out.println("  "+auftraeg.getString("auftrag")+ " Status:'"+auftraeg.getString("status")+"' Datei: "+auftraeg.getString("errorGrund"));
            }
        }
    }
    
    private void editDruckaufträge(){
        printerServer printer = waehleDrucker("Aufträge verwalten");
        if (printer != null){
            JsonObject jsonObj = getStatusJsonObjekt(printer);
            System.out.println("Drucker '"+printer.getName()+"' Status: "+jsonObj.getJsonString("status").getString()+" hat "+jsonObj.getInt("countedJobsInQueue")+" Aufträge. Davon "+jsonObj.getInt("countedJobsWithErrors")+" Aufträge mit Fehlern"); 
            JsonObject auftrag = chosePrintJob(jsonObj);
            if (auftrag != null){
                Integer eingabe = -1;
                while (eingabe != 0){

                    InputStreamReader isr = new InputStreamReader(System.in);
                    BufferedReader br = new BufferedReader(isr);
                    System.out.println("Was möchten Sie mit dem Auftrag '"+auftrag.getString("auftrag")+"' auf dem Durcker '"+printer.getName()+"' machen? ");
                    System.out.println("Ausgewählt: "+auftrag.getString("auftrag")+ " Status:'"+auftrag.getString("status")+"' Datei: "+auftrag.getString("dateiPfad"));

                    for (Integer i = 1; i <= auftragVerwaltungMenu.size(); i++) {
                        System.out.println("["+i+"] "+auftragVerwaltungMenu.get(i).getText());
                    }
                    System.out.println("[0] Abbrechen ");

                    try {
                        eingabe = Integer.parseInt(br.readLine());
                    } catch (IOException ex) {
                        Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (eingabe > auftragVerwaltungMenu.size() || eingabe <= 0){
                        if(eingabe != 0){
                            System.out.println("Geben Sie ein Zahl zwischen 0 und "+auftragVerwaltungMenu.size()+" an.");
                        } else{
                            break;
                        }
                    } else {

                        auftragVerwaltungMenu.get(eingabe).runFunk(printer, auftrag);
                        break;
                    }
                }
            }
        }
    }
    
    private JsonObject chosePrintJob(JsonObject jsonObj){
        JsonObject auserkorenerAuftrag = null;
        List<JsonObject> auftraege = new ArrayList<JsonObject>();
        //List <druckAuftrag> auftraege = jsonObj.getJsonArray("jobsInQeueu").getValuesAs(class<druckAuftrag>);
        for (Integer i = 0; i < jsonObj.getInt("countedJobsInQueue"); i++){
            auftraege.add( jsonObj.getJsonArray("jobsInQeueu").getJsonObject(i));
        }
        for (Integer i = 0; i < jsonObj.getInt("countedJobsWithErrors"); i++){
            auftraege.add( jsonObj.getJsonArray("errors").getJsonObject(i));
        }
        
           Integer eingabe = -1;
        while (eingabe != 0){
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isr);
            System.out.println("Welchen Auftrag möchten Sie verwalten? ");
           
            for (Integer i = 1; i <= auftraege.size(); i++) {
                //System.out.println("["+i+"] "+auftraege.get(i-1).getString("auftrag")+ " Status:'"+auftraege.get(i-1).getString("status")+"' Fehler: "+auftraege.get(i-1).getString("hasError")+" Datei: "+auftraege.get(i-1).getString("dateiPfad"));
                System.out.println("["+(i)+"] "+auftraege.get(i-1).getString("auftrag")+ " Status:'"+auftraege.get(i-1).getString("status")+"' Datei: "+auftraege.get(i-1).getString("dateiPfad"));
            }
            
            System.out.println("[0] Abbrechen ");
            
            try {
                eingabe = Integer.parseInt(br.readLine());
            } catch (IOException ex) {
                Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
            //eingabe parsen
            if (eingabe > auftraege.size() || eingabe <= 0){
                if(eingabe != 0){
                    System.out.println("Geben Sie ein Zahl zwischen 0 und "+auftraege.size()+" an.");
                }
            } else {
                    auserkorenerAuftrag = auftraege.get(eingabe-1);
                break;
            }
        }
        return auserkorenerAuftrag;
    }
    
    private void getUebersichtDruckaufträge(){
        for (Integer k = 1; k <= printerMap.size(); k++) {
            JsonObject jsonObj = getStatusJsonObjekt(printerMap.get(k));
            System.out.println("Drucker '"+printerMap.get(k).getName()+"' Status: "+jsonObj.getJsonString("status").getString()+" hat "+jsonObj.getInt("countedJobsInQueue")+" Aufträge. Davon "+jsonObj.getInt("countedJobsWithErrors")+" Aufträge mit Fehlern");  
        }
    }
    
    private JsonObject getStatusJsonObjekt(printerServer p){
        WebTarget resource = client.target(p.getURL()).path("status");
        String jsonString = resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        JsonReader reader = Json.createReader(new StringReader( jsonString ));
        JsonObject jsonObj = reader.readObject();
        reader.close();
        return jsonObj;
    }
    
    private void putOffPrinter(){
        printerServer printer = waehleDrucker("ausschalten");
        if (printer != null){
            WebTarget versionResource = client.target(printer.getURL()).path("switch").queryParam("off", true);
            String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
            System.out.println("DruckerSystem '"+printer.getName()+"' ausgeschaltet.");
        }
        
    }
    
    private void putOnPrinter(){
        printerServer printer = waehleDrucker("einschalten");
        if (printer != null){
            WebTarget versionResource = client.target(printer.getURL()).path("switch").queryParam("on", true);
            String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
            System.out.println("DruckerSystem '"+printer.getName()+"' eingeschaltet.");
        }
        
    }
    
    private void putOnAllPrinters(){
        for (Integer key : printerMap.keySet()) {
            WebTarget versionResource = client.target(printerMap.get(key).getURL()).path("switch").queryParam("on", true);
            String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
            System.out.println("DruckerSystem '"+printerMap.get(key).getName()+"' eingeschaltet.");
        }
    }
    
    private printerServer getPrinterWhitSmalestJopList(){
        printerServer derMitDemKurzen = null;
        if(printerMap.size()==1){
            derMitDemKurzen = printerMap.get(1);
        }else{
            Integer kleineNummer = -1;
            for (Integer key : printerMap.keySet()) {
                Integer x = getStatusJsonObjekt(printerMap.get(key)).getInt("countedJobsInQueue");
                if(kleineNummer == -1 || kleineNummer > x){
                    kleineNummer = x;
                    derMitDemKurzen = printerMap.get(key);
                }
            }
        }
        return derMitDemKurzen;
    }
    
    private Integer onlineServer(){
        Integer i = 0;
        
        for (Integer k=1; k <= printerMap.size();k++) {
            try{
                
            
            WebTarget resource = client.target(printerMap.get(k).getURL()).path("status");
            String jsonString = resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
               if (!jsonString.isEmpty()){i++;}
            } catch (Exception ex) {
                    printerMap.remove(k);
                }
        }
        return i;
    }

    public String getHtml() throws ClientErrorException {
        WebTarget webTarget;
        webTarget = client.target(printerMap.get("p1").getURL()).path("greeting");
        WebTarget resource = webTarget;
        return resource.request(javax.ws.rs.core.MediaType.TEXT_HTML).get(String.class);
    }

    public void close() {
        client.close();
    } 
    
    public double getVersion()  throws ClientErrorException {
        WebTarget versionResource = client.target(printerMap.get("p1").getURL()).path("version");
        String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        
        JsonReader reader = Json.createReader(new StringReader( jsonString ));
        JsonObject versionObject = reader.readObject();
        reader.close();
        
        double version = versionObject.getJsonNumber( "Version" ).doubleValue();
        return version;
    }
    
       public void sendPrintjob(){
        //zu druckende Datei auswählen
        Integer dateiZumDrucken = waehleDatei();
        if (dateiZumDrucken != 0){
            printerServer printer = waehleDrucker("beauftragen");
            if (printer != null){
                // TODO: wähle Farbe
                
                WebTarget versionResource = client.target(printer.getURL()).path("sendprintjob").queryParam("dateiPfad", druckbareDateien.get(dateiZumDrucken-1));
                String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);

                JsonReader reader = Json.createReader(new StringReader( jsonString ));
                JsonObject jsonObj = reader.readObject();
                reader.close();
                System.out.println("Druckauftrag gesendet. ");
            } else {System.out.println("Erfassung des Druckauftrags abgebrochen.");}
        } else {System.out.println("Erfassung des Druckauftrags abgebrochen.");}
        
         // Integer aktuelleAufträge = getStatusJsonObjekt(printer).getInt("countedJobsInQueue");
        //String data = jsonObj.getJsonString("countedJobsInQueue").getString();
        //int jobsInQeeue = jsonObj.getInt("countedJobsInQueue");
        
       }
       
       private printerServer waehleDrucker(String text){
           printerServer auserkorenerDrucker = null;
           Integer eingabe = -1;
        while (eingabe != 0){
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isr);
            System.out.println("Welchen Drucker möchten Sie "+text+"? ");
            Integer i = 1;
            while (i <= printerMap.size()) {
                System.out.println("["+i+"] "+printerMap.get(i).getName());
                i++;
            }
            while (i <= waehleDruckerMenu.size()+printerMap.size()) {
                System.out.println("["+i+"] "+waehleDruckerMenu.get(i-printerMap.size()).getText());
                i++;
            }
            System.out.println("[0] Abbrechen ");
            
            try {
                eingabe = Integer.parseInt(br.readLine());
            } catch (IOException ex) {
                Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
            //eingabe parsen
            if (eingabe > waehleDruckerMenu.size()+printerMap.size() || eingabe <= 0){
                if(eingabe != 0){
                    System.out.println("Geben Sie ein Zahl zwischen 0 und "+waehleDruckerMenu.size()+printerMap.size()+" an.");
                }
            } else {
                if (printerMap.size() >= eingabe){
                    auserkorenerDrucker = printerMap.get(eingabe);
                } else {
                    auserkorenerDrucker = waehleDruckerMenu.get(eingabe-printerMap.size()).runFunk();
                }
                break;
            }
        }
        return auserkorenerDrucker;
       }
       
       private Integer waehleDatei(){
        Integer eingabe = -1;
        while (eingabe != 0){
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isr);
            System.out.println("Welche Datei möchten Sie drucken? ");
            for (Integer i = 1; i <= druckbareDateien.size(); i++) {
                System.out.println("["+i+"] "+druckbareDateien.get(i-1));
            }
            System.out.println("[0] Abbrechen ");
            
            try {
                eingabe = Integer.parseInt(br.readLine());
            } catch (IOException ex) {
                Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (eingabe > druckbareDateien.size() || eingabe < 0){
                System.out.println("Geben Sie ein Zahl zwischen 0 und "+druckbareDateien.size()+" an.");
            } else {
                break;
            }
        }
        return eingabe;
       }
       
    public static void main(String[] args) {
        
        Dashboard client = new Dashboard();
        
        /*
        String html = client.getHtml();
        System.out.println( "GET returns: " + html);
        System.out.println( "Version == " + client.getVersion() );
        */
        
        client.close();
        
        
    }
    
}
