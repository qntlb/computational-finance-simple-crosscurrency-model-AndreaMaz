package info.quantlab.computationalfinance.assignments.montecarlo.processmodel;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.RandomVariableFromArrayFactory;
import net.finmath.montecarlo.model.ProcessModel;
import net.finmath.montecarlo.process.MonteCarloProcess;
import net.finmath.stochastic.RandomVariable;

public class ProcessModelFromLognormalProcessModel implements ProcessModel {
	
	private LocalDateTime referenceDate;
	private RandomVariableFactory randomVariableFactory;
	private LognormalProcessModel lognormalModel;

	public ProcessModelFromLognormalProcessModel(LocalDateTime referenceDate,
			RandomVariableFactory randomVariableFactory, LognormalProcessModel lognormalModel) {
		super();
		this.referenceDate = referenceDate;
		this.randomVariableFactory = randomVariableFactory;
		this.lognormalModel = lognormalModel;
	}

	public ProcessModelFromLognormalProcessModel(LognormalProcessModel model) {
		this(null, new RandomVariableFromArrayFactory(), model);
	}

	@Override
	public LocalDateTime getReferenceDate() {
		return referenceDate;
	}

	@Override
	public int getNumberOfComponents() {
		return lognormalModel.getNumberOfComponents();
	}

	/*
	 * The following two methods give the transformation to log-normal coordinates.
	 */
	@Override
	public RandomVariable applyStateSpaceTransform(MonteCarloProcess process, int timeIndex, int componentIndex, RandomVariable randomVariable) {
		return randomVariable.exp();
	}

	@Override
	public RandomVariable applyStateSpaceTransformInverse(MonteCarloProcess process, int timeIndex, int componentIndex, RandomVariable randomVariable) {
		return randomVariable.log();
	}

	/*
	 * The following methods wrap the constant coefficients into (deterministic) random variables,
	 * since the general ProcessModel interface expects all quantities to be of type RandomVariable.
	 */
	@Override
	public RandomVariable[] getInitialState(MonteCarloProcess process) {
		Double[] initialValue = lognormalModel.getInitialValue();

		Function<Double, RandomVariable> randomVariableMapper = x -> randomVariableFactory.createRandomVariable(x);
		RandomVariable[] initialState = Stream.of(initialValue).map(Math::log).map(randomVariableMapper).toArray(i -> new RandomVariable[i]);

		return initialState;
	}

	@Override
	public RandomVariable getNumeraire(MonteCarloProcess process, double time) throws CalculationException {
		return lognormalModel.getNumeraire(process, time);
	}

	@Override
	public RandomVariable[] getDrift(MonteCarloProcess process, int timeIndex, RandomVariable[] realizationAtTimeIndex, RandomVariable[] realizationPredictor) {
		Double[] drift = lognormalModel.getDrift(timeIndex);

		Function<Double, RandomVariable> randomVariableMapper = x -> randomVariableFactory.createRandomVariable(x);
		return Stream.of(drift).map(randomVariableMapper).toArray(i -> new RandomVariable[i]);
	}

	@Override
	public int getNumberOfFactors() {
		return lognormalModel.getNumberOfFactors();
	}

	@Override
	public RandomVariable[] getFactorLoading(MonteCarloProcess process, int timeIndex, int componentIndex, RandomVariable[] realizationAtTimeIndex) {
		Double[] factorLoadingsForComponent = lognormalModel.getFactorLoading(timeIndex, componentIndex);

		Function<Double, RandomVariable> randomVariableMapper = x -> randomVariableFactory.createRandomVariable(x);
		return Stream.of(factorLoadingsForComponent).map(randomVariableMapper).toArray(i -> new RandomVariable[i]);
	}

	@Override
	public RandomVariable getRandomVariableForConstant(double value) {
		return randomVariableFactory.createRandomVariable(value);
	}

	@Override
	public ProcessModel getCloneWithModifiedData(Map<String, Object> dataModified) throws CalculationException {
		throw new UnsupportedOperationException();
	}

}
