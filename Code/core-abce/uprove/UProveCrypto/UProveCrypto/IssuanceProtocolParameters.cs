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
    /// Version numbers for the U-Prove protocol.
    /// </summary>
    public enum ProtocolVersion {
        /// Version 1.1
        V1_1 
    };

    /// <summary>
    /// Contains parameters for an issuance protocol participant (Issuer or Prover).
    /// </summary>
    abstract public class IssuanceProtocolParameters
    {
        /// <summary>
        /// The protocol version.
        /// </summary>
        public ProtocolVersion ProtocolVersion { set; get; }

        private int numberOfTokens = 1;
        /// <summary>
        /// The number of tokens to issue. Must be a positive number.
        /// </summary>
        public int NumberOfTokens { 
            get
            {
                return numberOfTokens;
            }
            set
            {
                if (value <= 0)
                {
                    throw new ArgumentException("NumberOfTokens must be greater than 0");
                }
                numberOfTokens = value;
            }
        }

        /// <summary>
        ///  The token attributes. Either this or the <code>Gamma</code> property
        ///  must be set. If both are set, then the <code>Gamma</code> value takes priority.
        /// </summary>
        public byte[][] Attributes { get; set; }

        /// <summary>
        /// The token gamma value encoding the attribute values. Either this or the 
        /// <code>Attributes</code> property must be set. If both are set, then the
        /// <code>Gamma</code> value takes priority.
        /// </summary>
        public GroupElement Gamma { get; set; }

        /// <summary>
        /// The token information field value. Can be <code>null</code>.
        /// </summary>
        public byte[] TokenInformation { get; set; }

        /// <summary>
        /// The device's public key. Can be <code>null</code>.
        /// </summary>
        public GroupElement DevicePublicKey { get; set; }

        /// <summary>
        /// Validates the parameters object.
        /// </summary>
        public abstract void Validate();
    }
}
