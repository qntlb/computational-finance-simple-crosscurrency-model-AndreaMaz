package info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.product;

import info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.model.SimpleCrossCurrencyModel;
import net.finmath.montecarlo.AbstractMonteCarloProduct;
import net.finmath.montecarlo.MonteCarloSimulationModel;
import net.finmath.stochastic.RandomVariable;
import net.finmath.stochastic.Scalar;

public class GeneralizedCaplet extends AbstractMonteCarloProduct implements CrossCurrencyProduct {

	private int currency;

	private double fixingTime;
	private double periodStart;
	private double periodEnd;

	private double paymentTime;
	private double strike;

	private boolean isQuanto;

	public GeneralizedCaplet(int currency, boolean isQuanto, double fixingTime, double periodStart, double periodEnd,
			double paymentTime, double strike) {
		super();
		this.currency = currency;
		this.isQuanto = isQuanto;
		this.fixingTime = fixingTime;
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.paymentTime = paymentTime;
		this.strike = strike;
	}

	@Override
	public RandomVariable getValue(double evaluationTime, SimpleCrossCurrencyModel model) {

		RandomVariable forwardRate = model.getForwardRate(currency, fixingTime, periodStart, periodEnd);

		RandomVariable fxRate = isQuanto ? new Scalar(1.0) : model.getFXRate(currency, paymentTime);

		RandomVariable payment = forwardRate.sub(strike).floor(0.0);

		RandomVariable value = payment.mult(fxRate);

		RandomVariable numeraireAtPaymentTime = model.getNumeraire(paymentTime);
		RandomVariable numeraireAtEvaluationTime = model.getNumeraire(evaluationTime);

		return value.div(numeraireAtPaymentTime).mult(numeraireAtEvaluationTime);
	}

	@Override
	public RandomVariable getValue(double evaluationTime, MonteCarloSimulationModel model) {
		if (model instanceof SimpleCrossCurrencyModel) {
			return getValue(evaluationTime, (SimpleCrossCurrencyModel) model);
		} else {
			throw new IllegalArgumentException("Product requires a model implementing SimpleCrossCurrencyModel");
		}
	}
}