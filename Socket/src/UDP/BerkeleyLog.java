/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UDP;

import java.io.IOException;
import java.time.LocalTime;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author 31660551
 */
public class BerkeleyLog {

    private Logger logger = Logger.getLogger("BerkeleyLog");
    private FileHandler fh;

    public BerkeleyLog(String fh) {
        try {
            // This block configure the logger with handler and formatter  
            this.fh = new FileHandler(fh);
            logger.addHandler(this.fh);
            SimpleFormatter formatter = new SimpleFormatter();
            this.fh.setFormatter(formatter);
            

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public void writeNewMessage(String maquina, LocalTime horarioAntes, LocalTime horarioDepois){
        String msg = "Antes da sincronização: ["+horarioAntes.toString()+"] Ip da maquina: " + maquina + "\n";
        msg += "Depois da sincronização: [" +horarioDepois.toString()+"] Ip da maquina: " + maquina + "\n";
        logger.log(Level.INFO, msg);
    }

}
