package pokerbots.parser;
 

// The Pot class represents the amount of money in the pot
//
// pip: the amount you have added to the pot via betting during this betting round
// bets: the amount you have added to the pot via betting during previous rounds
// exchanges: the amount you have added to the pot via exchanges
// numExchanges: the number of times you have exchanged
// total: the total amount you have added to the pot
// opponentBets: the total amount your opponent has added to the pot via betting
// opponentExchanges: the total amount your opponent has added to the pot via exchanges
// opponentNumExchanges: the number of times your opponent has exchanged
// opponentTotal: the total amount your opponent has added to the pot
// grandTotal: the total size of the pot
public class Pot {
 
   /** Property pip */
   int pip;
 
   /** Property bets */
   int bets;
 
   /** Property numExchanges */
   int numExchanges;
 
   /** Property opponentBets */
   int opponentBets;
 
   /** Property opponentNumExchanges */
   int opponentNumExchanges;
 
   /**
    * Constructor
    */
   public Pot(int pip, int bets, int numExchanges, int opponentBets, int opponentNumExchanges) {
      this.pip = pip;
      this.bets = bets;
      this.numExchanges = numExchanges;
      this.opponentBets = opponentBets;
      this.opponentNumExchanges = opponentNumExchanges;
   }
 
   /**
    * Gets the pip
    */
   public int getPip() {
      return this.pip;
   }
 
   /**
    * Gets the bets
    */
   public int getBets() {
      return this.bets;
   }
 
   /**
    * Gets the exchanges
    */
   public int getExchanges() {
      return (1 << (this.numExchanges + 1)) - 2;
   }
 
   /**
    * Gets the numExchanges
    */
   public int getNumExchanges() {
      return this.numExchanges;
   }
 
   /**
    * Gets the total
    */
   public int getTotal() {
      return this.getPip() + this.getBets() + this.getExchanges();
   }
 
   /**
    * Gets the opponentBets
    */
   public int getOpponentBets() {
      return this.opponentBets;
   }
 
   /**
    * Gets the opponentExchanges
    */
   public int getOpponentExchanges() {
      return (1 << (this.opponentNumExchanges + 1)) - 2;
   }
 
   /**
    * Gets the opponentNumExchanges
    */
   public int getOpponentNumExchanges() {
      return this.opponentNumExchanges;
   }
 
   /**
    * Gets the opponentTotal
    */
   public int getOpponentTotal() {
      return this.getOpponentExchanges() + this.getOpponentBets();
   }
 
   /**
    * Gets the grandTotal
    */
   public int getGrandTotal() {
      return this.getTotal() + this.getOpponentTotal();
   }
}
