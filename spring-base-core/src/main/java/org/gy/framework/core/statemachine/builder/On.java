package org.gy.framework.core.statemachine.builder;

import org.gy.framework.core.statemachine.Condition;

/**
 * On
 *
 * 
 * @date 2020-02-07 6:14 PM
 */
public interface On<S, E, C> extends When<S, E, C>{
    /**
     * Add condition for the transition
     * @param condition transition condition
     * @return When clause builder
     */
    When<S, E, C> when(Condition<C> condition);
}
