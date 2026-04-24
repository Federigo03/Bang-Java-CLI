package player;

import java.util.LinkedList;

import lib.*;
import cards.IPlayingCard;
import cards.IWeapon;
import characters.ICharacters;
import match.Match;

public class Player implements IPlayer {
    private String name;
    private int startingLives;
    private int lives;
    private String role;
    private ICharacters character;
    private int range;
    private boolean unlimitedBang;
    private LinkedList<IPlayingCard> Hand = new LinkedList<IPlayingCard>();  
    private LinkedList<IPlayingCard> ActiveCards = new LinkedList<IPlayingCard>();
    private IWeapon weapon;

    public Player(String role, ICharacters character, String name, LinkedList<IPlayingCard> Deck) {
        setName(name);
        setRole(role);
        setCharacter(character);
        System.out.println(name + " is " + character.getName());
        setLives(startingLives);
        setWeapon(null);
        startingDraw(Deck);
    }

    public Player(String role, ICharacters character, LinkedList<IPlayingCard> Deck) {
        setName(Utils.nextLine("<Insert your name"));
        setRole(role);
        setCharacter(character);
        System.out.println(name + " is " + character.getName());
        setLives(startingLives);
        setWeapon(null);
        startingDraw(Deck);
        readRole();
    }

    @Override
    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }
    
    @Override
    public int getLives() {
        return this.lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    @Override
    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public ICharacters getCharacter() {
        return this.character;
    }

    public void setCharacter(ICharacters character) {
        this.character = character;
        if(role == "Sheriff")
            setStartingLives(character.getStartingLives() + 1);
        else
            setStartingLives(character.getStartingLives());
    }

    @Override
    public int getRange() {
        return this.range;
    }

    public void setRange(int range) {
        this.range = range;
    }
    
    @Override
    public boolean isUnlimitedBang() {
        return this.unlimitedBang;
    }

    public void setUnlimitedBang(boolean unlimitedBang) {
        this.unlimitedBang = unlimitedBang || (character.getName() == "Willy The Kid");
    }

    @Override
    public int getStartingLives() {
        return this.startingLives;
    }

    public void setStartingLives(int startingLives) {
        this.startingLives = startingLives;
    }
    
    @Override
    public LinkedList<IPlayingCard> getHand() {
        return this.Hand;
    }
 
    @Override
    public IWeapon getWeapon() {
        return this.weapon;
    }

    @Override
    public boolean isWeapon(){
        return this.weapon != null;
    }

    @Override
    public void setWeapon(IWeapon weapon) {
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

    @Override
    public IWeapon removeWeapon(){
        IWeapon w = getWeapon();
        setWeapon(null);
        return w;
    }

    @Override
    public LinkedList<IPlayingCard> getActiveCards(){
        return this.ActiveCards;
    }

    @Override
    public IPlayingCard getActiveCard(String name){
        for (IPlayingCard card : ActiveCards)
            if(card.getName() == name)
                return card;
        return null;
    }

    @Override
    public boolean isActiveCard(String name){
        for (IPlayingCard card : ActiveCards)
            if(card.getName() == name)
                return true;
        return false;
    }

    @Override
    public IPlayingCard removeActiveCard(String name){
        IPlayingCard card = getActiveCard(name);
        ActiveCards.remove(card);
        return card;
    }

    @Override
    public String stringActiveCards(){
        String s = new String();
        if(isWeapon())
            s += "\t" + weapon + "\n";
        for (IPlayingCard card : ActiveCards)
            s += "\t" + card + "\n";
        return s.stripTrailing();
    }
    
    @Override
    public void addLife(){
        if(lives < startingLives){
            lives++;
            System.out.println(this + " gained a life");
        }
    }
    
    @Override
    public void subLife(){  
        lives--;
        System.out.println(this + " lost a life");
    }

    private void startingDraw(LinkedList<IPlayingCard> Deck){
        for(int i = 0; i < lives; i++){
            System.out.println(this + ": (Drawn: " + Deck.getFirst());
            Hand.add(Deck.removeFirst());
        }
    }
    
    @Override
    public void firstPhase(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile, int drawn){
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
    
    @Override
    public void draw(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile){
        if(Deck.isEmpty())
            Deck.addAll(Match.discardIntoDeck(DiscardPile));
        System.out.println(this + ": (Drawn: " + Deck.getFirst());
        Hand.add(Deck.removeFirst());
    }

    @Override
    public void     discard(int card, LinkedList<IPlayingCard> DiscardPile){
        Hand.remove(card).discard(DiscardPile);
    }

    @Override
    public void discard(int card, LinkedList<IPlayingCard> DiscardPile, LinkedList<IPlayingCard> Deck){
        Hand.remove(card).discard(DiscardPile);
        suzyLafayette(Deck, DiscardPile);
    }

    @Override
    public void discard(IPlayingCard card, LinkedList<IPlayingCard> DiscardPile){
        if(getHand().remove(card))
            card.discard(DiscardPile);
    }

    @Override
    public void discard(IPlayingCard card, LinkedList<IPlayingCard> DiscardPile, LinkedList<IPlayingCard> Deck){
        if(getHand().remove(card))
            card.discard(DiscardPile);
        suzyLafayette(Deck, DiscardPile);
    }

    @Override
    public IPlayingCard removeFromHand(int card, LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile){
        IPlayingCard c = Hand.remove(card);
        suzyLafayette(Deck, DiscardPile);
        return c;
    }

    @Override
    public boolean removeFromHand(IPlayingCard card, LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile){
        boolean b = Hand.remove(card);
        suzyLafayette(Deck, DiscardPile);
        return b;
    }

    @Override
    public boolean explosion(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile, boolean twoPlayers){
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
    
    @Override
    public boolean drawHearts(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile){
        if(character.getName() == "Lucky Duke"){
            IPlayingCard[] tmp = new IPlayingCard[2];
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
    
    @Override
    public void kitCarlson(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile){
        IPlayingCard[] t = new IPlayingCard[3];
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

    @Override
    public boolean pedroRamirez(LinkedList<IPlayingCard> DiscardPile){
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
    
    @Override
    public void suzyLafayette(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile){
        if(character.getName() == "Suzy Lafayette" && Hand.isEmpty())
            draw(Deck, DiscardPile);
    }
    
    @Override
    public boolean jourdonnais(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile){
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
    
    @Override
    public boolean sidKetchum(LinkedList<IPlayingCard> DiscardPile){
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
    
    @Override
    public boolean savingBeer(LinkedList<IPlayingCard> DiscardPile, boolean twoPlayers){
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
        LinkedList<IPlayingCard> Beers = new LinkedList<IPlayingCard>();
        for(IPlayingCard card : Hand)
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
    
    @Override
    public void thirdPhase(LinkedList<IPlayingCard> DiscardPile){
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
    
    @Override
    public void discardAll(LinkedList<IPlayingCard> DiscardPile, LinkedList<IPlayingCard> Deck){
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
            IPlayingCard ToDiscard[] = new IPlayingCard[nTot];
            int i = 0;
            for (IPlayingCard card : Hand)
                ToDiscard[i++] = card;
            for (IPlayingCard card : ActiveCards)
                ToDiscard[i++] = card;
            if(isWeapon())
                ToDiscard[i] = weapon;
            i = 1;
            for (IPlayingCard card : ToDiscard)
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

    @Override
    public void autoDiscardAll(LinkedList<IPlayingCard> DiscardPile){
        while(!Hand.isEmpty())
            Hand.pop().discard(DiscardPile);
        while(!ActiveCards.isEmpty())
            ActiveCards.pop().discard(DiscardPile);
        getWeapon().discard(DiscardPile);
    }
    
    @Override
    public void readHand(){
        if(Hand.isEmpty())
            System.out.println(this + ": You don't have any card in your hand");
        else{
            System.out.println(this + ": (Your hand:");
            int i = 1;
            for(IPlayingCard card : Hand)
                System.out.println((i++) + ": " + card);
        }
    }

    @Override
    public void readRole(){
        System.out.println(this + ": (You are a " + role);
    }
    
    @Override
    public String toString(){
        return name + "(" + character.getName() + ")";
    }
}
