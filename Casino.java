import java.util.Random;
import java.util.Arrays;



/**
*    Lets play some bots against each other in simplified play
*/
public class Casino {
	public Random r;
	public HandEvaluator handEval;
	private Deck theDeck;
	public int numPlayers = 20;
	public int numRounds = 1000;
	public int numIterations = 1;
	public int annie = 50;
	public Player[] pool;
	//public Player[] table;
	public file logFile;
	public boolean debug = true;
	Bet bet = null;

	public Casino() {

		handEval = new HandEvaluator();
		logFile = new file("casino_log_1.txt");
		r=new Random();

		// first create some potential players
		pool = new Player[numPlayers];
		for (int i=0; i<numPlayers; i++) {
			pool[i] = new Player(i);
			pool[i].debug=debug;
			//pool[i].params.ranSet2(0.12f,8.0f,4.0f);
			if (i%5==0) pool[i].params.setConst(0.5f);
			if (i%5==1) pool[i].params.setConst(1);
			if (i%5==2) pool[i].params.setConst(2);
			if (i%5==3) pool[i].params.setConst(4);
			if (i%5==4) pool[i].params.setConst(8);
			pool[i].setBankroll(10000);
			pool[i].amountBought+=10000;
			pool[i].broke=false;
		}

		// keep track of our progress
		int delta = (int)(numRounds/100);

		// get the deck in order
		theDeck = new Deck();
		theDeck.shuffle();
		theDeck.shuffle();

		// we are going to have several tounaments, one at a time
		for (int m=0; m<numIterations; m++) {

			// play some rounds of games - OK le's play some cards
			for (int i=0; i<numRounds; i++) {
				if (i%delta==0) o("round: " + i);

				// here are the players for this hand
				int tableSize = 2+r.nextInt(8); // random table size 2 to 10
				Player[] players = new Player[tableSize];
				for (int j=0; j<tableSize; j++) {
					boolean foundPlayer = false;
					while (!foundPlayer) {
						//if (debug) o("getting players..");
						int rpn = r.nextInt(pool.length);
						if (pool[rpn].isActive == false && !pool[rpn].broke) {
							players[j]=pool[rpn];
							pool[rpn].isActive = true;
							foundPlayer=true;
						}
					}
				}

				playHand(players);
				// nice hand

				// lets take care of their banking needs
				for (int j=0; j<pool.length; j++) {
					int winnings = pool[j].bankroll-10000;
					pool[j].winnings+=winnings;
					pool[j].bankroll=10000;
					pool[j].broke = false;
					pool[j].isActive = false;
				}

				// reset the players for later action
				for (int j=0; j<players.length; j++) {
					players[j].reset();
				}

				if (debug) {
					for (int j=0; j<pool.length; j++) {
						System.out.println(pool[j].playerNumber+" : " + pool[j].winnings);
					}
				}
			}

			// w/ each iteration we ditch many of our homeboys
			/*Arrays.sort(pool);
			for (int i=0; i<pool.length/2; i++) {
				pool[i] = new Player(i);
				pool[i].debug=true;
				//pool[i].params.ranSet2(0.12f,8.0f,4.0f);
				if (i%4==0) pool[i].params.setConst(0.5f);
				if (i%4==1) pool[i].params.setConst(1);
				if (i%4==2) pool[i].params.setConst(2);
				if (i%4==3) pool[i].params.setConst(4);
				pool[i].setBankroll(10000);
				pool[i].amountBought+=10000;
				pool[i].broke=false;
			}
			*/

			Arrays.sort(pool);
			logFile.initWrite(false);
			logFile.write("Results after " + numRounds + " rounds: " + "\n");
			for (int i=0; i<pool.length; i++) {
				logFile.write(pool[i].getStatusLine());
			}
			logFile.closeWrite();

		}

		// report results
		Arrays.sort(pool);
		logFile.initWrite(false);
		logFile.write("Results after " + numRounds + " rounds: " + "\n");
		for (int i=0; i<pool.length; i++) {
			logFile.write(pool[i].getStatusLine());
		}
		logFile.closeWrite();
	}


	/**
	* To be used for competition of bots..
	*   Not for simulation by a bot..
	*
	*  CAREFUL  ACHTUNG MOTHERFUCKER
	*/
	public void playHand(Player[] p) {
		if (debug) {
			System.out.println("Casino playing hand with " + p.length + " players:");
			String s = "";
			for (int i=0; i<p.length; i++) { // give them all a pocket - no betting yet
				s+= " P" + p[i].playerNumber;
			}
			o(s);
		}
		theDeck.shuffle(); // HERE"S THE ONE SHUFFLE PER HAND


		int numStillIn = p.length;
		for (int i=0; i<p.length; i++) { // give them all a pocket
			p[i].givePocket(theDeck.deal(), theDeck.deal());
			// at this point everybody goes in blind
			p[i].bankroll-=annie;
			p[i].amountIn=annie;
		}


		int pot = 0;  // get the pot in the end from people still in or from foldCash
		int foldCash = 0;  // keep track of money left on the table
		boolean allIn = false;
		int cap = Integer.MAX_VALUE; // initialize

		// take bets round 1
		// start with player 1
		int currentBet = annie;
		// initialize every time

		// ROUND 0
		for (int i=0; i<p.length; i++) {
			// all are still in at first..
			bet = p[i].getBet(0,currentBet,numStillIn);
			if (debug) o("Got bet: " + bet.amount);
			if (bet.fold==true) {
				if (debug) o("we have a fold");
				p[i].isActive = false;
				numStillIn--;
				foldCash+=p[i].amountIn;
			}
			else if (bet.amount >= currentBet) {
				if (allIn) {
					// we need to credit this player the remainder of his bet
					int remainder = bet.amount - cap;
					if (debug) o("pocket returning money to somebody: " + remainder + " , for all in");
					p[i].bankroll+=remainder;
					p[i].amountIn-=remainder;
				}
				else if (bet.allIn==true) {
					currentBet = bet.amount;
					cap = bet.amount;
					allIn = true;
				}
				else {  currentBet = bet.amount; }
			}
			else {	System.out.println("*******problems with a pocket bet.. less than currentBet!");	}
		}

		// now a round of calls
		// ROUND 1
		for (int i=0; i<p.length; i++) {
			if (p[i].isActive && p[i].amountIn<currentBet) {
				bet = p[i].getBet(1,currentBet,numStillIn);
				if (bet.fold==true) {
					if (debug) o("calls in pocket - we have a fold");
					p[i].isActive = false;
					numStillIn--;  foldCash+=p[i].amountIn;
				}
				else if (bet.amount < currentBet) {
					System.out.println("*******problems with a pocket call.. less than currentBet!");
				}
			}
		}

		// flop
		Card waste = theDeck.deal();
		Card f1 = theDeck.deal();
		Card f2 = theDeck.deal();
		Card f3 = theDeck.deal();
		if (debug) o("flops: " + f1 + " " + f2 + " " + f3 + " numIn:" + numStillIn + " cb: " + currentBet);
		for (int i=0; i<p.length; i++) { // give them all a flop
			if (p[i].isActive) 	p[i].giveFlop(f1,f2,f3);
		}


		// flop bets..
		// ROUND 2
		for (int i=0; i<p.length; i++) {
			if (p[i].isActive) {
				bet = p[i].getBet(2,currentBet,numStillIn);
				if (bet.fold==true) {
					p[i].isActive = false;
					numStillIn--;  foldCash+=p[i].amountIn;
				}
				else if (bet.amount >= currentBet) {
					if (allIn) {
						// we need to credit this player the remainder of his bet
						int remainder = bet.amount - cap;
						p[i].bankroll+=remainder;  p[i].amountIn-=remainder;
					}
					else if (bet.allIn==true) {
						cap = bet.amount;
						currentBet = bet.amount;
						allIn = true;
					}
					else {currentBet = bet.amount; }
				}
				else {	System.out.println("********problems with a flop bet.. less than currentBet!"); }
			}
		}  // now a round of calls


		// ROUND 3
		for (int i=0; i<p.length; i++) {
			if (p[i].isActive && p[i].amountIn<currentBet) {
				bet = p[i].getBet(3, currentBet,numStillIn);
				if (bet.fold==true) {
					p[i].isActive = false;
					numStillIn--;  foldCash+=p[i].amountIn;
				}
				else if (bet.amount < currentBet) {	System.out.println("********problems with a flop call.. less than currentBet!"); }
			}
		} // done flop bets


		// turn
		waste = theDeck.deal();
		Card t1 = theDeck.deal();
		for (int i=0; i<p.length; i++) { // give them all a flop
			if (p[i].isActive) 	p[i].giveTurn(t1);
		}

		// turn bets..
		// ROUND 4
		for (int i=0; i<p.length; i++) {
			if (p[i].isActive) {
				bet = p[i].getBet(4,currentBet,numStillIn);
				if (bet.fold==true) {
					p[i].isActive = false;
					numStillIn--;  foldCash+=p[i].amountIn;
				}
				else if (bet.amount >= currentBet) {
					if (allIn) {
						// we need to credit this player the remainder of his bet
						int remainder = bet.amount - cap;
						p[i].bankroll+=remainder;  p[i].amountIn-=remainder;
					}
					else if (bet.allIn==true) {
						cap = bet.amount;
						currentBet = bet.amount;
						allIn = true;
					}
					else {	currentBet = bet.amount;  }
				}
				else {	System.out.println("*****problems with a turn bet.. less than currentBet!"); }
			}
		}

		// ROUND 5
		for (int i=0; i<p.length; i++) {
			if (p[i].isActive && p[i].amountIn<currentBet) {
				bet = p[i].getBet(5,currentBet,numStillIn);
				if (bet.fold==true) {
					p[i].isActive = false;
					numStillIn--;  foldCash+=p[i].amountIn;
				}
				else if (bet.amount < currentBet) {
					System.out.println("*****problems with a turn call.. less than currentBet!");
				}
			}
		}
		// done turn

		// river
		waste = theDeck.deal();
		Card r1 = theDeck.deal();
		for (int i=0; i<p.length; i++) { // give them all a flop
			if (p[i].isActive) 	p[i].giveRiver(r1);
		}

		// ROUND 6
		for (int i=0; i<p.length; i++) {
			if (p[i].isActive) {
				bet = p[i].getBet(6,currentBet,numStillIn);
				if (bet.fold==true) {
					p[i].isActive = false;
					numStillIn--;  foldCash+=p[i].amountIn;
				}
				else if (bet.amount >= currentBet) {
					if (allIn) {
						// we need to credit this player the remainder of his bet
						int remainder = bet.amount - cap;
						p[i].bankroll+=remainder;  p[i].amountIn-=remainder;
					}
					else if (bet.allIn==true) {
						cap = bet.amount;
						allIn = true;
						currentBet = bet.amount;
					}
					else { currentBet = bet.amount; }
				}
				else {	System.out.println("*****problems with a river bet.. less than currentBet!"); }
			}
		}

		//ROUND 7
		for (int i=0; i<p.length; i++) {
			if (p[i].isActive && p[i].amountIn<currentBet) {
				bet = p[i].getBet(7,currentBet,numStillIn);
				if (bet.fold==true) {
					p[i].isActive = false;
					numStillIn--;  foldCash+=p[i].amountIn;
				}
				else if (bet.amount < currentBet) {
					System.out.println("problems with a river call.. less than currentBet!");
				}
			}
		}

		//  ** all bets completed **
		// time to reconcile
		pot += foldCash;
		pot += numStillIn*currentBet;
		// who's still in?
		if (numStillIn<1) System.out.println("problem - everybody folded!!");
		else if (numStillIn==1) {
			for (int i=0; i<p.length; i++) {
				if (p[i].isActive) {
					p[i].bankroll+=pot;
					p[i].broke=false;
					if (debug) o("Delivering pot to one remainder: " + p[i].playerNumber + " : " + pot + "$");
				}

			}
		}
		else {
			int bestRank = 0;
			for (int i=0; i<p.length; i++) {
				if (p[i].isActive) {
					int rank = handEval.rankHand(p[i].getHand());
					p[i].rank = rank;
					if (rank >= bestRank) bestRank=rank;
				}
			}
			for (int i=0; i<p.length; i++) {
				if (p[i].rank==bestRank) {
					p[i].bankroll+=pot;
					p[i].broke=false;
					if (debug) o("pot goes to showdown winner: " + p[i].playerNumber);
				}
			}

		}
	}
	// done playHand(..)

	public void o(String s) {
		System.out.println("Cas: " +s);
	}

	public static final void main(String[] args) {
		Casino c = new Casino();
	}
}

