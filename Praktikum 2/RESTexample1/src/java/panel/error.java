/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package panel;

/**
 *
 * @author joe
 */
public class error {
    String errorText;
    String errorGrund;
    Integer errorNummer;
    public error(String x, String y, Integer z){
        errorText = y;
        errorGrund = x;
        errorNummer = z;
    }
    public String getErrorText(){
        return errorText;
    }
    public String getErrorGrund(){
        return errorGrund;
    }
    public Integer getErrorNummer(){
        return errorNummer;
    }
}
