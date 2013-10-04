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

using System.Runtime.Serialization;

namespace UProveCrypto.Math
{
    /// <summary>
    /// Abstract type representing an element in a prime field.
    /// </summary>
    [DataContract]
    public abstract class FieldZqElement
    {
        #region Static methods
        /// <summary>
        /// Returns <code>a+b</code>.
        /// </summary>
        /// <param name="a">First operand.</param>
        /// <param name="b">Second operand.</param>
        /// <returns>A field element.</returns>
        public static FieldZqElement operator +(FieldZqElement a, FieldZqElement b)
        {
            return a.Add(b);
        }

        /// <summary>
        /// Returns <code>a*b</code>.
        /// </summary>
        /// <param name="a">First operand.</param>
        /// <param name="b">Second operand.</param>
        /// <returns>A field element.</returns>
        public static FieldZqElement operator *(FieldZqElement a, FieldZqElement b)
        {
            return a.Multiply(b);
        }

        /// <summary>
        /// Returns true if <code>a == b</code>, false otherwise.
        /// </summary>
        /// <param name="a">First operand.</param>
        /// <param name="b">Second operand.</param>
        /// <returns>True if <code>a == b</code>.</returns>
        public static bool operator ==(FieldZqElement a, FieldZqElement b)
        {
            if ((object)a == null)
            {
                return ((object)b == null);
            }

            return a.Equals(b);
        }

        /// <summary>
        /// Returns true if <code>a != b</code>, false otherwise.
        /// </summary>
        /// <param name="a">First operand.</param>
        /// <param name="b">Second operand.</param>
        /// <returns>True if <code>a != b</code>.</returns>
        public static bool operator !=(FieldZqElement a, FieldZqElement b)
        {
            return !(a == b);
        }
        #endregion

        /// <summary>
        /// Returns the inverse of the element with respect to the field.
        /// </summary>
        /// <returns>The inverse of the element with respect to the field.</returns>
        public abstract FieldZqElement Invert();

        /// <summary>
        /// Negate this elemnt mod q.
        /// </summary>
        /// <returns>-this mod q</returns>
        public abstract FieldZqElement Negate();

        /// <summary>
        /// Multiply this element by the element e.
        /// </summary>
        /// <param name="e">The value to multiply this value by.</param>
        /// <returns>The value of this * e.</returns>
        public abstract FieldZqElement Multiply(FieldZqElement e);

        /// <summary>
        /// Add the value to this value and return the result.
        /// </summary>
        /// <param name="e">The value to add to this value.</param>
        /// <returns>This value plus e.</returns>
        public abstract FieldZqElement Add(FieldZqElement e);

        /// <summary>
        /// Exponentiate this value to the exponent power.
        /// </summary>
        /// <param name="exponent">The exponent to raise this value to.</param>
        /// <returns>This value to the exponent power.</returns>
        public abstract FieldZqElement ModPow(FieldZqElement exponent);

        /// <summary>
        /// Converts a FieldElement into a big-endian byte representaion.
        /// </summary>
        /// <returns>A byte array encoding the field element value.</returns>
        public abstract byte[] ToByteArray();

        /// <summary>
        /// Returns the hashcode for this instance.
        /// </summary>
        /// <returns>The hashcode for this instance.</returns>
        public override abstract int GetHashCode();

        /// <summary>
        /// Returns a value indiciating whether this instance is equal to the
        /// specified object.
        /// </summary>
        /// <param name="o">An object to compare to this instance.</param>
        /// <returns>True if this object equals the other object.</returns>
        public override abstract bool Equals(object o);

    }
}
