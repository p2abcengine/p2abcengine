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
using System.Collections;

namespace BouncyCastle
{
	/// <remarks>Base class for an elliptic curve.</remarks>
	public abstract class ECCurve
	{
		internal ECFieldElement a, b;

		public abstract int FieldSize { get; }
		public abstract ECFieldElement FromBigInteger(BigInteger x);
		public abstract ECPoint CreatePoint(BigInteger x, BigInteger y, bool withCompression);
		public abstract ECPoint DecodePoint(byte[] encoded);
		public abstract ECPoint Infinity { get; }

		public ECFieldElement A
		{
			get { return a; }
		}

		public ECFieldElement B
		{
			get { return b; }
		}

		public override bool Equals(
			object obj)
		{
			if (obj == this)
				return true;

			ECCurve other = obj as ECCurve;

			if (other == null)
				return false;

			return Equals(other);
		}

		protected bool Equals(
			ECCurve other)
		{
			return a.Equals(other.a) && b.Equals(other.b);
		}

		public override int GetHashCode()
		{
			return a.GetHashCode() ^ b.GetHashCode();
		}
	}

	public abstract class ECCurveBase : ECCurve
	{
		protected internal ECCurveBase()
		{
		}

		protected internal abstract ECPoint DecompressPoint(int yTilde, BigInteger X1);

		/**
		 * Decode a point on this curve from its ASN.1 encoding. The different
		 * encodings are taken account of, including point compression for
		 * <code>F<sub>p</sub></code> (X9.62 s 4.2.1 pg 17).
		 * @return The decoded point.
		 */
		public override ECPoint DecodePoint(
			byte[] encoded)
		{
			ECPoint p = null;
			int expectedLength = (FieldSize + 7) / 8;

			switch (encoded[0])
			{
				case 0x00: // infinity
				{
					if (encoded.Length != 1)
						throw new ArgumentException("Incorrect length for infinity encoding", "encoded");

					p = Infinity;
					break;
				}

				case 0x02: // compressed
				case 0x03: // compressed
				{
					if (encoded.Length != (expectedLength + 1))
						throw new ArgumentException("Incorrect length for compressed encoding", "encoded");

					int yTilde = encoded[0] & 1;
					BigInteger X1 = new BigInteger(1, encoded, 1, encoded.Length - 1);

					p = DecompressPoint(yTilde, X1);
					break;
				}

				case 0x04: // uncompressed
				case 0x06: // hybrid
				case 0x07: // hybrid
				{
					if (encoded.Length != (2 * expectedLength + 1))
						throw new ArgumentException("Incorrect length for uncompressed/hybrid encoding", "encoded");

					BigInteger X1 = new BigInteger(1, encoded, 1, expectedLength);
					BigInteger Y1 = new BigInteger(1, encoded, 1 + expectedLength, expectedLength);

					p = CreatePoint(X1, Y1, false);
					break;
				}

				default:
					throw new FormatException("Invalid point encoding " + encoded[0]);
			}

			return p;
		}
	}

	/**
     * Elliptic curve over Fp
     */
    public class FpCurve : ECCurveBase
    {
        private readonly BigInteger q;
		private readonly FpPoint infinity;

		public FpCurve(BigInteger q, BigInteger a, BigInteger b)
        {
            this.q = q;
            this.a = FromBigInteger(a);
            this.b = FromBigInteger(b);
			this.infinity = new FpPoint(this, null, null);
        }

		public BigInteger Q
        {
			get { return q; }
        }

		public override ECPoint Infinity
		{
			get { return infinity; }
		}

		public override int FieldSize
		{
			get { return q.BitLength; }
		}

		public override ECFieldElement FromBigInteger(BigInteger x)
        {
            return new FpFieldElement(this.q, x);
        }

		public override ECPoint CreatePoint(
			BigInteger	X1,
			BigInteger	Y1,
			bool		withCompression)
		{
			// TODO Validation of X1, Y1?
			return new FpPoint(
				this,
				FromBigInteger(X1),
				FromBigInteger(Y1),
				withCompression);
		}

		protected internal override ECPoint DecompressPoint(
			int			yTilde,
			BigInteger	X1)
		{
			ECFieldElement x = FromBigInteger(X1);
			ECFieldElement alpha = x.Multiply(x.Square().Add(a)).Add(b);
			ECFieldElement beta = alpha.Sqrt();

			//
			// if we can't find a sqrt we haven't got a point on the
			// curve - run!
			//
			if (beta == null)
				throw new ArithmeticException("Invalid point compression");

			BigInteger betaValue = beta.ToBigInteger();
			int bit0 = betaValue.TestBit(0) ? 1 : 0;

			if (bit0 != yTilde)
			{
				// Use the other root
				beta = FromBigInteger(q.Subtract(betaValue));
			}

			return new FpPoint(this, x, beta, true);
		}

		public override bool Equals(
            object obj)
        {
            if (obj == this)
                return true;

			FpCurve other = obj as FpCurve;

			if (other == null)
                return false;

			return Equals(other);
        }

		protected bool Equals(
			FpCurve other)
		{
			return base.Equals(other) && q.Equals(other.q);
		}

		public override int GetHashCode()
        {
            return base.GetHashCode() ^ q.GetHashCode();
        }
    }
}
