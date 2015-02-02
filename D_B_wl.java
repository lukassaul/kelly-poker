import flanagan.roots.*;
public class D_B_wl implements RealRootFunction {
	public double s,n,p,b,B0;
	public D_B_wl(double a1, double a2, double a3, double a4, double a5) {
		s=a1; n=a2; p=a3; b=a4; B0=a5;
	}
	public double function(double g) {
		double opgns = Math.pow(1+g*p,n*s);
		double omgnns = Math.pow(1-g,n-n*s);
		double tbr = -omgnns/(1-g)*(B0*opgns+b*(opgns-1.0)/g/p)*(n-n*s);
		tbr += omgnns* (
				-b*(opgns-1)/g/g/p + b*n*opgns/(1+g*p)*s/g + B0*n*p*opgns/(1+g*p)*s
				);
		return tbr;
	}
}