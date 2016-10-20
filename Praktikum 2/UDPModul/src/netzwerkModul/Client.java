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
public abstract class Client {
    public abstract void setSendData(byte[] b);
    public abstract byte[] getReceiveData();
    public abstract void setIPAdress(InetAddress ip);
    public abstract void setPort(Integer x);
    public abstract String sendString(String message);
    public abstract void send(byte[] a);
    public abstract void send();
    public abstract byte[] empfang();
    public abstract void enpfang();
    public abstract void closeSocket();
    
    public void test() {
        String sentence = "test lol alter";
      //String a = sendString(sentence);
      setSendData(sentence.getBytes());
      send();
      String a = new String(empfang());
      System.out.println("FROM SERVER:" + a);
      /*sentence = "test lol alter 2";
      a = sendString(sentence);
      System.out.println("FROM SERVER:" + a);
        */
        }
            //- See more at: http://systembash.com/a-simple-java-tcp-server-and-tcp-client/#sthash.x3n7y9Cw.dpuf

}
