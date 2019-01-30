import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import parser.*;
import parser.actions.*;
import trainer.*;
import util.Card;
import util.Debug;

/**
 * Simple example pokerbot, written in Java.
 * 
 * This is an example of a bare bones, pokerbot. It only sets up the socket
 * necessary to connect with the engine and then always returns the same action.
 * It is meant as an example of how a pokerbot should communicate with the
 * engine.
 * 
 */
public class Player extends Bot {
    private Random random;
    GameTree tree;

    public Player() throws IOException {
        tree = new GameTree();
        BufferedReader f = new BufferedReader(new FileReader("trainingData.txt"));
        String line;

        while ((line = f.readLine()) != null) {
            if (line.length() == 0) {
                break;
            }
            String[] pair = line.split(": ");
            String key = pair[0].substring(16, pair[0].length() - 1);
            String value = pair[1].substring(1, pair[1].length() - 1);

            String[] info = key.split(", ");
            HandInfo handInfo = new HandInfo(info[0].substring(34, info[0].length() - 1));
            int betSize = Integer.parseInt(info[1].substring(8));
            int player = Integer.parseInt(info[2].substring(7));
            int boardSize = Integer.parseInt(info[3].substring(10));
            boolean isExchange = info[4].endsWith("true");
            int allowedBetSize = Integer.parseInt(info[5].substring(15));
            InfoSet infoSet = new InfoSet(handInfo, betSize, player, boardSize, isExchange, allowedBetSize);

            String[] strat = value.split(", ");
            double[] avgStrat = new double[strat.length];
            for (int i = 0; i < strat.length; ++i) {
                avgStrat[i] = Double.parseDouble(strat[i]);
            }
            Node node = new Node(infoSet, avgStrat);

            tree.put(infoSet, node);
        }
        Debug.println(tree.size());

        random = new Random(12345);
    }

    // Called when a new game starts. Called exactly once.
    //
    // Arguments:
    // newGame: the parser.Game object.
    //
    // Returns:
    // Nothing.
    @Override
    public void handleNewGame(Game newGame) {}

    // Called when a new round starts. Called Game.num_rounds times.
    //
    // Arguments:
    // game: the parser.Game object.
    // newRound: the parser.Round object.
    //
    // Returns:
    // Nothing.
    @Override
    public void handleNewRound(Game game, Round newRound) {}

    // Called when a round ends. Called Game.num_rounds times.
    //
    // Arguments:
    // game: the parser.Game object.
    // round: the parser.Round object.
    // pot: the parser.Pot object.
    // cards: the cards you held when the round ended.
    // opponentCards: the cards your opponent held when the round ended, or null if they never showed.
    // boardCards: the cards on the board when the round ended.
    // result: "win", "loss" or "tie"
    // newBankroll: your total bankroll at the end of this round.
    // newOpponentBankroll: your opponent's total bankroll at the end of this round.
    // moveHistory: a list of moves that occurred during this round, earliest moves first.
    //
    // Returns:
    // Nothing.
    @Override
    public void handleRoundOver(
        Game game,
        Round round,
        Pot pot,
        String[] cards,
        String[] opponentCards,
        String[] boardCards,
        String result,
        int newBankroll,
        int newOpponentBankroll,
        String[] moveHistory
    ) {}

    // Where the magic happens - your code should implement this function.
    // Called any time the server needs an action from your bot.
    //
    // Arguments:
    // game: the parser.Game object.
    // round: the parser.Round object.
    // pot: the parser.Pot object.
    // cards: an array of your cards, in common format.
    // boardCards: an array of cards on the board. This list has length 0, 3, 4, or 5.
    // legalMoves: a set of the move classes that are legal to make.
    // moveHistory: a list of moves that have occurred during this round so far, earliest moves first.
    // timeLeft: a float of the number of seconds your bot has remaining in this match (not round).
    // minAmount: if BetAction or RaiseAction is valid, the smallest amount you can bet or raise to (i.e. the smallest you can increase your pip).
    // maxAmount: if BetAction or RaiseAction is valid, the largest amount you can bet or raise to (i.e. the largest you can increase your pip).
    //
    // Returns:
    // Your bot's action
    @Override
    public Action getAction(
        Game game,
        Round round,
        Pot pot,
        String[] cards,
        String[] boardCards,
        Set<Class<?>> legalMoves,
        String[] moveHistory,
        float timeLeft,
        int minAmount,
        int maxAmount
    ) {
//    	int cost = this.actionCost(pot, new CallAction());

        if (moveHistory[moveHistory.length - 1].startsWith("POST")) {
            Debug.println("AUTO CALL");
            return new CallAction();
        } else if (moveHistory[moveHistory.length - 1].startsWith("CALL") && moveHistory[moveHistory.length - 2].startsWith("POST")) {
            Debug.println("AUTO CHECK");
            return new CheckAction();
        }
//        ArrayList<Card> cards = gameState.getBoardCards();
//        Card[] hand = gameState.getHand();
        int[] playerCards = new int[cards.length + boardCards.length];
        for (int i = 0; i < boardCards.length; i++) {
            playerCards[i] = (new Card(boardCards[i])).getId();
        }

        for (int i = 0; i < cards.length; i++) {
            playerCards[boardCards.length + i] = (new Card(cards[i])).getId();
        }
        String[] stringCards = new String[playerCards.length];
        for (int i = 0; i < playerCards.length; ++i) {
            stringCards[i] = new Card(playerCards[i]).toString();
        }
        Debug.println("cards: " + Arrays.toString(stringCards));

        HandInfo handInfo = HandInfo.getHandInfo(playerCards);
        int amount = pot.getOpponentBets() - pot.getBets();
        int potSize = pot.getGrandTotal();

        Debug.println("history: " + Arrays.toString(moveHistory));
        int betSize = 1;
        int stackSize = 400;
        if (amount < 10) { // TODO: when we add more bet sizes, need to edit
            betSize = 0;
        } else if (stackSize - (pot.getOpponentTotal()) < amount) { // bet more than 1/2 remaining stack
            betSize = 2;
        }

        int allowed = 2;
        int dealIndex = dealIndex(moveHistory);
        int remaining = stackSize - pot.getTotal();
        Debug.println(remaining + " " + dealIndex + " " + amount);
        if (remaining == 0) {
            allowed = 0;
        } else if (dealIndex >= 0) {
            allowed = 1;
        }

//        GameTree tree = trainer.getTree();
        // don't want to get into exchange loop assume only exchange once per round of betting
        if (legalMoves.contains(ExchangeAction.class) && !canExchange(moveHistory)) {
            Debug.println(legalMoves);
            Debug.println("MY ACTION: " + (new CheckAction()).toString());
            return new CheckAction();
        }
        boolean isExchange = legalMoves.contains(ExchangeAction.class);
        InfoSet infoSet = new InfoSet(handInfo, betSize, round.getBigBlind() ? 0 : 1, boardCards.length, isExchange, allowed);
        Debug.println(infoSet);
        Node node = tree.get(infoSet);
        while (node == null) {
            handInfo = handInfo.lowerQuality();
            if (handInfo == null) { // lowest quality info set not in tree
                if (legalMoves.contains(CheckAction.class)) {
                    return new CheckAction();
                }
                if (amount < stackSize / 40) {
                    return new CallAction();
                } else {
                    return new FoldAction();
                }
            }
            infoSet = new InfoSet(infoSet, handInfo);
            node = tree.get(infoSet);
        }

        ArrayList<Act> validActions = infoSet.getValidActions();
        Debug.println(validActions);
        Action[] actions = new Action[validActions.size()];
        for (int i = 0; i < validActions.size(); ++i) {
            Act action = validActions.get(i);
            int actionAmount = action.getAmount();
            switch(action.getMove()) {
                case EXCHANGE:
                    actions[i] = new ExchangeAction();
                    break;
                case CHECK:
                    if (!isExchange && betSize == 0 && amount != 0) {
                        actions[i] = new CallAction();
                    } else {
                        actions[i] = new CheckAction();
                    }
                    break;
                case CALL:
                    actions[i] = new CallAction();
                    break;
                case BET:
                    String lastMove = moveHistory[moveHistory.length - 1];
                    if (!lastMove.startsWith("RAISE") && !lastMove.startsWith("BET")) {
                        if (actionAmount == 1) {
                            actions[i] = new BetAction(Math.max(Math.min(potSize, maxAmount), minAmount));
                        } else if (actionAmount == 2) {
                            actions[i] = new BetAction(maxAmount);
                        }
                    } else {
                        if (actionAmount == 2) {
                            actions[i] = new RaiseAction(maxAmount);
                        } else if (actionAmount == 1) {
                            if (dealIndex >= 0) {
                                actions[i] = new RaiseAction(Math.max(Math.min(potSize, maxAmount), minAmount));
                            } else {
                                actions[i] = new CallAction();
                            }
                        }
                    }
                    break;
                case FOLD:
                    actions[i] = new FoldAction();
                    break;
            }
        }

        Debug.println(Arrays.toString(actions));
        Debug.println(node);
        double[] strategy = node.getAverageStrategy();
        Debug.println(Arrays.toString(strategy));
        double probability = random.nextDouble();
        double sum = 0;
        int index = 0;
        while (probability > sum) {
            sum += strategy[index];
            index++;
        }
        Debug.println(legalMoves);
        Debug.println("MY ACTION: " + actions[index - 1].toString());
        return actions[index-1];
    }

    public boolean canExchange(String[] moveHistory) {
        for (int i = moveHistory.length - 1; i >= 0; --i) {
            String action = moveHistory[i];
            if (action.startsWith("DEAL")) {
                return true;
            }
            if ((moveHistory.length - i) % 2 == 0 && action.startsWith("EXCHANGE")) {
                return false;
            }
        }

        return true;
    }

    public int dealIndex(String[] moveHistory) {
        for (int i = moveHistory.length - 1; i >= 0; --i) {
            if (moveHistory[i].startsWith("DEAL")) {
                if (moveHistory.length - i <= 2 * TrainingState.getMaxTurns() + 1) {
                    return 0;
                } else {
                    return 1;
                }
            }
        }
        return -1;
    }
}
