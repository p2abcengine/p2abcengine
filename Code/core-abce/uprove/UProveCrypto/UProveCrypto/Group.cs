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
using System.Runtime.Serialization;
using UProveCrypto.Math;

namespace UProveCrypto
{
    /// <summary>
    /// Describes a group of order q, denoted Gq.
    /// </summary>
    [DataContract]
    public abstract class Group
    {
        /// <summary>
        /// The type of group.
        /// </summary>
        public GroupType Type { get; private set; }

        /// <summary>
        /// The parameter Q as a big-endian byte array.
        /// </summary>
        public abstract byte[] Q { get; internal set; }

        /// <summary>
        /// The generator element.
        /// </summary>
        public abstract GroupElement G { get; internal set; }

        /// <summary>
        /// The name of the group.
        /// </summary>
        public string GroupName { get; set; }

        /// <summary>
        /// Constructs a Group.
        /// <param name="type">The group construction.</param>
        /// <param name="q">The value q.</param>
        /// <param name="groupName">The group name.</param>
        /// </summary>
        protected Group(GroupType type, byte[] q, string groupName)
        {
            if (q== null) throw new ArgumentNullException("q");

            Type = type;
            Q = q;
            GroupName = groupName;
        }

        /// <summary>
        /// Verifies that e is a group element.
        /// </summary>
        /// <param name="e">The element to test.</param>
        /// <exception cref="InvalidUProveArtifactException">
        /// Thrown if e is not in the group.</exception>
        public abstract void ValidateGroupElement(GroupElement e);

        /// <summary>
        /// Returns the group element encoded in byte array.
        /// </summary>
        /// <param name="value">A byte array encoding a group element.</param>
        /// <returns>A group element.</returns>
        public abstract GroupElement CreateGroupElement(byte[] value);

        /// <summary>
        /// Updates the specified hash function with the group description elements.
        /// </summary>
        /// <param name="h">An hash function object.</param>
        internal abstract void UpdateHash(HashFunction h);

        /// <summary>
        /// Verifies that the group is correctly constructed.
        /// </summary>
        /// <exception cref="InvalidUProveArtifactException">
        /// Thrown if the group parameters are invalid.</exception>
        public abstract void Verify();

        /// <summary>
        /// Returns the identity element in the group.
        /// </summary>
        public abstract GroupElement Identity { get; }

        /// <summary>
        /// Derives an unpredictable element of the group, using the input. Each 
        /// construction defines its own derivation mechanism, but each takes an 
        /// optional context string and an index, and returns a counter. Calling 
        /// this method with the same parameter values returns the same element 
        /// and counter, calling it with a different context or index value must 
        /// return a different element.
        /// </summary>
        /// <param name="context">An optional context used by the derivation 
        /// mechanism, can be null.</param>
        /// <param name="index">An 8-bit integer index value.</param>
        /// <param name="counter">A counter value indicating the state at which the 
        /// derivation mechanism stopped.</param>
        /// <returns>A random group element.</returns>
        public abstract GroupElement DeriveElement(
            byte[] context, 
            byte index, 
            out int counter);

        /// <summary>
        /// Compute a product of powers.
        /// Return the product of the <code>bases[i].Exponentiate(exponents[i])</code> for <c>i</c> from <c>0</c> to <c>bases.Length -1</c>.
        /// The inputs <c>bases</c> and <c>exponents</c> must have the same length
        /// </summary>
        /// <param name="bases">Group elements array.</param>
        /// <param name="exponents">Field elements array.</param>
        /// <returns>Multi-exponentiation of the group elements to the field elements.</returns>
        public abstract GroupElement MultiExponentiate(GroupElement[] bases, FieldZqElement[] exponents);

        /// <summary>
        /// Returns a string describing the underlying math implementation being used. 
        /// This returns the name of the math implementation used to compile UProveCrypto.
        /// </summary>
        public static string MathImplName()
        {
#if BOUNCY_CASTLE
            return "Bouncy Castle";
#endif
        }

    }
}
