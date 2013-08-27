package org.javabenchmark.jvmwatchdog;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import java.io.File;
import java.io.IOException;
import java.util.List;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pmw.tinylog.Logger;

/**
 * The main class.
 *
 * @author julien.paoletti@gmail.com
 */
public class JvmWatchDog {

    public static final String AGENT_OPTION = "agent";
    public static final String PID_OPTION = "pid";
    private File agentJarFile;
    private String[] pids;

    /**
     * instantiates a new JVM watch dog.
     */
    public JvmWatchDog() {
    }

    /**
     * The main method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        JvmWatchDog watchDog = new JvmWatchDog();
        watchDog.processJvmOptions(args);
        watchDog.loadAgentIntoJvms();

    }

    private static String getValueOfOption(OptionSet options, String option) {
        if (checkOptionAndArgument(options, option)) {
            return (String) options.valueOf(option);
        } else {
            return null;
        }
    }

    private static List<String> getValuesOfOption(OptionSet options, String option) {
        if (checkOptionAndArgument(options, option)) {
            return (List<String>) options.valuesOf(option);
        } else {
            return null;
        }
    }

    private static boolean checkOptionAndArgument(OptionSet options, String option) {
        if (!options.has(option)) {
            Logger.error("The --{0} option is missing", option);
            return false;
        }
        if (!options.hasArgument(option)) {
            Logger.error("The argument of the --{0} option is missing", option);
            return false;
        }
        return true;
    }

    /**
     * processes the JVM options provided in the command line when the watch dog
     * is started.
     *
     * @param args the args of the main method.
     * @return true if options are valid, false otherwise.
     */
    private boolean processJvmOptions(String[] args) {

        // options parsing
        OptionParser parser = new OptionParser();
        parser.accepts(AGENT_OPTION).withRequiredArg();
        parser.accepts(PID_OPTION).withRequiredArg();
        OptionSet options = parser.parse(args);
        
        // options
        String agentPath = getValueOfOption(options, AGENT_OPTION);
        final String literalPids = getValueOfOption(options, PID_OPTION);

        // aborts in case of missing mandatory options
        if (agentPath == null || literalPids == null) {
            Logger.error("Aborting because of missing mandatory options");
            return false;
        }

        // checks pids
        pids = literalPids.split(",");
        if (pids.length == 0) {
            Logger.error("Aborting because of no pid provided");
            return false;
        }

        // checks agent JAR file
        agentJarFile = new File(agentPath);
        if (!agentJarFile.exists()) {
            Logger.error("Aborting because the agent JAR file does not exist: {1}", agentJarFile.getAbsolutePath());
            return false;
        }

        Logger.info("Options\n-------");
        Logger.info("Process Id(s): {0}", literalPids);
        Logger.info("Agent JAR file: {0}", agentJarFile.getAbsolutePath());
        return true;
    }
    
    /**
     * loads the watch dog agent into each JVM provided with the pid option.
     */
    private void loadAgentIntoJvms() {
        
        // loads the watchdog agent into each JVM
        for (int i = 0; i < pids.length; i++) {

            String pid = pids[i];
            try {
                VirtualMachine vm = VirtualMachine.attach(pid);
                vm.loadAgent(agentJarFile.getAbsolutePath());
                Logger.info("The Watch Dog Agent was loaded into JVM with pid {0} ({1})", pid, vm.provider().name());
                vm.detach();

            } catch (AttachNotSupportedException ex) {
                Logger.error("The JVM with pid {0} does not support dynamic attach !");
            } catch (IOException ex) {
                Logger.error("An IO error occurs when attaching to JVM with pid {0} because: {1}", pid, ex.getMessage());
            } catch (AgentLoadException ex) {
                Logger.error("The agent can not be loaded into the JVM with pid {0} because: {1}", pid, ex.getMessage());
            } catch (AgentInitializationException ex) {
                Logger.error("The agent can not be initialized into the JVM with pid {0} because: {1}", pid, ex.getMessage());
            }
        }
    }
}
