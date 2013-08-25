package org.javabenchmark.jvmwatchdog;

import java.lang.instrument.Instrumentation;

/**
 *
 * @author julien.paoletti@gmail.com
 */
public class JvmWatchDogAgent {

    public static void agentmain(String agentArgs, Instrumentation inst) {

        System.out.println("Hey, the agent was dynamically loaded !");
    }
}
