package player;

import java.util.LinkedList;

import cards.IPlayingCard;
import cards.IWeapon;
import characters.ICharacters;

public interface IPlayer {

    String getName();

    int getLives();

    String getRole();

    ICharacters getCharacter();

    int getRange();

    boolean isUnlimitedBang();

    int getStartingLives();

    LinkedList<IPlayingCard> getHand();

    IWeapon getWeapon();

    boolean isWeapon();

    void setWeapon(IWeapon weapon);

    IWeapon removeWeapon();

    LinkedList<IPlayingCard> getActiveCards();

    IPlayingCard getActiveCard(String name);

    boolean isActiveCard(String name);

    IPlayingCard removeActiveCard(String name);

    String stringActiveCards();

    void addLife();

    void subLife();

    void firstPhase(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile, int drawn);

    void draw(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile);

    void discard(int card, LinkedList<IPlayingCard> DiscardPile);

    void discard(int card, LinkedList<IPlayingCard> DiscardPile, LinkedList<IPlayingCard> Deck);

    void discard(IPlayingCard card, LinkedList<IPlayingCard> DiscardPile);

    void discard(IPlayingCard card, LinkedList<IPlayingCard> DiscardPile, LinkedList<IPlayingCard> Deck);

    IPlayingCard removeFromHand(int card, LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile);

    boolean removeFromHand(IPlayingCard card, LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile);

    boolean explosion(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile, boolean twoPlayers);

    boolean drawHearts(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile);

    void kitCarlson(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile);

    boolean pedroRamirez(LinkedList<IPlayingCard> DiscardPile);

    void suzyLafayette(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile);

    boolean jourdonnais(LinkedList<IPlayingCard> Deck, LinkedList<IPlayingCard> DiscardPile);

    boolean sidKetchum(LinkedList<IPlayingCard> DiscardPile);

    boolean savingBeer(LinkedList<IPlayingCard> DiscardPile, boolean twoPlayers);

    void thirdPhase(LinkedList<IPlayingCard> DiscardPile);

    void discardAll(LinkedList<IPlayingCard> DiscardPile, LinkedList<IPlayingCard> Deck);

    void autoDiscardAll(LinkedList<IPlayingCard> DiscardPile);

    void readHand();

    void readRole();

}