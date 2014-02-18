<p style="text-align: center;">
<a href="https://abc4trust.eu/" ><img src="https://raw2.github.com/p2abcengine/p2abcengine/master/Documentation/logos/abc4trust.png" alt="Abc4Trust" /></a>
<a href="http://fi-ware.eu" ><img src="https://raw2.github.com/p2abcengine/p2abcengine/master/Documentation/logos/fiware.png" alt="FI-Ware" /></a>
<a href="http://primelife.ercim.eu" ><img src="https://raw2.github.com/p2abcengine/p2abcengine/master/Documentation/logos/primelife.jpeg" alt="PrimeLife" /></a>
</p>

p2abcengine
===========

This Privacy-Preserving Attribute-Based Credential Engine enables application developers to use Privacy-ABCs with all their features without having to consider the specifics of the underlying cryptographic algorithms, similar to as they do today for digital signatures, where they do not need to worry about the particulars of the RSA and DSA algorithms either.

An introduction to the project and links to further information is found on the [wiki][wikihome].

License
===========
The source code of the p2abcengine is licensed under the [Apache License, Version 2.0](https://github.com/p2abcengine/p2abcengine/blob/master/Code/LICENSE.txt).

Note, however, that the p2abcengine depends on a number of Java libraries that are licensed under other licenses.
<br>
For details, see the [overview of licenses of the library dependencies](https://github.com/p2abcengine/p2abcengine/blob/master/Code/LICENSES-OF-DEPENDENCIES.txt).


Overview of Codebase
===========

We provide a number of core components for authentication with privacy-preserving attribute-based credentials. These core components deal with the policy language that specifies the authentication requirements, a user interface that allows user to select which credentials they want to use to satisfy the authentication policy and then some components to generate and verify authentication tokens.

Building a full-fledged authentication and authorization solution requires a number of additional components such as credential storage or key management. We provide basic implementations of such components and of example application as well. While these components could be useful as well, you might have to replace them with your own ones to integrate the core components into your application.

Finally, we do not provide that cryptography that generates the cryptographic values in the authentication token. However, our engine is designed to be used with Identity Mixer and U-Prove. So all you need to do is to download either [Microsoft U-Prove](http://uprovecsharp.codeplex.com) and/or [IBM Identity Mixer](http://prime.inf.tu-dresden.de/idemix) separately (some of the the features will only work if Identity Mixer is installed).

For more details about the components and how to integrate them into an application we refer to the [Architecture](https://github.com/p2abcengine/p2abcengine/wiki/Architecture) page.

Usage
==========

See the page on integration for information on how to [integrate the
ABCE][wikiintegration]. Or the page on how to build the ABCE for information on [how to build the ABCE for development][wikihowtobuild].

[wikihome]: https://github.com/p2abcengine/p2abcengine/wiki
[wikiintegration]: https://github.com/p2abcengine/p2abcengine/wiki/Integrating%20the%20ABC-Engine
[wikihowtobuild]: https://github.com/p2abcengine/p2abcengine/wiki/How-to-Build-the-ABC-Engine

Acknowledgements
===============

The  [architecture](https://github.com/p2abcengine/p2abcengine/wiki/Architecture) and the specification of the p2abcengine have been done as part of the 
[ABC4Trust](https://www.abc4trust.eu) project.  

The code and documentation available here is by [Alexandra Institute](http://www.alexandra.dk/), [Miracle](https://http://www.miracleas.dk), and [IBM Research-Zurich](http://www.zurich.ibm.com) with support from the [ABC4Trust](https://www.abc4trust.eu), [FI-ware](https://www.fi-ware.eu),  [FutureID](https://www.futureid.eu),
and [PrimeLife](http://www.primelife.eu) projects.


