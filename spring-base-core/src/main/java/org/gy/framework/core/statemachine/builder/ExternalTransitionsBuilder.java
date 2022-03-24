package org.gy.framework.core.statemachine.builder;

/**
 * ExternalTransitionsBuilder
 *
 * This builder is for multiple transitions, currently only support multiple sources <----> one target
 *
 * 
 * @date 2020-02-08 7:41 PM
 */
public interface ExternalTransitionsBuilder<S, E, C> {
    From<S, E, C> fromAmong(S... stateIds);
}
