/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netzwerkModul;

import java.net.InetAddress;

/**
 *
 * @author tomatenjoe
 */
public abstract class Server {
    public abstract Integer sendString(String t);
    public abstract InetAddress getClientIPAddress();
    public abstract Integer setAndSendData(byte[] b);
    public abstract Integer setSendData(byte[] b);
    public abstract byte[] getReceiveData();
    public abstract void enpfangeDaten();
    public abstract void socketClose();
    //public abstract void clientSocketClose();
    public abstract Integer antwortSenden();
    public void test(){
    // while(true)
        //    {
        enpfangeDaten();
        String sentence = new String(getReceiveData()).trim();
        System.out.println("RECEIVED: " + sentence);

        String capitalizedSentence = sentence.toUpperCase();
        setSendData(capitalizedSentence.getBytes());
        antwortSenden();
        //    } //- See more at: https://systembash.com/a-simple-java-udp-server-and-udp-client/
    }
}
