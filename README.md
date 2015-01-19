<p style="text-align: center;">
<a href="https://abc4trust.eu/" ><img src="https://raw.github.com/p2abcengine/p2abcengine/master/Documentation/logos/abc4trust.png" alt="Abc4Trust" /></a>
<a href="http://fi-ware.eu" ><img src="https://raw.github.com/p2abcengine/p2abcengine/master/Documentation/logos/fiware.png" alt="FI-Ware" /></a>
<a href="http://primelife.ercim.eu" ><img src="https://raw.github.com/p2abcengine/p2abcengine/master/Documentation/logos/primelife.jpeg" alt="PrimeLife" /></a>
</p>

p2abcengine
===========

This Privacy-Preserving Attribute-Based Credential Engine enables application developers to use Privacy-ABCs with all their features without having to consider the specifics of the underlying cryptographic algorithms, similar to as they do today for digital signatures, where they do not need to worry about the particulars of the RSA and DSA algorithms either.

An introduction to the project and links to further information is found on the [wiki][wikihome].


Overview of Codebase
===========

We provide a number of core components for authentication with privacy-preserving attribute-based credentials. These core components deal with the policy language that specifies the authentication requirements, a user interface that allows user to select which credentials they want to use to satisfy the authentication policy and then some components to generate and verify authentication tokens.

Building a full-fledged authentication and authorization solution requires a number of additional components such as credential storage or key management. We provide basic implementations of such components and of example application as well. While these components could be useful as well, you might have to replace them with your own ones to integrate the core components into your application.

Finally, we do not provide that cryptography that generates the cryptographic values in the authentication token. However, our engine is designed to be used with Identity Mixer. So all you need to do is to download [IBM Identity Mixer](https://abc4trust.eu/idemix) separately. Our library is also interoperable with [Microsoft U-Prove](http://uprovecsharp.codeplex.com).

For more details about the components and how to integrate them into an application we refer to the [Architecture](https://github.com/p2abcengine/p2abcengine/wiki/Architecture) page.

Usage
==========

NOTE: The java-ui component (and possibly other components) refer to an older version of core-abce. We are currently working on a fix for this, as well as updating documentation.

The p2abcengine can be used as either a series of platform independant web services or be integrated directly into a Java codebase.
For instructions on how to build the ABCE, look at the wiki page [how to build the ABCE for development][wikihowtobuild].
To set up the web services, refer to the wiki page [creating an ABC system using web services][wikiwebservices].
To integrate directly, see the page  [integrating the ABCE][wikiintegration]. 

[wikihome]: https://github.com/p2abcengine/p2abcengine/wiki
[wikiintegration]: https://github.com/p2abcengine/p2abcengine/wiki/Integrating%20the%20ABC-Engine
[wikihowtobuild]: https://github.com/p2abcengine/p2abcengine/wiki/How-to-Build-the-ABC-Engine
[wikiwebservices]: https://github.com/p2abcengine/p2abcengine/wiki/Creating-an-ABC-system-using-web-services

License
===========
The source code of the p2abcengine is licensed under the [Apache License, Version 2.0](https://github.com/p2abcengine/p2abcengine/blob/master/Code/LICENSE.txt).

Note, however, that the p2abcengine depends on a number of Java libraries that are licensed under other licenses.
For details, see the [overview of licenses of the library dependencies](https://github.com/p2abcengine/p2abcengine/blob/master/Code/LICENSES-OF-DEPENDENCIES.txt).

The p2abcengine requires [IBM Identity Mixer](https://abc4trust.eu/idemix) as cryptographic engine.
Identity Mixer is available under the [Identity Mixer License](https://abc4trust.eu/idemix).


Acknowledgements
===============

The  [architecture](https://github.com/p2abcengine/p2abcengine/wiki/Architecture) and the specification of the p2abcengine have been done as part of the 
[ABC4Trust](https://www.abc4trust.eu) project.  

The code and documentation available here is by [Alexandra Institute](http://www.alexandra.dk/), [Miracle](https://http://www.miracleas.dk), and [IBM Research - Zurich](http://www.zurich.ibm.com) with support from the [ABC4Trust](https://www.abc4trust.eu), [AU2EU](http://www.au2eu.eu), [FI-ware](https://www.fi-ware.eu),  [FutureID](https://www.futureid.eu), and [PrimeLife](http://www.primelife.eu) projects.


