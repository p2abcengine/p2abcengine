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

using System.ComponentModel;
using System.Runtime.Serialization;
using UProveCrypto.Math;

namespace UProveCrypto
{
    /// <summary>
    /// Represents a U-Prove key and token.
    /// </summary>
    [DataContract]
    public class UProveKeyAndToken
    {
        private UProveToken token;
        private FieldZqElement privateKey;

        /// <summary>
        /// Constructs a new <code>UProveKeyAndToken</code> instance.
        /// </summary>
        public UProveKeyAndToken()
        {
        }

        /// <summary>
        /// Gets or sets the U-Prove token.
        /// </summary>
        [DataMember(Name = "token", Order = 1)]
        public UProveToken Token
        {
            get { return token; }
            set { token = value; }
        }

        /// <summary>
        /// Gets or sets the public key.
        /// </summary>
        public FieldZqElement PrivateKey
        {
            get { return privateKey; }
            set { privateKey = value; }
        }

        #region Serialization

        [DataMember(Name = "key", Order = 2)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _key;

        [OnSerializing]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnSerializing(StreamingContext context)
        {
            _key = this.PrivateKey.ToBase64String();
        }

        [OnDeserialized]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnDeserialized(StreamingContext context)
        {
            if (_key == null)
                throw new UProveSerializationException("key");

            this.PrivateKey = _key.ToFieldZqElement(Serializer.ip.Zq);
        }

        #endregion Serialization

    }
}