package org.javabenchmark.jvmwatchdog;

import java.lang.instrument.Instrumentation;

/**
 * A java agent that is dynamically injected into JVM monitored by the watchdog.
 * Once loaded, this agent sends <i>heartbeat</i> to the watchdog. An heartbeat
 * is a JSON message that holds few data.
 *
 * @author julien.paoletti@gmail.com
 */
public class Agent {

    /**
     * starts the heartbeat generator.
     *
     * @param oneStringOptions the agent options. Options are: the pid of the JVM and the
     * port of the watchdog.
     * @param inst the instrumentation component. Ignored.
     */
    public static void agentmain(String oneStringOptions, Instrumentation inst) {

        // extracts options
        String[] options = oneStringOptions.split(",");
        String pid = options[0];
        int port = Integer.parseInt(options[1]);
        
        // starts a new heartbeat generator
        HeartbeatGenerator generator = new HeartbeatGenerator(pid, port);
        generator.start();

    }
}
