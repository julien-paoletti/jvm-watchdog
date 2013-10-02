package org.javabenchmark.jvmwatchdog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import static org.javabenchmark.jvmwatchdog.HeartbeatGenerator.HEARTBEAT_ENCODING;
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

        FileOutputStream fout = null;

        try {
            // reads agent's message
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), HEARTBEAT_ENCODING));
            String message = in.readLine();
            in.close();

            Logger.debug("Heartbeat message received: {0}", message);

            // checks message in case of agent IO failure
            if (message == null) {
                Logger.error("Heartbeat message is null ..");
                return;
            }

            // extracts metrics from agent's message
            String[] metrics = message.substring(1, message.length() - 1).split(",");
            String pid = extractValue(metrics[0]);
            String hbid = extractValue(metrics[1]);
            String mem = extractValue(metrics[2]);
            String slow = extractValue(metrics[3]);

//            Logger.info("pid={0}", pid);
//            Logger.info("hbid={0}", hbid);
//            Logger.info("mem={0}", mem);
//            Logger.info("slow={0}", slow);

            // metrics directory
            File metricsDirectory = new File(this.watchDog.getMetricsDirectory());
            if (!metricsDirectory.exists() && !metricsDirectory.mkdirs()) {
                Logger.error("Failed to make the metrics directory ..");
                Logger.error("Metrics sent are: {0}", message);
                return;
            }

            // metrics file
            File metricsFile = new File(metricsDirectory, pid + ".csv");
            boolean isNewFile = !metricsFile.exists();

            // the stream used to write into the metrics file
            fout = new FileOutputStream(metricsFile, true);

            if (isNewFile) {
                // appends csv header
                fout.write("pid,hbid,mem,slow\n".getBytes(HEARTBEAT_ENCODING));
            }

            // builds the metrics line to append
            StringBuilder sb = new StringBuilder(pid);
            sb.append(',').append(hbid);
            sb.append(',').append(mem);
            sb.append(',').append(slow).append('\n');
            String metricsLine = sb.toString();

            // appends a metrics line into a pid dedicated file
            fout.write(metricsLine.getBytes(HEARTBEAT_ENCODING));
            fout.flush();

        } catch (IOException e) {
            Logger.error("An error occurs when processing agent's message: {0}", e.getMessage());
        } finally {

            // closes stream
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException ex) {
                    Logger.error("Can not close the output stream: {0}", ex.getMessage());
                }
            }

            // closes socket
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    Logger.error("Can not close the output stream: {0}", ex.getMessage());
                }
            }
        }
    }

    private String extractValue(String keyValue) {
        return keyValue.substring(keyValue.indexOf(':') + 1);
    }
}
