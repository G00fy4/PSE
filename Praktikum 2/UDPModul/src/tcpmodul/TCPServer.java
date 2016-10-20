/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpmodul;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import netzwerkModul.Server;

/**
 *
 * @author tomatenjoe
 */
public class TCPServer extends Server {
    Integer port = 6789;
    Socket clientSocket = null;
    ServerSocket serverSocket = null;
    private byte[] receiveData = new byte[1024];
    private boolean receiveDataValit = false; //false= receiveData ist nciht aktuell
    private byte[] sendData = new byte[1024];
    private boolean sendDataValit = false; //False = nicht akteuell
    InetAddress clientIPAddress = null;
    Integer clientPort = 0;
   

    public TCPServer(Integer serverPort) {
        try {
            port = serverPort;
            try {
                System.out.println("Server-Adresse: " + InetAddress.getLocalHost() + ":" + port);
            } catch (UnknownHostException ex) {
                Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            System.out.println("Conection mit Client"+clientSocket.getRemoteSocketAddress());
        } catch (IOException ex) {
            Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Integer sendString(String t) {
        return setAndSendData(t.getBytes());
    }

    @Override
    public InetAddress getClientIPAddress() {
        return clientIPAddress;
    }

    @Override
    public Integer setAndSendData(byte[] b) {
        Integer i = setSendData(b);
        antwortSenden();
        return i;
    }

    @Override
    public Integer setSendData(byte[] b) {
        Integer error = 0;
        if (!sendDataValit) {
            sendData = new byte[1024];
            sendData = b;
            sendDataValit = true;
        } else {
            error = -1;
        }
        return error;
    }

    @Override
    public byte[] getReceiveData() {
        receiveDataValit = false;
        byte[] r = receiveData;
        receiveData = new byte[1024];
        return r;
    }

    @Override
    public void enpfangeDaten() {
        
        try {
            boolean x = true;
            while (x){
                InputStream in = clientSocket.getInputStream();
                in.read(receiveData, 0, receiveData.length);
                if (!new String(receiveData).trim().isEmpty()){
                    x = false;
 //                   System.out.println("Daten empfangen.");
                }
            }
            clientIPAddress = clientSocket.getInetAddress();
            clientPort = clientSocket.getPort();
            receiveDataValit = true;
                    } catch (IOException ex) {
            Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void socketClose() {
        try {
            clientSocket.close();
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void clientSocketClose(){
        try {
            clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Integer antwortSenden() {
        Integer error = 0;
        if (clientIPAddress == null) {
            error = -2;
        } else {
            try {
                OutputStream out = null;
                out = clientSocket.getOutputStream();
                out.write(sendData, 0, sendData.length);
                sendDataValit = false;

            } catch (IOException ex) {
                Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                error = -1;
            }
        }
        return error;
    }

    
}
