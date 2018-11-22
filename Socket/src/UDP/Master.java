package UDP;

/**
 *
 * @author Matheus de Vasconcelos Moura
 * @author Gabriel Batista Tenorio
 */
import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

class Master {

    int time;
    LocalTime machineTime = LocalDateTime.now().toLocalTime();
    List<NodeMachine> slaves;
    int limit;
    BerkeleyLog log;
    DatagramSocket[] serverSockets;
    int round = 0;
    InetAddress localhost;
    int deltaInit;
    int deltaFinish;

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
            String sentence = round + "," + "master" + "," + message;
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
                String[] sentenceComponents = sentence.split(",");
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

    private List<BerkeleyTimeHelper> deltaTimes(Map<NodeMachine, DatagramPacket> responses){
        List<BerkeleyTimeHelper> deltasAndTimes = new ArrayList<>();
        
        for (Map.Entry<NodeMachine, DatagramPacket> response : responses.entrySet()){
            String sentence = new String(response.getValue().getData(), response.getValue().getOffset(), response.getValue().getLength());
            System.out.println(sentence);
            String stringTime = sentence.split(",")[2];
            String[] stringTimeComponents = stringTime.split(":");
            LocalTime slaveTime = LocalTime.of(Integer.parseInt(stringTimeComponents[0]), Integer.parseInt(stringTimeComponents[1]));
            LocalTime differenceTime = slaveTime.minusHours(machineTime.getHour()).minusMinutes(machineTime.getMinute());
            
            if (differenceTime.getHour() == 0){
                if (differenceTime.getMinute() <= limit){
                    System.out.println("Escravo dentro limite:"+slaveTime.toString());
                    System.out.println("Diferença dentro limit:"+differenceTime.toString());
                    deltasAndTimes.add(new BerkeleyTimeHelper(response.getKey(), differenceTime.getMinute(), slaveTime));
                }
            }else{
                if (differenceTime.getMinute() + (differenceTime.getHour()*60) <= limit){
                    deltasAndTimes.add(new BerkeleyTimeHelper(response.getKey(), differenceTime.getMinute() + (differenceTime.getHour()*60), slaveTime));
                }
            }
            
        }
        
        return deltasAndTimes;
    }
    
    private List<NodeMachine> outOfLimitTimes(Map<NodeMachine, DatagramPacket> responses){
        List<NodeMachine> outOfLimit = new ArrayList<>();
        
        for (Map.Entry<NodeMachine, DatagramPacket> response : responses.entrySet()){
            String sentence = new String(response.getValue().getData(), response.getValue().getOffset(), response.getValue().getLength());

            String stringTime = sentence.split(",")[2];
            String[] stringTimeComponents = stringTime.split(":");
            LocalTime slaveTime = LocalTime.of(Integer.parseInt(stringTimeComponents[0]), Integer.parseInt(stringTimeComponents[1]));
            LocalTime differenceTime = slaveTime.minusHours(machineTime.getHour()).minusMinutes(machineTime.getMinute());
            if (differenceTime.getHour() == 0){
                if (differenceTime.getMinute() > limit){
                    System.out.println("Escravo fora limite:"+slaveTime.toString());
                    System.out.println("Diferença fora limite:"+differenceTime.toString());
                    outOfLimit.add(response.getKey());
                }
            }else {
                if (differenceTime.getMinute() + (differenceTime.getHour()*60) > limit){
                    outOfLimit.add(response.getKey());
                }
            }
        }
        
        return outOfLimit;
    }
    
    private Map<NodeMachine, LocalTime> timeCorrect(List<BerkeleyTimeHelper> slaveTimesAndDelta){
        Map<NodeMachine, LocalTime> correctTimes = new HashMap<>();
        
        int sum = 0;
        
        for (BerkeleyTimeHelper timeAndDelta : slaveTimesAndDelta){
            sum+= timeAndDelta.delta;
        }
        
        int avg;
        
        if (0 <= limit){
            avg = (sum+0)/(slaveTimesAndDelta.size()+1);
            TemporalAmount amout = Duration.ofMinutes((avg));
            LocalTime afterCalculus = machineTime.plus(amout);
            System.out.println("Novo horario: "+afterCalculus);
            try {
                Runtime.getRuntime().exec("sudo +%T date -s " + afterCalculus.getHour() + ":" + afterCalculus.getMinute() + ":" + afterCalculus.getSecond()); // MMddhhmm[[yy]yy]
            } catch (IOException ex) {
                Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
            }
            log.writeNewMessage(localhost.toString(), machineTime, afterCalculus);
            machineTime = afterCalculus;
        }else{
            avg = sum/slaveTimesAndDelta.size();
        }
        
        for (BerkeleyTimeHelper timeAndDelta : slaveTimesAndDelta){
            TemporalAmount amout = Duration.ofMinutes((avg-timeAndDelta.delta));
            correctTimes.put(timeAndDelta.slave, timeAndDelta.slaveTime.plus(amout));
        }
        
        return correctTimes;
    }
    
    public void work(){
        while(true){
            machineTime = LocalDateTime.now().toLocalTime();
            Map<NodeMachine, DatagramPacket> responses = new HashMap<>();
            for (NodeMachine slave :slaves){
                sendMessage(slave, round, "RequestTime");
                DatagramPacket response = receiveMessage(slave, round);
                if (response != null) {
                    responses.put(slave, response);
                }
            }
            if (responses.size() > 0){
                Map<NodeMachine, LocalTime> timesCorrect = timeCorrect(deltaTimes(responses));
            
                for (Map.Entry<NodeMachine, LocalTime> timeCorrect : timesCorrect.entrySet()) {
                    sendMessage(timeCorrect.getKey(), round, timeCorrect.toString());
                }
                
                List<NodeMachine> outOfLimitNodes = outOfLimitTimes(responses);
                
                for (NodeMachine node : outOfLimitNodes){
                    sendMessage(node, round, machineTime.toString());
                }
                round++;
            }
        }
    }
}
