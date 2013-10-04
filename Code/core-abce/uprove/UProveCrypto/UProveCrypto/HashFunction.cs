//*********************************************************
//
//    Copyright (c) Microsoft. All rights reserved.
//    This code is licensed under the Apache License
//    Version 2.0.
//
//    THIS CODE IS PROVIDED *AS IS* WITHOUT WARRANTY OF
//    ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING ANY
//    IMPLIED WARRANTIES OF FITNESS FOR A PARTICULAR
//    PURPOSE, MERCHANTABILITY, OR NON-INFRINGEMENT.
//
//*********************************************************

using System;
#if NETFX_CORE
using Windows.Security.Cryptography;
using Windows.Security.Cryptography.Core;
#else
using System.Security.Cryptography;
#endif
using UProveCrypto.Math;

namespace UProveCrypto
{
    /// <summary>
    /// A hash function for the U-Prove protocols.
    /// </summary>
    public class HashFunction
    {
        private
#if NETFX_CORE
         CryptographicHash
#else
        HashAlgorithm
#endif // NETFX_CORE
                            hash;
        private byte[]      digest;

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

#if NETFX_CORE
            hash = HashAlgorithmProvider.OpenAlgorithm(hashAlgorithm.Replace("-", "")).CreateHash();
#else // NETFX_CORE
#if SILVERLIGHT
            if (hashAlgorithm.Equals("SHA", StringComparison.OrdinalIgnoreCase) ||
                hashAlgorithm.Equals("SHA1", StringComparison.OrdinalIgnoreCase) ||
                hashAlgorithm.Equals("System.Security.Cryptography.SHA1", StringComparison.OrdinalIgnoreCase) ||
                hashAlgorithm.Equals("http://www.w3.org/2000/09/xmldsig#sha1", StringComparison.OrdinalIgnoreCase))
            {
                hash = new SHA1Managed();
            }
            else if (hashAlgorithm.Equals("SHA256", StringComparison.OrdinalIgnoreCase) ||
                hashAlgorithm.Equals("SHA-256", StringComparison.OrdinalIgnoreCase) ||
                hashAlgorithm.Equals("System.Security.Cryptography.SHA256", StringComparison.OrdinalIgnoreCase) ||
                hashAlgorithm.Equals("http://www.w3.org/2001/04/xmlenc#sha256", StringComparison.OrdinalIgnoreCase))
            {
                hash = new SHA256Managed();
            }
            else 
            {
                hash = null;
            }
#else
            hash = HashAlgorithm.Create(hashAlgorithm);
#endif // SILVERLIGHT
#endif // NETFX_CORE
            if (hash == null)
            {
                throw new ArgumentException("Unsupported hash algorithm: " + hashAlgorithm);
            }
        }

        private void HashInternal(byte[] value)
        {
            if (digest != null)
            {
                // we started to hash some data. If this instance was previously
                // used, let's wipe the cached digest value
                digest = null;
#if !NETFX_CORE
                if (!hash.CanReuseTransform)
                {
                    throw new InvalidOperationException("This hash algorithm cannot be reused.");
                }
                hash.Initialize();
#endif // NETFX_CORE
            }
#if NETFX_CORE
            hash.Append(CryptographicBuffer.CreateFromByteArray(value));
#else

            // DEBUG -- useful for seeing all bytes that get hashed, when debugging two implementations that don't interop
            //System.Diagnostics.Debug.Write("Hashing: ");
            //for (int i = 0; i < value.Length; i++)
            //    System.Diagnostics.Debug.Write(value[i] + ",");
            //System.Diagnostics.Debug.WriteLine("");
            // END DEBUG

            hash.TransformBlock(value, 0, value.Length, value, 0);
#endif // NETFX_CORE
        }

        /// <summary>
        /// Hash a byte.
        /// </summary>
        /// <param name="value">A byte to be hashed.</param>
        public void Hash(byte value)
        {
            HashInternal(new byte[1] {value});
        }

        /// <summary>
        /// Hash an integer.
        /// </summary>
        /// <param name="value">An integer to be hashed.</param>
        public void Hash(int value)
        {
            byte[] buffer = new byte[4];
            buffer[0] = (byte) (value >> 24);
            buffer[1] = (byte) (value >> 16);
            buffer[2] = (byte) (value >> 8);
            buffer[3] = (byte) value;
            HashInternal(buffer);
        }

        /// <summary>
        /// Hash a Group.
        /// </summary>
        /// <param name="value">A group to be hashed.</param>
        public void Hash(Group value)
        {
            value.UpdateHash(this);
        }

        /// <summary>
        /// Hash a FieldZqElement.
        /// </summary>
        /// <param name="value">A field Zq element to be hashed.</param>
        public void Hash(FieldZqElement value)
        {
            if (value == null)
            {
                HashNull();
                return;
            }
            Hash(value.ToByteArray());
        }

        /// <summary>
        /// Hash a GroupElement.
        /// </summary>
        /// <param name="value">A GroupElement to be hashed.</param>
        public void Hash(GroupElement value)
        {
            if (value == null)
            {
                HashNull();
                return;
            }
            value.UpdateHash(this);
        }

        /// <summary>
        /// Hash a byte array.
        /// </summary>
        /// <param name="value">An array of bytes to be hashed.</param>
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

        /// <summary>
        /// Hash a GroupElement array.
        /// </summary>
        /// <param name="values">An array of GroupElements to be hashed.</param>
        public void Hash(GroupElement[] values)
        {
            if (values == null)
            {
                HashNull();
            }
            else
            {
                Hash(values.Length);
                foreach (GroupElement i in values)
                {
                    i.UpdateHash(this);
                }
            }
        }

        /// <summary>
        /// Hash a FieldZqElement array.
        /// </summary>
        /// <param name="values">An array of FieldZqElements to be hashed.</param>
        public void Hash(FieldZqElement[] values)
        {
            if (values == null)
            {
                HashNull();
            }
            else
            {
                Hash(values.Length);
                foreach (FieldZqElement i in values)
                {
                    Hash(i);
                }
            }
        }

        /// <summary>
        /// Hash a series of byte arrays.
        /// </summary>
        /// <param name="values">An array of arrays of bytes to be hashed.</param>
        public void Hash(byte[][] values)
        {
            if (values == null)
            {
                HashNull();
            }
            else
            {
                Hash(values.Length);
                foreach (byte[] b in values)
                {
                    Hash(b);
                }
            }
        }

        /// <summary>
        /// Hash a series of int arrays.
        /// </summary>
        /// <param name="values">An array of integers to be hashed.</param>
        public void Hash(int[] values)
        {
            Hash(values.Length);
            foreach (int i in values)
            {
                Hash(i);
            }
        }

        /// <summary>
        /// Hash the null value.
        /// </summary>
        public void HashNull()
        {
            Hash((int) 0);
        }

        /// <summary>
        /// Hash a byte array without formatting.
        /// </summary>
        public void HashWithoutFormatting(byte[] value)
        {
            HashInternal(value);
        }

        /// <summary>
        /// Finalizes the hash function and returns the digest.
        /// </summary>
        public byte[] Digest
        {
            get {
                if (digest == null)
                {
#if NETFX_CORE
                    CryptographicBuffer.CopyToByteArray(hash.GetValueAndReset(), out digest);
#else 
                    byte[] empty = new byte[0];
                    hash.TransformFinalBlock(empty, 0, 0);
                    digest = hash.Hash;
#endif // NETFX_CORE
                }
                return digest;
            }
        }
    }
}
