package cards;

import java.util.LinkedList;

public class PlayingCard {
    private String name;
    private char suit;
    private int rank;

    public PlayingCard(String name, char suit, int rank) {
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

    public int getRank() {
        return this.rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void discard(LinkedList<PlayingCard> discardPile){
        discardPile.addFirst(this);
    }
    
    @Override
    public String toString() {
        return getName() + " (" + getRank() + getSuit() + ")";
    }
}
