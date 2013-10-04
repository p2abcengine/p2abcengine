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
using System.Diagnostics;

namespace BouncyCastle
{
	public abstract class ECFieldElement
	{
		public abstract BigInteger ToBigInteger();
		public abstract string FieldName { get; }
		public abstract int FieldSize { get; }
		public abstract ECFieldElement Add(ECFieldElement b);
		public abstract ECFieldElement Subtract(ECFieldElement b);
		public abstract ECFieldElement Multiply(ECFieldElement b);
		public abstract ECFieldElement Divide(ECFieldElement b);
		public abstract ECFieldElement Negate();
		public abstract ECFieldElement Square();
		public abstract ECFieldElement Invert();
		public abstract ECFieldElement Sqrt();

		public override bool Equals(
			object obj)
		{
			if (obj == this)
				return true;

			ECFieldElement other = obj as ECFieldElement;

			if (other == null)
				return false;

			return Equals(other);
		}

		protected bool Equals(
			ECFieldElement other)
		{
			return ToBigInteger().Equals(other.ToBigInteger());
		}

		public override int GetHashCode()
		{
			return ToBigInteger().GetHashCode();
		}

		public override string ToString()
		{
			return this.ToBigInteger().ToString(2);
		}
	}

	public class FpFieldElement
		: ECFieldElement
	{
		private readonly BigInteger q, x;

		public FpFieldElement(
			BigInteger	q,
			BigInteger	x)
		{
			if (x.CompareTo(q) >= 0)
				throw new ArgumentException("x value too large in field element");

			this.q = q;
			this.x = x;
		}

		public override BigInteger ToBigInteger()
		{
			return x;
		}

		/**
		 * return the field name for this field.
		 *
		 * @return the string "Fp".
		 */
		public override string FieldName
		{
			get { return "Fp"; }
		}

		public override int FieldSize
		{
			get { return q.BitLength; }
		}

		public BigInteger Q
		{
			get { return q; }
		}

		public override ECFieldElement Add(
			ECFieldElement b)
		{
			return new FpFieldElement(q, x.Add(b.ToBigInteger()).Mod(q));
		}

		public override ECFieldElement Subtract(
			ECFieldElement b)
		{
			return new FpFieldElement(q, x.Subtract(b.ToBigInteger()).Mod(q));
		}

		public override ECFieldElement Multiply(
			ECFieldElement b)
		{
			return new FpFieldElement(q, x.Multiply(b.ToBigInteger()).Mod(q));
		}

		public override ECFieldElement Divide(
			ECFieldElement b)
		{
			return new FpFieldElement(q, x.Multiply(b.ToBigInteger().ModInverse(q)).Mod(q));
		}

		public override ECFieldElement Negate()
		{
			return new FpFieldElement(q, x.Negate().Mod(q));
		}

		public override ECFieldElement Square()
		{
			return new FpFieldElement(q, x.Multiply(x).Mod(q));
		}

		public override ECFieldElement Invert()
		{
			return new FpFieldElement(q, x.ModInverse(q));
		}

		// D.1.4 91
		/**
		 * return a sqrt root - the routine verifies that the calculation
		 * returns the right value - if none exists it returns null.
		 */
		public override ECFieldElement Sqrt()
		{
			if (!q.TestBit(0))
				throw new NotImplementedException("even value of q");

			// p mod 4 == 3
			if (q.TestBit(1))
			{
				// TODO Can this be optimised (inline the Square?)
				// z = g^(u+1) + p, p = 4u + 3
				ECFieldElement z = new FpFieldElement(q, x.ModPow(q.ShiftRight(2).Add(BigInteger.One), q));

				return z.Square().Equals(this) ? z : null;
			}

			// p mod 4 == 1
			BigInteger qMinusOne = q.Subtract(BigInteger.One);

			BigInteger legendreExponent = qMinusOne.ShiftRight(1);
			if (!(x.ModPow(legendreExponent, q).Equals(BigInteger.One)))
				return null;

			BigInteger u = qMinusOne.ShiftRight(2);
			BigInteger k = u.ShiftLeft(1).Add(BigInteger.One);

			BigInteger Q = this.x;
			BigInteger fourQ = Q.ShiftLeft(2).Mod(q);

			BigInteger U, V;
			do
			{
				Random rand = new Random();
				BigInteger P;
				do
				{
					P = new BigInteger(q.BitLength, rand);
				}
				while (P.CompareTo(q) >= 0
					|| !(P.Multiply(P).Subtract(fourQ).ModPow(legendreExponent, q).Equals(qMinusOne)));

				BigInteger[] result = fastLucasSequence(q, P, Q, k);
				U = result[0];
				V = result[1];

				if (V.Multiply(V).Mod(q).Equals(fourQ))
				{
					// Integer division by 2, mod q
					if (V.TestBit(0))
					{
						V = V.Add(q);
					}

					V = V.ShiftRight(1);

					Debug.Assert(V.Multiply(V).Mod(q).Equals(x));

					return new FpFieldElement(q, V);
				}
			}
			while (U.Equals(BigInteger.One) || U.Equals(qMinusOne));

			return null;


//			BigInteger qMinusOne = q.Subtract(BigInteger.One);
//
//			BigInteger legendreExponent = qMinusOne.ShiftRight(1);
//			if (!(x.ModPow(legendreExponent, q).Equals(BigInteger.One)))
//				return null;
//
//			Random rand = new Random();
//			BigInteger fourX = x.ShiftLeft(2);
//
//			BigInteger r;
//			do
//			{
//				r = new BigInteger(q.BitLength, rand);
//			}
//			while (r.CompareTo(q) >= 0
//				|| !(r.Multiply(r).Subtract(fourX).ModPow(legendreExponent, q).Equals(qMinusOne)));
//
//			BigInteger n1 = qMinusOne.ShiftRight(2);
//			BigInteger n2 = n1.Add(BigInteger.One);
//
//			BigInteger wOne = WOne(r, x, q);
//			BigInteger wSum = W(n1, wOne, q).Add(W(n2, wOne, q)).Mod(q);
//			BigInteger twoR = r.ShiftLeft(1);
//
//			BigInteger root = twoR.ModPow(q.Subtract(BigInteger.Two), q)
//				.Multiply(x).Mod(q)
//				.Multiply(wSum).Mod(q);
//
//			return new FpFieldElement(q, root);
		}

//		private static BigInteger W(BigInteger n, BigInteger wOne, BigInteger p)
//		{
//			if (n.Equals(BigInteger.One))
//				return wOne;
//
//			bool isEven = !n.TestBit(0);
//			n = n.ShiftRight(1);
//			if (isEven)
//			{
//				BigInteger w = W(n, wOne, p);
//				return w.Multiply(w).Subtract(BigInteger.Two).Mod(p);
//			}
//			BigInteger w1 = W(n.Add(BigInteger.One), wOne, p);
//			BigInteger w2 = W(n, wOne, p);
//			return w1.Multiply(w2).Subtract(wOne).Mod(p);
//		}
//
//		private BigInteger WOne(BigInteger r, BigInteger x, BigInteger p)
//		{
//			return r.Multiply(r).Multiply(x.ModPow(q.Subtract(BigInteger.Two), q)).Subtract(BigInteger.Two).Mod(p);
//		}

		private static BigInteger[] fastLucasSequence(
			BigInteger	p,
			BigInteger	P,
			BigInteger	Q,
			BigInteger	k)
		{
			// TODO Research and apply "common-multiplicand multiplication here"

			int n = k.BitLength;
			int s = k.GetLowestSetBit();

			Debug.Assert(k.TestBit(s));

			BigInteger Uh = BigInteger.One;
			BigInteger Vl = BigInteger.Two;
			BigInteger Vh = P;
			BigInteger Ql = BigInteger.One;
			BigInteger Qh = BigInteger.One;

			for (int j = n - 1; j >= s + 1; --j)
			{
				Ql = Ql.Multiply(Qh).Mod(p);

				if (k.TestBit(j))
				{
					Qh = Ql.Multiply(Q).Mod(p);
					Uh = Uh.Multiply(Vh).Mod(p);
					Vl = Vh.Multiply(Vl).Subtract(P.Multiply(Ql)).Mod(p);
					Vh = Vh.Multiply(Vh).Subtract(Qh.ShiftLeft(1)).Mod(p);
				}
				else
				{
					Qh = Ql;
					Uh = Uh.Multiply(Vl).Subtract(Ql).Mod(p);
					Vh = Vh.Multiply(Vl).Subtract(P.Multiply(Ql)).Mod(p);
					Vl = Vl.Multiply(Vl).Subtract(Ql.ShiftLeft(1)).Mod(p);
				}
			}

			Ql = Ql.Multiply(Qh).Mod(p);
			Qh = Ql.Multiply(Q).Mod(p);
			Uh = Uh.Multiply(Vl).Subtract(Ql).Mod(p);
			Vl = Vh.Multiply(Vl).Subtract(P.Multiply(Ql)).Mod(p);
			Ql = Ql.Multiply(Qh).Mod(p);

			for (int j = 1; j <= s; ++j)
			{
				Uh = Uh.Multiply(Vl).Mod(p);
				Vl = Vl.Multiply(Vl).Subtract(Ql.ShiftLeft(1)).Mod(p);
				Ql = Ql.Multiply(Ql).Mod(p);
			}

			return new BigInteger[]{ Uh, Vl };
		}

//		private static BigInteger[] verifyLucasSequence(
//			BigInteger	p,
//			BigInteger	P,
//			BigInteger	Q,
//			BigInteger	k)
//		{
//			BigInteger[] actual = fastLucasSequence(p, P, Q, k);
//			BigInteger[] plus1 = fastLucasSequence(p, P, Q, k.Add(BigInteger.One));
//			BigInteger[] plus2 = fastLucasSequence(p, P, Q, k.Add(BigInteger.Two));
//
//			BigInteger[] check = stepLucasSequence(p, P, Q, actual, plus1);
//
//			Debug.Assert(check[0].Equals(plus2[0]));
//			Debug.Assert(check[1].Equals(plus2[1]));
//
//			return actual;
//		}
//
//		private static BigInteger[] stepLucasSequence(
//			BigInteger		p,
//			BigInteger		P,
//			BigInteger		Q,
//			BigInteger[]	backTwo,
//			BigInteger[]	backOne)
//		{
//			return new BigInteger[]
//			{
//				P.Multiply(backOne[0]).Subtract(Q.Multiply(backTwo[0])).Mod(p),
//				P.Multiply(backOne[1]).Subtract(Q.Multiply(backTwo[1])).Mod(p)
//			};
//		}

		public override bool Equals(
			object obj)
		{
			if (obj == this)
				return true;

			FpFieldElement other = obj as FpFieldElement;

			if (other == null)
				return false;

			return Equals(other);
		}

		protected bool Equals(
			FpFieldElement other)
		{
			return q.Equals(other.q) && base.Equals(other);
		}

		public override int GetHashCode()
		{
			return q.GetHashCode() ^ base.GetHashCode();
		}
	}
}
