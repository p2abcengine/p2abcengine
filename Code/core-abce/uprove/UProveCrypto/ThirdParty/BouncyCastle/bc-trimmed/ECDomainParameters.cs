//*********************************************************
//
// This file was imported from the C# Bouncy Castle project. Original license header is retained:
//
//
// License
// Copyright (c) 2000 - 2010 The Legion Of The Bouncy Castle (http://www.bouncycastle.org) 
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: 
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software. 
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
//
//*********************************************************

using System;

namespace BouncyCastle
{
    public class ECDomainParameters
    {
        internal ECCurve     curve;
        internal byte[]      seed;
        internal ECPoint     g;
        internal BigInteger  n;
        internal BigInteger  h;

		public ECDomainParameters(
            ECCurve     curve,
            ECPoint     g,
            BigInteger  n)
			: this(curve, g, n, BigInteger.One)
        {
        }

        public ECDomainParameters(
            ECCurve     curve,
            ECPoint     g,
            BigInteger  n,
            BigInteger  h)
			: this(curve, g, n, h, null)
		{
        }

		public ECDomainParameters(
            ECCurve     curve,
            ECPoint     g,
            BigInteger  n,
            BigInteger  h,
            byte[]      seed)
        {
			if (curve == null)
				throw new ArgumentNullException("curve");
			if (g == null)
				throw new ArgumentNullException("g");
			if (n == null)
				throw new ArgumentNullException("n");
			if (h == null)
				throw new ArgumentNullException("h");

			this.curve = curve;
            this.g = g;
            this.n = n;
            this.h = h;
            this.seed = (seed == null ? null : (byte[])seed.Clone());
        }

		public ECCurve Curve
        {
            get { return curve; }
        }

        public ECPoint G
        {
            get { return g; }
        }

        public BigInteger N
        {
            get { return n; }
        }

        public BigInteger H
        {
            get { return h; }
        }

        public byte[] GetSeed()
        {
            return (seed == null ? null : (byte[])seed.Clone());
        }

		public override bool Equals(
			object obj)
        {
			if (obj == this)
				return true;

			ECDomainParameters other = obj as ECDomainParameters;

			if (other == null)
				return false;

			return Equals(other);
        }

		protected bool Equals(
			ECDomainParameters other)
		{
            return curve.Equals(other.curve)
                && g.Equals(other.g)
                && n.Equals(other.n)
                && h.Equals(other.h)
                && true; // TODO: FIXME Arrays.AreEqual(seed, other.seed);
		}

		public override int GetHashCode()
        {
            return curve.GetHashCode()
				^	g.GetHashCode()
				^	n.GetHashCode()
				^	h.GetHashCode()
				
                //TODO: FIXME ^	Arrays.GetHashCode(seed)
                ;
        }
    }

}
