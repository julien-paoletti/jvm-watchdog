"%JAVA_HOME%"\bin\java -cp "%JAVA_HOME%\lib\tools.jar;lib\tinylog-0.8.jar;lib\jopt-simple-4.5.jar;lib\jvm-watchdog-1.0-SNAPSHOT.jar" -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false org.javabenchmark.jvmwatchdog.JvmWatchdog --agent lib\jvm-watchdog-1.0-SNAPSHOT.jar --pid %1