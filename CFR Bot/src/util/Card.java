package util;

import java.util.HashMap;
import java.util.Set;

public class Card {
    private static final HashMap<String, Integer> CARD_TO_ID = getCardToId();
    private static final HashMap<Integer, String> ID_TO_CARD = getIdToCard();

    private int id;

    private static HashMap<String, Integer> getCardToId(){
        String[] suits = { "h", "s", "c", "d" };
        String[] values = { "2", "3", "4", "5", "6", "7",
                "8", "9", "T", "J", "Q", "K", "A" };

        HashMap<String, Integer> cardToId = new HashMap<>();
        for (int i = 0; i < values.length; i++){
            for (int j = 0; j < suits.length; j++){
                cardToId.put(values[i] + suits[j], i*4 + j);
            }
        }
        return cardToId;
    }

    private static HashMap<Integer, String> getIdToCard(){
        String[] suits = { "h", "s", "c", "d" };
        String[] values = { "2", "3", "4", "5", "6", "7",
                "8", "9", "T", "J", "Q", "K", "A" };

        HashMap<Integer, String> cardToId = new HashMap<>();
        for (int i = 0; i < values.length; i++){
            for (int j = 0; j < suits.length; j++){
                cardToId.put(i*4 + j, values[i] + suits[j]);
            }
        }
        return cardToId;
    }

    public static Set<String> getCardSet() {
        return CARD_TO_ID.keySet();
    }

    public static boolean isCard(String cardId) {
        return CARD_TO_ID.containsKey(cardId);
    }

    public Card(int id) {
        this.id = id;
    }

    public Card(String card){
        try {
            if (CARD_TO_ID.containsKey(card)) {
                this.id = CARD_TO_ID.get(card);
            } else {
                throw new Exception("Specified card ID is not valid");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return ID_TO_CARD.get(id);
    }

    // Return a number between 0 and 51 as a string
    public int getId() {
        return id;
    }

    public int getSuit() {
        return id % 4;
    }

    public static int getSuit(int id) {
        return id % 4;
    }

    public int getNumber() {
        return id / 4;
    }

    public static int getNumber(int id) {
        return id / 4;
    }

    public static int toId(int number, int suit) {
        return number * 4 + suit;
    }
}
