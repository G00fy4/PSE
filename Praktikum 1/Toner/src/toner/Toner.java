/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package toner;

import java.util.logging.Level;
import java.util.logging.Logger;
import netzwerkModul.Server;
import tcpmodul.TCPServer;
import udpmodul.UDPServer;

/**
 *
 * @author joe
 */
public class Toner {
    Integer serverPort = 9872;
    Integer myFuellstand = 15000;
    boolean isTCPConnection = true;
    Integer timeMultiplikator = 0;
    
    Server druckerkopfServerConnection;
    boolean iAmHot = false;
    Integer secondsToHot;
    boolean makeHotThreadRun = false;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Toner t = new Toner();
    }
    public Toner(){
        System.out.println("Hallo ich bin ein  Toner");
        if (isTCPConnection){
            druckerkopfServerConnection = new TCPServer(serverPort);
        } else {
            druckerkopfServerConnection = new UDPServer(serverPort);
        }
        //netzwerkModul.test();
        //netzwerkModul.test();
        
        boolean schleife = true;
        while(schleife){
            druckerkopfServerConnection.enpfangeDaten();
            String anfrage= new String(druckerkopfServerConnection.getReceiveData()).trim();
            
            if (anfrage.contains("Are you empty?")){
                System.out.println("Are you empty? - "+myFuellstand.toString());
                druckerkopfServerConnection.sendString(myFuellstand.toString());
                
            } else if (anfrage.contains("Are you hot?")){
                if (iAmHot){
                    System.out.println("Are you hot? - yes");
                    druckerkopfServerConnection.sendString("yes");
                } else {
                    System.out.println("Are you hot? - no");
                    druckerkopfServerConnection.sendString("no");
                }
                
            } else if (anfrage.contains("need Stoff!;")){
                String[] a = anfrage.trim().split(";");
                if(myFuellstand-Integer.parseInt(a[1].trim()) < 0){
                    System.out.println(anfrage+" Füllstand reicht nicht aus -1");
                    druckerkopfServerConnection.sendString("-1");
                } else if (iAmHot){
                    myFuellstand = myFuellstand-Integer.parseInt(a[1].trim());
                    System.out.println(anfrage+" ok, neuer Füllstand: "+myFuellstand);
                    iAmHot = false;
                    druckerkopfServerConnection.sendString(a[1]);
                } else {
                    System.out.println(anfrage+" i'm not hot! 0");
                    druckerkopfServerConnection.sendString("0");
                }
              
            } else if (anfrage.contains("Make you hot!")){
                //ToDo tread machen mit counter
                Thread makeMeHotThread = new makeMeHotThread();
                if (!makeHotThreadRun){makeMeHotThread.start();}
                System.out.println("Make you hot! 1 \n counter started");
                druckerkopfServerConnection.sendString("1");
                
            } else if (anfrage.contains("go off")){
          
                druckerkopfServerConnection.sendString("1");
                druckerkopfServerConnection.socketClose();
                System.out.println("go off 1 \n good night");
                break;
            }else {
                System.out.println("ERROR: Befehl unbekannt: "+anfrage);
                druckerkopfServerConnection.sendString("Befehl unbekannt: "+anfrage);
            } 
            
             
            
        }
    }
/*
    Integer iAmClean = 100;
    boolean makeCleanThreadRun  = false;
    class makeCleanThread extends Thread {
        public void run() {
             makeCleanThreadRun = true;
             iAmClean = 0;
            try {
                 Integer prozentProSekundeGereinigt = 10;
                 while (iAmClean < 100){
                    Thread.sleep(1000*timeMultiplikator);
                    iAmClean = iAmClean + prozentProSekundeGereinigt;
                    System.out.println("Schon "+iAmClean+"% gereinigt.");
                 }

            } catch (InterruptedException ex) {
                Logger.getLogger(Toner.class.getName()).log(Level.SEVERE, null, ex);
            }
        iAmClean = 100;
        makeCleanThreadRun  = false;
        }
    } */

    
    class makeMeHotThread extends Thread {
 
    public void run() {
        makeHotThreadRun = true;
        try {
             Integer secondsToHot = 20;
             while (secondsToHot > 0){
                Thread.sleep(1000*timeMultiplikator);
                secondsToHot--;
                System.out.println("Noch "+secondsToHot+" Sekunden bis heiß.");
             }
                
        } catch (InterruptedException ex) {
            Logger.getLogger(Toner.class.getName()).log(Level.SEVERE, null, ex);
        }
        iAmHot = true;
        makeHotThreadRun = false;
    }
  
  }
    
}
