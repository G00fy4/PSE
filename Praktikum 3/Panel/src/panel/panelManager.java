/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package panel;

import druckerkopf.DruckerKopf;
import druckerkopf.TonerHelper;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import toner.Toner;

/**
 *
 * @author joe
 */
public class panelManager {
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
 /*   public static void main(String[] args) throws Exception {
        // TODO code application logic here
        panelManager pM = new panelManager();
 
    }*/
    boolean work = true;
    String broker       ="tcp://127.0.0.1:1883";
    public static String clientId     = "PrinterDevice01";
    int qos             = 2;
    String topicRecrestPrinter = "requestPrinter";
    String topicRecivePrinter = "recivePrinter";
    
    
    protected MQoutput mqRecrestJobs = null;
    protected static MQoutput mqResponseJobs = null;
    public panelManager(){
        mqRecrestJobs = new MQoutput(clientId+"Resive",broker,topicRecrestPrinter);
        mqResponseJobs =  new MQoutput(clientId+"Respone",broker,topicRecivePrinter);
        
        //String e =  "/home/tomatenjoe/Dokumente/h-da/Verteilte Systeme/Praktikum 1/BeispielSocketsInJava.tar.gz";
        //String e =  "/home/joe/Dokumente/h-da/Verteilte Systeme/Praktikum 1/BeispielSocketsInJava.tar.gz";
        String e = "/home/tomatenjoe/Dokumente/h-da/Verteilte Systeme/Praktikum 1/BeispielSocketsInJava.tar.gz";
        Toner.isTCPConnection = false;
        DruckerKopf.isTCPConnection = false;
        Panel.isTCPConnection = false;
        Toner.timeMultiplikator = 1;
        DruckerKopf.timeMultiplikator = 1;
        
        DruckerKopf.serverPort = 9871;
        Panel.druckerKopfIPAdresse = "127.0.0.1";
        Panel.druckerKopfPort = 9871;
        Panel.meinServerPort = 9870;
        
        
        Panel.work = true;
        TonerHelper tBlue = new TonerHelper("127.0.0.1",9872, "blue");
        DruckerKopf.toners.put("blue", tBlue);
        System.out.println("*********************************************");
        
        
        reciveJobs();
        sendStatus();
        
        Thread t;
        for (String key : DruckerKopf.toners.keySet()) {
            //Thread t = new Toner(Port,Füllstand);
            t = new Toner(DruckerKopf.toners.get(key).getPort(),1500000000);
            t.setName("Toner "+key);
            t.start();
        }
        Thread d = new DruckerKopf();
        Thread p = new Panel();
        
        
        
        d.setName("DruckerKopf ");
        p.setName("Panel ");
        try {                
            
            Thread.sleep(2000);
            d.start();
            Thread.sleep(2000);
            p.start();
        } catch (InterruptedException ex) {
            Logger.getLogger(panelManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Aus verdacht damit MQTT weiter gelesen wird?
        //while(true){}
       // mosquitto_pub -h 127.0.0.1 -t comPrinter -m '{"druckerID":"PrinterDevice01","from":"Terminmal","auftrag":"getStatus"}'
       // mosquitto_sub -h 127.0.0.1 -t comPrinter

    }
    
   
    
    public static void sendStatus(){
        try {
            
            mqResponseJobs.sendMessage(getStatusJson());
        } catch (MqttException me) {
            mqttExceptionHandler(me);
        }
        
    }
    
    private void reciveJobs(){
        
        mqRecrestJobs.getMqttClient().setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                System.out.println("DruckerQueue reciveJobs() connectionLost:"+throwable);
                    try {
                        mqRecrestJobs.getMqttClient().connect();
                    } catch (MqttException ex) {
                        mqttExceptionHandler(ex);
                    }
                }

                @Override
                public void messageArrived(String string, MqttMessage mm) throws Exception {
                    String text = new String(mm.getPayload());
                    JsonReader reader = Json.createReader(new StringReader(text));
                JsonObject jsonObj = reader.readObject();
                System.out.println("DruckerQueue Input:"+text);
                if (jsonObj.getJsonString("druckerID").getString().contains("hallo")){sendStatus();} 
                else if (jsonObj.getJsonString("druckerID").getString().contains(clientId) && !jsonObj.getJsonString("from").getString().contains(clientId)){
                        //Naricht ist für micht
                        if (jsonObj.getJsonString("auftrag").getString().contains("getStatus")){sendStatus();}
                        else if (jsonObj.getJsonString("auftrag").getString().contains("printFile")){//Auftrag entgegennehmen!!! 
                                
                                //druckAuftrag a = new druckAuftrag(jsonObj.getJsonString("datei").getString(),"blue",jsonObj.getJsonString("from").getString(),p);
                                druckAuftrag a = new druckAuftrag(jsonObj.getJsonString("datei").getString(),"blue",jsonObj.getJsonString("from").getString());
                                Panel.druckAuftraege.add(a);
                                sendStatus();
                            }
                    }
                
                }
   
                @Override
                public void deliveryComplete(IMqttDeliveryToken imdt) {
                    System.out.println("DruckerQueue reciveJobs() deliveryComplete:"+imdt);
                }
        });
        
        try {
           // messageQueueCom.getMqttClient().connect();
            mqRecrestJobs.getMqttClient().subscribe(topicRecrestPrinter);
        } catch (MqttException ex) {
            Logger.getLogger(panelManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    protected static String getStatusJson() {
        String x;
        if(Panel.work){x="on";}else{x="off";}

         JsonObject versionObject = Json.createObjectBuilder()
                    .add("druckerID", clientId)
                    .add("from", clientId)
                    //.add("debug",  f )
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
    
    private static JsonArrayBuilder getPrinterQeeue(){
        JsonArrayBuilder jArray = Json.createArrayBuilder();
        for (int i=0; i < Panel.druckAuftraege.size(); i++){
            //jArray.add(Panel.druckAuftraege.get(i));
            JsonObject jo = Json.createObjectBuilder()
                    .add("auftrag", Panel.druckAuftraege.get(i).getTimestamp().toString())
                    .add("dateiPfad", Panel.druckAuftraege.get(i).getPfad())
                    .add("auftraggeber", Panel.druckAuftraege.get(i).getUserId())
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
    private static JsonArrayBuilder getErrorsByAuftrag(druckAuftrag a){
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
    private static JsonArrayBuilder getPrinterErrors(){
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
    
    static panelManager p = null;
    public static void main(String args[]) {
        p = new panelManager();
    }
    private static void mqttExceptionHandler(MqttException me){
        System.out.println("reason "+me.getReasonCode());
        System.out.println("msg "+me.getMessage());
        System.out.println("loc "+me.getLocalizedMessage());
        System.out.println("cause "+me.getCause());
        System.out.println("excep "+me);
        me.printStackTrace();
    }

}
