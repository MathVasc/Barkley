/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UDP;

/**
 *
 * @author matheusdevasconcelos
 */
public class BerkeleyTimeHelper {
    NodeMachine slave;
    int delta;
    int slaveTime;

    public BerkeleyTimeHelper(NodeMachine slave, int delta, int slaveTime) {
        this.slave = slave;
        this.delta = delta;
        this.slaveTime = slaveTime;
    }
    
}