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
import netzwerkModul.Server;

/**
 *
 * @author tomatenjoe
 */
public class UDPServer extends Server {

    Integer port = 9876;
    private byte[] receiveData = new byte[1024];
    private boolean receiveDataValit = false; //false= receiveData ist nciht aktuell
    private byte[] sendData = new byte[1024];
    private boolean sendDataValit = false; //False = nicht akteuell
    InetAddress clientIPAddress = null;
    Integer clientPort = 0;

    DatagramSocket socket;

    public UDPServer(Integer x) {
        try {
            port = x;
            System.out.println("Server-Adresse: " + InetAddress.getLocalHost() + ":" + port);
        } catch (UnknownHostException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Integer sendString(String t) {
        return setAndSendData(t.getBytes());
    }

    public InetAddress getClientIPAddress() {
        return clientIPAddress;
    }

    public Integer setAndSendData(byte[] b) {
        Integer i = setSendData(b);
        antwortSenden();
        return i;
    }

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

    public byte[] getReceiveData() {
        receiveDataValit = false;
        byte[] r = receiveData;
        receiveData = new byte[1024];
        return r;
    }

    public void enpfangeDaten() {

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            socket.receive(receivePacket);
            clientIPAddress = receivePacket.getAddress();
            clientPort = receivePacket.getPort();
            receiveDataValit = true;
        } catch (IOException ex) {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        receiveData = receivePacket.getData();

    }

    public void socketClose() {
        socket.close();
    }
    //@Override
    public void clientSocketClose(){}

    public Integer antwortSenden() {
        Integer error = 0;
        if (clientIPAddress == null) {
            error = -2;
        } else {
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIPAddress, clientPort);
                socket.send(sendPacket);
                sendDataValit = false;

            } catch (IOException ex) {
                Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
                error = -1;
            }
        }
        return error;
    }

}
