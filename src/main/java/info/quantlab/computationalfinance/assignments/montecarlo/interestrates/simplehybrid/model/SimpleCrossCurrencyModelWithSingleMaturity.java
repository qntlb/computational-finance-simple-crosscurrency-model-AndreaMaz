package info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.model;

import java.util.Map;

import info.quantlab.computationalfinance.assignments.montecarlo.processmodel.LognormalProcessModel;
import info.quantlab.computationalfinance.assignments.montecarlo.processmodel.ProcessModelFromLognormalProcessModel;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.MonteCarloSimulationModel;
import net.finmath.montecarlo.model.ProcessModel;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.montecarlo.process.MonteCarloProcess;
import net.finmath.stochastic.RandomVariable;
import net.finmath.stochastic.Scalar;
import net.finmath.time.TimeDiscretization;

/**
 * This class provides the simulation of the domestic and foreign Libor, and of
 * the forward FX rate. All the processes are supposed to follow log-normal
 * dynamics.
 *
 * @author Andrea Mazzon
 *
 */
public class SimpleCrossCurrencyModelWithSingleMaturity implements SimpleCrossCurrencyModel {

	private double periodStart;
	private double periodEnd;
	private double domesticZeroBond;
	private double foreignZeroBond;

	private BrownianMotion brownianMotion;

	private transient MonteCarloProcess process;

	/**
	 * It constructs an object to simulate domestic and foreign Libor, and of the
	 * forward FX rate.
	 *
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
	 * @param periodStart,                     T_1
	 * @param periodEnd,                       T_2
	 * @param domesticZeroBond,                P^d(T_2;0)
	 * @param foreignZeroBond,                 P^f(T_2;0)
	 * @param brownianMotion,                  the Brownian motion to build the
	 *                                         simulation of the processes: these
	 *                                         represents the independent stochastic
	 *                                         drivers W^1, W^2, W^3
	 */
	public SimpleCrossCurrencyModelWithSingleMaturity(double initialValueDomesticForwardRate,
			double initialValueForeignForwardRate, double initialValueFX, double volatilityDomestic,
			double volatilityForeign, double volatilityFXForward, double correlationDomFor,
			double correlationFXDomestic, double correlationFXForeign, double periodStart, double periodEnd,
			double domesticZeroBond, double foreignZeroBond, BrownianMotion brownianMotion) {
		super();
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.domesticZeroBond = domesticZeroBond;
		this.foreignZeroBond = foreignZeroBond;
		this.brownianMotion = brownianMotion;

		/*
		 * Here we have to provide an object of type LognormalProcessModel: we want to
		 * give the data identifying the model we want to simulate.
		 */
		LognormalProcessModel lognormalProcessModel = new LognormalSimpleCrossCurrencyProcessModel(periodStart,
				periodEnd, domesticZeroBond, initialValueDomesticForwardRate, initialValueForeignForwardRate,
				initialValueFX * foreignZeroBond / domesticZeroBond, volatilityDomestic, volatilityForeign,
				volatilityFXForward, correlationDomFor, correlationFXDomestic, correlationFXForeign);

		/*
		 * Then, such an object gets passed to the constructor of
		 * ProcessModelFromLognormalProcessModel: it is basically wrapped in a class
		 * providing some other methods to define the model we simulate. For example,
		 * it's here that one specifies that the logarithm is actually simulated.
		 */
		ProcessModel processModel = new ProcessModelFromLognormalProcessModel(lognormalProcessModel);

		/*
		 * Then, we link together the specification of the model and the stochastic
		 * driver: the simulation is performed.
		 */
		process = new EulerSchemeFromProcessModel(processModel, brownianMotion);

	}

	private RandomVariable getProcessValue(double time, int componentIndex) {
		try {
			return process.getProcessValue(process.getTimeIndex(time), componentIndex);
		} catch (CalculationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public RandomVariable getForwardRate(int currency, double time, double periodStart, double periodEnd) {
		return getProcessValue(time, currency /* componentIndex */);
	}

	@Override
	public RandomVariable getFXRate(int currency, double time) {
		/*
		 * Here we want to return the FX rate relative to T_2, call it FX(T_2), based on
		 * the data we have. We know that FX(T_2; t) = FFX(T_2; t) P^d(T_2;t) /
		 * P^f(T_2;t).
		 */
		if (currency == 0) {
			return new Scalar(1.0);
		} else if (currency == 1) {
			if (time == 0) {
				/*
				 * At time 0, we can apply the formula above directly because we have P^d(T_2;0)
				 * and P^f(T_2;0), since they are the initial values of the forwards.
				 */
				RandomVariable forwardFXRate = getProcessValue(time, 2 /* componentIndex */);

				return forwardFXRate.div(foreignZeroBond).mult(domesticZeroBond);
			} else if (time == periodEnd) {

				// At time T_2, it is even easier: P^d(T_2;T_2) = P^f(T_2;T_2) = 1
				RandomVariable forwardFXRate = getProcessValue(time, 2 /* componentIndex */);

				return forwardFXRate;
			} else if (time == periodStart) {
				/*
				 * At time T_1, we need some more work, because we have to recover P^d(T_2;T_1)
				 * and P^f(T_2;T_1) from P^d(T_1,T_2;T_1) and P^f(T_1,T_2;T_1)
				 */
				RandomVariable forwardFXRate = getProcessValue(time, 2 /* componentIndex */);
				RandomVariable domesticForwardRate = getProcessValue(time, 0 /* componentIndex */);
				RandomVariable foreignForwardRate = getProcessValue(time, 1 /* componentIndex */);

				// P^i(T_1,T_2;T_1) = 1/(L^i(T_1,T_2;T_1)*(T_2-T_1)+1), i=d,f
				return forwardFXRate.mult(foreignForwardRate.mult(periodEnd - periodStart).add(1.0))
						.div(domesticForwardRate.mult(periodEnd - periodStart).add(1.0));
			} else {
				throw new IllegalArgumentException("Time not supported: " + time);
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public RandomVariable getNumeraire(double time) {
		try {
			return process.getModel().getNumeraire(process, time);
		} catch (CalculationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getNumberOfPaths() {
		return brownianMotion.getNumberOfPaths();
	}

	@Override
	public TimeDiscretization getTimeDiscretization() {
		return brownianMotion.getTimeDiscretization();
	}

	@Override
	public double getTime(int timeIndex) {
		return getTimeDiscretization().getTime(timeIndex);
	}

	@Override
	public int getTimeIndex(double time) {
		return getTimeDiscretization().getTimeIndex(time);
	}

	@Override
	public RandomVariable getRandomVariableForConstant(double value) {
		return brownianMotion.getRandomVariableForConstant(value);
	}

	@Override
	public RandomVariable getMonteCarloWeights(int timeIndex) throws CalculationException {
		return new Scalar(1.0);
	}

	@Override
	public RandomVariable getMonteCarloWeights(double time) throws CalculationException {
		return new Scalar(1.0);
	}

	@Override
	public MonteCarloSimulationModel getCloneWithModifiedData(Map<String, Object> dataModified)
			throws CalculationException {
		throw new UnsupportedOperationException();
	}
}