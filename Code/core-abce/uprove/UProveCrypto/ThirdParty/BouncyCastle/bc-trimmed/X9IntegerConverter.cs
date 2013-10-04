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
    public sealed class X9IntegerConverter
    {
		private X9IntegerConverter()
		{
		}

		public static int GetByteLength(
            ECFieldElement fe)
        {
			return (fe.FieldSize + 7) / 8;
        }

		public static int GetByteLength(
			ECCurve c)
		{
			return (c.FieldSize + 7) / 8;
		}

		public static byte[] IntegerToBytes(
			BigInteger	s,
			int			qLength)
		{
			byte[] bytes = s.ToByteArrayUnsigned();

			if (qLength < bytes.Length)
			{
				byte[] tmp = new byte[qLength];
				Array.Copy(bytes, bytes.Length - tmp.Length, tmp, 0, tmp.Length);
				return tmp;
			}
			else if (qLength > bytes.Length)
			{
				byte[] tmp = new byte[qLength];
				Array.Copy(bytes, 0, tmp, tmp.Length - bytes.Length, bytes.Length);
				return tmp;
			}

			return bytes;
		}
    }
}
