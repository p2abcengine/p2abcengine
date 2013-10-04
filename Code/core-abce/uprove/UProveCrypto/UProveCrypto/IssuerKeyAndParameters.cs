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
    /// Contains an Issuer parameters and the associated private key.
    /// </summary>
    [DataContract]
    public class IssuerKeyAndParameters
    {
        private FieldZqElement privateKey;
        private IssuerParameters issuerParameters;

        /// <summary>
        /// Constructs an IssuerKeyAndParameters instance.
        /// </summary>
        /// <param name="privateKey">The private key.</param>
        /// <param name="issuerParameters">The Issuer parameters.</param>
        public IssuerKeyAndParameters(FieldZqElement privateKey, IssuerParameters issuerParameters)
        {
            if (privateKey == null)
            {
                throw new ArgumentNullException("privateKey");
            }
            if (issuerParameters == null)
            {
                throw new ArgumentNullException("issuerParameters");
            }
            this.privateKey = privateKey;
            this.issuerParameters = issuerParameters;
        }

        /// <summary>
        /// Constructs an IssuerKeyAndParameters instance from serialized strings.
        /// </summary>
        /// <param name="serializedPrivateKey">The serialized private key.</param>
        /// <param name="serializedIssuerParameters">The serialized Issuer parameters.</param>
        public IssuerKeyAndParameters(string serializedPrivateKey, string serializedIssuerParameters)
        {
            this.IssuerParameters = new IssuerParameters(serializedIssuerParameters);
            this.privateKey = serializedPrivateKey.ToFieldZqElement(IssuerParameters.Zq);
        }

        /// <summary>
        /// The private key.
        /// </summary>
        public FieldZqElement PrivateKey
        {
            get { return privateKey; }
            set { privateKey = value; }
        }

        /// <summary>
        /// The Issuer parameters.
        /// </summary>
        public IssuerParameters IssuerParameters
        {
            get { return issuerParameters; }
            set { issuerParameters = value; }
        }


        #region Serialization

        [DataMember(Name = "ip", Order = 1)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal IssuerParameters _issuerParameters;

        [DataMember(Name = "key", Order = 2)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _privateKey;

        [OnSerializing]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnSerializing(StreamingContext context)
        {
            this._issuerParameters = this.issuerParameters;
            this._privateKey = this.PrivateKey.ToBase64String();
        }

        [OnDeserialized]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnDeserialized(StreamingContext context)
        {
            if (_issuerParameters == null)
                throw new UProveSerializationException("ip");
            if (_privateKey == null)
                throw new UProveSerializationException("key");

            this.issuerParameters = _issuerParameters;
            this.privateKey = _privateKey.ToFieldElement(this.issuerParameters);
        }

        #endregion
    }
}
