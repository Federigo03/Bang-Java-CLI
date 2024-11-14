package match;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import cards.*;
import characters.Characters;
import player.Player;

public class Match {
    private int nPlayers;
    final private Player[] players;
    private LinkedList<PlayingCard> deck;
    private LinkedList<PlayingCard> discardPile;
    private int sheriffIx;
    private int sidKetchumIx;
    private Scanner input;
    private int turnPlayerIx;
    private Player turnPlayer;
    private int distances[];

    public Match(int nStartPlayers){
        final int nStartingPlayers = nStartPlayers;
        if(nStartingPlayers > 7 || nStartingPlayers < 4){
            System.err.println("Invalid number of players");
            System.exit(-1);
        } 
        nPlayers = nStartingPlayers;
        final String[] names = new String[nPlayers];
        for(int i = 0; i < nPlayers; i++)
            names[i] = "Player " + (i+1);
        final String[] roles = drawRoles();
        final Characters[] characters = drawCharacters(createCharacters(), names);
        deck = shuffle(createCards());
        discardPile = new LinkedList<PlayingCard>();
        players = new Player[nPlayers];
        for(int i = 0; i < nPlayers; i++){
            players[i] = new Player(roles[i], characters[i], names[i], deck);
            players[i].readRole();
        }
        sheriffIx = findSheriff(roles, names);
        turnPlayerIx = sheriffIx;
        sidKetchumIx = findCharacter("Sid Ketchum");        
        input = new Scanner(System.in);
        skipTurn:
        while (true) {
            turnPlayer = players[turnPlayerIx];
            String turnCharacter = turnPlayer.getCharacter().getName();
            System.out.println(turnCharacter + "'s turn:");
            System.out.println("Cards remaining: " + deck.size());
            turnPlayer.readHand();
            int choice, drawn = 0;
            if(sidKetchumIx >= 0 && players[sidKetchumIx].getHand().size() >= 2)
                sidKetchum();
            if(turnPlayer.isActiveCard("Dynamite")){
                if(dynamiteEffect()){
                    endMatch(nStartingPlayers, turnPlayerIx);
                    death(-1, turnPlayerIx);
                    continue skipTurn;
                }
            }
            if(turnPlayer.isActiveCard("Jail")){
                boolean res;
                if(turnCharacter == "Lucky Duke")
                    res = luckyDuke();
                else
                    res = drawHearts();
                turnPlayer.removeActiveCard("Jail").discard(discardPile);
                if(!res){
                    if(turnPlayerIx == nPlayers - 1)
                        turnPlayerIx = 0;
                    else
                        turnPlayerIx++;
                    continue skipTurn;
                }
            }
            switch(turnCharacter){
                case "Jesse Jones":
                    do{
                        System.out.println(turnCharacter + ": <Choose 1 to activate your character's ability or 0 to ignore ");
                        choice = input.nextInt();
                    }while(choice < 0 || choice > 1);
                    if(sidKetchumIx >= 0 && players[sidKetchumIx].getHand().size() >= 2)
                        sidKetchum();
                    if(choice == 1){
                        do{
                            System.out.println(turnCharacter + ": <Choose the player you want to draw from or 0 to cancel ");
                            printNHands(0);
                            choice = input.nextInt() - 1;
                        }while(choice < -1 || choice >= nPlayers);
                        if(sidKetchumIx >= 0 && players[sidKetchumIx].getHand().size() >= 2)
                            sidKetchum();
                        if(choice == -1)
                            break;
                        if(choice != turnPlayerIx && !players[choice].getHand().isEmpty())
                            panicEffect(turnPlayerIx, choice);
                        else
                            System.out.println(turnCharacter + ": Ability unsuccessfull: no card to draw from his hand. You can't draw it from the deck either.");
                        drawn++;
                    }
                    break;
                case "Kit Carlson":
                    PlayingCard[] t = new PlayingCard[3];
                    if(sidKetchumIx >= 0 && players[sidKetchumIx].getHand().size() >= 2)
                        sidKetchum();
                    System.out.println(turnCharacter + ": <Choose two of these");                    
                    for(int j = 0; j < 3; j++){
                        if(deck.isEmpty())
                            deck = noDeck(discardPile);
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
                    turnPlayer.getHand().add(t[choice]);
                    turnPlayer.getHand().add(t[x]);
                    if(x + choice == 1)
                        deck.addFirst(t[2]);
                    else if(x + choice == 2)
                        deck.addFirst(t[1]);
                    else
                        deck.addFirst(t[0]);
                    drawn = 2;
                    break;
                case "Pedro Ramirez":
                    readDiscard();
                    if(sidKetchumIx >= 0 && players[sidKetchumIx].getHand().size() >= 2)
                        sidKetchum();
                    do{
                        System.out.println(turnCharacter + ": <Insert 1 to activate your character's ability or 0 to ignore ");
                        choice = input.nextInt();
                    }while(choice < 0 || choice > 1);
                    if(choice == 1){
                        if(!discardPile.isEmpty())
                            turnPlayer.draw(discardPile, null);
                        drawn++;
                    }
                    if(sidKetchumIx >= 0 && players[sidKetchumIx].getHand().size() >= 2)
                        sidKetchum();
            }
            for(; drawn < 2; drawn++){
                if(sidKetchumIx >= 0 && players[sidKetchumIx].getHand().size() >= 2)
                    sidKetchum();
                if(turnCharacter == "Black Jack" && drawn == 1){
                    if(deck.isEmpty())
                        deck = noDeck(discardPile);
                    System.out.println("Black Jack revealed " + deck.getFirst());
                    if(deck.getFirst().getSuit() == 'H' || deck.getFirst().getSuit() == 'D'){
                        turnPlayer.draw(deck, discardPile);
                    }
                }
                turnPlayer.draw(deck, discardPile);
            }
            if(sidKetchumIx >= 0 && players[sidKetchumIx].getHand().size() >= 2)
                sidKetchum();
            boolean goon = true, bang = false;
            distances = distances();
            turnPlayer.readHand();
            while(goon){
                System.out.println(turnCharacter + ": <Choose an option:\n1: Print remaining lives.\n2: Print characters.\n3: Print the Sheriff.\n4: Number of cards in other's hand.\n5: Print your hand.\n6: Print your role.\n7: Print active cards.\n8: Print ranges\n9: Print distances.\n10: Print first card in the discard pile.\n11: Print remaining cards in the deck\n12: Play a card.\n13: Terminate your turn.");
                choice = input.nextInt();
                switch (choice) {
                    case 1:
                        do{
                            System.out.println(turnCharacter + ": <Choose player's id to check his lives or 0 to check everyones'");
                            printActivePlayers();
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printLives(choice);
                        break;
                    case 2:
                        do{
                            System.out.println(turnCharacter + ": <Choose player's id to check his character or 0 to check everyone's ");
                            printActivePlayers();
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printCharacters(choice);
                        break;
                    case 3:
                        findSheriff();
                        break;
                    case 4:
                        do{
                            System.out.println(turnCharacter + ": <Choose player's id to check the number of cards in his hands or 0 to check everyone's ");
                            printActivePlayers();
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printNHands(choice);
                        break;
                    case 5:
                        turnPlayer.readHand();
                        break;
                    case 6:
                        turnPlayer.readRole();
                        break;
                    case 7:
                        do{
                            System.out.println(turnCharacter + ": <Choose player's id to check his active cards or 0 to check everyones' ");
                            printActivePlayers();
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printActiveCards(choice);
                        break;
                    case 8:
                        do{
                            System.out.println(turnCharacter + ": <Choose player's id to check his active range or 0 to check everyones' ");
                            printActivePlayers();
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printRange(choice);
                        break;
                    case 9:
                        do{
                            System.out.println(turnCharacter + ": <Choose player's id to check his actual distance from you or 0 to check everyone's ");
                            printActivePlayers();
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printDistances(choice);
                        break;
                    case 10:
                        readDiscard();
                        break;
                    case 11:
                        System.out.println("cards remaining:" + deck.size());
                        break;    
                    case 12:
                        turnPlayer.readHand();
                        do{
                            System.out.println(turnCharacter + ": <Choose the card you want to play or 0 to cancel ");
                            choice = input.nextInt() - 1;
                        }while(choice < -1 || choice >= turnPlayer.getHand().size());
                        if(choice == -1)
                            break;
                        int card = choice;
                        String name = turnPlayer.getHand().get(card).getName();
                        boolean possible;
                        if(checkPlayable(name, bang)){
                            switch (name) {
                                case "Missed!":
                                case "Bang!":
                                    possible = false;
                                    do{
                                        System.out.println(turnCharacter + ": <Choose the player you want to shoot or 0 to cancel");
                                        printDistances(0);
                                        choice = input.nextInt() - 1;
                                        if(choice >= 0 && choice < nPlayers){
                                            possible = turnPlayer.getRange() >= distances[choice];
                                            if(!possible)
                                                System.out.println("You don't see him");                                            
                                        }
                                        else if(choice == -1)
                                            possible = true;
                                    }while(!possible);
                                    if(choice == -1)
                                        break;
                                    int target = choice;
                                    turnPlayer.discard(card, discardPile, deck);
                                    bang = true;
                                    if(bang(target, false)){
                                        endMatch(nStartingPlayers, target);
                                        death(turnPlayerIx, target);
                                        if(turnPlayerIx == target){
                                            if(turnPlayerIx == nPlayers)
                                                turnPlayerIx = 0;
                                            continue skipTurn;
                                        }
                                    }
                                    break;
                                case "Cat Balou":
                                    do{
                                        System.out.println(turnCharacter + ": <Choose the player you want to discard a card from or 0 to cancel ");
                                        printActivePlayers();
                                        choice = input.nextInt() - 1;
                                    }while(choice < -1 || choice >= nPlayers);
                                    if(choice == -1)
                                        break;
                                    target = choice;
                                    boolean done = false;
                                    do{ 
                                        System.out.println(turnCharacter + ": <Choose:\n0 if you want to cancel\n1 if you want to discard a card from his hand or");
                                        int i = 2;
                                        for (PlayingCard activeCard : players[target].getActiveCards())
                                            System.out.println((i++) + " if you want to discard his " + activeCard.getName());
                                        if(players[target].isWeapon())
                                            System.out.println(i + " if you want to discard his weapon");
                                        choice = input.nextInt();
                                        if(choice == 0)
                                            done = true;
                                        else if(choice == 1){
                                            done = true;
                                            turnPlayer.discard(card, discardPile, deck);
                                            if(!players[target].getHand().isEmpty())
                                                players[target].discard((int) Math.random() * players[target].getHand().size(), discardPile, deck);
                                        }
                                        else if(choice > 1 && choice < i){
                                            done = true;
                                            turnPlayer.discard(card, discardPile, deck);
                                            players[target].getActiveCards().remove(choice-2).discard(discardPile);
                                        }
                                        else if(choice == i){
                                            if(players[target].isWeapon()){
                                                done = true;
                                                turnPlayer.discard(card, discardPile, deck);
                                                players[target].removeWeapon().discard(discardPile);
                                            }
                                        }
                                        if(!done)
                                            System.err.println("Wrong input");
                                    }while(!done);
                                    if(choice != 0)
                                        System.out.println(discardPile.getFirst() + " discarded for Cat Balou effect");
                                    break;
                                case "Panic!":  
                                    do{
                                        System.out.println(turnCharacter + ": <Choose the player you want to draw from or 0 to cancel ");
                                        printDistances(-1);
                                        choice = input.nextInt() - 1;
                                    }while(choice < -1 || choice >= nPlayers || (choice != -1 && distances[choice] > 1));
                                    if(choice == -1)
                                        break;
                                    target = choice;
                                    done = false;
                                    do{
                                        System.out.println("Choose:\n0 if you want to cancel\n1 if you want to get a card from his hand or");
                                        int i = 2;
                                        for (PlayingCard activeCard : players[target].getActiveCards())
                                            System.out.println((i++) + " if you want to get his " + activeCard.getName());
                                        if(players[target].isWeapon())
                                            System.out.println(i + " if you want to get his weapon");
                                        choice = input.nextInt();
                                        if(choice == 0)
                                            done = true;
                                        else if(choice == 1){
                                            done = true;
                                            turnPlayer.discard(card, discardPile, deck);
                                            if(turnPlayerIx != target && !players[target].getHand().isEmpty())
                                                panicEffect(turnPlayerIx, target);
                                        }
                                        else if(choice < i && choice > 1){
                                            done = true;
                                            turnPlayer.discard(card, discardPile, deck);
                                            turnPlayer.getHand().add(players[target].getActiveCards().remove(choice-2));
                                        }
                                        else if(choice == i){
                                            if(players[target].isWeapon()){
                                                done = true;
                                                turnPlayer.discard(card, discardPile, deck);
                                                turnPlayer.getHand().add(players[target].removeWeapon());
                                            }
                                        }
                                        if(!done)
                                            System.out.println("Wrong input");
                                    }while(!done);
                                    break;
                                case "Wells Fargo":
                                    turnPlayer.discard(card, discardPile, deck);
                                    turnPlayer.draw(deck, discardPile);
                                case "Stagecoach":
                                    if(name == "Stagecoach")
                                        turnPlayer.discard(card, discardPile, deck);                                
                                    for(int i = 0; i < 2; i++){
                                        turnPlayer.draw(deck, discardPile);
                                    }
                                    break;
                                case "Scope":
                                    if(!turnPlayer.isActiveCard("Scope"))
                                        for(int i = 0; i < nPlayers; i++)
                                            if(i != turnPlayerIx)
                                                distances[i]--;
                                case "Barrel":
                                case "Dynamite":
                                case "Mustang":
                                    turnPlayer.removeActiveCard(name).discard(discardPile);
                                    turnPlayer.getActiveCards().add(turnPlayer.removeFromHand(card, deck, discardPile));
                                    break;
                                case "Jail":
                                    do{
                                        System.out.println(turnCharacter + " <Choose the player you want to put in jail or 0 to cancel ");
                                        printActivePlayers();
                                        choice = input.nextInt() - 1;
                                        if(choice == sheriffIx)
                                            System.out.println("Impossible to put the Sheriff in jail");                                        
                                    }while(choice < -1 || choice >= nPlayers || choice == sheriffIx);
                                    if(choice != -1){
                                        players[choice].removeActiveCard("Jail").discard(discardPile);
                                        players[choice].getActiveCards().add(turnPlayer.removeFromHand(card, deck, discardPile));
                                    }
                                    break;
                                case "Saloon":
                                    turnPlayer.discard(card, discardPile, deck);
                                    for(int i = 0; i < nPlayers; i++)
                                        players[i].addLife();
                                    break;
                                case "Beer":
                                    turnPlayer.discard(card, discardPile, deck);
                                    if(nPlayers > 2)
                                        turnPlayer.addLife();
                                    break;
                                case "Gatling":
                                    turnPlayer.discard(card, discardPile, deck);
                                    for(int i = turnPlayerIx + 1; i != turnPlayerIx; i++){
                                        if(i == nPlayers)
                                            i = 0;
                                        if(bang(i, true)){
                                            endMatch(nStartingPlayers, i);
                                            death(turnPlayerIx, i);
                                            i--;
                                        }
                                        if(i == nPlayers - 1 && turnPlayerIx == 0)
                                            i = -1;
                                    }
                                    break;
                                case "Duel":
                                    do{
                                        System.out.println(turnCharacter + " <Choose a player you want to duel or 0 to cancel ");
                                        printActivePlayers();
                                        choice = input.nextInt() - 1;
                                    }while(choice < -1 || choice >= nPlayers || choice == turnPlayerIx);
                                    if(choice != -1){
                                        turnPlayer.discard(card, discardPile, deck);
                                        int res = duel(choice);
                                        if(res == 1){
                                            endMatch(nStartingPlayers, turnPlayerIx);
                                            death(turnPlayerIx, turnPlayerIx);
                                            if(turnPlayerIx == nPlayers)
                                                turnPlayerIx = 0;
                                            continue skipTurn;   
                                        }
                                        else if(res == 2){
                                            endMatch(nStartingPlayers, choice);
                                            death(turnPlayerIx, choice);
                                        }
                                    }
                                    break;
                                case "Indians!":
                                    turnPlayer.discard(card, discardPile, deck);
                                    for(int i = turnPlayerIx + 1; i != turnPlayerIx; i++){
                                        if(i == nPlayers)
                                            i = 0;
                                        if(indians(i)){
                                            endMatch(nStartingPlayers, i);
                                            death(turnPlayerIx, i);
                                            i--;
                                        }
                                        if(i == nPlayers - 1 && turnPlayerIx == 0)
                                            i = -1;
                                    }
                                    break;
                                case "General Store":
                                    turnPlayer.discard(card, discardPile, deck);
                                    ArrayList<PlayingCard> generalStore = new ArrayList<PlayingCard>(nPlayers);
                                    int storeSize = nPlayers;
                                    for(int i = 0; i < nPlayers; i++){
                                        if(deck.isEmpty())
                                            deck = noDeck(discardPile);
                                        generalStore.add(deck.removeFirst());
                                    }
                                    for(int i = turnPlayerIx; storeSize > 1; i++, storeSize--){
                                        if(i == nPlayers)
                                            i = 0;
                                        players[i].readHand();
                                        do{
                                            System.out.println("Cards available in the General Store:");
                                            for(int j = 0; j < storeSize; j++)
                                                System.out.println((j+1) + ") " + generalStore.get(j));
                                            choice = input.nextInt() - 1;
                                        }while(choice < 0 || choice >= storeSize);
                                        players[i].getHand().add(generalStore.remove(choice));
                                    }
                                    if(turnPlayerIx == 0)
                                        players[nPlayers-1].getHand().add(generalStore.removeFirst());
                                    else
                                        players[turnPlayerIx-1].getHand().add(generalStore.removeFirst());
                                    break;
                                default:
                                    if(turnPlayer.isWeapon())
                                        turnPlayer.removeWeapon().discard(discardPile);
                                    turnPlayer.setWeapon((Weapon) (turnPlayer.removeFromHand(card, deck, discardPile)));
                                    break;
                            }
                        }
                        else
                            System.err.println(turnCharacter + ": (You can't play this card");
                        break;
                    case 13:
                        goon = false;
                        break;
                    default:
                        System.out.println("Unavailable option.");
                }
            }
            int handSize = turnPlayer.getHand().size();
            while(handSize > turnPlayer.getLives()){
                System.out.println(turnCharacter + " has to discard cards until those are the same number of his lives.");
                turnPlayer.readHand();
                do{
                    System.out.println(turnCharacter + " <Choose a card to discard");
                    choice = input.nextInt() - 1;
                }while(choice < 0 || choice >= handSize);
                turnPlayer.discard(choice, discardPile);
                handSize--;
            }
            if(turnPlayerIx == nPlayers - 1)
                turnPlayerIx = 0;
            else
                turnPlayerIx++;
        }
    }

    private int findSheriff(String[] roles, String[] names){
        for(int i = 0; i < nPlayers; i++)
            if(roles[i] == "Sheriff"){
                System.out.println(names[i] + " is the Sheriff");
                return i;
            }
        return -1;
    }

    private int findSheriff(){
        for(int i = 0; i < nPlayers; i++)
            if(players[i].getRole() == "Sheriff"){
                System.out.println(players[i].toString() + " is the Sheriff");
                return i;
            }
        return -1;
    }

    private int findCharacter(String name){
        for(int i = 0; i < nPlayers; i++)
            if(players[i].getCharacter().getName() == name)
                return i;
        return -1;
    }

    private Characters[] createCharacters(){ 
        Characters[] characters = new Characters[16];
        characters[0] = new Characters("Jesse Jones", 4, "He may draw his first card from the hand of a player.");
        characters[1] = new Characters("Black Jack", 4, "He shows the second card he draws. On heart or Diamonds, he draws one more card.");
        characters[2] = new Characters("Rose Doolan", 4, "She sees all players at a distance decreased by 1.");
        characters[3] = new Characters("El Gringo", 3, "Each time he is hit by a player, he draws a card from the hand of that player.");
        characters[4] = new Characters("Bart Cassidy", 4, "Each time he is hit, he draws a card.");
        characters[5] = new Characters("Lucky Duke", 4, "Each time he \"draws!\", he flips the top two cards and chooses one.");
        characters[6] = new Characters("Sid Ketchum", 4, "He may discard 2 cards to regain one life point.");
        characters[7] = new Characters("Suzy Lafayette", 4, "As soon as she has no cards in hand, she draws a card.");
        characters[8] = new Characters("Vulture Sam", 4, "Whenever a player is eliminated from play, he takes in hand all the cards of that player.");
        characters[9] = new Characters("Kit Carlson", 4, "He looks at the top three cards of the deck and chooses the 2 to draw.");
        characters[10] = new Characters("Willy The Kid", 4, "He can play any number of BANG! cards.");
        characters[11] = new Characters("Slab The Killer", 4, "Player needs 2 Missed! cards to cancel his BANG! card.");
        characters[12] = new Characters("Pedro Ramirez", 4, "He may draw his first card from the discard pile.");
        characters[13] = new Characters("Jourdonnais", 4, "Whenever he is the target of a BANG!, he may \"draw\": on a Heart, he is missed.");
        characters[14] = new Characters("Calamity Janet", 4, "She can play BANG! cards as Missed! and vice versa.");
        characters[15] = new Characters("Paul Regret", 3, "All players see him at a distance increased by 1.");
        return characters;
    }

    private String[] drawRoles(){
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

    private Characters[] drawCharacters(Characters[] availableCharacters, String[] names){
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

    private PlayingCard[] createCards(){
        PlayingCard[] playingCards = new PlayingCard[80];
        playingCards[0] = new PlayingCard("Cat Balou", 'D', 11);
        playingCards[1] = new Weapon("Rev. Carabine", 'C', 1, 4, false);
        playingCards[2] = new PlayingCard("Bang!", 'C', 7);
        playingCards[3] = new PlayingCard("Jail", 'S', 11);
        playingCards[4] = new PlayingCard("Duel", 'S', 11);
        playingCards[5] = new PlayingCard("Mustang", 'H', 9);
        playingCards[6] = new PlayingCard("Barrel", 'S', 12);
        playingCards[7] = new PlayingCard("Jail", 'S', 10);
        playingCards[8] = new PlayingCard("Scope", 'S', 1);
        playingCards[9] = new PlayingCard("Dynamite", 'H', 2);
        playingCards[10] = new PlayingCard("Bang!", 'D', 7);
        playingCards[11] = new Weapon("Remington", 'C', 13, 3, false);
        playingCards[12] = new PlayingCard("Wells Fargo", 'H', 3);
        playingCards[13] = new PlayingCard("Beer", 'H', 10);
        playingCards[14] = new PlayingCard("Bang!", 'C', 6);
        playingCards[15] = new PlayingCard("Bang!", 'C', 5);
        playingCards[16] = new PlayingCard("Beer", 'H', 7);
        playingCards[17] = new PlayingCard("Missed!", 'S', 4);
        playingCards[18] = new PlayingCard("Bang!", 'D', 13);
        playingCards[19] = new PlayingCard("Bang!", 'D', 12);
        playingCards[20] = new PlayingCard("Missed!", 'C', 11);
        playingCards[21] = new Weapon("Winchester", 'S', 8, 5, false);
        playingCards[22] = new PlayingCard("Stagecoach", 'S', 9);
        playingCards[23] = new PlayingCard("Beer", 'H', 6);
        playingCards[24] = new PlayingCard("Bang!", 'D', 4);
        playingCards[25] = new PlayingCard("Gatling", 'H', 10);
        playingCards[26] = new PlayingCard("Panic!", 'D', 8);
        playingCards[27] = new PlayingCard("Bang!", 'H', 13);
        playingCards[28] = new PlayingCard("Panic!", 'H', 12);
        playingCards[29] = new PlayingCard("Jail", 'H', 4);
        playingCards[30] = new PlayingCard("Missed!", 'C', 10);
        playingCards[31] = new Weapon("Volcanic", 'C', 10, 1, true);
        playingCards[32] = new PlayingCard("Bang!", 'C', 4);
        playingCards[33] = new PlayingCard("Missed!", 'C', 1);
        playingCards[34] = new PlayingCard("General Store", 'C', 9);
        playingCards[35] = new PlayingCard("Indians!", 'D', 13);
        playingCards[36] = new PlayingCard("Bang!", 'C', 2);
        playingCards[37] = new PlayingCard("Bang!", 'D', 6);
        playingCards[38] = new PlayingCard("Bang!", 'D', 10);
        playingCards[39] = new PlayingCard("Bang!", 'D', 3);
        playingCards[40] = new PlayingCard("Cat Balou", 'D', 10);
        playingCards[41] = new Weapon("Schofield", 'S', 13, 2, false);
        playingCards[42] = new PlayingCard("Bang!", 'D', 5);
        playingCards[43] = new PlayingCard("Beer", 'H', 8);
        playingCards[44] = new PlayingCard("Beer", 'H', 11);
        playingCards[45] = new PlayingCard("Bang!", 'C', 3);
        playingCards[46] = new PlayingCard("Panic!", 'H', 11);
        playingCards[47] = new PlayingCard("Bang!", 'H', 12);
        playingCards[48] = new PlayingCard("Missed!", 'S', 2);
        playingCards[49] = new PlayingCard("Saloon", 'H', 5);
        playingCards[50] = new PlayingCard("Bang!", 'D', 2);
        playingCards[51] = new Weapon("Schofield", 'C', 12, 2, false);
        playingCards[52] = new PlayingCard("Bang!", 'C', 9);
        playingCards[53] = new PlayingCard("Bang!", 'S', 1);
        playingCards[54] = new PlayingCard("Bang!", 'H', 1);
        playingCards[55] = new PlayingCard("Stagecoach", 'S', 9);
        playingCards[56] = new PlayingCard("Bang!", 'D', 8);
        playingCards[57] = new PlayingCard("Bang!", 'D', 11);
        playingCards[58] = new PlayingCard("Beer", 'H', 9);
        playingCards[59] = new PlayingCard("Duel", 'D', 12);
        playingCards[60] = new PlayingCard("Duel", 'C', 8);
        playingCards[61] = new Weapon("Schofield", 'C', 11, 2, false);
        playingCards[62] = new PlayingCard("Panic!", 'H', 1);
        playingCards[63] = new PlayingCard("Indians!", 'D', 1);
        playingCards[64] = new PlayingCard("Mustang", 'H', 8);
        playingCards[65] = new PlayingCard("Cat Balou", 'D', 9);
        playingCards[66] = new PlayingCard("General Store", 'S', 12);
        playingCards[67] = new PlayingCard("Bang!", 'D', 1);
        playingCards[68] = new PlayingCard("Missed!", 'S', 7);
        playingCards[69] = new PlayingCard("Missed!", 'S', 3);
        playingCards[70] = new PlayingCard("Missed!", 'S', 8);
        playingCards[71] = new Weapon("Volcanic", 'S', 10, 1, true);
        playingCards[72] = new PlayingCard("Missed!", 'C', 12);
        playingCards[73] = new PlayingCard("Cat Balou", 'H', 13);
        playingCards[74] = new PlayingCard("Bang!", 'C', 8);
        playingCards[75] = new PlayingCard("Bang!", 'D', 9);
        playingCards[76] = new PlayingCard("Missed!", 'S', 6);
        playingCards[77] = new PlayingCard("Missed!", 'S', 5);
        playingCards[78] = new PlayingCard("Barrel", 'S', 13);
        playingCards[79] = new PlayingCard("Missed!", 'C', 13);
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

    private void readDiscard(){
        if(discardPile.isEmpty())
            System.out.println("Discard pile is empty");            
        else
            System.out.println("First card in the discard pile: " + discardPile.getFirst());
    }

    private void sidKetchum(){
        boolean c;
        do{
            c = players[sidKetchumIx].sidKetchum(discardPile, input);
        }while(c);
    }

    private void printLives(int choice){
        if(choice == 0)
            for(int i = 0; i < nPlayers; i++)
                System.out.println(players[i].toString() + "'s lives: " + players[i].getLives() + "/" + players[i].getStartingLives());
        else{
            choice--;
            System.out.println(players[choice].toString() + "'s lives: " + players[choice].getLives() + "/" + players[choice].getStartingLives());
        }
    }

    private void printCharacters(int choice){
        if(choice == 0)
            for(int i = 0; i < nPlayers; i++)
                System.out.println(players[i].getName() + "'s character: " + players[i].getCharacter());
        else{
            choice--;
            System.out.println(players[choice].getName() + "'s character: " + players[choice].getCharacter());
        }
    }

    private void printNHands(int choice){
        if(choice == 0)
            for(int i = 0; i < nPlayers; i++)
                System.out.println(players[i].toString() + " has " + players[i].getHand().size() + " in his hands");
        else
            System.out.println(players[choice-1].toString() + "has " + players[choice-1].getHand().size() + " in his hands");
    }

    private void printActiveCards(int choice){
        if(choice == 0){
            for(int i = 0; i < nPlayers; i++){
                if(!players[i].getActiveCards().isEmpty())
                    System.out.println(players[i].toString() + " active cards:\n" + players[i].stringActiveCards());
                else
                    System.out.println(players[i].toString() + " has no active cards");
            }
        }
        else{
            choice--;
            if(!players[choice].getActiveCards().isEmpty())
                System.out.println(players[choice].toString() + " active cards:\n" + players[choice].stringActiveCards());
            else
                System.out.println(players[choice].toString() + " has no active cards");
        }
    }

    private void printRange(int choice){
        if(choice == 0)
            for(int i = 0; i < nPlayers; i++)
                System.out.println(players[i].toString() + "'s range is " + players[i].getRange());
        else
            System.out.println(players[choice-1].toString() + "'s range is " + players[choice-1].getRange());
    }

    private int[] distances(){
        int[] distances = new int[nPlayers];
        distances[turnPlayerIx] = 0;
        for(int i = 0; i < nPlayers; i++)
            if(i != turnPlayerIx){
                distances[i] = Math.min(Math.min(Math.abs(turnPlayerIx - i), Math.abs(turnPlayerIx - i - nPlayers)), Math.abs(turnPlayerIx - i + nPlayers));
                if(players[i].getCharacter().getName() == "Paul Regret")
                    distances[i]++;
                if(players[i].isActiveCard("Mustang"))
                    distances[i]++;
                if(turnPlayer.getCharacter().getName() == "Rose Doolan")
                    distances[i]--;
                if(turnPlayer.isActiveCard("Scope"))
                    distances[i]--;
            }
        return distances;
    }
    
    private void printDistances(int choice){
        if(choice == -1){
            for(int i = 0; i < nPlayers; i++)
                if(distances[i] <= 1)
                    System.out.println(players[i].toString() + "'s distance from you:\n" + distances[i]);
        }
        else if(choice == 0){
            for(int i = 0; i < nPlayers; i++)
                if(i != turnPlayerIx)
                    System.out.println(players[i].toString() + "'s distance from you:\n" + distances[i]);
        }
        else{
            choice--;
            System.out.println(players[choice].toString() + "'s distance from you:\n" + distances[choice]);
        }
    }

    private boolean bang(int defender, boolean gatling){
        int shot = 1, choice;
        if(turnPlayer.getCharacter().getName() == "Slab The Killer" && !gatling){
            System.out.println(players[defender] + " will need 2 \"Missed!\" for this Bang!");
            shot++;
        }
        if(players[defender].isActiveCard("Barrel")){
            do{
                System.out.println(players[defender] + " <Choose 1 to activate barrel or 0 to ignore ");
                choice = input.nextInt();
            }while(choice < 0 || choice > 1);
            if(choice == 1){
                boolean res;
                if(players[defender].getCharacter().getName() == "Lucky Duke")
                    res = luckyDuke();
                else
                    res = drawHearts();
                if(res){
                    shot--;
                    System.out.println("Missed!");
                }
            }
        }
        if(shot > 0 && players[defender].getCharacter().getName() == "Jourdonnais"){
            do{
                System.out.println(players[defender] + " <Choose 1 to activate your character's ability or 0 to ignore ");
                choice = input.nextInt();
            }while(choice < 0 || choice > 1);
            if(choice == 1)
                if(drawHearts()){
                    shot--;
                    System.out.println("Missed!");
                }
        }        
        for(int i = 0; i < shot; i++){
            players[defender].readHand();
            boolean miss = false;
            do{
                System.out.println(players[defender] + " <Choose a \"Missed\" or 0 to lose a life ");
                choice = input.nextInt() - 1;
                if(choice >= 0 && choice < players[defender].getHand().size()){
                    miss = players[defender].getHand().get(choice).getName() == "Missed!";
                    if(players[defender].getCharacter().getName() == "Calamity Janet")
                        miss |= players[defender].getHand().get(choice).getName() == "Bang!";
                }
                if(choice == -1)
                    miss = true;
            }while(!miss);
            if(choice == -1){
                if(subLife(defender, turnPlayerIx))
                    return true;
                i++;
            }
            else{
                players[defender].discard(choice, discardPile, deck); //suzy vs slab?
                System.out.println("Missed!");
            }
        }
        return false;
    }

    private int duel(int target){
        Player dueller = players[target];
        int choice;
        while(true){
            boolean bang = false;
            do{
                dueller.readHand();
                System.out.println(dueller + " <Choose a \"Bang!\" to go on with the duel or 0 to lose a life ");
                choice = input.nextInt() - 1;
                if(choice >= 0 && choice < dueller.getHand().size()){
                    bang = dueller.getHand().get(choice).getName() == "Bang!";
                    if(dueller.getCharacter().getName() == "Calamity Janet")
                        bang |= dueller.getHand().get(choice).getName() == "Missed!";
                }
                if(choice == -1)
                    bang = true;
            }while(!bang);
            if(choice == -1){
                if(dueller.getCharacter().getName() == players[target].toString()){
                    if(subLife(target, turnPlayerIx))
                        return 2;
                }
                else if(subLife(turnPlayerIx, turnPlayerIx))
                    return 1;
                return 0;
            }
            dueller.discard(choice, discardPile, deck);
            if(dueller.getCharacter().getName() == players[target].getCharacter().getName())
                dueller = turnPlayer;
            else
                dueller = players[target];
        }
    }
    
    private boolean luckyDuke(){
        PlayingCard[] tmp = new PlayingCard[2];
        int choice1, choice2;
        for(int i = 0; i < 2; i++)
            if(deck.isEmpty()){
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

    private boolean drawHearts(){
        if(deck.isEmpty())
            deck = noDeck(discardPile);
        deck.removeFirst().discard(discardPile);
        System.out.println(discardPile.getFirst() + " \"drawn\" for effect");
        if(discardPile.getFirst().getSuit() == 'H')
            return true;
        return false;
    }

    private boolean subLife(int wounded, int attacker){
        players[wounded].subLife();
        if(players[wounded].getCharacter().getName() == "El Gringo" && attacker >= 0 && !players[attacker].getHand().isEmpty())
            panicEffect(wounded, attacker);
        else if(players[wounded].getCharacter().getName() == "Bart Cassidy"){
            players[wounded].draw(deck, discardPile);
        }
        if(players[wounded].getLives() == 0){
            LinkedList<PlayingCard> beers = new LinkedList<PlayingCard>();
            for(PlayingCard card : players[wounded].getHand())
                if(card.getName() == "Beer")
                    beers.add(card);
            if(!beers.isEmpty()){
                int choice;
                int nBeers = beers.size();
                do{ 
                    System.out.println(players[wounded] + " (Your beers:");
                    for(int i = 0; i < nBeers; i++)
                        System.out.println((i+1) + ") " + players[wounded].getHand().get(i));
                    System.out.println(players[wounded] + " <Choose a beer or 0 to ignore");
                    choice = input.nextInt() - 1;
                }while(choice < -1 || choice >= nBeers);
                if(choice != -1){
                    players[wounded].discard(beers.get(choice), discardPile, deck);
                    if(nPlayers > 2)
                        players[wounded].addLife();
                    System.out.println(discardPile.getFirst() + " used and life gained");
                }
            }
        }
        if(players[wounded].getLives() == 0)
            return true;
        return false;
    }

    private void death(int killer, int killed){
        int vulture = findCharacter("Vulture Sam");
        if(vulture >= 0 && vulture != killed){
            while (!players[killed].getHand().isEmpty())
                players[vulture].getHand().add(players[killed].getHand().removeFirst());
            while(!players[killed].getActiveCards().isEmpty())
                players[vulture].getHand().add(players[killed].getActiveCards().removeFirst());
            if(players[killed].isWeapon())
                players[vulture].getHand().add(players[killed].removeWeapon());
        }
        else
            players[killed].discardAll(input, discardPile, deck);
        if(killer >= 0){
            if(players[killer].getRole() == "Sheriff" && players[killer].getRole() == "Deputy")
                players[killer].discardAll(input, discardPile, deck);
            else if(players[killed].getRole() == "Outlaw")
                for(int i = 0; i < 3; i++)
                    players[killer].draw(deck, discardPile);
        }
        for(int i = killed + 1; i != nPlayers; i++){
            Player tmp = players[i];
            players[i] = players[i-1];
            players[i-1] = tmp;
        }
        nPlayers--;
        if(turnPlayerIx == nPlayers)
            turnPlayerIx = 0;
        else if(turnPlayerIx > killed)
            turnPlayerIx--;
        if(sheriffIx > killed)
            sheriffIx--;
        if(sidKetchumIx > killed)
            sidKetchumIx--;
        else if(sidKetchumIx == killed)
            sidKetchumIx = -1;
        distances = distances();
    }

    private void panicEffect(int robber, int robbed){
        PlayingCard rand = players[robbed].removeFromHand((int) Math.random() * players[robbed].getHand().size(), deck, discardPile);
        System.out.println(players[robber].toString() + ": (Drawn " + rand);
        System.out.println(players[robbed].toString() + ": (" + players[robber].toString() + " robbed your " + rand);
        players[robber].getHand().add(rand);
    }

    private void printActivePlayers(){
        for(int i = 0; i < nPlayers; i++)
            System.out.println((i+1) + ") " + players[i]);
    }

    private void endMatch(int startingNPlayers, int dead){
        System.out.println(players[dead] + " was a " + players[dead].getRole());
        if(players[dead].getRole() == "Sheriff"){
            int renegade = -1;
            for(int i = 0; i < nPlayers; i++)
                if(i != dead){
                    if(players[i].getRole() != "Renegade")
                        outlawsWin(startingNPlayers);
                    /*else if(renegade != -1){    //if there is more than one renegade(expansion)
                        outlawsWin(players, startingNPlayers);
                        return true;
                    }*/
                    else
                        renegade = i;
                }
            System.out.println(players[renegade] + "(" + players[renegade].getName() + ") has won the match as a Renegade");
            System.exit(3);
        }
        for(int i = 0; i < nPlayers; i++)
            if(i != dead)
                if(players[i].getRole() != "Sheriff" && players[i].getRole() != "Deputy")
                    return;
        sheriffWin(startingNPlayers);
    }

    private void outlawsWin(int startingNPlayers){
        System.out.println("Outlaws have won the match\nWinners:");
        for(int i = 0; i < startingNPlayers; i++)
            if(players[i].getRole() == "Outlaw")
                System.out.println(players[i] + "(" + players[i].getName() + ") has won the match as a Outlaw");
        System.exit(2);
    }

    private void sheriffWin(int startingNPlayers){
        System.out.println("Sheriff and his Deputies has won the match\nWinners:");
        for(int i = 0; i < startingNPlayers; i++)
            if(players[i].getRole() == "Sheriff" || players[i].getRole() == "Deputy")
                System.out.println(players[i] + "(" + players[i].getName() + ") has won the match as a " + players[i].getRole());
        System.exit(1);
    }

    private boolean dynamiteEffect(){
        if(turnPlayer.getCharacter().getName() == "Lucky Duke"){
            PlayingCard[] tmp = new PlayingCard[2];
            int choice1, choice2;
            for(int i = 0; i < 2; i++){
                if(deck.isEmpty())
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
                return explosion();
        }
        else{
            if(deck.isEmpty())
                deck = noDeck(discardPile);
            deck.removeFirst().discard(discardPile);
            System.out.println(discardPile.getFirst() + " drawn for effect");
            if(discardPile.getFirst().getSuit() == 'S' && discardPile.getFirst().getRank() >= 2 && discardPile.getFirst().getRank() <= 9)
                return explosion();
        }
        for(int i = turnPlayerIx + 1; i != turnPlayerIx; i++){
            if(i == nPlayers)
                i = 0;
            printActiveCards(0);
            if(!players[i].isActiveCard("Dynamite")){
                players[i].getActiveCards().add(turnPlayer.removeActiveCard("Dynamite"));
                i = turnPlayerIx - 1;
            }
            printActiveCards(0);
            if(i == nPlayers - 1 && turnPlayerIx == 0)
                i = -1;
        }
        return false;
    }

    private boolean explosion(){
        turnPlayer.removeActiveCard("Dynamite").discard(discardPile);
        System.out.println("Dynamite exploded: " + turnPlayer + " is going to lose 3 lives");
        for(int i = 0; i < 3; i++)
            if(subLife(turnPlayerIx, -1))
                return true;
        return false;
    }

    private boolean indians(int target){
        boolean bang = false;
        int choice;
        do{
            players[target].readHand();
            System.out.println(players[target] + " <Choose a \"Bang!\" or 0 to lose a life ");
            choice = input.nextInt() - 1;
            if(choice >= 0 && choice < players[target].getHand().size()){
                bang = players[target].getHand().get(choice).getName() == "Bang!";
                if(players[target].getCharacter().getName() == "Calamity Janet")
                    bang |= players[target].getHand().get(choice).getName() == "Missed!";
            }
            if(choice == -1)
                bang = true;
        }while(!bang);
        if(choice == -1)
            return subLife(target, turnPlayerIx);
        players[target].discard(choice, discardPile, deck);
        return false;
    }

    private boolean checkPlayable(String card, boolean bang){
        switch(card){
            case "Missed!":
                if(turnPlayer.getCharacter().getName() != "Calamity Janet")      //se falso va comunque nel caso Bang!
                    return false;                                   
            case "Bang!":
                if(!bang || turnPlayer.isUnlimitedBang())
                    return true;
                return false;
            default:
                return true;                                  
        }
    }
}
