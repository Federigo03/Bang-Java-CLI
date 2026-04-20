package lib;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Utils {
    final private static Scanner INPUT = new Scanner(System.in); 
    
    // Costruttore privato per evitare istanze della classe
    private Utils() {
        throw new UnsupportedOperationException("Utility class - no instantiation allowed");
    }

    static public int nextInt(String s){
        do{
            try {
                System.out.println(s);
                return INPUT.nextInt();
            } catch (InputMismatchException e) {
                System.err.println("Devi inserire un numero!");
                INPUT.nextLine();
            }
        }while(true);
    }

    static public int nextInt(){
        do{
            try {
                return INPUT.nextInt();
            } catch (InputMismatchException e) {
                System.err.println("Devi inserire un numero!");
                INPUT.nextLine();
            }
        }while(true);
    }

    static public String nextLine(String s){
        System.out.println(s);
        return INPUT.nextLine();
    }

    static public String nextLine(){
        return INPUT.nextLine();
    }
}
