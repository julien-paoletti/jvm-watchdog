package org.javabenchmark.jvmwatchdog;

import com.sun.tools.attach.VirtualMachine;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.management.MalformedObjectNameException;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 * Watchdog dedicated smoke test.
 *
 * @author julien.paoletti@gmail.com
 */
public class JvmWatchdogSmokeTest {

    /**
     * Starts a watchdog that monitors the JVM that is running this test for 10
     * seconds. Then stops the watchdog and controls that a dedicated metrics
     * file exists.
     *
     * @throws MalformedURLException
     * @throws IOException
     * @throws MalformedObjectNameException
     * @throws InterruptedException
     */
    @Test
    public void shouldMonitorThisJvm() throws MalformedURLException, IOException, MalformedObjectNameException, InterruptedException {

        // gets this JVM
        final VirtualMachine thisVm = JvmWatchdogTestHelper.findThisVm();

        // gets the file corresponding to the agent.jar
        final String agentJarFileName = JvmWatchdogTestHelper.findAgentJarFileName();

        // starts the watchdog
        JvmWatchdogTestHelper.startWatchdog(agentJarFileName, thisVm);

        // sleeps for 10s
        Thread.sleep(10 * 1000);

        // shutdown watchdog
        JvmWatchdogShutdown.main(null);

        // checks that metrics file exists
        File metricsFile = new File(JvmWatchdogTestHelper.METRICS_DIRECTORY, thisVm.id() + ".csv");
        assertThat(metricsFile).exists();
    }
}