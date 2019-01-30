package trainer;

import java.util.ArrayList;
import java.util.Objects;

public class InfoSet {
    private HandInfo handInfo;
    private int betSize; // bet size is how much the other person SIMPLY bet, allowed bet size is how much i can SIMPLY bet
    private int player;
    private int boardSize;
    private boolean isExchange;
    private int allowedBetSize; // 0 - check, 12 - call, 1 - bet some, 12 - bet all

    public ArrayList<Act> getValidActions() {
        ArrayList<Act> acts = new ArrayList<>();
        if (isExchange) {
            acts.add(new Act(Act.Move.EXCHANGE, 0));
            acts.add(new Act(Act.Move.CHECK, 0));
            return acts;
        }

        if (betSize == 3) {
            acts.add(new Act(Act.Move.CALL, 0));
            acts.add(new Act(Act.Move.FOLD, 0));
            return acts;
        }

        if (betSize == 0) {
            if (allowedBetSize == 1) {
                acts.add(new Act(Act.Move.BET, 1));
                acts.add(new Act(Act.Move.BET, 2));
            }
            if (allowedBetSize > 0 && boardSize == TrainingState.getMaxBoardCards()) {
                acts.add(new Act(Act.Move.BET, 3));
            }
            acts.add(new Act(Act.Move.CHECK, 0));
            return acts;
        }

        acts.add(new Act(Act.Move.CALL, 0));
        if (boardSize == TrainingState.getMaxBoardCards()) {
            acts.add(new Act(Act.Move.BET, 3));
        }
        acts.add(new Act(Act.Move.FOLD, 0));
        if (allowedBetSize == 1) {
            acts.add(new Act(Act.Move.BET, 1));
            acts.add(new Act(Act.Move.BET, 2));
        }
        return acts;
    }

    public int getPlayer() {
        return player;
    }

    public InfoSet(HandInfo handInfo, int betSize, int player, int boardSize, boolean isExchange, int allowedBetSize) {
        this.handInfo = handInfo;
        this.betSize = betSize;
        this.player = player;
        this.boardSize = boardSize;
        this.isExchange = isExchange;
        this.allowedBetSize = allowedBetSize;
    }

    public InfoSet(InfoSet other, int allowedBetSize) {
        this(other.handInfo, other.betSize, other.player, other.boardSize, other.isExchange, allowedBetSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InfoSet infoSet = (InfoSet) o;
        return betSize == infoSet.betSize &&
                player == infoSet.player &&
                boardSize == infoSet.boardSize &&
                isExchange == infoSet.isExchange &&
                allowedBetSize == infoSet.allowedBetSize &&
                Objects.equals(handInfo, infoSet.handInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handInfo, betSize, player, boardSize, isExchange, allowedBetSize);
    }

    @Override
    public String toString() {
        return "trainer.InfoSet{" +
                "handInfo=" + handInfo +
                ", betSize=" + betSize +
                ", player=" + player +
                ", boardSize=" + boardSize +
                ", isExchange=" + isExchange +
                ", allowedBetSize=" + allowedBetSize +
                '}';
    }
}