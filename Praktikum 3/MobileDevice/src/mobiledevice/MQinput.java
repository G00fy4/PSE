/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobiledevice;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.StringReader;
import java.util.logging.Level; 
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author joe
 */
public class MQinput extends Thread{
    private String MQAdresse, clientID, MQTopic;
    public MQinput(String id, String server ,String t){
        MQAdresse = server;
        clientID = id;
        MQTopic = t;
    }
    
    public void run(){
    MqttClient client;
        try {
            client = new MqttClient(MQAdresse, clientID);
        
            client.setCallback(new MqttCallback(){
            @Override
               public void connectionLost(Throwable throwable) { 
                   System.out.println("ERROR:" +clientID+" connectionLost from "+MQAdresse);
               }

               @Override
               public void messageArrived(String t, MqttMessage m) throws Exception {
                   String message = new String(m.getPayload());
                 System.out.println("Input: "+message);
                 JsonReader reader = Json.createReader(new StringReader( message ));
                JsonObject statusObject = reader.readObject();
               
               }

               @Override
               public void deliveryComplete(IMqttDeliveryToken t) { }
               });

        client.connect();

        client.subscribe(MQTopic);
    
        } catch (MqttException ex) {
            Logger.getLogger(MQinput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
