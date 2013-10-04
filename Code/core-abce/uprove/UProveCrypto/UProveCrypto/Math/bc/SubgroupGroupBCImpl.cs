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
using System.Linq;

using BCBigInt = BouncyCastle.BigInteger;

namespace UProveCrypto.Math.BC
{
    /// <summary>
    /// Represents a group Gq using the subgroup construction.
    /// This implementation uses the Bouncy Castle library.
    /// </summary>
    public class SubgroupGroupBCImpl : SubgroupGroup
    {
        /// <summary>
        /// The value of p as a big-endian byte array.
        /// </summary>
        private byte[] pBytes;

        /// <summary>
        /// The value of p as a Bouncy Castle BigInteger.
        /// </summary>
        private BCBigInt pValue;

        /// <summary>
        /// The value of q as a big-endian byte array.
        /// </summary>
        private byte[] qBytes;

        /// <summary>
        /// The value of q as a Bouncy Castle BigInteger.
        /// </summary>
        private BCBigInt qValue;
        
        /// <summary>
        /// The value of the generator g as a GroupElement.
        /// </summary>
        private GroupElement gElement;

        /// <summary>
        /// Constructs a SubgroupGroup.
        /// </summary>
        /// <param name="p">The value p.</param>
        /// <param name="q">The value q.</param>
        /// <param name="g">The value g.</param>
        /// <param name="groupName">The known name of the group, or null.</param>
        /// <param name="domainParameterSeed">The domain parameter seed if the NIST generation 
        /// is used, or null.</param>
        internal SubgroupGroupBCImpl(
            byte[] p, 
            byte[] q, 
            byte[] g, 
            string groupName, 
            byte[] domainParameterSeed) : 
            base(q, groupName, domainParameterSeed)
        {
            P = p;
            this.gElement = CreateGroupElement(g);
        }

        /// <summary>
        /// Gets or sets the value p.
        /// </summary>
        public override byte[] P
        {
            get 
            { 
                return pBytes; 
            }
            internal set
            {
                if (value == null)
                {
                    throw new ArgumentNullException();
                }
                pBytes = value;
                pValue = new BCBigInt(1, pBytes);
            }
        }

        /// <summary>
        /// Gets or sets the value q.
        /// </summary>
        public override byte[] Q
        {
            get
            {
                return qBytes;
            }
            internal set
            {
                if (value == null)
                {
                    throw new ArgumentNullException();
                }
                qBytes = value;
                qValue = new BCBigInt(1, qBytes);
            }
        }

        /// <summary>
        /// The subgroup generator G.
        /// </summary>
        public override GroupElement G
        {
            get
            {
                return this.gElement;
            }
            internal set
            {
                if (value == null) { throw new ArgumentNullException(); }
                this.gElement = (SubgroupGroupElementBCImpl)value;
            }
        }

        /// <summary>
        /// Verifies that e is an element of the group.
        /// </summary>
        /// <param name="e">The element to test.</param>
        /// <exception cref="InvalidUProveArtifactException">
        /// Thrown if i is not in the group.</exception>
        public override void ValidateGroupElement(GroupElement e)
        {
            SubgroupGroupElementBCImpl sge = e as SubgroupGroupElementBCImpl;
            if (sge == null)
            {
                throw new InvalidUProveArtifactException(
                    "Invalid group element (wrong construction)");
            }

            // verify that 1 < g < p
            if (sge.i <= BCBigInt.One || sge.i >= pValue)
            {
                throw new InvalidUProveArtifactException(
                    "Invalid group element (out of range)");
            }

            // verify that g^q mod p = 1
            BCBigInt modpow = sge.i.ModPow(qValue, pValue);
            if (sge.i.ModPow(qValue, pValue) != BCBigInt.One)
            {
                throw new InvalidUProveArtifactException(
                    "Invalid group element (i^Q mod P != 1)");
            }
        }

        /// <summary>
        /// Returns the group element encoded in byte array.
        /// </summary>
        /// <param name="value">A byte array encoding a group element.</param>
        /// <returns>A group element.</returns>
        public override GroupElement CreateGroupElement(byte[] value)
        {
            return new SubgroupGroupElementBCImpl(new BCBigInt(1, value), pValue);
        }

        /// <summary>
        /// Verifies that the group is correctly constructed.
        /// </summary>
        /// <exception cref="InvalidUProveArtifactException">
        /// Thrown if the group parameters are invalid.</exception>
        public override void Verify()
        {
            // verify that p is an odd prime
            if (pValue <= BCBigInt.Two || 
                !pValue.IsProbablePrime(ProtocolHelper.PrimalityTestingCertainty))
            {
                throw new InvalidUProveArtifactException(
                    "Invalid group: P is not an odd prime");
            }

            // verify that q is an odd prime
            if (qValue <= BCBigInt.Two || 
                !qValue.IsProbablePrime(ProtocolHelper.PrimalityTestingCertainty))
            {
                throw new InvalidUProveArtifactException(
                    "Invalid group: Q is not an odd prime");
            }

            // verify that q divides p - 1
            BCBigInt remainder;
            BCBigInt.DivRem(pValue - BCBigInt.One, qValue, out remainder);
            if (remainder != BCBigInt.Zero)
            {
                throw new InvalidUProveArtifactException(
                    "Invalid group: Q does not divide P-1)");
            }

            // verify that g is a group element
            try
            {
                ValidateGroupElement(G);
            }
            catch (InvalidUProveArtifactException)
            {
                throw new InvalidUProveArtifactException(
                    "Invalid group: G is not a group element");
            }
        }

        /// <summary>
        /// Returns the identity element in the group.
        /// </summary>
        public override GroupElement Identity
        {
            get
            {
                return new SubgroupGroupElementBCImpl(BCBigInt.One, pValue);
            }
        }

        /// <summary>
        /// Updates the specified hash function with the group description elements.
        /// </summary>
        /// <param name="h">An instanciated hash function.</param>
        internal override void UpdateHash(HashFunction h)
        {
            h.Hash(P);
            h.Hash(Q);
            h.Hash(G);
        }

        /// <summary>
        /// This method implements the method defined in recommended parameters spec
        /// </summary>
        public override GroupElement DeriveElement(
            byte[] context, 
            byte index, 
            out int counter)
        {
            byte[] ggen = new byte[] { (byte)0x67, (byte)0x67, (byte)0x65, (byte)0x6E };

            string hashAlg = null;
            int bitlength = qValue.BitLength;
            if (bitlength >= 512)
            {
                hashAlg = "SHA-512";
            }
            else if (bitlength >= 256)
            {
                hashAlg = "SHA-256";
            }
            else if (bitlength >= 160)
            {
                hashAlg = "SHA1";
            }
            else
            {
                throw new ArgumentException("q is too small");
            }
            HashFunction hash = new HashFunction(hashAlg);

            // references to "step x" in comments refer to alg from apendix A.2.3 of FIPS 186-3
            // int N = this.q.BitLength; // step 2 (usused)
            BCBigInt e = (pValue - BCBigInt.One) / qValue; // step 3
            
            // prepare U array =  context || "ggen" || index || count
            byte[] contextBytes = (context == null ? new byte[] {} : context);
            int contextLength = (contextBytes == null ? 0 : contextBytes.Length);
            byte[] U = new byte[contextLength + 
                ggen.Length + 2];
            int arrayIndex = 0;
            if (contextLength > 0)
            {
                Array.Copy(contextBytes, 0, U, arrayIndex, contextLength);
                arrayIndex += contextLength;
            }
            Array.Copy(ggen, 0, U, arrayIndex, ggen.Length);
            U[U.Length - 2] = index;

            byte count = 0; // step 4
            BCBigInt g = BCBigInt.Zero;
            while (g < BCBigInt.Two) // step 10
            {
                if (count == 255)
                {
                    throw new InvalidUProveArtifactException(
                        "can't derive an element; count exceeded");
                }
                count++; // step 5
                // complete U array
                U[U.Length - 1] = count; // step 7
                hash.HashWithoutFormatting(U);

                // BUGBUG: is that ok, will that wrap correctly?
                BCBigInt W = new BCBigInt(1, hash.Digest); // step 8 
                g = W.ModPow(e, pValue); // step 9
            }

            counter = count;
            return new SubgroupGroupElementBCImpl(g, pValue);
        }

        /// <summary>
        /// Bouncy castle implementation of multi-exponentiation.
        /// </summary>
        /// <param name="g">bases</param>
        /// <param name="f">exponents</param>
        /// <returns></returns>
        public override GroupElement MultiExponentiate(GroupElement[] g, FieldZqElement[] f)
        {
            if (g == null || f == null || g.Length != f.Length)
            {
                throw new ArgumentException("g and f must be non-null and of the same length");
            }
            GroupElement value = Identity;
            for (int i = 0; i < g.Length; i++)
            {
                value *= g[i].Exponentiate(f[i]);
            }
            return value;
        }

        /// <summary>
        /// Returns a value indiciating whether this instance is equal to the
        /// specified object.
        /// </summary>
        /// <param name="o">An object to compare to this instance.</param>
        /// <returns>True if this object equals the other object.</returns>
        public override bool Equals(object o)
        {
            if (Object.ReferenceEquals(o, this))
            {
                return true;
            }

            SubgroupGroupBCImpl sggi = o as SubgroupGroupBCImpl;
            if (sggi == null)
            {
                return false;
            }

            if (!this.P.SequenceEqual(sggi.P)) return false;
            if (!this.Q.SequenceEqual(sggi.Q)) return false;
            if (!this.G.Equals(sggi.G)) return false;

            // check for null 
            if (this.DomainParameterSeed != null)
            {
                if (sggi.DomainParameterSeed == null) return false;

                // okay do a full comparison
                if (!this.DomainParameterSeed.SequenceEqual(sggi.DomainParameterSeed)) 
                    return false;
            }
            else
            {
                if (sggi.DomainParameterSeed != null) return false;
            }

            // check for null
            if (this.GroupName != null)
            {
                if (sggi.GroupName == null) return false;
                // okay do a full comparison
                if (!this.GroupName.Equals(sggi.GroupName)) return false;
            }
            else
            {
                if (sggi.GroupName != null) return false;
            }

            return true;
        }

        /// <summary>
        /// Returns the hashcode for this instance.
        /// </summary>
        /// <returns>The hashcode for this instance.</returns>
        public override int GetHashCode()
        {
            return pValue.GetHashCode() ^ qValue.GetHashCode();
        }
    }
}

#endif