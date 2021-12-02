package info.quantlab.computationalfinance.assignments.montecarlo.processmodel;

import net.finmath.montecarlo.process.MonteCarloProcess;
import net.finmath.stochastic.RandomVariable;

public interface LognormalProcessModel {

	int getNumberOfComponents();
	
	Double[] getInitialValue();

	RandomVariable getNumeraire(MonteCarloProcess process, double time);

	Double[] getDrift(int timeIndex);

	int getNumberOfFactors();

	Double[] getFactorLoading(int timeIndex, int componentIndex);
}
