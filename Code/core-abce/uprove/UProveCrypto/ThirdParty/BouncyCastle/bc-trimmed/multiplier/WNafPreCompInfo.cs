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

namespace BouncyCastle
{
	/**
	* Class holding precomputation data for the WNAF (Window Non-Adjacent Form)
	* algorithm.
	*/
	internal class WNafPreCompInfo
		: PreCompInfo 
	{
		/**
		* Array holding the precomputed <code>ECPoint</code>s used for the Window
		* NAF multiplication in <code>
		* {@link org.bouncycastle.math.ec.multiplier.WNafMultiplier.multiply()
		* WNafMultiplier.multiply()}</code>.
		*/
		private ECPoint[] preComp = null;

		/**
		* Holds an <code>ECPoint</code> representing twice(this). Used for the
		* Window NAF multiplication in <code>
		* {@link org.bouncycastle.math.ec.multiplier.WNafMultiplier.multiply()
		* WNafMultiplier.multiply()}</code>.
		*/
		private ECPoint twiceP = null;

		internal ECPoint[] GetPreComp()
		{
			return preComp;
		}

		internal void SetPreComp(ECPoint[] preComp)
		{
			this.preComp = preComp;
		}

		internal ECPoint GetTwiceP()
		{
			return twiceP;
		}

		internal void SetTwiceP(ECPoint twiceThis)
		{
			this.twiceP = twiceThis;
		}
	}
}
