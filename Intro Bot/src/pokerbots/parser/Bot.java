package pokerbots.parser;

import pokerbots.parser.actions.*;
import java.util.Set;

public abstract class Bot {

    // Called when a new game starts. Called exactly once.
    //
    // Arguments:
    // newGame: the pokerbots.parser.Game object.
    //
    // Returns:
    // Nothing.
    public abstract void handleNewGame(Game newGame);

    // Called when a new round starts. Called Game.num_rounds times.
    //
    // Arguments:
    // game: the pokerbots.parser.Game object.
    // newRound: the pokerbots.parser.Round object.
    //
    // Returns:
    // Nothing.
    public abstract void handleNewRound(Game game, Round newRound);

    // Called when a round ends. Called Game.num_rounds times.
    //
    // Arguments:
    // game: the pokerbots.parser.Game object.
    // round: the pokerbots.parser.Round object.
    // pot: the pokerbots.parser.Pot object.
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
    public abstract void handleRoundOver(
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
    );

    // Returns the cost of performing a specific action.
    public static int actionCost(Pot pot, Action action) {
        if (action instanceof CheckAction) {
            return 0;
        } else if (action instanceof FoldAction) {
            return 0;
        } else if (action instanceof ExchangeAction) {
            return 1 << (pot.getNumExchanges() + 1);
        } else if (action instanceof CallAction) {
            return pot.getOpponentBets() - pot.getBets() - pot.getPip();
        } else if (action instanceof RaiseAction) {
            RaiseAction ra = (RaiseAction) action;
            return ra.getAmount() - pot.getPip();
        } else if (action instanceof BetAction) {
            BetAction ba = (BetAction) action;
            return ba.getAmount() - pot.getPip();
        }
        return 0;
    }


    // Where the magic happens - your code should implement this function.
    // Called any time the server needs an action from your bot.
    //
    // Arguments:
    // game: the pokerbots.parser.Game object.
    // round: the pokerbots.parser.Round object.
    // pot: the pokerbots.parser.Pot object.
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
    public abstract Action getAction(
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
    );
}

