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
using BouncyCastle;

namespace UProveCrypto.Math.BC
{
    /// <summary>
    /// An element of a group using the elliptic curve construction.
    /// </summary>
    internal class ECGroupElementBCImpl : ECGroupElement
    {
        /// <summary>
        /// A Bouncy Castle FpPoint representing the point.
        /// </summary>
        internal FpPoint Point { get; private set; }

        /// <summary>
        /// Create an ECGroupElementBCImpl object.
        /// </summary>
        /// <param name="point">A Bouncy Castle FpPoint object.</param>
        public ECGroupElementBCImpl(FpPoint point)
        {
            Point = point;
        }

        /// <summary>
        /// Returns <code>this^exponent</code>.
        /// </summary>
        /// <param name="exponent">The exponent.</param>
        /// <returns>A group element.</returns>
        public override GroupElement Exponentiate(FieldZqElement exponent)
        {
            return new ECGroupElementBCImpl(
                Point.Multiply( (exponent as FieldZqElementBCImpl).i) as FpPoint );
        }

        /// <summary>
        /// Returns <code>this*a</code>.
        /// </summary>
        /// <param name="a">The operand.</param>
        /// <returns>A group element.</returns>
        public override GroupElement Multiply(GroupElement a)
        {
            return new ECGroupElementBCImpl(
                Point.Add( (a as ECGroupElementBCImpl).Point) as FpPoint );
        }

        /// <summary>
        /// Returns a value indiciating whether this instance is equal to the
        /// specified object.
        /// </summary>
        /// <param name="o">An object to compare to this instance.</param>
        /// <returns>True if this object equals the other object.</returns>
        public override bool Equals(Object o)
        {
            if (o == null) 
            { 
                return false; 
            }
            
            ECGroupElementBCImpl e = o as ECGroupElementBCImpl;
            if (e == null)
            {
                return false;
            }

            return Point.Equals(e.Point);
        }

        /// <summary>
        /// Returns the hashcode for this instance.
        /// </summary>
        /// <returns>The hashcode for this instance.</returns>
        public override int GetHashCode()
        {
            return Point.GetHashCode();
        }

        /// <summary>
        /// Updates the specified hash function with the group element.
        /// </summary>
        /// <param name="h">An instanciated hash function.</param>
        internal override void UpdateHash(HashFunction h)
        {
            h.Hash(Point.GetEncoded());
        }

        /// <summary>
        /// Returns an encoded group element. The element can be parsed by calling
        /// the corresponding group's <code>CreateGroupElement</code> method.
        /// </summary>
        /// <returns>Encoded group element.</returns>
        public override byte[] GetEncoded()
        {
            return Point.GetEncoded();
        }
    }
}

#endif