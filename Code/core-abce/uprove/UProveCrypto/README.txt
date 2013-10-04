		   U-Prove Crypto SDK V1.1.2 (C# Edition)
		   ======================================

The U-Prove Crypto SDK V1.1 (C# Edition) implements the U-Prove Cryptographic
Specification V1.1 Revision 2 [UPCS]. This SDK was developed by Microsoft to
support experimentation with the foundational features of the U-Prove technology.
It is made available under the Apache 2.0 open-source license, with patent
rights granted under the Open Specification Promise.

For more information about U-Prove, visit http://www.microsoft.com/u-prove.


CONTENTS:
---------

LICENSE.TXT	    - The license and patent grant under which this
                      package is distributed
ThirdParty          - Bouncy Castle library files
UProveCrypto.sln    - Visual Studio 2012 solution file
UProveCrypto/       - SDK project directory
UProveSample/       - Sample project 
UProveUnitTest/     - Unit test project


BUILDING THE SDK:
-----------------

Open the solution file (UProveCrypto.sln) in Visual Studio 2012 and select
"Build Solution" from the "Build" menu.


USING THE UNIT TESTS:
---------------------

In the "Test" menu of Visual Studio 2012, select the "All Tests"
from the "Run" submenu item. Note that a complete test run takes some
time to complete.


USING THE SDK:
--------------

Add the UProveCrypto assembly to the set of References for a project.


REFERENCES:
-----------

[UPCS]    Christian Paquin, Greg Zaverucha. U-Prove Cryptographic Specification V1.1.
          Microsoft Corporation, April 2013. http://www.microsoft.com/u-prove.
