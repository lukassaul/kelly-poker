

/**
 * Class for identifying / comparing / ranking Hands.
 *
 * <P>Source Code: <A HREF="http://www.cs.ualberta.ca/~davidson/poker/src/poker/HandEvaluator.java">HandEvaluator.java</A><P>
 * <P><A HREF="http://www.cs.ualberta.ca/~davidson/poker/src/eval.html">JNI Poker Evaluation Library Code</A><P>
 *
 * @author  Aaron Davidson, Darse Billings, Denis Papp
 * @version 2.1.2
 */

public class HandEvaluator {

	private static boolean use_native = false;

	/**
	 * Construct a new Hand Evaluator.
	 * If the libeval.so library is present,
	 * the native methods will be used, otherwise
	 * the slower java methods will be used instead.
	 */
	public HandEvaluator() {
		try {
			String osname = System.getProperty("os.name");
			if (osname.equals("Linux")) {
			    System.loadLibrary("eval_linux");
			} else if (osname.equals("SunOS")) {
			    System.loadLibrary("eval_sunos");
			} else {
				return;
			}
			use_native = true;
		} catch (UnsatisfiedLinkError ule) {
			use_native = false;
		}
	}

	/**
	 * Returns true if the native evaluation library is loaded.
	 */
	public boolean isNative() {
		return use_native;
	}

/**********************************************************************/
// HAND COMPARISON STUFF
/**********************************************************************/

	/**
	 * Get a numerical ranking of this hand.
	 * Uses a Native Method. (Make sure the feval library is installed!)
	 * Uses GNU Poker Lib:  eval.h
	 * @param h a 5-7 card hand
	 * @return a unique number representing the hand strength of the best
	 * 5-card poker hand in the given 7 cards. The higher the number, the better
	 * the hand is.
	 */
	public int rankHand(Hand h) {
		if (use_native) return CRankHandFast(h.getCardArray());
		else return rankHand_Java(h);
	}

	/**
	 * Given a hand, return a string naming the hand ('Ace High Flush', etc..)
	 */
	public static String nameHand(Hand h) {
		return name_hand(rankHand_Java(h));
	}

	/**
	 * Compares two 7 card hands against each other.
	 * @param h1 The first hand
	 * @param h2 The second hand
	 * @return 1 = first hand is best, 2 = second hand is best, 0 = tie
	 */
	public int compareHands(Hand h1, Hand h2) {
		if (use_native) {
			int r1 = rankHand(h1);
			int r2 = rankHand(h2);

			if (r1 > r2) return 1;
			if (r1 < r2) return 2;
			return 0;
		} else {
			int[] ch1 = h1.getCardArray();
			int[] bh1 = new int[6];
			int ht1 = Find_Hand(ch1,bh1);

			int[] ch2 = h2.getCardArray();
			int[] bh2 = new int[6];
			int ht2 = Find_Hand(ch2,bh2);

			if (ht1 > ht2) return 1;
			if (ht1 < ht2) return 2;

			return Best_Hand(bh1,bh2);
		}
	}

	/**
	 * Compares two 5-7 card hands against each other.
	 * @param h1 The rank of the first hand
	 * @param h2 The second hand
	 * @return 1 = first hand is best, 2 = second hand is best, 0 = tie
	 */
	public int compareHands(int rank1, Hand h2) {
		int r1 = rank1;
		int r2 = rankHand(h2);

		if (r1 > r2) return 1;
		if (r1 < r2) return 2;
		return 0;
	}

	/**
	 * Get a numerical ranking of this hand.
	 * Native Method. (Make sure the feval library is installed!)
	 * Uses GNU Poker Lib:  eval.h
	 * @param cards an array of up to 7 card integers
	 * @return a unique number representing the hand strength of the best
	 * poker hand in the given cards. The higher the number, the better
	 * the hand is.
	 */
	public static native int CRankHandFast(int[] cards);

	/**
	 * Get a numerical ranking of this hand.
	 * Native Method. (Make sure the feval library is installed!)
	 * Uses GNU Poker Lib:  eval7.h
	 * @param cards an array of 7 card integers
	 * @return a unique number representing the hand strength of the best
	 * 5-card poker hand in the given 7 cards. The higher the number, the better
	 * the hand is.
	 */
	public static native int CRankHandFast7(int[] cards);


	/**
	 * Get a numerical ranking of this hand.
	 * Uses a Native Method. (Make sure the feval library is installed!)
	 * Uses GNU Poker Lib:  eval7.h
	 * @param h a 7 card hand
	 * @return a unique number representing the hand strength of the best
	 * 5-card poker hand in the given 7 cards. The higher the number, the better
	 * the hand is.
	 */
	public int rankHand7(Hand h) {
		return CRankHandFast7(h.getCardArray());
	}


/**********************************************************************/
// CACHED HAND COMPARISON STUFF
/**********************************************************************/

	/**
	 * Given a board, cache all possible two card combinations
	 * of hand ranks, so that lightenting fast hand comparisons
	 * may be done later.
	 */
	public int[][] getRanks(Hand board) {
		Hand myhand = new Hand(board);
		int[][] rc = new int[52][52];
		int i,j,v,n1,n2;
        d.reset();
        d.extractHand(board);

        // tabulate ranks
		for (i=d.getTopCardIndex();i<Deck.NUM_CARDS;i++) {
			myhand.addCard(d.getCard(i));
			n1 = d.getCard(i).getIndex();
			for (j=i+1;j<Deck.NUM_CARDS;j++) {
				myhand.addCard(d.getCard(j));
				n2 = d.getCard(j).getIndex();
				rc[n1][n2] = rc[n2][n1] = rankHand(myhand);
				myhand.removeCard();
			}
			myhand.removeCard();
		}
		return rc;
	}


/**********************************************************************/
// HAND RANK STUFF
/**********************************************************************/

	private Deck d = new Deck();
	private int good = 0;
	private int bad = 0;
	private int tied = 0;

	/**
  	 * Calculates the probability of having
  	 * the best hand against one opponent.
	 * @param c1 hole card 1
	 * @param c2 hole card 2
	 * @param h  the board
	 * @return probability of having the best hand.
	 */
	public double handRank(Card c1, Card c2, Hand h) {
		int i,j,v;
		Hand myHand = new Hand(h);
		Hand xxHand = new Hand(h);
		myHand.addCard(c1);
		myHand.addCard(c2);
		int myRank = rankHand(myHand);

		good = bad = tied = 0;

		// remove all known cards
		d.reset();
		d.extractCard(c1);
		d.extractCard(c2);
		d.extractHand(h);

		// tabulate rank
		for (i=d.getTopCardIndex();i<Deck.NUM_CARDS;i++) {
			xxHand.addCard(d.getCard(i));
			for (j=i+1;j<Deck.NUM_CARDS;j++) {
				xxHand.addCard(d.getCard(j));
				v = compareHands(myRank,xxHand);
				if (v == 1) good++;
				else if (v==2) bad++;
				else tied++;
				xxHand.removeCard();
			}
			xxHand.removeCard();
		}

		return (double)((double)(good+(double)(tied/2))/(double)(good+bad+tied));
	}


	/**
	 * Get the number of hands worse than the last hand ranked.
	 */
	public int getNumWorse() { return good; }

	/**
	 * Get the number of hands better than the last hand ranked.
	 */
	public int getNumBetter() { return bad; }

	/**
	 * Get the number of hands tied with the last hand ranked.
	 */
	public int getNumTied() { return tied; }


/**********************************************************************/
// MORE HAND COMPARISON STUFF (Adapted from C code by Darse Billings)
/**********************************************************************/

	/**
	 * Get the best 5 card poker hand from a 7 card hand
	 * @param h Any 7 card poker hand
	 * @return A Hand containing the highest ranked 5 card hand possible from the input.
	 */
	public Hand getBest5CardHand(Hand h) {
		int[] ch = h.getCardArray();
		int[] bh = new int[6];
		int j = Find_Hand(ch,bh);
		Hand nh = new Hand();
		for (int i=0;i<5;i++)
			nh.addCard(bh[i+1]);
		return nh;
	}

	private final static int  unknown  = -1;
	private final static int  strflush =  9;
	private final static int  quads = 8;
	private final static int  fullhouse = 7;
	private final static int  flush = 6;
	private final static int  straight = 5;
	private final static int  trips = 4;
	private final static int  twopair = 3;
	private final static int  pair = 2;
	private final static int  nopair = 1;
	private final static int  highcard = 1;

	/**
	 * Get a string from a hand type.
	 * @param handtype number coding a hand type
	 * @return name of hand type
	 */
	private  String  drb_Name_Hand (int handtype) {
		switch (handtype) {
			case -1:  { return ("Hidden Hand");  }
			case 1:  { return("High Card");  }
	  		case 2:  { return("Pair");  }
	  		case 3:  { return("Two Pair");  }
	  		case 4:  { return("Three of a Kind");  }
	  		case 5:  { return("Straight");  }
	  		case 6:  { return("Flush");  }
	  		case 7:  { return("Full House");  }
	  		case 8:  { return("Four of a Kind");  }
	  		case 9:  { return("Straight Flush");  }
	  		default: { return("Very Weird hand indeed"); }
		}
	}

	/* drbcont:  want to Find_ the _best_ flush and _best_ strflush ( >9 cards) */

	private boolean Check_StrFlush (int[] hand, int[] dist, int[] best) {
	    int i, j, suit, strght, strtop;
	    boolean  returnvalue;
	    int[] suitvector = new int[14];
	  	/* _23456789TJQKA boolean vector
		   01234567890123 indexing */

	    returnvalue = false;	/* default */

	    /* do flat distribution of whole suits (cdhs are 0123 respectively) */

	    for (suit = 0; suit <= 3; suit++) {

		/* explicitly initialize suitvector */
		suitvector[0] = 13;
		for (i = 1; i <= suitvector[0]; i++) {
		    suitvector[i] = 0;
		}
		for (i = 1; i <= hand[0]; i++) {
		    if ( (hand[i] != unknown) && ((hand[i] / 13) == suit) )
		      { suitvector[(hand[i] % 13) + 1] = 1; };
		}

		/* now look for straights */
		if (suitvector[13] >= 1) 	 /* Ace low straight */
		  { strght = 1;}
		else strght = 0;
		strtop = 0;

		for (i = 1; i <= 13; i++) {
		    if (suitvector[i] >= 1)
		      { strght++;
			if (strght >= 5)
			  { strtop = i-1; }; }
		    else strght = 0;
		}

		/* determine if there was a straight flush and copy it to best[] */

		if (strtop > 0) {   	/* no 2-high straight flushes */
		    for (j = 1; j <= 5; j++) {
			best[j] = ((13 * suit) + strtop + 1 - j);
		    }
		    /* Adjust for case of Ace low (five high) straight flush */
		    if (strtop == 3) {
			best[5] = best[5] + 13;
		    }
		    returnvalue = true;
		}
	    }
	    return(returnvalue);
	}

	private void Find_Quads (int[] hand, int[] dist, int[] best) {
	    int i, j, quadrank=0, kicker;

	    /* find rank of largest quads */
	    for (i = 1; i <= 13; i++) {
		if (dist[i] >= 4) { quadrank = i-1; };
	    }

	    /* copy those quads */
	    i = 1;		/* position in hand[] */
	    j = 1;		/* position in best[] */
	    while (j <= 4) { 	/* assume all four will be found before i > hand[0] */
		if ( (hand[i] != unknown) && ((hand[i] % 13) == quadrank) )
		  { best[j] = hand[i]; j++; };
		i++;
	    }

	    /* find best kicker */
	    kicker = unknown; 		/* default is unknown kicker */
	    for (i = 1; i <= 13; i++) { /* find rank of largest kicker */
		if ((dist[i] >= 1) && ((i-1) != quadrank))
		  { kicker = i-1; }
	    }

	    /* copy kicker */
	    if (kicker != unknown) {
		i = 1;		 /* position in hand[] */
		while (j <= 5) { /* assume kicker will be found before i > hand[0] */
		    if ( (hand[i] != unknown) && ((hand[i] % 13) == kicker) )
		      { best[j] = hand[i]; j++; };
		    i++;
		}
	    }
	    else { best[j] = unknown; j++; }
	}

	private void Find_FullHouse (int[] hand, int[] dist, int[] best) {
	    int i, j, tripsrank=0, pairrank =0;

	    /* find rank of largest trips */
	    for (i = 1; i <= 13; i++) {
		if (dist[i] >= 3) { tripsrank = i-1; };
	    }

	    /* copy those trips */
	    i = 1;		/* position in hand[] */
	    j = 1;		/* position in best[] */
	    while (j <= 3) { 	/* assume all three will be found before i > hand[0] */
	        if ( (hand[i] != unknown) && ((hand[i] % 13) == tripsrank) )
		  { best[j] = hand[i]; j++; };
		i++;
	    }

	    /* find best pair */
	    i = 13;
	    pairrank = -1;
	    while (pairrank < 0) { /* assume kicker will be found before i = 0 */
		if ((dist[i] >= 2) && ((i-1) != tripsrank))
		  { pairrank = i-1; }
		else i--;
	    }

	    /* copy best pair */
	    i = 1;		/* position in hand[] */
	    while (j <= 5) { 	/* assume pair will be found before i > hand[0] */
	        if ( (hand[i] != unknown) && ((hand[i] % 13) == pairrank) )
		  { best[j] = hand[i]; j++; };
		i++;
	    }
	}

	private void Find_Flush (int[] hand, int[] dist, int[] best) {	/* finds only the best flush in highest suit */
	    int i, j, flushsuit=0;
	    int[] suitvector = new int[14];
			  	/* _23456789TJQKA boolean vector
				   01234567890123 indexing */
	    /* find flushsuit */
	    for (i = 14; i <= 17; i++) {
		if (dist[i] >= 5) { flushsuit = i - 14; };
	    }

	    /* explicitly initialize suitvector */
	    suitvector[0] = 13;
	    for (i = 1; i <= suitvector[0]; i++) {
		suitvector[i] = 0;
	    }

	    /* do flat distribution of whole flushsuit */
	    for (i = 1; i <= hand[0]; i++) {
	        if ( (hand[i] != unknown) && ((hand[i] / 13) == flushsuit) )
		  { suitvector[(hand[i] % 13) + 1] = 1; };
	    }

	    /* determine best five cards in flushsuit */
	    i = 13;
	    j = 1;
	    while (j <= 5) { /* assume all five flushcards will be found before i < 1 */
		if (suitvector[i] >= 1)
		  { best[j] = (13 * flushsuit) + i - 1; j++; };
		i--;
	    }
	}

	private void Find_Straight (int[] hand, int[] dist, int[] best) {
	    int i, j, strght, strtop;

	    /* look for highest straight */
	    if (dist[13] >= 1)		 /* Ace low straight */
	      { strght = 1; }
	    else strght = 0;
	    strtop = 0;

	    for (i = 1; i <= 13; i++) {
		if (dist[i] >= 1)
		  { strght++;
		    if (strght >= 5)
		      { strtop = i-1; }; }
		else strght = 0;
	    }

	    /* copy the highest straight */
	    if (strtop > 3) {		/* note: different extraction from others */
		for (j = 1; j <= 5; j++) {
		    for (i = 1; i <= hand[0]; i++) {
			if ( (hand[i] != unknown) && (hand[i]%13 == (strtop + 1 - j)) )
			  { best[j] = hand[i]; };
		    }
		}
	    }
	    else if (strtop == 3) {
		for (j = 1; j <= 4; j++) {
		    for (i = 1; i <= hand[0]; i++) {
			if ( (hand[i] != unknown) && (hand[i]%13 == (strtop + 1 - j)) )
			  { best[j] = hand[i]; };
		    }
		}
		for (i = 1; i <= hand[0]; i++) {    /* the Ace in a low straight */
		    if ( (hand[i] != unknown) && (hand[i]%13 == 12) )
		      { best[5] = hand[i]; };
		}
	    }
	}

	private void Find_Trips (int[] hand, int[] dist, int[] best) {
	    int i, j, tripsrank=0, kicker1, kicker2;

	    /* find rank of largest trips */
	    for (i = 1; i <= 13; i++) {
		if (dist[i] >= 3) { tripsrank = i-1; };
	    }

	    /* copy those trips */
	    i = 1;		/* position in hand[] */
	    j = 1;		/* position in best[] */
	    while (j <= 3) { 	/* assume all three will be found before i > hand[0] */
	        if ( (hand[i] != unknown) && ((hand[i] % 13) == tripsrank) )
		  { best[j] = hand[i]; j++; };
		i++;
	    }

	    /* find best kickers */
	    kicker1 = unknown; 		/* default is unknown kicker */
	    for (i = 1; i <= 13; i++) { /* find rank of largest kicker */
		if ((dist[i] >= 1) && ((i-1) != tripsrank))
		  { kicker1 = i-1; };
	    }

	    kicker2 = unknown;
	    for (i = 1; i <= kicker1; i++) { /* find rank of second kicker */
		if ((dist[i] >= 1) && ((i-1) != tripsrank))
		  { kicker2 = i-1; };
	    }

	    /* copy kickers */
	    if (kicker1 != unknown) {
		i = 1;		 /* position in hand[] */
		while (j <= 4) { /* assume kicker1 will be found before i > hand[0] */
		    if ( (hand[i] != unknown) && ((hand[i] % 13) == kicker1) )
		      { best[j] = hand[i]; j++; };
		    i++;
		}
	    }
	    else { best[j] = unknown; j++; }

	    if (kicker2 != unknown) {
		i = 1;		 /* position in hand[] */
		while (j <= 5) { /* assume kicker2 will be found before i > hand[0] */
		    if ( (hand[i] != unknown) && ((hand[i] % 13) == kicker2) )
		      { best[j] = hand[i]; j++; };
		    i++;
		}
	    }
	    else { best[j] = unknown; j++; }
	}

	private void Find_TwoPair (int[] hand, int[] dist, int[] best) {
	    int i, j, pairrank1=0, pairrank2=0, kicker;

	    /* find rank of largest pair */
	    for (i = 1; i <= 13; i++) {
		if (dist[i] >= 2) { pairrank1 = i-1; };
	    }
	    /* find rank of second largest pair */
	    for (i = 1; i <= 13; i++) {
		if ((dist[i] >= 2) && ((i-1) != pairrank1))
		  { pairrank2 = i-1; };
	    }

	    /* copy those pairs */
	    i = 1;		/* position in hand[] */
	    j = 1;		/* position in best[] */
	    while (j <= 2) { 	/* assume both will be found before i > hand[0] */
	        if ( (hand[i] != unknown) && ((hand[i] % 13) == pairrank1) )
		  { best[j] = hand[i]; j++; };
		i++;
	    }
	    i = 1;		/* position in hand[] */
	    while (j <= 4) { 	/* assume both will be found before i > hand[0] */
	        if ( (hand[i] != unknown) && ((hand[i] % 13) == pairrank2) )
		  { best[j] = hand[i]; j++; };
		i++;
	    }

	    /* find best kicker */
	    kicker = unknown; 		/* default is unknown kicker */
	    for (i = 1; i <= 13; i++) { /* find rank of largest kicker */
		if ((dist[i] >= 1) && ((i-1) != pairrank1) && ((i-1) != pairrank2))
		  { kicker = i-1; }
	    }

	    /* copy kicker */
	    if (kicker != unknown) {
		i = 1;		 /* position in hand[] */
		while (j <= 5) { /* assume kicker will be found before i > hand[0] */
		    if ( (hand[i] != unknown) && ((hand[i] % 13) == kicker) )
		      { best[j] = hand[i]; j++; };
		    i++;
		}
	    }
	    else { best[j] = unknown; j++; }
	}

	private void Find_Pair (int[] hand, int[] dist, int[] best) {
	    int i, j, pairrank=0, kicker1, kicker2, kicker3;

	    /* find rank of largest pair */
	    for (i = 1; i <= 13; i++) {
		if (dist[i] >= 2) { pairrank = i-1; };
	    }

	    /* copy that pair */
	    i = 1;		/* position in hand[] */
	    j = 1;		/* position in best[] */
	    while (j <= 2) { 	/* assume both will be found before i > hand[0] */
	        if ( (hand[i] != unknown) && ((hand[i] % 13) == pairrank) )
		  { best[j] = hand[i]; j++; };
		i++;
	    }

	    /* find best kickers */
	    kicker1 = unknown; 		/* default is unknown kicker */
	    for (i = 1; i <= 13; i++) { /* find rank of largest kicker */
		if ((dist[i] >= 1) && ((i-1) != pairrank))
		  { kicker1 = i-1; };
	    }
	    kicker2 = unknown;
	    for (i = 1; i <= kicker1; i++) { /* find rank of second kicker */
		if ((dist[i] >= 1) && ((i-1) != pairrank))
		  { kicker2 = i-1; };
	    }
	    kicker3 = unknown;
	    for (i = 1; i <= kicker2; i++) { /* find rank of third kicker */
		if ((dist[i] >= 1) && ((i-1) != pairrank))
		  { kicker3 = i-1; }
	    }

	    /* copy kickers */
	    if (kicker1 != unknown) {
		i = 1;		 /* position in hand[] */
		while (j <= 3) { /* assume kicker1 will be found before i > hand[0] */
		    if ( (hand[i] != unknown) && ((hand[i] % 13) == kicker1) )
		      { best[j] = hand[i]; j++; };
		    i++;
		}
	    }
	    else { best[j] = unknown; j++; }

	    if (kicker2 != unknown) {
		i = 1;		 /* position in hand[] */
		while (j <= 4) { /* assume kicker2 will be found before i > hand[0] */
		    if ( (hand[i] != unknown) && ((hand[i] % 13) == kicker2) )
		      { best[j] = hand[i]; j++; };
		    i++;
		}
	    }
	    else { best[j] = unknown; j++; }

	    if (kicker3 != unknown) {
		i = 1;		 /* position in hand[] */
		while (j <= 5) { /* assume kicker3 will be found before i > hand[0] */
		    if ( (hand[i] != unknown) && ((hand[i] % 13) == kicker3) )
		      { best[j] = hand[i]; j++; };
		    i++;
		}
	    }
	    else { best[j] = unknown; j++; }
	}

	private void Find_NoPair (int[] hand, int[] dist, int[] best) {
	    int i, j, kicker1, kicker2, kicker3, kicker4, kicker5;

	    /* find best kickers */
	    kicker1 = unknown; 		/* default is unknown kicker */
	    for (i = 1; i <= 13; i++) { /* find rank of largest kicker */
		if (dist[i] >= 1)
		  { kicker1 = i-1; };
	    }
	    kicker2 = unknown;
	    for (i = 1; i <= kicker1; i++) { /* find rank of second kicker */
		if (dist[i] >= 1)
		  { kicker2 = i-1; };
	    }
	    kicker3 = unknown;
	    for (i = 1; i <= kicker2; i++) { /* find rank of third kicker */
		if (dist[i] >= 1)
		  { kicker3 = i-1; };
	    }
	    kicker4 = unknown;
	    for (i = 1; i <= kicker3; i++) { /* find rank of fourth kicker */
		if (dist[i] >= 1)
		  { kicker4 = i-1; };
	    }
	    kicker5 = unknown;
	    for (i = 1; i <= kicker4; i++) { /* find rank of fifth kicker */
		if (dist[i] >= 1)
		  { kicker5 = i-1; };
	    }

	    /* copy kickers */
	    j = 1;		/* position in best[] */

	    if (kicker1 != unknown) {
		i = 1;		 /* position in hand[] */
		while (j <= 1) { /* assume kicker1 will be found before i > hand[0] */
		    if ( (hand[i] != unknown) && ((hand[i] % 13) == kicker1) )
		      { best[j] = hand[i]; j++; };
		    i++;
		}
	    }
	    else { best[j] = unknown; j++; }

	    if (kicker2 != unknown) {
		i = 1;		 /* position in hand[] */
		while (j <= 2) { /* assume kicker2 will be found before i > hand[0] */
		    if ( (hand[i] != unknown) && ((hand[i] % 13) == kicker2) )
		      { best[j] = hand[i]; j++; };
		    i++;
		}
	    }
	    else { best[j] = unknown; j++; }

	    if (kicker3 != unknown) {
		i = 1;		 /* position in hand[] */
		while (j <= 3) { /* assume kicker3 will be found before i > hand[0] */
		    if ( (hand[i] != unknown) && ((hand[i] % 13) == kicker3) )
		      { best[j] = hand[i]; j++; };
		    i++;
		}
	    }
	    else { best[j] = unknown; j++; }

	    if (kicker4 != unknown) {
		i = 1;		 /* position in hand[] */
		while (j <= 4) { /* assume kicker4 will be found before i > hand[0] */
		    if ( (hand[i] != unknown) && ((hand[i] % 13) == kicker4) )
		      { best[j] = hand[i]; j++; };
		    i++;
		}
	    }
	    else { best[j] = unknown; j++; }

	    if (kicker5 != unknown) {
		i = 1;		 /* position in hand[] */
		while (j <= 5) { /* assume kicker5 will be found before i > hand[0] */
		    if ( (hand[i] != unknown) && ((hand[i] % 13) == kicker5) )
		      { best[j] = hand[i]; j++; };
		    i++;
		}
	    }
	    else { best[j] = unknown; j++; }
	}


	private int Best_Hand (int[] hand1, int[] hand2) {	/* sorted 5-card hands, both same type */
	    /* could check for proper hand types... */

	    /* check value of top cards, then on down */
	    if ((hand1[1] % 13) > (hand2[1] % 13))
	      { return(1); }
	    else if ((hand1[1] % 13) < (hand2[1] % 13))
	      { return(2); }

	    /* same top, check second */
	    else if ((hand1[2] % 13) > (hand2[2] % 13))
	      { return(1); }
	    else if ((hand1[2] % 13) < (hand2[2] % 13))
	      { return(2); }

	    /* same second, check third */
	    else if ((hand1[3] % 13) > (hand2[3] % 13))
	      { return(1); }
	    else if ((hand1[3] % 13) < (hand2[3] % 13))
	      { return(2); }

	    /* same third, check fourth */
	    else if ((hand1[4] % 13) > (hand2[4] % 13))
	      { return(1); }
	    else if ((hand1[4] % 13) < (hand2[4] % 13))
	      { return(2); }

	    /* same fourth, check fifth */
	    else if ((hand1[5] % 13) > (hand2[5] % 13))
	      { return(1); }
	    else if ((hand1[5] % 13) < (hand2[5] % 13))
	      { return(2); }

	    else /* same hands */
	      { return(0); }
	}

	public int Find_Hand (int[] hand, int[] best)  { /* -1 means unknown card */
	    int i, card, rank, suit, hand_type,
	        rankmax1, rankmax2, flushmax, strght, strmax;
	    int[] dist = new int[18];
			/* _23456789TJQKAcdhs  distribution vector
  		           012345678901234567  indexing */

 	   /* explicitly initialize distribution vector */
 	   dist[0] = 17;
 	   for (i = 1; i <= dist[0]; i++) {
		dist[i] = 0;
	    }

	    for (i = 1; i <= hand[0]; i++) {
		if (hand[i] != unknown) {
		    card = hand[i];
		    rank = card % 13;
		    suit = card / 13;

		    if (!((rank < 0) || (rank > 12))) {
		    	dist[rank+1]++;
		    }

		    if (!((suit < 0) || (suit > 3))) {
		     	dist[suit+14]++;
		    }
		}
	    }

	    /* scan the distribution array for maximums */
	    rankmax1 = 0;
	    rankmax2 = 0;
	    flushmax = 0;
	    strmax = 0;

	    if (dist[13] >= 1) { strght = 1;} else strght = 0; /* Ace low straight */

	    for (i = 1; i <= 13; i++) {
		if (dist[i] > rankmax1) { rankmax2 = rankmax1; rankmax1 = dist[i]; }
		else if (dist[i] > rankmax2) { rankmax2 = dist[i]; };

		if (dist[i] >= 1) { strght++; if (strght > strmax) { strmax = strght;}}
		else strght = 0;
	    }

	    for (i = 14; i <= 17; i++) {
		if (dist[i] > flushmax) { flushmax = dist[i]; }
	    }

	    hand_type = unknown;

	    if ((flushmax >= 5) && (strmax >= 5))
	      { if (Check_StrFlush (hand, dist, best))
		  { hand_type = strflush; }
	        else { hand_type = flush; Find_Flush (hand, dist, best); }; }
	    else if (rankmax1 >= 4)
	      { hand_type = quads;  Find_Quads (hand, dist, best); }
	    else if ((rankmax1 >= 3) && (rankmax2 >= 2))
	      { hand_type = fullhouse; Find_FullHouse (hand, dist, best); }
	    else if (flushmax >= 5)
	      { hand_type = flush; Find_Flush (hand, dist, best); }
	    else if (strmax >= 5)
	      { hand_type = straight; Find_Straight (hand, dist, best); }
	    else if (rankmax1 >= 3)
	      { hand_type = trips; Find_Trips (hand, dist, best); }
	    else if ((rankmax1 >= 2) && (rankmax2 >= 2))
	      { hand_type = twopair; Find_TwoPair (hand, dist, best); }
	    else if (rankmax1 >= 2)
	      { hand_type = pair; Find_Pair (hand, dist, best); }
	    else
	      { hand_type = nopair; Find_NoPair (hand, dist, best); };

	    return(hand_type);
	}


/**********************************************************************/
// DENIS PAPP'S HAND RANK IDENTIFIER CODE:
/**********************************************************************/

	private static final int POKER_HAND = 5;

	private static final int  HIGH = 0;
	private static final int  PAIR = 1;
	private static final int  TWOPAIR = 2;
	private static final int  THREEKIND = 3;
	private static final int  STRAIGHT = 4;
	private static final int  FLUSH = 5;
	private static final int  FULLHOUSE = 6;
	private static final int  FOURKIND = 7;
	private static final int  STRAIGHTFLUSH = 8;
	private static final int  FIVEKIND = 9;
	private static final int  NUM_HANDS = 10;

	private static final int  NUM_RANKS = 13;

	private static final int  ID_GROUP_SIZE  = (Card.NUM_RANKS*Card.NUM_RANKS*Card.NUM_RANKS*Card.NUM_RANKS*Card.NUM_RANKS);

	private static boolean ID_ExistsStraightFlush(Hand h, Byte straight_high, byte major_suit) {
	        int i;
	        int straight;
	        byte high;
	        boolean[] present = new boolean[Card.NUM_RANKS];
	        for (i=0;i<Card.NUM_RANKS;i++) present[i]=false;

	        for (i=0;i<h.size();i++)
			if (h.getCard(i+1).getSuit() == major_suit)
	        	        present[h.getCard(i+1).getRank()] = true;

	        straight = present[Card.ACE] ? 1 : 0;
	        high = 0;
	        for (i=0;i<Card.NUM_RANKS;i++) {
	                if (present[i]) {
	                        if ( (++straight) >= POKER_HAND)
	                                high = (byte)i;
	                } else straight = 0;
	        }
	        if (high == 0) return false;
	        straight_high = new Byte(high);
	        return true;
	}

	// suit: Card.NUM_SUITS means any
	// not_allowed: Card.NUM_RANKS means any
	// returns ident value
	private static int ID_KickerValue(byte[] paired, int kickers, byte[] not_allowed) {
	        int i = Card.ACE;
	        int value=0;
	        while (kickers != 0) {
	                while ( paired[i]==0 || i==not_allowed[0] || i==not_allowed[1])
	                        i--;
	                kickers--;
	                value+=pow(Card.NUM_RANKS,kickers)*i;
	                i--;
	        }
	        return value;
	}

	private static int ID_KickerValueSuited(Hand h, int kickers, byte suit) {
	        int i;
	        int value=0;

	        boolean[] present = new boolean[Card.NUM_RANKS];
	        for (i=0;i<Card.NUM_RANKS;i++) present[i] = false;

	        for (i=0;i<h.size();i++)
	 	       if (h.getCard(i+1).getSuit() == suit)
	                	present[h.getCard(i+1).getRank()] = true;

	        i = Card.ACE;
	        while (kickers != 0) {
	                while (present[i] == false) i--;
	                kickers--;
	                value += pow(Card.NUM_RANKS,kickers)*i;
	                i--;
	        }
	        return value;
	}

	/**
	 * Get a numerical ranking of this hand.
	 * Uses java based code, so may be slower than using the native
	 * methods, but is more compatible this way.
	 *
	 * Based on Denis Papp's Loki Hand ID code (id.cpp)
	 * Given a 1-9 card hand, will return a unique rank
 	 * such that any two hands will be ranked with the
 	 * better hand having a higher rank.
	 *
	 * @param h a 1-9 card hand
	 * @return a unique number representing the hand strength of the best
	 * 5-card poker hand in the given 7 cards. The higher the number, the better
	 * the hand is.
	 */
	public static int rankHand_Java(Hand h) {
	        boolean straight = false;
	        boolean flush = false;
	        byte max_hand = (byte)(h.size() >= POKER_HAND ? POKER_HAND : h.size());
			int r,c;
	        byte rank,suit;

			// pair data
	        byte[] group_size = new byte[POKER_HAND+1];
	        byte[] paired = new byte[Card.NUM_RANKS];
	        byte[][] pair_rank = new byte[POKER_HAND+1][2];
	        // straight
	        byte straight_high = 0;
	        byte straight_size;
	        // flush
	        byte[] suit_size = new byte[Card.NUM_SUITS];
	        byte major_suit = 0;

	        // determine pairs, dereference order data, check flush
	        for (r=0;r<Card.NUM_RANKS;r++) paired[r] = 0;
	        for (r=0;r<Card.NUM_SUITS;r++) suit_size[r] = 0;
	        for (r=0;r<=POKER_HAND;r++) group_size[r] = 0;
	        for (r=0;r<h.size();r++) {
	                rank = (byte)h.getCard(r+1).getRank();
	                suit = (byte)h.getCard(r+1).getSuit();

	                paired[rank]++;
	                group_size[paired[rank]]++;
	                if (paired[rank] != 0)
	                        group_size[paired[rank]-1]--;
	                if ((++suit_size[suit]) >= POKER_HAND) {
	                        flush = true;
	                        major_suit = suit;
	                }
	        }
	        // Card.ACE low?
	        straight_size = (byte)(paired[Card.ACE] != 0 ? 1 : 0);

			for (int i=0;i<(POKER_HAND+1);i++) {
				pair_rank[i][0] = (byte)Card.NUM_RANKS;
				pair_rank[i][1] = (byte)Card.NUM_RANKS;
			}

	        // check for straight and pair data
	        for (r=0;r<Card.NUM_RANKS;r++) {
	                // check straight
	                if (paired[r]!=0) {
	                        if ( (++straight_size)>=POKER_HAND ) {
	                                straight = true;
	                                straight_high = (byte)r;
	                        }
	                } else
	                        straight_size = 0;

	                // get pair ranks, keep two highest of each
	                c = paired[r];
	                if ( c != 0 ) {
	                        pair_rank[c][1] = pair_rank[c][0];
	                        pair_rank[c][0] = (byte)r;
	                }
	        }

	        // now id type
	        int ident;

			Byte str_hi = new Byte(straight_high);


	        if (group_size[POKER_HAND]!=0) {
	    	    	ident = FIVEKIND*ID_GROUP_SIZE;
	                ident+=pair_rank[POKER_HAND][0];
	        } else if ( straight && flush && ID_ExistsStraightFlush(h,str_hi,major_suit)) {
	        		straight_high = str_hi.byteValue();
	    	    	ident = STRAIGHTFLUSH*ID_GROUP_SIZE;
	                ident+=straight_high;
	        } else if (group_size[4] != 0) {
	    	    	ident = FOURKIND*ID_GROUP_SIZE;
	                ident+=pair_rank[4][0]*Card.NUM_RANKS;
	                pair_rank[4][1] = (byte)Card.NUM_RANKS;    // just in case 2 sets quads
	                ident+=ID_KickerValue(paired,1,pair_rank[4]);
	        } else if (group_size[3]>=2) {
	    	    	ident = FULLHOUSE*ID_GROUP_SIZE;
	                ident+=pair_rank[3][0]*Card.NUM_RANKS;
	                ident+=pair_rank[3][1];
	        } else if (group_size[3]==1 && group_size[2]!=0) {
	    	    	ident = FULLHOUSE*ID_GROUP_SIZE;
	                ident+=pair_rank[3][0]*Card.NUM_RANKS;
	                ident+=pair_rank[2][0];
	        } else if (flush) {
	    	    	ident = FLUSH*ID_GROUP_SIZE;
	                ident+=ID_KickerValueSuited(h,5,major_suit);
	        } else if (straight) {
	    	    	ident = STRAIGHT*ID_GROUP_SIZE;
	                ident+=straight_high;
	        } else if (group_size[3]==1) {
	    	    	ident = THREEKIND*ID_GROUP_SIZE;
	                ident+=pair_rank[3][0]*Card.NUM_RANKS*Card.NUM_RANKS;
	                ident+=ID_KickerValue(paired,max_hand-3,pair_rank[3]);
	        } else if (group_size[2]>=2) {
	    	    	ident = TWOPAIR*ID_GROUP_SIZE;
	                ident+=pair_rank[2][0]*Card.NUM_RANKS*Card.NUM_RANKS;
	                ident+=pair_rank[2][1]*Card.NUM_RANKS;
	                ident+=ID_KickerValue(paired,max_hand-4,pair_rank[2]);
	        } else if (group_size[2]==1) {
	    	    	ident = PAIR*ID_GROUP_SIZE;
	                ident+=pair_rank[2][0]*Card.NUM_RANKS*Card.NUM_RANKS*Card.NUM_RANKS;
	                ident+=ID_KickerValue(paired,max_hand-2,pair_rank[2]);
	        } else {
	    	    	ident = HIGH*ID_GROUP_SIZE;
	                ident+=ID_KickerValue(paired,max_hand,pair_rank[2]);
	        }
	        return ident;
	}


	private static int pow(int n, int p) {
		int res=1;
        while (p-- > 0)
                res *= n;
        return res;
	}

	private static final String[] hand_name = {
			"HIGH","PAIR","TWO PAIR","THREE KIND","STRAIGHT",
	       	"FLUSH","FULL HOUSE","FOUR KIND", "STRAIGHT FLUSH", "FIVE KIND" };

	private static final String[] rank_name = {"Two","Three","Four","Five","Six","Seven","Eight",
	         "Nine","Ten","Jack","Queen","King","Ace" };

	/**
	 * Return a string naming the hand
	 * @param rank calculated by rankHand_java()
	 */
	private static String name_hand(int rank) {

	    int type = (int)(rank/ID_GROUP_SIZE);
	    int ident = (int)(rank%ID_GROUP_SIZE),ident2;

	    String t = new String();

	    switch (type) {
			case HIGH:
				ident /= NUM_RANKS*NUM_RANKS*NUM_RANKS*NUM_RANKS;
				t = rank_name[ident] + " High";
				break;
			case FLUSH:
				ident /= NUM_RANKS*NUM_RANKS*NUM_RANKS*NUM_RANKS;
				t = "a Flush, " + rank_name[ident] + " High";
				break;
			case PAIR:
				ident /= NUM_RANKS*NUM_RANKS*NUM_RANKS;
				t = "a Pair of " + rank_name[ident] + "s";
				break;
			case TWOPAIR:
				ident2 = ident / (NUM_RANKS*NUM_RANKS);
				ident = (ident % (NUM_RANKS*NUM_RANKS)) / NUM_RANKS;
				t = "Two Pair, " + rank_name[ident2] + "s and " + rank_name[ident] + "s" ;
				break;
			case THREEKIND:
				t = "Three of a Kind, " + rank_name[ident/(NUM_RANKS*NUM_RANKS)] + "s";
				break;
			case FULLHOUSE:
				t = "a Full House, " + rank_name[ident/NUM_RANKS] + "s over " + rank_name[ident%NUM_RANKS] + "s";
				break;
			case FOURKIND:
				t = "Four of a Kind, " + rank_name[ident/NUM_RANKS] + "s";
				break;
			case STRAIGHT:
				t = "a " + rank_name[ident] + " High Straight";
				break;
			case STRAIGHTFLUSH:
				t = "a " + rank_name[ident] + " High Straight Flush";
				break;
			case FIVEKIND:
				t = "Five of a Kind, " + rank_name[ident] + "s";
				break;
			default:
				t = hand_name[type];
	    }

	    return t;
	}



}
