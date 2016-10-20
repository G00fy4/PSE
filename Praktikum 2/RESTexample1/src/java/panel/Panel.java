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
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import netzwerkModul.Client;
import netzwerkModul.Server;
import tcpmodul.*;
import udpmodul.*;
import panel.error;


/**
 *
 * @author tomatenjoe
 * ...To compile the file, open your terminal and type
 + javac filename.java
 + ...To run the generated class file, use
 + java filename
 */
public class Panel extends Thread{
    public static String druckerKopfIPAdresse = "127.0.0.1";
    public static Integer druckerKopfPort = 9871, meinServerPort = 9870;
    public static boolean isTCPConnection = false;
    
    
    public static List<druckAuftrag> druckAuftraege = new ArrayList<druckAuftrag>();
    public static Map<UUID,druckAuftrag> errors = new HashMap<UUID,druckAuftrag>();
    public static boolean work = false;
    public Panel(){
    
    }
 
    public void addDuckauftrag(druckAuftrag x){
        druckAuftraege.add(x);
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
        boolean zeigeMessage= true;
        while (work){
            while (druckAuftraege.size() > 0 && druckAuftraege.get(0).hasError() && !druckAuftraege.get(0).getRetry()){
                errors.put(druckAuftraege.get(0).getTimestamp(), druckAuftraege.get(0));
                druckAuftraege.remove(0);
            }
            
            if (druckAuftraege.isEmpty() && zeigeMessage){
                System.out.println("Habe keine Druckaufträge!");
                zeigeMessage = false;
            } 
            
            
            while (druckAuftraege.size() > 0){
                zeigeMessage = true;
                System.out.println("Drucke nächste Datei. Noch "+druckAuftraege.size()+" Druckaufträge in der Warteschlange");


                dateiSenden(druckAuftraege.get(0));
                if (druckAuftraege.get(0).hasError()){break;}

                try {
                    dateiDrucken(druckAuftraege.get(0));
                } catch (noDataToPrintFound ex) {
                    Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
                }

            }//end while (druckAuftraege.size() > 0)
        
        } // end while(work)
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
    private void dateiDrucken(druckAuftrag auftrag) throws noDataToPrintFound{
        
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
                druckAuftraege.get(0).setStatus(a+" gedruckt");
                auftrag.setStatus(a);
                statusServer.sendString("How long do you need?");
            }
            if (a.contains("Error")){
                //Fehlerbehandlung
                if (a.contains("tonerLeer")){
                    error e = new error(auftrag.dateiPfad, "Bitte Toner "+auftrag.getFarbe()+" auffüllen und ok eingeben",404 );
                    auftrag.addError(e);
                    errors.put(auftrag.getTimestamp(), auftrag);
                    druckAuftraege.remove(0);
                    /*
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
                    */
                    //Druckauftrag muss erneut gestartet werden
                }
            } else {
                //Druckauftrag erfolgreich
                auftrag.setStatus("finish");
                druckAuftraege.remove(0);
            }
                    
            statusServer.socketClose();
            confNetzwerkModul = null;
        }
        
    }
    
    private void dateiSenden(druckAuftrag auftrag){
        Client netzwerkModul = configClient();
                
        FileInputStream fis = null;
        Integer error = 0;
        while (error < 3){
            try {
                auftrag.setStatus("Sende Druckauftrag zum Druckkopf");
                //Druckerauftrag senden
                File fileProp = new File(auftrag.getPfad());
                System.out.println("Dateigroeße: "+(int)fileProp.length());
                if ((int)fileProp.length() == 0){
                     System.out.println("Datei ist 0 Byte groß");
                    error e = new error(auftrag.getPfad(), "Datei ist 0 Byte groß",404 );
                    auftrag.addError(e);
                    break;
                }
                fis = new FileInputStream(fileProp);
               
                try {
                    fis.read();
                } catch (IOException ex) {
                    Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Datei zum Drucken konnte nicht geöffnet werde.");
                    error e = new error(auftrag.getPfad(), "Datei zum Drucken konnte nicht geöffnet werde.",404 );
                    auftrag.addError(e);
                    break;
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
                        error e = new error(auftrag.getPfad(), "Druckkopf konnte Druckauftrag nicht annehmen.",404 );
                        auftrag.addError(e);
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
                            auftrag.setStatus("Sende Druckauftrag zum Druckkopf: "+ h.trim() +" von " + fileProp.length() + " byte übertragen.");
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
                error e = new error(auftrag.getPfad(), "Datei wurde nicht gefunden",404 );
                auftrag.addError(e);
                break;
            } catch (failedToSendPrintJob ex) {
                Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
                error e = new error(auftrag.getPfad(), "Datei konnte nicht übertragen werden. Versuch: "+error,400 );
                auftrag.addError(e);
                
            } /*finally {
                try {
                    
                    fis.close();
                } catch (IOException ex) {
                    Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }*/
            
        }
        if (error >= 3){
            System.out.println("Druckauftrag konnte inerhalb drei Versuchen nicht übermittelt werden! \n bitte versuchen sie es später erneut.");
            error e = new error(auftrag.getPfad(), "Druckauftrag konnte inerhalb drei Versuchen nicht übermittelt werden!",404 );
            auftrag.addError(e);
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
