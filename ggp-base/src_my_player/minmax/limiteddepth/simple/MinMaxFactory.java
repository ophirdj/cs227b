package minmax.limiteddepth.simple;

import minmax.IMinMax;
import minmax.IMinMaxFactory;

import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;

import classifier.IClassifier;


public final class MinMaxFactory extends IMinMaxFactory {

	@Override
	public IMinMax createLimitedDepthMinMax(StateMachine machine, Role maxPlayer,
			IClassifier classifier, int depth, boolean cached) {
		return new MinMax(machine, maxPlayer, classifier, depth, cached);
	}

	
	@Override
	public String toString() {
		return MinMax.class.getSimpleName();
	}
}
