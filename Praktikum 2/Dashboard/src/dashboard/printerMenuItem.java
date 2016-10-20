/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard;

/**
 *
 * @author joe
 */
public class printerMenuItem {
    String text = null;
    chosePrinterAction funk = null;
         public printerMenuItem  (String t, chosePrinterAction m){
             text = t;
             funk = m;
         }
    String getText(){
        return text;
    }
    printerServer runFunk(){
        return funk.run();
    }
}
