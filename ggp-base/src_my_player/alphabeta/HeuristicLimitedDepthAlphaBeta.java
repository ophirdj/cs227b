package alphabeta;

import heuristics.StateClassifier;
import heuristics.StateClassifier.ClassificationException;

import java.util.List;
import java.util.Map.Entry;

import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import alphabeta.StateGenerator.SortFunction;
import debugging.Verbose;
import state.MyState;

public class HeuristicLimitedDepthAlphaBeta implements LimitedDepthAlphaBeta {

	private StateMachine machine;
	private Role maxPlayer;
	private Role minPlayer;
	private int searchDepth;
	private StateClassifier classifier;
	private AlphaBetaCache<HeuristicAlphaBetaEntry> cache;

	private StateGenerator stateGenerator;

	private SortFunction maxSortFunction = new SortFunction() {

		@Override
		public boolean isGreater(MyState state1, MyState state2)
				throws ClassificationException {
			return classifier.classifyState(state1) > classifier
					.classifyState(state2);
		}
	};
	private SortFunction minSortFunction = new SortFunction() {

		@Override
		public boolean isGreater(MyState state1, MyState state2)
				throws ClassificationException {
			return classifier.classifyState(state1) < classifier
					.classifyState(state2);
		}
	};

	public HeuristicLimitedDepthAlphaBeta(StateMachine machine, Role maxPlayer,
			StateClassifier classifier) {
		List<Role> roles = machine.getRoles();
		assert (roles.size() == 2);
		this.machine = machine;
		this.maxPlayer = maxPlayer;
		this.minPlayer = (roles.get(0).equals(maxPlayer) ? roles.get(1) : roles
				.get(0));
		this.searchDepth = 2;
		this.classifier = classifier;
		this.cache = new AlphaBetaCache<HeuristicAlphaBetaEntry>();
		this.stateGenerator = new StateGenerator(machine);
	}

	@Override
	public void setDepth(int depth) {
		searchDepth = depth;
	}

	@Override
	public Move bestMove(MyState state) throws MinMaxException {
		if (state == null) {
			throw new MinMaxException();
		}
		try {
			Verbose.printVerbose("START MINMAX", Verbose.MIN_MAX_VERBOSE);
			return alphabeta(state, Double.NEGATIVE_INFINITY,
					Double.POSITIVE_INFINITY, searchDepth).getMove();
		} catch (GoalDefinitionException | MoveDefinitionException
				| TransitionDefinitionException | ClassificationException e) {
			e.printStackTrace();
			throw new AlphaBetaException();
		}
	}

	private HeuristicAlphaBetaEntry alphabeta(MyState state, double alpha,
			double beta, int depth) throws GoalDefinitionException,
			MoveDefinitionException, TransitionDefinitionException,
			ClassificationException {
		if (cache.containsKey(state, depth)) {
			return cache.get(state);
		}
		HeuristicAlphaBetaEntry entry = null;
		if (machine.isTerminal(state.getState())) {
			double goalValue = (machine.getGoal(state.getState(), maxPlayer) - machine
					.getGoal(state.getState(), minPlayer)) * 10000;
			Verbose.printVerbose("Final State with goal value " + goalValue,
					Verbose.MIN_MAX_VERBOSE);
			entry = new HeuristicAlphaBetaEntry(goalValue, goalValue, null, -1);
		} else if (depth <= 0) {
			double heuristicValue = classifier.classifyState(state);
			Verbose.printVerbose("reached final depth with heuristic value "
					+ heuristicValue, Verbose.MIN_MAX_VERBOSE);
			entry = new HeuristicAlphaBetaEntry(heuristicValue, heuristicValue,
					null, 0);
		} else if (state.getControlingPlayer() == maxPlayer) {
			Verbose.printVerbose("MAX PLAYER MOVE", Verbose.MIN_MAX_VERBOSE);
			entry = maxMove(state, alpha, beta, depth);
		} else if (state.getControlingPlayer() == minPlayer) {
			Verbose.printVerbose("MIN PLAYER MOVE", Verbose.MIN_MAX_VERBOSE);
			entry = minMove(state, alpha, beta, depth);
		} else {
			throw new RuntimeException(
					"alpha-beta error: no match for controlingPlayer");
		}
		cache.put(state, entry);
		return entry;
	}

	private HeuristicAlphaBetaEntry maxMove(MyState state, double alpha,
			double beta, int depth) throws MoveDefinitionException,
			TransitionDefinitionException, GoalDefinitionException,
			ClassificationException {
		HeuristicAlphaBetaEntry maxEntry = null;
		for (Entry<Move, MyState> child : stateGenerator.getNextStates(state,
				maxPlayer, minPlayer, maxSortFunction)) {
			HeuristicAlphaBetaEntry entry = alphabeta(state, alpha, beta,
					depth - 1);
			if (maxEntry == null || entry.getAlpha() > maxEntry.getAlpha()) {
				maxEntry = new HeuristicAlphaBetaEntry(entry.getAlpha(),
						entry.getBeta(), child.getKey(), depth);
			}
			if (entry.getAlpha() > alpha) {
				alpha = entry.getAlpha();
			}
			if (alpha >= beta) {
				break;
			}
		}
		return maxEntry;
	}

	private HeuristicAlphaBetaEntry minMove(MyState state, double alpha,
			double beta, int depth) throws MoveDefinitionException,
			TransitionDefinitionException, GoalDefinitionException,
			ClassificationException {
		HeuristicAlphaBetaEntry minEntry = null;
		for (Entry<Move, MyState> child : stateGenerator.getNextStates(state,
				minPlayer, maxPlayer, minSortFunction)) {
			HeuristicAlphaBetaEntry entry = alphabeta(state, alpha, beta,
					depth - 1);
			if (minEntry == null || entry.getBeta() < minEntry.getBeta()) {
				minEntry = new HeuristicAlphaBetaEntry(entry.getAlpha(),
						entry.getBeta(), child.getKey(), depth);
			}
			if (entry.getBeta() < beta) {
				beta = entry.getBeta();
			}
			if (alpha >= beta) {
				break;
			}
		}
		return minEntry;
	}

}
