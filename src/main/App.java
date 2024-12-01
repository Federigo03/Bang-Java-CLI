package main;

import match.Match;

public class App {

    public static void main(String[] args) throws Exception {
        int nStartingPlayers = 0;
        try {
            nStartingPlayers = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.out.println("You have to input the number of players");
            System.exit(-1);
        }
        boolean inputName;
        if(args.length > 1)
            inputName = Boolean.parseBoolean(args[1]);
        else
            inputName = true;
        try {
            new Match(nStartingPlayers, inputName);
        } catch (Exception e) {
            System.out.println("Wrong input" + e);
        }
    }
}
