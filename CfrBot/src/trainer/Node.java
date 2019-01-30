package trainer;

import java.util.Arrays;

public class Node {
    private int numActions;
    private InfoSet infoSet;
    private double[] regretSum;
    private double[] strategy;
    private double[] strategySum;
    private double[] averageStrategy;

    public Node(InfoSet infoSet) {
        this.infoSet = infoSet;
        numActions = infoSet.getValidActions().size();
        regretSum = new double[numActions];
        strategy = new double[numActions];
        strategySum = new double[numActions];
        averageStrategy = new double[0];
    }

    public Node(InfoSet infoset, double[] avgStrat) {
        this.infoSet = infoset;
        numActions = avgStrat.length;
        regretSum = new double[numActions];
        strategy = new double[numActions];
        strategySum = new double[numActions];
        this.averageStrategy = avgStrat;
    }

    public double[] getStrategy(double realizationWeight) {
        double normalizingSum = 0;
        for (int a = 0; a < numActions; a++) {
            strategy[a] = regretSum[a] > 0 ? regretSum[a] : 0;
            normalizingSum += strategy[a];
        }
        for (int a = 0; a < numActions; a++) {
            if (normalizingSum > 0)
                strategy[a] /= normalizingSum;
            else
                strategy[a] = 1.0 / numActions;
            strategySum[a] += realizationWeight * strategy[a];
        }
        return strategy;
    }

    public double[] getAverageStrategy() {
        if (averageStrategy.length != 0) {
            return averageStrategy;
        }
        double[] avgStrategy = new double[numActions];
        double normalizingSum = 0;
        for (int a = 0; a < numActions; a++)
            normalizingSum += strategySum[a];
        for (int a = 0; a < numActions; a++)
            if (normalizingSum > 0)
                avgStrategy[a] = strategySum[a] / normalizingSum;
            else
                avgStrategy[a] = 1.0 / numActions;
        return avgStrategy;
    }

    public String toString() {
        return String.format("%4s: %s", infoSet, Arrays.toString(getAverageStrategy()));
    }

    public int getNumActions() {
        return numActions;
    }

    public void updateRegretSum(int actionIndex, double value) {
        regretSum[actionIndex] += value;
    }
}