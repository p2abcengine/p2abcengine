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
and/or [IBM Identity Mixer](http://prime.inf.tu-dresden.de/idemix) separately (some of the the features will only work if Identity Mixer is installed).


For more details about the components and how to integrate them into an application we refer to the [Architecture](https://github.com/p2abcengine/p2abcengine/wiki/Architecture) page.


How to Build
==========

### Requirements

The following components are required for building the project:

* Java Development Kit (JDK) 1.6 or higher. Note that the Java Runtime Environment (JRE) is not sufficient.

  Unix: `sudo apt-get install openjdk-6-jdk`

* [Maven 3.0.x](http://maven.apache.org)
  
  Unix: `sudo apt-get install maven`

  Although we use Maven as build tool, there are two required libraries that are not available in
  public Maven repositories. Therefore, in the following, we provide instructions on how these libraries
  can be integrated with your local Maven repository.

* IBM Identity Mixer Version 2.3.40.
  [Download](https://prime.inf.tu-dresden.de/idemix/) the binary (com.ibm.zurich.idmx.2-3-40.jar) and install
  it into your local maven repository:
```
mvn install:install-file \
   -DgroupId=com.ibm.zurich \
   -DartifactId=idmx \
   -Dpackaging=jar \
   -Dversion=2-3-40 \
   -Dfile=com.ibm.zurich.idmx.2-3-40.jar \
   -DgeneratePom=true
```

* PLT Utilities, which is a [component](http://drjava.sourceforge.net/components.shtml) of the [DrJava](http://drjava.sourceforge.net/) project.
  [Download](https://drjava.svn.sourceforge.net/svnroot/drjava/trunk/drjava/lib/plt.jar) the binary (plt.jar) and install it into your local maven repository:
```
mvn install:install-file \
   -DgroupId=plt \
   -DartifactId=plt \
   -Dpackaging=jar \
   -Dversion=1.0 \
   -Dfile=plt.jar \
   -DgeneratePom=true
```

* Microsoft .NET runtime version 4 _FULL Profile_<br>
  _or_<br>
  [Mono project](http://mono-project.com/) version > 2.8

  [Ubuntu](http://mono-project.com/DistroPackages/Ubuntu): `sudo apt-get install mono-complete` (Note: Ubuntu does not come with Mono installed by default any more. Also the _mono-runtime_ package is not sufficient).

* [Microsoft U-Prove Binary](http://uprovecsharp.codeplex.com)

### Building

2. Change directory:

    ```cd Code/java-ri```

4. Place U-Prove binaries in dotNet/releases/1.0.0/

5. On Windows 7 - start UProve Service:

    ```ABC4Trust-UProve.exe 32123```

    On Windows XP/Unix/Mono platforms, the service starts automatically.

6. Build the code with the command
   
    ```mvn clean install -DskipTests```
    
   If the build fails with java.lang.OutOfMemoryError Exceptions, make sure the Maven build process has enough memory:
   * Windows: `set MAVEN_OPTS=-Xmx1024m -Xms256m -XX:MaxPermSize=512m`<br>
     Be aware that the 'set' command only sets the MAVEN_OPTS variable for the current console session.
     To have the variable set permanently (for all future console sessions), set this variable as Windows environment variable manually or via 'setx'.    
   * Unix variants: `export MAVEN_OPTS='-Xmx2024m -Xms256m -XX:MaxPermSize=1024m'`<br>
     In Unix, to prevent this common error, these options are set automatically if you run `mvn` from the java-ri folder.

   Once the code can successfully be built, you can go a step further and also execute all unit tests:

   ```mvn clean install```

### Eclipse Import

You can optionally use Maven to generate Eclipse project files (.project):

```mvn eclise:eclipse```

The projects are generated in the individual module folders and can be imported in Eclipse as existing projects.


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


