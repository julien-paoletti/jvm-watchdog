package org.javabenchmark.jvmwatchdog;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JvmWatchDog Test.
 * @author julien.paoletti@gmail.com
 */
public class JvmWatchDogTest {

    @Test
    public void shouldMonitorGivenJvms() {
        
        String[] args = new String[] {};
        JvmWatchDog.main(args);
    }
}