# computational finance - small three times lognormal cross currency model

In this exercise your task is to implement a Monte-Carlo simulation of a hybrid cross currency model and implement
the valuation of a few non-standard products to be valued with this model.

The model is a simplified version of a classical log-normal cross-currency discrete forward rate model (LIBOR market model).

The simplification we take is that our tenor structure consists of three time points only, 0 = T<sub>0</sub>, T<sub>1</sub>, T<sub>2</sub>. This allows us to specify the model
with a simplified drift term (for a richer tenor structure the derivation of the no-arbitrage drift would be required).

The model allows you to explore some aspects:

- The impact of volatility and correlation of the Brownian driver.
- The effect of paying in unnatural units, like a quanto or an in-advance payment.

### Model Specification

We assume a log-normal dynamic for the domestic and foreign forward rate for the period [T<sub>1</sub>,T<sub>2</sub>] and a log-normal dynamic
for the forward FX rate FFX(T<sub>2</sub>) - under the measure associated with the zero coupon bond maturing in T<sub>2</sub>.

dL<sub>dom</sub> = &mu;<sub>dom</sub> L<sub>dom</sub>(t) dt + L<sub>dom</sub>(t) (&lambda;<sub>dom,1</sub> dW<sub>1</sub> + .... + &lambda;<sub>dom,m</sub> dW<sub>m</sub>)

dL<sub>for</sub> = &mu;<sub>dom</sub> L<sub>for</sub>(t) dt + L<sub>for</sub>(t) (&lambda;<sub>for,1</sub> dW<sub>1</sub> + .... + &lambda;<sub>for,m</sub> dW<sub>m</sub>)

dFFX = &mu;<sub>ffx</sub> FFX(t) dt + FFX(t) (&lambda;<sub>ffx,1</sub> dW<sub>1</sub> + .... + &lambda;<sub>ffx,m</sub> dW<sub>m</sub>)

### Drift

This implies fairly simple drift terms:

- The drift of the domestic forward rate is 0.
- The drift of the foreign forward rate is the quanto adjustment.
- The drift of the forward FX rate is 0.

### Numeraire

The numeraire can be specified in all three time points:

- t = 0: N(t) = P(T<sub>2</sub>;t) (a deterministic model parameter (zeroBondDomestic)
- t = T<sub>2</sub>: N(t) = 1
- t = T<sub>1</sub>: N(t) = 1 / (1+L(T<sub>1</sub>,T<sub>2</sub>;T<sub>1</sub>) (T<sub>2</sub>-T<sub>1</sub>))

Note: we cannot give the numeraire in other time points, because the model does not simulate the other forward rates.

### Model Parameters

The model is given by the following parameters:

- the time discretization points T<sub>1</sub>, T<sub>2</sub>
- the initial values of the three quantities,
- the three volatility parameters,
- the six correlation parameters,
- the domestic and foreign zero coupon bond with maturity T<sub>2</sub>.

### Correlation

We assume that the Brownian increments dW<sub>j</sub> are independent, i.e. dW<sub>i</sub>dW<sub>j</sub> = 0 for i &neq; j. This imples that the correlation if given
as a function of the model parameters (the matrix) &lambda;.

## Task 1: Implementing the model

Your task is to implement a class that implements the interface `SimpleCrossCurrencyModel` and that provides
such a Monte-Carlo simulation model.

```
public interface SimpleCrossCurrencyModel extends MonteCarloSimulationModel {

	RandomVariable getForwardRate(int currency, double time, double periodStart, double periodEnd);

	RandomVariable getFXRate(int currency, double time);
		
	RandomVariable getNumeraire(double time);
}
```
Remark: The interface is not part of this project. It is part of the project `numerical-methods-lecture` which we use to perform automated
test of your project solution.

Note that the interface provides the FX rate, but the model simulates the FFX rate. Note also that the interface looks as if you may
request the numeraire and the forward rate for all times and periods, but the model is restricted to T<sub>0</sub>, T<sub>1</sub>, T<sub>2</sub>.
So your code should check the time and thrown an IllegalArgumentException if unsupported times are requested.

The model can then be used to value a variety of hybrid interest rate derivatives.

## Task 2: Implement a valuation of (some) financial products

Your task is to implement one or many financial products, implementing the following interface:

```
public interface CrossCurrencyProduct extends MonteCarloProduct {

	RandomVariable getValue(double evaluationTime, SimpleCrossCurrencyModel model);
	
	default double getValue(SimpleCrossCurrencyModel model) {
		return getValue(0.0, model).getAverage();
	}
	
}

```
Remark: The interface is not part of this project. It is part of the project `numerical-methods-lecture` which we use to perform automated
test of your project solution.

Your task is to implement some of the following derivatives and check you model against known analytic values

- The domestic bond with maturity T<sub>2</sub>.
- The domestic bond with maturity T<sub>1</sub>.
- The foreign bond with maturity T<sub>2</sub>.
- The foreign bond with maturity T<sub>1</sub>.
- The domestic caplet.
- The foreign caplet.
- The quanto caplet.
- The quanto caplet with payment in advance (a quanto LIBOR in arrears), where the foreign interest rate for the period [T<sub>1</sub>,T<sub>2</sub>] is paid in T<sub>1</sub> without conversion by the FX rate.
- The quanto caplet with payment in advance (a quanto LIBOR in arrears), where the foreign interest rate for the period [T<sub>1</sub>,T<sub>2</sub>] is paid in T<sub>1</sub> without conversion by the FX rate.

Note: This looks much more work than it is. The caplets may be implemented in a single class.

You may also implement:

- The FX option with maturity T<sub>2</sub>.
- The FX option with maturity T<sub>1</sub>.

The model may be also used to value some hybrid interest rate derivatives for which no analytic formula has been derived in the lecture (or maybe even none is known).

## Aided implementation

For the implementation of the interface `SimpleCrossCurrencyModel` you may:

- create you own implementation from scratch (only using basic types like `RandomVariable` from finmath lib) or
- use all methods and tools from finmath lib and use the stub code we provide (search for TODO in the code)

To ease the implementation we provide a simplified `ProcessModel` for the `EulerSchemeFromProcessModel` that specifically describes a log-normal process

dX<sub>i</sub> = &mu;<sub>i</sub> X<sub>i</sub>(t) dt + X<sub>i</sub>(t) (&lambda;<sub>i,1</sub> dW<sub>1</sub> + .... + &lambda;<sub>i,m</sub> dW<sub>m</sub>) ; X<sub>i</sub>(0) = X<sub>i,0</sub>

So you can solve the exercise by implementing the interface `LognormalProcessModel` that provides the following model parameters:

- The three initial values X<sub>i,0</sub>, i = 1,..,3
- The three drift parameters &mu;<sub>i</sub>, i = 1,..,3
- The nine factor loadings &lambda;<sub>i,j</sub>, i = 1,..,3, j = 1,...3.

Then use the following code to create a class implementing `SimpleCrossCurrencyModel`:

```
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

		LognormalProcessModel lognormalProcessModel = new // YOUR CLASS IMPLEMENTING THE INTERFACE LognormalProcessModel
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
		// MORE CODE OF YOU....
	}
```
where lognormalProcessModel is an object of your class implementing `LognormalProcessModel`.


## Task 0:

Theoretically derive the parameters X<sub>i,0</sub>, &mu;<sub>i</sub>, &lambda;<sub>i,j</sub> as function of the following model parameters:

```
	double initialValueDomesticForwardRate, double initialValueForeignForwardRate, double initialValueFX,
	double volatilityDomestic, double volatilityForeign, double volatiltiyFXForward,
	double correlationDomFor, double correlationFXDomenstic, double correlationFXForeign,
	double periodStart, double periodEnd, double domesticZeroBond, double foreignZeroBond
```

Note: Some relations are trivial (X<sub>0,0</sub> = `initialValueForwardDomestic`), others are a bit more involved (&lambda; is a function of the correlations and volatilities).

Note: The model can provide the FX and the Numeraire only at the time steps T<sub>0</sub> = 0, T<sub>1</sub>, T<sub>2</sub>. The Monte-Carlo simulation of the other quantities (forward rates and FX forward) may be performed in finer steps.

## Task 1:

Implement a class implementing the interface `SimpleCrossCurrencyModel` and
allow us to construct an object of your class by implementing the factory method `getSimpleCrossCurrencyModel`
in the class `SimpleLognormalCrossCurrencyModelSolution`.

## Task 2:

Implement a class implementing the interface `CrossCurrencyProduct` and
allow us to construct an object of your class by implementing the factory method `getGeneralizedCaplet`
in the class `SimpleLognormalCrossCurrencyModelSolution`.

## Task 3:

Test your implementation. Test your implementation by comparing the Monte-Carlo valuation to an analytic formula.
You can also run the unit test `SimpleLognormalCrossCurrencyModelAssignmentTest` (see `src/test/java`).

## Optional

The model allows to value some more exotic options, for example the exchange option

max(L<sub>dom</sub>(T<sub>1</sub>) - L<sub>for</sub>(T<sub>1</sub>), 0)

(where one index is natural and the other is quanto). You may explore this product (and its dependency on volatility and correlation).
You may plot the dependency of the value on some parameters, if you like. We might attribute 1 bonus point.

## Submission of the Solution

Commit code changes to your repository. If you commit code changed to your project's repository, automated unit test will run.

### Working in Eclipse

Import this git repository into Eclipse and start working.

- Click on the link to your repository (the link starts with qntlb/computational-finance… )
- Click on “Clone or download” and copy the URL to your clipboard.
- Go to Eclipse and select File -> Import -> Git -> Projects from Git.
- Select “Clone URI” and paste the GitHub URL from step 2.
- Select “master”, then Next -> Next
- In the Wizard for Project Import select “Import existing Eclipse projects”, then Next -> Finish

