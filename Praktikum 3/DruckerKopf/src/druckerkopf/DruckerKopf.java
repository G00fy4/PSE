/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package druckerkopf;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import netzwerkModul.Client;
import netzwerkModul.Server;
import tcpmodul.TCPClient;
import tcpmodul.TCPServer;
import udpmodul.*;
/**
 *
 * @author tomatenjoe
 */
public class DruckerKopf extends Thread{
    public static Integer serverPort = 9871;
    public static boolean isTCPConnection = false;
    public static Integer timeMultiplikator = 0;
    
    public static HashMap <String,TonerHelper> toners = new HashMap<String,TonerHelper>();
    /*TonerHelper t = new TonerHelper("127.0.0.1",9872, "blue");
    private void configTonerMap(){
        toners.put("blue", t);
    }*/
    
    Server panelServerConnection;
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    /*public static void main(String[] args) throws Exception {
        // verfügbare Toner dem Druckerkopf bekannt machen
        

        
        //DruckerKopf initialisieren
        Thread d = new DruckerKopf();
        d.start();
    }*/
    
    public DruckerKopf(){
    }
    @Override
    public void run(){
        /*
        Server netzwerkModul = new TCPServer(9871);
        netzwerkModul.test();
        */
        System.out.println("Hallo ich bin der Druckkopf");
        //configTonerMap();
        getfuellstandFromAllToners();
        
        if (isTCPConnection){
            panelServerConnection = new TCPServer(serverPort);
        } else {
            panelServerConnection = new UDPServer(serverPort);
        }
        File printFile = null;
        //netzwerkModul.test();
        //netzwerkModul.test();
        
        boolean schleife = true;
        while(schleife){
            panelServerConnection.enpfangeDaten();
            String anfrage= new String(panelServerConnection.getReceiveData());
            
            if (anfrage.contains("receiveThisFile")){
                printFile = empfangeDruckauftrag();
            } else if (anfrage.contains("printThis")){
                
                try {
                    
                    printData(printFile);
                } catch (IOException ex) {
                    panelServerConnection.sendString("Error - No data to print.");
                    System.out.println("Die zu druckende Datei existiert nicht.");
                    Logger.getLogger(DruckerKopf.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (anfrage.contains("shutDown")){
                panelServerConnection.sendString("Fahre Systeme runter.");
                System.out.println("Fahre Systeme runter.");
                for (String key : toners.keySet()) {
                    toners.get(key).shoutDown();
                }
                panelServerConnection.socketClose();
                break;
            }
            
             
            
        }
        
        
    }
    private File empfangeDruckauftrag(){
        FileOutputStream fos= null;
        File file = null;
        try {
            System.out.println("Empfange Druckauftrag...");
            panelServerConnection.sendString("ok");
            panelServerConnection.enpfangeDaten();
            String dateiName= new String(panelServerConnection.getReceiveData()).trim();
            file = new File(dateiName);
            System.out.println("Druckauftrag: " + dateiName + " wird uebermittelt.");
            //Dateigröße empfangen
            panelServerConnection.enpfangeDaten();
            String d = new String(panelServerConnection.getReceiveData()).trim();
            Integer dateiGroese= Integer.parseInt(d);
            panelServerConnection.sendString(String.valueOf(dateiGroese));
            System.out.println("Erwartete Dateigröße: " + String.valueOf(dateiGroese) + " ");
            
            

            fos = new FileOutputStream(file);
            try {
                while (fos.getChannel().size() < dateiGroese){
                    System.out.println("Bereits übertragen: " +fos.getChannel().size() +" / "+ dateiGroese );
                    panelServerConnection.enpfangeDaten();
                    try {
                        //Wenn gesendete byte[] strom größer ist als noch benötigte daten
                        if (panelServerConnection.getReceiveData().length > (dateiGroese-fos.getChannel().size())){
                            Long b = dateiGroese-fos.getChannel().size();
                            byte[] lastByte = new byte[b.intValue()];
                            byte[] tempLastByte = panelServerConnection.getReceiveData();
                            //System.out.println("lastByte: "+b.intValue()+" echtelänge:"+lastByte.length);
                            for (Integer i = 0; i < b.intValue(); i++){
                                lastByte[i]=tempLastByte[i];
                            }
                            //System.out.println(" echtelänge 2:"+lastByte.length);
                            fos.write(lastByte);
                        } else {
                            fos.write(panelServerConnection.getReceiveData());
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(DruckerKopf.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //Antwort senden? - nein
                    panelServerConnection.sendString(String.valueOf(fos.getChannel().size()));
                    
                }
                System.out.println("Fertig übertragen: " +fos.getChannel().size() +" / "+ dateiGroese );
                
            } catch (IOException ex) {
                Logger.getLogger(DruckerKopf.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
                
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(DruckerKopf.class.getName()).log(Level.SEVERE, null, ex);
            }
          
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DruckerKopf.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return file;
    }
    
    private void printData(File data) throws IOException{
        Long dateiGroesse = data.length();
        panelServerConnection.sendString("ok. On witch port i have send my messages?");
        panelServerConnection.enpfangeDaten();
        String d = new String(panelServerConnection.getReceiveData()).trim();
        Integer panelServerPort = Integer.parseInt(d);
        InetAddress panelIPAddress = panelServerConnection.getClientIPAddress();
        panelServerConnection.sendString("bye");
        /*if (isTCPConnection){
            panelServerConnection.clientSocketClose();
        }*/
        
        try {
            //Brauchen wir für schnelle Rechner
                    Thread.sleep(3000 * timeMultiplikator + 1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DruckerKopf.class.getName()).log(Level.SEVERE, null, ex);
                }
        System.out.println("Ich sende Pakete nun an "+ panelIPAddress.toString()+":"+ d);
        panelServerConnection.socketClose();
        
        
        Client sendStatus = null;
        if (isTCPConnection){
            sendStatus = new TCPClient(panelIPAddress,panelServerPort);
        } else {
            sendStatus = new UDPClient();
        }
        
        sendStatus.setIPAdress(panelIPAddress);
        sendStatus.setPort(panelServerPort);
        
        for (String key : toners.keySet()) {
            System.out.println("Toner "+key+" Füllstand: "+ toners.get(key).getFullstand()+" Gram");
            sendStatus.sendString("Toner "+key+" Füllstand: "+ toners.get(key).getFullstand()+" Gram");
        }
        
        
        sendStatus.sendString("Heize Farbtoner und Düsen auf.");
        
        //Auswahl welche Farbe gedruckt werden kann
        toners.get("blue").areYouHot();
        toners.get("blue").makeYouHot();
        
        Boolean x = toners.get("blue").areYouHot();
        while(!x){
            try {
                
                Thread.sleep(4000*timeMultiplikator);
            } catch (InterruptedException ex) {
                Logger.getLogger(DruckerKopf.class.getName()).log(Level.SEVERE, null, ex);
            }
            String m = "Toner 'blue' wird noch aufgewährmt. ...";
             System.out.println(m);
             sendStatus.sendString(m);
             x = toners.get("blue").areYouHot();
        }
        System.out.println("Beginne mit druckvorgang.");
        sendStatus.sendString("Beginne mit druckvorgang.");
        boolean e = false;
        try {
            e = toners.get("blue").iNeedStoff( dateiGroesse.intValue() );
        
        if (e){
            Integer k=1; long y = 100;
            while(dateiGroesse.intValue() >= k){
                y = 100;
                y = (dateiGroesse / y); 
                y = k / y; 
                //System.out.println("k:" +k+" dateiGroesse:" + dateiGroesse);
                Integer p = (int) y;
                //Prozentwert = Grundwert * (Prozentsatz/100)
                System.out.println("|| "+p+"% ");
                sendStatus.sendString("|| "+p+"% ");
                try {
                    Thread.sleep(1000*timeMultiplikator);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DruckerKopf.class.getName()).log(Level.SEVERE, null, ex);
                }
                //100 KB pro sekunde drucken
                k = k + 100;
            }
        }
        for (String key : toners.keySet()) {
            System.out.println("Toner "+key+" Füllstand: "+ toners.get(key).getFullstand()+" Gram");
            sendStatus.sendString("Toner "+key+" Füllstand: "+ toners.get(key).getFullstand()+" Gram");
        
        }
        
        sendStatus.sendString("Finish.");
        
        } catch (TonerHelper.tonerLeer ex) {
            sendStatus.sendString("Error - "+ex);
            System.out.println("Error - "+ex);
           // Logger.getLogger(DruckerKopf.class.getName()).log(Level.SEVERE, null, ex);
           
           sendStatus.sendString("Finish with Error.");
        } finally {
            
          if (data.exists()) {
            data.delete();
            System.out.println("Datei gelöscht!");
          }
          
          sendStatus.closeSocket();

          if (isTCPConnection){
              panelServerConnection = new TCPServer(serverPort);
          } else {
              panelServerConnection = new UDPServer(serverPort);
          }
        }
        
        
        
        
        
        
    }
    
    private void getfuellstandFromAllToners(){
        for (String key : toners.keySet()) {
            System.out.println("Toner "+key+" Füllstand: "+ toners.get(key).getFullstand()+" Gram");
         //for( HashMap.Entry<String, TonerHelper> toner : toners.entrySet()){
           // System.out.println("Toner "+toner.getKey()+" Füllstand: "+toner.getValue().getFullstand()+" Gram");
        }
    }
    
}
