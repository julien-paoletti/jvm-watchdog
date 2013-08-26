package org.javabenchmark.jvmwatchdog;

import java.lang.instrument.Instrumentation;

/**
 * A java agent that is dynamically injected into JVM monitored by the watch dog.
 * Once loaded, this agent sends <i>heart beat</i> to the watch dog. An heart beat
 * is a JSON message that holds few data.
 * @author julien.paoletti@gmail.com
 */
public class JvmWatchDogAgent {

    /**
     * starts the heart beat generator.
     * @param agentArgs the agent args
     * @param inst the instrumentation component
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {

        HeartBeatGenerator generator = new HeartBeatGenerator();
        generator.start();
        
    }
    
}
