package player;

import characters.Characters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import cards.*;

public class Player {
    private String name;
    private short lifes;
    private String role;
    private Characters character;
    private short range;
    private boolean unlimitedBang;
    private short startingLifes;
    private PlayingCard[] activeCards;
    private Weapon weapon;
    private ArrayList<PlayingCard> hand;
    private int handSize;    

    public Player(String role, Characters character, String name) {
        setName(name);
        setRole(role);
        setCharacter(character);
        if(role == "Sheriff")
            startingLifes++;
        setLifes(startingLifes);
        setWeapon(null);
        hand = new ArrayList<PlayingCard>();
        activeCards = new PlayingCard[5];
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }
    
    public short getLifes() {
        return this.lifes;
    }

    public void setLifes(short lifes) {
        this.lifes = lifes;
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
        setStartingLifes(character.getStartingLifes());
    }

    public short getRange() {
        return this.range;
    }

    public void setRange(short range) {
        this.range = range;
    }
    
    public boolean isUnlimitedBang() {
        return this.unlimitedBang;
    }

    public void setUnlimitedBang(boolean unlimitedBang) {
        this.unlimitedBang = unlimitedBang || (character.getName() == "Willy The Kid");
    }

    public short getStartingLifes() {
        return this.startingLifes;
    }

    public void setStartingLifes(short startingLifes) {
        this.startingLifes = startingLifes;
    }

    public PlayingCard[] getActiveCards(){
        return this.activeCards;
    }
    
    public Weapon getWeapon() {
        return this.weapon;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
        if(weapon == null){
            setRange((short) 1);
            setUnlimitedBang(false);
        }
        else{
            setRange(weapon.getRange());
            setUnlimitedBang(weapon.getUnlimitedBang());
        }
    }

    public Weapon removeWeapon(){
        Weapon w = getWeapon();
        setWeapon(null);
        setRange((short) 1);
        setUnlimitedBang(false);
        return w;
    }
    
    public PlayingCard getDynamite() {
        return this.activeCards[0];
    }

    public void setDynamite(PlayingCard dynamite) {
        this.activeCards[0] = dynamite;
    }

    public PlayingCard removeDynamite() {
        PlayingCard p = getDynamite();
        setDynamite(null);
        return p;
    }
    
    public PlayingCard getScope() {
        return this.activeCards[1];
    }

    public void setScope(PlayingCard scope) {
        this.activeCards[1] = scope;
    }

    public PlayingCard removeScope() {
        PlayingCard p = getScope();
        setScope(null);
        return p;
    }

    public PlayingCard getJail() {
        return this.activeCards[2];
    }

    public void setJail(PlayingCard jail) {
        this.activeCards[2] = jail;
    }

    public PlayingCard removeJail() {
        PlayingCard p = getJail();
        setJail(null);
        return p;
    }

    public PlayingCard getBarrel() {
        return this.activeCards[3];
    }

    public void setBarrel(PlayingCard barrel) {
        this.activeCards[3] = barrel;
    }

    public PlayingCard removeBarrel() {
        PlayingCard p = getBarrel();
        setBarrel(null);
        return p;
    }
    
    public PlayingCard getMustang() {
        return this.activeCards[4];
    }

    public void setMustang(PlayingCard mustang) {
        this.activeCards[4] = mustang;
    }

    public PlayingCard removeMustang() {
        PlayingCard p = getMustang();
        setMustang(null);
        return p;
    }

    public String stringActiveCards(){
        String s = new String();
        if(weapon != null)
            s += weapon + "\n";
        for(int i = 0; i < activeCards.length; i++ ){
            if(activeCards[i] != null)
                s += activeCards[i] + "\n";
        }
        return s;
    }

    public ArrayList<PlayingCard> getHand() {
        return this.hand;
    }

    public int getHandSize(){
        return handSize;
    }
    
    public void increaseHandSize(){
        handSize++; 
    }

    public void decreaseHandSize(){
        handSize--;
    }
    
    public void addLife(){
        if(lifes < startingLifes)
            lifes++;
    }
    
    public void draw(LinkedList<PlayingCard> deck){
        System.out.println(character.getName() + ": (Drawn: " + deck.getFirst());
        hand.add(deck.removeFirst());
        increaseHandSize();
    }

    public void discard(int card, LinkedList<PlayingCard> discardPile){
        getHand().remove(card).discard(discardPile);
        decreaseHandSize();
    }
    
    public void discard(PlayingCard card, LinkedList<PlayingCard> discardPile){
        card.discard(discardPile);
    }
    
    public void readHand(){
        int n = hand.size();
        if(n > 0){
            System.out.println(character.getName() + ": (Your hand:");
            for(int i = 0; i < n; i++)
                System.out.println((i+1) + ": " + hand.get(i));
        }
        else
            System.out.println(character.getName() + ": You don't have any card in your hand");
    }

    public void readRole(){
        System.out.println(character.getName() + ": (You are a " + role);
    }    
    
    public boolean sidKetchum(LinkedList<PlayingCard> discardPile, Scanner input){
        int length = hand.size();
        if(length >= 2){
            int choice;
            readHand();
            do{    
                System.out.println("Sid Ketchum: Insert 1 to activate your character's ability or 0 to ignore ");
                choice = input.nextInt();
            }while(choice < 0 || choice > 1);
            if(choice == 1){
                do{
                    System.out.println("Select a card to discard ");
                    choice = input.nextInt();
                }while(choice < 0 || choice >= length);
                discard(hand.remove(choice+1), discardPile);
                readHand();
                do{
                    System.out.println("Select a card to discard ");
                    choice = input.nextInt();
                }while(choice < 0 || choice >= length - 1);
                discard(hand.remove(choice+1), discardPile);
                addLife();
                return true;
            }
        }
        return false;
    }

    public void subLife(){
        lifes--;
    }

    public void discardAll(Scanner input, LinkedList<PlayingCard> discardPile){
        int j = 0, choice;
        while(handSize > 0 && stringActiveCards() != null){
            readHand();
            for(int i = 0; i < 5; i++){
                if(activeCards[i] != null){
                    System.out.println((j + handSize) + ": " + activeCards[i]);
                    j++;
                }
            }
            if(weapon != null){
                System.out.println((j + handSize) + ": " + weapon);
                j++;
            }
            do{
                System.out.println(this.toString() + " <Choose how to discard your cards");
                choice = input.nextInt() - 1;
            }while(choice < 0 || choice >= (handSize + j));
            if(choice < handSize)
                discard(choice, discardPile);
            else if(choice == handSize + j - 1)
                discard(weapon, discardPile);
            else{
                choice -= handSize;
                j = 0;
                for(int i = 0; i < 5; i++)
                    if(j == choice && activeCards[i] != null){
                        discard(activeCards[i], discardPile);
                        i = 5;
                    }
                    else if(activeCards[i] == null)
                        j++;
            }
        }
    }

    @Override
    public String toString(){
        return character.getName();
    }
}
