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
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 * JvmWatchdog Test.
 *
 * @author julien.paoletti@gmail.com
 */
public class JvmWatchdogTest {

    public static final String MY_JVM_VALUE = "myJVM";

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

        // starts the watchdog against the JVM in a dedicated thread to avoid the wait
        Runnable r = new Runnable() {
            @Override
            public void run() {
                System.out.println("Starting the watchdog in a dedicated thread ..");
                String[] args = new String[]{"--" + JvmWatchdog.AGENT_OPTION, "target/" + jarNames[0], "--" + JvmWatchdog.PID_OPTION, theVm.id()};
                JvmWatchdog.main(args);
            }
        };
        Thread t = new Thread(r);
        t.start();
        
        // sleeps for 3s
        Thread.sleep(3000);
        
        // JMX client
        System.out.println("Connecting to the watchdog via JMX");
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

        // stops the watchdog
        System.out.println("Stopping watchdog via JMX");
        ObjectName mbeanName = new ObjectName(JvmWatchdog.MXBEAN_NAME);
        JvmWatchdogMXBean mbeanProxy = JMX.newMXBeanProxy(mbsc, mbeanName, JvmWatchdogMXBean.class, true);
        mbeanProxy.stop();

        // closes JMX connectino
        System.out.println("Closing connection");
        jmxc.close();
    }
}