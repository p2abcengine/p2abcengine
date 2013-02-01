using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Numerics;
using System.Security.Cryptography;

namespace ABC4TrustSmartCard
{
  public static class SmartCardCrypto
  {
    /// <summary>
    /// This is the descrypt method from the ABC4Trust Lite doc appendix B p. 227
    /// It will decrypt the chipher based on the N provided.
    /// Please note that the smartcard return Big-endian byte stream and descrypt expect
    /// little endian data.
    /// </summary>
    /// <param name="keyPair">keys used to decrypt the data</param>
    /// <param name="chipher">data to descrypt</param>
    /// <returns>Byte stream of the descrypted data.</returns>
    public static byte[] Decrypt(KeyPair keyPair, byte[] chipher)
    {
      BigInteger d = ModInverse(new BigInteger(3), keyPair.Phi);
      BigInteger iChiper = new BigInteger(chipher);
      BigInteger plain = BigInteger.ModPow(iChiper, d, keyPair.N);

      byte[] NArray = keyPair.N.ToByteArray();
      byte[] plainBytes = plain.ToByteArray();
      Array.Reverse(plainBytes, 0, plainBytes.Length);
      byte[] plainBytesWithZero = new byte[NArray.Length];
      Buffer.BlockCopy(plainBytes, 0, plainBytesWithZero, plainBytesWithZero.Length - plainBytes.Length, plainBytes.Length);
      
      byte[] pad = new byte[plainBytesWithZero.Length - 32];
      byte[] h = new byte[32];
      Buffer.BlockCopy(plainBytesWithZero, 0, pad, 0, pad.Length);
      Buffer.BlockCopy(plainBytesWithZero, pad.Length, h, 0, 32);

      
      SHA256 shaM = new SHA256Managed();
      byte[] hPrime = shaM.ComputeHash(pad);
      if (!Utils.ByteArrayCompare(hPrime, h))
      {
        Utils.PrintByteArrayToConsole(hPrime);
        Utils.PrintByteArrayToConsole(h);
      }
      shaM.Clear(); //dispose the sha256 object.

      byte[] L = new byte[2];
      Buffer.BlockCopy(pad, 1, L, 0, 2);
      Array.Reverse(L, 0, L.Length);

      int LSize = BitConverter.ToInt16(L, 0);
      byte[] data = new byte[LSize];
      Buffer.BlockCopy(pad, 3, data, 0, data.Length);

      return data;
    }

    /// <summary>
    /// A C# std Numerics do not have a modInverse we impl our own.
    /// The method calculate the modInverse of a and n. This could
    /// be optimized but for our use it will provide for now.
    /// </summary>
    /// <param name="a"></param>
    /// <param name="n"></param>
    /// <returns>The modular inverse of a and n as a BigInteger</returns>
    public static BigInteger ModInverse(BigInteger a, BigInteger n)
    {
      BigInteger i = n;
      BigInteger v = BigInteger.Zero;
      BigInteger d = BigInteger.One;
      while (a > 0)
      {
        BigInteger t = i / a;
        BigInteger x = a;
        a = i % x;
        i = x;
        x = d;
        d = v - t * x;
        v = x;
      }
      v %= n;
      if (v < 0)
      {
        v = (v + n) % n;
      }
      return v;
    }
  }
}
