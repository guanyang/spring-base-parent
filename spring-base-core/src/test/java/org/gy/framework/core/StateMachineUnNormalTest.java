package org.gy.framework.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.gy.framework.core.StateMachineTest.Context;
import org.gy.framework.core.StateMachineTest.Events;
import org.gy.framework.core.StateMachineTest.States;
import org.gy.framework.core.statemachine.Action;
import org.gy.framework.core.statemachine.Condition;
import org.gy.framework.core.statemachine.StateMachine;
import org.gy.framework.core.statemachine.builder.StateMachineBuilder;
import org.gy.framework.core.statemachine.builder.StateMachineBuilderFactory;
import org.gy.framework.core.statemachine.impl.StateMachineException;
import org.junit.jupiter.api.Test;

/**
 * StateMachineUnNormalTest
 *
 * 
 * @date 2020-02-08 5:52 PM
 */
public class StateMachineUnNormalTest {

    @Test
    public void testConditionNotMeet() {
        StateMachineBuilder<States, Events, Context> builder = StateMachineBuilderFactory.create();
        builder.externalTransition()
            .from(StateMachineTest.States.STATE1)
            .to(StateMachineTest.States.STATE2)
            .on(StateMachineTest.Events.EVENT1)
            .when(checkConditionFalse())
            .perform(doAction());

        StateMachine<States, Events, Context> stateMachine = builder.build("NotMeetConditionMachine");
        StateMachineTest.States target = stateMachine.fireEvent(StateMachineTest.States.STATE1,
            StateMachineTest.Events.EVENT1, new StateMachineTest.Context());
        assertEquals(StateMachineTest.States.STATE1, target);
    }


    @Test
    public void testDuplicatedTransition() {
        assertThrows(StateMachineException.class, () -> {
            StateMachineBuilder<StateMachineTest.States, StateMachineTest.Events, StateMachineTest.Context> builder = StateMachineBuilderFactory.create();
            builder.externalTransition()
                .from(StateMachineTest.States.STATE1)
                .to(StateMachineTest.States.STATE2)
                .on(StateMachineTest.Events.EVENT1)
                .when(checkCondition())
                .perform(doAction());

            builder.externalTransition()
                .from(StateMachineTest.States.STATE1)
                .to(StateMachineTest.States.STATE2)
                .on(StateMachineTest.Events.EVENT1)
                .when(checkCondition())
                .perform(doAction());
        });

    }

    @Test
    public void testDuplicateMachine() {
        assertThrows(StateMachineException.class, () -> {
            StateMachineBuilder<StateMachineTest.States, StateMachineTest.Events, StateMachineTest.Context> builder = StateMachineBuilderFactory.create();
            builder.externalTransition()
                .from(StateMachineTest.States.STATE1)
                .to(StateMachineTest.States.STATE2)
                .on(StateMachineTest.Events.EVENT1)
                .when(checkCondition())
                .perform(doAction());

            builder.build("DuplicatedMachine");
            builder.build("DuplicatedMachine");
        });
    }

    private Condition<Context> checkCondition() {
        return (ctx) -> {
            return true;
        };
    }

    private Condition<StateMachineTest.Context> checkConditionFalse() {
        return (ctx) -> {
            return false;
        };
    }

    private Action<States, Events, Context> doAction() {
        return (from, to, event, ctx) -> {
            System.out.println(
                ctx.operator + " is operating " + ctx.entityId + "from:" + from + " to:" + to + " on:" + event);
        };
    }
}
