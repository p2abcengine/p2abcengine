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
using System.Diagnostics;

namespace BouncyCastle
{
	/**
	 * base class for points on elliptic curves.
	 */
	public abstract class ECPoint
	{
		internal readonly ECCurve			curve;
		internal readonly ECFieldElement	x, y;
		internal readonly bool				withCompression;
		internal ECMultiplier				multiplier = null;
		internal PreCompInfo				preCompInfo = null;

		protected internal ECPoint(
			ECCurve			curve,
			ECFieldElement	x,
			ECFieldElement	y,
			bool			withCompression)
		{
			if (curve == null)
				throw new ArgumentNullException("curve");

			this.curve = curve;
			this.x = x;
			this.y = y;
			this.withCompression = withCompression;
		}

		public ECCurve Curve
		{
			get { return curve; }
		}

		public ECFieldElement X
		{
			get { return x; }
		}

		public ECFieldElement Y
		{
			get { return y; }
		}

		public bool IsInfinity
		{
			get { return x == null && y == null; }
		}

		public bool IsCompressed
		{
			get { return withCompression; }
		}

		public override bool Equals(
			object obj)
		{
			if (obj == this)
				return true;

			ECPoint o = obj as ECPoint;

			if (o == null)
				return false;

			if (this.IsInfinity)
				return o.IsInfinity;

			return x.Equals(o.x) && y.Equals(o.y);
		}

		public override int GetHashCode()
		{
			if (this.IsInfinity)
				return 0;

			return x.GetHashCode() ^ y.GetHashCode();
		}

//		/**
//		 * Mainly for testing. Explicitly set the <code>ECMultiplier</code>.
//		 * @param multiplier The <code>ECMultiplier</code> to be used to multiply
//		 * this <code>ECPoint</code>.
//		 */
//		internal void SetECMultiplier(
//			ECMultiplier multiplier)
//		{
//			this.multiplier = multiplier;
//		}

		/**
		 * Sets the <code>PreCompInfo</code>. Used by <code>ECMultiplier</code>s
		 * to save the precomputation for this <code>ECPoint</code> to store the
		 * precomputation result for use by subsequent multiplication.
		 * @param preCompInfo The values precomputed by the
		 * <code>ECMultiplier</code>.
		 */
		internal void SetPreCompInfo(
			PreCompInfo preCompInfo)
		{
			this.preCompInfo = preCompInfo;
		}

		public abstract byte[] GetEncoded();

		public abstract ECPoint Add(ECPoint b);
		public abstract ECPoint Subtract(ECPoint b);
		public abstract ECPoint Negate();
		public abstract ECPoint Twice();
		public abstract ECPoint Multiply(BigInteger b);

		/**
		* Sets the appropriate <code>ECMultiplier</code>, unless already set. 
		*/
		internal virtual void AssertECMultiplier()
		{
			if (this.multiplier == null)
			{
				lock (this)
				{
					if (this.multiplier == null)
					{
						this.multiplier = new FpNafMultiplier();
					}
				}
			}
		}
	}

	public abstract class ECPointBase
		: ECPoint
	{
		protected internal ECPointBase(
			ECCurve			curve,
			ECFieldElement	x,
			ECFieldElement	y,
			bool			withCompression)
			: base(curve, x, y, withCompression)
		{
		}

		protected internal abstract bool YTilde { get; }

		/**
		 * return the field element encoded with point compression. (S 4.3.6)
		 */
		public override byte[] GetEncoded()
		{
			if (this.IsInfinity)
				return new byte[1];

			// Note: some of the tests rely on calculating byte length from the field element
			// (since the test cases use mismatching fields for curve/elements)
			int byteLength = X9IntegerConverter.GetByteLength(x);
			byte[] X = X9IntegerConverter.IntegerToBytes(this.X.ToBigInteger(), byteLength);
			byte[] PO;

			if (withCompression)
			{
				PO = new byte[1 + X.Length];

				PO[0] = (byte)(YTilde ? 0x03 : 0x02);
			}
			else
			{
				byte[] Y = X9IntegerConverter.IntegerToBytes(this.Y.ToBigInteger(), byteLength);
				PO = new byte[1 + X.Length + Y.Length];

				PO[0] = 0x04;

				Y.CopyTo(PO, 1 + X.Length);
			}

			X.CopyTo(PO, 1);

			return PO;
		}

		/**
		 * Multiplies this <code>ECPoint</code> by the given number.
		 * @param k The multiplicator.
		 * @return <code>k * this</code>.
		 */
		public override ECPoint Multiply(
			BigInteger k)
		{
			if (k.SignValue < 0)
				throw new ArgumentException("The multiplicator cannot be negative", "k");

			if (this.IsInfinity)
				return this;

			if (k.SignValue == 0)
				return this.curve.Infinity;

			AssertECMultiplier();
			return this.multiplier.Multiply(this, k, preCompInfo);
		}

        public override string ToString()
        {
            return "x=" + x.ToBigInteger().ToString() + ", y=" + y.ToBigInteger().ToString();
        }
	}

	/**
	 * Elliptic curve points over Fp
	 */
	public class FpPoint
		: ECPointBase
	{
		/**
		 * Create a point which encodes with point compression.
		 *
		 * @param curve the curve to use
		 * @param x affine x co-ordinate
		 * @param y affine y co-ordinate
		 */
		public FpPoint(
			ECCurve			curve,
			ECFieldElement	x,
			ECFieldElement	y)
			: this(curve, x, y, false)
		{
		}

		/**
		 * Create a point that encodes with or without point compresion.
		 *
		 * @param curve the curve to use
		 * @param x affine x co-ordinate
		 * @param y affine y co-ordinate
		 * @param withCompression if true encode with point compression
		 */
		public FpPoint(
			ECCurve			curve,
			ECFieldElement	x,
			ECFieldElement	y,
			bool			withCompression)
			: base(curve, x, y, withCompression)
		{
			if ((x != null && y == null) || (x == null && y != null))
				throw new ArgumentException("Exactly one of the field elements is null");
		}

		protected internal override bool YTilde
		{
			get
			{
				return this.Y.ToBigInteger().TestBit(0);
			}
		}

		// B.3 pg 62
		public override ECPoint Add(
			ECPoint b)
		{
			if (this.IsInfinity)
				return b;

			if (b.IsInfinity)
				return this;

			// Check if b = this or b = -this
			if (this.x.Equals(b.x))
			{
				if (this.y.Equals(b.y))
				{
					// this = b, i.e. this must be doubled
					return this.Twice();
				}

				Debug.Assert(this.y.Equals(b.y.Negate()));

				// this = -b, i.e. the result is the point at infinity
				return this.curve.Infinity;
			}

			ECFieldElement gamma = b.y.Subtract(this.y).Divide(b.x.Subtract(this.x));

			ECFieldElement x3 = gamma.Square().Subtract(this.x).Subtract(b.x);
			ECFieldElement y3 = gamma.Multiply(this.x.Subtract(x3)).Subtract(this.y);

			return new FpPoint(curve, x3, y3);
		}

		// B.3 pg 62
		public override ECPoint Twice()
		{
			// Twice identity element (point at infinity) is identity
			if (this.IsInfinity)
				return this;

			// if y1 == 0, then (x1, y1) == (x1, -y1)
			// and hence this = -this and thus 2(x1, y1) == infinity
			if (this.y.ToBigInteger().SignValue == 0)
				return this.curve.Infinity;

			ECFieldElement TWO = this.curve.FromBigInteger(BigInteger.Two);
			ECFieldElement THREE = this.curve.FromBigInteger(BigInteger.Three);
			ECFieldElement gamma = this.x.Square().Multiply(THREE).Add(curve.a).Divide(y.Multiply(TWO));

			ECFieldElement x3 = gamma.Square().Subtract(this.x.Multiply(TWO));
			ECFieldElement y3 = gamma.Multiply(this.x.Subtract(x3)).Subtract(this.y);

			return new FpPoint(curve, x3, y3, this.withCompression);
		}

		// D.3.2 pg 102 (see Note:)
		public override ECPoint Subtract(
			ECPoint b)
		{
			if (b.IsInfinity)
				return this;

			// Add -b
			return Add(b.Negate());
		}

		public override ECPoint Negate()
		{
			return new FpPoint(this.curve, this.x, this.y.Negate(), this.withCompression);
		}

		/**
		 * Sets the default <code>ECMultiplier</code>, unless already set. 
		 */
		internal override void AssertECMultiplier()
		{
			if (this.multiplier == null)
			{
				lock (this)
				{
					if (this.multiplier == null)
					{
						this.multiplier = new WNafMultiplier();
					}
				}
			}
		}
	}

}
