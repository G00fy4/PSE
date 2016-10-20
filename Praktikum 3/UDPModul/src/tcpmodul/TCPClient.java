/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpmodul;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import netzwerkModul.Client;

/**
 *
 * @author tomatenjoe
 */
public class TCPClient extends Client {
    Socket clientSocket = null;
    Integer port = 9871;
    InetAddress IPAddress = null;
    byte[] sendData = new byte[1024];
    byte[] receiveData = new byte[1024];
    
    public TCPClient(InetAddress ip,Integer p){
        try {
            IPAddress = ip;
            port = p;
            try {
                clientSocket = new Socket(IPAddress, port);
                clientSocket.setSoTimeout(10000);
            } catch (IOException ex) {
                Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        
            System.out.println("Client-Adresse: "+InetAddress.getLocalHost()+":"+clientSocket.getLocalPort());
        } catch (UnknownHostException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setSendData(byte[] b) {
        sendData = new byte[1024];
        sendData=b;
    }

    @Override
    public byte[] getReceiveData() {
        return receiveData;
    }

    @Override
    public void setIPAdress(InetAddress ip) {
        IPAddress = ip;
    }

    @Override
    public void setPort(Integer x) {
        port = x;
    }

    @Override
    public String sendString(String message) {
        sendData = new byte[1024];
        sendData = message.getBytes();
        send();
        enpfang();
        String a  = new String(receiveData).trim();
        return a;
    }

    @Override
    public void send(byte[] a) {
        sendData = new byte[1024];
        sendData = a;
        send();
    }

    @Override
    public void send() {
        OutputStream out = null;
        try {
            out = clientSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            dos.write(sendData, 0, sendData.length);
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        
        }
    }

    @Override
    public byte[] empfang() {
        enpfang();
        return receiveData;
    }

    @Override
    public void enpfang() {
        InputStream in = null;
        try {
            boolean x = true;
            while (x){
                in = clientSocket.getInputStream();

                DataInputStream dis = new DataInputStream(in);
                receiveData = new byte[1024];
                dis.read(receiveData, 0, receiveData.length);
                if (!new String(receiveData).trim().isEmpty()){
                    x = false;
 //                   System.out.println("Daten empfangen.");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    
    }

    @Override
    public void closeSocket() {
        try {
            clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

  
}
