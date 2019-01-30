package parser;


// The Game class represents a single match between two bots.
//
// name: your bot's name on the engine
// opponentName: the opponent's bot's name on the engine
// roundStack: the number of chips you have available at the start of every hand
// bigBlind: the size of the big blind
// numRounds: the number of hands that will be played during this match
// timeBank: the total amount of seconds your bot has to play this game.
public class Game {

   /** Property name */
   String name;

   /** Property opponentName */
   String opponentName;

   /** Property roundStack */
   int roundStack;

   /** Property bigBlind */
   int bigBlind;

   /** Property numHands */
   int numHands;

   /** Property timeBank */
   float timeBank;

   /**
    * Constructor
    */
   public Game(String name, String opponentName, int roundStack, int bigBlind, int numHands, float timeBank) {
      this.name = name;
      this.opponentName = opponentName;
      this.roundStack = roundStack;
      this.bigBlind = bigBlind;
      this.numHands = numHands;
      this.timeBank = timeBank;
   }

   /**
    * Gets the name
    */
   public String getName() {
      return this.name;
   }

   /**
    * Gets the opponentName
    */
   public String getOpponentName() {
      return this.opponentName;
   }

   /**
    * Gets the roundStack
    */
   public int getRoundStack() {
      return this.roundStack;
   }

   /**
    * Gets the bigBlind
    */
   public int getBigBlind() {
      return this.bigBlind;
   }

   /**
    * Gets the numHands
    */
   public int getNumHands() {
      return this.numHands;
   }

   /**
    * Gets the timeBank
    */
   public float getTimeBank() {
      return this.timeBank;
   }
}
