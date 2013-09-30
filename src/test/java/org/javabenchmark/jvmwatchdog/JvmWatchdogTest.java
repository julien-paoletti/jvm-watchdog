package org.javabenchmark.jvmwatchdog;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import javax.management.MalformedObjectNameException;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 * Starts a watchdog that monitors the JVM that is running this test for 10
 * seconds. Then stops the watchdog and controls that a dedicated metrics file
 * exists.
 *
 * @author julien.paoletti@gmail.com
 */
public class JvmWatchdogTest {

    public static final String MY_JVM_VALUE = "myJVM";
    public static final String METRICS_DIRECTORY = "target/metrics";

    @Test
    public void shouldMonitorThisJvm() throws MalformedURLException, IOException, MalformedObjectNameException, InterruptedException {

        // set a unique system property for the current JVM
        String uniqueProperty = UUID.randomUUID().toString();
        System.setProperty(uniqueProperty, MY_JVM_VALUE);

        // gets available JVMs
        boolean jvmWasFound = false;
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();

        // loops over VMs to find our
        VirtualMachine vm = null;
        for (VirtualMachineDescriptor vmd : vms) {
            System.out.println("Checking JVM: " + vmd.displayName() + " (" + vmd.id() + ")");
            try {
                // attaching to the VM
                vm = vmd.provider().attachVirtualMachine(vmd);
                // checking properties
                Properties p = vm.getSystemProperties();
                if (MY_JVM_VALUE.equals(p.getProperty(uniqueProperty))) {
                    // we found our JVM
                    System.out.println("This is our JVM !");
                    jvmWasFound = true;
                    // stops the loop
                    break;
                }
            } catch (AttachNotSupportedException ex) {
                System.err.println("Can't attach because of: " + ex.getMessage());
            } catch (IOException ex) {
                System.err.println("IO error: " + ex.getMessage());
            }
        }

        // controls that a JVM was found
        assertThat(jvmWasFound).isTrue();

        final VirtualMachine theVm = vm;

        // searches for the agent jar into the target directory
        File targetDir = new File("target");
        final String[] jarNames = targetDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("jvm-watchdog") && name.endsWith(".jar");
            }
        });
        assertThat(jarNames).isNotNull();
        assertThat(jarNames).hasSize(1);

        // starts the watchdog against the JVM in a dedicated thread to avoid the wait
        Runnable r = new Runnable() {
            @Override
            public void run() {
                System.out.println("Starting the watchdog in a dedicated thread ..");
                String[] args = new String[]{"--" + JvmWatchdog.METRICS_DIR_OPTION, METRICS_DIRECTORY, "--" + JvmWatchdog.AGENT_OPTION, "target/" + jarNames[0], "--" + JvmWatchdog.PID_OPTION, theVm.id()};
                System.out.println("With Command line options:");
                for (int i = 0; i < args.length; i++) {
                    System.out.print(args[i] + " ");
                }
                System.out.println();
                JvmWatchdog.main(args);
            }
        };
        Thread t = new Thread(r);
        t.start();

        // sleeps for 10s
        Thread.sleep(10 * 1000);

        // shutdown watchdog
        JvmWatchdogShutdown.main(null);

        // checks that metrics file exists
        File metricsFile = new File(METRICS_DIRECTORY, vm.id() + ".csv");
        assertThat(metricsFile).exists();
    }
}