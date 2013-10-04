package player;

import minmax.MinMaxFactory;

import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.exception.GameAnalysisException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.match.Match;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import simulator.MapValueSimulatorFactory;

public abstract class ParaStateMachinePlayer {
	private StateMachineGamer caller;
	
	public ParaStateMachinePlayer(StateMachineGamer caller) {
		this.caller = caller;
	}
	
	public Role getRole() {
		return caller.getRole();
	}

	public Match getMatch() {
		return caller.getMatch();
	}


	public StateMachine getStateMachine() {
		return caller.getStateMachine();
	}
	
	public void notifyObservers(GamerSelectedMoveEvent gamerSelectedMoveEvent) {
		caller.notifyObservers(gamerSelectedMoveEvent);
	}


	public MachineState getCurrentState() {
		return caller.getCurrentState();
	}

	public abstract void initialize(int exampleAmount, MapValueSimulatorFactory mapValueSimulatorFactory, MinMaxFactory minmaxFactory, BuilderFactory builderFactory, int minmaxDepth);
	
	public abstract void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException;

	public abstract Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException,
			GoalDefinitionException;

	public abstract void stateMachineStop();

	public abstract String getName();

	public abstract void analyze(Game g, long timeout) throws GameAnalysisException;

	public abstract void stateMachineAbort();
}
