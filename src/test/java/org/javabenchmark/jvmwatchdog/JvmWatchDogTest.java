package org.javabenchmark.jvmwatchdog;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 * JvmWatchDog Test.
 *
 * @author julien.paoletti@gmail.com
 */
public class JvmWatchDogTest {

    public static final String MY_JVM_VALUE = "myJVM";
    
    @Test
    public void shouldMonitorThisJvm() {

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

        // searches for the agent jar into the target directory
        File targetDir = new File("target");
        String[] jarNames = targetDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("jvm-watchdog") && name.endsWith(".jar");
            }
        });

        // starts the watchdog against the JVM
        String[] args = new String[]{"--" + JvmWatchDog.AGENT_OPTION, "target/" + jarNames[0], "--" + JvmWatchDog.PID_OPTION, vm.id()};
        JvmWatchDog.main(args);

    }
}