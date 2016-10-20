/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobiledevice;


import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.ListDataListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author joe
 */
public class MobileDevice {
    String broker       = "tcp://127.0.0.1:1883";
    
    String printFile = "/home/joe/Dokumente/h-da/Verteilte Systeme/Praktikum 2/BeispielSocketsInJava.tar.gz";
    //String printFile = "/home/Tomatenjoe/Dokumente/h-da/Verteilte Systeme/Praktikum 2/BeispielSocketsInJava.tar.gz";
    //String printFile = "/home/Debian/Dokumente/h-da/Verteilte Systeme/Praktikum 2/BeispielSocketsInJava.tar.gz";
    
    
    
    String clientId     = "MobileDevice";
    int qos             = 2;
    String topicRecivePrinter = "recivePrinter";
    String topicRequestPrinter = "requestPrinter";
    
    
    
    
    MQoutput mqRequestPrinter;
    MqttClient mqRecivePrinter;
    String aktuellePrinterID=null;
    JLabel ausgabeDruckerStartus = null;
    JLabel ausgabeAuftraegeStatus = null;
    JLabel ausgabeKosten = null;
    public MobileDevice(){
    }
    String defoultItem = "Übersicht";
    
    public void iniPrinterStatus(){
        printers.add(defoultItem);
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
                 System.out.println("Input: "+message);
                 JsonReader reader = Json.createReader(new StringReader( message ));
                JsonObject jsonObj = reader.readObject();
                
                if(jsonObj.getJsonString("druckerID").getString().contains(clientId) && jsonObj.getJsonString("from").getString().contains("MASTER")){
                    //Rechnung vom Dashboad
                    System.out.println("Rechnung erkannt");
                    showKosten(jsonObj.getJsonNumber("bill").intValue());
                } else {
                    if(jsonObj.getJsonString("druckerID").getString().contains(jsonObj.getJsonString("from").getString())){
                        //Nachricht von einem Drucker
                        System.out.println("Drucker '"+jsonObj.getJsonString("druckerID").getString()+"' Status: "+jsonObj.getJsonString("status").getString()+" hat "+jsonObj.getInt("countedJobsInQueue")+" Aufträge. Davon "+jsonObj.getInt("countedJobsWithErrors")+" Aufträge mit Fehlern");  
                        pinterStatusMap.put(jsonObj.getJsonString("druckerID").getString(), jsonObj);

                        //Neue Printer in PrinterMenu hinzufügen
                        if (!pinterMap.containsValue(jsonObj.getJsonString("druckerID").getString())){
                            printers.add("Printer "+pinterMap.size());
                            pinterMap.put("Printer "+pinterMap.size(), jsonObj.getJsonString("druckerID").getString());

                        }
                        if(aktuellePrinterID!=null && (jsonObj.getJsonString("druckerID").getString().contains(aktuellePrinterID) || aktuellePrinterID.contains(defoultItem))){
                            aktualisiereAusgabe();
                        }
                    }
                }
                
               }

               @Override
               public void deliveryComplete(IMqttDeliveryToken t) { }
               });

        mqRecivePrinter.connect();

        mqRecivePrinter.subscribe(topicRecivePrinter);
        
        sendHalloPrinters();
            
        } catch (MqttException me) {
            mqttExceptionHandler(me);
        }
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
    
    Map<String,JsonObject> pinterStatusMap = new HashMap<String,JsonObject>();
    //final List<String> printers = new ArrayList<String>();
    final Vector<String> printers = new Vector<String>();
    Map<String,String> pinterMap = new HashMap<String,String>();
    
    protected DefaultComboBoxModel getPrinters(){
        
        DefaultComboBoxModel numberCombo = new DefaultComboBoxModel(printers);
        return numberCombo;
        
    }
    protected void aktualisiereAusgabe(){
        if (ausgabeDruckerStartus != null){
            String t = selectAuswahl;
            if (aktuellePrinterID == null) {t = defoultItem;}
            afterSelectPrinter(t, ausgabeDruckerStartus, ausgabeAuftraegeStatus, ausgabeKosten);
        }
    }
    String selectAuswahl = null;
    protected void afterSelectPrinter(String t, JLabel jLabel2, JLabel jLabel3, JLabel jLabel4){
        System.out.println("Auswahl:"+t+":");
        ausgabeDruckerStartus = jLabel2;
        ausgabeAuftraegeStatus = jLabel3;
        ausgabeKosten = jLabel4;
        selectAuswahl = t;
        if(!t.contains(defoultItem)){
        aktuellePrinterID = pinterMap.get(t);
        System.out.println("test: "+ aktuellePrinterID);
        //Printer Status anzeigen usw
        if(!pinterStatusMap.containsKey(aktuellePrinterID)){
            jLabel2.setText("Drucker Informationen nicht vorhanden. Bitte warten und erneut auswählen.");
            getPrinterstatus(aktuellePrinterID);
        } else { 
            jLabel2.setText("Drucker: "+t+"' Status: "+pinterStatusMap.get(aktuellePrinterID).getJsonString("status").getString()+" hat "+pinterStatusMap.get(aktuellePrinterID).getInt("countedJobsInQueue")+" Aufträge. Davon "+pinterStatusMap.get(aktuellePrinterID).getInt("countedJobsWithErrors")+" Aufträge mit Fehlern");  
            jLabel3.setText(showDruckaufträge());
            //System.out.println("Drucker Schalter= "+pinterStatusMap.get(aktuellePrinterID).getJsonString("status").getString());
        }
        
        
        } else {
            sendHalloPrinters();
            //to dooo
            jLabel2.setText("Übersicht aller bekannten Drucker:");
            String text ="<html>";
            for (Integer i = 1; i < printers.size(); i++){
                text = text +"<br>Drucker: "+printers.get(i)+"' Status: "
                        +pinterStatusMap.get(pinterMap.get(printers.get(i))).getJsonString("status").getString()+" hat "
                        +pinterStatusMap.get(pinterMap.get(printers.get(i))).getInt("countedJobsInQueue")+" Aufträge. Davon "
                        +pinterStatusMap.get(pinterMap.get(printers.get(i))).getInt("countedJobsWithErrors")+" Aufträge mit Fehlern"; 
            }
            jLabel3.setText(text+"</html>");
        }
    }
    protected void getPrinterstatus(String PrinterID){
        JsonObject JsonObj = Json.createObjectBuilder()
                    .add("druckerID", PrinterID)
                    .add("from", clientId)
                    .add("auftrag",  "getStatus")
                    .build();
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = Json.createWriter(stringWriter);
        writer.writeObject(JsonObj);
        writer.close();
        String result =  stringWriter.getBuffer().toString();
        MqttMessage message = new MqttMessage(result.getBytes());
        message.setQos(qos);
        try {
            mqRecivePrinter.publish(topicRequestPrinter, message);
        } catch (MqttException me) {
            mqttExceptionHandler(me);
        }
       
        //iniPrinterStatus();
    }
    
    protected void sendPrinterJob(){
        if (aktuellePrinterID!=null){
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
    }
    
    private void mqttExceptionHandler(MqttException me){
        System.out.println("reason "+me.getReasonCode());
        System.out.println("msg "+me.getMessage());
        System.out.println("loc "+me.getLocalizedMessage());
        System.out.println("cause "+me.getCause());
        System.out.println("excep "+me);
        me.printStackTrace();
    }
    
    private void showKosten(Integer wert){
        ausgabeKosten.setText("Kosten: "+wert+"€");
    }
    
    private String showDruckaufträge(){
        String auftraege ="<html><body>";
        if (aktuellePrinterID != null){
            JsonObject jsonObj = pinterStatusMap.get(aktuellePrinterID);
            //System.out.println("Drucker '"+printer.getName()+"' Status: "+jsonObj.getJsonString("status").getString()+" hat "+jsonObj.getInt("countedJobsInQueue")+" Aufträge. Davon "+jsonObj.getInt("countedJobsWithErrors")+" Aufträge mit Fehlern"); 
            auftraege="In der Druckerwarteschlange:<br>";
            for (Integer i = 0; i < jsonObj.getInt("countedJobsInQueue"); i++){
                JsonObject auftraeg = jsonObj.getJsonArray("jobsInQeueu").getJsonObject(i);
                //System.out.println("  "+auftraeg.getString("auftrag")+ " Status:'"+auftraeg.getString("status")+"' Fehler: "+auftraeg.getString("hasError")+" Datei: "+auftraeg.getString("dateiPfad"));
                auftraege=auftraege+"  "+auftraeg.getString("auftrag")+ " Auftraggeber: "+ auftraeg.getString("auftraggeber")+ " Status:'"+auftraeg.getString("status")+"' Datei: "+auftraeg.getString("dateiPfad")+"<br>";
            
            }
            System.out.println("In der Error-Liste:<br>");
            for (Integer i = 0; i < jsonObj.getInt("countedJobsWithErrors"); i++){
                JsonObject auftraeg =  jsonObj.getJsonArray("errors").getJsonObject(i);
                JsonArray error = auftraeg.getJsonArray("errors"); 
                System.out.println("  "+auftraeg.getString("auftrag")+ ":");
                for (Integer l = 0; l < error.size(); l++){
                    auftraege=auftraege+"     "+error.getJsonObject(l).getJsonNumber("errorCode")+ " : " +error.getJsonObject(l).getJsonString("errorText").toString()+" - "+error.getJsonObject(l).getJsonString("errorGrund").toString()+"<br>";                
                }
                //System.out.println("  "+auftraeg.getString("auftrag")+ " Status:'"+auftraeg.getString("status")+"' Fehler: "+auftraeg.getString("hasError")+" Datei: "+auftraeg.getString("dateiPfad"));
                //System.out.println("  "+auftraeg.getString("auftrag")+ " Status:'"+auftraeg.getString("status")+"' Datei: "+auftraeg.getString("errorGrund"));
            }
        }
        auftraege=auftraege+"</body></html>";
        return auftraege;
    }
}
