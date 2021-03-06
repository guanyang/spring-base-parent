package org.gy.framework.core;

import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskEventEnum.Agree_Over_P0_Sell;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskEventEnum.Apply_Over_P0_Sell;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskEventEnum.Create;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskEventEnum.Normal_Update;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskEventEnum.P0_Changed;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskEventEnum.Page_Price_changed;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskEventEnum.Reject_Over_P0_Sell;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskEventEnum.Supplier_Agree;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskEventEnum.Supplier_Reject;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskEventEnum.Supplier_Timeout;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskStatusEnum.Closed;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskStatusEnum.None;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskStatusEnum.Price_Manager_Processing;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskStatusEnum.Supplier_Manager_Processing;
import static org.gy.framework.core.StateMachinePlantUMLTest.PriceAdjustmentTaskStatusEnum.Supplier_Processing;

import java.util.stream.Stream;
import org.gy.framework.core.StateMachineTest.Context;
import org.gy.framework.core.statemachine.Action;
import org.gy.framework.core.statemachine.Condition;
import org.gy.framework.core.statemachine.StateMachine;
import org.gy.framework.core.statemachine.builder.StateMachineBuilder;
import org.gy.framework.core.statemachine.builder.StateMachineBuilderFactory;
import org.gy.framework.core.statemachine.impl.Debugger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * StateMachinePlantUMLTest
 *
 * 
 * @date 2020-02-09 7:53 PM
 */
public class StateMachinePlantUMLTest {

    static enum PriceAdjustmentTaskStatusEnum {
        /**
         * ????????????
         */
        None,
        /**
         * ???????????????
         */
        Supplier_Processing,
        /**
         * ?????????????????????
         */
        Supplier_Manager_Processing,
        /**
         * ???????????????????????????
         */
        Price_Manager_Processing,
        /**
         * ??????
         */
        Closed
    }

    static enum PriceAdjustmentTaskEventEnum {

        // ????????????
        Create,
        Normal_Update,
        /**
         * ???????????????
         */
        P0_Changed,
        /**
         * ??????????????????
         */
        Page_Price_changed,

        // ????????????
        Supplier_Reject,
        Supplier_Agree,
        Supplier_Timeout,

        // ??????????????????
        Apply_Over_P0_Sell,

        // ??????????????????
        Agree_Over_P0_Sell,
        Reject_Over_P0_Sell;

        public boolean isSupplierTimeout() {
            return this == Supplier_Timeout;
        }

        public boolean isSystemEvent(){
            return  this == Create ||
                    this == Normal_Update ||
                    this == P0_Changed ||
                    this == Page_Price_changed;
        }
    }

    @BeforeAll
    public static void init(){
        Debugger.enableDebug();
    }

    @Test
    public void testPlantUML(){
        StateMachineBuilder<PriceAdjustmentTaskStatusEnum, PriceAdjustmentTaskEventEnum, Context> builder = StateMachineBuilderFactory.create();

        builder.externalTransition()
                .from(None)
                .to(Supplier_Processing)
                .on(Create)
                .when(checkCondition())
                .perform(doAction());

        // ????????????
        Stream.of(Supplier_Processing, Supplier_Manager_Processing, Price_Manager_Processing)
                .forEach(status ->
                        builder.externalTransition()
                                .from(status)
                                .to(Closed)
                                .on(Supplier_Agree)
                                .when(checkCondition())
                                .perform(doAction())
                );

        // ?????? -?????????-> ????????????
        builder.externalTransition()
                .from(Supplier_Processing)
                .to(Supplier_Manager_Processing)
                .on(Supplier_Reject)
                .when(checkCondition())
                .perform(doAction());

        builder.externalTransition()
                .from(Supplier_Processing)
                .to(Supplier_Manager_Processing)
                .on(Supplier_Timeout)
                .when(checkCondition())
                .perform(doAction());

        // ??????????????????P0??????
        builder.externalTransition()
                .from(Supplier_Manager_Processing)
                .to(Price_Manager_Processing)
                .on(Apply_Over_P0_Sell)
                .when(checkCondition())
                .perform(doAction());

        // ????????????P0?????????
        builder.externalTransition()
                .from(Price_Manager_Processing)
                .to(Closed)
                .on(Agree_Over_P0_Sell)
                .when(checkCondition())
                .perform(doAction());

        // ????????????P0?????????
        builder.externalTransition()
                .from(Price_Manager_Processing)
                .to(Supplier_Manager_Processing)
                .on(Reject_Over_P0_Sell)
                .when(checkCondition())
                .perform(doAction());

        // ????????????????????????
        Stream.of(Supplier_Processing, Supplier_Manager_Processing, Price_Manager_Processing)
                .forEach(status -> builder
                        .internalTransition()
                        .within(status)
                        .on(Normal_Update)
                        .when(checkCondition())
                        .perform(doAction())
                );

        // P0????????????????????????????????????????????????
        Stream.of(P0_Changed, Page_Price_changed)
                .forEach(event -> builder.externalTransitions()
                        .fromAmong(Supplier_Processing, Supplier_Manager_Processing, Price_Manager_Processing)
                        .to(Closed)
                        .on(event)
                        .when(checkCondition())
                        .perform(doAction()));

        StateMachine stateMachine = builder.build("AdjustPriceTask");
        String plantUML = stateMachine.generatePlantUML();
        System.out.println(plantUML);

    }

    private Condition<Context> checkCondition() {
        return (ctx) -> {return true;};
    }

    private Action<PriceAdjustmentTaskStatusEnum, PriceAdjustmentTaskEventEnum, Context> doAction() {
        return (from, to, event, ctx)->{
            System.out.println(ctx.operator+" is operating "+ctx.entityId+" from:"+from+" to:"+to+" on:"+event);
        };
    }
}
