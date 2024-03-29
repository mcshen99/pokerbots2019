import java.util.Set;
import parser.*;
import parser.actions.*;

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

	public Player() {}

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
    	int cost = this.actionCost(pot, new CallAction());

    	if (legalMoves.contains(CheckAction.class)) {
    		return new CheckAction();
    	} else {
    		return new CallAction();
    	}
    }
}
