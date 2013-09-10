package org.javabenchmark.jvmwatchdog;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.pmw.tinylog.Logger;

/**
 * A component that is started by the watchdog agent to send heartbeat to the
 * watchdog.
 *
 * @author julien.paoletti@gmail.com
 */
public class HeartbeatGenerator {

    /**
     * process id
     */
    private String pid;
    /**
     * the port of the watchdog
     */
    private int port;
    /**
     * the sequence for counting heartbeats
     */
    private long heartbeatSequence = 0l;
    /**
     * the amount of time that exceeds 1000 millis between two heartbeats
     * generation.
     */
    private long slowness;
    /**
     * the service that launches heartbeat generation every 1000 millis.
     */
    private ScheduledExecutorService scheduler;

    /**
     * builds a new heartbeat generator.
     */
    public HeartbeatGenerator(String aPid, int aPort) {

        this.pid = aPid;
        this.port = aPort;

        // builds a thread scheduler
        scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "WATCHDOG-AGENT-THREAD");
            }
        });
    }

    /**
     * starts generating heartbeat every second.
     */
    public void start() {

        scheduler.scheduleWithFixedDelay(new Runnable() {
            private long previousTime = System.currentTimeMillis();

            @Override
            public void run() {

                // computes elapsed time since previous run (must be near 1000 millis)
                long now = System.currentTimeMillis();
                slowness = now - previousTime - 1000;

                sendHeartbeat();

                // updates time
                previousTime = System.currentTimeMillis();;
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * builds a JSON message that holds JVM metrics, connects to the watchog,
     * sends the message and closes the connection.
     */
    private void sendHeartbeat() {

        long startTime = System.currentTimeMillis();
        
        // the socket to the watchdog
        Socket socket = null;

        try {

            // sends a JSON message with pid, hbid, mem, slowness %
            StringBuilder message = new StringBuilder("{");

            // appends process id and heartbeat id
            message.append("pid:").append(pid)
                    .append(",hbid:").append(heartbeatSequence++);

            // appends memory usage
            MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage memoryUsage = memoryMxBean.getHeapMemoryUsage();
            message.append(",mem:").append(memoryUsage.getUsed());

            // appends slowness %
            message.append(",slow:").append(slowness);

            // ends message
            message.append("}");

            // establishes a connexion with the watchdog
            socket = new Socket("localhost", port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Logger.debug("Agent for pid {0} established a connection with watchdog on port: {1}", pid, port);

            // sends message
            out.println(message.toString());
            Logger.debug("Message sent to watchdog: {0}", message.toString());
            out.close();

        } catch (UnknownHostException ex) {
            Logger.error(ex);
        } catch (ConnectException ex) {
            Logger.error("Can not connect to the watchdog .. Must be off");
        } catch (IOException ex) {
            Logger.error(ex);
        } finally {
            // closes connection
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    Logger.error("Can not close the socket", ex);
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        Logger.debug("Send heartbeat took {0} ms", endTime - startTime);
    }
}
