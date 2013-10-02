package org.javabenchmark.jvmwatchdog;

import com.sun.tools.attach.VirtualMachine;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.management.MalformedObjectNameException;
import org.junit.Test;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.runner.RunWith;
import static org.fest.assertions.api.Assertions.*;

/**
 * Watchdog dedicated test that checks metrics dir creation failure.
 *
 * @author julien.paoletti@gmail.com
 */
@RunWith(BMUnitRunner.class)
public class JvmWatchdogMetricsDirFailureTest {

    /**
     * A smoke test with fault injection. Force File.mkdirs() method to return
     * false.
     *
     * @throws MalformedURLException
     * @throws IOException
     * @throws MalformedObjectNameException
     */
    @Test
    @BMRule(name = "Mkdirs Rule",
            targetClass = "File",
            targetMethod = "mkdirs()",
            targetLocation = "AT ENTRY",
            condition = "TRUE",
            action = "return false")
    public void shouldHandleMkdirsFailure() throws MalformedURLException, IOException, MalformedObjectNameException, InterruptedException {

        // deletes metrics dir if necessary
        File metricsDirectory = new File(JvmWatchdogTestHelper.METRICS_DIRECTORY);
        if (metricsDirectory.exists()) {
            File[] metricsFiles = metricsDirectory.listFiles();
            for (int i = 0; i < metricsFiles.length; i++) {
                File file = metricsFiles[i];
                assertThat(file.delete()).isTrue();
            }
            assertThat(metricsDirectory.delete()).isTrue();
        }

        // gets this JVM
        final VirtualMachine thisVm = JvmWatchdogTestHelper.findThisVm();

        // gets the file corresponding to the agent.jar
        final String agentJarFileName = JvmWatchdogTestHelper.findAgentJarFileName();

        // starts the watchdog
        JvmWatchdogTestHelper.startWatchdog(agentJarFileName, thisVm);

        // sleeps for 5 to let failures occur
        Thread.sleep(5 * 1000);

        // shutdown watchdog
        JvmWatchdogShutdown.main(null);
    }
}