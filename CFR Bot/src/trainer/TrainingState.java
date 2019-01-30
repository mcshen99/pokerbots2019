package trainer;

import parser.actions.BetAction;
import util.HandUtil;

import java.util.Arrays;

public class TrainingState implements Cloneable {
    private static final int MAX_BOARD_CARDS = 5;
    private static final int HAND_SIZE = 2;
    private static final int MAX_TURNS = 2;
    private int[] cards;
    private int boardSize;
    private boolean isExchange;
    private int player;
    private int[] cardIndex;
    private int[] potSizes;
    private int stackSize;
    private int minRaise;
    private int[] betSizes;
    private int winner; // -1 if no winner
    private int numTurns;

    public TrainingState(int[] cards, int[] potSizes, int stackSize) {
        this(cards, potSizes, stackSize, new int[]{0, 0});
    }

    public TrainingState(int[] cards, int[] potSizes, int stackSize, int[] betSizes) {
        this.cards = cards;
        boardSize = 0;
        isExchange = false;
        player = 0;
        cardIndex = new int[]{5, 9};
        this.potSizes = potSizes;
        this.stackSize = stackSize;
        minRaise = Trainer.BIG_BLIND;
        this.betSizes = betSizes;
        winner = -1;
        numTurns = 0;
    }

    @Override
    public String toString() {
        return "trainer.TrainingState{" +
                "cards=" + Arrays.toString(cards) +
                ", boardSize=" + boardSize +
                ", isExchange=" + isExchange +
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
        System.arraycopy(cards, cardIndex[p], playerCards, boardSize, HAND_SIZE);

        return new InfoSet(HandInfo.getHandInfo(playerCards), simplifyBetSize(1-player), player,
                boardSize, isExchange, getAllowedBetSize());
    }

    public TrainingState makeMove(Act act) {
        Act.Move move = act.getMove();
        TrainingState state = new TrainingState(cards, potSizes.clone(), stackSize);
        state.boardSize = boardSize;
        state.cardIndex = new int[]{cardIndex[0], cardIndex[1]};
        switch (move) {
            case EXCHANGE: // assume can only exchange once
                state.cardIndex[player] = cardIndex[player] + 2;
                if (player == 0) {
                    state.isExchange = true;
                }
                state.player = 1 - player;
                break;
            case CHECK:
                if (isExchange) {
                    if (player == 0) {
                        state.isExchange = true;
                    }
                } else if (player == 1) {
                    state.numTurns = numTurns + 1;
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
                        state.isExchange = true;
                    }
                }
                state.player = 1 - player;
                break;
            case CALL:
                state.potSizes[player] = potSizes[1 - player];
                state.numTurns = numTurns + 1;
                if (boardSize == 0) {
                    if (player == 0 && potSizes[1] == Trainer.BIG_BLIND) { // very beginning of game
                        state.player = 1 - player;
                    } else {
                        state.boardSize = 3;
                        state.player = 0;
                        state.isExchange = true;
                    }
                } else if (boardSize == 3 || boardSize == 4) {
                    state.boardSize += 1;
                    state.player = 0;
                    if (boardSize == 3) {
                        state.isExchange = true;
                    }
                } else {
                    state.winner = getWinner();
                    state.player = 1 - player;
                }
                break;
            case BET:
                state.betSizes[player] = toRealBetSize(act.getAmount(), player);
                state.minRaise = state.betSizes[player];
                state.potSizes[player] = state.potSizes[1 - player] + state.betSizes[player];
                state.player = 1 - player;
                state.numTurns = numTurns + 1;
                break;
            case FOLD:
                state.winner = 1 - player;
                state.player = 1 - player;
                state.numTurns = numTurns + 1;
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


    public int toRealBetSize(int betSize, int player) { //3 is all, 2 is a lot, 1 is a little, 0 is (basically) none
        int potSize = potSizes[0] + potSizes[1];
        int remaining = stackSize - potSizes[1-player];

        if (betSize == 3) {
            return remaining;
        } else if (betSize == 0) {
            return 0;
        } else if (betSize == 1) {
            return potSize * 2 / 3;
        } else { //betSize = 2
            return Math.min(Math.max(potSize, stackSize / 2 - potSizes[1-player]), remaining);
        }
    }

    // From the real bet size, simplify to 0 for 0, 1 for 2/3, 2 for pot or 1/2 stack, 3 for all in
    public int simplifyBetSize(int player) {
        int betSize = betSizes[player]; //how much the player has ACTUALLY bet overall

        if (betSize != 0 && stackSize == potSizes[player]) {
            return 3;
        }

        if (betSize == 0) {
            return 0;
        }

        if (betSize == (potSizes[0] + potSizes[1]) * 2 / 3) {
            return 1;
        }
        return 2;
    }

    public int getAllowedBetSize() {
        int remaining = stackSize - potSizes[1-player];
        if (remaining == 0) {
            return 0;
        }

        if (numTurns < MAX_TURNS) { // have not reraised too many times
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

    public static int getMaxBoardCards() {
        return MAX_BOARD_CARDS;
    }

    public static int getMaxTurns() {
        return MAX_TURNS;
    }
}