package match;

import java.util.LinkedList;
import java.util.Scanner;

import cards.*;
import characters.Characters;
import player.Player;

public class Match {
    private int nPlayers;
    final private Player[] Players;
    private int sheriffIx;
    private int sidKetchumIx;
    private int turnPlayerIx;
    private Player turnPlayer;
    private int Distances[];
    private LinkedList<Player> Winners = new LinkedList<Player>();
    final private Scanner input = new Scanner(System.in);

    public Match(int nStartPlayers, boolean inputName){
        final int nStartingPlayers = nStartPlayers;
        if(nStartingPlayers > 7 || nStartingPlayers < 4){
            System.err.println("Invalid number of players");
            System.exit(-1);
        }
        nPlayers = nStartingPlayers;
        final String[] Roles = drawRoles();
        final Characters[] Characters = drawCharacters(createCharacters());
        LinkedList<PlayingCard> Deck = shuffle(createCards());
        LinkedList<PlayingCard> DiscardPile = new LinkedList<PlayingCard>();
        Players = new Player[nStartingPlayers];
        if(inputName)
            for(int i = 0; i < nStartingPlayers; i++)
                Players[i] = new Player(Roles[i], Characters[i], input, Deck);
        else{
            final String[] Names = new String[nPlayers];
            for(int i = 0; i < nPlayers; i++)
                Names[i] = "Player " + (i+1);
            for(int i = 0; i < nPlayers; i++){
                Players[i] = new Player(Roles[i], Characters[i], Names[i], Deck);
                Players[i].readRole();
            }
        }
        sheriffIx = findSheriff();
        turnPlayerIx = sheriffIx;
        sidKetchumIx = findCharacter("Sid Ketchum");
        boolean match = true;
        skipTurn:
        while (match) {
            turnPlayer = Players[turnPlayerIx];
            System.out.println(turnPlayer + "'s turn:");
            System.out.println("Cards remaining: " + Deck.size());
            turnPlayer.readHand();
            int choice, drawn = 0;
            if(turnPlayer.isActiveCard("Dynamite"))
                if(dynamite(Deck, DiscardPile)){
                    if(endMatch(nStartingPlayers, new int[]{turnPlayerIx})){
                        match = false;
                        continue skipTurn;
                    }
                    death(-1, turnPlayerIx, false, Deck, DiscardPile);
                    continue skipTurn;
                }
            if(turnPlayer.isActiveCard("Jail")){
                sidKetchum(turnPlayer + " drawing for Jail", DiscardPile);
                boolean res = turnPlayer.drawHearts(Deck, DiscardPile, input);
                turnPlayer.removeActiveCard("Jail").discard(DiscardPile);
                if(!res){
                    if(turnPlayerIx == nPlayers - 1)
                        turnPlayerIx = 0;
                    else
                        turnPlayerIx++;
                    continue skipTurn;
                }
            }
            switch(turnPlayer.getCharacter().getName()){
                case "Jesse Jones":
                    sidKetchum(turnPlayer + " can activate his ability", DiscardPile);
                    do{
                        System.out.println(turnPlayer + ": <Choose 1 to activate your character's ability or 0 to ignore");
                        choice = input.nextInt();
                    }while(choice < 0 || choice > 1);
                    if(choice == 1){
                        do{
                            System.out.println(turnPlayer + ": <Choose the player you want to draw from or 0 to cancel");
                            printNHands(0);
                            choice = input.nextInt() - 1;
                        }while(choice < -1 || choice >= nPlayers);
                        if(choice == -1)
                            break;
                        if(choice != turnPlayerIx && !Players[choice].getHand().isEmpty())
                            panic(turnPlayerIx, choice, Deck, DiscardPile);
                        drawn++;
                    }
                    break;
                case "Kit Carlson":
                    sidKetchum(turnPlayer + " can activate his ability", DiscardPile);
                    turnPlayer.kitCarlson(Deck, DiscardPile, input);
                    drawn = 2;
                    break;
                case "Pedro Ramirez":
                    readDiscard(DiscardPile);
                    sidKetchum(turnPlayer + " can activate his ability", DiscardPile);
                    if(turnPlayer.pedroRamirez(DiscardPile, input))
                        drawn++;
            }
            sidKetchum(turnPlayer + " is going to draw", DiscardPile);
            turnPlayer.firstPhase(Deck, DiscardPile, drawn, input);
            boolean goon = true, bang = false;
            Distances = distances();
            sidKetchum(turnPlayer + " is starting is second phase", DiscardPile);
            turnPlayer.readHand();
            while(goon){
                System.out.println(turnPlayer + ": <Choose an option:\n1: Print remaining lives.\n2: Print characters.\n3: Print the Sheriff.\n4: Number of cards in other's hand.\n5: Print your hand.\n6: Print your role.\n7: Print active cards.\n8: Print ranges\n9: Print distances.\n10: Print first card in the discard pile.\n11: Print remaining cards in the deck\n12: Play a card.\n13: Terminate your turn.");
                choice = input.nextInt();
                switch (choice) {
                    case 1:
                        do{
                            System.out.println(turnPlayer + ": <Choose player's id to check his lives or 0 to check everyones'");
                            printActivePlayers();
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printLives(choice);
                        break;
                    case 2:
                        do{
                            System.out.println(turnPlayer + ": <Choose player's id to check his character or 0 to check everyone's");
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
                            System.out.println(turnPlayer + ": <Choose player's id to check the number of cards in his hands or 0 to check everyone's");
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
                            System.out.println(turnPlayer + ": <Choose player's id to check his active cards or 0 to check everyones'");
                            printActivePlayers();
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printActiveCards(choice);
                        break;
                    case 8:
                        do{
                            System.out.println(turnPlayer + ": <Choose player's id to check his active range or 0 to check everyones'");
                            printActivePlayers();
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printRange(choice);
                        break;
                    case 9:
                        do{
                            System.out.println(turnPlayer + ": <Choose player's id to check his actual distance from you or 0 to check everyone's");
                            printActivePlayers();
                            choice = input.nextInt();
                        }while(choice < 0 || choice > nPlayers);
                        printDistances(choice);
                        break;
                    case 10:
                        readDiscard(DiscardPile);
                        break;
                    case 11:
                        System.out.println("cards remaining:" + Deck.size());
                        break;    
                    case 12:
                        turnPlayer.readHand();
                        if(turnPlayer.getHand().isEmpty())
                            break;
                        do{
                            System.out.println(turnPlayer + ": <Choose the card you want to play or 0 to cancel");
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
                                        System.out.println(turnPlayer + ": <Choose the player you want to shoot or 0 to cancel");
                                        printDistances(-1);
                                        choice = input.nextInt() - 1;
                                        if(choice >= 0 && choice < nPlayers){
                                            possible = turnPlayer.getRange() >= Distances[choice];
                                            if(!possible)
                                                System.out.println("You don't see him");                                            
                                        }
                                        else if(choice == -1)
                                            possible = true;
                                    }while(!possible);
                                    if(choice == -1)
                                        break;
                                    int target = choice;
                                    bang = true;
                                    turnPlayer.discard(card, DiscardPile, Deck);
                                    if(bang(target, Deck, DiscardPile)){
                                        if(endMatch(nStartingPlayers, new int[]{target})){
                                            match = false;
                                            continue skipTurn;
                                        }
                                        death(turnPlayerIx, target, false, Deck, DiscardPile);;
                                        if(turnPlayerIx == target){
                                            if(turnPlayerIx == nPlayers)
                                                turnPlayerIx = 0;
                                            continue skipTurn;
                                        }
                                    }
                                    break;
                                case "Cat Balou":
                                    do{
                                        System.out.println(turnPlayer + ": <Choose the player you want to discard a card from or 0 to cancel");
                                        printActivePlayers();
                                        choice = input.nextInt() - 1;
                                    }while(choice < -1 || choice >= nPlayers);
                                    if(choice == -1)
                                        break;
                                    target = choice;
                                    boolean done = false;
                                    do{ 
                                        System.out.println(turnPlayer + ": <Choose:\n0 if you want to cancel\n1 if you want to discard a card from his hand");
                                        int i = 2;
                                        for (PlayingCard activeCard : Players[target].getActiveCards())
                                            System.out.println((i++) + " if you want to discard his " + activeCard.getName());
                                        if(Players[target].isWeapon())
                                            System.out.println(i + " if you want to discard his weapon");
                                        choice = input.nextInt();
                                        if(choice == 0)
                                            done = true;
                                        else if(choice == 1){
                                            done = true;
                                            turnPlayer.discard(card, DiscardPile);
                                            if(!Players[target].getHand().isEmpty()){
                                                PlayingCard p = Players[target].getHand().get((int) Math.random() * Players[target].getHand().size());
                                                System.out.println(p + " discarded by Cat Balou effect");
                                                Players[target].discard(p, DiscardPile, Deck);
                                            }
                                            else
                                                System.out.println("No card discarded by Cat Balou effect");
                                        }
                                        else if(choice > 1 && choice < i){
                                            done = true;
                                            turnPlayer.discard(card, DiscardPile);
                                            Players[target].getActiveCards().remove(choice-2).discard(DiscardPile);
                                            System.out.println(DiscardPile.getFirst() + " discarded by Cat Balou effect");
                                            if(DiscardPile.getFirst().getName() == "Scope")
                                                scope(false);
                                        }
                                        else if(choice == i){
                                            if(Players[target].isWeapon()){
                                                done = true;
                                                turnPlayer.discard(card, DiscardPile);
                                                Players[target].removeWeapon().discard(DiscardPile);
                                                System.out.println(DiscardPile.getFirst() + " discarded by Cat Balou effect");
                                            }
                                        }
                                        if(done && choice != 0){
                                            turnPlayer.suzyLafayette(Deck, DiscardPile);
                                            sidKetchum("", DiscardPile);
                                        }
                                        else
                                            System.err.println("Wrong input");
                                    }while(!done);
                                    break;
                                case "Panic!":
                                    do{
                                        System.out.println(turnPlayer + ": <Choose the player you want to draw from or 0 to cancel");
                                        printDistances(-1);
                                        choice = input.nextInt() - 1;
                                    }while(choice < -1 || choice >= nPlayers || (choice != -1 && Distances[choice] > 1));
                                    if(choice == -1)
                                        break;
                                    target = choice;
                                    done = false;
                                    do{
                                        System.out.println("Choose:\n0 if you want to cancel\n1 if you want to get a card from his hand");
                                        int i = 2;
                                        for (PlayingCard activeCard : Players[target].getActiveCards())
                                            System.out.println((i++) + " if you want to get his " + activeCard.getName());
                                        if(Players[target].isWeapon())
                                            System.out.println(i + " if you want to get his weapon");
                                        choice = input.nextInt();
                                        if(choice == 0)
                                            done = true;
                                        else if(choice == 1){
                                            done = true;
                                            turnPlayer.discard(card, DiscardPile);
                                            if(turnPlayerIx != target && !Players[target].getHand().isEmpty())
                                                panic(turnPlayerIx, target, Deck, DiscardPile);;
                                            turnPlayer.suzyLafayette(Deck, DiscardPile);
                                        }
                                        else if(choice < i && choice > 1){
                                            done = true;
                                            turnPlayer.discard(card, DiscardPile);
                                            if(target == turnPlayerIx && turnPlayer.getActiveCards().get(choice - 2).getName() == "Scope")
                                                scope(false);
                                            turnPlayer.getHand().add(Players[target].getActiveCards().remove(choice-2));
                                        }
                                        else if(choice == i){
                                            if(Players[target].isWeapon()){
                                                done = true;
                                                turnPlayer.discard(card, DiscardPile);
                                                turnPlayer.getHand().add(Players[target].removeWeapon());
                                            }
                                        }
                                        if(done){
                                            if(choice != 0)
                                                sidKetchum("", DiscardPile);
                                        }
                                        else
                                            System.out.println("Wrong input");
                                    }while(!done);
                                    break;
                                case "Wells Fargo":
                                    turnPlayer.discard(card, DiscardPile);
                                    for(int i = 0; i < 3; i++)
                                        turnPlayer.draw(Deck, DiscardPile);
                                    sidKetchum("", DiscardPile);
                                    break;
                                case "Stagecoach":
                                    turnPlayer.discard(card, DiscardPile);             
                                    for(int i = 0; i < 2; i++)
                                        turnPlayer.draw(Deck, DiscardPile);
                                    sidKetchum("", DiscardPile);
                                    break;
                                case "Scope":
                                    scope(true);
                                case "Barrel":
                                case "Dynamite":
                                case "Mustang":
                                    turnPlayer.getActiveCards().add(turnPlayer.getHand().remove(card));
                                    turnPlayer.suzyLafayette(Deck, DiscardPile);
                                    sidKetchum("", DiscardPile);
                                    break;
                                case "Jail":
                                    do{
                                        System.out.println(turnPlayer + " <Choose the player you want to put in jail or 0 to cancel");
                                        printActivePlayers();
                                        choice = input.nextInt() - 1;
                                        if(choice == sheriffIx)
                                            System.out.println("Impossible to put the Sheriff in jail");                                        
                                    }while(choice < -1 || choice >= nPlayers || choice == sheriffIx);
                                    if(choice != -1){
                                        Players[choice].getActiveCards().add(turnPlayer.getHand().remove(card));
                                        turnPlayer.suzyLafayette(Deck, DiscardPile);
                                        sidKetchum("", DiscardPile);
                                    }
                                    break;
                                case "Saloon":
                                    turnPlayer.discard(card, DiscardPile);
                                    for(int i = 0; i < nPlayers; i++)
                                        Players[i].addLife();
                                    turnPlayer.suzyLafayette(Deck, DiscardPile);
                                    sidKetchum("", DiscardPile);
                                    break;
                                case "Beer":
                                    turnPlayer.discard(card, DiscardPile);
                                    if(nPlayers > 2)
                                        turnPlayer.addLife();
                                    turnPlayer.suzyLafayette(Deck, DiscardPile);
                                    sidKetchum("", DiscardPile);
                                    break;
                                case "Gatling":
                                    turnPlayer.discard(card, DiscardPile, Deck);
                                    LinkedList<Integer> Hit = new LinkedList<Integer>();
                                    int Dead[] = gatling(Hit, Deck, DiscardPile);;
                                    if(Dead.length > 0)
                                        if(endMatch(nStartingPlayers, Dead)){
                                            match = false;
                                            continue skipTurn;
                                        }
                                    multipleDeaths(Dead, Hit, Deck, DiscardPile);;
                                    break;
                                case "Duel":
                                    do{
                                        System.out.println(turnPlayer + " <Choose a player you want to duel or 0 to cancel");
                                        printActivePlayers();
                                        choice = input.nextInt() - 1;
                                    }while(choice < -1 || choice >= nPlayers);
                                    if(choice != -1){
                                        turnPlayer.discard(card, DiscardPile);
                                        int res = duel(choice, Deck, DiscardPile);;
                                        if(res == 1){
                                            if(endMatch(nStartingPlayers, new int[]{turnPlayerIx})){
                                                match = false;
                                                continue skipTurn;
                                            }
                                            death(turnPlayerIx, turnPlayerIx, false, Deck, DiscardPile);;
                                            if(turnPlayerIx == nPlayers)
                                                turnPlayerIx = 0;
                                            continue skipTurn;   
                                        }
                                        else if(res == 2){
                                            if(endMatch(nStartingPlayers, new int[]{choice})){
                                                match = false;
                                                continue skipTurn;
                                            }
                                            death(turnPlayerIx, choice, false, Deck, DiscardPile);;
                                        }
                                    }
                                    break;
                                case "Indians!":
                                    turnPlayer.discard(card, DiscardPile);
                                    Hit = new LinkedList<Integer>();
                                    Dead = indians(Hit, DiscardPile);
                                    if(Dead.length > 0)
                                        if(endMatch(nStartingPlayers, Dead)){
                                            match = false;
                                            continue skipTurn;
                                        }
                                    multipleDeaths(Dead, Hit, Deck, DiscardPile);;
                                    break;
                                case "General Store":
                                    turnPlayer.discard(card, DiscardPile);
                                    generalStore(Deck, DiscardPile);;
                                    sidKetchum("", DiscardPile);
                                    break;
                                default:
                                    if(turnPlayer.isWeapon())
                                        turnPlayer.removeWeapon().discard(DiscardPile);
                                    turnPlayer.setWeapon((Weapon) (turnPlayer.getHand().remove(card)));
                                    turnPlayer.suzyLafayette(Deck, DiscardPile);
                                    sidKetchum("", DiscardPile);
                                    break;
                            }
                        }
                        else
                            System.err.println(turnPlayer + ": (You can't play this card");
                        break;
                    case 13:
                        goon = false;
                        break;
                    default:
                        System.out.println("Unavailable option.");
                }
            }
            turnPlayer.thirdPhase(DiscardPile, input);
            if(turnPlayerIx == nPlayers - 1)
                turnPlayerIx = 0;
            else
                turnPlayerIx++;
        }
    }

    public LinkedList<Player> getPlayers(){
        LinkedList<Player> P = new LinkedList<Player>();
        for(int i = 0; i < Players.length; i++)
            P.add(Players[i]);
        return P;
    }

    public int getNPlayers(){
        return Players.length;
    }
    
    public LinkedList<Player> getWinners(){
        return Winners;
    }
    
    private Characters[] createCharacters(){ 
        Characters[] Characters = new Characters[16];
        Characters[0] = new Characters("Jesse Jones", 4, "He may draw his first card from the hand of a player.");
        Characters[1] = new Characters("Black Jack", 4, "He shows the second card he draws. On heart or Diamonds, he draws one more card.");
        Characters[2] = new Characters("Rose Doolan", 4, "She sees all players at a distance decreased by 1.");
        Characters[3] = new Characters("El Gringo", 3, "Each time he is hit by a player, he draws a card from the hand of that player.");
        Characters[4] = new Characters("Bart Cassidy", 4, "Each time he is hit, he draws a card.");
        Characters[5] = new Characters("Lucky Duke", 4, "Each time he \"draws!\", he flips the top two cards and chooses one.");
        Characters[6] = new Characters("Sid Ketchum", 4, "He may discard 2 cards to regain one life point.");
        Characters[7] = new Characters("Suzy Lafayette", 4, "As soon as she has no cards in hand, she draws a card.");
        Characters[8] = new Characters("Vulture Sam", 4, "Whenever a player is eliminated from play, he takes in hand all the cards of that player.");
        Characters[9] = new Characters("Kit Carlson", 4, "He looks at the top three cards of the deck and chooses the 2 to draw.");
        Characters[10] = new Characters("Willy The Kid", 4, "He can play any number of BANG! cards.");
        Characters[11] = new Characters("Slab The Killer", 4, "Player needs 2 Missed! cards to cancel his BANG! card.");
        Characters[12] = new Characters("Pedro Ramirez", 4, "He may draw his first card from the discard pile.");
        Characters[13] = new Characters("Jourdonnais", 4, "Whenever he is the target of a BANG!, he may \"draw\": on a Heart, he is missed.");
        Characters[14] = new Characters("Calamity Janet", 4, "She can play BANG! cards as Missed! and vice versa.");
        Characters[15] = new Characters("Paul Regret", 3, "All players see him at a distance increased by 1.");
        return Characters;
    }

    private String[] drawRoles(){
        String[] availableRoles = {"Sheriff", "Renegade", "Outlaw", "Outlaw", "Deputy", "Outlaw", "Deputy"};
        String[] Roles = new String[nPlayers];
        int n = nPlayers - 1;
        for(int i = 0; i < n; i++){
            int pick = (int) (Math.random() * (nPlayers - i));
            Roles[i] = availableRoles[pick];
            availableRoles[pick] = availableRoles[n - i];
        }
        Roles[n] = availableRoles[0];
        return Roles;
    }

    private Characters[] drawCharacters(Characters[] availableCharacters){
        Characters[] Characters = new Characters[nPlayers];
        int n = availableCharacters.length - 1;
        for(int i = 0; i < nPlayers; i++){
            int pick = (int) (Math.random() * (availableCharacters.length - i));
            Characters[i] = availableCharacters[pick];
            availableCharacters[pick] = availableCharacters[n - i];
        }
        return Characters;
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
    
    private static LinkedList<PlayingCard> shuffle(PlayingCard[] PlayingCards){
        LinkedList<PlayingCard> Deck = new LinkedList<PlayingCard>();
        int n = PlayingCards.length - 1;
        for(int i = 0; i < n; i++){
            int pick = (int) (Math.random() * (PlayingCards.length - i));
            Deck.addFirst(PlayingCards[pick]);
            PlayingCards[pick] = PlayingCards[n - i];
        }
        Deck.addFirst(PlayingCards[0]);
        return Deck;
    }
    
    public static LinkedList<PlayingCard> discardIntoDeck(LinkedList<PlayingCard> DiscardPile){
        PlayingCard[] Deck = new PlayingCard[DiscardPile.size()];
        for(int i = 0; i < Deck.length; i++)
            Deck[i] = DiscardPile.remove();
        return shuffle(Deck);
    }
    
    private int findSheriff(){
            for(int i = 0; i < nPlayers; i++)
                if(Players[i].getRole() == "Sheriff"){
                    System.out.println(Players[i] + " is the Sheriff");
                    return i;
                }
            return -1;
        }

    private int findCharacter(String name){
            for(int i = 0; i < nPlayers; i++)
                if(Players[i].getCharacter().getName() == name)
                    return i;
            return -1;
        }

    private void readDiscard(LinkedList<PlayingCard> DiscardPile){
        if(DiscardPile.isEmpty())
            System.out.println("Discard pile is empty");            
        else
            System.out.println("First card in the discard pile: " + DiscardPile.getFirst());
    }

    private void sidKetchum(String message, LinkedList<PlayingCard> DiscardPile){
        if(sidKetchumIx >= 0 && Players[sidKetchumIx].getHand().size() >= 2){
            if(!message.isEmpty())
                System.out.println(message);
            boolean c;
            do{
                c = Players[sidKetchumIx].sidKetchum(DiscardPile, input);
            }while(c);
        }
    }

    private void printLives(int choice){
        if(choice == 0)
            for(int i = 0; i < nPlayers; i++)
                System.out.println(Players[i] + "'s lives: " + Players[i].getLives() + "/" + Players[i].getStartingLives());
        else{
            choice--;
            System.out.println(Players[choice] + "'s lives: " + Players[choice].getLives() + "/" + Players[choice].getStartingLives());
        }
    }

    private void printCharacters(int choice){
        if(choice == 0)
            for(int i = 0; i < nPlayers; i++)
                System.out.println(Players[i].getName() + "'s character: " + Players[i].getCharacter());
        else{
            choice--;
            System.out.println(Players[choice].getName() + "'s character: " + Players[choice].getCharacter());
        }
    }

    private void printNHands(int choice){
        if(choice == 0)
            for(int i = 0; i < nPlayers; i++)
                System.out.println(Players[i] + " has " + Players[i].getHand().size() + " cards in his hands");
        else
            System.out.println(Players[choice-1] + "has " + Players[choice-1].getHand().size() + " cards in his hands");
    }

    private void printActiveCards(int choice){
        if(choice == 0){
            for(int i = 0; i < nPlayers; i++){
                if(!Players[i].getActiveCards().isEmpty() || Players[i].isWeapon())
                    System.out.println(Players[i] + " active cards:\n" + Players[i].stringActiveCards());
                else
                    System.out.println(Players[i] + " has no active cards");
            }
        }
        else{
            Player p = Players[choice - 1];
            if(!p.getActiveCards().isEmpty() || p.isWeapon())
                System.out.println(p + " active cards:\n" + p.stringActiveCards());
            else
                System.out.println(p + " has no active cards");
        }
    }

    private void printRange(int choice){
        if(choice == 0)
            for(int i = 0; i < nPlayers; i++)
                System.out.println(Players[i] + "'s range is " + Players[i].getRange());
        else
            System.out.println(Players[choice-1] + "'s range is " + Players[choice-1].getRange());
    }

    private int[] distances(){
        int[] Distances = new int[nPlayers];
        Distances[turnPlayerIx] = 0;
        for(int i = 0; i < nPlayers; i++)
            if(i != turnPlayerIx){
                Distances[i] = Math.min(Math.min(Math.abs(turnPlayerIx - i), Math.abs(turnPlayerIx - i - nPlayers)), Math.abs(turnPlayerIx - i + nPlayers));
                if(Players[i].getCharacter().getName() == "Paul Regret")
                    Distances[i]++;
                if(Players[i].isActiveCard("Mustang"))
                    Distances[i]++;
                if(turnPlayer.getCharacter().getName() == "Rose Doolan")
                    Distances[i]--;
                if(turnPlayer.isActiveCard("Scope"))
                    Distances[i]--;
            }
        return Distances;
    }
    
    private void scope(boolean set){
        int x;
        if(set)
            x = 1;
        else
            x = -1;
        for(int i = 0; i < nPlayers; i++)
            if(i != turnPlayerIx)
                Distances[i] += x;
    }
    
    private void printDistances(int choice){
        if(choice == -1){
            for(int i = 0; i < nPlayers; i++)
                System.out.println((i+1) + ") " + Players[i] + "'s distance from you: " + Distances[i]);
        }
        else if(choice == 0){
            for(int i = 0; i < nPlayers; i++)
                if(i != turnPlayerIx)
                    System.out.println(Players[i] + "'s distance from you: " + Distances[i]);
        }
        else
            System.out.println(Players[choice - 1] + "'s distance from you: " + Distances[choice - 1]);
    }
    
    private void printActivePlayers(){
        for(int i = 0; i < nPlayers; i++)
            System.out.println((i+1) + ") " + Players[i]);
    }

    private boolean bang(int defender, LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        sidKetchum("", DiscardPile);
        int shot;
        if(turnPlayer.getCharacter().getName() == "Slab The Killer"){
            System.out.println(Players[defender] + " will need 2 Missed! for this Bang!");
            shot = 2;
        }
        else
            shot = 1;
        if(barrelEffect(Players[defender], Deck, DiscardPile))
            shot--;
        if(shot > 0)
            if(Players[defender].jourdonnais(input, Deck, DiscardPile))
                shot--;
        for(int i = 0; i < shot; i++){
            if(!missed(Players[defender], DiscardPile)){
                if(subLife(defender, turnPlayerIx, Deck, DiscardPile))
                    return true;
                i = shot;
            }
            else{
                boolean firstSid;
                if(defender >= turnPlayerIx && sidKetchumIx >= turnPlayerIx || defender < turnPlayerIx && sidKetchumIx < turnPlayerIx){
                    if(defender < sidKetchumIx)
                        firstSid = false;
                    else
                        firstSid = true;
                }
                else if(defender > sidKetchumIx)
                    firstSid = false;
                else
                    firstSid = true;
                if(firstSid){
                    sidKetchum("", DiscardPile);
                    Players[defender].suzyLafayette(Deck, DiscardPile);
                }
                else{
                    Players[defender].suzyLafayette(Deck, DiscardPile);
                    sidKetchum("", DiscardPile);
                }
            }
        }
        return false;
    }

    private boolean missed(Player defender, LinkedList<PlayingCard> DiscardPile){
        defender.readHand();
        boolean miss = false;
        int choice;
        do{
            System.out.print(defender + " <Choose a Missed!");
            if(defender.getCharacter().getName() == "Calamity Janet")
                System.out.print(" or a Bang!");
            System.out.println(" or 0 to lose a life");
            choice = input.nextInt() - 1;
            if(choice >= 0 && choice < defender.getHand().size()){
                miss = defender.getHand().get(choice).getName() == "Missed!";
                if(defender.getCharacter().getName() == "Calamity Janet")
                    miss |= defender.getHand().get(choice).getName() == "Bang!";
            }
            else if(choice == -1)
                miss = true;
        }while(!miss);
        if(choice == -1)
            return false;
        System.out.println("Missed!");
        defender.discard(choice, DiscardPile);
        return true;
    }
    
    private int duel(int target, LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        Player dueller = Players[target];
        int choice;
        while(true){
            boolean bang = false;
            do{
                dueller.readHand();
                System.out.print(dueller + " <Choose a Bang!");
                if(dueller.getCharacter().getName() == "Calamity Janet")
                    System.out.print(" or a Missed!");
                System.out.println(" to go on with the duel or 0 to lose a life");
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
                if(dueller.getCharacter().getName() == turnPlayer.getCharacter().getName()){
                    if(subLife(turnPlayerIx, turnPlayerIx, Deck, DiscardPile))
                        return 1;
                }
                else if(subLife(target, turnPlayerIx, Deck, DiscardPile))
                    return 2;
                return 0;
            }
            dueller.discard(choice, DiscardPile);
            if(dueller.getCharacter().getName() == Players[target].getCharacter().getName())
                dueller = turnPlayer;
            else
                dueller = Players[target];
        }
    }

    private int[] gatling(LinkedList<Integer> Hit, LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        for(int i = turnPlayerIx + 1; i != turnPlayerIx && !(i == nPlayers && turnPlayerIx == 0); i++){
            if(i == nPlayers)
                i = 0;
            if(barrelEffect(Players[i], Deck, DiscardPile))
                continue;
            if(Players[i].jourdonnais(input, Deck, DiscardPile))
                continue;
            if(!missed(Players[i], DiscardPile))
                Hit.add(i);
        }
        return multipleHits(Hit, DiscardPile);
    }
    
    private int[] indians(LinkedList<Integer> Hit, LinkedList<PlayingCard> DiscardPile){
        int choice;
        for(int i = turnPlayerIx + 1; i != turnPlayerIx && !(i == nPlayers && turnPlayerIx == 0); i++){
            if(i == nPlayers)
                i = 0;
            boolean bang = false;
            Player target = Players[i];
            do{
                target.readHand();
                System.out.print(target + " <Choose a Bang!"); 
                if(target.getCharacter().getName() == "Calamity Janet")
                    System.out.print(" or a Missed!");
                System.out.println(" or 0 to lose a life");
                choice = input.nextInt() - 1;
                if(choice >= 0 && choice < target.getHand().size()){
                    bang = target.getHand().get(choice).getName() == "Bang!";
                    if(target.getCharacter().getName() == "Calamity Janet")
                        bang |= target.getHand().get(choice).getName() == "Missed!";
                }
                if(choice == -1)
                    bang = true;
            }while(!bang);
            if(choice == -1)
                Hit.add(i);
            else
                target.discard(choice, DiscardPile);
        }
        return multipleHits(Hit, DiscardPile);
    }

    private int[] multipleHits(LinkedList<Integer> Hit, LinkedList<PlayingCard> DiscardPile){
        LinkedList<Integer> Dead = new LinkedList<Integer>();
        for (int i : Hit) {
            if(Players[i].getLives() == 1){
                if(!Players[i].savingBeer(input, DiscardPile, nPlayers == 2)){
                    Players[i].subLife();
                    Dead.add(i);
                }
            }
            else
                Players[i].subLife();
        }
        int D[] = new int[Dead.size()];
        for (int i = 0; i < D.length; i++)
            D[i] = Dead.removeFirst();
        return D;
    }

    private boolean barrelEffect(Player defender, LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        if(defender.isActiveCard("Barrel")){
            int choice;
            do{
                System.out.println(defender + " <Choose 1 to activate your Barrel or 0 to ignore");
                choice = input.nextInt();
            }while(choice < 0 || choice > 1);
            if(choice == 1){
                boolean res = defender.drawHearts(Deck, DiscardPile, input);
                if(res){
                    System.out.println("Missed!");
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean subLife(int wounded, int attacker, LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        //not damage from dynamite
        if(Players[wounded].getLives() == 1){
            if(!Players[wounded].savingBeer(input, DiscardPile, nPlayers == 2)){
                Players[wounded].subLife();
                return true;
            }
        }
        else
            Players[wounded].subLife();     
        turnPlayer.suzyLafayette(Deck, DiscardPile);
        boolean firstSid;
        if(wounded >= turnPlayerIx && sidKetchumIx >= turnPlayerIx || wounded < turnPlayerIx && sidKetchumIx < turnPlayerIx){
            if(wounded < turnPlayerIx)
                firstSid = false;
            else
                firstSid = true;
        }
        else if(wounded > turnPlayerIx)
            firstSid = false;
        else
            firstSid = true;
        if(firstSid){
            sidKetchum("", DiscardPile);
            if(!Players[attacker].getHand().isEmpty() && Players[wounded].getCharacter().getName() == "El Gringo" && attacker != wounded)
                panic(wounded, attacker, Deck, DiscardPile);
            else if(Players[wounded].getCharacter().getName() == "Bart Cassidy")
                Players[wounded].draw(Deck, DiscardPile);
            else
                Players[wounded].suzyLafayette(Deck, DiscardPile);
        }
        else{
            if(!Players[attacker].getHand().isEmpty() && Players[wounded].getCharacter().getName() == "El Gringo" && attacker != wounded)
                panic(wounded, attacker, Deck, DiscardPile);
            else if(Players[wounded].getCharacter().getName() == "Bart Cassidy")
                Players[wounded].draw(Deck, DiscardPile);
            else
                Players[wounded].suzyLafayette(Deck, DiscardPile);
            sidKetchum("", DiscardPile);
        }
        return false;
    }

    private void death(int killer, int killed, boolean multiple, LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        deathDiscard(killed, Deck, DiscardPile);;
        if(killer >= 0 && !multiple)
            penaltiesAndRewards(Players[killer], Players[killed], Deck, DiscardPile);;
        for(int i = killed + 1; i != nPlayers; i++){
            Player tmp = Players[i];
            Players[i] = Players[i-1];
            Players[i-1] = tmp;
        }
        nPlayers--;
        if(turnPlayerIx == nPlayers)
            turnPlayerIx = 0;
        else if(turnPlayerIx > killed){
            turnPlayerIx--;
            if(!multiple)
                Distances = distances();
        }
        else if(!multiple)
            Distances = distances();
        if(sheriffIx > killed)
            sheriffIx--;
        if(sidKetchumIx > killed)
            sidKetchumIx--;
        else if(sidKetchumIx == killed)
            sidKetchumIx = -1;
        if(!multiple){
            int i = turnPlayerIx;
            do{
                Players[i].suzyLafayette(Deck, DiscardPile);
                if(i == sidKetchumIx)
                    sidKetchum("", DiscardPile);
                if(i == nPlayers - 1)
                    i = 0;
                else
                    i++;
            }while(i != turnPlayerIx);
        }
    }

    private void multipleDeaths(int[] Dead, LinkedList<Integer> Hit, LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        Player[] Killed = new Player[Dead.length];
        for (int i = 0; i < Dead.length; i++) 
            Killed[i] = Players[Dead[i]];
        int i = turnPlayerIx;
        do{
            String character = Players[i].getCharacter().getName();
            if(Players[i].getLives() > 0){
                switch (character) {
                    case "Suzy Lafayette":
                        Players[i].suzyLafayette(Deck, DiscardPile);
                        break;
                    case "Sid Ketchum":
                        sidKetchum("", DiscardPile);
                        break;
                    case "El Gringo":
                        if(!turnPlayer.getHand().isEmpty())
                            if(Hit.contains(i))
                                panic(i, turnPlayerIx, Deck, DiscardPile);;
                        break;
                    case "Bart Cassidy":
                        if(Hit.contains(i))
                            Players[i].draw(Deck, DiscardPile);
                        break;
                    default:
                        break;
                }
                i++;
            }
            else
                death(turnPlayerIx, i, true, Deck, DiscardPile);;
            if(i == nPlayers)
                i = 0;
        }while(i != turnPlayerIx);
        for (Player k : Killed) 
            penaltiesAndRewards(turnPlayer, k, Deck, DiscardPile);;
        Distances = distances();
    }

    private void penaltiesAndRewards(Player killer, Player killed, LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        if(killer.getRole() == "Sheriff" && killed.getRole() == "Deputy")
            killer.discardAll(input, DiscardPile, Deck);
        else if(killed.getRole() == "Outlaw")
            for(int i = 0; i < 3; i++)
                killer.draw(Deck, DiscardPile);
    }
    
    private void deathDiscard(int killed, LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        int vulture = findCharacter("Vulture Sam");
        if(vulture >= 0 && vulture != killed){
            while (!Players[killed].getHand().isEmpty())
                Players[vulture].getHand().add(Players[killed].getHand().removeFirst());
            while(!Players[killed].getActiveCards().isEmpty())
                Players[vulture].getHand().add(Players[killed].getActiveCards().removeFirst());
            if(Players[killed].isWeapon())
                Players[vulture].getHand().add(Players[killed].removeWeapon());
        }
        else
            Players[killed].discardAll(input, DiscardPile, Deck);
    }
    
    private void panic(int robber, int robbed, LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        PlayingCard rand = Players[robbed].getHand().remove((int) Math.random() * Players[robbed].getHand().size());
        System.out.println(Players[robber] + ": (Drawn " + rand);
        System.out.println(Players[robbed] + ": (" + Players[robber] + " robbed your " + rand);
        Players[robber].getHand().add(rand);
        Players[robbed].suzyLafayette(Deck, DiscardPile);
    }

    private void generalStore(LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        LinkedList<PlayingCard> generalStore = new LinkedList<PlayingCard>();
        int storeSize = nPlayers;
        for(int i = 0; i < nPlayers; i++){
            if(Deck.isEmpty())
                Deck.addAll(discardIntoDeck(DiscardPile));
            generalStore.add(Deck.removeFirst());
        }
        for(int i = turnPlayerIx; storeSize > 1; i++, storeSize--){
            if(i == nPlayers)
                i = 0;
            Players[i].readHand();
            int choice;
            do{
                System.out.println("Cards available in the General Store:");
                for(int j = 0; j < storeSize; j++)
                    System.out.println((j+1) + ") " + generalStore.get(j));
                choice = input.nextInt() - 1;
            }while(choice < 0 || choice >= storeSize);
            Players[i].getHand().add(generalStore.remove(choice));
        }
        if(turnPlayerIx == 0)
            Players[nPlayers-1].getHand().add(generalStore.removeFirst());
        else
            Players[turnPlayerIx-1].getHand().add(generalStore.removeFirst());                            
    }

    private boolean endMatch(int startingNPlayers, int[] Dead){
        LinkedList<Integer> Living = new LinkedList<Integer>();
        for (int i : Dead)
            System.out.println(Players[i] + " was a " + Players[i].getRole());
        for(int i = 0; i < nPlayers; i++){
            boolean alive = true;
            for(int j = 0; j < Dead.length; j++)
                if(i == j){
                    alive = false;
                    j = Dead.length;
                }
            if(alive)
                Living.add(i);
        }
        if(!Living.contains(sheriffIx)){
            if(Living.size() == 1 && Players[Living.getFirst()].getRole() == "Renegade"){
                System.out.println(Players[Living.getFirst()] + "(" + Players[Living.getFirst()].getName() + ") has won the match as a Renegade");
                Winners.add(Players[Living.getFirst()]);
                return true;
            }
            outlawsWin(startingNPlayers);
            return true;
        }
        for (int i : Living)
            if(Players[i].getRole() != "Sheriff" && Players[i].getRole() != "Deputy")
                return false;
        sheriffWin(startingNPlayers);
        return true;
    }

    private void outlawsWin(int startingNPlayers){
        System.out.println("Outlaws have won the match.\nWinners:");
        for(int i = 0; i < startingNPlayers; i++)
            if(Players[i].getRole() == "Outlaw"){
                System.out.println(Players[i] + "(" + Players[i].getName() + ") has won the match as a Outlaw");
                Winners.add(Players[i]);
            }
    }

    private void sheriffWin(int startingNPlayers){
        System.out.println("Sheriff and his Deputies has won the match.\nWinners:");
        for(int i = 0; i < startingNPlayers; i++)
            if(Players[i].getRole() == "Sheriff" || Players[i].getRole() == "Deputy"){
                System.out.println(Players[i] + "(" + Players[i].getName() + ") has won the match as a " + Players[i].getRole());
                Winners.add(Players[i]);
            }
    }

    private boolean dynamite(LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        sidKetchum(turnPlayer + " drawing for Dynamite", DiscardPile);
        if(turnPlayer.getCharacter().getName() == "Lucky Duke"){
            PlayingCard[] tmp = new PlayingCard[2];
            int choice1, choice2;
            for(int i = 0; i < 2; i++){
                if(Deck.isEmpty())
                    Deck.addAll(discardIntoDeck(DiscardPile));
                tmp[i] = Deck.removeFirst();
                System.out.println((i+1) +") " + tmp[i]);
            }
            do{
                System.out.println("Lucky Duke: <Choose which one you want to use");
                choice1 = input.nextInt() - 1;
            }while (choice1 < 0 || choice1 > 1);
            do{
                System.out.println("Lucky Duke: <Choose which one you want to discard before");
                choice2= input.nextInt() - 1;
            }while (choice2 < 0 || choice2 > 1);
            if(choice2 == 0){
                tmp[0].discard(DiscardPile);
                tmp[1].discard(DiscardPile);
            }
            else{
                tmp[1].discard(DiscardPile);
                tmp[0].discard(DiscardPile);
            }
            if(tmp[choice1].getSuit() == 'S' && tmp[choice1].getRank() >= 2 && tmp[choice1].getRank() <= 9)
                return turnPlayer.explosion(Deck, DiscardPile, input, nPlayers == 2);
        }
        else{
            if(Deck.isEmpty())
                Deck.addAll(discardIntoDeck(DiscardPile));
            Deck.removeFirst().discard(DiscardPile);
            System.out.println(DiscardPile.getFirst() + " drawn for effect of dynamite");
            if(DiscardPile.getFirst().getSuit() == 'S' && DiscardPile.getFirst().getRank() >= 2 && DiscardPile.getFirst().getRank() <= 9)
                return turnPlayer.explosion(Deck, DiscardPile, input, nPlayers == 2);
        }
        for(int i = turnPlayerIx + 1; i != turnPlayerIx; i++){
            if(i == nPlayers)
                i = 0;
            if(!Players[i].isActiveCard("Dynamite")){
                Players[i].getActiveCards().add(turnPlayer.removeActiveCard("Dynamite"));
                i = turnPlayerIx - 1;
            }
            if(i == nPlayers - 1 && turnPlayerIx == 0)
                i = -1;
        }
        return false;
    }

    private boolean checkPlayable(String name, boolean bang){
        switch(name){
            case "Missed!":
                if(turnPlayer.getCharacter().getName() != "Calamity Janet")      //se falso va comunque nel caso Bang!
                    return false;                                   
            case "Bang!":
                if(!bang || turnPlayer.isUnlimitedBang())
                    return true;
                return false;
            case "Scope":
            case "Barrel":
            case "Dynamite":
            case "Mustang":
                if(turnPlayer.isActiveCard(name))
                    return false;
                return true;
            case "Jail":
                for(int i = 0; i < nPlayers; i++)
                    if(Players[i].getRole() != "Sheriff" && !Players[i].isActiveCard("Jail"))
                        return true;
            default:
                return true;
        }
    }
}
