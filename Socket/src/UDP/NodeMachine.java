/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UDP;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 31660551
 */
public class NodeMachine {
    
    private InetAddress ip;
    private int port;
    private boolean receivePackage = false; 

    public NodeMachine(String ip, String port) {
        try {
            this.ip = InetAddress.getByName(ip);
        } catch (UnknownHostException ex) {
            Logger.getLogger(NodeMachine.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.port = Integer.parseInt(port);
    }

    public NodeMachine(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }
    
    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isReceivePackage() {
        return receivePackage;
    }

    public void setReceivePackage(boolean receivePackage) {
        this.receivePackage = receivePackage;
    }

       
    
}
