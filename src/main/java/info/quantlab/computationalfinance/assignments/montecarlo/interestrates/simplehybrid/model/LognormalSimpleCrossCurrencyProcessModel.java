package info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.model;

import info.quantlab.computationalfinance.assignments.montecarlo.processmodel.LognormalProcessModel;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.process.MonteCarloProcess;
import net.finmath.stochastic.RandomVariable;
import net.finmath.stochastic.Scalar;

/**
 * This class provides the model for the simulation of the domestic and foreign
 * Libors and for the forward FX rate. All the processes are supposed to follow
 * log-normal dynamics. For this reason, one simulates the logarithm of such
 * processes and then applies the exponential. In this way, it is possible to
 * get rid of the propagation of the discretization error, since one does not
 * have the current value of the processes in the Euler scheme. The processes
 * are possibly correlated.
 *
 * @author Andrea Mazzon
 *
 */
public class LognormalSimpleCrossCurrencyProcessModel implements LognormalProcessModel {

	private double periodStart;
	private double periodEnd;

	private double domesticZeroBond;

	private double initialValueDomesticForwardRate;
	private double initialValueForeignForwardRate;
	private double initialValueFXForward;// we simulate the process FFX

	private double volatilityDomestic;
	private double volatilityForeign;
	private double volatilityFXForward;

	private double correlationDomFor;
	private double correlationFXDomestic;
	private double correlationFXForeign;

	/**
	 * It creates an object to provides the model for the simulation of the domestic
	 * and foreign Libors and for the forward FX rate.
	 *
	 * @param periodStart,                     T_1
	 * @param periodEnd,                       T_2
	 * @param domesticZeroBond,                P^d(T_2;0)
	 * @param initialValueDomesticForwardRate, L^d(T_1, T_2;0)
	 * @param initialValueForeignForwardRate,  L^f(T_1, T_2;0)
	 * @param initialValueFX,                  FFX(T_2;0)
	 * @param volatilityDomestic,              the log-volatility of the process
	 *                                         (L^d(T_1, T_2;t))_{0 <= t <= T_1}
	 * @param volatilityForeign,               the log-volatility of the process
	 *                                         (L^f(T_1, T_2;t))_{0 <= t <= T_1}
	 * @param volatilityFXForward,             the log-volatility of the process
	 *                                         (FFX(T_2;t))_{0 <= t <= T_1}
	 * @param correlationDomFor,               the correlation between L^d and L^f
	 * @param correlationFXDomestic,           the correlation between L^d and FFX
	 * @param correlationFXForeign,            the correlation between L^f and FFX
	 */
	public LognormalSimpleCrossCurrencyProcessModel(double periodStart, double periodEnd, double domesticZeroBond,
			double initialValueDomesticForwardRate, double initialValueForeignForwardRate, double initialValueFX,
			double volatilityDomestic, double volatilityForeign, double volatilityFXForward, double correlationDomFor,
			double correlationFXDomestic, double correlationFXForeign) {
		super();
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.domesticZeroBond = domesticZeroBond;
		this.initialValueDomesticForwardRate = initialValueDomesticForwardRate;
		this.initialValueForeignForwardRate = initialValueForeignForwardRate;
		this.initialValueFXForward = initialValueFX;
		this.volatilityDomestic = volatilityDomestic;
		this.volatilityForeign = volatilityForeign;
		this.volatilityFXForward = volatilityFXForward;
		this.correlationDomFor = correlationDomFor;
		this.correlationFXDomestic = correlationFXDomestic;
		this.correlationFXForeign = correlationFXForeign;
	}

	@Override
	public int getNumberOfComponents() {
		return 3;
	}

	@Override
	public Double[] getInitialValue() {
		return new Double[] { initialValueDomesticForwardRate, initialValueForeignForwardRate, initialValueFXForward };
	}

	@Override
	public RandomVariable getNumeraire(MonteCarloProcess process, double time) {
		/*
		 * The numeraire is the T_2-domestic zero coupon bond. We want to return it at
		 * times 0, T_1 and T_2. The only issue is how to get it at time T_1 only
		 * knowing the values of the Libors.
		 */
		if (time == 0) {
			return new Scalar(domesticZeroBond); // P^d(T_2;0)
		} else if (time == periodStart) {
			try {
				RandomVariable domesticForwardRate = process.getProcessValue(process.getTimeIndex(time),
						0 /* componentIndex */);// This is L^d(T_1, T_2;T_1)
				// 1/(L^d(T_1,T_2;T_1)*(T_2-T_1)+1)=1/(P^d(T_1;T_1)/P^d(T_2;T_1)-1+1)=P^d(T_2;T_1)
				return domesticForwardRate.mult(periodEnd - periodStart).add(1.0).invert();
			} catch (CalculationException e) {
				throw new RuntimeException(e);
			}
		} else if (time == periodEnd) {
			return new Scalar(1.0);// P^d(T_2;T_2)=1
		} else {
			throw new IllegalArgumentException("Time not supported; " + time);
		}
	}

	@Override
	public Double[] getDrift(int timeIndex) {
		/*
		 * Pay attention here: the drift of the processes L^d and FFX is 0, BUT here we
		 * simulate the logarithm! If we apply Itô's formula to the process with
		 * dynamics dL^d(t)=\sigma^dL^d(t)dW_1(t), we find that Y^d:=log(L^d) has
		 * dynamics dY^d(t)=-1/2\sigma^d*\sigma^d + \sigma^d dW_1(t). So the drift is
		 * not zero! Same thing for FFX and almost same thing (apart from the fact that
		 * in the drift you already had the quanto adjustment) for L^f.
		 */
		return new Double[] { -0.5 * volatilityDomestic * volatilityDomestic,
				-0.5 * volatilityForeign * volatilityForeign // this comes from Itô
						// the next term is the quanto adjustment
						- volatilityForeign * volatilityFXForward * correlationFXForeign,
				-0.5 * volatilityFXForward * volatilityFXForward };
	}

	@Override
	public int getNumberOfFactors() {
		return 3;
	}

	@Override
	public Double[] getFactorLoading(int timeIndex, int componentIndex) {
		/*
		 * First thing: note that the fact that you don't have to multiply by the
		 * current value of the process is also due to the application of Itô's formula
		 * to the logarithm! See above
		 */
		switch (componentIndex) {
		/*
		 * Second thing: how to compute the lambdas? You have the three processes L^d,
		 * L^f, FFX following the dynamics dL^d(t) = (...)dt + \sigma^dL^d(t)dB^1(t),
		 * dL^f(t) = (...)dt + \sigma^fL^f(t)dB^2(t),
		 * dFFX(t)=(...)dt+\sigma^FFFX(t)dB^3(t), where B^1, B^2, B^3 are possibly
		 * correlated Brownian motions. You express B^1, B^2, B^3 in terms of the
		 * independent Brownian motion W^1, W^2, W^3. That is you have, for any i=1,2,3,
		 * B^i= \lambda _{1i} W^1+\lambda _{2i} W^2+\lambda _{3i} W^3, i=1,2,3. You have
		 * to choose the values of the lambdas such that
		 * d<B^1,B^2>(t)=correlationDomFor*dt, d<B^2,B^3>(t)=correlationFXForeign*dt,
		 * d<B^1,B^3>(t)= correlationDomForeign*dt, with the additional requirement
		 * (ensuring that B^1, B^2, B^3 are Brownian motions) that for any i=1,2,3 holds
		 * \lambda _{1i}^2+\lambda _{2i}^2+\lambda _{3i}^2 = 1. You have freedom: for
		 * example, you can choose \lambda_{11} = 1, \lambda_{12} = 0, \lambda_{13} = 0.
		 * Then in order d<B^1,B^2>(t)=correlationDomFor*dt to be satisfied, you have to
		 * choose \lambda_{21}=correlationDomFor, setting then
		 * \lambda_{22}=sqrt(1-correlationDomFor^2) and \lambda_{23} = 0. The choices of
		 * \lambda_{13}, \lambda_{23}, \lambda_{33} are then made in the same spirit:
		 * \lambda_{31}=correlationFXDomestic so that
		 * d<B^1,B^3>(t)=correlationFXDomestic*dt. Then you choose \lambda_{23} such
		 * that d<B^1,B^3>(t)=(\lambda_{21}*\lambda_{31}+\lambda_{22}\lambda_{32})=
		 * correlationFXForeign*dt and finally \lambda_{33} such that we have
		 * \lambda_{13}^2+\lambda _{23}^2+\lambda _{33}^2=1
		 */
		case 0:
			return new Double[] { volatilityDomestic, 0.0, 0.0 };
		case 1:
			return new Double[] { volatilityForeign * correlationDomFor,
					volatilityForeign * Math.sqrt(1 - correlationDomFor * correlationDomFor), 0.0 };
		case 2:
			double factorLoadingForeign = (correlationFXForeign - correlationDomFor * correlationFXDomestic)
					/ Math.sqrt(1 - correlationDomFor * correlationDomFor);
			return new Double[] { volatilityFXForward * correlationFXDomestic,
					volatilityFXForward * factorLoadingForeign,
					volatilityFXForward * Math.sqrt(1 - correlationFXDomestic * correlationFXDomestic
							- factorLoadingForeign * factorLoadingForeign) };
		default:
			throw new IllegalArgumentException();
		}
	}
}