package main;

import match.Match;

public class App {

    public static void main(String[] args) throws Exception {
        try {
            final int nStartingPlayers = Integer.parseInt(args[0]);
            Match match = new Match(nStartingPlayers);
        } catch (NumberFormatException e) {
            System.out.println("You have to input the number of players");
            System.exit(-1);
        }
    }
}
