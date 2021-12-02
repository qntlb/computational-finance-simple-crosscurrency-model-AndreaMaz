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

public class SimpleCrossCurrencyModelWithSingleMaturity implements SimpleCrossCurrencyModel {

	private double periodStart;
	private double periodEnd;
	private double domesticZeroBond;
	private double foreignZeroBond;	

	private BrownianMotion brownianMotion;

	private transient MonteCarloProcess process;

	public SimpleCrossCurrencyModelWithSingleMaturity(
			double initialValueDomesticForwardRate, double initialValueForeignForwardRate, double initialValueFX,
			double volatilityDomestic, double volatilityForeign, double volatiltiyFXForward, double correlationDomFor,
			double correlationFXDomenstic, double correlationFXForeign, double periodStart, double periodEnd, double domesticZeroBond, double foreignZeroBond,
			BrownianMotion brownianMotion) {
		super();
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.domesticZeroBond = domesticZeroBond;
		this.foreignZeroBond = foreignZeroBond;		
		this.brownianMotion = brownianMotion;

		LognormalProcessModel lognormalProcessModel = null; // TODO: Create an object of your process model here
	
		ProcessModel processModel = new ProcessModelFromLognormalProcessModel(lognormalProcessModel);

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
		// TODO: Implement the forward rate here (depending on the arguments)
		throw new RuntimeException("Methode not implemented");
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
	public MonteCarloSimulationModel getCloneWithModifiedData(Map<String, Object> dataModified) throws CalculationException {
		throw new UnsupportedOperationException();
	}	
}
