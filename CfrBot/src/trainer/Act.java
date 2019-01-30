package trainer;

public class Act {
    public enum Move {
        EXCHANGE, CHECK, CALL, BET, FOLD
    }
    private Move move;
    private int amount; // amount of money

    public Act(Move m, int amount) {
        this.move = m;
        this.amount = amount;
    }

    public Move getMove() {
        return move;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Act{" +
                "move=" + move +
                ", amount=" + amount +
                '}';
    }
}
