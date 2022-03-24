package org.gy.framework.core.statemachine;

/**
 * Generic strategy interface used by a state machine to respond
 * events by executing an {@code Action} with a {@link StateContext}.
 *
 * 
 * @date 2020-02-07 2:51 PM
 */
public interface Action<S, E, C> {

//    /**
//     * Execute action with a {@link StateContext}.
//     *
//     * @param context the state context
//     */
//    void execute(StateContext<S, E> context);

    public void execute(S from, S to, E event, C context);

}
