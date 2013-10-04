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
    /// Contains parameters for a presentation protocol participant (Prover or Verifier).
    /// Abstract class to be extended by prover and verifier specific subclasses.
    /// </summary>
    abstract public class PresentationProtocolParameters
    {
        /// <summary>
        /// Gets or sets the Issuer parameters.
        /// </summary>
        public IssuerParameters IP { get; set; }

        /// <summary>
        /// Gets or sets the array of disclosed attribute indices.
        /// </summary>
        public int[] Disclosed { get; set; }

        /// <summary>
        /// Gets or sets the array of committed attribute indices.
        /// </summary>
        public int[] Committed { get; set; }

        /// <summary>
        /// Gets or sets the index of the attribute used to derive a scope-exclusive pseudonym.
        /// If a pseudonym is presented, both <code>PseudonymAttributeIndex</code> and 
        /// <code>PseudonymScope</code> must be set.
        /// </summary>
        public int PseudonymAttributeIndex { get; set; }

        /// <summary>
        /// Gets or sets the scope for the generation of a scope-exclusive pseudonym.
        /// If a pseudonym is presented, both <code>PseudonymAttributeIndex</code> and 
        /// either <code>PseudonymScope</code> or <code>PseudonymScopeElement</code> must be set.
        /// If the same scope will be reused multiple time, it is recommended to compute
        /// and cache the scope element value using 
        /// <see cref="M:ProtocolHelper.GenerateScopeElement"/>
        /// and set it using <see cref="PseudonymScopeElement"/>.
        /// </summary>
        public byte[] PseudonymScope { get; set; }

        private GroupElement pseudonymScopeElement;
        /// <summary>
        /// Gets or sets the scope element for the generation of a scope-exclusive pseudonym.
        /// If a pseudonym is presented, both <code>PseudonymAttributeIndex</code> and 
        /// either <code>PseudonymScope</code> or <code>PseudonymScopeElement</code> must be set.
        /// </summary>
        public GroupElement PseudonymScopeElement
        {
            get
            {
                if (pseudonymScopeElement == null)
                {
                    if (IP != null && PseudonymScope != null)
                    {
                        // compute scope element from scope and save it
                        pseudonymScopeElement = ProtocolHelper.GenerateScopeElement(IP.Gq, PseudonymScope);
                    }
                }
                return pseudonymScopeElement;
            }
            set
            {
                pseudonymScopeElement = value;
            }
        }

        /// <summary>
        /// Gets or sets the presentation message.
        /// </summary>
        public byte[] Message { get; set; }

        /// <summary>
        /// Gets or sets the Device message, if a Device-protected token is presented.
        /// </summary>
        public byte[] DeviceMessage { get; set; }

        /// <summary>
        /// Validates the parameters object.
        /// </summary>
        public void Validate()
        {
            if (PseudonymScope != null && PseudonymScopeElement != null)
            {
                throw new InvalidUProveArtifactException("PseudonymScope and PseudonymScopeElement cannot both be set");
            }

        }
    }

    /// <summary>
    /// Presentation protocol parameters for the Prover.
    /// </summary>
    public class ProverPresentationProtocolParameters : PresentationProtocolParameters
    {
        /// <summary>
        /// Constructs a <code>ProverPresentationProtocolParameters</code> instance.
        /// </summary>
        public ProverPresentationProtocolParameters()
        {
        }

        /// <summary>
        /// Constructs a <code>ProverPresentationProtocolParameters</code> instance.
        /// </summary>
        /// <param name="ip">The issuer parameters.</param>
        /// <param name="disclosed">Disclosed attribute indices.</param>
        /// <param name="message">Presentation message.</param>
        /// <param name="keyAndToken">Presented key and token.</param>
        /// <param name="attributes">Token attributes.</param>
        public ProverPresentationProtocolParameters(IssuerParameters ip, int[] disclosed, byte[] message, UProveKeyAndToken keyAndToken, byte[][] attributes)
        {
            IP = ip;
            Disclosed = disclosed;
            Message = message;
            KeyAndToken = keyAndToken;
            Attributes = attributes;
        }

        /// <summary>
        /// Gets or sets the Device context, if a Device-protected token is presented.
        /// </summary>
        public IDevicePresentationContext DeviceContext { get; set; }

        /// <summary>
        /// Gets or sets the presented U-Prove key and token.
        /// </summary>
        public UProveKeyAndToken KeyAndToken { get; set; }

        /// <summary>
        /// Gets or sets the token attributes.
        /// </summary>        
        public byte[][] Attributes { get; set; }

        /// <summary>
        /// Gets or sets the pregenerated random data.
        /// </summary>        
        public ProofGenerationRandomData RandomData { get; set; }

        /// <summary>
        /// Sets the device presentation data.
        /// </summary>
        /// <param name="deviceMessage">The Device message.</param>
        /// <param name="deviceContext">The Device context.</param>
        public void SetDeviceData(byte[] deviceMessage, IDevicePresentationContext deviceContext)
        {
            DeviceMessage = deviceMessage;
            DeviceContext = deviceContext;
        }
    }

    /// <summary>
    /// Presentation protocol parameters for the Verifier.
    /// </summary>
    public class VerifierPresentationProtocolParameters : PresentationProtocolParameters
    {
        /// <summary>
        /// Constructs a <code>VerifierPresentationProtocolParameters</code> instance.
        /// </summary>
        public VerifierPresentationProtocolParameters()
        {
        }

        /// <summary>
        /// Constructs a <code>VerifierPresentationProtocolParameters</code> instance.
        /// </summary>
        /// <param name="ip">The issuer parameters.</param>
        /// <param name="disclosed">Disclosed attribute indices.</param>
        /// <param name="message">Presentation message.</param>
        /// <param name="token">Presented token.</param>
        public VerifierPresentationProtocolParameters(IssuerParameters ip, int[] disclosed, byte[] message, UProveToken token)
        {
            IP = ip;
            Disclosed = disclosed;
            Message = message;
            Token = token;
        }

        /// <summary>
        /// Gets or sets the presented U-Prove token.
        /// </summary>
        public UProveToken Token { get; set; }

    }
}
