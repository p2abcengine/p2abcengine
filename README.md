p2abcengine
===========

This Privacy-Preserving Attribute-Based Credential Engine enables application developers to use Privacy-ABCs with all their features without having to consider the specifics of the underlying cryptographic algorithms, similar to as they do today for digital signatures, where they do not need to worry about the particulars of the RSA and DSA algorithms either.

An introduction to the project and links to further information is found on the [wiki][wikihome].

Overview of Codebase
===========

We here provide a number of core components for authentication with privacy-preserving attribute-based credentials. These core components deal with the policy language that specifies the authentication requirements, a user interface that allows user to select 
which credentials they want to use to satisfy the authentication policy and then 
some components to generate and verify authentication tokens. 

Building a full-fledged authentication and authorization solution requires a number of additional components such as credential storage or key management. We provide basic implementations of such components and of example application as well. While these components could be useful as well, you might have to replace them with your own ones to integrate the core components into your application.

Finally, we do not provide that cryptography that generates the cryptographic values in the authentication token. However, our engine is designed to be used with Identity Mixer and U-Prove. So all you need to do is to download either [Microsoft U-Prove](http://uprovecsharp.codeplex.com) 
and/or [IBM Identity Mixer](http://prime.inf.tu-dresden.de/idemix) (Version 2.3.40) separately (some of the the features will only work if Identity Mixer is installed).


For more details about the components and how to integrate them into an application we refer to the [Architecture](https://github.com/p2abcengine/p2abcengine/wiki/Architecture) page.


How to Build
==========

Requirements
----------
Java JDK 1.6 or higher is required for building the project.

Microsoft .NET runtime *FULL Profile* version 4 or Mono runtime version > 2.8 is required for building the project (also when just building the Java tree).

[IBM Identity Mixer](http://prime.inf.tu-dresden.de/idemix) jar (Version 2.3.40) & [Microsoft U-Prove Binary](http://uprovecsharp.codeplex.com).

Building
----------

1. Find and install Maven 3.0.x from  http://maven.apache.org - if not already installed

2. Change directory:

    ```cd java/ri/trunk```

3. Decompress the file Dependencies/idemix-mvn.tar.gz in your local maven repository (in Linux: ~/.m2/repository).
   Place the Identity Mixer jar file in the appropriate folder of the maven repository (~/.m2/repository/com/ibm/zurich/idmx/2-3-40/idmx-2-3-40.jar).

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
See the page on integration for information on how to [integrate the ABCE][wikiintegration].

[wikihome]: https://github.com/p2abcengine/p2abcengine/wiki
[wikiintegration]: https://github.com/p2abcengine/p2abcengine/wiki/Integrating%20the%20ABC-Engine


Acknowledgements
===============

The  [architecture](https://github.com/p2abcengine/p2abcengine/wiki/Architecture) and the specification of the p2abcengine have been done as part of the 
[ABC4Trust](https://www.abc4trust.eu) project.  

The code and documentation available here is by [Alexandra Institute](http://www.alexandra.dk/), [Miracle](https://http://www.miracleas.dk), and [IBM Research-Zurich](http://www.zurich.ibm.com) with support from the [ABC4Trust](https://www.abc4trust.eu), [FI-ware](https://www.fi-ware.eu),  [FutureID](https://www.futureid.eu),
and [PrimeLife](http://www.primelife.eu) projects.


