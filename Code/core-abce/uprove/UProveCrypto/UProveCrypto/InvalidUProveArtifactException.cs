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

namespace UProveCrypto
{
    /// <summary>
    /// This exception is thrown when a U-Prove artifact (an IssuerParameters, a UProveToken, or a PresentationProof)
    /// is invalid.
    /// </summary>
    public class InvalidUProveArtifactException : Exception
    {
        /// <summary>
        /// Constructs a new InvalidUProveArtifactException.
        /// </summary>
        public InvalidUProveArtifactException()
        {
        }

        /// <summary>
        /// Constructs a new InvalidUProveArtifactException.
        /// </summary>
        /// <param name="message">The exception message.</param>
        public InvalidUProveArtifactException(string message) : base(message)
        {
        }
    }
}
