package pokerbots.parser;
 

// The Round class represents a single round of the game above
// handNum: the round #. Ranges from 1 to Game.numRounds
// bankroll: the total amount you've gained or lost from the beginning of the game to the start of this round
// opponentBankroll: the total amount your opponent has gained or lost. Usually the negative of the above
// bigBlind: true if you had the big blind, false otherwise.
public class Round {
 
   /** Property handNum */
   int handNum;
 
   /** Property bankroll */
   int bankroll;
 
   /** Property opponentBankroll */
   int opponentBankroll;
 
   /** Property bigBlind */
   boolean bigBlind;
 
   /**
    * Constructor
    */
   public Round(int handNum, int bankroll, int opponentBankroll, boolean bigBlind) {
      this.handNum = handNum;
      this.bankroll = bankroll;
      this.opponentBankroll = opponentBankroll;
      this.bigBlind = bigBlind;
   }
 
   /**
    * Gets the handNum
    */
   public int getHandNum() {
      return this.handNum;
   }
 
   /**
    * Gets the bankroll
    */
   public int getBankroll() {
      return this.bankroll;
   }
 
   /**
    * Gets the opponentBankroll
    */
   public int getOpponentBankroll() {
      return this.opponentBankroll;
   }
 
   /**
    * Gets the bigBlind
    */
   public boolean getBigBlind() {
      return this.bigBlind;
   }
}
