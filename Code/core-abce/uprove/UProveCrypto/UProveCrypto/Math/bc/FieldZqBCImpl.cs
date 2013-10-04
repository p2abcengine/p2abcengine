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

#if BOUNCY_CASTLE

using System;
using System.Collections.Generic;
using System.Security.Cryptography;

using BCBigInt = BouncyCastle.BigInteger;

namespace UProveCrypto.Math.BC
{
    /// <summary>
    /// Represents the field Zq.
    /// </summary>
    internal class FieldZqBCImpl : FieldZq
    {
        /// <summary>
        /// A .Net RNGCSP for generating cryptographic random numbers.
        /// </summary>
        private static RNGCryptoServiceProvider rngCSP = new RNGCryptoServiceProvider();

        /// <summary>
        /// A dictionary of (x, 2^x) values.
        /// </summary>
        private static Dictionary<int, BCBigInt> TwoToTheX = new Dictionary<int, BCBigInt>();
        
        /// <summary>
        /// The value of the modulus in big endian form.
        /// </summary>
        public readonly byte[] modulusBytes;

        /// <summary>
        /// The modulus as a Bouncy Castle BigInteger.
        /// </summary>
        internal BCBigInt modulus;

        /// <summary>
        /// Constructs a FieldZqImpl.
        /// </summary>
        /// <param name="modulus">The field modulus</param>
        public FieldZqBCImpl(byte[] modulus)
        {
            if (modulus == null) throw new ArgumentNullException("modulus");
            this.modulusBytes = modulus;
            this.modulus = new BCBigInt(1, modulus);
            Zero = new FieldZqElementBCImpl(BCBigInt.Zero, this);
            One = new FieldZqElementBCImpl(BCBigInt.One, this);
        }

        /// <summary>
        /// Construct a FieldZqElement with the value given.
        /// </summary>
        /// <param name="value">An integer less than the modulus.</param>
        /// <returns>A new FieldZqElement from this field with the given value.</returns>
        public override FieldZqElement GetElement(int value)
        {
            byte[] bytes = BitConverter.GetBytes(value);
            Array.Reverse(bytes);
            return GetElement(bytes); 
        }

        /// <summary>
        /// Construct a FieldZqElement with the value given.
        /// </summary>
        /// <param name="value">A byte array representing the value in big endian order.</param>
        /// <returns>A new FieldZqElement from this field with the given value.</returns>
        public override FieldZqElement GetElement(byte[] value)
        {
            return new FieldZqElementBCImpl(new BCBigInt(1, value), this);
        }

        /// <summary>
        /// Generates a random Zq element.
        /// </summary>
        /// <param name="nonZero">True to return a non-zero element.</param>
        /// <param name="maxBitLength">Maximum lenght of the random element, or -1 for full size elements.</param>
        /// <returns>A random Zq element.</returns>
        public override FieldZqElement GetRandomElement(bool nonZero, int maxBitLength = -1)
        {
            BCBigInt element = null;
            do 
            {
                BCBigInt max = null;
                if (maxBitLength > 0)
                {
                    if (TwoToTheX.ContainsKey(maxBitLength))
                    {
                        max = TwoToTheX[maxBitLength];
                    }
                    else
                    {
                        max = BCBigInt.Two.Pow(maxBitLength);
                        TwoToTheX.Add(maxBitLength, max);
                    }
                } 
                else
                {
                    max = this.modulus;
                }
                element = GetRandomValue(max);
            } while (nonZero && element == 0);

            return new FieldZqElementBCImpl(element, this);
        }

        /// <summary>
        /// Transforms a hash digest into a Zq element. The digest is interpreted as an unsigned
        /// integer in big-endian byte order modulo q.
        /// </summary>
        /// <param name="digest">A digest value.</param>.
        /// <returns>A Zq element.</returns>
        public override FieldZqElement GetElementFromDigest(byte[] digest)
        {
            return new FieldZqElementBCImpl(new BCBigInt(1, digest) % this.modulus, this);
        }

        /// <summary>
        /// Returns true if the given element is an element from this field.
        /// </summary>
        /// <param name="element">The element to check.</param>
        /// <returns>True if the given element is an element from this field.</returns>
        public override bool IsElement(FieldZqElement element)
        {
            FieldZqElementBCImpl xdImpl = element as FieldZqElementBCImpl;
            if (xdImpl == null)
            {
                throw new ArgumentNullException();
            }

            if (((xdImpl.field) as FieldZqBCImpl).modulus != modulus)
            {
                return false;
            }

            if (xdImpl.i < BCBigInt.Zero || xdImpl.i >= this.modulus)
            {
                return false;
            }

            return true;

        }

        /// <summary>
        /// Returns a random BigInteger x such that 0 &lt;= x &lt; max. 
        /// </summary>
        /// <param name="max">Maximal value (exclusive).</param>
        /// <returns>A random BigInteger.</returns>
        private static BCBigInt GetRandomValue(BCBigInt max)
        {
            if (max <= BCBigInt.Zero)
            {
                throw new ArgumentException("max must be greater than zero");
            }
            
            int length = max.ToByteArray().Length;
            BCBigInt bi;
            do
            {
                // randomly generate an array with a trailing 0 byte
                byte[] randomBytes = new byte[length];
                rngCSP.GetBytes(randomBytes);
                // generate a random positive BigInteger
                bi = new BCBigInt(1, randomBytes);
            } while (
                // Make sure the value is smaller than max to avoid
                // bias in the RNG. This would open up
                // attacks on the system (see Bleichenbacher's attack
                // on FIPS 186's RNG). This technique is equivalent to
                // NIST SP 800-90 (draft)'s "simple discard method"
                // (section B.5.1.1).
                    (bi >= max));

            return bi;
        }
    }
}

#endif