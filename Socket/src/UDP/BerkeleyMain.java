/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UDP;

import java.util.List;

/**
 *
 * @author matheusdevasconcelos
 */

public class BerkeleyMain {
    public static void main(String[] args) {
        int time;
        String logFile;
        String opt = "-s";
        
        switch (opt){
            case "-m":
                time = 300;
                int limit = 20;
                String slaveFile = "slaves.txt";
                logFile = "masterLog.txt";
                List<NodeMachine> slaves = BerkeleyFileReader.readFile(slaveFile);
                Master master = new Master(time, slaves, limit, logFile);
                master.work();
                break;
            case "-s":
                String ip = "localhost";
                String port = "8081";
                time = 250;
                logFile = "slaveFile.txt";
                
                NodeMachine masterNode = new NodeMachine(ip, port);
                
                Slave slave = new Slave(masterNode, time, logFile);
                slave.work();
                break;
            default:
                System.out.println("Flag invalida, utiliza '-m' para criar um mestre e '-s' para um escravo");
                break;
        }
        
//        switch (args[0]){
//            case "-m":
//                time = Integer.parseInt(args[1]);
//                int limit = Integer.parseInt(args[2]);
//                String slaveFile = args[3];
//                logFile = args[4];
//                List<NodeMachine> slaves = BerkeleyFileReader.readFile(slaveFile);
//                Master master = new Master(time, slaves, limit, logFile);
//                master.work();
//                break;
//            case "-s":
//                String ip = args[1];
//                String port = args[2];
//                time = Integer.parseInt(args[3]);
//                logFile = args[4];
//                
//                NodeMachine masterNode = new NodeMachine(ip, port);
//                
//                Slave slave = new Slave(masterNode, time, logFile);
//                slave.work();
//                break;
//            default:
//                System.out.println("Flag invalida, utiliza '-m' para criar um mestre e '-s' para um escravo");
//                break;
//        }
    }
}
