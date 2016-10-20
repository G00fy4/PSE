/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package toner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import udpmodul.UDPClient;

/**
 *
 * @author joe
 */
public class TonerTest {
    UDPClient netzwerkModul = new udpmodul.UDPClient();
    public TonerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        try {
            
            netzwerkModul.setIPAdress(InetAddress.getByName("127.0.1.1"));
            netzwerkModul.setPort(9874);
        } catch (UnknownHostException ex) {
            Logger.getLogger(TonerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @After
    public void tearDown() {
        netzwerkModul.closeSocket();
    }

    /**
     * Test of main method, of class Toner.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        Toner.main(args);
        // TODO review the generated test code and remove the default call to fail.
        String a = netzwerkModul.sendString("Are you empty?");
         assertEquals(Integer.parseInt(a.trim()), 10000) ;
        //fail("The test case is a prototype.");
    }
    
}
