using System;
using System.Security.Cryptography;

namespace abc4trust_uprove
{
  /// <summary>
  /// A hash function for the U-Prove protocols.
  /// </summary>
  public sealed class HashFunction : IDisposable
  {
    private HashAlgorithm hash;
    private byte[] digest;


    private bool isSha1Managed(string hashAlgorithm)
    {
      return (hashAlgorithm.Equals("1.3.14.3.2.26", StringComparison.OrdinalIgnoreCase) ||
            hashAlgorithm.Equals("SHA", StringComparison.OrdinalIgnoreCase) ||
            hashAlgorithm.Equals("SHA1", StringComparison.OrdinalIgnoreCase) ||
            hashAlgorithm.Equals("System.Security.Cryptography.SHA1", StringComparison.OrdinalIgnoreCase) ||
            hashAlgorithm.Equals("http://www.w3.org/2000/09/xmldsig#sha1", StringComparison.OrdinalIgnoreCase));
    }

    private bool isSha256Managed(string hashAlgorithm)
    {
      return (hashAlgorithm.Equals("2.16.840.1.101.3.4.2.1", StringComparison.OrdinalIgnoreCase) ||
            hashAlgorithm.Equals("SHA256", StringComparison.OrdinalIgnoreCase) ||
            hashAlgorithm.Equals("SHA-256", StringComparison.OrdinalIgnoreCase) ||
            hashAlgorithm.Equals("System.Security.Cryptography.SHA256", StringComparison.OrdinalIgnoreCase) ||
            hashAlgorithm.Equals("http://www.w3.org/2001/04/xmlenc#sha256", StringComparison.OrdinalIgnoreCase));
    }
    /// <summary>
    /// Constructs a HashFunction.
    /// </summary>
    /// <param name="hashAlgorithm">The name of the hash algorithm. Must be one of the value listed in http://msdn.microsoft.com/en-us/library/wet69s13.aspx</param>
    public HashFunction(String hashAlgorithm)
    {

      if (hashAlgorithm == null)
      {
        throw new ArgumentNullException("hashAlgorithm");
      }

      if (isSha1Managed(hashAlgorithm))
      {
        hash = new SHA1Managed();
      }
      else if (isSha256Managed(hashAlgorithm))
      {
        hash = new SHA256Managed();
      }
      else
      {
        hash = null;
      }

      if (hash == null)
      {
        throw new ArgumentException("Unsupported hash algorithm: " + hashAlgorithm);
      }
    }

    public void Dispose()
    {
      hash.Dispose();
    }

    private void HashInternal(byte[] value)
    {
      if (digest != null)
      {
        // we started to hash some data. If this instance was previously
        // used, let's wipe the cached digest value
        digest = null;
        if (!hash.CanReuseTransform)
        {
          throw new InvalidOperationException("This hash algorithm cannot be reused.");
        }
        hash.Initialize();
      }
      hash.TransformBlock(value, 0, value.Length, value, 0);
    }

    public int HashSize
    {
      get { return hash.HashSize; }
    }

    public void Hash(byte b)
    {
      HashInternal(new byte[1] { b });
    }

    public void Hash(int value)
    {
      byte[] buffer = new byte[4];
      buffer[0] = (byte)(value >> 24);
      buffer[1] = (byte)(value >> 16);
      buffer[2] = (byte)(value >> 8);
      buffer[3] = (byte)value;
      HashInternal(buffer);
    }

    public void Hash(byte[] value)
    {
      if (value == null)
      {
        HashNull();
        return;
      }
      Hash(value.Length);
      HashInternal(value);
    }

    public void HashNull()
    {
      Hash((int)0);
    }

    /// <summary>
    /// Finalizes the hash function and returns the digest.
    /// </summary>
    public byte[] Digest
    {
      get
      {
        if (digest == null)
        {
          byte[] empty = new byte[0];
          hash.TransformFinalBlock(empty, 0, 0);
          digest = hash.Hash;
        }
        return digest;
      }
    }
  }
}
