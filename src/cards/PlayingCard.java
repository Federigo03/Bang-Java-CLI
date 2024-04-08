package cards;

import java.util.LinkedList;

public class PlayingCard {
    private String name;
    private char suit;
    private short rank;

    public PlayingCard(String name, char suit, short rank) {
        setName(name);
        setSuit(suit);
        setRank(rank);
    }
    
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char getSuit() {
        return this.suit;
    }

    public void setSuit(char suit) {
        this.suit = suit;
    }

    public short getRank() {
        return this.rank;
    }

    public void setRank(short rank) {
        this.rank = rank;
    }

    public void discard(LinkedList<PlayingCard> discardPile){
        discardPile.addFirst(this);
    }
    
    public String toString() {
        return getName() + " (" +getRank() + getSuit() + ")";
    }
}
