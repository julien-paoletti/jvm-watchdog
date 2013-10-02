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
 * Watchdog dedicated test that checks socket failure.
 *
 * @author julien.paoletti@gmail.com
 */
@RunWith(BMUnitRunner.class)
public class JvmWatchdogSocketFailureTest {

    /**
     * A smoke test with fault injection. Throws an IOException when getOutputStream()
     * method is invoked on a socket.
     * @throws MalformedURLException
     * @throws IOException
     * @throws MalformedObjectNameException 
     */
    @Test
    @BMRule(name = "Socket Rule",
            targetClass = "Socket",
            targetMethod = "getOutputStream()",
            targetLocation = "AT ENTRY",
            condition = "Thread.currentThread().getName().equals(\"WATCHDOG-AGENT-THREAD\")",
            action = "throw new java.io.IOException(\"Fake IO exception in a JUnit\")")
    public void shouldHandleAgentSocketFailure() throws MalformedURLException, IOException, MalformedObjectNameException, InterruptedException {

        // gets this JVM
        final VirtualMachine thisVm = JvmWatchdogTestHelper.findThisVm();

        // gets the file corresponding to the agent.jar
        final String agentJarFileName = JvmWatchdogTestHelper.findAgentJarFileName();

        // starts the watchdog
        JvmWatchdogTestHelper.startWatchdog(agentJarFileName, thisVm);
        
        // sleeps for 15s to let failures occur
        Thread.sleep(15 * 1000);

        // shutdown watchdog
        JvmWatchdogShutdown.main(null);
    }
}