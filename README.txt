     ___   ____  __  __      __    _      _    ___            
  _ | \ \ / /  \/  | \ \    / /_ _| |_ __| |_ |   \ ___  __ _ 
 | || |\ V /| |\/| |  \ \/\/ / _` |  _/ _| ' \| |) / _ \/ _` |
  \__/  \_/ |_|  |_|   \_/\_/\__,_|\__\__|_||_|___/\___/\__, |
                                                        |___/   v${project.version}

This is a tool that monitors a given JVM process in order to detect any failures that may occur.

----------------
Run the watchdog
----------------

Warning: The JAVA HOME variable must be set before running the watchdog.


On Unix, executes: ./run-with-unsecured-jmx.sh
On Windows, executes: run-with-unsecured-jmx.bat

In both cases, the script starts the watchdog with JMX enabled, but without any
security control when accessing the watchdog through JMX (i.e anonymous mode,
every one can connect).
