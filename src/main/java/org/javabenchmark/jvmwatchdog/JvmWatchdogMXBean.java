package org.javabenchmark.jvmwatchdog;

/**
 * Interface JvmWatchdogMXBean.
 *
 * @author julien.paoletti@gmail.com
 */
public interface JvmWatchdogMXBean {

    /**
     * closes server socket, shutdowns services and stops waiting.
     */
    public void stop();
    
}
