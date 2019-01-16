package pokerbots.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import pokerbots.parser.actions.*;
            
public class Runner {
    private Socket socket;
    private PrintWriter outStream;
    private BufferedReader inStream;
    private Bot bot;
    private Game currentGame;
    private Round currentRound;
    private String[] currentCards;
    private Pot currentPot;
    private ArrayList<String> moveHistory;

    private int minAmount;
    private int maxAmount;
    private String[] opponentCards;

    public Runner(Bot bot, Socket socket) throws IOException {
        this.socket = socket;
        this.outStream = new PrintWriter(socket.getOutputStream(), true);
        this.inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.bot = bot;
    }

    private void send(String out) {
        this.outStream.println(out);
    }

    public void run() throws IOException {
        String packet;
        while ((packet = this.inStream.readLine()) != null) {
            this.receive(packet);
        }
    }

    private void receive(String packet) {
        String[] command = packet.split(" ");

        String action = command[0];

        if (action.equals("NEWGAME")) {
            this.handleNewGame(command);
        } else if (action.equals("NEWHAND")) {
            this.handleNewHand(command);
        } else if (action.equals("EXCHANGE")) {
            this.handleExchange(command);
        } else if (action.equals("GETACTION")) {
            Action act = this.handleGetAction(command);
            this.handleAction(act);
        } else if (action.equals("HANDOVER")) {
            this.handleHandOver(command);
        } else if (action.equals("REQUESTKEYVALUES")) {
            this.send("FINISH");
        }
    }

    private void handleNewGame(String[] command) {
        this.currentGame = null;
        this.currentPot = null;
        this.currentRound = null;
        this.currentCards = null;
        this.moveHistory = null;

        this.currentGame = new Game(
            command[1],
            command[2],
            Integer.parseInt(command[3]),
            Integer.parseInt(command[4]),
            Integer.parseInt(command[5]),
            Float.parseFloat(command[6])
        );

        this.bot.handleNewGame(this.currentGame);
    }

    private void handleNewHand(String[] command) {
        int handNum = Integer.parseInt(command[1]);
        boolean bigBlind = command[2].equals("false");
        this.currentCards = command[3].split(",");
        int bankroll = Integer.parseInt(command[4]);
        int opponentBankroll = Integer.parseInt(command[5]);
        this.moveHistory = new ArrayList<String>();
        this.currentPot = new Pot(
            (bigBlind) ? this.currentGame.bigBlind : (this.currentGame.bigBlind / 2),
            0,
            0,
            (bigBlind) ? (this.currentGame.bigBlind / 2) : this.currentGame.bigBlind,
            0
        );
        this.currentRound = new Round(
            handNum, bankroll, opponentBankroll, bigBlind
        );

        this.bot.handleNewRound(this.currentGame, this.currentRound);
    }


    private void handleExchange(String[] command) {
        this.currentCards = command[2].split(",");
    }

    private Set<Class<?>> getLegalMoves(String legalMoveString) {
        this.minAmount = 0;
        this.maxAmount = 0;
        Set<Class<?>> result = new HashSet<Class<?>>();

        String[] legalMoveStrings = legalMoveString.split(";");

        for (String moveString : legalMoveStrings) {
            if (moveString.startsWith("CHECK")) {
                result.add(CheckAction.class);
            } else if (moveString.startsWith("CALL")) {
                result.add(CallAction.class);
            } else if (moveString.startsWith("FOLD")) {
                result.add(FoldAction.class);
            } else if (moveString.startsWith("EXCHANGE")) {
                result.add(ExchangeAction.class);
            } else if (moveString.startsWith("RAISE")) {
                result.add(RaiseAction.class);
            } else if (moveString.startsWith("BET")) {
                result.add(BetAction.class);
            }

            if (moveString.startsWith("RAISE") || moveString.startsWith("BET")) {
                String[] moveInfo = moveString.split(":");
                this.minAmount = Integer.parseInt(moveInfo[1]);
                this.maxAmount = Integer.parseInt(moveInfo[2]);
            }
        }

        return result;
    }

    private boolean checkMoveValidity(Action action, Set<Class<?>> legalMoves) {
        if (!legalMoves.contains(action.getClass())) {
            return false;
        }

        if (action instanceof RaiseAction) {
            RaiseAction ra = (RaiseAction) action;
            int amount = ra.getAmount();
            return amount >= this.minAmount && amount <= this.maxAmount;
        }

        if (action instanceof BetAction) {
            BetAction ra = (BetAction) action;
            int amount = ra.getAmount();
            return amount >= this.minAmount && amount <= this.maxAmount;
        }

        return true;
    }

    private void updatePot(String[] newMoves, int newPotTotal) {
        int pip = this.currentPot.getPip();
        int bets = this.currentPot.getBets();
        int numExchanges = this.currentPot.getNumExchanges();
        int opponentBets = this.currentPot.getOpponentBets();
        int opponentNumExchanges = this.currentPot.getOpponentNumExchanges();

        for (String move : newMoves) {
            if (move.startsWith("EXCHANGE")) {
                String[] moveInfo = move.split(":");
                if (moveInfo[moveInfo.length - 1] == this.currentGame.opponentName) {
                    opponentNumExchanges += 1;
                }
            } else if (move.startsWith("DEAL") || move.startsWith("WIN") || move.startsWith("TIE")) {
                bets += pip;
                pip = 0;
            }
        }

        opponentBets = newPotTotal - this.currentPot.getTotal() - ((1 << (opponentNumExchanges + 1)) - 2);

        this.currentPot = new Pot(pip, bets, numExchanges, opponentBets, opponentNumExchanges);
    }

    private String[] parseBoardCards(String boardString) {
        if (boardString.equals("None")) {
            return new String[0];
        } else {
            return boardString.split(",");
        }
    }

    private Action handleGetAction(String[] command) {
        int newPotTotal = Integer.parseInt(command[1]);
        String[] newMoves = command[5].split(";");
        this.updatePot(newMoves, newPotTotal);
        Collections.addAll(this.moveHistory, newMoves);

        String[] board = this.parseBoardCards(command[3]);

        Set<Class<?>> legalMoves = this.getLegalMoves(command[7]);

        float timeLeft = Float.parseFloat(command[8]);

        Action action = this.bot.getAction(
            this.currentGame,
            this.currentRound,
            this.currentPot,
            this.currentCards,
            board,
            legalMoves,
            this.moveHistory.toArray(new String[this.moveHistory.size()]),
            timeLeft,
            this.minAmount,
            this.maxAmount
        );

        if (!this.checkMoveValidity(action, legalMoves)) {
            System.out.println("Bot returned invalid move: " + action.toString());
            if (legalMoves.contains(FoldAction.class)) {
                action = new FoldAction();
            } else {
                action = new CheckAction();
            }
        }

        return action;
    }


    private void handleAction(Action action) {
        int pip = this.currentPot.getPip();
        int bets = this.currentPot.getBets();
        int numExchanges = this.currentPot.getNumExchanges();
        int opponentBets = this.currentPot.getOpponentBets();
        int opponentNumExchanges = this.currentPot.getOpponentNumExchanges();

        if (action instanceof CheckAction) {
            this.send("CHECK");
        } else if (action instanceof FoldAction) {
            this.send("FOLD");
        } else if (action instanceof ExchangeAction) {
            numExchanges += 1;
            this.send("EXCHANGE");
        } else if (action instanceof CallAction) {
            pip += Bot.actionCost(this.currentPot, action);
            this.send("CALL");
        } else if (action instanceof RaiseAction) {
            RaiseAction ra = (RaiseAction) action;
            pip += Bot.actionCost(this.currentPot, action);
            this.send(String.format("RAISE:%d", ra.getAmount()));
        } else if (action instanceof BetAction) {
            BetAction ba = (BetAction) action;
            pip += Bot.actionCost(this.currentPot, action);
            this.send(String.format("BET:%d", ba.getAmount()));
        }

        this.currentPot = new Pot(pip, bets, numExchanges, opponentBets, opponentNumExchanges);
    }

    private String getResult(String[] newMoves) {
        this.opponentCards = null;
        for (String move : newMoves) {
            if (move.startsWith("SHOW") && move.endsWith(this.currentGame.opponentName)) {
                this.opponentCards = Arrays.copyOfRange(move.split(":"), 1, 3);
            } else if (move.startsWith("TIE")) {
                return "tie";
            } else if (move.startsWith("WIN")) {
                return (move.endsWith(this.currentGame.name) ? "win" : "loss");
            }
        }
        return null;
    }

    private void handleHandOver(String[] command) {
        int bankroll = Integer.parseInt(command[1]);
        int opponentBankroll = Integer.parseInt(command[2]);
        String[] boardCards = this.parseBoardCards(command[4]);
        String[] newMoves = command[6].split(";");
        int newPotTotal = Integer.parseInt(command[7]);
        this.updatePot(newMoves, newPotTotal);
        String result = this.getResult(newMoves);
        Collections.addAll(this.moveHistory, newMoves);
        this.bot.handleRoundOver(
            this.currentGame,
            this.currentRound,
            this.currentPot,
            this.currentCards,
            this.opponentCards,
            boardCards,
            result,
            bankroll,
            opponentBankroll,
            this.moveHistory.toArray(new String[this.moveHistory.size()])
        );
    }

}
