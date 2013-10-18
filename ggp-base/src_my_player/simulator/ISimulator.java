package simulator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;




import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.observer.Event;
import org.ggp.base.util.observer.Subject;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import states.LabeledState;
import states.MyState;


/**
 * Interface for running game simulations.
 * 
 * 
 * @author Ophir De Jager
 * 
 */
public interface ISimulator extends Subject {

	/**
	 * Run simulation.
	 * 
	 * @param state
	 *            Starting state.
	 * @throws TransitionDefinitionException
	 * @throws MoveDefinitionException
	 * @throws GoalDefinitionException
	 */
	void Simulate(MyState state) throws MoveDefinitionException,
			TransitionDefinitionException, GoalDefinitionException;

	/**
	 * 
	 * @return A set of all state contents that were encountered during
	 *         simulations.
	 */
	Set<GdlSentence> getAllContents();

	/**
	 * 
	 * @return A collection of labeled states that were found during
	 *         simulations.
	 */
	Collection<LabeledState> getLabeledStates();

	/**
	 * Event generated by simulator
	 * 
	 * @author Ophir De Jager
	 *
	 */
	class SimulatorEvent extends Event {

		public final Set<GdlSentence> contents;
		public final Map<Double, Integer> labelsHistogram;
		public final int discoveredStates;
		public final int labeledStates;
		
		public SimulatorEvent(Set<GdlSentence> contents, Map<Double, Integer> labelsHistogram, int discoveredStates, int labeledStates) {
			this.contents = contents;
			this.labelsHistogram = labelsHistogram;
			this.discoveredStates = discoveredStates;
			this.labeledStates = labeledStates;
		}
		
	}

}
