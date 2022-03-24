package org.gy.framework.core.statemachine;

/**
 * Visitable
 *
 * 
 * @date 2020-02-08 8:41 PM
 */
public interface Visitable {
    String accept(final Visitor visitor);
}
