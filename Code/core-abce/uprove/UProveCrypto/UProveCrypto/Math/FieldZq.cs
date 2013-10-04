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
#else
using System.Security.Cryptography;
#endif

using System.Runtime.Serialization;

#if BOUNCY_CASTLE
using UProveCrypto.Math.BC;
#endif

namespace UProveCrypto.Math
{
    /// <summary>
    /// Represents the prime field Zq.
    /// </summary>
    [DataContract]
    public abstract class FieldZq
    {
        /// <summary>
        /// The field element zero.
        /// </summary>
        public FieldZqElement Zero { get; protected set; }

        /// <summary>
        /// The field element one.
        /// </summary>
        public FieldZqElement One { get; protected set; }

        /// <summary>
        /// Construct a FieldZqElement with the value given.
        /// </summary>
        /// <param name="value">An integer less than the modulus.</param>
        /// <returns>A new FieldZqElement from this field with the given value.</returns>
        public abstract FieldZqElement GetElement(int value);

        /// <summary>
        /// Construct a FieldZqElement with the value given.
        /// </summary>
        /// <param name="value">A byte array representing the value in big endian order.</param>
        /// <returns>A new FieldZqElement from this field with the given value.</returns>
        public abstract FieldZqElement GetElement(byte[] value);

        /// <summary>
        /// Construct a FieldZqElement array with the values given.
        /// </summary>
        /// <param name="values">An array of arrays of bytes representing the values in big endian order.</param>
        /// <returns>A new FieldZqElement from this field with the given value.</returns>
        public FieldZqElement[] GetElements(byte[][] values)
        {
            FieldZqElement[] elements = new FieldZqElement[values.Length];
            for (int j = 0; j < values.Length; j++)
            {
                elements[j] = GetElement(values[j]);
            }

            return elements;
        }

        /// <summary>
        /// Generates a random Zq element.
        /// </summary>
        /// <param name="nonZero">True to return a non-zero element.</param>
        /// <param name="maxBitLength">Maximum length of the random element, or -1 for full size elements.</param>
        /// <returns>A random Zq element.</returns>
        public abstract FieldZqElement GetRandomElement(bool nonZero, int maxBitLength = -1);

        /// <summary>
        /// Generates an array of random Zq elements.
        /// </summary>
        /// <param name="n">The number of elements to return.</param>
        /// <param name="nonZero">True to return non-zero elements.</param>
        /// <param name="maxBitLength">Maximum length of the random elements, or -1 for full size elements.</param>
        /// <returns>Random Zq elements.</returns>
        public FieldZqElement[] GetRandomElements(int n, bool nonZero, int maxBitLength = -1)
        {
            FieldZqElement[] r = new FieldZqElement[n];
            for (int i = 0; i < n; i++)
            {
                r[i] = GetRandomElement(nonZero, maxBitLength);
            }
            return r;
        }

        /// <summary>
        /// Transforms a hash digest into a Zq element. The digest is interpreted as an unsigned
        /// integer in big-endian byte order modulo q.
        /// </summary>
        /// <param name="digest">A digest value.</param>.
        /// <returns>A Zq element.</returns>
        public abstract FieldZqElement GetElementFromDigest(byte[] digest);

        /// <summary>
        /// Returns true if the given element is an element from this field.
        /// </summary>
        /// <param name="element">The element to check.</param>
        /// <returns>True if the given element is an element from this field.</returns>
        public abstract bool IsElement(FieldZqElement element);

        /// <summary>
        /// Instantiates a new FieldZq object.
        /// </summary>
        /// <param name="modulus">The modulus q.</param>
        /// <returns>A FieldZqObject.</returns>
        public static FieldZq CreateFieldZq(byte[] modulus)
        {
#if BOUNCY_CASTLE
            return new FieldZqBCImpl(modulus);
#endif
        }
    }
}
