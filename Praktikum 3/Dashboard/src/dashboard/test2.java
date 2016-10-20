/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard;

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
public class test2 {
     public static void main(String[] args) {
     MqttClient client;
        try {
            client = new MqttClient("tcp://127.0.0.1:1883", "JavaSample2");
        
            client.setCallback(new MqttCallback(){
            @Override
               public void connectionLost(Throwable throwable) { }

               @Override
               public void messageArrived(String t, MqttMessage m) throws Exception {
                 System.out.println("hallo"+new String(m.getPayload()));
               }

               @Override
               public void deliveryComplete(IMqttDeliveryToken t) { }
               });

        client.connect();

        client.subscribe("MQTT");
    
        } catch (MqttException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        }
}}
