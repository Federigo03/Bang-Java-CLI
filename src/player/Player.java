package player;

import java.util.LinkedList;

import lib.*;
import cards.*;
import characters.Characters;
import match.Match;

public class Player {
    private String name;
    private int startingLives;
    private int lives;
    private String role;
    private Characters character;
    private int range;
    private boolean unlimitedBang;
    private LinkedList<PlayingCard> Hand = new LinkedList<PlayingCard>();  
    private LinkedList<PlayingCard> ActiveCards = new LinkedList<PlayingCard>();
    private Weapon weapon;

    public Player(String role, Characters character, String name, LinkedList<PlayingCard> Deck) {
        setName(name);
        setRole(role);
        setCharacter(character);
        System.out.println(name + " is " + character.getName());
        setLives(startingLives);
        setWeapon(null);
        startingDraw(Deck);
    }

    public Player(String role, Characters character, LinkedList<PlayingCard> Deck) {
        setName(Utils.nextLine("<Insert your name"));
        setRole(role);
        setCharacter(character);
        System.out.println(name + " is " + character.getName());
        setLives(startingLives);
        setWeapon(null);
        startingDraw(Deck);
        readRole();
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }
    
    public int getLives() {
        return this.lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Characters getCharacter() {
        return this.character;
    }

    public void setCharacter(Characters character) {
        this.character = character;
        if(role == "Sheriff")
            setStartingLives(character.getStartingLives() + 1);
        else
            setStartingLives(character.getStartingLives());
    }

    public int getRange() {
        return this.range;
    }

    public void setRange(int range) {
        this.range = range;
    }
    
    public boolean isUnlimitedBang() {
        return this.unlimitedBang;
    }

    public void setUnlimitedBang(boolean unlimitedBang) {
        this.unlimitedBang = unlimitedBang || (character.getName() == "Willy The Kid");
    }

    public int getStartingLives() {
        return this.startingLives;
    }

    public void setStartingLives(int startingLives) {
        this.startingLives = startingLives;
    }
    
    public LinkedList<PlayingCard> getHand() {
        return this.Hand;
    }
 
    public Weapon getWeapon() {
        return this.weapon;
    }

    public boolean isWeapon(){
        return this.weapon != null;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
        if(weapon == null){
            setRange(1);
            setUnlimitedBang(false);
        }
        else{
            setRange(weapon.getRange());
            setUnlimitedBang(weapon.isUnlimitedBang());
        }
    }

    public Weapon removeWeapon(){
        Weapon w = getWeapon();
        setWeapon(null);
        return w;
    }

    public LinkedList<PlayingCard> getActiveCards(){
        return this.ActiveCards;
    }

    public PlayingCard getActiveCard(String name){
        for (PlayingCard card : ActiveCards)
            if(card.getName() == name)
                return card;
        return null;
    }

    public boolean isActiveCard(String name){
        for (PlayingCard card : ActiveCards)
            if(card.getName() == name)
                return true;
        return false;
    }

    public PlayingCard removeActiveCard(String name){
        PlayingCard card = getActiveCard(name);
        ActiveCards.remove(card);
        return card;
    }

    public String stringActiveCards(){
        String s = new String();
        if(isWeapon())
            s += "\t" + weapon + "\n";
        for (PlayingCard card : ActiveCards)
            s += "\t" + card + "\n";
        return s.stripTrailing();
    }
    
    public void addLife(){
        if(lives < startingLives){
            lives++;
            System.out.println(this + " gained a life");
        }
    }
    
    public void subLife(){  
        lives--;
        System.out.println(this + " lost a life");
    }

    private void startingDraw(LinkedList<PlayingCard> Deck){
        for(int i = 0; i < lives; i++){
            System.out.println(this + ": (Drawn: " + Deck.getFirst());
            Hand.add(Deck.removeFirst());
        }
    }
    
    public void firstPhase(LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile, int drawn){
        for(; drawn < 2; drawn++){
            if(character.getName() == "Black Jack" && drawn == 1){
                if(Deck.isEmpty())
                    Deck = Match.discardIntoDeck(DiscardPile);
                System.out.println(this + " revealed " + Deck.getFirst());
                if(Deck.getFirst().getSuit() == '♥' || Deck.getFirst().getSuit() == '♦')
                    this.draw(Deck, DiscardPile);
            }
            this.draw(Deck, DiscardPile);
        }
    }
    
    public void draw(LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        if(Deck.isEmpty())
            Deck.addAll(Match.discardIntoDeck(DiscardPile));
        System.out.println(this + ": (Drawn: " + Deck.getFirst());
        Hand.add(Deck.removeFirst());
    }

    public void     discard(int card, LinkedList<PlayingCard> DiscardPile){
        Hand.remove(card).discard(DiscardPile);
    }

    public void discard(int card, LinkedList<PlayingCard> DiscardPile, LinkedList<PlayingCard> Deck){
        Hand.remove(card).discard(DiscardPile);
        suzyLafayette(Deck, DiscardPile);
    }

    public void discard(PlayingCard card, LinkedList<PlayingCard> DiscardPile){
        if(getHand().remove(card))
            card.discard(DiscardPile);
    }

    public void discard(PlayingCard card, LinkedList<PlayingCard> DiscardPile, LinkedList<PlayingCard> Deck){
        if(getHand().remove(card))
            card.discard(DiscardPile);
        suzyLafayette(Deck, DiscardPile);
    }

    public PlayingCard removeFromHand(int card, LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        PlayingCard c = Hand.remove(card);
        suzyLafayette(Deck, DiscardPile);
        return c;
    }

    public boolean removeFromHand(PlayingCard card, LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        boolean b = Hand.remove(card);
        suzyLafayette(Deck, DiscardPile);
        return b;
    }

    public boolean explosion(LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile, boolean twoPlayers){
        removeActiveCard("Dynamite").discard(DiscardPile);
        System.out.println("Dynamite exploded: " + this + " is going to lose 3 lives");
        for(int i = 0; i < 3; i++){
            if(lives == 1){
                if(!savingBeer(DiscardPile, twoPlayers)){
                    subLife();
                    return true;
                }
            }
            else
                subLife();
        }
        if(character.getName() == "Bart Cassidy")
            for(int i = 0; i < 3; i++)
                draw(Deck, DiscardPile);
        suzyLafayette(Deck, DiscardPile);
        if(character.getName() == "Sid Ketchum")
            sidKetchum(DiscardPile);
        return false;
    }
    
    public boolean drawHearts(LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        if(character.getName() == "Lucky Duke"){
            PlayingCard[] tmp = new PlayingCard[2];
            int choice1, choice2;
            for(int i = 0; i < 2; i++)
                if(Deck.isEmpty()){
                    Deck.addAll(Match.discardIntoDeck(DiscardPile));
                tmp[i] = Deck.removeFirst();
                System.out.println((i+1) + ") " + tmp[i]);
                }
            do{
                choice1 = Utils.nextInt(this + ": <Choose which one you want to use") - 1;
            }while (choice1 < 0 || choice1 > 1);
            do{
                choice2= Utils.nextInt(this + ": <Choose which one you want to discard before") - 1;
            }while (choice2 < 0 || choice2 > 1);
            if(choice2 == 0){
                tmp[0].discard(DiscardPile);
                tmp[1].discard(DiscardPile);
            }
            else{
                tmp[1].discard(DiscardPile);
                tmp[0].discard(DiscardPile);
            }
            if(tmp[choice1].getSuit() == '♥')
                return true;
            return false;
        }
        else{
            if(Deck.isEmpty())
                Deck.addAll(Match.discardIntoDeck(DiscardPile));
            Deck.removeFirst().discard(DiscardPile);
            System.out.println(DiscardPile.getFirst() + " \"drawn\" for effect");
            if(DiscardPile.getFirst().getSuit() == '♥')
                return true;
            return false;
        }
    }
    
    public void kitCarlson(LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        PlayingCard[] t = new PlayingCard[3];
        System.out.println(this + ": <Choose two of these");                    
        for(int j = 0; j < 3; j++){
            if(Deck.isEmpty())
                Deck = Match.discardIntoDeck(DiscardPile);
            t[j] = Deck.removeFirst();
            System.out.println((j+1) + ") " + t[j]);
        }
        int choice, x;
        do{
            choice = Utils.nextInt("") - 1;
        }while(choice < 0 || choice >= 3);
        do{
            x = Utils.nextInt("") - 1;
        }while (x < 0 || x >= 3 || x == choice);
        System.out.println(this + "(Drawn: " + t[choice]);
        System.out.println(this + "(Drawn: " + t[x]);
        this.getHand().add(t[choice]);
        this.getHand().add(t[x]);
        if(x + choice == 1)
            Deck.addFirst(t[2]);
        else if(x + choice == 2)
            Deck.addFirst(t[1]);
        else
            Deck.addFirst(t[0]);
    }

    public boolean pedroRamirez(LinkedList<PlayingCard> DiscardPile){
        int choice;
        do{
            choice = Utils.nextInt(this + ": <Insert 1 to activate your character's ability or 0 to ignore");
        }while(choice < 0 || choice > 1);
        if(choice == 1){
            if(!DiscardPile.isEmpty())
                this.draw(DiscardPile, null);
            return true;
        }
        return false;
    }
    
    public void suzyLafayette(LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        if(character.getName() == "Suzy Lafayette" && Hand.isEmpty())
            draw(Deck, DiscardPile);
    }
    
    public boolean jourdonnais(LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        if(character.getName() == "Jourdonnais"){
            int choice;
            do{
                choice = Utils.nextInt(this + " <Choose 1 to activate your character's ability or 0 to ignore");
            }while(choice < 0 || choice > 1);
            if(choice == 1)
                if(drawHearts(Deck, DiscardPile)){
                    System.out.println("Missed!");
                    return true;
                }
        }
        return false;
    }
    
    public boolean sidKetchum(LinkedList<PlayingCard> DiscardPile){
        int handSize = Hand.size();
        if(handSize >= 2){
            int choice;
            readHand();
            do{
                choice = Utils.nextInt(this + ": <Insert 1 to activate your character's ability or 0 to ignore");
            }while(choice < 0 || choice > 1);
            if(choice == 1){
                readHand();
                do{
                    choice = Utils.nextInt("<Choose a card to discard") - 1;
                }while(choice < 0 || choice >= handSize);
                discard(choice, DiscardPile);
                handSize--;
                readHand();
                do{
                    choice = Utils.nextInt("<Choose a card to discard") - 1;
                }while(choice < 0 || choice >= handSize);
                discard(choice, DiscardPile);
                addLife();
                return true;
            }
        }
        return false;
    }
    
    public boolean savingBeer(LinkedList<PlayingCard> DiscardPile, boolean twoPlayers){
        boolean goOn, done = false;
        if(character.getName() == "Sid Ketchum"){
            System.out.println(this + " is losing his last life point");
            do{
                goOn = sidKetchum(DiscardPile);
                done |= goOn;
            }while(goOn);
            if(done)
                return true;
        }
        LinkedList<PlayingCard> Beers = new LinkedList<PlayingCard>();
        for(PlayingCard card : Hand)
            if(card.getName() == "Beer")
                Beers.add(card);
        if(!Beers.isEmpty()){
            if(character.getName() != "Sid Ketchum")
                System.out.println(this + " is losing his last life point");
            int nBeers = Beers.size();
            int choice;
            do{
                System.out.println(this + " (Your beers:");
                for(int i = 0; i < nBeers; i++)
                    System.out.println((i+1) + ") " + Hand.get(i));
                choice = Utils.nextInt(this + " <Choose a beer or 0 to ignore") - 1;
            }while(choice < -1 || choice >= nBeers);
            if(choice != -1){
                this.discard(Beers.get(choice), DiscardPile);
                if(!twoPlayers)
                    return true;
            }
        }
        return false;
    }
    
    public void thirdPhase(LinkedList<PlayingCard> DiscardPile){
        int handSize = Hand.size(), choice;
        while(handSize > lives){
            System.out.println(this + " has to discard cards until those are the same number of his lives.");
            readHand();
            do{
                choice = Utils.nextInt(this + " <Choose a card to discard") - 1;
            }while(choice < 0 || choice >= handSize);
            discard(choice, DiscardPile);
            handSize--;
        }
    }
    
    public void discardAll(LinkedList<PlayingCard> DiscardPile, LinkedList<PlayingCard> Deck){
        int choice;
        do{
            choice = Utils.nextInt(this + ": <Insert 1 to discard your cards automatically or 0 to choose the order");
        }while(choice != 0 && choice != 1);
        if(choice == 1)
            autoDiscardAll(DiscardPile);
        else{
            int handSize = Hand.size();
            int nTot = handSize + ActiveCards.size();
            if(isWeapon())
                nTot++;
            PlayingCard ToDiscard[] = new PlayingCard[nTot];
            int i = 0;
            for (PlayingCard card : Hand)
                ToDiscard[i++] = card;
            for (PlayingCard card : ActiveCards)
                ToDiscard[i++] = card;
            if(isWeapon())
                ToDiscard[i] = weapon;
            i = 1;
            for (PlayingCard card : ToDiscard)
                System.out.println((i++) + ") " + card);
            if(nTot > 0)
                System.out.println(this + ": <Choose how to discard your cards ");
            
            for(i = 0; i < nTot; i++){
                do{
                    choice = Utils.nextInt("") - 1;
                }while(choice < 0 || choice >= nTot || ToDiscard[choice] == null);
                if(choice < handSize)
                    ToDiscard[choice].discard(DiscardPile);
                else
                    ToDiscard[choice - handSize].discard(DiscardPile);
                ToDiscard[choice] = null;
            }
            Hand = null;
            ActiveCards = null;
        }
        setWeapon(null);
        if(lives > 0)
            suzyLafayette(Deck, DiscardPile);
    }

    public void autoDiscardAll(LinkedList<PlayingCard> DiscardPile){
        while(!Hand.isEmpty())
            Hand.pop().discard(DiscardPile);
        while(!ActiveCards.isEmpty())
            ActiveCards.pop().discard(DiscardPile);
        getWeapon().discard(DiscardPile);
    }
    
    public void readHand(){
        if(Hand.isEmpty())
            System.out.println(this + ": You don't have any card in your hand");
        else{
            System.out.println(this + ": (Your hand:");
            int i = 1;
            for(PlayingCard card : Hand)
                System.out.println((i++) + ": " + card);
        }
    }

    public void readRole(){
        System.out.println(this + ": (You are a " + role);
    }
    
    @Override
    public String toString(){
        return name + "(" + character.getName() + ")";
    }
}
