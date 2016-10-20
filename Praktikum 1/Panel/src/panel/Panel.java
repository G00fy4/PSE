/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package panel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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
 * ...To compile the file, open your terminal and type
 + javac filename.java
 + ...To run the generated class file, use
 + java filename
 */
public class Panel extends Thread {
    String druckerKopfIPAdresse = "127.0.0.1";
    Integer druckerKopfPort = 9871, meinServerPort = 9870;
    public boolean isTCPConnection = true;
    
    
    List<String> druckAuftraege = new ArrayList<String>();
    
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        //String e =  "/home/tomatenjoe/Dokumente/h-da/Verteilte Systeme/Praktikum 1/BeispielSocketsInJava.tar.gz";
        String e =  "/home/tomatenjoe/Dokumente/h-da/Verteilte Systeme/Praktikum 1/BeispielSocketsInJava.tar.gz";
        //String e = "/home/tomatenjoe/Dokumente/h-da/Verteilte Systeme/Praktikum 1/BeispielSocketsInJava.tar.gz";
        
        Panel p = new Panel();
   
        p.addDuckauftrag(e);
        p.addDuckauftrag(e);
        p.addDuckauftrag(e);
        p.addDuckauftrag(e);
        p.addDuckauftrag(e);
        
         p.run();
        
    }
    public void addDuckauftrag(String x){
        druckAuftraege.add(x);
    }
    
    
    public Panel(){
    
    }
    public void  run(){
        /*
        Client netzwerkModult = new TCPClient(9871);
        netzwerkModult.test();
        */
        
        
        System.out.println("Wilkommen... \n... Ihr Druckersystem fährt hoch.");
        System.out.println("    "
                + "...");
        long time = System.currentTimeMillis();
        
        if (druckAuftraege.isEmpty()){
            System.out.println("Habe keine Druckaufträge!");
        }
        
        while (druckAuftraege.size() > 0){
            System.out.println("Drucke nächste Datei. Noch "+druckAuftraege.size()+" Druckaufträge in der Warteschlange");
            
            
            dateiSenden(druckAuftraege.get(0));


            try {
                dateiDrucken();
            } catch (noDataToPrintFound ex) {
                Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        }
        time = (time - System.currentTimeMillis())*-1;
        String benoetigteZeit = "";
        if (isTCPConnection){
                benoetigteZeit = "TCP benötigte "+ time + "ms";
            } else {
                benoetigteZeit = "UDP benötigte "+ time + "ms";
            }
        System.out.println(benoetigteZeit);
        schreibeInLogDatei(benoetigteZeit);
        
        Client netzwerkModul = configClient();
        String a = sendeMitFehlerbehandlung(netzwerkModul,"shutDown");

        System.out.println(a+"\n bye bye");
        netzwerkModul.closeSocket();
        confNetzwerkModul = null;
        
            
    }
    private void dateiDrucken() throws noDataToPrintFound{
        
        Client netzwerkModul = configClient();
        String a = sendeMitFehlerbehandlung(netzwerkModul,"printThis");
        if (a.contains("Error")){
            System.out.println("Druckerkopf meldet: "+a);
            netzwerkModul.closeSocket();
            throw new noDataToPrintFound();
        } else {
            a = sendeMitFehlerbehandlung(netzwerkModul,meinServerPort.toString());
            netzwerkModul.closeSocket();
            confNetzwerkModul = null;
            System.out.println("Beende Verbindung");
            Server statusServer = null;
            if (isTCPConnection){
                statusServer = new TCPServer(meinServerPort);
            } else {
                statusServer = new UDPServer(meinServerPort);
            }
            while (!a.contains("Error") && !a.contains("Finish")){
                statusServer.enpfangeDaten();
                a = new String(statusServer.getReceiveData()).trim();
                System.out.println("Status: "+a);
                statusServer.sendString("How long do you need?");
            }
            if (a.contains("Error")){
                //Fehlerbehandlung
                if (a.contains("tonerLeer")){
                    Boolean s = true;
                    while(s){
                        InputStreamReader isr = new InputStreamReader(System.in);
                        BufferedReader br = new BufferedReader(isr);
                        System.out.println("Bitte Toner auffüllen und ok eingeben: ");
                        String eingabe = "";
                        try {
                            eingabe = br.readLine();
                        } catch (IOException ex) {
                            Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println("Du hast " + eingabe + " eingegeben.");
                        if (eingabe.equalsIgnoreCase("ok")){
                            s = false;
                        }
                    }
                    //Druckauftrag muss erneut gestartet werden
                }
            } else {
                //Druckauftrag erfolgreich
                druckAuftraege.remove(0);
            }
                    
            statusServer.socketClose();
            confNetzwerkModul = null;
        }
        
    }
    
    private void dateiSenden(String dateiPfad){
        Client netzwerkModul = configClient();
                
        FileInputStream fis = null;
        Integer error = 0;
        while (error < 3){
            try {
                //Druckerauftrag senden
                File fileProp = new File(dateiPfad);
                System.out.println("Dateigroeße: "+(int)fileProp.length());
                fis = new FileInputStream(fileProp);
                try {
                    fis.read();
                } catch (IOException ex) {
                    Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
                }
                byte[] buffer = new byte[1024];
                String m = "receiveThisFile";
                String antwort;
                for (Integer i = 0; i<10;i++){
                    antwort = sendeMitFehlerbehandlung(netzwerkModul,m);
                    //System.out.println("antwort: "+antwort);

                    if (antwort.contains("ok")){
                        System.out.println("Sende druckauftrag...");
                        i = 11;
                    }
                    if ( i == 9 ){
                        throw new failedToSendPrintJob();
                    }
                }
                
                String a = sendeMitFehlerbehandlung(netzwerkModul,fileProp.getName().trim());
                if (!a.contains("ok")){
                    System.out.println("Dateiname konnte nicht übermittelt werden");
                }
                //System.out.println("-"+a);
                String s = sendeMitFehlerbehandlung(netzwerkModul,String.valueOf(fileProp.length())).trim();
                //System.out.println("--");
                if (Integer.parseInt(s) != fileProp.length()){
                    System.out.println("Dateigröße konnte nicht übermittelt werden");
                    error++;
                } else{System.out.println("Dateigröße gesendet");}

                if (error == 0){
                    //Datei übermitteln
                    System.out.println("Übertrage Datei...");
                    try {
                        while ( fis.read(buffer) != -1 ) {
                            netzwerkModul.setSendData(buffer);
                            netzwerkModul.send();
                            buffer = new byte[1024];
                            String h = new String(netzwerkModul.empfang());
                            System.out.println(""+ h.trim() +" von " + fileProp.length() + " byte übertragen.") ;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        fis.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Datei erfolgreich übermittelt.");
                    break;
                } 



            } catch (FileNotFoundException ex) {
                Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (failedToSendPrintJob ex) {
                Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        if (error >= 3){
            System.out.println("Druckauftrag konnte inerhalb drei Versuchen nicht übermittelt werden! \n bitte versuchen sie es später erneut.");
        } else {
            if (!isTCPConnection){
                netzwerkModul.closeSocket();
                confNetzwerkModul = null;
            }
        }
        
    }
    private String sendeMitFehlerbehandlung(Client netzwerkModul, String m){
        while(true){
                
                netzwerkModul.send(m.getBytes());
                
                m = new String(netzwerkModul.empfang());
                
                if (1==1) break;
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("DruckerKopf hat nicht reagiert.");
            } 
        return m;
    }
    private Client confNetzwerkModul = null;
    private Client configClient(){
        
        if (isTCPConnection){
            if (confNetzwerkModul == null){
                try {
                    confNetzwerkModul = new TCPClient(InetAddress.getByName(druckerKopfIPAdresse),druckerKopfPort);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            confNetzwerkModul = new UDPClient();
        }
        try {
            InetAddress dIPAdresse = InetAddress.getByName(druckerKopfIPAdresse);
            confNetzwerkModul.setIPAdress(dIPAdresse);
            confNetzwerkModul.setPort(druckerKopfPort);
        } catch (UnknownHostException ex) {
            System.out.println("Druckkopf-Netzwerkeinstellunmgen konnten nicht übernommen werden.");
            Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return confNetzwerkModul;
    }
    
    public void schreibeInLogDatei(String text){
        PrintWriter pWriter = null;
        try {
            pWriter = new PrintWriter(new BufferedWriter(new FileWriter("log.txt",true)));
            pWriter.println(text);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (pWriter != null){
                pWriter.flush();
                pWriter.close();
            }
        } 
     }

    private static class failedToSendPrintJob extends Exception {

        public failedToSendPrintJob() {
            super("Druckkopf konnte Druckauftrag nicht annehmen.");
        }
    }

    private static class noDataToPrintFound extends Exception {

        public noDataToPrintFound() {
            super("Druckkopf konnte Datei zum drucken nicht finden.");
        }
    }
}
