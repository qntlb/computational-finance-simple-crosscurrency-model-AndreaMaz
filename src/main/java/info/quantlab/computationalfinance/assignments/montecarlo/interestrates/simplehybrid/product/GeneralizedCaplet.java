package info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.product;

import info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.model.SimpleCrossCurrencyModel;
import net.finmath.montecarlo.AbstractMonteCarloProduct;
import net.finmath.montecarlo.MonteCarloSimulationModel;
import net.finmath.stochastic.RandomVariable;
import net.finmath.stochastic.Scalar;

/**
 * This class provides the valuation of a generalized caplet. That is, the
 * caplet can be payed in natural units (i.e. in T_2) or in arrears (i.e., in
 * T_1), and it can be a domestic, foreign or quanto caplet.
 *
 * @author Andrea Mazzon
 *
 */
public class GeneralizedCaplet extends AbstractMonteCarloProduct implements CrossCurrencyProduct {

	private int currency;

	private double fixingTime;
	private double periodStart;
	private double periodEnd;

	private double paymentTime;
	private double strike;

	private boolean isQuanto;

	/**
	 * It constructs an object for the valuation of a caplet.
	 *
	 * @param currency,    0 if domestic, 1 if foreign
	 * @param isQuanto,    true if it is a quanto, false if foreign caplet
	 * @param fixingTime,  T_1, in our case
	 * @param periodStart, T_1
	 * @param periodEnd,   T_2
	 * @param paymentTime, T_1 (if payed in advance, i.e., if in arrears) or T_2
	 * @param strike,      K
	 */
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

		// If currency 0, it is L^d(T_1,T_2;T_1), if currency=1, L^f(T_1, T_2; T_1)
		RandomVariable forwardRate = model.getForwardRate(currency, fixingTime, periodStart, periodEnd);

		/*
		 * It is 1 if we are just interested in the domestic caplet. Note that in
		 * general the FX rate is taken at T_1 if in the payment is done in advance
		 */
		RandomVariable fxRate = isQuanto ? new Scalar(1.0) : model.getFXRate(currency, paymentTime);

		// the payoff of the caplet
		RandomVariable payment = forwardRate.sub(strike).floor(0.0);

		RandomVariable value = payment.mult(fxRate);

		// payment at T_2 or in advance
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