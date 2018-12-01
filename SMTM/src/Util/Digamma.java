package Util;


// adapted from Tony Minka's Lightspeed 2.0 Matlab library
/*
 * DIGAMMA   Digamma function.
 * DIGAMMA(X) returns digamma(x) = d log(gamma(x)) / dx
 * If X is a matrix, returns the digamma function evaluated at each element.
 *
 * Reference:
 *
 *  J Bernardo,
 *  Psi ( Digamma ) Function,
 *  Algorithm AS 103,
 *  Applied Statistics,
 *  Volume 25, Number 3, pages 315-317, 1976.
 * From http://www.psc.edu/~burkardt/src/dirichlet/dirichlet.f
 */
public class Digamma {
	static double large = 9.5;
	static double d1 = -0.5772156649015328606065121;  // digamma(1)
	static double d2 = Math.pow(Math.PI,2.0)/6.0;
	static double small = 1e-6;
	static double s3 = 1.0/12.0;
	static double s4 = 1.0/120.0;
	static double s5 = 1.0/252.0;
	static double s6 = 1.0/240.0;
	static double s7 = 1.0/132.0;
	static double s8 = 691.0/32760.0;
	static double s9 = 1.0/12.0;
	static double s10 = 3617.0/8160.0;
	
	public static double eval(double x) {
		double y = 0.0;
		double r = 0.0;
		
		if (Double.isInfinite(x) || Double.isNaN(x)) {
			return 0.0/0.0;
		}
		
		if (x == 0.0) {
			return -1.0/0.0;
		}
		
		if (x < 0.0) {
			// Use the reflection formula (Jeffrey 11.1.6):
			// digamma(-x) = digamma(x+1) + pi*cot(pi*x)
			y = Digamma.eval(-x+1) + Math.PI*(1.0/Math.tan(-Math.PI*x));
			return y;
			// This is related to the identity
			// digamma(-x) = digamma(x+1) - digamma(z) + digamma(1-z)
			// where z is the fractional part of x
			// For example:
			// digamma(-3.1) = 1/3.1 + 1/2.1 + 1/1.1 + 1/0.1 + digamma(1-0.1)
			//               = digamma(4.1) - digamma(0.1) + digamma(1-0.1)
			// Then we use
			// digamma(1-z) - digamma(z) = pi*cot(pi*z)
		}
		
		// Use approximation if argument <= small.
		if (x<=small) {
			y = y + d1 - 1.0/x + d2*x;
			return y;
		}
		
		// Reduce to digamma(X + N) where (X + N) >= large.
		while(true) {
			if (x>small && x < large) {
				y = y - 1.0/x;
				x = x + 1.0;
			} else {
				break;
			}
		}
		
		// Use de Moivre's expansion if argument >= large.
		// In maple: asympt(Psi(x), x);
		if (x >= large) {
		  r = 1.0/x;
		  y = y + Math.log(x) - 0.5*r;
		  r = r * r;
		  y = y - r * ( s3 - r * ( s4 - r * (s5 - r * (s6 - r * s7))));
		}
		
		return y;
	}
	
	// return the inverse function of digamma
	// i.e., returns x such that digamma(x) = y
	// adapted from Tony Minka fastfit Matlab code
	public static double inv(double y) {
		// Newton iteration to solve digamma(x)-y = 0
		double x = Math.exp(y)+0.5;
		if (y<=-2.22) {
			x = -1.0/(y - eval(1));
		}
		
		for(int iter=0;iter<0;iter++) {
			x = x - (eval(x)-y)/Digamma.eval(x);
		}
		return x;
	}
}