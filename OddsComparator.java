import java.util.Random;
/**
*  Compare random wins and losses to
*   one or the other first or last
*/
public class OddsComparator {
	public Random r;
	public file outF = new file("oc_results4.txt");
	public float hand1;

	float s,v,f,P;
	public OddsComparator() {

		s=0.2f;
		v=8.0f;
		f=0.1f;
		P=0.2f;
		outF.initWrite(false);
		r=new Random();
		int numWins = 0;
		int numLosses = 0;
		int numWins2 = 0;
		int numLosses2 = 0;
		int numWins3 = 0;
		int numLosses3 = 0;
		int numWins4 = 0;
		int numLosses4 = 0;

		float bankroll = 1; // start at 1
		float bankroll2 = 1;
		float bankroll3 = 1;
		float bankroll4 = 1;

		for (int i=0; i<100; i++) {
			float game = r.nextFloat();
			float game2 = r.nextFloat();
			float game3 = r.nextFloat();
			float game4 = r.nextFloat();
			if (game<s) {
				numWins++;
				bankroll += bankroll*f*v + P;
			}
			else {
				numLosses++;
				bankroll -= bankroll*f;
			}
			if (game2<s) {
				numWins2++;
				bankroll2 += bankroll2*f*v + P;
			}
			else {
				numLosses2++;
				bankroll2 -= bankroll2*f;
			}
			if (game3<s) {
				numWins3++;
				bankroll3 += bankroll3*f*v + P;
			}
			else {
				numLosses3++;
				bankroll3 -= bankroll3*f;
			}
			if (game4<s) {
				numWins4++;
				bankroll4 += bankroll4*f*v + P;
			}
			else {
				numLosses4++;
				bankroll4 -= bankroll4*f;
			}
			outF.write(bankroll+"\t"+bankroll2+"\t"+bankroll3+"\t"+bankroll4+"\t"+
					getBankWinsFirst(i,s,f,v,P)+"\n");
		}
		outF.closeWrite();

	}

	/**
	*  Assumbes initial B0 = 1
	*
	*/
	public float getBankWinsFirst(int wins, int losses, float f, float v, float P) {
		float tbr=(float)Math.pow(1+f*v,wins)*(float)Math.pow(1-f,losses);
		float next = 0.0f;
		for (int i=0; i<wins; i++) {
			next+=Math.pow(1+f*v,i);
		}
		next *= P*(float)Math.pow(1-f,losses);
		return tbr+next;
	}
	/**
	*  Assumbes initial B0 = 1
	*
	*/
	public float getBankWinsFirst(int num, float s, float f, float v, float P) {
		int wins = (int)((float)num*s);
		int losses = num-wins;
		return getBankWinsFirst(wins,losses,f,v,P);
	}


	/**
	*  Assumbes initial B0 = 1
	*
	*/
	public float getBankLossesFirst(int wins, int losses, float f, float v, float P) {
		float tbr=(float)Math.pow(1+f*v,wins)*(float)Math.pow(1-f,losses);
		float next = 0.0f;
		for (int i=0; i<wins; i++) {
			next+=Math.pow(1+f*v,i);
		}
		next *= P;
		return tbr+next;
	}

	public static final void main(String[] args) {
		OddsComparator oc = new OddsComparator();
	}
}