package org.gy.framework.core.statemachine.builder;

/**
 * InternalTransitionBuilder
 *
 * 
 * @date 2020-02-07 9:39 PM
 */
public interface InternalTransitionBuilder <S, E, C> {
    /**
     * Build a internal transition
     * @param stateId id of transition
     * @return To clause builder
     */
    To<S, E, C> within(S stateId);
}