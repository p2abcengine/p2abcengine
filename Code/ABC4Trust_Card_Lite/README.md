# [ABC4Trust Card Lite - CryptoExperts](http://www.cryptoexperts.com)

ABC4Trust Card Lite is an implementation of Privacy-ABC systems for ML3-36K-R1 Multos Smartcards. The card is
mono-applicative and dedicated to the device-binding versions of the two specific systems U-Prove and IdentityMixer,
although it might be reused to support other discrete-log-based, device-bound Privacy-ABCs. The card also supports a
number of customized functionalities required by the Patras and Söderhamn pilots in their first version. At the time you
compile, you need to choose the target version, please see the Makefile.

### What's included

You only need the file `main.c` and `main.h` to generate an ALU, which is the file you need to upload on your Multos
smartcard. Compiling this code and uploading the generated ALU requires softwares distributed by MULTOS
(http://www.multos.com).

```
ABC4Trust_Card_Lite/
├── main.c
├── main.h
├── LICENSE
├── MakeFike
├── README.md
```

### How to contact us

https://www.cryptoexperts.com/about

### License

Copyright 2013 CryptoExperts. See the LICENSE file for license rights and limitations (GPLv3).