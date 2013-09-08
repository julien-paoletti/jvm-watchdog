package org.javabenchmark.jvmwatchdog;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.pmw.tinylog.Logger;

/**
 * A component that is started by the watchdog agent to send heartbeat to the
 * watch dog.
 *
 * @author julien.paoletti@gmail.com
 */
public class HeartbeatGenerator {

    private String pid;
    private int port;
    private long heartBeatSequence = 0l;
    private ScheduledExecutorService scheduler;

    /**
     * builds a new heart beat generator.
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
        try {
            // establishes a connexion with the watchdog
            Socket socket = new Socket("localhost", port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Logger.debug("Agent for pid {0} established a connection with watchdog on port: {1}", pid, port);

            // sends a JSON message with pid, hbid, mem, cpu, slow
            StringBuilder message = new StringBuilder("{");
            message.append("pid:").append(pid)
                    .append(",hbid:").append(heartBeatSequence++)
                    .append("}");
            out.println(message.toString());
            Logger.debug("Message sent to watchdog: {0}", message.toString());

            // closes connection
            out.close();
            socket.close();

        } catch (UnknownHostException ex) {
            Logger.error(ex);
        } catch (ConnectException ex) {
            Logger.error("Can not connect to the watchdog .. Must be off");
        } catch (IOException ex) {
            Logger.error(ex);
        }
    }
}
