import java.util.Random;
import java.util.Arrays;


/**
*
*    Lets play some bots against each other in simplified play
*
*/
public class Casino {
	public Random r;
	public HandEvaluator handEval;
	private Deck theDeck;
	public int numPlayers = 10;
	public int numRounds = 100;
	public int numIterations = 20;
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

		// first create the players
		pool = new Player[numPlayers];
		for (int i=0; i<numPlayers; i++) {
			pool[i] = new Player(i);
			pool[i].debug=true;
			pool[i].params.ranSet2(0.12f,8.0f,4.0f);
			pool[i].setBankroll(10000);
			pool[i].amountBought+=10000;
			pool[i].broke=false;
		}

		// get the deck in order
		theDeck = new Deck();
		theDeck.shuffle();
		theDeck.shuffle();

		// we are going to have several tounaments, one at a time
		for (int m=0; m<numIterations; m++) {

			// play some rounds of games - OK le's play some cards
			for (int i=0; i<numRounds; i++) {
				if (i%10==0) o("round: " + i);/*
				int numBroke = 0;
				for (int j=0; j<pool.length; j++) {
					if (pool[j].broke) numBroke++;
				}
				int tableSize = 2+r.nextInt(8); // random table size 2 to 10
				if (numPlayers-numBroke<=pool.length/2) i=numRounds;
				else {*/
				Player[] players = new Player[tableSize];
				for (int j=0; j<tableSize; j++) {
					boolean foundPlayer = false;
					while (!foundPlayer) {
						int rpn = r.nextInt(pool.length);
						if (pool[rpn].isActive == false && !pool[rpn].broke) {
							players[j]=pool[rpn];
							pool[rpn].isActive = true;
							foundPlayer=true;
						}
					}
				}
				playHand(players);
				if (debug) {
					for (int j=0; j<pool.length; j++) {
						System.out.println(pool[j].playerNumber+" : " + pool[j].bankroll);
					}
				}

			/*	for (int j=0; j<pool.length; j++) {
					if (pool[j].broke) {
						pool[j] = new Player(pool[j].playerNumber);
						pool[j].params.ranSet2(0.12f,8.0f,4.0f);
						pool[j].setBankroll(10000);
						pool[j].broke=false;
					}
				}
			*/
				/*if (i<numRounds-2) {
					Arrays.sort(pool);
					if (i%100 == 99) {
						o("adding new players..");
						for (int j=0; j<tableSize; j++) {
							if (j<tableSize/2) {
								pool[j] = new Player(pool[j].playerNumber);
								pool[j].params.ranSet1(0.2f,5.0f,3.0f);
								pool[j].setBankroll(10000);
								pool[j].broke=false;
							}
						}
					}
				}*/
			}
			o("iterating");
			for (int j=0; j<pool.length; j++) {
				if (pool[j].broke) {
					pool[j] = new Player(pool[j].playerNumber);
					pool[j].params.ranSet2(0.12f,8.0f,4.0f);
					pool[j].setBankroll(10000);
					pool[j].broke=false;
				}
				else {
					pool[j].bankroll+=10000;
				}
			}

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
	*/
	public void playHand(Player[] p) {
		if (debug) {
			System.out.println("Casino playing hand with " + p.length + " players:");
			for (int i=0; i<p.length; i++) { // give them all a pocket - no betting yet
				o("Player: " + p[i].playerNumber);
			}
		}

		//if (debug) System.out.println("Setting debug on for player 0 ");
		//p[0].debug = true;
		theDeck.shuffle();
		int numStillIn = p.length;
		for (int i=0; i<p.length; i++) { // give them all a pocket - no betting yet
			p[i].givePocket(theDeck.deal(), theDeck.deal());
		}

		int pot = 0;
		int foldCash = 0;  // keep track of money left on the table
		boolean allIn = false;
		int cap = Integer.MAX_VALUE; // initialize

		// take bets round 1
		// start with player 1
		int currentBet = 0; // initialize every time
		for (int i=0; i<p.length; i++) {
			// all are still in at first..
			if (debug) o("asking for bet: " + i + " numIn " + numStillIn + " cb: " + currentBet);
			bet = p[i].getBet(0,currentBet,numStillIn);
			if (debug) o("Cas: got bet: " + bet.amount);
			if (bet.fold==true) {
				if (debug) o("we have a fold");
				p[i].isActive = false;
				numStillIn--;  foldCash+=p[i].amountIn;
			}
			else if (bet.amount >= currentBet) {
				if (allIn) {
					// we need to credit this player the remainder of his bet
					int remainder = bet.amount - cap;
					if (debug) o("pocket returning money to somebody: " + remainder + " , for all in");
					p[i].bankroll+=remainder;  p[i].amountIn-=remainder;
				}
				else if (bet.allIn==true) {
					currentBet = bet.amount;
					cap = bet.amount;
					allIn = true;
				}
				else {
					currentBet = bet.amount;
				}
			}
			else {
				//System.out.println("problems with a pocket bet.. less than currentBet!");
			}
		}  // now a round of calls

		for (int i=0; i<p.length; i++) {
			if (p[i].isActive && p[i].amountIn<currentBet) {
				bet = p[i].getBet(1,currentBet,numStillIn);
				if (bet.fold==true) {
					if (debug) o("calls in pocket - we have a fold");
					p[i].isActive = false;
					numStillIn--;  foldCash+=p[i].amountIn;
				}
				else if (bet.amount < currentBet) {
					//System.out.println("problems with a pocket call.. less than currentBet!");
				}
			}
		}

		// flop
		Card waste = theDeck.deal();
		Card f1 = theDeck.deal();
		Card f2 = theDeck.deal();
		Card f3 = theDeck.deal();
		if (debug) o("flops: " + f1 + " " + f2 + " " + f3 + " numIn:" + numStillIn);

		for (int i=0; i<p.length; i++) { // give them all a flop
			if (p[i].isActive) 	p[i].giveFlop(f1,f2,f3);
		}
		// flop bets..
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
					else {
						currentBet = bet.amount;
					}
				}
				else {
					//System.out.println("problems with a flop bet.. less than currentBet!");
				}
			}
		}  // now a round of calls

		for (int i=0; i<p.length; i++) {
			if (p[i].isActive && p[i].amountIn<currentBet) {
				bet = p[i].getBet(3, currentBet,numStillIn);
				if (bet.fold==true) {
					p[i].isActive = false;
					numStillIn--;  foldCash+=p[i].amountIn;
				}
				else if (bet.amount < currentBet) {
					//System.out.println("problems with a flop call.. less than currentBet!");
				}
			}
		}
		// done flop bets

		// turn
		waste = theDeck.deal();
		Card t1 = theDeck.deal();
		for (int i=0; i<p.length; i++) { // give them all a flop
			if (p[i].isActive) 	p[i].giveTurn(t1);
		}
		// turn bets..
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
					else {
						currentBet = bet.amount;
					}
				}
				else {
					//System.out.println("problems with a turn bet.. less than currentBet!");
				}
			}
		}

		for (int i=0; i<p.length; i++) {
			if (p[i].isActive && p[i].amountIn<currentBet) {
				bet = p[i].getBet(5,currentBet,numStillIn);
				if (bet.fold==true) {
					p[i].isActive = false;
					numStillIn--;  foldCash+=p[i].amountIn;
				}
				else if (bet.amount < currentBet) {
					//System.out.println("problems with a turn call.. less than currentBet!");
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
					else {
						currentBet = bet.amount;
					}
				}
				else {
					//System.out.println("problems with a river bet.. less than currentBet!");
				}
			}
		}
		for (int i=0; i<p.length; i++) {
			if (p[i].isActive && p[i].amountIn<currentBet) {
				bet = p[i].getBet(7,currentBet,numStillIn);
				if (bet.fold==true) {
					p[i].isActive = false;
					numStillIn--;  foldCash+=p[i].amountIn;
				}
				else if (bet.amount < currentBet) {
					//System.out.println("problems with a river call.. less than currentBet!");
				}
			}
		}

		// all bets completed.
		// time to reconcile
		if (pot != 0) System.out.println("problems, pot should still be 0...");
		pot += foldCash;
		pot += numStillIn*currentBet;
		// who's still in?
		if (numStillIn<1) System.out.println("problem - everybody folded!!");
		else if (numStillIn==1) {
			for (int i=0; i<p.length; i++) {
				if (p[i].isActive) {
					p[i].bankroll+=pot;
					p[i].broke=false;
					if (debug) o("Delivering pot to one remainer: " + p[i].playerNumber);
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

		// reset the players for later action
		for (int i=0; i<p.length; i++) {
			p[i].reset();
		}
	}

	public void o(String s) {
		System.out.println("Cas: " +s);
	}

	public static final void main(String[] args) {
		Casino c = new Casino();
	}
}

