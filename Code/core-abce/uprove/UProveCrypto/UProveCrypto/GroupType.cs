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

namespace UProveCrypto
{
    /// <summary>
    /// Defines the supported group constructions: Subgroup and ECC.
    /// </summary>
    public enum GroupType 
    {
        /// <summary>
        /// A group which uses the Subgroup construction.
        /// </summary>
        Subgroup, 

        /// <summary>
        /// A group which uses the Elliptic Curve construction.
        /// </summary>
        ECC 
    };
}