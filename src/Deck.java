import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lawrencew on 11/25/2015.
 */
public class Deck {
    private List<Card> deck = new ArrayList<>();
    private int amount = 0;
    private Card cardBack;

    public Deck(int amount) {
        this.amount = amount;
        loadCards();
        shuffle();
    }

    public int getSize() {
        return deck.size();
    }

    public void removeCard(int position) {
        deck.remove(position);
    }
    public Card getTopCard()
    {
        Card returnCard = deck.get(0);
        deck.remove(0);
        return returnCard;
    }
    public Card checkCard(int position) {
        return deck.get(position);
    }

    public void add(Card card) {
        deck.add(card);
    }
    public Card getCardBack()
    {
        return cardBack;
    }

    public void shuffle() {
        for (int x = 0; x < deck.size() * 5; x++) {
            int spot1 = (int) (Math.random() * deck.size());
            int spot2 = (int) (Math.random() * deck.size());
            Card holder = deck.get(spot1);
            Card holder2 = deck.get(spot2);
            deck.set(spot1, holder2);
            deck.set(spot2, holder);
        }
    }

    private void loadCards() {

        for (int j = 0; j < amount; j++) {
            for (int k = 1; k < 14; k++) {
                deck.add(new Card("spade",k));
                deck.add(new Card("club", k));
                deck.add(new Card("heart", k));
                deck.add(new Card("diamond", k));
            }
        }
        cardBack = new Card("", 0);
    }
}