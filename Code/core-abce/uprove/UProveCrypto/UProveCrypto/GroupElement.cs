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
using UProveCrypto.Math;

namespace UProveCrypto
{
    /// <summary>
    /// Represents an element of Gq. 
    /// The group operation uses the multiplication notation.
    /// </summary>
    [DataContract]
    public abstract class GroupElement
    {
        #region Static Methods
        /// <summary>
        /// Returns <code>a*b</code>.
        /// </summary>
        /// <param name="a">First operand.</param>
        /// <param name="b">Second operand.</param>
        /// <returns>A group element.</returns>
        public static GroupElement operator *(GroupElement a, GroupElement b)
        {
            return a.Multiply(b);
        }

        /// <summary>
        /// Returns true if <code>a == b</code>, false otherwise.
        /// </summary>
        /// <param name="a">First operand.</param>
        /// <param name="b">Second operand.</param>
        /// <returns>True if <code>a == b</code>.</returns>
        public static bool operator ==(GroupElement a, GroupElement b)
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
        public static bool operator !=(GroupElement a, GroupElement b)
        {
            return !(a == b);
        }

        #endregion

        /// <summary>
        /// Returns <code>this^exponent</code>.
        /// </summary>
        /// <param name="exponent">The exponent.</param>
        /// <returns>A group element.</returns>
        public abstract GroupElement Exponentiate(FieldZqElement exponent);
        
        /// <summary>
        /// Returns <code>this*a</code>.
        /// </summary>
        /// <param name="a">The operand.</param>
        /// <returns>A group element.</returns>
        public abstract GroupElement Multiply(GroupElement a);

        /// <summary>
        /// Returns a value indiciating whether this instance is equal to the
        /// specified object.
        /// </summary>
        /// <param name="obj">An object to compare to this instance.</param>
        /// <returns>True if this object equals the other object.</returns>
        public override abstract bool Equals(object obj);

        /// <summary>
        /// Returns the hashcode for this instance.
        /// </summary>
        /// <returns>The hashcode for this instance.</returns>
        public override abstract int GetHashCode();

        /// <summary>
        /// Updates the specified hash function with the group element.
        /// </summary>
        /// <param name="h">An instanciated hash function.</param>
        internal abstract void UpdateHash(HashFunction h);

        /// <summary>
        /// Returns an encoded group element. The element can be parsed by calling
        /// the corresponding group's <code>CreateGroupElement</code> method.
        /// </summary>
        /// <returns>Encoded group element.</returns>
        public abstract byte[] GetEncoded();
    }
}
