/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package druckerkopf;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import netzwerkModul.Client;
import tcpmodul.TCPClient;
import udpmodul.UDPClient;

/**
 *
 * @author joe
 */
public class TonerHelper {
    private String farbe;
    private InetAddress myIP;
    private Integer myPort;
    private Integer myFuellstand;
    
    public TonerHelper(String ip, Integer p, String f){
        this.farbe = f;
        this.myFuellstand = -1;
        this.myPort = p;
        try {
            this.myIP = InetAddress.getByName(ip);
        } catch (UnknownHostException ex) {
            System.out.println("Toner-Ip-Adresse von "+f+" konnten nicht übernommen werden.");
            Logger.getLogger(TonerHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
   public String getFarbe(){
       return this.farbe;
   }
   public InetAddress getIP(){
       return this.myIP;
   }
   public Integer getPort(){
       return this.myPort;
   }
   
   public Integer getFullstand(){
       Client c = configClient();
       String a = c.sendString("Are you empty?");
       //c.closeSocket();
       return Integer.parseInt(a.trim());
   }
   
   public boolean areYouHot(){
       boolean b = false;
       Client c = configClient();
       String a = c.sendString("Are you hot?").trim();
       //c.closeSocket();
       if (a.contentEquals("yes")){
           b = true;
       }
       return b;
   }
   
   public boolean iNeedStoff(Integer gram) throws tonerLeer{
       boolean b = false;
       Client c = configClient();
       String a = c.sendString("need Stoff!;"+gram);
       //c.closeSocket();
       if (Integer.parseInt(a.trim()) == gram ){
           b = true;
       } else if (Integer.parseInt(a.trim()) < 0){
           throw new tonerLeer();
       }
       return b;
   }
   
   public boolean makeYouHot(){
       boolean b = false;
       Client c = configClient();
       String a = c.sendString("Make you hot!");
       //c.closeSocket();
       if (Integer.parseInt(a.trim()) == 1 ){
           b = true;
       }
       return b;
   }
   
   public boolean shoutDown(){
       boolean b = false;
       Client c = configClient();
       String a = c.sendString("go off");
       //c.closeSocket();
       if (Integer.parseInt(a.trim()) == 1 ){
           b = true;
       }
       return b;
   }
   private Client netzwerkModul = null;
   private Client configClient(){
        
        if (DruckerKopf.isTCPConnection){
            //ToDo: nur einmal Socket erstellen
            if (netzwerkModul == null){
                System.out.println("Neuer TCP-Client");
                netzwerkModul = new TCPClient(this.myIP,this.myPort);
            }
        } else {
            netzwerkModul = new UDPClient();
        }
        netzwerkModul.setIPAdress(this.myIP);
        netzwerkModul.setPort(this.myPort);
        return netzwerkModul;
    }

    public class tonerLeer extends Exception {

        public tonerLeer() {
            super("Tonerbehälter "+farbe+" reicht nicht für dieses Druckauftrag!");
        }
    }
}
