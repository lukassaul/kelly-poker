import java.util.Arrays;
import java.util.Random;

/**
* Simulate a player with set strategies
*
*  note - getBet gets ENTIRE bet, not just additional during a betting round!!
*
*/
public class Player implements Comparable<Player> {

	private Random r;
	public PlayerParameters params;
	private Card pocket1, pocket2, flop1, flop2, flop3, turn, river;
	public int bankroll;
	public int initialBankroll;
	public int amountBought;
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
	public boolean broke;
	public int winnings;

	public Player(int num) {
		params = new PlayerParameters();
		playerNumber = num;
		r=new Random();
		simTable = new Table();
		//simTable.debug = true;
		simSize = 100;
		isActive = false;
		numHandsPlayed = 0;
		bankroll = 0;
		initialBankroll = 0;
		fold = new Bet(0,false,true,false,false);
		check = new Bet(0,false,false,true,true);
		amountIn=0;
		rank = 0;
		broke=true;
		debug = false;
		amountBought = 0;
		winnings = 0;
	}

	public int compareTo(Player p) {
		return winnings - p.winnings;
	}

	public void reset() {
		isActive = false;
		amountIn=0;
		rank = 0;
		pocket1 = null;
		pocket2 = null;
		flop1=null;
		flop2=null;
		flop3=null;
		turn=null;
		river=null;
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
		//int winnings = bankroll - initialBankroll;
		if (numHandsPlayed>0) return (float)winnings/(float)numHandsPlayed;
		return 0;
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
	//	if (debug) o("given pocket: " + p1 + " " + p2);
		pocket1 = p1;
		pocket2 = p2;
		numHandsPlayed++;
	}

	public void giveFlop(Card p1, Card p2, Card p3) {
	//	if (debug) o("given flop: " + p1 + " " + p2 + " " + p3);
		flop1 = p1;
		flop2 = p2;
		flop3 = p3;
	}

	public void giveTurn(Card p1) {
	//	if (debug) o("given turn: " + p1);
		turn = p1;
	}

	public void giveRiver(Card p1) {
	//	if (debug) o("given river: " + p1);
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
		boolean call=false;
		if (round%2==1) call = true;
		Bet bet = new Bet();
		simTable.reset();
		simTable.numPlayers = numIn;
		float s = 0.0f;
		if (round == 0)	 {s = simTable.simulate(simSize,pocket1,pocket2);}
		if (round == 1)	 {s = simTable.simulate(simSize,pocket1,pocket2);}
		if (round == 2)	 {s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3);}
		if (round == 3)	 {s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3);}
		if (round == 4)	 {s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3,turn);}
		if (round == 5)	 {s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3,turn);}
		if (round == 6)	 {s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3,turn,river);}
		if (round == 7)	 {s = simTable.simulate(simSize,pocket1,pocket2,flop1,flop2,flop3,turn,river);}

		// that's all there is to it!
		kellyBet = (s*numIn+s-1)/numIn;

		// ok we may want to bet.  check parameters for multiplicative factors.
		float pbFactor = (float)r.nextGaussian();
		switch (round) {
			case 0: {
				pbFactor *= params.pb_bet_sig;
				pbFactor += params.pb_bet_mean;
			}
			case 1: {
				pbFactor *= params.pb_call_sig;
				pbFactor += params.pb_call_mean;
			}
			case 2: {
				pbFactor *= params.fb_bet_sig;
				pbFactor += params.fb_bet_mean;
			}
			case 3: {
				pbFactor *= params.fb_call_sig;
				pbFactor += params.fb_call_mean;
			}
			case 4: {
				pbFactor *= params.tb_bet_sig;
				pbFactor += params.tb_bet_mean;
			}
			case 5: {
				pbFactor *= params.tb_call_sig;
				pbFactor += params.tb_call_mean;
			}
			case 6: {
				pbFactor *= params.rb_bet_sig;
				pbFactor += params.rb_bet_mean;
			}
			case 7: {
				pbFactor *= params.rb_call_sig;
				pbFactor += params.rb_call_mean;
			}
		}

		float betAmount = pbFactor * kellyBet * bankroll;
		//if (debug) o(" S calc: " + s + " k calc: " + kellyBet + "betAmount: " + betAmount);
		bet.amount = (int)betAmount;

		if (debug) {
			//o("asked to bet round: " +round + " current bet: " + currentBet + " numIn: " + numIn);
			o(pocket1 + " " + pocket2 + " " + flop1 + " " + flop2 + " " + flop3 + " "
						+ turn + " " + river + " a_in:" + amountIn + " cb:" + currentBet + " calc: " + betAmount);
		}

		//  ***  cases for what to bet ***
		if (currentBet < amountIn) System.out.println("********Something wrong, currentBet < amountIn******");

		// should we ever play when we are expected to lose?
		else if (bet.amount < 0  &&  currentBet > amountIn) {
			//if (debug) o("folded in round " + round + " to bet: " + currentBet);
			bet = fold;
			bet.amount=currentBet;
		}

		// yes, if we can call
		else if (bet.amount <= currentBet && currentBet==amountIn) {
			bet.call=true;
			bet.amount = currentBet;
		}

		// all in!
		else if ((bet.amount - amountIn) >= bankroll) {
			bet.allIn = true;
			bet.amount = amountIn+bankroll;
			amountIn +=bankroll;
			bankroll = 0;
		}
		else if (currentBet>bet.amount) {
			//if (debug) o("folded in round: " + round+ " to bet: " + currentBet);
			bet = fold;
			bet.amount = currentBet;
		}

		// our bet doesn't match the previous bet so fold - we had a chance to call already if 0 bet
		else if (!call) {

			// only case left is make the bet as usual
			else if (bet.amount>currentBet && round%2==0) {
				// this is an initial bet phase
				// bet.amount >= currentBet
				bankroll -= (bet.amount - amountIn);
				amountIn=bet.amount;
			}
		}

		else if (bet.amount>currentBet) {
			// this is a call phase
			bankroll -= currentBet - amountIn;
			bet.amount = currentBet;
			amountIn = currentBet;
		}

		else {	o("****** ---  missing bet?");}

		if (debug) {
			String ss = "";
			if (bet.fold) ss+= "Folding: ";
			if (bet.allIn) ss+="Going all in: ";
			if (bet.call) ss+="Calling on already in : ";
			ss+="placing bet in round " + round + " : " + bet.amount + " bankroll: " + bankroll;
			o(ss);
		}


		if (bet.amount<0) o("NEGATIVE BET");
		if (bankroll==0) broke=true;
		return bet;
	}

	public void o(String s) {
		System.out.println(playerNumber + ": " + s);
	}
}
