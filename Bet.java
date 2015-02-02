/**
*  Convenience class for passing bets around from players to tables or casinos
*
*/
public class Bet {

	public int amount;
	public boolean allIn;
	public boolean fold;
	public boolean call;
	public boolean check;

	public Bet() {
		amount = 0;
		allIn = false;
		fold = false;
		call = false;
		check = false;
	}

	public Bet(int a) {
		amount = a;
		allIn = false;
		fold = false;
		call = false;
		check = false;
	}

	public Bet(int a, boolean ai, boolean f, boolean c, boolean ch) {
		amount = a;
		allIn = ai;
		fold = f;
		call = c;
		check = ch;
	}
}
