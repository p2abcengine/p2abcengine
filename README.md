p2abcengine
===========

An introduction to the project is found on the wiki. See the [wiki][wikihome].

Overview of Codebase
===========



How to Build
==========

Requirements
----------
Java JDK 1.6 or higher is required for building the project.

Microsoft .NET runtime *FULL Profile* version 4 or Mono runtime version > 2.8 is required for building the project (also when just building the Java tree).

IBM Identity Mixer jar version 2-3-39. TODO, where can the jar be downloaded? 

Microsoft U-Prove Binary. TODO, where can the binary be downloaded?

Building
----------

1. Find and install Maven 3.0.x from  http://maven.apache.org - if not already installed

2. Change directory:

    ```cd java/ri/trunk```

3. Place Identity Mixer jar in maven repository.

4. Place U-Prove binaries in dotNet/releases/1.0.0/

5. On Windows 7 - start UProve Service:

    ```ABC4Trust-UProve.exe 32123```

    (on Windows XP/Unix/Mono platforms - the service starts automatically)

6. Run:

    ```mvn clean install```

    If the build fails and the output contains java.lang.OutOfMemoryError Exceptions, make sure the Maven build process has enough memory:
    * Windows 32 bit: ``set MAVEN_OPTS=-Xmx1024m -Xms256m -XX:MaxPermSize=256m``
    * Windows 64 bit: ``set MAVEN_OPTS=-Xmx1024m -Xms256m -XX:MaxPermSize=512m``
    * Unix variants:  ``export MAVEN_OPTS='-Xmx1024m -Xms256m -XX:MaxPermSize=512m' ``

    Be aware that the "set" command only sets the MAVEN_OPTS variable for the current console session. To have the variable set permanently (for all future console sessions), set this variable as Windows environment variable manually or via "setx".

7. Optionally generate Eclipse project files by running:

    ```mvn eclise:eclipse```

8. Optionally import the projects in Eclipse as existing Maven projects


Usage
==========
See the page on integration for information on how to [integrate the ABCE][wikihome].

[wikihome]: https://github.com/p2abcengine/p2abcengine/wiki
[wikiintegration]: https://github.com/p2abcengine/p2abcengine/wiki/Integrating%20the%20ABC-Engine