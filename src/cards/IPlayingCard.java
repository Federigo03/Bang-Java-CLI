package cards;

import java.util.LinkedList;

public interface IPlayingCard {

    String getName();

    char getSuit();

    int getRank();

    String getStringRank();

    void discard(LinkedList<IPlayingCard> discardPile);

}