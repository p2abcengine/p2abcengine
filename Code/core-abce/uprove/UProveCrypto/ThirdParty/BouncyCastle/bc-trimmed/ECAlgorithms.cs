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
	public class ECAlgorithms
	{
		public static ECPoint SumOfTwoMultiplies(ECPoint P, BigInteger a,
			ECPoint Q, BigInteger b)
		{
			ECCurve c = P.Curve;
			if (!c.Equals(Q.Curve))
				throw new ArgumentException("P and Q must be on same curve");

/*
            // Point multiplication for Koblitz curves (using WTNAF) beats Shamir's trick
			if (c is F2mCurve)
			{
				F2mCurve f2mCurve = (F2mCurve) c;
				if (f2mCurve.IsKoblitz)
				{
					return P.Multiply(a).Add(Q.Multiply(b));
				}
			}
*/
			return ImplShamirsTrick(P, a, Q, b);
		}

		/*
		* "Shamir's Trick", originally due to E. G. Straus
		* (Addition chains of vectors. American Mathematical Monthly,
		* 71(7):806-808, Aug./Sept. 1964)
		*  
		* Input: The points P, Q, scalar k = (km?, ... , k1, k0)
		* and scalar l = (lm?, ... , l1, l0).
		* Output: R = k * P + l * Q.
		* 1: Z <- P + Q
		* 2: R <- O
		* 3: for i from m-1 down to 0 do
		* 4:        R <- R + R        {point doubling}
		* 5:        if (ki = 1) and (li = 0) then R <- R + P end if
		* 6:        if (ki = 0) and (li = 1) then R <- R + Q end if
		* 7:        if (ki = 1) and (li = 1) then R <- R + Z end if
		* 8: end for
		* 9: return R
		*/
		public static ECPoint ShamirsTrick(
			ECPoint		P,
			BigInteger	k,
			ECPoint		Q,
			BigInteger	l)
		{
			if (!P.Curve.Equals(Q.Curve))
				throw new ArgumentException("P and Q must be on same curve");

			return ImplShamirsTrick(P, k, Q, l);
		}

		private static ECPoint ImplShamirsTrick(ECPoint P, BigInteger k,
			ECPoint Q, BigInteger l)
		{
			int m = System.Math.Max(k.BitLength, l.BitLength);
			ECPoint Z = P.Add(Q);
			ECPoint R = P.Curve.Infinity;

			for (int i = m - 1; i >= 0; --i)
			{
				R = R.Twice();

				if (k.TestBit(i))
				{
					if (l.TestBit(i))
					{
						R = R.Add(Z);
					}
					else
					{
						R = R.Add(P);
					}
				}
				else
				{
					if (l.TestBit(i))
					{
						R = R.Add(Q);
					}
				}
			}

			return R;
		}
	}
}
