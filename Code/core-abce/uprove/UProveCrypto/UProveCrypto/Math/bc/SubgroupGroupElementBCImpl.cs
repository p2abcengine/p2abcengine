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

using BCBigInt = BouncyCastle.BigInteger;

namespace UProveCrypto.Math.BC
{
    /// <summary>
    /// An element of a group using the subgroup construction.
    /// </summary>
    public class SubgroupGroupElementBCImpl : SubgroupGroupElement
    {
        internal BCBigInt i;
        internal BCBigInt p;

        /// <summary>
        /// Constructs a new SubgroupGroupElement.
        /// </summary>
        /// <param name="i">The element value.</param>
        /// <param name="p">The modulus.</param>
        internal SubgroupGroupElementBCImpl(BCBigInt i, BCBigInt p)
        {
            if (i == null || p == null)
            {
                throw new ArgumentNullException();
            }

            this.i = i;
            this.p = p;
        }

        public override GroupElement Exponentiate(FieldZqElement exponent)
        {
            return new SubgroupGroupElementBCImpl(i.ModPow(((FieldZqElementBCImpl)exponent).i, p), p);
        }

        public override GroupElement Multiply(GroupElement a)
        {
            return new SubgroupGroupElementBCImpl(i * ((SubgroupGroupElementBCImpl)a).i % p, p);
        }

        public override bool Equals(Object o)
        {
            if (o == null) { return false; }
            
            SubgroupGroupElementBCImpl e = o as SubgroupGroupElementBCImpl;
            if (e == null)
            {
                return false;
            }

            return this.i == e.i && this.p == e.p;
        }

        public override int GetHashCode()
        {
            return this.i.GetHashCode();
        }

        internal override void UpdateHash(HashFunction h)
        {
            h.Hash(GetEncoded());
        }

        public override byte[] GetEncoded()
        {
            return i.ToByteArrayUnsigned();
        }
    }
}

#endif