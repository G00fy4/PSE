/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dashboard;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author joe
 */
class printerServer {
    String name;
    String BASE_URI;
    //Farbe , Füllstand
    Map<String, Integer> toner = new HashMap<String, Integer>();
    
    
    public printerServer(String n, String url){
        BASE_URI = url;
        name = n;
    }
    
    String getURL(){
        return BASE_URI;
    }
    String getName(){
        return name;
    }
}
