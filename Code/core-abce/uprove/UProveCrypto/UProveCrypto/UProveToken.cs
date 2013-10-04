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
    /// Represents a U-Prove token.
    /// </summary>
    [DataContract]
    public class UProveToken
    {
        private byte[] uidp;
        private GroupElement h;
        private byte[] ti;
        private byte[] pi;
        private GroupElement sigmaZPrime;
        private FieldZqElement sigmaCPrime;
        private FieldZqElement sigmaRPrime;
        private bool isDeviceProtected;

        /// <summary>
        /// Constructs a U-Prove token.
        /// </summary>
        public UProveToken()
        {
        }

        /// <summary>
        /// Represents a U-Prove token.
        /// </summary>
        /// <param name="uidp">The Issuer parameters UID.</param>
        /// <param name="h">The public key value h.</param>
        /// <param name="TI">The token information field value.</param>
        /// <param name="PI">The Prover information field value.</param>
        /// <param name="sigmaZPrime">The sigmaZPrime value.</param>
        /// <param name="sigmaCPrime">The sigmaCPrime value.</param>
        /// <param name="sigmaRPrime">The sigmaRPrime value.</param>
        /// <param name="isDeviceProtected">True if the token is Device-protected, false otherwise.</param>
        public UProveToken(byte[] uidp, GroupElement h, byte[] TI, byte[] PI, GroupElement sigmaZPrime, FieldZqElement sigmaCPrime, FieldZqElement sigmaRPrime, bool isDeviceProtected)
        {
            this.uidp = uidp;
            this.h = h;
            this.ti = TI;
            this.pi = PI;
            this.sigmaZPrime = sigmaZPrime;
            this.sigmaCPrime = sigmaCPrime;
            this.sigmaRPrime = sigmaRPrime;
            this.isDeviceProtected = isDeviceProtected;
        }

        /// <summary>
        /// Gets or sets the Issuer parameters UID.
        /// </summary>
        public byte[] Uidp {
            get { return uidp; }
            set { uidp = value; }
        }

        /// <summary>
        /// Gets or sets the public key value h.
        /// </summary>
        public GroupElement H
        {
            get { return h; }
            set { h = value; }
        }

        /// <summary>
        /// Gets or sets the token information field value.
        /// </summary>
        public byte[] TI
        {
            get { return ti; }
            set { ti = value; }
        }

        /// <summary>
        /// Gets or sets the Prover information field value.
        /// </summary>
        public byte[] PI
        {
            get { return pi; }
            set { pi = value; }
        }

        /// <summary>
        /// Gets or sets the sigmaZPrime value.
        /// </summary>
        public GroupElement SigmaZPrime
        {
            get { return sigmaZPrime; }
            set { sigmaZPrime = value; }
        }

        /// <summary>
        /// Gets or sets the sigmaCPrime value.
        /// </summary>
        public FieldZqElement SigmaCPrime
        {
            get { return sigmaCPrime; }
            set { sigmaCPrime = value; }
        }

        /// <summary>
        /// Gets or sets the sigmaRPrime value.
        /// </summary>
        public FieldZqElement SigmaRPrime
        {
            get { return sigmaRPrime; }
            set { sigmaRPrime = value; }
        }

        /// <summary>
        /// Gets or sets the boolean indicating if the token is Device-protected.
        /// </summary>
        public bool IsDeviceProtected
        {
            get { return isDeviceProtected; }
            set { isDeviceProtected = value; }
        }


        #region Serialization

        [DataMember(Name = "uidp", Order = 1)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _uidp;

        [DataMember(Name = "h", Order = 2)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _h;

        [DataMember(Name = "ti", EmitDefaultValue = false, Order = 3)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _ti;

        [DataMember(Name = "pi", EmitDefaultValue = false, Order = 4)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _pi;

        [DataMember(Name = "szp", Order = 5)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _sigmaZPrime;

        [DataMember(Name = "scp", Order = 6)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _sigmaCPrime;

        [DataMember(Name = "srp", Order = 7)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _sigmaRPrime;

        [DataMember(Name = "d", EmitDefaultValue = false, Order = 8)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal bool? _isDeviceProtected;

        // Constructor takes a UProveToken. The fields are then converted to the desired
        // serializable types in this object before being serialized.
        [OnSerializing]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnSerializing(StreamingContext context)
        {
            _uidp              = this.Uidp.ToBase64String();
            _h                 = this.H.ToBase64String();
            _ti                = this.TI.ToBase64String();
            _pi                = this.PI.ToBase64String();
            _sigmaZPrime       = this.SigmaZPrime.ToBase64String();
            _sigmaCPrime       = this.SigmaCPrime.ToBase64String();
            _sigmaRPrime       = this.SigmaRPrime.ToBase64String();
            _isDeviceProtected = this.IsDeviceProtected;
        }

        // After deserialization of this object, this method will recreate an actual UProveToken
        [OnDeserialized]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnDeserialized(StreamingContext context)
        {
            if (_uidp == null)
                throw new UProveSerializationException("uidp");
            if (_h == null)
                throw new UProveSerializationException("h");
            if (_sigmaZPrime == null)
                throw new UProveSerializationException("szp");
            if (_sigmaCPrime == null)
                throw new UProveSerializationException("scp");
            if (_sigmaRPrime == null)
                throw new UProveSerializationException("srp");
            
            // default to false if not provided
            if (_isDeviceProtected == null)
                _isDeviceProtected = false;

            this.uidp = _uidp.ToByteArray();
            this.h = _h.ToGroupElement(Serializer.ip);
            this.ti = _ti.ToByteArray();
            this.pi = _pi.ToByteArray();
            this.sigmaZPrime = _sigmaZPrime.ToGroupElement(Serializer.ip);
            this.sigmaCPrime = _sigmaCPrime.ToFieldZqElement(Serializer.ip.Zq);
            this.sigmaRPrime = _sigmaRPrime.ToFieldZqElement(Serializer.ip.Zq);
            this.isDeviceProtected = _isDeviceProtected.Value;
        }

        #endregion Serialization

    }
}
