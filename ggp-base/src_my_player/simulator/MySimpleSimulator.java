package simulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.observer.Event;
import org.ggp.base.util.observer.Observer;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;






import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import state.LabeledState;
import state.MyState;
import interfaces.ISimulator;
import interfaces.IStateLabeler;

public class MySimpleSimulator implements ISimulator{

	private StateMachine machine;
	private IStateLabeler labeler;
	private Role maxplayer;
	private Set<GdlSentence> contents;
	private Map<MachineState, LabeledState> labeled;

	public MySimpleSimulator(StateMachine machine, IStateLabeler labeler, Role maxplayer) {
		this.machine = machine;
		this.labeler = labeler;
		this.maxplayer = maxplayer;
		this.contents = new HashSet<GdlSentence>();
		this.labeled = new HashMap<MachineState, LabeledState>();
	}
	
	@Override
	public void addObserver(Observer observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyObservers(Event event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Simulate(MyState rootState) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		List<MyState> simulation = new ArrayList<MyState>();
		MyState state = rootState;
		while(!labeled.containsKey(state) || machine.isTerminal(state.getState())) {
			simulation.add(state);
			addContents(state);
			MachineState nextState = machine.getRandomNextState(state.getState());
			state = MyState.createChild(state, nextState);
		}
		simulation.add(state);
		addContents(state);
		labelSimulation(simulation);
	}

	private void labelSimulation(List<MyState> simulation) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		MyState last = simulation.get(simulation.size() - 1);
		labeled.put(last.getState(), labeler.label(last));
		ListIterator<MyState> iterator = simulation.listIterator(simulation.size() - 1);
		while(iterator.hasPrevious()) {
			MyState state = iterator.previous();
			LabeledState label = checkChildren(state);
			if(label != null) {
				labeled.put(state.getState(), label);
			} else {
				break;
			}
		}
	}

	private LabeledState checkChildren(MyState state) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		LabeledState bestLabel = null;
		for(MachineState child: machine.getNextStates(state.getState())) {
			LabeledState label = null;
			if(labeled.containsKey(child)) {
				label = labeled.get(child);
			} else if(machine.isTerminal(child)) {
				MyState childState = MyState.createChild(state, child);
				label = labeler.label(childState);
				labeled.put(child, label);
			} else {
				return null;
			}
			bestLabel = betterLabel(bestLabel, label, state.getRole());
		}
		return labeler.createLabel(state, bestLabel.getValue());
	}
	
	private LabeledState betterLabel(LabeledState label1, LabeledState label2, Role role) {
		if(label1 == null) {
			return label2;
		} else if (label2 == null) {
			return label1;
		} else if(role == maxplayer){
			return label1.getValue() > label2.getValue() ? label1 : label2;
		} else {
			return label1.getValue() < label2.getValue() ? label1 : label2;
		}
	}

	private void addContents(MyState state) {
		contents.addAll(state.getContents());
	}

	@Override
	public Set<GdlSentence> getAllContents() {
		return contents;
	}

	@Override
	public Collection<LabeledState> getLabeledStates() {
		return labeled.values();
	}


}
