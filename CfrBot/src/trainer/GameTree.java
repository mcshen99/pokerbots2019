package trainer;

import java.util.HashMap;

public class GameTree {
    private HashMap<InfoSet, Node> nodeMap;

    public GameTree() {
        nodeMap = new HashMap<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
//        sb.append(nodeMap.size()).append("\n");
        for (Node n : nodeMap.values()) {
            sb.append(n).append("\n");
        }

        return sb.toString();
    }

    public Node get(InfoSet infoSet) {
        return nodeMap.get(infoSet);
    }

    public void put(InfoSet infoSet, Node n) {
        nodeMap.put(infoSet, n);
    }

    public int size() {
        return nodeMap.size();
    }
}