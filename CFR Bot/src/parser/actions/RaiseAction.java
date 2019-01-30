package parser.actions;

public class RaiseAction extends Action {
    private int amount;

    public RaiseAction(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return this.amount;
    }

    @Override
    public String toString() {
        return "RaiseAction{" +
                "amount=" + amount +
                '}';
    }
}
