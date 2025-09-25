package io.github.guanyang.core.statemachine.builder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.github.guanyang.core.statemachine.State;
import io.github.guanyang.core.statemachine.StateMachine;
import io.github.guanyang.core.statemachine.StateMachineFactory;
import io.github.guanyang.core.statemachine.impl.StateMachineImpl;
import io.github.guanyang.core.statemachine.impl.TransitionType;

/**
 * StateMachineBuilderImpl
 *
 * 
 * @date 2020-02-07 9:40 PM
 */
public class StateMachineBuilderImpl<S, E, C> implements StateMachineBuilder<S, E, C> {

    /**
     * StateMap is the same with stateMachine, as the core of state machine is holding reference to states.
     */
    private final Map<S, State< S, E, C>> stateMap = new ConcurrentHashMap<>();
    private final StateMachineImpl<S, E, C> stateMachine = new StateMachineImpl<>(stateMap);

    @Override
    public ExternalTransitionBuilder<S, E, C> externalTransition() {
        return new TransitionBuilderImpl<>(stateMap, TransitionType.EXTERNAL);
    }

    @Override
    public ExternalTransitionsBuilder<S, E, C> externalTransitions() {
        return new TransitionsBuilderImpl<>(stateMap, TransitionType.EXTERNAL);
    }

    @Override
    public InternalTransitionBuilder<S, E, C> internalTransition() {
        return new TransitionBuilderImpl<>(stateMap, TransitionType.INTERNAL);
    }

    @Override
    public StateMachine<S, E, C> build(String machineId) {
        stateMachine.setMachineId(machineId);
        stateMachine.setReady(true);
        StateMachineFactory.register(stateMachine);
        return stateMachine;
    }

}
