import flanagan.roots.*;
public class D_B_lw implements RealRootFunction {
	public double s,n,p,b,B0;
	public D_B_lw(double a1, double a2, double a3, double a4, double a5) {
		s=a1; n=a2; p=a3; b=a4; B0=a5;
	}
	public double function(double g) {
		double opgns = Math.pow(1+g*p,n*s);
		double omgnns = Math.pow(1-g,n-n*s);
		double tbr = b*n*s/g*opgns/(1+g*p);
		tbr -= b/g/g/p*(opgns-1);
		tbr += B0*omgnns*n*p*s*opgns/(1+g*p);
		tbr -= B0*omgnns/(1-g)*opgns*(n-n*s);
		return tbr;
	}
}