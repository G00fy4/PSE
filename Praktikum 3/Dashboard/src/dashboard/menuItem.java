package dashboard;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author joe
 */
public class menuItem {
    
    
    String text = null;
    MoveAction funk = null;
         public menuItem  (String t, MoveAction m){
             text = t;
             funk = m;
         }
    String getText(){
        return text;
    }
    void runFunk(){
        funk.run();
    }
}
