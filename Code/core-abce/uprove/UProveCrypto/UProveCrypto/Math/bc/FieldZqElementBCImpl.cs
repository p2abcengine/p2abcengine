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
using System.Runtime.Serialization;

using BCBigInt = BouncyCastle.BigInteger;

namespace UProveCrypto.Math.BC
{
    /// <summary>
    /// Represents an element in a prime field. This implementation uses the 
    /// Bouncy Castle big integer API's.
    /// </summary>
    [DataContract]
    public class FieldZqElementBCImpl : FieldZqElement
    {
        /// <summary>
        /// A Bouncy Castle BigInteger representing the value of this element.
        /// </summary>
        internal BCBigInt i;

        /// <summary>
        /// A FieldZqBCImpl object representing the field to which this element 
        /// belongs.
        /// </summary>
        internal FieldZqBCImpl field;

        /// <summary>
        /// Internal only constructor for a Bouncy Castle based field Zq element.
        /// </summary>
        /// <param name="i">The value of the element.</param>
        /// <param name="field">The field to which the element belongs.</param>
        internal FieldZqElementBCImpl(BCBigInt i, FieldZqBCImpl field)
        {
            this.i = i;
            this.field = field;
        }

        /// <summary>
        /// Returns the inverse of the element with respect to the field.
        /// </summary>
        /// <returns>The inverse of the element with respect to the field.</returns>
        public override FieldZqElement Invert()
        {
            if (i <= BCBigInt.Zero)
            {
                throw new ArgumentOutOfRangeException("i must be greater than 0");
            }

            return new FieldZqElementBCImpl(i.ModInverse(field.modulus), field);
        }

        /// <summary>
        /// Returns the negation of the Zq element, i.e., the additive inverse mod q.
        /// </summary>
        /// <returns>-i mod q</returns>
        public override FieldZqElement Negate()
        {
            return new FieldZqElementBCImpl(i.Negate() % field.modulus, field);
        }

        /// <summary>
        /// Multiply this element by the element e.
        /// </summary>
        /// <param name="e">The value to multiply this value by.</param>
        /// <returns>The value of this * e.</returns>
        public override FieldZqElement Multiply(FieldZqElement e)
        {
            return new FieldZqElementBCImpl(i.Multiply((e as FieldZqElementBCImpl).i) % field.modulus, field);
        }

        /// <summary>
        /// Add the value to this value and return the result.
        /// </summary>
        /// <param name="e">The value to add to this value.</param>
        /// <returns>This value plus e.</returns>
        public override FieldZqElement Add(FieldZqElement e)
        {
            return new FieldZqElementBCImpl(i.Add((e as FieldZqElementBCImpl).i) % field.modulus, field);
        }

        /// <summary>
        /// Exponentiate this value to the exponent power.
        /// </summary>
        /// <param name="exponent">The exponent to raise this value to.</param>
        /// <returns>This value to the exponent power.</returns>
        public override FieldZqElement ModPow(FieldZqElement exponent)
        {
            return new FieldZqElementBCImpl(i.ModPow((exponent as FieldZqElementBCImpl).i, field.modulus), field);
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

            FieldZqElementBCImpl fe = o as FieldZqElementBCImpl;
            if (fe == null)
            {
                return false;
            }

            return this.i.Equals(fe.i) && this.field.modulus.Equals(fe.field.modulus);
        }

        /// <summary>
        /// Returns the hashcode for this instance.
        /// </summary>
        /// <returns>The hashcode for this instance.</returns>
        public override int GetHashCode()
        {
            return this.i.GetHashCode() ^ this.field.modulus.GetHashCode();
        }

        /// <summary>
        /// Returns a string representation of this object.
        /// </summary>
        /// <returns>A string representation of this object.</returns>
        public override string ToString()
        {
            return i.ToString();
        }

        /// <summary>
        /// Converts a FieldElement into a big-endian byte representaion.
        /// </summary>
        /// <returns>A byte array encoding the field element value.</returns>
        public override byte[] ToByteArray()
        {
            return i.ToByteArrayUnsigned();
        }

    }
}

#endif