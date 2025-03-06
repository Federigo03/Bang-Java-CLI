package lib;

import java.util.Scanner;

public class Utils {    
    // Costruttore privato per evitare istanze della classe
    private Utils() {
        throw new UnsupportedOperationException("Utility class - no instantiation allowed");
    }

    static public int nextInt(Scanner input, String s){
        do{
            try {
                if(s != "")
                    s += '\n';
                System.out.print(s);
                return input.nextInt();
            } catch (Exception e) {
                System.err.println("Devi inserire un numero!");
                input.nextLine();
            }
        }while(true);
    }
}
