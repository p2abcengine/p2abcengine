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
using UProveCrypto.Math;

using BCBigInt = BouncyCastle.BigInteger;

namespace UProveCrypto.Math.BC
{
    /// <summary>
    /// Represents a group Gq using the elliptic curve construction.
    /// </summary>
    public class ECGroupBCImpl : ECGroup
    {
        /// <summary>
        /// UTF-8 encoder for use with serialization.
        /// </summary>
        static private System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();

        /// <summary>
        /// The HashFunction used to derive group elements.
        /// </summary>
        private HashFunction hash = new HashFunction("SHA-256");

        /// <summary>
        /// The hash function used to derive group elements. Defaults to <code>SHA-256</code>.
        /// </summary>
        public string HashAlg
        {
            set { hash = new HashFunction(value); } 
        }

        /// <summary>
        /// The hash byte size.
        /// </summary>
        private const int hashByteSize = 32;

        /// <summary>
        /// The domain parameters object for the elliptic curve.
        /// </summary>
        private BouncyCastle.ECDomainParameters domainParams;

        /// <summary>
        /// The curve object itself.
        /// </summary>
        private BouncyCastle.FpCurve curve;

        //private ECGroupRepresentation groupRepresentation;
        private GroupElement g;

        /// <summary>
        /// Constructs an ECCGroup.
        /// <param name="p">The p parameter, representing the prime field domain for the 
        /// x and y coordinate spaces.</param>
        /// <param name="a">The a parameter for the eliptic curve.</param>
        /// <param name="b">The b parameter for the eliptic curve.</param>
        /// <param name="g_x">The x coordinate of the generator point.</param>
        /// <param name="g_y">The y coordinate of the generator point.</param>
        /// <param name="n">The order of the group.</param>
        /// <param name="groupName">The known name of the group, or null.</param>
        /// <param name="curveName">The known name of the curve, or null.</param>
        /// </summary>
        public ECGroupBCImpl(
            byte[] p, 
            byte[] a, 
            byte[] b, 
            byte[] g_x, 
            byte[] g_y, 
            byte[] n, 
            string groupName, 
            string curveName)
            : base(p, a, b, g_x, g_y, n, groupName, curveName)
        {
            this.curve = new BouncyCastle.FpCurve(
                new BCBigInt(1, p), 
                new BCBigInt(1, a), 
                new BCBigInt(1, b));

            BouncyCastle.ECPoint generator = this.curve.CreatePoint(
                new BCBigInt(1, g_x), 
                new BCBigInt(1, g_y), 
                false);

            this.domainParams = new BouncyCastle.ECDomainParameters(
                                    this.curve,
                                    generator,
                                    new BCBigInt(1, n));

            this.g = new ECGroupElementBCImpl(
                this.domainParams.G as BouncyCastle.FpPoint);
        }

        /// <summary>
        /// The generator element.
        /// </summary>
        public override GroupElement G
        {
            get
            {
                return g;
            }
            internal set
            {
                if (value == null) throw new ArgumentNullException();
                this.g = (ECGroupElementBCImpl)value;
            }
        }

        /// <summary>
        /// The parameter Q as a big-endian byte array.
        /// </summary>
        public override byte[] Q
        {
            get
            {
                return this.q;
            }
            internal set
            {
                if (value == null) throw new ArgumentNullException();
                this.q = value;
            }
        }

        private void CheckOnCurve(GroupElement e)
        {
            ECGroupElementBCImpl ecge = e as ECGroupElementBCImpl;
            if (ecge == null)
            {
                throw new ArgumentNullException();
            }

            if (!ecge.Point.Y.Square().Equals(ecge.Point.X.Multiply(ecge.Point.X.Square().Add(domainParams.Curve.A)).Add(domainParams.Curve.B)))
            {
                throw new InvalidUProveArtifactException("point is not on curve");
            }

        }

        /// <summary>
        /// Verifies if e is a group element.
        /// </summary>
        /// <param name="e">The element to test.</param>
        /// <exception cref="InvalidUProveArtifactException">Thrown if i is not in the group.</exception>
        public override void ValidateGroupElement(GroupElement e)
        {
            if (domainParams.H != BCBigInt.One)
            {
                throw new NotImplementedException("only curves with a cofactor of 1 are supported");
            }

            CheckOnCurve(e);
        }

        /// <summary>
        /// Returns the group element encoded in byte array.
        /// </summary>
        /// <param name="value">A byte array encoding a group element.</param>
        /// <returns>A group element.</returns>
        public override GroupElement CreateGroupElement(byte[] value)
        {
            return new ECGroupElementBCImpl(domainParams.Curve.DecodePoint(value) as BouncyCastle.FpPoint);
        }

        /// <summary>
        /// Returns the group element encoded as two byte arrays, one for
        /// each coordinate.
        /// </summary>
        /// <param name="x">A byte array encoding the x coordinate.</param>
        /// <param name="y">A byte array encoding the y coordinate.</param>
        /// <returns>A group element.</returns>
        public override GroupElement CreateGroupElement(byte[] x, byte[] y)
        {
            BouncyCastle.FpCurve curve = (BouncyCastle.FpCurve)domainParams.Curve;
            return new ECGroupElementBCImpl(
                new BouncyCastle.FpPoint(
                    domainParams.Curve,
                    new BouncyCastle.FpFieldElement(curve.Q, new BCBigInt(1, x)),
                    new BouncyCastle.FpFieldElement(curve.Q, new BCBigInt(1, y))));
        }

        /// <summary>
        /// Updates the specified hash function with the group description elements.
        /// </summary>
        /// <param name="h">An hash function object.</param>
        internal override void UpdateHash(HashFunction h)
        {
            // desc(Gq) = (p,a,b,g,q,1)
            byte[] cofactorArray = { 0x01 };
            h.Hash(this.p);
            h.Hash(this.a);
            h.Hash(this.b);
            h.Hash(this.G.GetEncoded());
            h.Hash(this.Q);
            h.Hash(cofactorArray);
        }

        /// <summary>
        /// Verifies that the group is correctly constructed.
        /// </summary>
        /// <exception cref="InvalidUProveArtifactException">
        /// Thrown if the group parameters are invalid.</exception>
        public override void Verify()
        {
            if (!ParameterSet.ContainsParameterSet(GroupName))
            {
                throw new ArgumentException("Unsupported prime curve");
            }
        }

        /// <summary>
        /// Returns the identity element in the group.
        /// </summary>
        public override GroupElement Identity
        {
            get { return new ECGroupElementBCImpl(domainParams.Curve.Infinity as BouncyCastle.FpPoint); }
        }

        private BouncyCastle.FpFieldElement GetX(
            byte[] input, 
            BouncyCastle.FpCurve curve, 
            int index, 
            int counter)
        {
            int numIterations = (int)System.Math.Ceiling(
                (double)(curve.Q.BitLength / 8) / (double)hashByteSize);

            byte[] digest = new byte[numIterations * hashByteSize];

            for (int iteration = 0; iteration < numIterations; iteration++)
            {
                byte[] hashInput = ProtocolHelper.Concatenate(
                    input, 
                    encoding.GetBytes(
                        index.ToString() + counter.ToString() + iteration.ToString()));
                hash.HashWithoutFormatting(hashInput);
                Array.Copy(hash.Digest, 0, digest, hashByteSize * iteration, hashByteSize);
            }

            BCBigInt x = new BCBigInt(1, digest).Mod(curve.Q);
            return curve.FromBigInteger(x) as BouncyCastle.FpFieldElement;
        }

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
        public override GroupElement DeriveElement(byte[] context, byte index, out int counter)
        {
            // concatenate context and curve name
            BouncyCastle.FpCurve curve = this.domainParams.Curve as BouncyCastle.FpCurve;
            int count = 0;
            BouncyCastle.ECFieldElement x = null, y = null;
            while (y == null)
            {
                x = GetX(context, curve, index, count);

                BouncyCastle.ECFieldElement alpha = x.Multiply(x.Square().Add(curve.A)).Add(curve.B);
                if (alpha.ToBigInteger() == BCBigInt.Zero)
                {
                    y = alpha;
                }
                else
                {
                    y = alpha.Sqrt(); // returns null if sqrt does not exist
                }
                count++;
            }
            // determine which sqrt to return, i.e., Min(y, -y)
            BouncyCastle.ECFieldElement minusY = y.Negate();
            counter = count - 1;
            return new ECGroupElementBCImpl(new BouncyCastle.FpPoint(curve, x, y.ToBigInteger() < minusY.ToBigInteger() ? y : minusY));
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

            ECGroupBCImpl ecgi = o as ECGroupBCImpl;
            if (ecgi == null)
            {
                return false;
            }

            return this.domainParams.Equals(ecgi.domainParams) && this.curveName == ecgi.curveName && this.GroupName == ecgi.GroupName;
        }

        /// <summary>
        /// Returns the hashcode for this instance.
        /// </summary>
        /// <returns>The hashcode for this instance.</returns>
        public override int GetHashCode()
        {
            return this.curveName.GetHashCode() ^
                   this.domainParams.GetHashCode();
        }
    }
}

#endif