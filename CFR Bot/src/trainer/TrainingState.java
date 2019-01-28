package trainer;

import util.HandUtil;

import java.util.Arrays;

public class TrainingState implements Cloneable {
    private static final int MAX_BOARD_CARDS = 5;
    private static final int HAND_SIZE = 2;
    private int[] cards;
    private int boardSize;
    private int numExchange;
    private int player;
    private int[] cardIndex;
    private int[] potSizes;
    private int stackSize;
    private int minRaise;
    private int[] betSizes;
    private int winner; // -1 if no winner

    public TrainingState(int[] cards, int[] potSizes, int stackSize) {
        this(cards, potSizes, stackSize, new int[]{0, 0});
    }

    public TrainingState(int[] cards, int[] potSizes, int stackSize, int[] betSizes) {
        this.cards = cards;
        boardSize = 0;
        numExchange = 0;
        player = 0;
        cardIndex = new int[]{5, 9};
        this.potSizes = potSizes;
        this.stackSize = stackSize;
        minRaise = Trainer.BIG_BLIND;
        this.betSizes = betSizes;
        winner = -1;
    }

    @Override
    public String toString() {
        return "trainer.TrainingState{" +
                "cards=" + Arrays.toString(cards) +
                ", boardSize=" + boardSize +
                ", numExchange=" + numExchange +
                ", player=" + player +
                ", cardIndex=" + Arrays.toString(cardIndex) +
                ", potSizes=" + Arrays.toString(potSizes) +
                ", stackSize=" + stackSize +
                ", minRaise=" + minRaise +
                ", betSizes=" + Arrays.toString(betSizes) +
                ", winner=" + winner +
                '}';
    }

    public int getPlayer() {
        return player;
    }

    public InfoSet getInfoSet() {
        int[] playerCards = new int[boardSize + HAND_SIZE];
        if (boardSize >= 0) System.arraycopy(cards, 0, playerCards, 0, boardSize);

        int p = getPlayer();
        System.arraycopy(cards, cardIndex[p] + 0, playerCards, boardSize + 0, HAND_SIZE);

        return new InfoSet(HandInfo.getHandInfo(playerCards), simplifyBetSize(1-player), player,
                boardSize, (numExchange > 0), getAllowedBetSize());
    }

    public TrainingState makeMove(Act act) {
        Act.Move move = act.getMove();
        TrainingState state = new TrainingState(cards, potSizes.clone(), stackSize);
        state.boardSize = boardSize;
        state.cardIndex = new int[]{cardIndex[0], cardIndex[1]};
        switch (move) {
            case EXCHANGE: //assume can only exchange once
                state.cardIndex[player] = cardIndex[player] + 2;
                if (player == 0) {
                    state.numExchange = 1;
                }
                state.player = 1 - player;
                break;
            case CHECK:
                if (numExchange > 0) {
                    if (player == 0) {
                        state.numExchange = 1;
                    }
                } else if (player == 1) {
                    if (boardSize == 0) {
                        state.boardSize = 3;
                    } else if (boardSize == 3 || boardSize == 4) {
                        state.boardSize += 1;
                    } else {
                        state.winner = getWinner();
                        state.player = 1 - player;
                        break;
                    }

                    if (boardSize == 0 || boardSize == 3) {
                        state.numExchange = 1;
                    }
                }
                state.player = 1 - player;
                break;
            case CALL:
                state.potSizes[player] = potSizes[1 - player];
                if (boardSize == 0) {
                    if (player == 0 && potSizes[1] == Trainer.BIG_BLIND) { // very beginning of game
                        state.player = 1 - player;
                    } else {
                        state.boardSize = 3;
                        state.player = 0;
                        state.numExchange = 1;
                    }
                } else if (boardSize == 3 || boardSize == 4) {
                    state.boardSize += 1;
                    state.player = 0;
                    if (boardSize == 3) {
                        state.numExchange = 1;
                    }
                } else {
                    state.winner = getWinner();
                    state.player = 1 - player;
                }
                break;
            case RAISE:
                state.betSizes[player] = toRealBetSize(act.getAmount(), player);
                state.minRaise = state.betSizes[player];
                state.potSizes[player] = state.potSizes[1 - player] + state.betSizes[player];
                state.player = 1 - player;
                break;
            case BET:
                state.betSizes[player] = toRealBetSize(act.getAmount(), player);
                state.minRaise = state.betSizes[player];
                state.potSizes[player] += state.betSizes[player];
                state.player = 1 - player;
                break;
            case FOLD:
                state.winner = 1 - player;
                state.player = 1 - player;
                break;
        }
        assert state.betSizes[0] == 0 || state.betSizes[1] == 0;
        return state;
    }


    public Double getUtility() {
        if (winner != -1) {
            if (winner != 2) {
                double won = potSizes[1 - winner];
                return winner == 0 ? won : -won;
            }

            // tied
            return 0.0;
        }

        return null;
    }


    public int toRealBetSize(int betSize, int player) {
        int potSize = potSizes[0] + potSizes[1];
        int remaining = stackSize - potSizes[1-player];

        if (betSize == 2) {
            return remaining;
        }

        assert potSize / 2 >= minRaise && potSize / 2 < remaining;
        assert betSize == 1;
        return potSize / 2;
    }

    // From the real bet size, simplify to 0, 1, 2 for 0, 50% pot, all in
    public int simplifyBetSize(int player) {
        int betSize = betSizes[player];

        if (betSize != 0 && stackSize == potSizes[player]) {
            return 2;
        }

        if (betSize == 0) {
            return 0;
        }
        return 1;
    }

    public int getAllowedBetSize() {
        int remaining = stackSize - potSizes[1-player];
        if (remaining == 0) {
            return 0;
        }

        int potSize = potSizes[0] + potSizes[1];
        if (potSize / 2 >= minRaise && potSize / 2 < remaining) {
            return 1;
        }
        return 2;
    }

    public int getWinner() {
        int[] hand0 = new int[7];
        int[] hand1 = new int[7];
        for (int i = 0; i < MAX_BOARD_CARDS; i++) {
            hand0[i] = cards[i];
            hand1[i] = cards[i];
        }

        for (int j = 0; j < HAND_SIZE; j++) {
            hand0[MAX_BOARD_CARDS + j] = cards[cardIndex[0] + j];
            hand1[MAX_BOARD_CARDS + j] = cards[cardIndex[1] + j];
        }

        int compare = HandUtil.compareTo(hand0, hand1);
        if (compare == 0) {
            return 2;
        } else if (compare > 0) {
            return 0;
        } else {
            return 1;
        }
    }
}