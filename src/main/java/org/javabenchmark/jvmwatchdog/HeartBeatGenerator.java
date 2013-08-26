package org.javabenchmark.jvmwatchdog;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A component that is started by the watch dog agent to send heart beat
 * to the watch dog.
 *
 * @author julien.paoletti@gmail.com
 */
public class HeartBeatGenerator {

    private static final AtomicLong HEART_BEAT_SEQ = new AtomicLong();
    
    private ScheduledExecutorService scheduler;
    
    /**
     * builds a new heart beat generator.
     */
    public HeartBeatGenerator() {
        
        // builds a thread scheduler
        scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "WATCHDOG-AGENT-THREAD");
            }
        });
    }
    
    /**
     * starts generating heart beat every second.
     */
    public void start() {
        
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                sendHeartBeat();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
    
    private void sendHeartBeat() {
        // TODO sends a JSON message with pid, hbid, mem, cpu, slow
        System.out.println("Hey, the agent sent one heart-beat");
    }
}
