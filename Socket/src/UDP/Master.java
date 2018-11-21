package UDP;

/**
 *
 * @author Matheus de Vasconcelos Moura
 * @author Gabriel Batista Tenorio
 */
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

class Master {

    int time;
    List<NodeMachine> slaves;
    int limit;
    BerkeleyLog log;
    DatagramSocket[] serverSockets;
    int round = 0;
    InetAddress localhost;

    public Master(int time, List<NodeMachine> slaves, int limit, String logFile) {
        this.time = time;
        this.slaves = slaves;
        this.limit = limit;
        this.log = new BerkeleyLog(logFile);
        this.serverSockets = new DatagramSocket[slaves.size()];
        try {
            this.localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
        }
        slaveInicializer();
    }

    private void slaveInicializer() {
        for (int i = 0; i < slaves.size(); i++) {
            try {
                this.serverSockets[i] = new DatagramSocket(slaves.get(i).getPort());
            } catch (SocketException ex) {
                Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void sendMessage(NodeMachine slave, int round, String message) {
            String sentence = round + ":" + "master" + ":" + message;
            byte[] packageMsg = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(packageMsg, sentence.length(), slave.getIp(), slave.getPort());
            try {
                serverSockets[slaves.indexOf(slave)].send(sendPacket);
            } catch (IOException ex) {
                Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    private DatagramPacket receiveMessage(NodeMachine slave, int round) {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        if (!slave.isReceivePackage()) {
                            serverSockets[slaves.indexOf(slave)].disconnect();
                        }
                    }
                };
                Timer timer = new Timer("Timer");
                long delay = 20000L;
                timer.schedule(task, delay);

                serverSockets[slaves.indexOf(slave)].receive(receivePacket);
                timer.cancel();
                slave.setReceivePackage(true);
                String sentence = new String(receivePacket.getData());
                String[] sentenceComponents = sentence.split(":");
                if (sentenceComponents[0].equals(String.valueOf(round)) && !sentenceComponents[1].equals("master")) {
                    return receivePacket;
                }
            } catch (IOException ex) {
                if (!slave.isReceivePackage()) {
                    serverSockets[slaves.indexOf(slave)].connect(slave.getIp(), slave.getPort());
                }
                Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
    }

    private Map<NodeMachine, Integer> deltaTimes(Map<NodeMachine, DatagramPacket> responses){
        Map<NodeMachine, Integer> deltas = new HashMap<>();
        
        for (Map.Entry<NodeMachine, DatagramPacket> response : responses.entrySet()){
            String sentence = new String(response.getValue().getData());
            int slaveTime = Integer.parseInt(sentence.split(":")[1]);
            
            if (slaveTime <= limit) {
                int delta = slaveTime - time;
                deltas.put(response.getKey(), delta);
            }
        }
        
        return deltas;
    }
    
    private Map<NodeMachine, Integer> timeCorrect(Map<NodeMachine, Integer> deltas){
        Map<NodeMachine, Integer> correctTimes = new HashMap<>();
        
        int sum = 0;
        
        for (Map.Entry<NodeMachine, Integer> delta : deltas.entrySet()) {
            sum+= delta.getValue();
        }
        
        int avg;
        
        if (time <= limit){
            avg = (sum+time)/deltas.size();
            log.writeNewMessage(localhost.toString(), String.valueOf(time), String.valueOf(time-avg));
            time = time - avg;
        }else{
            avg = sum/deltas.size();
        }
        
        for (Map.Entry<NodeMachine, Integer> delta : deltas.entrySet()) {
            correctTimes.put(delta.getKey(), delta.getValue()-avg);
        }
        
        return correctTimes;
    }
    
    public void work(){
        while(true){
            Map<NodeMachine, DatagramPacket> responses = new HashMap<>();
            for (NodeMachine slave :slaves){
                sendMessage(slave, round, "RequestTime");
                DatagramPacket response = receiveMessage(slave, round);
                if (response != null) {
                    responses.put(slave, response);
                }
            }
            
            if (responses.size() > 0){
                Map<NodeMachine, Integer> timesCorrect = timeCorrect(deltaTimes(responses));
            
                for (Map.Entry<NodeMachine, Integer> timeCorrect : timesCorrect.entrySet()) {
                  sendMessage(timeCorrect.getKey(), round, String.valueOf(timeCorrect.getValue()));
                }
                round++;
            }
        }
    }
}
