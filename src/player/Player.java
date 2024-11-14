package player;

import java.util.LinkedList;
import java.util.Scanner;

import cards.*;
import characters.Characters;
import match.*;

public class Player {
    private String name;
    private int lives;
    private String role;
    private Characters character;
    private int range;
    private boolean unlimitedBang;
    private int startingLives;
    private LinkedList<PlayingCard> hand = new LinkedList<PlayingCard>();  
    private LinkedList<PlayingCard> activeCards = new LinkedList<PlayingCard>();
    private Weapon weapon;

    public Player(String role, Characters character, String name, LinkedList<PlayingCard> deck) {
        setName(name);
        setRole(role);
        setCharacter(character);
        setLives(startingLives);
        setWeapon(null);
        startingDraw(deck);
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
        setStartingLives(character.getStartingLives());
        if(role == "Sheriff")
            startingLives++;
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
        return this.activeCards;
    }

    public PlayingCard getActiveCard(String name){
        for (PlayingCard card : activeCards)
            if(card.getName() == name)
                return card;
        return null;
    }

    public boolean isActiveCard(String name){
        for (PlayingCard card : activeCards)
            if(card.getName() == name)
                return true;
        return false;
    }

    public PlayingCard removeActiveCard(String name){
        PlayingCard card = getActiveCard(name);
        activeCards.remove(card);
        return card;
    }

    public String stringActiveCards(){
        String s = new String();
        if(isWeapon())
            s += weapon + "\n";
        for (PlayingCard card : activeCards)
            s += card + "\n";
        return s;
    }

    public LinkedList<PlayingCard> getHand() {
        return this.hand;
    }
    
    public void addLife(){
        if(lives < startingLives)
            lives++;
    }

    private void startingDraw(LinkedList<PlayingCard> deck){
        for(int i = 0; i < lives; i++){
            System.out.println(character.getName() + ": (Drawn: " + deck.getFirst());
            hand.add(deck.removeFirst());
        }
    }
    
    public void draw(LinkedList<PlayingCard> deck, LinkedList<PlayingCard> discardPile){
        if(deck.isEmpty())
            deck = Match.noDeck(discardPile);
        System.out.println(character.getName() + ": (Drawn: " + deck.getFirst());
        hand.add(deck.removeFirst());
    }

    public void discard(int card, LinkedList<PlayingCard> discardPile){
        hand.remove(card).discard(discardPile);
    }

    public void discard(int card, LinkedList<PlayingCard> discardPile, LinkedList<PlayingCard> deck){
        hand.remove(card).discard(discardPile);
        suzyLafayette(deck, discardPile);
    }

    public void discard(PlayingCard card, LinkedList<PlayingCard> discardPile, LinkedList<PlayingCard> deck){
        if(getHand().remove(card))
            card.discard(discardPile);
        suzyLafayette(deck, discardPile);
    }

    public PlayingCard removeFromHand(int card, LinkedList<PlayingCard> deck, LinkedList<PlayingCard> discardPile){
        PlayingCard c = hand.remove(card);
        suzyLafayette(deck, discardPile);
        return c;
    }

    public boolean removeFromHand(PlayingCard card, LinkedList<PlayingCard> deck, LinkedList<PlayingCard> discardPile){
        boolean b = hand.remove(card);
        suzyLafayette(deck, discardPile);
        return b;
    }

    private void suzyLafayette(LinkedList<PlayingCard> deck, LinkedList<PlayingCard> discardPile){
        if(character.getName() == "Suzy Lafayette" && hand.isEmpty())
            draw(deck, discardPile);
    }
    
    public void readHand(){
        if(hand.isEmpty())
            System.out.println(character.getName() + ": You don't have any card in your hand");
        else{
            System.out.println(character.getName() + ": (Your hand:");
            int i = 1;
            for(PlayingCard card : hand)
                System.out.println((i++) + ": " + card);
        }
    }

    public void readRole(){
        System.out.println(character.getName() + ": (You are a " + role);
    }
    
    public boolean sidKetchum(LinkedList<PlayingCard> discardPile, Scanner input){
        int handSize = hand.size();
        if(handSize >= 2){
            int choice;
            readHand();
            do{    
                System.out.println("Sid Ketchum: Insert 1 to activate your character's ability or 0 to ignore ");
                choice = input.nextInt();
            }while(choice < 0 || choice > 1);
            if(choice == 1){
                readHand();
                do{
                    System.out.println("Select a card to discard ");
                    choice = input.nextInt() - 1;
                }while(choice < 0 || choice >= handSize);
                discard(choice, discardPile);
                handSize--;
                readHand();
                do{
                    System.out.println("Select a card to discard ");
                    choice = input.nextInt() - 1;
                }while(choice < 0 || choice >= handSize);
                discard(choice, discardPile);
                addLife();
                return true;
            }
        }
        return false;
    }

    public void subLife(){
        lives--;
    }

    public void discardAll(Scanner input, LinkedList<PlayingCard> discardPile, LinkedList<PlayingCard> deck){
        int handSize = hand.size();
        int nTot = handSize + activeCards.size();
        if(isWeapon())
            nTot++;
        PlayingCard toDiscard[] = new PlayingCard[nTot];
        int i = 0;
        for (PlayingCard card : hand)
            toDiscard[i++] = card;
        for (PlayingCard card : activeCards)
            toDiscard[i++] = card;
        if(isWeapon())
            toDiscard[i] = weapon;
        i = 1;
        for (PlayingCard card : toDiscard)
            System.out.println((i++) + ") " + card);
        if(nTot > 0)
            System.out.println(this.toString() + " <Choose how to discard your cards ");
        int choice;
        for(i = 0; i < nTot; i++){
            do{
                choice = input.nextInt() - 1;
            }while(choice < 0 || choice >= nTot || toDiscard[choice] == null);
            if(choice < handSize)
                hand.remove(choice).discard(discardPile);
            else
                activeCards.remove(choice - handSize);
            toDiscard[choice] = null;
        }
        if(lives > 0)
            suzyLafayette(deck, discardPile);
    }

    @Override
    public String toString(){
        return character.getName();
    }
}
