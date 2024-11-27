package player;

import java.util.LinkedList;
import java.util.Scanner;

import cards.*;
import characters.Characters;
import match.*;

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

    public Player(String role, Characters character, Scanner input, LinkedList<PlayingCard> Deck) {
        System.out.println("<Insert your name");
        setName(input.nextLine());
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
    }

    private void startingDraw(LinkedList<PlayingCard> Deck){
        for(int i = 0; i < lives; i++){
            System.out.println(character.getName() + ": (Drawn: " + Deck.getFirst());
            Hand.add(Deck.removeFirst());
        }
    }
    
    public void draw(LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        if(Deck.isEmpty())
            Deck.addAll(Match.discardIntoDeck(DiscardPile));
        System.out.println(character.getName() + ": (Drawn: " + Deck.getFirst());
        Hand.add(Deck.removeFirst());
    }

    public void discard(int card, LinkedList<PlayingCard> DiscardPile){
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

    public void suzyLafayette(LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        if(character.getName() == "Suzy Lafayette" && Hand.isEmpty())
            draw(Deck, DiscardPile);
    }
    
    public boolean jourdonnais(Scanner input, LinkedList<PlayingCard> Deck, LinkedList<PlayingCard> DiscardPile){
        if(character.getName() == "Jourdonnais"){
            int choice;
            do{
                System.out.println(this + " <Choose 1 to activate your character's ability or 0 to ignore");
                choice = input.nextInt();
            }while(choice < 0 || choice > 1);
            if(choice == 1)
                if(Match.drawHearts(Deck, DiscardPile)){
                    System.out.println("Missed!");
                    return true;
                }
        }
        return false;
    }
    
    public boolean savingBeer(Scanner input, LinkedList<PlayingCard> DiscardPile, boolean twoPlayers){
        System.out.println(this + ": You are losing your last life point");
        boolean goOn, done = false;
        do{
            goOn = sidKetchum(DiscardPile, input);
            done |= goOn;
        }while(goOn);
        if(done)
            return true;
        LinkedList<PlayingCard> Beers = new LinkedList<PlayingCard>();
        for(PlayingCard card : Hand)
            if(card.getName() == "Beer")
                Beers.add(card);
        int choice;
        if(!Beers.isEmpty()){
            int nBeers = Beers.size();
            do{
                System.out.println(this + " (Your beers:");
                for(int i = 0; i < nBeers; i++)
                    System.out.println((i+1) + ") " + Hand.get(i));
                System.out.println(this + " <Choose a beer or 0 to ignore");
                choice = input.nextInt() - 1;
            }while(choice < -1 || choice >= nBeers);
            if(choice != -1){
                this.discard(Beers.get(choice), DiscardPile);
                if(!twoPlayers)
                    return true;
            }
        }
        return false;
    }
    
    public void readHand(){
        if(Hand.isEmpty())
            System.out.println(character.getName() + ": You don't have any card in your Hand");
        else{
            System.out.println(character.getName() + ": (Your hand:");
            int i = 1;
            for(PlayingCard card : Hand)
                System.out.println((i++) + ": " + card);
        }
    }

    public void readRole(){
        System.out.println(character.getName() + ": (You are a " + role);
    }
    
    public boolean sidKetchum(LinkedList<PlayingCard> DiscardPile, Scanner input){
        int handSize = Hand.size();
        if(handSize >= 2){
            int choice;
            readHand();
            do{    
                System.out.println(this + ": <Insert 1 to activate your character's ability or 0 to ignore");
                choice = input.nextInt();
            }while(choice < 0 || choice > 1);
            if(choice == 1){
                readHand();
                do{
                    System.out.println("<Choose a card to discard");
                    choice = input.nextInt() - 1;
                }while(choice < 0 || choice >= handSize);
                discard(choice, DiscardPile);
                handSize--;
                readHand();
                do{
                    System.out.println("<Choose a card to discard");
                    choice = input.nextInt() - 1;
                }while(choice < 0 || choice >= handSize);
                discard(choice, DiscardPile);
                addLife();
                return true;
            }
        }
        return false;
    }

    public void discardAll(Scanner input, LinkedList<PlayingCard> DiscardPile, LinkedList<PlayingCard> Deck){
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
            System.out.println(this.toString() + " <Choose how to discard your cards ");
        int choice;
        for(i = 0; i < nTot; i++){
            do{
                choice = input.nextInt() - 1;
            }while(choice < 0 || choice >= nTot || ToDiscard[choice] == null);
            if(choice < handSize)
                Hand.remove(choice).discard(DiscardPile);
            else
                ActiveCards.remove(choice - handSize);
            ToDiscard[choice] = null;
        }
        if(lives > 0)
            suzyLafayette(Deck, DiscardPile);
    }

    @Override
    public String toString(){
        return name + "(" + character.getName() + ")";
    }
}
