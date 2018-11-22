package UDP;

/**
 *
 * @author Matheus de Vasconcelos Moura
 * @author Gabriel Batista Tenorio
 */
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;

class Slave {
    
    NodeMachine master;
    int time;
    LocalTime machineTime = LocalDateTime.now().toLocalTime();
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
            masterSocket = new DatagramSocket(master.getPort());
        } catch (SocketException ex) {
            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void sendMessage(String round, DatagramPacket packet){
        String sentence = round + "," + "slave" + "," + machineTime.getHour() + ":" + machineTime.getMinute() + ":" + machineTime.getSecond();
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
            String[] sentenceComponents = sentence.split(",");
            if (sentenceComponents[1].equals("master")) {
                if (sentenceComponents[2].equals("RequestTime")){
                    sendMessage(sentenceComponents[0], receivePacket);
                }else{
                    String[] stringTimeComponents = sentenceComponents[2].split(":");
                    LocalTime newTime = LocalTime.of(Integer.parseInt(stringTimeComponents[0]), Integer.parseInt(stringTimeComponents[1]));
                    updateTime(newTime);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void updateTime(LocalTime newTime){
        try {
            Runtime.getRuntime().exec("sudo +%T date -s " + newTime.getHour() + ":" + newTime.getMinute() + ":" + newTime.getSecond()); // MMddhhmm[[yy]yy]
        } catch (IOException ex) {
            Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
        }
        log.writeNewMessage(localhost.toString(), machineTime, newTime);
        
    }
    
    public void work(){
        while (true) {
            machineTime = LocalDateTime.now().toLocalTime();
            receiveMessage();
        }
    }
    
}
