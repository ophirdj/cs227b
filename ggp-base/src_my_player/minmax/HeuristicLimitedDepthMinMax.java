package minmax;

import heuristics.StateClassifier;
import heuristics.StateClassifier.ClassificationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ggp.base.util.observer.Event;
import org.ggp.base.util.observer.Observer;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import debugging.Verbose;
import state.MyState;

public class HeuristicLimitedDepthMinMax implements LimitedDepthMinMax{
	
	private StateMachine machine;
	private Role maxPlayer;
	private Role minPlayer;
	private StateClassifier classifier;
	private int searchDepth;
	private Map<MyState, MinMaxEntry> cache;
	private MinMaxEventReporter reporter;
	
	public HeuristicLimitedDepthMinMax(StateMachine machine, Role maxPlayer, StateClassifier classifier) {
		List<Role> roles = machine.getRoles();
		assert (roles.size() == 2);
		this.machine = machine;
		this.maxPlayer = maxPlayer;
		this.minPlayer = (roles.get(0).equals(maxPlayer) ? roles.get(1) : roles
				.get(0));
		this.classifier = classifier;
		this.searchDepth = 2;
		this.cache = new HashMap<MyState, MinMaxEntry>();
		this.reporter = new MinMaxEventReporter();
	}

	@Override
	public Move bestMove(MyState state) throws MinMaxException, GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException, ClassificationException {
		if (state == null) {
			throw new MinMaxException();
		}
		Verbose.printVerbose("START MINMAX", Verbose.MIN_MAX_VERBOSE);
		long startTime = System.currentTimeMillis();
		Move move;
		try {
			move = minmaxValueOf(state, searchDepth).getMove();
			long endTime = System.currentTimeMillis();
			reporter.reportAndReset(move, cache.size(), searchDepth, endTime - startTime);
			return move;
		//This should only happen if time was over timeout
		} catch (InterruptedException e) {
			return null;
		}
	}
	
	private MinMaxEntry minmaxValueOf(MyState state, int depth)
			throws GoalDefinitionException, MoveDefinitionException,
			TransitionDefinitionException, ClassificationException, MinMaxException, InterruptedException {
		if (Thread.currentThread().isInterrupted()){
			throw new InterruptedException();
		}
		reporter.exploreNode();
		if (cache.containsKey(state) && cache.get(state) != null) {
			reporter.cacheHit();
			return cache.get(state);
		} else if (cache.containsKey(state)) {
			return null;
		}
		MinMaxEntry minmaxEntry = null;
		cache.put(state, null);
		if (machine.isTerminal(state.getState())) {
			reporter.visitTerminal();
			double goalValue = (machine.getGoal(state.getState(), maxPlayer)
					- machine.getGoal(state.getState(), minPlayer)) * 10000;
			Verbose.printVerbose("Final State with goal value " + goalValue, Verbose.MIN_MAX_VERBOSE);
			return new MinMaxEntry(goalValue, null, 10);
		} else if(depth < 0) {
			Verbose.printVerbose("FINAL DEPTH REACHED", Verbose.MIN_MAX_VERBOSE);
			return new MinMaxEntry(classifier.classifyState(state), null, 0);
		} else if (maxPlayer.equals(state.getControlingPlayer())) {
			Verbose.printVerbose("MAX PLAYER MOVE", Verbose.MIN_MAX_VERBOSE);
			minmaxEntry = maxMove(state, depth);
		} else if (minPlayer.equals(state.getControlingPlayer())) {
			Verbose.printVerbose("MIN PLAYER MOVE", Verbose.MIN_MAX_VERBOSE);
			minmaxEntry = minMove(state, depth);
		} else {
			throw new MinMaxException(
					"minmax error: no match for controlingPlayer");
		}
		cache.put(state, minmaxEntry);
		return minmaxEntry;
	}

	private MinMaxEntry maxMove(MyState state, int depth) throws MoveDefinitionException,
			TransitionDefinitionException, GoalDefinitionException, ClassificationException, MinMaxException, InterruptedException {
		MinMaxEntry maxEntry = null;
		Set<Entry<Move, List<MachineState>>> children = machine.getNextStates(
				state.getState(), maxPlayer).entrySet();
		reporter.expandNode(children.size());
		for (Entry<Move, List<MachineState>> maxMove : children) {
			assert (maxMove.getValue().size() == 1);
			MachineState nextMachineState = maxMove.getValue().get(0);
			MyState nextState = new MyState(nextMachineState,
					state.getTurnNumber() + 1, minPlayer);
			MinMaxEntry nextEntry = minmaxValueOf(nextState, depth - 1);
			if ((maxEntry == null)
					|| ((nextEntry != null) && (nextEntry.getValue() > maxEntry
							.getValue()))) {
				maxEntry = new MinMaxEntry(nextEntry.getValue(),
						maxMove.getKey(), nextEntry.getImportance() + 1);
			}
		}
		return maxEntry;
	}

	private MinMaxEntry minMove(MyState state, int depth) throws MoveDefinitionException,
			TransitionDefinitionException, GoalDefinitionException, ClassificationException, MinMaxException, InterruptedException {
		MinMaxEntry minEntry = null;
		Set<Entry<Move, List<MachineState>>> children = machine.getNextStates(
				state.getState(), minPlayer).entrySet();
		reporter.expandNode(children.size());
		for (Entry<Move, List<MachineState>> minMove : children) {
			assert (minMove.getValue().size() == 1);
			MachineState nextMachineState = minMove.getValue().get(0);
			MyState nextState = new MyState(nextMachineState,
					state.getTurnNumber() + 1, maxPlayer);
			MinMaxEntry nextEntry = minmaxValueOf(nextState, depth - 1);
			if ((minEntry == null)
					|| ((nextEntry != null) && (nextEntry.getValue() < minEntry
							.getValue()))) {
				minEntry = new MinMaxEntry(nextEntry.getValue(),
						minMove.getKey(), nextEntry.getImportance() + 1);
			}
		}
		return minEntry;
	}

	@Override
	public void setDepth(int depth) {
		this.searchDepth = depth;
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public void addObserver(Observer observer) {
		reporter.addObserver(observer);
	}

	@Override
	public void notifyObservers(Event event) {
		reporter.notifyObservers(event);
	}
}
