package info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.product;

import info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.model.SimpleCrossCurrencyModel;
import net.finmath.montecarlo.AbstractMonteCarloProduct;
import net.finmath.montecarlo.MonteCarloSimulationModel;
import net.finmath.stochastic.RandomVariable;

public class GeneralizedCaplet extends AbstractMonteCarloProduct implements CrossCurrencyProduct {


	@Override
	public RandomVariable getValue(double evaluationTime, SimpleCrossCurrencyModel model) {
		
		// TODO: Implement your product valuation
		return null;
	}

	@Override
	public RandomVariable getValue(double evaluationTime, MonteCarloSimulationModel model) {
		if(model instanceof SimpleCrossCurrencyModel) {
			return getValue(evaluationTime, (SimpleCrossCurrencyModel)model);
		}
		else {
			throw new IllegalArgumentException("Product requires a model implementing SimpleCrossCurrencyModel");
		}
	}	
}
