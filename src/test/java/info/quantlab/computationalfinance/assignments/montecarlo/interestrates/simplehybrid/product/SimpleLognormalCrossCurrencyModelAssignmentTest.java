package info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.product;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.SimpleLognormalCrossCurrencyModelAssignment;
import info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.SimpleLognormalCrossCurrencyModelSolution;
import info.quantlab.computationalfinance.assignments.montecarlo.interestrates.simplehybrid.check.SimpleLognormalCrossCurrencyModelChecker;

/**
 * This class tests the implementation of your class.
 * The actual test code is not part of this project.
 * 
 * @author Christian Fries
 */
public class SimpleLognormalCrossCurrencyModelAssignmentTest {

	private static final SimpleLognormalCrossCurrencyModelAssignment solution = new SimpleLognormalCrossCurrencyModelSolution();;

	@Test
	void testCapletDomestic() {
		test("Caplet Domestic");
	}
	
	@Test
	void testCapletForeign() {
		test("Caplet Foreign");
	}
	
	@Test
	void testCapletQuanto() {
		test("Caplet Quanto");
	}
	
	@Test
	void testCapletDomesticInAdvance() {
		test("Caplet Domestic with In-Advance Payment");
	}

	@Test
	void testCapletForeignInAdvance() {
		test("Caplet Foreign with In-Advance Payment");
	}

	@Test
	void testCapletQuantoInAdvance() {
		test("Caplet Quanto with In-Advance Payment");
	}

	private void test(String testCase) {
		System.out.println("Testing " + testCase);
		
		boolean success = SimpleLognormalCrossCurrencyModelChecker.check(solution, testCase);

		if(!success) {
			System.out.println("Sorry, the test failed.");
		}
		else {
			System.out.println("Congratulation! You solved this part of the exercise.");
		}

		System.out.println("_".repeat(79));

		if(!success) fail();
	}
}
