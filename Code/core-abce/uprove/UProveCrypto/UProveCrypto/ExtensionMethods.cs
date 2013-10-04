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

using System;
using UProveCrypto.Math;

namespace UProveCrypto
{
    /// <summary>
    /// Serialization Extension methods to simplify the Base64 encoding that we do all over the place
    /// </summary>  
    public static class ExtensionMethods
    {
        /// <summary>
        /// Convert a byte[] to a base64 representation.
        /// </summary>
        /// <param name="bytes">The value to convert.</param>
        /// <returns>The base64 representation.</returns>
        public static String ToBase64String(this Byte[] bytes)
        {
            if (bytes == null) return null;
            return Convert.ToBase64String(bytes);
        }

        /// <summary>
        /// Convert an int to a base64 representation.
        /// </summary>
        /// <param name="integer">The value to convert.</param>
        /// <returns>The base64 representation.</returns>
        public static String ToBase64String(this int integer)
        {
            return Convert.ToBase64String(BitConverter.GetBytes(integer));
        }

        /// <summary>
        /// Convert a GroupElement to a base64 representation.
        /// </summary>
        /// <param name="groupElement">The value to convert.</param>
        /// <returns>The base64 representation.</returns>
        public static String ToBase64String(this GroupElement groupElement)
        {
            if (groupElement == null) return null;
            return Convert.ToBase64String(groupElement.GetEncoded());
        }

        /// <summary>
        /// Convert a FieldElement to a base64 representation.
        /// </summary>
        /// <param name="fieldElement">The value to convert.</param>
        /// <returns>The base64 representation.</returns>
        public static String ToBase64String(this FieldZqElement fieldElement)
        {
            if (fieldElement == null) return null;
            return Convert.ToBase64String(fieldElement.ToByteArray());
        }

        /// <summary>
        /// Convert a GroupElement[] to a base64 representation.
        /// </summary>
        /// <param name="groupElements">The value to convert.</param>
        /// <returns>The base64 representation.</returns>
        public static string[] ToBase64StringArray(this GroupElement[] groupElements)
        {
            if (groupElements == null) return null;
            string[] encodedElements = new string[groupElements.Length];
            for (int i = 0; i < groupElements.Length; i++)
            {
                encodedElements[i] = groupElements[i].ToBase64String();
            }
            return encodedElements;
        }

        /// <summary>
        /// Convert a fieldElement[] to a base64 representation.
        /// </summary>
        /// <param name="fieldElements">The value to convert.</param>
        /// <returns>The base64 representation.</returns>
        public static string[] ToBase64StringArray(this UProveCrypto.Math.FieldZqElement[] fieldElements)
        {
            if (fieldElements == null) return null;
            string[] encodedFieldElements = new string[fieldElements.Length];
            for (int i = 0; i < fieldElements.Length; i++)
            {
                encodedFieldElements[i] = fieldElements[i].ToBase64String();
            }
            return encodedFieldElements;
        }

        /// <summary>
        /// Convert a base64 string to a byte[].
        /// </summary>
        /// <param name="encodedString">The encoded string to convert.</param>
        /// <returns>The converted object.</returns>
        public static Byte[] ToByteArray(this String encodedString)
        {
            if (encodedString == null) return null;
            return Convert.FromBase64String(encodedString);
        }

        /// <summary>
        /// Convert a base64 string to an int.
        /// </summary>
        /// <param name="encodedString">The encoded string to convert.</param>
        /// <returns>The converted object.</returns>
        public static int ToInt(this String encodedString)
        {
            if (encodedString == null) return 0;
            return BitConverter.ToInt32(Convert.FromBase64String(encodedString), 0);
        }

        /// <summary>
        /// Convert a base64 string to a FieldElement using FieldZq from a particular IssuerParameters object.
        /// </summary>
        /// <param name="encodedString">The encoded string to convert.</param>
        /// <param name="issuerParameters">The IssuerParameters object.</param>
        /// <returns>The converted object.</returns>
        public static FieldZqElement ToFieldElement(this String encodedString, IssuerParameters issuerParameters)
        {
            if (encodedString == null) return null;
            if (issuerParameters == null) throw new ArgumentNullException("issuerParameters");
            return issuerParameters.Zq.GetElement(Convert.FromBase64String(encodedString));
        }

        /// <summary>
        /// Convert a base64 string to a FieldElement.
        /// </summary>
        /// <param name="encodedString">The encoded string to convert.</param>
        /// <param name="Zq">The FieldZq object to which the encoded element belongs.</param>
        /// <returns>The converted object.</returns>
        public static FieldZqElement ToFieldZqElement(this String encodedString, FieldZq Zq)
        {
            if (encodedString == null) return null;
            if (Zq == null) throw new ArgumentNullException("Zq");
            return Zq.GetElement(Convert.FromBase64String(encodedString));
        }

        /// <summary>
        /// Convert a base64 string to a GroupElement using Gq from an IssuerParameters object.
        /// </summary>
        /// <param name="encodedString">The encoded string to convert.</param>
        /// <param name="issuerParameters">The issuer parameters object to use for conversion.</param>
        /// <returns>The converted object.</returns>
        public static GroupElement ToGroupElement(this String encodedString, IssuerParameters issuerParameters)
        {
            if (encodedString == null) return null;
            if (issuerParameters == null) throw new ArgumentNullException("issuerParameters");
            return issuerParameters.Gq.CreateGroupElement(encodedString.ToByteArray());
        }

        /// <summary>
        /// Convert a base64 string to a GroupElement using a specific Group object.
        /// </summary>
        /// <param name="encodedString">The encoded string to convert.</param>
        /// <param name="group">The group object to use.</param>
        /// <returns>The converted object.</returns>
        public static GroupElement ToGroupElement(this String encodedString, Group group)
        {
            if (encodedString == null) return null;
            if (group == null) throw new ArgumentNullException("group");
            return group.CreateGroupElement(encodedString.ToByteArray());
        }

        /// <summary>
        /// Convert a base64 string to a GroupElement[] using a specific Group object.
        /// </summary>
        /// <param name="encodedElements">The encoded string to convert.</param>
        /// <param name="group">The group object to use.</param>
        /// <returns>The converted object.</returns>
        public static GroupElement[] ToGroupElementArray(this String[] encodedElements, Group group)
        {
            if (encodedElements == null) return null;
            if (group == null) throw new ArgumentNullException("group");
            GroupElement[] groupElements = new GroupElement[encodedElements.Length];
            for (int i = 0; i < encodedElements.Length; i++)
            {
                groupElements[i] = encodedElements[i].ToGroupElement(group);
            }
            return groupElements;
        }

        /// <summary>
        /// Convert a base64 string to a FieldElement[] using a specific FieldZq object.
        /// </summary>
        /// <param name="encodedElements">The encoded string to convert.</param>
        /// <param name="Zq">The fieldZq object to use.</param>
        /// <returns>The converted object.</returns>
        public static FieldZqElement[] ToFieldElementArray(this String[] encodedElements, FieldZq Zq)
        {
            if (encodedElements == null) return null;
            FieldZqElement[] fieldElements = new FieldZqElement[encodedElements.Length];
            for (int i = 0; i < encodedElements.Length; i++)
            {
                fieldElements[i] = encodedElements[i].ToFieldZqElement(Zq);
            }
            return fieldElements;
        }
    }
}
