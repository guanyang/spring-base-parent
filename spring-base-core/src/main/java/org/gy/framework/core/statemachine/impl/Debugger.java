package org.gy.framework.core.statemachine.impl;

/**
 * Debugger, This is used to decouple Logging framework dependency
 *
 * 
 * @date 2020-02-11 11:08 AM
 */
public class Debugger {

    private static boolean isDebugOn = false;

    public static void debug(String message){
        if(isDebugOn){
            System.out.println(message);
        }
    }

    public static void enableDebug(){
        isDebugOn = true;
    }
}
