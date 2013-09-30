package org.javabenchmark.jvmwatchdog;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * An helper class to shutdown the running watchdog.
 *
 * @author julien.paoletti@gmail.com
 */
public class JvmWatchdogShutdown  {

    /**
     * The main method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws MalformedURLException, IOException, MalformedObjectNameException {

        // JMX client to connect to the watchdog
        System.out.println("Connecting to the watchdog via JMX");
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

        // stops the watchdog through JMX
        System.out.println("Stopping watchdog via JMX");
        ObjectName mbeanName = new ObjectName(JvmWatchdog.MXBEAN_NAME);
        JvmWatchdogMXBean mbeanProxy = JMX.newMXBeanProxy(mbsc, mbeanName, JvmWatchdogMXBean.class, true);
        mbeanProxy.shutdown();
        
        // closes JMX connection
        System.out.println("Closing JMX connection");
        jmxc.close();
    }
}
