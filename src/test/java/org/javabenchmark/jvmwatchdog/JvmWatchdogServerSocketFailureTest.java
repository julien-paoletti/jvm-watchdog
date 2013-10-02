package org.javabenchmark.jvmwatchdog;

import com.sun.tools.attach.VirtualMachine;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.management.MalformedObjectNameException;
import org.junit.Test;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.runner.RunWith;

/**
 * Watchdog dedicated test that checks server socket failure.
 *
 * @author julien.paoletti@gmail.com
 */
@RunWith(BMUnitRunner.class)
public class JvmWatchdogServerSocketFailureTest {

    /**
     * A smoke test with fault injection. Throws an IOException when a server
     * socket is instantiated.
     * @throws MalformedURLException
     * @throws IOException
     * @throws MalformedObjectNameException 
     */
    @Test
    @BMRule(name = "ServerSocket Rule",
            targetClass = "ServerSocket",
            targetMethod = "<init>(int)",
            targetLocation = "AT ENTRY",
            condition = "TRUE",
            action = "throw new java.io.IOException(\"Fake IO exception in a JUnit\")")
    public void shouldHandleServerSocketFailure() throws MalformedURLException, IOException, MalformedObjectNameException {

        // gets this JVM
        final VirtualMachine thisVm = JvmWatchdogTestHelper.findThisVm();

        // gets the file corresponding to the agent.jar
        final String agentJarFileName = JvmWatchdogTestHelper.findAgentJarFileName();

        // starts the watchdog
        JvmWatchdogTestHelper.startWatchdog(agentJarFileName, thisVm);

        // shutdown watchdog
        JvmWatchdogShutdown.main(null);
    }
}