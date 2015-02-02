
import flanagan.roots.*;

public class PotBet {

	public PotBet() {
		D_B_wl dbwl= new D_B_wl(0.6,10.0,1.0,10.0,100.0);
		D_B_lw dblw= new D_B_lw(0.6,10.0,1.0,10.0,100.0);
		double test1 = RealRoot.bisect(dbwl,0.01,0.6,0.01);
		double test2 = RealRoot.bisect(dblw,0.01,0.6,0.01);
		System.out.println("testwl: " + test1 + " testlw: " + test2);
	}

	public final static void main(String[] args) {
		PotBet pb = new PotBet();
	}
}





