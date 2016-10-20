/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//Quelle: https://www.tutorials.de/threads/udp-mit-java.205651/
package udpmodul;
 
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import netzwerkModul.Client;
/**
 *
 * @author tomatenjoe
 */
public class UDPClient extends Client {
    InetAddress IPAddress;
    Integer port = 9876;
    byte[] sendData = new byte[1024];
    byte[] receiveData = new byte[1024];
    DatagramSocket socket;
    
    public UDPClient() {
        try {
            this.IPAddress = InetAddress.getByName("localhost");
        } catch (UnknownHostException ex) {
            Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(10000);
        } catch (SocketException ex) {
            Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            System.out.println("Client-Adresse: "+InetAddress.getLocalHost()+":"+socket.getLocalPort());
        } catch (UnknownHostException ex) {
            Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param b
     */
    @Override
    public void setSendData(byte[] b){
        sendData = new byte[1024];
        sendData=b;
    }
    @Override
    public byte[] getReceiveData(){
        return receiveData;
    }

    /**
     *
     * @param ip
     */
    @Override
    public void setIPAdress(InetAddress ip){
        IPAddress = ip;
    }

    /**
     *
     * @param x
     */
    @Override
    public void setPort(Integer x){
        port = x;
    }

    /**
     *
     * @param message
     * @return
     */
    @Override
    public String sendString(String message){
        sendData = new byte[1024];
        sendData = message.getBytes();
        send();
        enpfang();
        String a  = new String(receiveData).trim();
        return a;
    }
    /*
    @byte[] zu sendenden Daten
    */

    /**
     *
     * @param a
     */

    @Override
    public void send(byte[] a){
        sendData = new byte[1024];
        sendData = a;
        send();
    }
    
    /**
     *
     */
    @Override
    public void send(){
        
           DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
           //Sendet Daten
            try {
                socket.send(sendPacket);
            } catch (IOException ex) {
                Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
            }
              //FEhlerbehandlung für localhost namensauflösung
        
    }

    /**
     *
     * @return
     */
    @Override
    public byte[] empfang(){
        enpfang();
        return receiveData;
    }
    
    /**
     *
     */
    @Override
    public void enpfang(){
        
        //warte auf antwort
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                //System.out.println("cccc"+receiveData.length);
                socket.receive(receivePacket);
                //System.out.println("bbbb");
            } catch (IOException ex) {
                Logger.getLogger(UDPClient.class.getName()).log(Level.SEVERE, null, ex);
                //System.out.println("aaaa");
            }
            receiveData = new byte[1024];
            receiveData = receivePacket.getData();
            
    }
    @Override
    public void closeSocket(){
        socket.close();
    }
       
}
