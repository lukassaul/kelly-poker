
import java.util.Random;

/**
* Simulate a player with set strategies
*
*  note - getBet gets ENTIRE bet, not just additional during a betting round!!
*
*/
public class Player {
	private Random r;
	public PlayerParameters params;
	private Card pocket1, pocket2, flop1, flop2, flop3, turn, river;
	public int bankroll;
	public int initialBankroll;
	public int simSize;
	private Table simTable;
	public int playerNumber;
	public boolean isActive;
	public int numHandsPlayed;
	public Bet fold, check;
	private float kellyBet;
	public int amountIn;
	public int rank;
	public boolean debug;

	public Player(int num) {
		params = new PlayerParameters();
		playerNumber = num;
		r=new Random();
		simTable = new Table();
		simSize = 100;
		isActive = false;
		numHandsPlayed = 0;
		bankroll = 0;
		initialBankroll = 0;
		fold = new Bet(0,false,true,false,false);
		check = new Bet(0,false,false,true,true);
		amountIn=0;
		rank = 0;
	}

	public void reset() {
		isActive = true;
		amountIn=0;
		rank = 0;
	}

	public Bet check() {
		return new Bet(amountIn,false,false,true,true);
	}

	public Hand getHand() {
		return new Hand(pocket1, pocket2, flop1, flop2, flop3, turn, river);
	}


	/**
	* At start of round of hands only..
	*/
	public void setBankroll(int n) {
		initialBankroll = n;
		bankroll = n;
	}

	public float getPerHandRatio() {
		int winnings = bankroll - initialBankroll;
		return (float)winnings/(float)numHandsPlayed;
	}

	public String getStatusLine() {
		String tbr = "";
		tbr += playerNumber + "\t";
		tbr += getPerHandRatio() +"\t";
		tbr += params.getParamsLine();
		tbr += "\n";
		return tbr;
	}

	public void setParams(PlayerParameters pp) {
		params = pp;
	}

	public void givePocket(Card p1, Card p2) {
		if (debug) System.out.println("pocket: " + p1 + " " + p2);
		pocket1 = p1;
		pocket2 = p2;
		numHandsPlayed++;
	}

	public void giveFlop(Card p1, Card p2, Card p3) {
		if (debug) System.out.println("flop: " + p1 + " " + p2 + " " + p3);
		flop1 = p1;
		flop2 = p2;
		flop3 = p3;
	}

	public void giveTurn(Card p1) {
		if (debug) System.out.println("turn: " + p1);
		turn = p1;
	}

	public void giveRiver(Card p1) {
		if (debug) System.out.println("river: " + p1);
		river = p1;
	}

	/**
	* Now encorporated into one method, getBet
	*
	*  Rounds: 0 pocket_bet
	*          1 pocket_call
	*          2 flop_bet ...   etc.
	*/
	public Bet getBet(int round, int currentBet, int numIn) {
		if (debug) System.out.println("request bet round: " +round + " current bet: " + currentBet + " numIn: " + numIn);
		Bet bet = new Bet();
		simTable.reset();
		simTable.numPlayers = numIn;
		float s = 0.0f;
		switch (round) {
			case 0: float s = simTable.simulate(simSize,pocket1,pocket2);
			case 1: float s = simTable.simulate(simSize,pocket1,pocket2);
			case 2: float s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3);
			case 3: float s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3);
			case 4: float s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3,turn);
			case 5: float s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3,turn);
			case 6: float s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3,turn,river);
			case 7: float s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3,turn,river);
		}
		kellyBet = (s*numIn+s-1)/numIn;
		float pbFactor = (float)r.nextGaussian();
		switch (round) {
			case 0: {
				pbFactor *= params.pb_bet_sig;
				pbFactor += params.pb_bet_mean;
			}
			case 0: {
				pbFactor *= params.pb_call_sig;
				pbFactor += params.pb_call_mean;
			}
			case 0: {
				pbFactor *= params.fb_bet_sig;
				pbFactor += params.fb_bet_mean;
			}
			case 0: {
				pbFactor *= params.fb_call_sig;
				pbFactor += params.fb_call_mean;
			}
			case 0: {
				pbFactor *= params.tb_bet_sig;
				pbFactor += params.tb_bet_mean;
			}
			case 0: {
				pbFactor *= params.tb_call_sig;
				pbFactor += params.tb_call_mean;
			}
			case 0: {
				pbFactor *= params.rb_bet_sig;
				pbFactor += params.rb_bet_mean;
			}
			case 0: {
				pbFactor *= params.rb_call_sig;
				pbFactor += params.rb_call_mean;
			}
		}

		float betAmount = pbFactor * kellyBet * bankroll;
		bet.amount = (int)betAmount;

		if (bet.amount >= bankroll) { // all in
			if (debug) System.out.println("all in in pocket to be: " + currentBet);
			bet.allIn = true;
			bet.amount = amountIn+bankroll;
		}
		else if (currentBet > bet.amount && currentBet > amountIn) {
			if (debug) System.out.println("folded in pocket to bet: " + currentBet);
			bet = fold;
		}
		else {
			// bet.amount >= currentBet
			bankroll -= (bet.amount - amountIn);
			amountIn=bet.amount;
		}
		if (debug) System.out.println("betting in pocket: " + bet.amount + " bankroll: " + bankroll);
		return bet;
	}

	/**
	* return -1 is fold..  return 0 is check();
	*
	*/
	public Bet getPocketBet(int currentBet, int numIn) {
		if (debug) System.out.println("request pocket bet: " + currentBet + " numIn: " + numIn);
		Bet bet = new Bet();
		simTable.reset();
		simTable.numPlayers = numIn;
		float s = simTable.simulate(simSize,pocket1,pocket2);
		kellyBet = (s*numIn+s-1)/numIn;

		float pbFactor = (float)r.nextGaussian();
		pbFactor *= params.pb_bet_sig;
		pbFactor += params.pb_bet_mean;

		float betAmount = pbFactor * kellyBet * bankroll;
		bet.amount = (int)betAmount;
		if (bet.amount >= bankroll) { // all in
			if (debug) System.out.println("all in in pocket");
			bet.allIn = true;
			bet.amount = bankroll;
		}
		else if (currentBet > bet.amount && currentBet >= amountIn) {
			if (debug) System.out.println("folded in pocket");
			return fold;
		}
		else {
			bankroll -= bet.amount - amountIn;
			amountIn=bet.amount;
		}
		if (debug) System.out.println("betting in pocket: " + bet.amount);
		return bet;
	}

	public Bet getPocketCall(int currentBet, int numIn) {
		if (debug) System.out.println("request pocket call: " + currentBet + " numIn: " + numIn);
		Bet bet = new Bet();
		simTable.reset();
		simTable.numPlayers = numIn;
		float s = simTable.simulate(simSize,pocket1,pocket2);
		kellyBet = (s*numIn+s-1)/numIn;

		float pbFactor = (float)r.nextGaussian();
		pbFactor *= params.pb_call_sig;
		pbFactor += params.pb_call_mean;

		float betAmount = pbFactor * kellyBet * bankroll;
		bet.amount = (int)betAmount;
		if (currentBet > betAmount) {
			if (debug) System.out.println("folded in pocket call");
			return fold;
		}
		else {
			bet.amount = currentBet;
			bankroll -= bet.amount - amountIn;
			amountIn=bet.amount;
		}
		if (debug) System.out.println("betting in pocket call: " + bet.amount);
		return bet;
	}

	public Bet getFlopBet(int currentBet, int numIn) {
		if (debug) System.out.println("request flop bet: " + currentBet + " numIn: " + numIn);
		Bet bet = new Bet();
		simTable.reset();
		simTable.numPlayers = numIn;
		float s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3);
		kellyBet = (s*numIn+s-1)/numIn;
		float pbFactor = (float)r.nextGaussian();
		pbFactor *= params.fb_bet_sig;
		pbFactor += params.fb_bet_mean;

		float betAmount = pbFactor * kellyBet * bankroll;
		bet.amount = (int)betAmount;
		if (bet.amount >= bankroll) { // all in
			bet.allIn = true;
			bet.amount = bankroll;
		}
		else if (currentBet > betAmount) {
			if (debug) System.out.println("folded in flop ");
			return fold;
		}
		else {
			bankroll -= bet.amount - amountIn;
			amountIn=bet.amount;
		}
		if (debug) System.out.println("betting in flop: " + bet.amount);
		return bet;
	}

	public Bet getFlopCall(int currentBet, int numIn) {
		if (debug) System.out.println("request flop call: " + currentBet + " numIn: " + numIn);
		Bet bet = new Bet();
		simTable.reset();
		simTable.numPlayers = numIn;
		float s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3);
		kellyBet = (s*numIn+s-1)/numIn;
		float pbFactor = (float)r.nextGaussian();
		pbFactor *= params.fb_call_sig;
		pbFactor += params.fb_call_mean;

		float betAmount = pbFactor * kellyBet * bankroll;
		bet.amount = (int) betAmount;

		if (currentBet > bet.amount && currentBet > amountIn) {
			if (debug) System.out.println("folded in flop call");
			return fold;
		}
		else{
			bet.amount = currentBet;
			bankroll -= bet.amount-amountIn;
			amountIn=bet.amount;
		}
		if (debug) System.out.println("betting in flop call: " + bet.amount);
		return bet;
	}


	public Bet getTurnBet(int currentBet, int numIn) {
		if (debug) System.out.println("request turn bet: " + currentBet + " numIn: " + numIn);
		Bet bet = new Bet();
		simTable.reset();
		simTable.numPlayers = numIn;
		float s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3,turn);
		kellyBet = (s*numIn+s-1)/numIn;
		float pbFactor = (float)r.nextGaussian();
		pbFactor *= params.tb_bet_sig;
		pbFactor += params.tb_bet_mean;

		float betAmount = pbFactor * kellyBet * bankroll;
		bet.amount = (int)betAmount;
		if (betAmount >= bankroll) { // all in
			bet.allIn = true;
			bet.amount = bankroll;
		}
		else if (currentBet > betAmount) {
			if (debug) System.out.println("folded in turn");
			return fold;
		}
		else {
			bankroll -= bet.amount-amountIn;
			amountIn=bet.amount;
		}
		if (debug) System.out.println("betting in turn: " + bet.amount);
		return bet;
	}

	public Bet getTurnCall(int currentBet, int numIn) {
		if (debug) System.out.println("request turn call: " + currentBet + " numIn: " + numIn);
		Bet bet = new Bet();
		simTable.reset();
		simTable.numPlayers = numIn;
		float s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3,turn);
		kellyBet = (s*numIn+s-1)/numIn;
		float pbFactor = (float)r.nextGaussian();
		pbFactor *= params.tb_call_sig;
		pbFactor += params.tb_call_mean;

		float betAmount = pbFactor * kellyBet * bankroll;
		bet.amount = (int)betAmount;

		if (currentBet > bet.amount && currentBet >= amountIn) {
			if (debug) System.out.println("folded in turn call");
			return fold;
		}
		else {
			bet.amount = currentBet;
			bankroll -= bet.amount-amountIn;
			amountIn=bet.amount;
		}
		if (debug) System.out.println("betting in turn call: " + bet.amount);
		return bet;
	}

	public Bet getRiverBet(int currentBet, int numIn) {
		if (debug) System.out.println("request river bet: " + currentBet + " numIn: " + numIn);
		Bet bet = new Bet();
		simTable.reset();
		simTable.numPlayers = numIn;
		float s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3,turn,river);
		kellyBet = (s*numIn+s-1)/numIn;

		float pbFactor = (float)r.nextGaussian();
		pbFactor *= params.rb_bet_sig;
		pbFactor += params.rb_bet_mean;

		float betAmount = pbFactor * kellyBet * bankroll;
		bet.amount = (int)betAmount;
		if (betAmount >= bankroll) { // all in
			bet.allIn = true;
			bet.amount = bankroll;
		}
		else if (currentBet > betAmount && currentBet >= amountIn) {
			if (debug) System.out.println("folded in river");
			return fold;
		}
		else {
			bankroll -= (int)betAmount - amountIn;
			amountIn=bet.amount;
		}
		if (debug) System.out.println("betting in river: " + bet.amount);
		return bet;
	}

	public Bet getRiverCall(int currentBet, int numIn) {
		if (debug) System.out.println("request river call: " + currentBet + " numIn: " + numIn);
		Bet bet = new Bet();
		simTable.reset();
		simTable.numPlayers = numIn;
		float s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3,turn,river);
		kellyBet = (s*numIn+s-1)/numIn;
		float pbFactor = (float)r.nextGaussian();
		pbFactor *= params.rb_call_sig;
		pbFactor += params.rb_call_mean;

		float betAmount = pbFactor * kellyBet * bankroll;
		bet.amount = (int)betAmount;
		if (currentBet > betAmount) {
			if (debug) System.out.println("folded in river call");
			return fold;
		}
		else {
			bet.amount = (int)currentBet;
			bankroll -= (int)betAmount - amountIn;
			amountIn=bet.amount;
		}
		if (debug) System.out.println("betting in river call: " + bet.amount);
		return bet;
	}

}



