/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonWriter;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/**
 *
 * @author joe
 */
public class Dashboard {
    boolean isREST = false;
    String broker       ="tcp://127.0.0.1:1883";
    String clientId     = "Dashboard";
    String clientIDCost = "DashboardCoast";
    int qos             = 2;
    String topicRecivePrinter = "recivePrinter";
    String topicRequestPrinter = "requestPrinter";
    
    
    

    MQoutput mqRequestPrinter;
    MqttClient mqRecivePrinter;
    Map<String,JsonObject> pinterStatusMap = new HashMap<String,JsonObject>();
    
    private void init(){
        //Drucker 
        if (isREST){
        printerMap.put(printerMap.size()+1, new printerServer("p1","http://localhost:8080/RESTexample1/webresources/"));
        printerMap.put(printerMap.size()+1, new printerServer("p2","http://localhost:8081/"));
        } else {
            mqIniPrinterStatus();
            mqCalkulateKost();
        }
        //Dateien zum Drucken
        druckbareDateien.add("/home/joe/Dokumente/h-da/Verteilte Systeme/Praktikum 2/BeispielSocketsInJava.tar.gz");
        druckbareDateien.add("/home/Tomatenjoe/Dokumente/h-da/Verteilte Systeme/Praktikum 2/BeispielSocketsInJava.tar.gz");
        druckbareDateien.add("/home/Debian/Dokumente/h-da/Verteilte Systeme/Praktikum 2/BeispielSocketsInJava.tar.gz");
        
        //MainMenu (Adapter pattern)
        mainMenu.put(mainMenu.size()+1, new menuItem("Druckauftrag einreichen", new MoveAction() { public void run() { sendPrintjob(); } }));
        mainMenu.put(mainMenu.size()+1, new menuItem("Übersicht aller Drucker", new MoveAction() { public void run() { getUebersichtDruckaufträge(); } }));
        mainMenu.put(mainMenu.size()+1, new menuItem("Druckaufträge ansehen", new MoveAction() { public void run() { showDruckaufträge(); } }));
        if (isREST){
            mainMenu.put(mainMenu.size()+1, new menuItem("Druckaufträge verwalten", new MoveAction() { public void run() { editDruckaufträge(); } }));
            mainMenu.put(mainMenu.size()+1, new menuItem("Einen Drucker einschalten", new MoveAction() { public void run() { putOnPrinter(); } }));
            mainMenu.put(mainMenu.size()+1, new menuItem("Einen Drucker ausschalten", new MoveAction() { public void run() { putOffPrinter(); } }));
            mainMenu.put(mainMenu.size()+1, new menuItem("Alle Drucker einschalten", new MoveAction() { public void run() { putOnAllPrinters(); } }));
        }
        
        //Druck auftrag Verwaltung Menu (Adapter pattern)
        if (isREST){
            auftragVerwaltungMenu.put(auftragVerwaltungMenu.size()+1, new jobMenuItem("Druckauftrag löschen",  new MoveJobAction() { public void run(printerServer p, JsonObject x) { removeJob(p,x); } }));
            auftragVerwaltungMenu.put(auftragVerwaltungMenu.size()+1, new jobMenuItem("Druckauftrag priorisieren",  new MoveJobAction() { public void run(printerServer p, JsonObject x) { prioJob(p,x); } }));
            auftragVerwaltungMenu.put(auftragVerwaltungMenu.size()+1, new jobMenuItem("[wirft Error] fehlgeschlagener Druckauftrag wiederholen",  new MoveJobAction() { public void run(printerServer p, JsonObject x) { retryJob(p,x); } }));
        }
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
        
        System.out.println("Es sind "+printerMap.size()+" DruckerServer bekannt.");
        if(isREST){
            System.out.println("Davon sind " +onlineServer()+ " Server erreichbar.");
        }
        
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
        if (isREST){
            for (Integer key = 1; key <= printerMap.size(); key++) {
            //for (Integer key : printerMap.keySet()) {
            WebTarget versionResource = client.target(printerMap.get(key).getURL()).path("switch").queryParam("on", false);
            String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
            }
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
                System.out.println("  "+auftraeg.getString("auftrag")+ " Auftraggeber: "+ auftraeg.getString("auftraggeber")+" Status:'"+auftraeg.getString("status")+"' Datei: "+auftraeg.getString("dateiPfad"));
            
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
        for (Integer key = 1; key <= printerMap.size(); key++) {
        //for (Integer key : printerMap.keySet()) {
            JsonObject jsonObj = getStatusJsonObjekt(printerMap.get(key));
            System.out.println("Drucker '"+printerMap.get(key).getName()+"' Status: "+jsonObj.getJsonString("status").getString()+" hat "+jsonObj.getInt("countedJobsInQueue")+" Aufträge. Davon "+jsonObj.getInt("countedJobsWithErrors")+" Aufträge mit Fehlern");  
        }
    }
    
    private JsonObject getStatusJsonObjekt(printerServer p){
        JsonObject jsonObj = null;
        if(isREST){
            WebTarget resource = client.target(p.getURL()).path("status");
            String jsonString = resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
            JsonReader reader = Json.createReader(new StringReader( jsonString ));
            jsonObj = reader.readObject();
            reader.close();
        } else {
            jsonObj = pinterStatusMap.get(p.getURL());
        }
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
        for (Integer key = 1; key <= printerMap.size(); key++) {
        //for (Integer key : printerMap.keySet()) {
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
            for (Integer key = 1; key <= printerMap.size(); key++) {
            //for (Integer key : printerMap.keySet()) {
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
        for (Integer key = 1; key <= printerMap.size(); key++) {
        //for (Integer key : printerMap.keySet()) {
            try{
            WebTarget resource = client.target(printerMap.get(key).getURL()).path("status");
            String jsonString = resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
               if (!jsonString.isEmpty()){i++;}
            } catch (Exception ex) {
                    printerMap.remove(key);
                }
        }
        return i;
    }

   /* public String getHtml() throws ClientErrorException {
        WebTarget webTarget;
        webTarget = client.target(printerMap.get("p1").getURL()).path("greeting");
        WebTarget resource = webTarget;
        return resource.request(javax.ws.rs.core.MediaType.TEXT_HTML).get(String.class);
    }*/

    public void close() {
        client.close();
    } 
    /*
    public double getVersion()  throws ClientErrorException {
        WebTarget versionResource = client.target(printerMap.get("p1").getURL()).path("version");
        String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        
        JsonReader reader = Json.createReader(new StringReader( jsonString ));
        JsonObject versionObject = reader.readObject();
        reader.close();
        
        double version = versionObject.getJsonNumber( "Version" ).doubleValue();
        return version;
    }*/
    
       public void sendPrintjob(){
        //zu druckende Datei auswählen
        Integer dateiZumDrucken = waehleDatei();
        if (dateiZumDrucken != 0){
            printerServer printer = waehleDrucker("beauftragen");
            if (printer != null){
                // TODO: wähle Farbe
                
                if(isREST){
                WebTarget versionResource = client.target(printer.getURL()).path("sendprintjob").queryParam("dateiPfad", druckbareDateien.get(dateiZumDrucken-1));
                String jsonString = versionResource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
                JsonReader reader = Json.createReader(new StringReader( jsonString ));
                JsonObject jsonObj = reader.readObject();
                reader.close();
                } else {
                    mqSendPrinterJob(printer.getURL(), druckbareDateien.get(dateiZumDrucken-1));
                }
                
                System.out.println("Druckauftrag gesendet. ");
            } else {System.out.println("Erfassung des Druckauftrags abgebrochen.");}
        } else {System.out.println("Erfassung des Druckauftrags abgebrochen.");}
        
         // Integer aktuelleAufträge = getStatusJsonObjekt(printer).getInt("countedJobsInQueue");
        //String data = jsonObj.getJsonString("countedJobsInQueue").getString();
        //int jobsInQeeue = jsonObj.getInt("countedJobsInQueue");
        
       }
       
       protected void mqSendPrinterJob(String aktuellePrinterID, String printFile){
        JsonObject JsonObj = Json.createObjectBuilder()
                    .add("druckerID", aktuellePrinterID)
                    .add("from", clientId)
                    .add("auftrag",  "printFile")
                    .add("datei",  printFile)
                    .build();
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = Json.createWriter(stringWriter);
        writer.writeObject(JsonObj);
        writer.close();
        String result =  stringWriter.getBuffer().toString();
        try{
            mqRequestPrinter.sendMessage(result);
        } catch (MqttException me) {
            mqttExceptionHandler(me);
        }
        
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
       
      
    public void mqIniPrinterStatus(){
        mqRequestPrinter = new MQoutput(clientId,broker,topicRequestPrinter);
        
        try {
            mqRecivePrinter = new MqttClient(broker, clientId+"Com",new MemoryPersistence());
        
            mqRecivePrinter.setCallback(new MqttCallback(){
            @Override
               public void connectionLost(Throwable throwable) { 
                   System.out.println("ERROR:" +clientId+" connectionLost from "+broker+" Grund: "+throwable);
                   try {
                        mqRecivePrinter.connect();
                    } catch (MqttException ex) {
                        mqttExceptionHandler(ex);
                    }
               }

               @Override
               public void messageArrived(String t, MqttMessage m) throws Exception {
                   String message = new String(m.getPayload());
 //                System.out.println("Input: "+message);
                 JsonReader reader = Json.createReader(new StringReader( message ));
                JsonObject jsonObj = reader.readObject();
                if(jsonObj.getJsonString("druckerID").getString().contains(jsonObj.getJsonString("from").getString())){
                    //Nachricht von einem Drucker
//                    System.out.println("Drucker '"+jsonObj.getJsonString("druckerID").getString()+"' Status: "+jsonObj.getJsonString("status").getString()+" hat "+jsonObj.getInt("countedJobsInQueue")+" Aufträge. Davon "+jsonObj.getInt("countedJobsWithErrors")+" Aufträge mit Fehlern");  
                    pinterStatusMap.put(jsonObj.getJsonString("druckerID").getString(), jsonObj);
                    
                    //Neue Printer in PrinterMenu hinzufügen
                    //if (!mqPrinterMap.containsValue(jsonObj.getJsonString("druckerID").getString())){
                    if (!isPrinter(jsonObj.getJsonString("druckerID").getString())){
                        printerMap.put(printerMap.size()+1, new printerServer("p"+printerMap.size()+1,jsonObj.getJsonString("druckerID").getString())); 
                    }
                    
                }
                
               }

               @Override
               public void deliveryComplete(IMqttDeliveryToken t) { }
               });

        mqRecivePrinter.connect();

        mqRecivePrinter.subscribe(topicRecivePrinter);
        
        
        sendHalloPrinters();
            
        try {
                    Thread.sleep(2000);
        } catch (InterruptedException ex) {
                    Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        } catch (MqttException me) {
            mqttExceptionHandler(me);
        } 
    }
    
    private boolean isPrinter(String p){
        boolean found = false;
        for (Integer i =1; i <=printerMap.size(); i++ ){
            if(printerMap.get(i).getURL().contains(p)){found = true;}
        }
        return found;
    }
    
    private void mqCalkulateKost(){
        MqttClient mqCostRequest;
        try {
            mqCostRequest = new MqttClient(broker, clientIDCost+"2", new MemoryPersistence());
        
            mqCostRequest.setCallback(new MqttCallback(){
            @Override
               public void connectionLost(Throwable throwable) { 
                   System.out.println("ERROR:" +clientIDCost+" CostRequest connectionLost from "+broker+" Grund: "+throwable);
               }

               @Override
               public void messageArrived(String t, MqttMessage mm) throws Exception {
                 String text = new String(mm.getPayload());
                    JsonReader reader = Json.createReader(new StringReader(text));
                JsonObject jsonObj = reader.readObject();
                System.out.println("DruckerQueue Input:"+text);
                if (isPrinter(jsonObj.getJsonString("druckerID").getString()) && !isPrinter(jsonObj.getJsonString("from").getString()) && !jsonObj.getJsonString("from").getString().contains(clientId)){
                        //Naricht ist für drucker
                        if (jsonObj.getJsonString("auftrag").getString().contains("printFile")){//Auftrag entgegennehmen!!!
                                mqSendeKundeRechnung(jsonObj.getJsonString("from").getString(),jsonObj.getJsonString("datei").getString());
                            }
                    }
               }
               
               @Override
               public void deliveryComplete(IMqttDeliveryToken t) { }
               });

        mqCostRequest.connect();
        mqCostRequest.subscribe(topicRequestPrinter);
        } catch (MqttException ex) {
            mqttExceptionHandler(ex);
        }
    }
    private void mqSendeKundeRechnung(String kundenID, String Datei){
        JsonObject versionObject = Json.createObjectBuilder()
                    .add("druckerID", kundenID)
                    .add("from", clientId+"MASTER")
                    .add("bill",  getPreis(kundenID, Datei) )
                    .build();
         
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = Json.createWriter(stringWriter);
        writer.writeObject(versionObject);
        writer.close();
        
        String result =  stringWriter.getBuffer().toString();
        
        
        MqttClient mqCostReplay;
        try {
            mqCostReplay = new MqttClient(broker, clientIDCost, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
 //           System.out.println("Connecting to broker: "+broker);
            mqCostReplay.connect(connOpts);
 //           System.out.println("Connected");
            MqttMessage message = new MqttMessage(result.getBytes());
            message.setQos(qos);
            mqCostReplay.publish(topicRecivePrinter, message);
            mqCostReplay.disconnect();
        } catch (MqttException ex) {
            mqttExceptionHandler(ex);
        }
    }
    Map<String,Integer> kundenrechnungen = new HashMap<String,Integer>();
    private Integer getPreis(String KundenID, String Datei){
        //Hier KundenDatenbank abfrage usw.
        Integer zuZahlen = 5;
        if(kundenrechnungen.containsKey(KundenID)){
            zuZahlen= kundenrechnungen.get(KundenID)+zuZahlen;
        }
        kundenrechnungen.put(KundenID, zuZahlen);
        return zuZahlen;
    }
       
    private void sendHalloPrinters(){
        JsonObject JsonObj = Json.createObjectBuilder()
                        .add("druckerID", "hallo")
                        .add("from", clientId)
                        .add("auftrag",  "hallo")
                        .build();
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = Json.createWriter(stringWriter);
            writer.writeObject(JsonObj);
            writer.close();
            String result =  stringWriter.getBuffer().toString();
           
        try {
            mqRequestPrinter.sendMessage(result);
        } catch (MqttException ex) {
            mqttExceptionHandler(ex);
        }
    }
    
    private void mqttExceptionHandler(MqttException me){
        System.out.println("reason "+me.getReasonCode());
        System.out.println("msg "+me.getMessage());
        System.out.println("loc "+me.getLocalizedMessage());
        System.out.println("cause "+me.getCause());
        System.out.println("excep "+me);
        me.printStackTrace();
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
