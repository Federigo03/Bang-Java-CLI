package main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;


import cards.*;
import characters.Characters;
import player.Player;

public class App {

    public static void main(String[] args) throws Exception {
        boolean match = true;
        if(args[0].compareTo("4") < 0 || args[0].compareTo("7") > 0){
            System.out.println("Impossible to play with this number of player");
            System.exit(-1);
        }
        int startingNPlayers = Integer.parseInt(args[0]);
        int nPlayers = startingNPlayers;
        String[] names = new String[nPlayers];
        for(int i = 0; i < nPlayers; i++)
            names[i] = "Player " + (i+1);
        String[] roles = drawRoles(nPlayers);
        int sheriff = findSheriff(roles, nPlayers, names);
        Characters[] characters = drawCharacters(createCharacters(), nPlayers, names);
        Player[] players = new Player[nPlayers];
        for(int i = 0; i < nPlayers; i++)
            players[i] = new Player(roles[i], characters[i], names[i]);
        int sidKetchum = findCharacter(players, "Sid Ketchum", nPlayers);
        LinkedList<PlayingCard> deck = shuffle(createCards());
        LinkedList<PlayingCard> discardPile = new LinkedList<PlayingCard>();
        for(int i = 0; i < nPlayers; i++)
            for(int j = 0; j < players[i].getLifes(); j++)
                players[i].draw(deck);
        int turnPlayer = sheriff;
        Scanner input = new Scanner(System.in);
        skipTurn:
        while (match) {
            String turnCharacter = players[turnPlayer].toString();
            System.out.println(turnCharacter + "'s turn:");
            players[turnPlayer].readHand();
            int choice = 0, drawn = 0;
            boolean possible = false;
            if(sidKetchum >= 0 && players[sidKetchum].getHandSize() >= 2)
                sidKetchum(players[sidKetchum], discardPile, input);
            if(players[turnPlayer].getDynamite() != null){
                boolean res;
                if(turnCharacter == "Lucky Duke")
                    res = luckyDynamite(players, turnPlayer, nPlayers, deck, discardPile, input);
                else
                    res = dynamiteEffect(players, turnPlayer, nPlayers, deck, discardPile, input);
                if(res)
                    if(!endMatch(players, nPlayers, startingNPlayers, turnPlayer)){
                        death(players, nPlayers, -1, turnPlayer, sheriff, sidKetchum, null, turnPlayer, input, deck, discardPile);
                        if(turnPlayer == nPlayers)
                            turnPlayer = 0;
                        else
                            turnPlayer++;
                        continue skipTurn;
                    }
            }
            if(players[turnPlayer].getJail() != null){
                boolean res;
                if(turnCharacter == "Lucky Duke")
                    res = luckyLuke(deck, discardPile, input);
                else
                    res = drawHearts(deck, discardPile);
                players[turnPlayer].removeJail().discard(discardPile);
                if(!res){
                    if(turnPlayer == nPlayers-1)
                        turnPlayer = 0;
                    else
                        turnPlayer++;
                    continue;
                }
            }
            for(int i = 0; i < nPlayers; i++)
                if(i != turnPlayer)
                    if(players[i].getHandSize() > 0){
                        possible = true;
                        i = nPlayers;
                    }
            switch(turnCharacter){
                case "Jesse Jones":
                    if(possible){
                        do{
                            System.out.println(turnCharacter + ": <Choose 1 to activate your character's ability or 0 to ignore ");
                            choice = input.nextInt();
                        }while(choice < 0 || choice > 1);
                        if(sidKetchum >= 0 && players[sidKetchum].getHandSize() >= 2)
                            sidKetchum(players[sidKetchum], discardPile, input);
                        if(choice == 1){
                            possible = false;
                            do{
                                System.out.println(turnCharacter + ": <Choose player's id you want to draw from or 0 to cancel");
                                printActivePlayers(players, nPlayers);
                                choice = input.nextInt() - 1;
                                if(choice >= 0 && choice < nPlayers && choice != turnPlayer)
                                    if(players[choice].getHandSize() > 0)
                                        possible = true;
                            }while(!possible || choice < -1 || choice >= nPlayers);
                            if(sidKetchum >= 0 && players[sidKetchum].getHandSize() >= 2)
                                sidKetchum(players[sidKetchum], discardPile, input);
                            if(choice != -1){
                                if(players[choice].getHandSize() > 0)
                                    panicEffect(players[turnPlayer], players[choice]);
                                else
                                    System.out.println(turnCharacter + ": Ability unsuccessfull: no card to draw from his hand. You can't draw it from the deck either");
                                drawn++;
                            }
                        }
                    }
                    break;
                case "Kit Carlson":
                    PlayingCard[] t = new PlayingCard[3];
                    System.out.println(turnCharacter + ": <Choose two of these");                    
                    for(int j = 0; j < 3; j++){
                        if(deck.getFirst() == null)
                            deck = noDeck(discardPile);
                        if(sidKetchum >= 0 && players[sidKetchum].getHandSize() >= 2)
                            sidKetchum(players[sidKetchum], discardPile, input);
                        t[j] = deck.removeFirst();
                        System.out.println((j+1) + ") " + t[j]);
                    }
                    do{
                        choice = input.nextInt() - 1;
                    }while(choice < 0 || choice >= 3);
                    int x;
                    do{
                        x = input.nextInt() - 1;
                    }while (x < 0 || x >= 3 || x == choice);
                    System.out.println(turnCharacter + "(Drawn: " + t[choice]);
                    System.out.println(turnCharacter + "(Drawn: " + t[x]);
                    players[turnPlayer].getHand().add(t[choice]);
                    players[turnPlayer].getHand().add(t[x]);
                    players[turnPlayer].increaseHandSize();
                    players[turnPlayer].increaseHandSize();
                    if(x + choice == 1)
                        deck.addFirst(t[2]);
                    else if(x + choice == 2)
                        deck.addFirst(t[1]);
                    else if(x + choice == 3)
                        deck.addFirst(t[0]);
                    drawn = 2;
                    break;
                case "Pedro Ramirez":
                    if(discardPile.getFirst() != null){
                        readDiscard(discardPile.getFirst());
                        if(sidKetchum >= 0 && players[sidKetchum].getHandSize() >= 2)
                            sidKetchum(players[sidKetchum], discardPile, input);
                        do{
                            System.out.println(turnCharacter + ": <Insert 1 to activate your character's ability or 0 to ignore ");
                            choice = input.nextInt();
                        }while(choice < 0 || choice > 1);
                        if(sidKetchum >= 0 && players[sidKetchum].getHandSize() >= 2)
                            sidKetchum(players[sidKetchum], discardPile, input);
                        if(choice == 1){
                            players[turnPlayer].draw(discardPile);
                            drawn++;
                        }
                        if(sidKetchum >= 0 && players[sidKetchum].getHandSize() >= 2)
                            sidKetchum(players[sidKetchum], discardPile, input);
                    }
                    break;
            }
            for(; drawn < 2; drawn++){
                if(sidKetchum >= 0 && players[sidKetchum].getHandSize() >= 2)
                    sidKetchum(players[sidKetchum], discardPile, input);
                if(deck.getFirst() == null)
                    deck = noDeck(discardPile);
                if(drawn == 1 && turnCharacter == "Black Jack"){
                    System.out.println(deck.getFirst());
                    if(sidKetchum >= 0 && players[sidKetchum].getHandSize() >= 2)
                        sidKetchum(players[sidKetchum], discardPile, input);
                    if(deck.getFirst().getSuit() == 'H' || deck.getFirst().getSuit() == 'D'){
                        players[turnPlayer].draw(deck);
                        if(deck.getFirst() == null)
                            deck = noDeck(discardPile);
                    }
                }
                players[turnPlayer].draw(deck);
            }
            if(sidKetchum >= 0 && players[sidKetchum].getHandSize() >= 2)
                sidKetchum(players[sidKetchum], discardPile, input);
            boolean goon = true, bang = false;
            int[] distances = distances(players, nPlayers, turnPlayer);
            while(goon){
                do{
                    System.out.println(turnCharacter + ": <Choose an option:\n1: Print remaining lifes.\n2: Print characters.\n3: Print the Sheriff.\n4: Number of cards in other's hand.\n5: Print your hand.\n6: Print your role.\n7: Print active cards.\n8: Print distances.\n9: Play a card.\n10: Terminate your turn.");
                    choice = input.nextInt();
                }while(choice < 0 || choice > 10);
                switch (choice) {
                    case 1:
                        do{
                            System.out.println(turnCharacter + ": <Choose player's id to check his lifes or 0 to check everyones'");
                            printActivePlayers(players, nPlayers);
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printLifes(players, choice, nPlayers);
                        break;
                    case 2:
                        do{
                            System.out.println(turnCharacter + ": <Choose player's id to check his character or 0 to check everyone's ");
                            printActivePlayers(players, nPlayers);
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printCharacters(players, choice, nPlayers);
                        break;
                    case 3:
                        findSheriff(players, nPlayers);
                        break;
                    case 4:
                        do{
                            System.out.println(turnCharacter + ": <Choose player's id to check the number of cards in his hands or 0 to check everyone's ");
                            printActivePlayers(players, nPlayers);
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printNHands(players, choice, nPlayers);
                        break;
                    case 5:
                        players[turnPlayer].readHand();
                        break;
                    case 6:
                        players[turnPlayer].readRole();
                        break;
                    case 7:
                        do{
                            System.out.println(turnCharacter + ": <Choose player's id to check his active cards or 0 to check everyones' ");
                            printActivePlayers(players, nPlayers);
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printActiveCards(players, choice, nPlayers);
                        break;  
                    case 8:
                        do{
                            System.out.println(turnCharacter + ": <Choose player's id to check his actual distance from you or 0 to check everyone's ");
                            printActivePlayers(players, nPlayers);
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers || choice == turnPlayer);
                        printDistances(distances, choice, nPlayers, turnPlayer, players);
                        break;
                    case 9:
                        int playable[] = new int[players[turnPlayer].getHandSize()];
                        int pSize = 0;
                        for(int i = 0; i < players[turnPlayer].getHandSize(); i++){
                            String name = players[turnPlayer].getHand().get(i).getName();
                            switch(name){
                                case "Missed!":
                                    if(players[turnPlayer].toString() != "Calamity Janet")
                                        break;
                                case "Bang!":
                                    if(!bang || players[turnPlayer].isUnlimitedBang()){
                                        playable[pSize++] = i;
                                        System.out.println((pSize) + ": " + players[turnPlayer].getHand().get(i));                                        
                                    }
                                    break;
                                case "Jail":
                                    for(int j = 0; j < nPlayers; j++){
                                        if(players[j].getJail() == null && j != sheriff){
                                            playable[pSize++] = i;
                                            System.out.println((pSize) + ": " + players[turnPlayer].getHand().get(i));                                            
                                            j = nPlayers;
                                        }
                                    }
                                    break;
                                case "Mustang":
                                    if(players[turnPlayer].getMustang() == null){
                                        playable[pSize++] = i;
                                        System.out.println((pSize) + ": " + players[turnPlayer].getHand().get(i));                                        
                                    }
                                    break;
                                case "Dynamite":
                                    if(players[turnPlayer].getDynamite() == null){
                                        playable[pSize++] = i;
                                        System.out.println((pSize) + ": " + players[turnPlayer].getHand().get(i));                                       
                                    }
                                    break;
                                case "Scope":
                                    if(players[turnPlayer].getScope() == null){
                                        playable[pSize++] = i;
                                        System.out.println((pSize) + ": " + players[turnPlayer].getHand().get(i));                                        
                                    }
                                    break;
                                case "Barrel":
                                    if(players[turnPlayer].getBarrel() == null){
                                        playable[pSize++] = i;
                                        System.out.println((pSize) + ": " + players[turnPlayer].getHand().get(i));                                        
                                    }
                                    break;
                                default:
                                    playable[pSize++] = i;
                                    System.out.println((pSize) + ": " + players[turnPlayer].getHand().get(i));                                  
                            }
                        }
                        do{
                            System.out.println(turnCharacter + ": <Choose the card you want to play or 0 to cancel ");
                            choice = input.nextInt() - 1;
                        }while(choice < -1 || choice >= pSize);
                        if(choice != -1){
                            int card = choice;
                            String name = players[turnPlayer].getHand().get(playable[card]).getName();
                            switch (name) {
                                case "Missed!":
                                case "Bang!":
                                    possible = false;
                                    do{
                                        System.out.println(turnCharacter + ": <Choose the player you want to shoot or 0 to cancel");
                                        printActivePlayers(players, nPlayers);
                                        choice = input.nextInt() - 1;
                                        if(choice >= 0 && choice < nPlayers){
                                            possible = players[turnPlayer].getRange() >= distances[choice];
                                            if(!possible)
                                                System.out.println("You don't see him");                                            
                                        }
                                        else if(choice == -1)
                                            possible = true;
                                    }while(!possible);
                                    if(choice != -1){
                                        players[turnPlayer].discard(playable[card], discardPile);
                                        if(bang(players[turnPlayer], players[choice], input, deck, discardPile, nPlayers, false)){
                                            if(!endMatch(players, nPlayers, startingNPlayers, choice))
                                                death(players, nPlayers, turnPlayer, choice, sheriff, sidKetchum, distances, turnPlayer, input, deck, discardPile);
                                        }
                                    }
                                    break;
                                case "Cat Balou":
                                    possible = false;
                                    do{
                                        System.out.println(turnCharacter + ": <Choose the player you want to discard a card from or 0 to cancel ");
                                        choice = input.nextInt() - 1;
                                        if(choice >= 0 && choice < nPlayers){
                                            possible = players[choice].stringActiveCards().length() > 0;
                                            if(choice == turnPlayer)
                                                possible |= players[turnPlayer].getHandSize() > 1;
                                            else
                                                possible |= players[choice].getHandSize() > 0;
                                        }
                                    }while(choice < -1 || choice >= nPlayers);
                                    if(choice == -1)
                                        break;
                                    else if(!possible)
                                        players[turnPlayer].discard(playable[card], discardPile);
                                    else{   
                                        int target = choice;
                                        do{
                                            System.out.println("Choose:\n");
                                            if(players[target].getHandSize() > 1 || (turnPlayer != target && players[target].getHandSize() > 0))
                                                System.out.println("1 if you want to discard a card from his hand or \n");
                                            if(players[target].getBarrel() != null)
                                                System.out.println("2 if you want to discard his Barrel or \n");
                                            if(players[target].getDynamite() != null)
                                                System.out.println("3 if you want to discard his Dynamite or \n");
                                            if(players[target].getJail() != null)
                                                System.out.println("4 if you want to discard his Jail or \n");
                                            if(players[target].getMustang() != null)
                                                System.out.println("5 if you want to discard his Mustang or \n");
                                            if(players[target].getScope() != null)
                                                System.out.println("6 if you want to discard his Scope or \n");
                                            if(players[target].getWeapon() != null)
                                                System.out.println("7 if you want to discard his weapon or \n");
                                            System.out.println("0 if you want to cancel\n");
                                            choice = input.nextInt();
                                            switch(choice){
                                                case 1:
                                                    if(players[target].getHandSize() > 1 || (turnPlayer != target && players[target].getHandSize() > 0)){
                                                        players[turnPlayer].discard(playable[card], discardPile);
                                                        players[target].discard((int) Math.random() * players[target].getHandSize(), discardPile);
                                                    }
                                                    break;
                                                case 2:
                                                    if(players[target].getBarrel() != null){
                                                        players[turnPlayer].discard(playable[card], discardPile);
                                                        players[target].removeBarrel().discard(discardPile);
                                                    }
                                                    break;
                                                case 3:
                                                    if(players[target].getDynamite() != null){
                                                        players[turnPlayer].discard(playable[card], discardPile);
                                                        players[target].removeDynamite().discard(discardPile);
                                                    }
                                                    break;
                                                case 4:
                                                    if(players[target].getJail() != null){
                                                        players[turnPlayer].discard(playable[card], discardPile);
                                                        players[target].removeJail().discard(discardPile);
                                                    }
                                                    break;
                                                case 5:
                                                    if(players[target].getMustang() != null){
                                                        players[turnPlayer].discard(playable[card], discardPile);
                                                        players[target].removeMustang().discard(discardPile);
                                                    }
                                                    break;
                                                case 6:
                                                    if(players[target].getScope() != null){
                                                        players[turnPlayer].discard(playable[card], discardPile);
                                                        players[target].removeScope().discard(discardPile);
                                                    }
                                                    break;
                                                case 7:
                                                    if(players[target].getWeapon() != null){
                                                        players[turnPlayer].discard(playable[card], discardPile);
                                                        players[target].removeWeapon().discard(discardPile);
                                                    }
                                                case 0:
                                                    break;
                                                default:
                                                    possible = false;
                                            }
                                        }while(!possible);
                                    }  
                                    break;
                                case "Panic!":
                                    possible = false;
                                    do{
                                        System.out.println(turnCharacter + ": <Choose the player you want to get a card from or 0 to cancel ");
                                        choice = input.nextInt() - 1;
                                        if(choice >= 0 && choice < nPlayers && distances[choice] <= 1){
                                            possible = players[choice].stringActiveCards().length() > 0;
                                            if(choice == turnPlayer)
                                                possible |= players[turnPlayer].getHandSize() > 1;
                                            else
                                                possible |= players[choice].getHandSize() > 0;
                                        }
                                    }while(choice < -1 || choice >= nPlayers || distances[choice] > 1);
                                    if(choice == -1)
                                        break;
                                    else if(!possible)
                                        players[turnPlayer].discard(playable[card], discardPile);
                                    else{   
                                        int target = choice;
                                        do{
                                            System.out.println("Choose:\n");
                                            if(turnPlayer != target && players[target].getHandSize() > 0)
                                                System.out.println("1 if you want to get a card from his hand or \n");
                                            if(players[target].getBarrel() != null)
                                                System.out.println("2 if you want to get his Barrel or \n");
                                            if(players[target].getDynamite() != null)
                                                System.out.println("3 if you want to get his Dynamite or \n");
                                            if(players[target].getJail() != null)
                                                System.out.println("4 if you want to get his Jail or \n");
                                            if(players[target].getMustang() != null)
                                                System.out.println("5 if you want to get his Mustang or \n");
                                            if(players[target].getScope() != null)
                                                System.out.println("6 if you want to get his Scope or \n");
                                            if(players[target].getWeapon() != null)
                                                System.out.println("7 if you want to get his weapon or \n");
                                            System.out.println("0 if you want to cancel\n");
                                            choice = input.nextInt();
                                            switch(choice){
                                                case 1:
                                                    if(turnPlayer != target && players[target].getHandSize() > 0)
                                                        panicEffect(players[turnPlayer], players[target]);
                                                    break;
                                                case 2:
                                                    if(players[target].getBarrel() != null){
                                                        players[turnPlayer].getHand().add(players[target].removeBarrel());
                                                        players[turnPlayer].increaseHandSize();
                                                    }
                                                    break;
                                                case 3:
                                                    if(players[target].getDynamite() != null){
                                                        players[turnPlayer].getHand().add(players[target].removeDynamite());
                                                        players[turnPlayer].increaseHandSize();
                                                    }
                                                    break;
                                                case 4:
                                                    if(players[target].getJail() != null){
                                                        players[turnPlayer].getHand().add(players[target].removeJail());
                                                        players[turnPlayer].increaseHandSize();
                                                    }
                                                    break;
                                                case 5:
                                                    if(players[target].getMustang() != null){
                                                        players[turnPlayer].getHand().add(players[target].removeMustang());
                                                        players[turnPlayer].increaseHandSize();
                                                    }
                                                    break;
                                                case 6:
                                                    if(players[target].getScope() != null){
                                                        players[turnPlayer].getHand().add(players[target].removeScope());
                                                        players[turnPlayer].increaseHandSize();
                                                    }
                                                    break;
                                                case 7:
                                                    if(players[target].getWeapon() != null){
                                                        players[turnPlayer].getHand().add(players[target].removeWeapon());
                                                        players[turnPlayer].increaseHandSize();
                                                    }
                                                case 0:
                                                    break;
                                                default:
                                                    possible = false;
                                            }
                                            if(possible && choice != 0)
                                                players[turnPlayer].discard(playable[card], discardPile);
                                        }while(!possible);
                                    }
                                    break;
                                case "Wells Fargo":
                                    if(deck.getFirst() == null)
                                        noDeck(discardPile);
                                    players[turnPlayer].draw(deck);
                                case "Stagecoach":
                                    players[turnPlayer].discard(playable[card], discardPile);
                                    for(int i = 0; i < 2; i++){
                                        if(deck.getFirst() == null)
                                            noDeck(discardPile);
                                        players[turnPlayer].draw(deck);
                                    }
                                    break;
                                case "Scope":
                                    players[turnPlayer].setScope(players[turnPlayer].getHand().remove(playable[card]));
                                    players[turnPlayer].decreaseHandSize();
                                    break;
                                case "Barrel":
                                    players[turnPlayer].setBarrel(players[turnPlayer].getHand().remove(playable[card]));
                                    players[turnPlayer].decreaseHandSize();
                                    break;
                                case "Dynamite":
                                    players[turnPlayer].setDynamite(players[turnPlayer].getHand().remove(playable[card]));
                                    players[turnPlayer].decreaseHandSize();
                                    break;
                                case "Mustang":
                                    players[turnPlayer].setMustang(players[turnPlayer].getHand().remove(playable[card]));
                                    players[turnPlayer].decreaseHandSize();
                                    break;
                                case "Jail":
                                    do{
                                        System.out.println(turnCharacter + " <Choose the player you want to put in jail or 0 to cancel ");
                                        choice = input.nextInt() - 1;
                                        if(choice == sheriff)
                                            System.out.println("Impossible to put the Sheriff in jail");                                        
                                    }while(choice < -1 || choice >= nPlayers || choice == sheriff || players[choice].getDynamite() != null);
                                    if(choice != -1){
                                        players[choice].setDynamite(players[turnPlayer].getHand().remove(playable[card]));
                                        players[turnPlayer].decreaseHandSize();
                                    }
                                    break;
                                case "Saloon":
                                    players[turnPlayer].discard(playable[card], discardPile);
                                    for(int i = 0; i < nPlayers; i++)
                                        players[i].addLife();
                                    break;
                                case "Beer":
                                    players[turnPlayer].discard(playable[card], discardPile);
                                    if(nPlayers > 2)
                                        players[turnPlayer].addLife();
                                    break;
                                case "Gatling":
                                    players[turnPlayer].discard(playable[card], discardPile);
                                    for(int i = turnPlayer+1; i != turnPlayer; i++){
                                        if(i == nPlayers)
                                            i = 0;
                                        if(bang(players[turnPlayer], players[i], input, deck, discardPile, nPlayers, true)){
                                            if(!endMatch(players, nPlayers, startingNPlayers, i))
                                                death(players, nPlayers, turnPlayer, i, sheriff, sidKetchum, distances, turnPlayer, input, deck, discardPile);
                                        }
                                    }
                                    break;
                                case "Duel":
                                    do{
                                        System.out.println(turnCharacter + " <Choose a player you want to duel or 0 to cancel ");
                                        choice = input.nextInt() - 1;
                                    }while(choice < -1 || choice >= nPlayers || choice == turnPlayer);
                                    if(choice != -1){
                                        players[turnPlayer].discard(playable[card], discardPile);
                                        int res = duel(players[turnPlayer], players[choice], input, deck, discardPile, nPlayers);
                                        if(res == 1){
                                            if(!endMatch(players, nPlayers, startingNPlayers, turnPlayer)){
                                                death(players, nPlayers, turnPlayer, turnPlayer, sheriff, sidKetchum, distances, turnPlayer, input, deck, discardPile);
                                                if(turnPlayer == nPlayers)
                                                    turnPlayer = 0;
                                                else
                                                    turnPlayer++;
                                                continue skipTurn;
                                            }   
                                        }
                                        else if(res == 2)
                                            if(!endMatch(players, nPlayers, startingNPlayers, choice))
                                                death(players, nPlayers, turnPlayer, choice, sheriff, sidKetchum, distances, turnPlayer, input, deck, discardPile);
                                        }
                                    break;
                                case "Indians!":
                                    players[turnPlayer].discard(playable[card], discardPile);
                                    for(int i = turnPlayer + 1; i != turnPlayer; i++){
                                        if(i == nPlayers)
                                            i = 0;
                                        if(indians(players[turnPlayer], players[i], discardPile, deck, input, nPlayers))
                                            if(!endMatch(players, nPlayers, startingNPlayers, i))
                                                death(players, nPlayers, turnPlayer, i, sheriff, sidKetchum, distances, turnPlayer, input, deck, discardPile);
                                    }
                                    break;
                                case "General Store":
                                    players[turnPlayer].discard(playable[card], discardPile);
                                    ArrayList<PlayingCard> generalStore = new ArrayList<PlayingCard>(nPlayers);
                                    int storeSize = nPlayers;
                                    for(int i = 0; i < nPlayers; i++){
                                        if(deck.getFirst() == null)
                                            deck = noDeck(discardPile);
                                        generalStore.add(deck.removeFirst());
                                    }
                                    for(int i = turnPlayer; i != turnPlayer; i++){
                                        if(i == nPlayers)
                                            i = 0;
                                        players[i].readHand();
                                        do{
                                            for(int j = 0; j < storeSize; j++)
                                                System.out.println((j+1) + ") " + generalStore.get(j));
                                            choice = input.nextInt() - 1;
                                        }while(choice < 0 || choice >= storeSize);
                                        players[i].getHand().add(generalStore.get(choice));
                                        players[i].increaseHandSize();
                                        storeSize--;
                                    }
                                    break;
                                default:
                                    if(players[turnPlayer].getWeapon() != null)
                                        players[turnPlayer].removeWeapon().discard(discardPile);
                                    players[turnPlayer].setWeapon(new Weapon(players[turnPlayer].getHand().remove(playable[card])));
                                    break;
                            }
                        }
                        break;
                    case 10:
                        goon = false;
                        break;
                    default:
                        System.out.println("Unavailable option.\n");
                }
            }
            while(players[turnPlayer].getHandSize() > players[turnPlayer].getLifes()){
                System.out.println(turnCharacter + "has to discard cards until those are the same number of his lifes.");
                players[turnPlayer].readHand();
                do{
                    System.out.println(turnCharacter + " <Choose a card to discard");
                    choice = input.nextInt() - 1;
                }while(choice < 0 || choice >= players[turnPlayer].getHandSize());
                players[turnPlayer].discard(choice, discardPile);
            }
            if(turnPlayer == nPlayers - 1)
                turnPlayer = 0;
            else
                turnPlayer++;
        }
    }

    private static int findSheriff(String[] roles, int nPlayers, String[] names){
        for(int i = 0; i < nPlayers; i++)
            if(roles[i] == "Sheriff"){
                int sheriff = i;
                System.out.println(names[i] + " is the Sheriff");
                return sheriff;
            }
        return nPlayers;
    }

    private static int findSheriff(Player[] players, int nPlayers){
        for(int i = 0; i < nPlayers; i++)
            if(players[i].getRole() == "Sheriff"){
                int sheriff = i;
                System.out.println(players[i].toString() + " is the Sheriff");
                return sheriff;
            }
        return nPlayers;
    }

    private static int findCharacter(Player[] players, String name, int nPlayers){
        for(int i = 0; i < nPlayers; i++)
            if(players[i].toString() == name)
                return i;
        return -1;
    }

    private static Characters[] createCharacters(){ 
        Characters[] characters = new Characters[16];
        characters[0] = new Characters("Jesse Jones", (short) 4, "He may draw his first card from the hand of a player.");
        characters[1] = new Characters("Black Jack", (short) 4, "He shows the second card he draws. On heart or Diamonds, he draws one more card.");
        characters[2] = new Characters("Rose Doolan", (short) 4, "She sees all players at a distance decreased by 1.");
        characters[3] = new Characters("El Gringo", (short) 3, "Each time he is hit by a player, he draws a card from the hand of that player.");
        characters[4] = new Characters("Bart Cassidy", (short) 4, "Each time he is hit, he draws a card.");
        characters[5] = new Characters("Lucky Duke", (short) 4, "Each time he \"draws!\", he flips the top two cards and chooses one.");
        characters[6] = new Characters("Sid Ketchum", (short) 4, "He may discard 2 cards to regain one life point.");
        characters[7] = new Characters("Suzy Lafayette", (short) 4, "As soon as she has no cards in hand, she draws a card.");
        characters[8] = new Characters("Vulture Sam", (short) 4, "Whenever a player is eliminated from play, he takes in hand all the cards of that player.");
        characters[9] = new Characters("Kit Carlson", (short) 4, "He looks at the top three cards of the deck and chooses the 2 to draw.");
        characters[10] = new Characters("Willy The Kid", (short) 4, "He can play any number of BANG! cards.");
        characters[11] = new Characters("Slab The Killer", (short) 4, "Player needs 2 Missed! cards to cancel his BANG! card.");
        characters[12] = new Characters("Pedro Ramirez", (short) 4, "He may draw his first card from the discard pile.");
        characters[13] = new Characters("Jourdonnais", (short) 4, "Whenever he is the target of a BANG!, he may \"draw\": on a Heart, he is missed.");
        characters[14] = new Characters("Calamity Janet", (short) 4, "She can play BANG! cards as Missed! and vice versa.");
        characters[15] = new Characters("Paul Regret", (short) 3, "All players see him at a distance increased by 1.");
        return characters;
    }

    private static String[] drawRoles(int nPlayers){
        String[] availableRoles = {"Sheriff", "Renegade", "Outlaw", "Outlaw", "Deputy", "Outlaw", "Deputy"};
        String[] roles = new String[nPlayers];
        int n = nPlayers - 1;
        for(int i = 0; i < n; i++){
            int pick = (int) (Math.random() * (nPlayers - i));
            roles[i] = availableRoles[pick];
            availableRoles[pick] = availableRoles[n - i];
        }
        roles[n] = availableRoles[0];
        return roles;
    }

    private static Characters[] drawCharacters(Characters[] availableCharacters, int nPlayers, String[] names){
        Characters[] characters = new Characters[nPlayers];
        int n = availableCharacters.length - 1;
        for(int i = 0; i < nPlayers; i++){
            int pick = (int) (Math.random() * (availableCharacters.length - i));
            characters[i] = availableCharacters[pick];
            availableCharacters[pick] = availableCharacters[n - i];
            System.out.println(names[i] + " is " + characters[i]);
        }
        return characters;
    }

    private static PlayingCard[] createCards(){
        PlayingCard[] playingCards = new PlayingCard[80];
        playingCards[0] = new PlayingCard("Cat Balou", 'D', (short) 11);
        playingCards[1] = new PlayingCard("Rev. Carabine", 'C', (short) 1);
        playingCards[2] = new PlayingCard("Bang!", 'C', (short) 7);
        playingCards[3] = new PlayingCard("Jail", 'S', (short) 11);
        playingCards[4] = new PlayingCard("Duel", 'S', (short) 11);
        playingCards[5] = new PlayingCard("Mustang", 'H', (short) 9);
        playingCards[6] = new PlayingCard("Barrel", 'S', (short) 12);
        playingCards[7] = new PlayingCard("Jail", 'S', (short) 10);
        playingCards[8] = new PlayingCard("Scope", 'S', (short) 1);
        playingCards[9] = new PlayingCard("Dynamite", 'H', (short) 2);
        playingCards[10] = new PlayingCard("Bang!", 'D', (short) 7);
        playingCards[11] = new PlayingCard("Remington", 'C', (short) 13);
        playingCards[12] = new PlayingCard("Wells Fargo", 'H', (short) 3);
        playingCards[13] = new PlayingCard("Beer", 'H', (short) 10);
        playingCards[14] = new PlayingCard("Bang!", 'C', (short) 6);
        playingCards[15] = new PlayingCard("Bang!", 'C', (short) 5);
        playingCards[16] = new PlayingCard("Beer", 'H', (short) 7);
        playingCards[17] = new PlayingCard("Missed!", 'S', (short) 4);
        playingCards[18] = new PlayingCard("Bang!", 'D', (short) 13);
        playingCards[19] = new PlayingCard("Bang!", 'D', (short) 12);
        playingCards[20] = new PlayingCard("Missed!", 'C', (short) 11);
        playingCards[21] = new PlayingCard("Winchester", 'S', (short) 8);
        playingCards[22] = new PlayingCard("Stagecoach", 'S', (short) 9);
        playingCards[23] = new PlayingCard("Beer", 'H', (short) 6);
        playingCards[24] = new PlayingCard("Bang!", 'D', (short) 4);
        playingCards[25] = new PlayingCard("Gatling", 'H', (short) 10);
        playingCards[26] = new PlayingCard("Panic!", 'D', (short) 8);
        playingCards[27] = new PlayingCard("Bang!", 'H', (short) 13);
        playingCards[28] = new PlayingCard("Panic!", 'H', (short) 12);
        playingCards[29] = new PlayingCard("Jail", 'H', (short) 4);
        playingCards[30] = new PlayingCard("Missed!", 'C', (short) 10);
        playingCards[31] = new PlayingCard("Volcanic", 'C', (short) 10);
        playingCards[32] = new PlayingCard("Bang!", 'C', (short) 4);
        playingCards[33] = new PlayingCard("Missed!", 'C', (short) 1);
        playingCards[34] = new PlayingCard("General Store", 'C', (short) 9);
        playingCards[35] = new PlayingCard("Indians!", 'D', (short) 13);
        playingCards[36] = new PlayingCard("Bang!", 'C', (short) 2);
        playingCards[37] = new PlayingCard("Bang!", 'D', (short) 6);
        playingCards[38] = new PlayingCard("Bang!", 'D', (short) 10);
        playingCards[39] = new PlayingCard("Bang!", 'D', (short) 3);
        playingCards[40] = new PlayingCard("Cat Balou", 'D', (short) 10);
        playingCards[41] = new PlayingCard("Schofield", 'S', (short) 13);
        playingCards[42] = new PlayingCard("Bang!", 'D', (short) 5);
        playingCards[43] = new PlayingCard("Beer", 'H', (short) 8);
        playingCards[44] = new PlayingCard("Beer", 'H', (short) 11);
        playingCards[45] = new PlayingCard("Bang!", 'C', (short) 3);
        playingCards[46] = new PlayingCard("Panic!", 'H', (short) 11);
        playingCards[47] = new PlayingCard("Bang!", 'H', (short) 12);
        playingCards[48] = new PlayingCard("Missed!", 'S', (short) 2);
        playingCards[49] = new PlayingCard("Saloon", 'H', (short) 5);
        playingCards[50] = new PlayingCard("Bang", 'D', (short) 2);
        playingCards[51] = new PlayingCard("Schofield", 'C', (short) 12);
        playingCards[52] = new PlayingCard("Bang!", 'C', (short) 9);
        playingCards[53] = new PlayingCard("Bang!", 'S', (short) 1);
        playingCards[54] = new PlayingCard("Bang!", 'H', (short) 1);
        playingCards[55] = new PlayingCard("Stagecoach", 'S', (short) 9);
        playingCards[56] = new PlayingCard("Bang!", 'D', (short) 8);
        playingCards[57] = new PlayingCard("Bang!", 'D', (short) 11);
        playingCards[58] = new PlayingCard("Beer", 'H', (short) 9);
        playingCards[59] = new PlayingCard("Duel", 'D', (short) 12);
        playingCards[60] = new PlayingCard("Duel", 'C', (short) 8);
        playingCards[61] = new PlayingCard("Schofield", 'C', (short) 11);
        playingCards[62] = new PlayingCard("Panic!", 'H', (short) 1);
        playingCards[63] = new PlayingCard("Indians!", 'D', (short) 1);
        playingCards[64] = new PlayingCard("Mustang", 'H', (short) 8);
        playingCards[65] = new PlayingCard("Cat Balou", 'D', (short) 9);
        playingCards[66] = new PlayingCard("General Store", 'S', (short) 12);
        playingCards[67] = new PlayingCard("Bang!", 'D', (short) 1);
        playingCards[68] = new PlayingCard("Missed!", 'S', (short) 7);
        playingCards[69] = new PlayingCard("Missed!", 'S', (short) 3);
        playingCards[70] = new PlayingCard("Missed!", 'S', (short) 8);
        playingCards[71] = new PlayingCard("Volcanic", 'S', (short) 10);
        playingCards[72] = new PlayingCard("Missed!", 'C', (short) 12);
        playingCards[73] = new PlayingCard("Cat Balou", 'H', (short) 13);
        playingCards[74] = new PlayingCard("Bang!", 'C', (short) 8);
        playingCards[75] = new PlayingCard("Bang!", 'D', (short) 9);
        playingCards[76] = new PlayingCard("Missed!", 'S', (short) 6);
        playingCards[77] = new PlayingCard("Missed!", 'S', (short) 5);
        playingCards[78] = new PlayingCard("Barrel", 'S', (short) 13);
        playingCards[79] = new PlayingCard("Missed!", 'C', (short) 13);
        return playingCards;
    }
    
    private static LinkedList<PlayingCard> shuffle(PlayingCard[] playingCards){
        LinkedList<PlayingCard> deck = new LinkedList<PlayingCard>();
        int n = playingCards.length - 1;
        for(int i = 0; i < n; i++){
            int pick = (int) (Math.random() * (playingCards.length - i));
            deck.addFirst(playingCards[pick]);
            playingCards[pick] = playingCards[n - i];
        }
        deck.addFirst(playingCards[0]);
        return deck;
    }
    
    public static LinkedList<PlayingCard> noDeck(LinkedList<PlayingCard> discardPile){
        Object[] objects = discardPile.toArray();
        PlayingCard[] playingCards = new PlayingCard[objects.length];
        for(int i = 0; i < objects.length; i++)
            playingCards[i] = (PlayingCard) objects[i];
        discardPile.clear();
        return shuffle(playingCards);
    }

    private static void readDiscard(PlayingCard card){
        System.out.println("First card in the discard pile: " + card);
    }

    private static boolean sidKetchum(Player sid, LinkedList<PlayingCard> discardPile, Scanner input){
        boolean c, ret = false;
        do{
            c = sid.sidKetchum(discardPile, input);
            ret |= c;
        }while(c);
        return ret;
    }

    private static void printLifes(Player[] players, int choice, int nPlayers){
        if(choice == 0)
            for(int i = 0; i < nPlayers; i++)
                System.out.println(players[i].toString() + "'s lifes: " + players[i].getLifes() + "/" + players[i].getStartingLifes());
        else
            System.out.println(players[choice-1].toString() + "'s lifes: " + players[choice-1].getLifes() + "/" + players[choice-1].getStartingLifes());
    }

    private static void printCharacters(Player[] players, int choice, int nPlayers){
        if(choice == 0)
            for(int i = 0; i < nPlayers; i++)
                System.out.println(players[i].getName() + "'s character: " + players[i].getCharacter());
        else
            System.out.println(players[choice-1].getName() + "'s character: " + players[choice-1].getCharacter());
    }

    private static void printNHands(Player[] players, int choice, int nPlayers){
        if(choice == 0)
            for(int i = 0; i < nPlayers; i++)
                System.out.println(players[i].toString() + " has " + players[i].getHandSize() + " in his hands");
        else
            System.out.println(players[choice-1].toString() + "has " + players[choice-1].getHandSize() + " in his hands");
    }

    private static void printActiveCards(Player[] players, int choice, int nPlayers){
        if(choice == 0)
            for(int i = 0; i < nPlayers; i++)
                if(players[i].stringActiveCards() != null)
                    System.out.println(players[i].toString() + " active cards:\n" + players[i].stringActiveCards());
                else
                    System.out.println(players[i].toString() + " has no active cards");
        else
            if(players[choice].stringActiveCards() != null)
                System.out.println(players[choice-1].toString() + " active cards:\n" + players[choice-1].stringActiveCards());
            else
                System.out.println(players[choice-1].toString() + " has no active cards");
    }

    private static int[] distances(Player[] players, int nPlayers, int from){
        int[] distances = new int[nPlayers];
        distances[from] = 0;
        for(int i = 0; i < nPlayers; i++)
            if(i != from){
                distances[i] = Math.min(Math.abs(from - i), Math.abs(from - i + nPlayers));
                if(players[i].toString() == "Paul Regret")
                    distances[i]++;
                if(players[i].getMustang() != null)
                    distances[i]++;
                if(players[from].toString() == "Rose Doolan")
                    distances[i]--;
                if(players[from].getScope() != null)
                    distances[i]--;
            }
        return distances;
    }
    
    private static void printDistances(int[] distances, int choice, int nPlayers, int player, Player[] players){
        if(choice == 0){
            for(int i = 0; i < nPlayers; i++)
                if(i != player)
                    System.out.println(players[i].toString() + "'s distance from you:\n" + distances[i]);
        }
        else
            System.out.println(players[choice-1].toString() + "'s distance from you:\n" + distances[choice-1]);
    }

    private static boolean bang(Player shooter, Player defender, Scanner input, LinkedList<PlayingCard> deck, LinkedList<PlayingCard> discardPile, int nPlayers, boolean gatling){
        int shot = 1, choice;
        if(shooter.toString() == "Slab The Killer" && !gatling){
            System.out.println(defender + " will need 2 \"Missed!\" for this Bang!");
            shot++;
        }
        if(defender.getBarrel() != null){
            do{
                System.out.println(defender + " <Choose 1 to activate barrel or 0 to ignore ");
                choice = input.nextInt();
            }while(choice < 0 || choice > 1);
            if(choice == 1){
                boolean res;
                if(defender.toString() == "Lucky Duke")
                    res = luckyLuke(deck, discardPile, input);
                else
                    res = drawHearts(deck, discardPile);
                if(res){
                    shot--;
                    System.out.println("Missed!");
                }
            }
        }
        if(shot > 0 && defender.toString() == "Jourdonnais"){
            do{
                System.out.println(defender + " <Choose 1 to activate your character's ability or 0 to ignore ");
                choice = input.nextInt();
            }while(choice < 0 || choice > 1);
            if(choice == 1)
                if(drawHearts(deck, discardPile)){
                    shot--;
                    System.out.println("Missed!");
                }
        }        
        for(int i = 0; i < shot; i++){
            defender.readHand();
            boolean miss = false;
            do{
                System.out.println(defender + " <Choose a \"Missed\" or 0 to lose a life ");
                choice = input.nextInt() - 1;
                if(choice >= 0 && choice < defender.getHandSize()){
                    miss = defender.getHand().get(choice).getName() == "Missed!";
                    if(defender.toString() == "Calamity Janet")
                        miss |= defender.getHand().get(choice).getName() == "Bang!";
                }
                if(choice == -1)
                    miss = true;
            }while(!miss);
            if(choice == -1)
                if(subLife(defender, shooter, nPlayers, input, discardPile, deck))
                    return true;
            else{
                defender.discard(choice, discardPile);
                System.out.println("Missed!");
            }
        }
        return false;
    }

    private static int duel(Player attacker, Player target, Scanner input, LinkedList<PlayingCard> deck, LinkedList<PlayingCard> discardPile, int nPlayers){
        Player turnPlayer = target;
        int choice;
        while(true){
            boolean bang = false;
            do{
                turnPlayer.readHand();
                System.out.println(turnPlayer + " <Choose a \"Bang!\" to go on with the duel or 0 to lose a life ");
                choice = input.nextInt() - 1;
                if(choice >= 0 && choice < turnPlayer.getHandSize()){
                    bang = turnPlayer.getHand().get(choice).getName() == "Bang!";
                    if(turnPlayer.toString() == "Calamity Janet")
                        bang |= turnPlayer.getHand().get(choice).getName() == "Missed!";
                }
                if(choice == -1)
                    bang = true;
            }while(!bang);
            if(choice == -1){
                if(subLife(turnPlayer, attacker, nPlayers, input, discardPile, deck)){
                    if(turnPlayer == attacker)
                        return 1;
                    else
                        return 2;
                }
                else
                    return 0;
            }
            turnPlayer.discard(choice, discardPile);
            if(turnPlayer == target)
                turnPlayer = attacker;
            else
                turnPlayer = target;
        }
    }
    
    private static boolean luckyLuke(LinkedList<PlayingCard> deck, LinkedList<PlayingCard> discardPile, Scanner input){
        PlayingCard[] tmp = new PlayingCard[2];
        int choice1, choice2;
        for(int i = 0; i < 2; i++){
            if(deck.getFirst() == null)
                deck = noDeck(discardPile);
            tmp[i] = deck.removeFirst();
            System.out.println((i+1) + ") " + tmp[i]);
        }
        do{
            System.out.println("Lucky Duke: <Choose which one you want to use ");
            choice1 = input.nextInt() - 1;
        }while (choice1 < 0 || choice1 > 1);
        do{
            System.out.println("Lucky Duke: <Choose which one you want to discard before ");
            choice2= input.nextInt() - 1;
        }while (choice2 < 0 || choice2 > 1);
        if(choice2 == 0){
            tmp[0].discard(discardPile);
            tmp[1].discard(discardPile);
        }
        else{
            tmp[1].discard(discardPile);
            tmp[0].discard(discardPile);
        }
        if(tmp[choice1].getSuit() == 'H')
            return true;
        return false;
    }   
    
    private static boolean drawHearts(LinkedList<PlayingCard> deck, LinkedList<PlayingCard> discardPile){
        if(deck.getFirst() == null)
            deck = noDeck(discardPile);
        deck.removeFirst().discard(discardPile);
        System.out.println(discardPile.getFirst() + " drawn for effect");
        if(discardPile.getFirst().getSuit() == 'H')
            return true;
        return false;
    }

    private static boolean subLife(Player wounded, Player attacker, int nPlayers, Scanner input, LinkedList<PlayingCard> discardPile, LinkedList<PlayingCard> deck){
        wounded.subLife();
        if(wounded.toString() == "El Gringo" && attacker != null && attacker.getHandSize() > 0)
            panicEffect(wounded, attacker);
        else if(wounded.toString() == "Bart Cassidy"){
            if(deck.getFirst() == null)
                deck = noDeck(discardPile);
            wounded.draw(deck);
        }
        if(wounded.getLifes() == 0 && nPlayers > 2){
            int[] beers = new int[wounded.getHandSize()];
            int bSize = 0;
            for(int i = 0; i < wounded.getHandSize(); i++)
                if(wounded.getHand().get(i).getName() == "Beer")
                    beers[bSize++] = i;
            if(bSize > 0){
                int choice;
                do{
                    System.out.println(wounded + " (Your beers:");
                    for(int i = 0; i < bSize; i++)
                        System.out.println((i+1) + ") " + wounded.getHand().get(beers[i]));
                    System.out.println(wounded + " <Choose a beer or 0 to ignore\n");
                    choice = input.nextInt() - 1;
                }while(choice < 0 || choice > bSize);
                if(choice != 0){
                    wounded.discard(beers[choice], discardPile);
                    wounded.addLife();
                    System.out.println(discardPile.getFirst() + " used and life gained");
                }
            }
        }
        if(wounded.getLifes() == 0)
            return true;
        return false;
    }

    private static void death(Player[] players, int nPlayers, int killer, int killed, int sheriff, int sidKetchum, int[] distances, int turnPlayer, Scanner input, LinkedList<PlayingCard> deck, LinkedList<PlayingCard> discardPile){
        if(sheriff == nPlayers - 1)
            sheriff = killed;
        if(killed == sidKetchum)
            sidKetchum = -1;
        else if(sidKetchum == nPlayers - 1)
            sidKetchum = killed;
        int vulture = findCharacter(players, "Vulture Sam", nPlayers);
        System.out.println(players[killed] + " was a " + players[killed].getRole());
        if(vulture >= 0 && vulture != killed){
            while (players[killed].getHandSize() > 0){
                players[vulture].getHand().add(players[killed].getHand().removeFirst());
                players[vulture].increaseHandSize();
                players[killed].decreaseHandSize();
            }
            for(int i = 0; i < 5;i++)
                if(players[killed].getActiveCards()[i] != null){
                    players[vulture].getHand().add(players[killed].getActiveCards()[i]);
                    players[vulture].increaseHandSize();
                }
            if(players[killed].getWeapon() != null){
                players[vulture].getHand().add(players[killed].getWeapon());
                players[vulture].increaseHandSize();
            }
        }
        else
            players[killed].discardAll(input, discardPile);
        if(killer >= 0){
            if(players[killer].getRole() == "Sheriff" && players[killer].getRole() == "Deputy")
                players[killer].discardAll(input, discardPile);
            else if(players[killed].getRole() == "Outlaw"){
                for(int i = 0; i < 3; i++){
                    if(deck.getFirst() == null)
                        deck = noDeck(discardPile);
                    players[killer].draw(deck);
                }
            }
        }
        for(int i = killed+1; i != nPlayers; i++){
            Player tmp = players[i];
            players[i] = players[i-1];
            players[i-1] = tmp;
        }
        nPlayers--;
        sheriff = findSheriff(players, nPlayers);
        sidKetchum = findCharacter(players, "Sid Ketchum", nPlayers);
        if(turnPlayer != killed)
            distances = distances(players, nPlayers, turnPlayer);
    }

    private static void panicEffect(Player robber, Player robbed){
        PlayingCard rand = robbed.getHand().remove((int) Math.random() * robbed.getHandSize());
        System.out.println(robber.toString() + "(Drawn: " + rand);
        System.out.println(robbed.toString() + "(" + robber.toString() + " robbed your " + rand);
        robber.getHand().add(rand);
        robber.increaseHandSize();
        robbed.decreaseHandSize();
    }

    private static void printActivePlayers(Player[] players, int nPlayers){
        for(int i = 0; i < nPlayers; i++)
            System.out.println((i+1) + ") " + players[i]);
        System.out.print("\n");
    }

    private static boolean endMatch(Player[] players, int nPlayers, int startingNPlayers, int dead){
        if(players[dead].getRole() == "Sheriff"){
            int renegade = 0;
            for(int i = 0; i < nPlayers; i++){
                if(i != dead){
                    if(players[i].getRole() != "Renegade"){
                        outlawsWin(players, startingNPlayers);
                        return true;
                    }
                    else
                        i = renegade;
                }
            }
            System.out.println(players[renegade] + "(" + players[renegade].getName() + ") has won the match as a Renegade");
            return true;
        }
        for(int i = 0; i < nPlayers; i++){
            if(i != dead){
                if(players[i].getRole() != "Sheriff" && players[i].getRole() != "Deputy")
                    return false;
            }
        }
        sheriffWin(players, startingNPlayers);
        return false;
    }

    private static void outlawsWin(Player[] players, int startingNPlayers){
        System.out.println("Outlaws have won the match\nWinners:");
        for(int i = 0; i < startingNPlayers; i++)
            if(players[i].getRole() == "Outlaw")
                System.out.println(players[i] + "(" + players[i].getName() + ") has won the match as a Outlaw");
    }

    private static void sheriffWin(Player[] players, int startingNPlayers){
        System.out.println("Sheriff and his Deputies has won the match\nWinners:");
        for(int i = 0; i < startingNPlayers; i++)
            if(players[i].getRole() == "Sheriff" || players[i].getRole() == "Deputy")
            System.out.println(players[i] + "(" + players[i].getName() + ") has won the match as a " + players[i].getRole());
    }

    private static boolean dynamiteEffect(Player[] players, int target, int nPlayers, LinkedList<PlayingCard> deck, LinkedList<PlayingCard> discardPile, Scanner input){
        if(deck.getFirst() == null)
            deck = noDeck(discardPile);
        deck.removeFirst().discard(discardPile);
        System.out.println(discardPile.getFirst() + " drawn for effect");
        if(discardPile.getFirst().getSuit() == 'S' && discardPile.getFirst().getRank() >= 2 && discardPile.getFirst().getRank() <= 9)
            return explosion(players[target], nPlayers, deck, discardPile, input);
        for(int i = target+1; i != target; i++){
            if(i == nPlayers)
                i = 0;
            if(players[i].getDynamite() == null)
                players[i].setDynamite(players[target].removeDynamite());
        }
        return false;
    }

    private static boolean luckyDynamite(Player[] players, int target, int nPlayers, LinkedList<PlayingCard> deck, LinkedList<PlayingCard> discardPile, Scanner input){
        PlayingCard[] tmp = new PlayingCard[2];
        int choice1, choice2;
        for(int i = 0; i < 2; i++){
            if(deck.getFirst() == null)
                deck = noDeck(discardPile);
            tmp[i] = deck.removeFirst();
            System.out.println((i+1) + ") " + tmp[i]);
        }
        do{
            System.out.println("Lucky Duke: <Choose which one you want to use ");
            choice1 = input.nextInt() - 1;
        }while (choice1 < 0 || choice1 > 1);
        do{
            System.out.println("Lucky Duke: <Choose which one you want to discard before ");
            choice2= input.nextInt() - 1;
        }while (choice2 < 0 || choice2 > 1);
        if(choice2 == 0){
            tmp[0].discard(discardPile);
            tmp[1].discard(discardPile);
        }
        else{
            tmp[1].discard(discardPile);
            tmp[0].discard(discardPile);
        }
        if(tmp[choice1].getSuit() == 'S' && tmp[choice1].getRank() >= 2 && tmp[choice1].getRank() <= 9)
            return explosion(players[target], nPlayers,  deck, discardPile, input);
        for(int i = target+1; i != target; i++){
            if(i == nPlayers)
                i = 0;
            if(players[i].getDynamite() == null)
                players[i].setDynamite(players[target].removeDynamite());
        }
        return false;
    }

    private static boolean explosion(Player damaged, int nPlayers, LinkedList<PlayingCard> deck, LinkedList<PlayingCard> discardPile, Scanner input){
        damaged.removeDynamite().discard(discardPile);
        System.out.println("Dynamite exploded: " + damaged + " is going to lose 3 lifes");
        for(int i = 0; i < 3; i++)
            if(subLife(damaged, null, nPlayers, input, discardPile, deck))
                return true;
        return false;
    }

    private static boolean indians(Player attacker, Player target, LinkedList<PlayingCard> discardPile, LinkedList<PlayingCard> deck, Scanner input, int nPlayers){
        boolean bang = false;
        int choice;
        do{
            target.readHand();
            System.out.println(target + " <Choose a \"Bang!\" or 0 to lose a life ");
            choice = input.nextInt() - 1;
            if(choice >= 0 && choice < target.getHandSize()){
                bang = target.getHand().get(choice).getName() == "Bang!";
                if(target.toString() == "Calamity Janet")
                    bang |= target.getHand().get(choice).getName() == "Missed!";
            }
            if(choice == -1)
                bang = true;
        }while(!bang);
        if(choice == -1)
            return subLife(target, attacker, nPlayers, input, discardPile, deck);
        target.discard(choice, discardPile);
        return false;
    }
}
