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
using System.ComponentModel;
using System.Runtime.Serialization;
using UProveCrypto.Math;

namespace UProveCrypto
{
    /// <summary>
    /// Issuance protocol parameters for the Issuer.
    /// </summary>
    public class IssuerProtocolParameters : IssuanceProtocolParameters
    {
        /// <summary>
        /// Constructs new issuance protocol parameters for the Issuer.
        /// </summary>
        /// <param name="ikap">The Issuer key and parameters.</param>
        public IssuerProtocolParameters(IssuerKeyAndParameters ikap)
        {
            if (ikap == null || ikap.PrivateKey == null || ikap.IssuerParameters == null)
            {
                throw new ArgumentNullException("IssuerKeyAndParameters is malformed or null");
            } 
            
            IssuerKeyAndParameters = ikap;
        }

        /// <summary>
        /// The Issuer key and parameters.
        /// </summary>
        public IssuerKeyAndParameters IssuerKeyAndParameters { get; private set; }
        
        /// <summary>
        /// The pregenerated issuer randomizers. Can be <code>null</code>.
        /// </summary>
        public FieldZqElement[] PreGeneratedW { get; set; }

        /// <summary>
        /// Validates the issuance parameters.
        /// </summary>
        public override void Validate()
        {
            if (PreGeneratedW != null && PreGeneratedW.Length != NumberOfTokens)
            {
                throw new ArgumentException("invalid PreGeneratedW array length");
            }
        }

        /// <summary>
        /// Creates an Issuer instance.
        /// </summary>
        /// <returns>An Issuer instance.</returns>
        public Issuer CreateIssuer()
        {
            Validate();
            if (Gamma == null)
            {
                Gamma = ProtocolHelper.ComputeIssuanceInput(IssuerKeyAndParameters.IssuerParameters, Attributes, TokenInformation, DevicePublicKey);
            }
            return new Issuer(IssuerKeyAndParameters, NumberOfTokens, Gamma, PreGeneratedW);
        }
    }

    /// <summary>
    /// Issuance state after the first message generation. This
    /// object can be used to re-instanciate an <code>Issuer</code> instance
    /// that can complete the token issuance.
    /// </summary>
    [DataContract]
    public class PostFirstMessageState
    {
        /// <summary>
        /// The issuance randomizers.
        /// </summary>
        public FieldZqElement[] W { get; set; }

        /// <summary>
        /// Constructs a <code>PostFirstMessageState</code> instance.
        /// </summary>
        /// <param name="w">The issuance randomizers.</param>
        public PostFirstMessageState(FieldZqElement[] w)
        {
            if (w == null)
            {
                throw new ArgumentNullException("w");
            }
            W = new FieldZqElement[w.Length];
            Array.Copy(w, W, w.Length);
        }

        #region Serialization

        [DataMember(Name = "W", Order = 1)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string[] _w;

        [OnSerializing]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnSerializing(StreamingContext context)
        {
            _w = new string[this.W.Length];
            for (int i = 0; i < this.W.Length; i++)
            {
                _w[i] = this.W[i].ToBase64String();
            }
        }

        [OnDeserialized]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnDeserialized(StreamingContext context)
        {
            if (_w == null)
            {
                throw new UProveSerializationException("W");
            }

            this.W = new FieldZqElement[_w.Length];
            for (int i = 0; i < _w.Length; i++)
            {
                this.W[i] = _w[i].ToFieldZqElement(Serializer.ip.Zq);
            }
        }

        #endregion Serialization
    }

    /// <summary>
    /// Implements the <code>Issuer</code> side of the U-Prove issuance protocol. One instance must be created for
    /// each run of the issuance protocol, in which many U-Prove tokens can be obtained in parallel.
    /// </summary>
    public class Issuer
    {
        enum State { Initialized, First, Third };
        private State state;
        private int numberOfTokens;
        private IssuerKeyAndParameters ikap;
        private FieldZqElement[] w;
        private GroupElement sigmaZ;
        private GroupElement[] sigmaA;
        private GroupElement[] sigmaB;

        /// <summary>
        /// Constructs a new <code>Issuer</code> instance.
        /// </summary>
        /// <param name="ikap">The Issuer key and parameters.</param>
        /// <param name="numberOfTokens">Number of tokens to issue.</param>
        /// <param name="A">The token attribute values.</param>
        /// <param name="TI">The token information field value.</param>
        /// <param name="hd">The Device public key. If this parameter is non-null, then the issued tokens will be Device-protected.</param>
        [Obsolete("Use IssuerProtocolParameters.Create()")]
        public Issuer(IssuerKeyAndParameters ikap, int numberOfTokens, byte[][] A, byte[] TI, GroupElement hd)
            : this(ikap, numberOfTokens, ProtocolHelper.ComputeIssuanceInput(ikap.IssuerParameters, A, TI, hd), null)
        { }

        /// <summary>
        /// Constructs a new <code>Issuer</code> instance.
        /// </summary>
        /// <param name="ikap">The Issuer key and parameters.</param>
        /// <param name="numberOfTokens">Number of tokens to issue.</param>
        /// <param name="A">The token attribute values.</param>
        /// <param name="TI">The token information field value.</param>
        /// <param name="hd">The Device public key. If this parameter is non-null, then the issued tokens will be Device-protected.</param>
        /// <param name="preGeneratedW">Optional pregenerated <code>numberOfTokens</code> random Zq elements.</param>
        [Obsolete("Use IssuerProtocolParameters.Create()")]
        public Issuer(IssuerKeyAndParameters ikap, int numberOfTokens, byte[][] A, byte[] TI, GroupElement hd, FieldZqElement[] preGeneratedW)
            : this(ikap, numberOfTokens, ProtocolHelper.ComputeIssuanceInput(ikap.IssuerParameters, A, TI, hd), preGeneratedW)
        { }

        /// <summary>
        /// Constructs a new <code>Issuer</code> instance ready to complete the issuance.
        /// </summary>
        /// <param name="ikap">The issuer key and parameters.</param>
        /// <param name="pfms">State of another <code>Issuer</code> instance after the first message was generated.</param>
        public Issuer(IssuerKeyAndParameters ikap, PostFirstMessageState pfms)
        {
            this.ikap = ikap;
            this.w = pfms.W;
            this.numberOfTokens = w.Length;
            state = State.First;
        }

        /// <summary>
        /// Constructs a new <code>Issuer</code> instance.
        /// </summary>
        /// <param name="ikap">The Issuer key and parameters.</param>
        /// <param name="numberOfTokens">Number of tokens to issue.</param>
        /// <param name="gamma">The gamma value encoding the token attributes.</param>
        /// <param name="preGeneratedW">Optional pregenerated <code>numberOfTokens</code> random Zq elements.</param>
        internal Issuer(IssuerKeyAndParameters ikap, int numberOfTokens, GroupElement gamma, FieldZqElement[] preGeneratedW)
        {
            if (ikap == null || ikap.PrivateKey == null || ikap.IssuerParameters == null)
            {
                throw new ArgumentNullException("ikap is malformed or null");
            }
            this.ikap = ikap;

            if (numberOfTokens <= 0)
            {
                throw new ArgumentException("numberOfTokens must be greater than 0");
            }
            this.numberOfTokens = numberOfTokens;

            if (gamma == null)
            {
                throw new ArgumentNullException("gamma is null");
            }

            if (preGeneratedW != null && preGeneratedW.Length != numberOfTokens)
            {
                throw new ArgumentException("invalid preGeneratedW array length");
            }

            Precompute(gamma, preGeneratedW);
        }

        private void Precompute(GroupElement gamma, FieldZqElement[] preGenW)
        {
            IssuerParameters ip = ikap.IssuerParameters;
            Group Gq = ip.Gq;
            FieldZq Zq = ip.Zq;
            sigmaZ = gamma.Exponentiate(ikap.PrivateKey);
            if (preGenW == null)
            {
                w = Zq.GetRandomElements(numberOfTokens, false);
            }
            else
            {
                w = preGenW;
            }

            sigmaA = new GroupElement[numberOfTokens];
            sigmaB = new GroupElement[numberOfTokens];
            for (int i = 0; i < numberOfTokens; i++)
            {
                sigmaA[i] = Gq.G.Exponentiate(w[i]);
                sigmaB[i] = gamma.Exponentiate(w[i]);
            }
            state = State.Initialized;
        }

        /// <summary>
        /// Generates the first issuance message.
        /// </summary>
        /// <returns>The first issuance message.</returns>
        public FirstIssuanceMessage GenerateFirstMessage()
        {
            if (state != State.Initialized)
            {
                throw new InvalidOperationException("Issuer not initialized properly");
            }
            state = State.First;
            return new FirstIssuanceMessage(this.sigmaZ, this.sigmaA, this.sigmaB);
        }

        /// <summary>
        /// Get the issuance state after the first message generation.
        /// </summary>
        /// <returns>The issuance state after the first message generation.</returns>
        public PostFirstMessageState ExportPostFirstMessageState()
        {
            if (state != State.First)
            {
                throw new InvalidOperationException("GenerateFirstMessage must be called first");
            }

            // we update the state so this object cannot be used by mistake to complete the issuance
            // (we don't want the same randomizer w to be used twice on two user-provided challenges)
            state = State.Third;

            return new PostFirstMessageState(w);
        }


        /// <summary>
        /// Generates the third issuance message.
        /// </summary>
        /// <param name="message">The second issuance message.</param>
        /// <returnn>The third issuance message.</returnn>
        public ThirdIssuanceMessage GenerateThirdMessage(SecondIssuanceMessage message)
        {
            if (state != State.First)
            {
                throw new InvalidOperationException("GenerateFirstMessage must be called first");
            }

            if (message.sigmaC.Length != numberOfTokens)
            {
                throw new ArgumentException("invalid sigmaC array length");
            }

            FieldZqElement[] sigmaR = new FieldZqElement[message.sigmaC.Length];
            Group Gq = ikap.IssuerParameters.Gq;
            FieldZq Zq = ikap.IssuerParameters.Zq;
            for (int i = 0; i < message.sigmaC.Length; i++)
            {
                sigmaR[i] = message.sigmaC[i] * ikap.PrivateKey + w[i];
                w[i] = Zq.Zero;
            }
            w = null;
            state = State.Third;
            return new ThirdIssuanceMessage(sigmaR);
        }
    }
}
