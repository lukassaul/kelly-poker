import java.util.Random;

/**
* Lukas Saul 12/06
*
* A class to hold poker simulation control
*
* March 07 - adding fold simulation - assume all folds had less than kelly bet zero
*
*/
public class Table {
	public Random r;
	public HandEvaluator handEval;
	public Deck theDeck;

	public int foldSimulations;
	public Table foldTable;
	private Card pocket1, pocket2;
	public boolean debug;

	/**
	* Number of players beginning hand
	*/
	public int numPlayers;
	public int numPlayersLeft;

	public Table() {
		new Table(0);
		debug = false;
	}

	public Table(int num) {
		r = new Random();
		numPlayers = num;
		numPlayersLeft = num;
		theDeck = new Deck();
		theDeck.shuffle();
		theDeck.shuffle();
		handEval = new HandEvaluator();
	}

	public void reset() {
		handEval = new HandEvaluator();
		//System.out.println("called table.reset");
		theDeck = new Deck();
		theDeck.shuffle();
	}

	//public void setPocket(Hand h) {
	//	theDeck.extractHand(h);
	//	Card[] cardArray = h.getCardArray();
	//	if (cardArray.length != 2) System.out.println("problem in table.setPocket");
	//	else {
	//		pocket1 = cardArray[0];
	//		pocket2 = cardArray[1];
	//	}
	//}

	public void setPocket(Card c1, Card c2) {
		theDeck.extractCard(c1);
		theDeck.extractCard(c2);
		pocket1 = c1;
		pocket2 = c2;
	}

	/*public void setPocketFold(int numFolds) {
		for (int i=0; i<numFolds; i++) {
			foldTable = new Table(numPlayersLeft);
			boolean foldHand = false;
			while (!foldHand) {
				Card test1 = theDeck.deal();
				Card test2 = theDeck.deal();
				foldTable.setPocket(test1,test2);



	}*/


	public Card[] getFlop() {
		Card waste = theDeck.deal();
		// check deck
		//theDeck.extractCard(waste);
		// already gone - this is good
		Card[] tbr = new Card[3];
		tbr[0] = theDeck.deal();
		tbr[1] = theDeck.deal();
		tbr[2] = theDeck.deal();
		return tbr;
	}

	public void setFlop(Card c1, Card c2, Card c3) {
		//System.out.println(" setting flop: " + c1 + " " + c2 + " " + c3);
		theDeck.extractCard(c1);
		theDeck.extractCard(c2);
		theDeck.extractCard(c3);
	}


	public Card getTurnOrRiver() {
		Card waste = theDeck.deal();
		return theDeck.deal();
	}

	public void setTurnOrRiver(Card c1) {
		theDeck.extractCard(c1);
	}


	/**
	*  Return probability of win from /num simulations
	*
	*/
	public float simulate(int num, Card p1, Card p2) {
		if (debug) System.out.println("sim p called");
		int numWins = 0;
		for (int i=0; i<num; i++) {
			reset();
			if (oneHandResult(p1,p2)) numWins++;
		}
		return (float)numWins/(float)num;
	}

	/**
	*  Return probability of win from /num simulations
	*
	*/
	public float simulate(int num, Card p1, Card p2, Card p3, Card p4, Card p5) {
		if (debug) System.out.println("sim f called");
		int numWins = 0;
		for (int i=0; i<num; i++) {
			reset();
			if (oneHandResult(p1,p2,p3,p4,p5)) numWins++;
		}
		return (float)numWins/(float)num;
	}
	/**
	*  Return probability of win from /num simulations
	*
	*/
	public float simulate(int num, Card p1, Card p2, Card p3, Card p4, Card p5, Card p6) {
		if (debug) System.out.println("sim t called");
		int numWins = 0;
		for (int i=0; i<num; i++) {
			reset();
			if (oneHandResult(p1,p2,p3,p4,p5,p6)) numWins++;
		}
		return (float)numWins/(float)num;
	}
	/**
	*  Return probability of win from /num simulations
	*
	*/
	public float simulate(int num, Card p1, Card p2, Card p3, Card p4, Card p5, Card p6, Card p7) {
		if (debug) System.out.println("sim r called");
		int numWins = 0;
		for (int i=0; i<num; i++) {
			reset();
			if (oneHandResult(p1,p2,p3,p4,p5,p6,p7)) numWins++;
		}
		return (float)numWins/(float)num;
	}

	/**
	* Simulate basic single deal hand
	*
	* Return true if this hand wins the table
	*/
	public boolean oneHandResult(Card p1, Card p2) {
		if (debug) System.out.println("called OHR: " + p1 + " " + p2);
		Hand pocket = new Hand();
		//pocket.makeEmpty();
		pocket.addCard(p1);
		pocket.addCard(p2);
		setPocket(p1,p2);
		Hand[] hands = new Hand[numPlayers];
		for (int i=0; i<numPlayers; i++) {
			hands[i] = new Hand();
			hands[i].addCard(theDeck.deal());
			hands[i].addCard(theDeck.deal());
		}

		// flop
		Card[] flop = getFlop();
		for (int i=0; i<numPlayers; i++) {
			hands[i].addCard(flop[0]);
			hands[i].addCard(flop[1]);
			hands[i].addCard(flop[2]);
		}
		pocket.addCard(flop[0]);
		pocket.addCard(flop[1]);
		pocket.addCard(flop[2]);

		// turn
		Card next = getTurnOrRiver();
		for (int i=0; i<numPlayers; i++) {
			hands[i].addCard(next);
		}
		pocket.addCard(next);

		// river
		next = getTurnOrRiver();
		for (int i=0; i<numPlayers; i++) {
			hands[i].addCard(next);
		}
		pocket.addCard(next);

		// evaluate
		//System.out.println("pocket.length: " + pocket.size());
		if (handEval==null) System.out.println("null handEval..");
		int myRank = handEval.rankHand(pocket);
		for (int i=0; i<numPlayers; i++) {
			// treat tie as loss
			if (myRank <= handEval.rankHand(hands[i])) return false;
		}
		return true;
	}

	private boolean oneHandResult(Card c1, Card c2, Card c3, Card c4, Card c5) {
		if (debug) System.out.println("called OHR: " + c1 + " " + c2 + " " + c3 + " " + c4+ " " + c5);
		Hand pocket = new Hand();
		pocket.addCard(c1);
		pocket.addCard(c2);
		setPocket(c1,c2);
		setFlop(c3,c4,c5);
		Hand[] hands = new Hand[numPlayers];
		for (int i=0; i<numPlayers; i++) {
			hands[i] = new Hand();
			hands[i].addCard(theDeck.deal());
			hands[i].addCard(theDeck.deal());
		}
		for (int i=0; i<numPlayers; i++) {
			hands[i].addCard(c3);
			hands[i].addCard(c4);
			hands[i].addCard(c5);
		}
		pocket.addCard(c3);
		pocket.addCard(c4);
		pocket.addCard(c5);

		// turn
		Card next = getTurnOrRiver();
		for (int i=0; i<numPlayers; i++) {
			hands[i].addCard(next);
		}
		pocket.addCard(next);

		// river
		next = getTurnOrRiver();
		for (int i=0; i<numPlayers; i++) {
			hands[i].addCard(next);
		}
		pocket.addCard(next);

		// evaluate
		int myRank = handEval.rankHand(pocket);
		for (int i=0; i<numPlayers; i++) {
			// treat tie as loss
			if (myRank <= handEval.rankHand(hands[i])) return false;
		}
		return true;
	}

	private boolean oneHandResult(Card c1, Card c2, Card c3, Card c4, Card c5, Card c6) {
		if (debug) System.out.println("called OHR: " + c1 + " " + c2 + " " + c3 + " " + c4+ " " +c5+" "+c6);
		Hand pocket = new Hand();
		pocket.addCard(c1);
		pocket.addCard(c2);
		setPocket(c1,c2);
		setFlop(c3,c4,c5);
		setTurnOrRiver(c6);
		Hand[] hands = new Hand[numPlayers];
		for (int i=0; i<numPlayers; i++) {
			hands[i] = new Hand();
			hands[i].addCard(theDeck.deal());
			hands[i].addCard(theDeck.deal());
		}
		for (int i=0; i<numPlayers; i++) {
			hands[i].addCard(c3);
			hands[i].addCard(c4);
			hands[i].addCard(c5);
		}
		pocket.addCard(c3);
		pocket.addCard(c4);
		pocket.addCard(c5);

		// turn
		for (int i=0; i<numPlayers; i++) {
			hands[i].addCard(c6);
		}
		pocket.addCard(c6);

		// river
		Card next = getTurnOrRiver();
		for (int i=0; i<numPlayers; i++) {
			hands[i].addCard(next);
		}
		pocket.addCard(next);

		// evaluate
		int myRank = handEval.rankHand(pocket);
		for (int i=0; i<numPlayers; i++) {
			// treat tie as loss
			if (myRank <= handEval.rankHand(hands[i])) return false;
		}
		return true;
	}

	private boolean oneHandResult(Card c1, Card c2, Card c3, Card c4, Card c5, Card c6, Card c7) {
		if (debug) System.out.println("called OHR: " + c1 + " " + c2 + " " + c3 + " " + c4+ " " +c5+" "+c6+" "+c7);
		Hand pocket = new Hand();
		pocket.addCard(c1);
		pocket.addCard(c2);
		setPocket(c1,c2);
		setFlop(c3,c4,c5);
		setTurnOrRiver(c6);
		setTurnOrRiver(c7);
		Hand[] hands = new Hand[numPlayers];
		for (int i=0; i<numPlayers; i++) {
			hands[i] = new Hand();
			hands[i].addCard(theDeck.deal());
			hands[i].addCard(theDeck.deal());
		}
		for (int i=0; i<numPlayers; i++) {
			hands[i].addCard(c3);
			hands[i].addCard(c4);
			hands[i].addCard(c5);
		}
		pocket.addCard(c3);
		pocket.addCard(c4);
		pocket.addCard(c5);

		// turn
		for (int i=0; i<numPlayers; i++) {
			hands[i].addCard(c6);
		}
		pocket.addCard(c6);

		// river
		for (int i=0; i<numPlayers; i++) {
			hands[i].addCard(c7);
		}
		pocket.addCard(c7);

		// evaluate
		int myRank = handEval.rankHand(pocket);
		for (int i=0; i<numPlayers; i++) {
			// treat tie as loss
			if (myRank <= handEval.rankHand(hands[i])) return false;
		}
		return true;
	}

	public static final void main(String[] args) {
		Table t = new Table(5);
		Card[] hand = t.getFlop();
		System.out.println(hand[0]+" ");
	}
}
