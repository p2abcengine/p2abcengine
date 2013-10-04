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

#if BOUNCY_CASTLE
using UProveCrypto.Math.BC;
#endif

namespace UProveCrypto.Math
{
    /// <summary>
    /// Defines a elliptic-curve group.
    /// </summary>
    public abstract class ECGroup : Group
    {
        /// <summary>
        /// The p parameter, representing the prime field domain for the x 
        /// and y coordinate spaces.
        /// </summary>
        protected byte[] p;
        
        /// <summary>
        /// The a parameter for the eliptic curve.
        /// </summary>
        protected byte[] a;

        /// <summary>
        /// The b parameter for the eliptic curve.
        /// </summary>
        protected byte[] b;
        
        /// <summary>
        /// The order of the curve.
        /// </summary>
        protected byte[] q;
        
        /// <summary>
        /// The known name of the curve, or null.
        /// </summary>
        protected string curveName;

        /// <summary>
        /// Constructs a ECGroup.
        /// <param name="p">The p parameter, representing the prime field domain for the x and y coordinate spaces.</param>
        /// <param name="a">The a parameter for the eliptic curve.</param>
        /// <param name="b">The b parameter for the eliptic curve.</param>
        /// <param name="g_x">The x coordinate of the generator point.</param>
        /// <param name="g_y">The y coordinate of the generator point.</param>
        /// <param name="n">The order of the group.</param>
        /// <param name="groupName">The known name of the group, or null.</param>
        /// <param name="curveName">The known name of the curve, or null.</param>
        /// </summary>
        protected ECGroup(
            byte[] p, 
            byte[] a, 
            byte[] b, 
            byte[] g_x, 
            byte[] g_y, 
            byte[] n, 
            string groupName, 
            string curveName) 
            : base(GroupType.ECC, n, groupName)
        {
            if ((p == null) || 
                (a == null) || 
                (b == null) || 
                (g_x == null) || 
                (g_y == null) || 
                (n == null))
            {
                throw new ArgumentNullException("No null parameters allowed to ECGroup constructor");
            }

            this.p = p;
            this.a = a;
            this.b = b;
            this.curveName = (curveName == null) ? "" : curveName;
        }

        /// <summary>
        /// Creates an ECCGroup.
        /// <param name="p">The p parameter, representing the prime field domain for the x and y coordinate spaces.</param>
        /// <param name="a">The a parameter for the eliptic curve.</param>
        /// <param name="b">The b parameter for the eliptic curve.</param>
        /// <param name="g_x">The x coordinate of the generator point.</param>
        /// <param name="g_y">The y coordinate of the generator point.</param>
        /// <param name="n">The order of the group.</param>
        /// <param name="groupName">The known name of the group, or null.</param>
        /// <param name="curveName">The known name of the curve, or null.</param>
        /// </summary>
        public static ECGroup CreateECGroup(
            byte[] p, 
            byte[] a, 
            byte[] b, 
            byte[] g_x, 
            byte[] g_y, 
            byte[] n, 
            string groupName, 
            string curveName)
        { 
#if BOUNCY_CASTLE
            return new ECGroupBCImpl(p, a, b, g_x, g_y, n, groupName, curveName);
#endif
        }

        /// <summary>
        /// Creates a group element (curve point) from a (x,y) coordinate.
        /// </summary>
        /// <param name="x">The x-coordinate.</param>
        /// <param name="y">The y-coordinate.</param>
        /// <returns></returns>
        public abstract GroupElement CreateGroupElement(byte[] x, byte[] y);

    }
}
