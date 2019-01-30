package trainer;

import util.Debug;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class Trainer {
    private static final Random random = new Random(1234);
    private static GameTree tree = new GameTree();
    public static final int BIG_BLIND = 2;
    public static final int STACK_SIZE = 400;
    public static final int[] INITIAL_POT_SIZES = new int[]{BIG_BLIND / 2, BIG_BLIND};

    public void train(int iterations) {
        int[] cards = new int[52];
        for (int i = 0; i < cards.length; i++) {
            cards[i] = i;
        }

        double util = 0;
        for (int i = 0; i < iterations; i++) {
            for (int c1 = cards.length - 1; c1 > 0; c1--) {
                int c2 = random.nextInt(c1 + 1);
                int tmp = cards[c1];
                cards[c1] = cards[c2];
                cards[c2] = tmp;
            }

            util += cfr(new TrainingState(cards.clone(), INITIAL_POT_SIZES, STACK_SIZE), 1, 1);
            if (i % 100 == 0) {
                System.out.println(i / 100);
            }
        }
        Debug.println("Average game value: " + util / iterations);

        Debug.println(tree.toString());
    }

    private double cfr(TrainingState state, double p0, double p1) {
        Double utility = state.getUtility();
        if (utility != null) {
            return utility;
        }
//        TODO: if h is a chance node then do stuff

        InfoSet infoSet = state.getInfoSet();
        Node node = tree.get(infoSet);
        if (node == null) {
            node = new Node(infoSet);
            tree.put(infoSet, node);
        }

        int player = infoSet.getPlayer();
        ArrayList<Act> validActs = infoSet.getValidActions();
        int numActions = node.getNumActions();
        double[] strategy = node.getStrategy(player == 0 ? p0 : p1);
        double[] util = new double[numActions];
        double nodeUtil = 0;
        for (int i = 0; i < numActions; ++i) {
            Act a = validActs.get(i);
            TrainingState nextState = state.makeMove(a);
            // update util based on which player it is
            util[i] = player == 0
                    ? cfr(nextState, p0 * strategy[i], p1)
                    : - cfr(nextState, p0, p1 * strategy[i]);
            nodeUtil += strategy[i] * util[i];
        }

        for (int i = 0; i < numActions; i++) {
            double regret = util[i] - nodeUtil;
            double value = (player == 0 ? p1 : p0) * regret;
            node.updateRegretSum(i, value);
        }

        // always return utility of player 0
        return player == 0 ? nodeUtil : -nodeUtil;
    }

    public GameTree getTree() {
        return tree;
    }

    public static void main(String[] args) throws IOException {
        int iterations = 5000;
        new Trainer().train(iterations);
        PrintWriter out = new PrintWriter(new FileWriter("trainingData.txt"));
        out.print(tree.toString());
        out.close();
    }

}