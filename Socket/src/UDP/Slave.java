package UDP;

/**
 *
 * @author Matheus de Vasconcelos Moura
 * @author Gabriel Batista Tenorio
 */
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class Slave {
    
    NodeMachine master;
    int time;
    BerkeleyLog log;
    DatagramSocket masterSocket;
    InetAddress localhost;
    
    public Slave(NodeMachine master, int time, String logFile) {
        this.master = master;
        this.time = time;
        this.log = new BerkeleyLog(logFile);
        try {
            this.localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
        }
        initMaster(this.master);
    }
    
    private void initMaster(NodeMachine master){
        try {
            masterSocket = new DatagramSocket(8080);
        } catch (SocketException ex) {
            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void sendMessage(String round, DatagramPacket packet){
        String sentence = round + ":" + "slave" + ":" + time;
        byte[] packageMsg = sentence.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(packageMsg, sentence.length(), packet.getAddress(), packet.getPort());
        
        try {
            masterSocket.send(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void receiveMessage(){
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            masterSocket.receive(receivePacket);
            String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
            String[] sentenceComponents = sentence.split(":");
            if (sentenceComponents[1].equals("master")) {
                if (sentenceComponents[2].equals("RequestTime")){
                    sendMessage(sentenceComponents[0], receivePacket);
                }else{
                    System.out.println(sentenceComponents[0]);
                    System.out.println(sentenceComponents[1]);
                    System.out.println(sentenceComponents[2]);
                    updateTime(Integer.parseInt(sentenceComponents[2]));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void updateTime(int value){
        log.writeNewMessage(localhost.toString(), String.valueOf(time), String.valueOf(time+value));
        time+= value;
    }
    
    public void work(){
        while (true) {
            receiveMessage();
        }
    }
    
}
