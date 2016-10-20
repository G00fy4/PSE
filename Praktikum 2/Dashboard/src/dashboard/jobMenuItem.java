/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard;

import javax.json.JsonObject;

/**
 *
 * @author joe
 */
public class jobMenuItem {
    String text = null;
    MoveJobAction funk = null;
    
         public jobMenuItem  (String t, MoveJobAction m){
             text = t;
             funk = m;
             
         }
    String getText(){
        return text;
    }
    void runFunk(printerServer p, JsonObject json){
        funk.run(p, json);
    }
}
