README notes for the uprove webservices.
This is a list of collected notes that are important to read and understand. The list is not ordered

- java vs c# endianness stuff.
  Java is using big-endian to store multibyte values where c# by the spec is defined to depend on the CPU target. This should include mono
  ATM the uprove code do not check for BitConverter.IsLittleEndian() to figure out what it is running on. For all upcoming abc4trust pilots we can assume that they are all little endian.
  When sending data between the two system that needs to be used as a byte[] the caller must make checks to ensure that data are correct. 
  Otherwise strange errors will happen.
  The ABC4Trust smartcard impl will return byte arrays as big-endian and normaly it will be needed to reverse the byte array. But the UProve BigInteger expect the byte array
  to be in big-endian.


- To create a new wsdl file start the server using windows .net and download the wsdl file. copy it to the java path and update all the java bindings.
  Ensure to update the uprove exe on jenkins as this is done automatic.

- Do note that idemix and uprove do not hash the same way. Take a look at the HashFunction class. This is by spec.


