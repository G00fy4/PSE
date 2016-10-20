/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package panel;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import panel.error;

/**
 *
 * @author joe
 */
public class druckAuftrag {
    UUID time;
    String dateiPfad, farbe, status, userID;
    List<error> errorList = new ArrayList<error>();
    boolean retry = true;
    panelManager panelM = null;
    //public druckAuftrag(String pfad, String f, String u, panelManager p) {
    public druckAuftrag(String pfad, String f, String u) {
        farbe=f;
        dateiPfad=pfad;
        time = UUID.randomUUID(); 
        status = "Warte in Druckerwarteschlange";
        userID= u;
    }
    public void addError(error e){
        errorList.add(e);
        retry = false;
    }
    public List<error> getErrors(){
        return errorList;
    }
    public boolean hasError(){
        boolean r = true;
        if(errorList.size() == 0){
            r = false;
        }
        return r;
    }
    public String getPfad(){
        return dateiPfad;
    }
    public String getFarbe(){
        return farbe;
    }
    public UUID getTimestamp(){
        return time;
    }
    public void setStatus(String x){
        status =x;
        panelM.sendStatus();
    }
    public String getStatus(){
       return status;
    }
    public void retry(){
        retry = true;
    }
    public Boolean getRetry(){
       return retry;
    }
    public String getUserId(){
        return userID;
    }
}
