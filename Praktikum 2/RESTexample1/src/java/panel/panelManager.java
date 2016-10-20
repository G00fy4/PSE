/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package panel;

import druckerkopf.DruckerKopf;
import druckerkopf.TonerHelper;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    
    public panelManager(){
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
        
        
        TonerHelper tBlue = new TonerHelper("127.0.0.1",9872, "blue");
        DruckerKopf.toners.put("blue", tBlue);
        System.out.println("*********************************************");
        
        Thread t;
        for (String key : DruckerKopf.toners.keySet()) {
            //Thread t = new Toner(Port,Füllstand);
            t = new Toner(DruckerKopf.toners.get(key).getPort(),15000);
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
        
        
        
    }

}
