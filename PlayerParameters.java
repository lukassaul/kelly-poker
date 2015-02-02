import java.util.Random;

/**
*
*/
public class PlayerParameters {

	/**
	*multiplicative factors
	*/
	public float pb_bet_mean;
	public float pb_bet_sig;
	public float pb_call_mean;
	public float pb_call_sig;
	public float fb_bet_mean;
	public float fb_bet_sig;
	public float fb_call_mean;
	public float fb_call_sig;
	public float tb_bet_mean;
	public float tb_bet_sig;
	public float tb_call_mean;
	public float tb_call_sig;
	public float rb_bet_mean;
	public float rb_bet_sig;
	public float rb_call_mean;
	public float rb_call_sig;

	/**
	* bluff parameters
	*/
	public float pb_bluff;
	public float pb_bluff_mean;
	public float pb_bluff_sig;
	public float fb_bluff;
	public float fb_bluff_mean;
	public float fb_bluff_sig;
	public float tb_bluff;
	public float tb_bluff_mean;
	public float tb_bluff_sig;
	public float rb_bluff;
	public float rb_bluff_mean;
	public float rb_bluff_sig;

	public PlayerParameters () { }

	public String getParamsLine() {
		String tbr = "";
		tbr += pb_bet_mean + "\t";
		tbr += pb_bet_sig + "\t";
		tbr += pb_call_mean + "\t";
		tbr += pb_call_sig + "\t";
		tbr += fb_bet_mean + "\t";
		tbr += fb_bet_sig + "\t";
		tbr += fb_call_mean + "\t";
		tbr += fb_call_sig + "\t";
		tbr += tb_bet_mean + "\t";
		tbr += tb_bet_sig + "\t";
		tbr += tb_call_mean + "\t";
		tbr += tb_call_sig + "\t";
		tbr += rb_bet_mean + "\t";
		tbr += rb_bet_sig + "\t";
		tbr += rb_call_mean + "\t";
		tbr += rb_call_sig + "\t";
		return tbr;
	}

	public void setConst(float f) {
		pb_bet_mean = f;
		pb_bet_sig = 0;
		pb_call_mean = f;
		pb_call_sig = 0;
		fb_bet_mean = f;
		fb_bet_sig = 0;
		fb_call_mean = f;
		fb_call_sig = 0;
		tb_bet_mean = f;
		tb_bet_sig = 0;
		tb_call_mean = f;
		tb_call_sig = 0;
		rb_bet_mean = f;
		rb_bet_sig = 0;
		rb_call_mean = f;
		rb_call_sig = 0;
	}


	/**
	* Try w/ (5,5)
	*/
	public void ranSet1(float meanMax, float meanMin, float sigMax) {
		Random r = new Random();
		pb_bet_mean = meanMin+((meanMax-meanMin)*r.nextFloat());
		pb_bet_sig = sigMax*r.nextFloat();
		pb_call_mean = meanMin+((meanMax-meanMin)*r.nextFloat());
		pb_call_sig = sigMax*r.nextFloat();
		fb_bet_mean = meanMin+((meanMax-meanMin)*r.nextFloat());
		fb_bet_sig = sigMax*r.nextFloat();
		fb_call_mean = meanMin+((meanMax-meanMin)*r.nextFloat());
		fb_call_sig = sigMax*r.nextFloat();
		tb_bet_mean = meanMin+((meanMax-meanMin)*r.nextFloat());
		tb_bet_sig = sigMax*r.nextFloat();
		tb_call_mean = meanMin+((meanMax-meanMin)*r.nextFloat());
		tb_call_sig = sigMax*r.nextFloat();
		rb_bet_mean = meanMin+((meanMax-meanMin)*r.nextFloat());
		rb_bet_sig = sigMax*r.nextFloat();
		rb_call_mean = meanMin+((meanMax-meanMin)*r.nextFloat());
		rb_call_sig = sigMax*r.nextFloat();
	}

	/**
	* Keeps mean and sig the same for all rounds
	*/
	public void ranSet2(float meanMax, float meanMin, float sigMax) {
		Random r = new Random();
		pb_bet_mean = meanMin+((meanMax-meanMin)*r.nextFloat());
		pb_bet_sig = sigMax*r.nextFloat();
		pb_call_mean = pb_bet_mean;
		pb_call_sig = pb_bet_sig;
		fb_bet_mean = pb_bet_mean;
		fb_bet_sig = pb_bet_sig;
		fb_call_mean = pb_bet_mean;
		fb_call_sig = pb_bet_sig;
		tb_bet_mean = pb_bet_mean;
		tb_bet_sig = pb_bet_sig;
		tb_call_mean = pb_bet_mean;
		tb_call_sig = pb_bet_sig;
		rb_bet_mean = pb_bet_mean;
		rb_bet_sig = pb_bet_sig;
		rb_call_mean = pb_bet_mean;
		rb_call_sig = pb_bet_sig;
	}
}