package info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid;

import info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.model.SimpleCrossCurrencyModel;
import info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.model.SimpleCrossCurrencyModelWithSingleMaturity;
import info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.product.CrossCurrencyProduct;
import info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.product.GeneralizedCaplet;
import net.finmath.montecarlo.BrownianMotion;

public class SimpleLognormalCrossCurrencyModelSolution implements SimpleLognormalCrossCurrencyModelAssignment {

	/**
	 * @param initialValueDomesticForwardRate The initial value for the domestic
	 *                                        forward rate
	 * @param initialValueForeignForwardRate  The initial value for the foreign
	 *                                        forward rate
	 * @param initialValueFX                  The initial value of the FX rate
	 *                                        (note: the FX rate, not the forward FX
	 *                                        rate (FFX)).
	 * @param volatilityDomestic              The volatility of the domestic forward
	 *                                        rate.
	 * @param volatilityForeign               The volatility of the domestic forward
	 *                                        rate.
	 * @param volatiltiyFXForward             The volatility of the FFX (note: of
	 *                                        the FFX, not of the FX rate).
	 * @param correlationDomFor               The correlation between the log of the
	 *                                        foreign and domestic rate
	 * @param correlationFXDomenstic          The correlation between the log of the
	 *                                        FFX and domestic rate
	 * @param correlationFXForeign            The correlation between the log of the
	 *                                        FFX and foreign rate
	 * @param periodStart                     The fixing time T<sub>1</sub> for the
	 *                                        forward rate.
	 * @param maturity                        The time T<sub>2</sub> (periodEnd and
	 *                                        maturity of the zero bonds)
	 * @param domesticZeroBond                The domestic zero bond with maturity
	 *                                        in T<sub>2</sub>
	 * @param foreignZeroBond                 The foreign zero bond with maturity in
	 *                                        T<sub>2</sub>
	 * @param BrownianMotion                  brownianMotion The Brownian motion to
	 *                                        be used in the numerical scheme.
	 * @return The model.
	 */
	@Override
	public SimpleCrossCurrencyModel getSimpleCrossCurrencyModel(double initialValueDomesticForwardRate,
			double initialValueForeignForwardRate, double initialValueFX, double volatilityDomestic,
			double volatilityForeign, double volatiltiyFXForward, double correlationDomFor,
			double correlationFXDomenstic, double correlationFXForeign, double periodStart, double maturity,
			double domesticZeroBond, double foreignZeroBond, BrownianMotion brownianMotion) {

		/*
		 * Return your implementation of an SimpleCrossCurrencyModel. Note: You may just
		 * complete the stub implementation which we provide. If you implement a
		 * different class, change the line below to your implementation.
		 */
		return new SimpleCrossCurrencyModelWithSingleMaturity(initialValueDomesticForwardRate,
				initialValueForeignForwardRate, initialValueFX, volatilityDomestic, volatilityForeign,
				volatiltiyFXForward, correlationDomFor, correlationFXDomenstic, correlationFXForeign, periodStart,
				maturity, domesticZeroBond, foreignZeroBond, brownianMotion);
	}

	/**
	 * Create the Monte-Carlo valuation of a generalized caplet paying max(L - K, 0)
	 * * (isQuanto ? 1 : FX) in T where L may be a domestic or foreign interest rate
	 * and T may be the period start or period end date.
	 *
	 * @param currency    The currency of the forward rate to be paid (0=domestic,
	 *                    1=foreign)
	 * @param isQuanto    If true, it will be a quanto (only applied for currency ==
	 *                    1).
	 * @param fixingTime  The fixing time of the forward rate. Should be T1
	 * @param periodStart The period start time of the forward rate. Should be T1
	 * @param periodEnd   The period end time of the forward rate. Should be T2
	 * @param paymentTime The payment time of the cash flow. May be T1 or T2.
	 * @param strike      The strike rate.
	 * @return The product.
	 */
	@Override
	public CrossCurrencyProduct getGeneralizedCaplet(int currency, boolean isQuanto, double fixingTime,
			double periodStart, double periodEnd, double paymentTime, double strike) {

		/*
		 * Return your implementation of an CrossCurrencyProduct. Note: You may just
		 * complete the stub implementation which we provide. If you implement a
		 * different class, change the line below to your implementation.
		 */
		return new GeneralizedCaplet(currency, isQuanto, fixingTime, periodStart, periodEnd, paymentTime, strike);

	}
}