package org.javabenchmark.jvmwatchdog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import org.pmw.tinylog.Logger;

/**
 * A processor for heartbeat messages sent over network to the watchdog. This
 * class runs in a dedicated thread. Its goal is to read the message, extract
 * and forward content to the watchdog.
 *
 * @author julien.paoletti@gmail.com
 */
public class HeartbeatProcessor implements Runnable {

    private Socket socket;
    private JvmWatchdog watchDog;

    /**
     * instantiates a new heartbeat processor with given socket and watchdog.
     *
     * @param aSocket a client socket.
     * @param aWatchDog a watchdog.
     */
    public HeartbeatProcessor(Socket aSocket, JvmWatchdog aWatchDog) {
        this.socket = aSocket;
        this.watchDog = aWatchDog;
    }

    /**
     * Reads the heartbeat message of the agent, extracts and forwards metrics
     * to the watchdog.
     */
    @Override
    public void run() {

        try {
            // reads agent's message
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = in.readLine();
            Logger.debug("Heartbeat Message received: {0}", message);

            // closes connection
            in.close();
            socket.close();

            // extracts metrics from agent's message
            String[] metrics = message.substring(1, message.length() - 1).split(",");
            String pid = extractValue(metrics[0]);
            String hbid = extractValue(metrics[1]);
            Logger.info("pid={0}", pid);
            Logger.info("hbid={0}", hbid);

        } catch (IOException e) {
            Logger.error("An error occurs when reading agent's message ..", e);
        }
    }

    private String extractValue(String keyValue) {
        return keyValue.substring(keyValue.indexOf(':') + 1);
    }
}
