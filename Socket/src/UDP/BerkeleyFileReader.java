/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UDP;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;



/**
 *
 * @author 31660551
 */
public class BerkeleyFileReader {

    public static List<NodeMachine> readFile(String filePath) {
        File file = new File(filePath);
        try {
            Scanner sc = new Scanner(file);
             List<NodeMachine> nodes = new ArrayList<NodeMachine>(); 
            
            while (sc.hasNextLine()) {
                String[] line = sc.nextLine().split(":");
                NodeMachine nm = new NodeMachine(line[0], line[1]);
                nodes.add(nm);
            }
            return nodes;
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado");
        }
        return null;
    }
}
