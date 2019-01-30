package parser.actions;

public class BetAction extends Action {
    private int amount;

    public BetAction(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return this.amount;
    }

    @Override
    public String toString() {
        return "BetAction{" +
                "amount=" + amount +
                '}';
    }
}
