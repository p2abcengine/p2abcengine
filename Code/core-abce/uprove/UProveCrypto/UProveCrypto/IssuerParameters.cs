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
    /// Represents the Issuer Parameters, including the Device parameters if issuance of Device-protected
    /// tokens is supported.
    /// </summary>
    [DataContract]
    public class IssuerParameters
    {
        private byte[] uidp;
        private Group group;
        private string uidh;
        private GroupElement[] g;
        private GroupElement gd;
        private byte[] e;
        private byte[] s;
        private byte[][] digest = new byte[2][];
        private FieldZq fieldZq;
        private bool usesRecommendedParameters;

        /// <summary>
        /// Constructs an Issuer parameters instance.
        /// </summary>
        public IssuerParameters()
        {
            IssuerParameters.serializer = new Serializer(this);
        }

        /// <summary>
        /// Constructs an Issuer parameters instance from a serialized string
        /// </summary>
        public IssuerParameters(string serializedIssuerParameters)
        {
            IssuerParameters.serializer = new Serializer(this);

            IssuerParameters issuerParameters = this.Deserialize<IssuerParameters>(serializedIssuerParameters);
            this.uidp = issuerParameters.uidp;
            this.group = issuerParameters.group;
            this.uidh = issuerParameters.uidh;
            this.g = issuerParameters.g;
            this.gd = issuerParameters.gd;
            this.e = issuerParameters.e;
            this.s = issuerParameters.s;
            this.usesRecommendedParameters = issuerParameters.usesRecommendedParameters;
        }

        /// <summary>
        /// Constructs an Issuer parameters instance.
        /// </summary>
        /// <param name="uidp">The Issuer parameters UID.</param>
        /// <param name="group">The group description.</param>
        /// <param name="uidh">The hash algorithm identifier.</param>
        /// <param name="g">The generator values.</param>
        /// <param name="gd">The Device generator, or null.</param>
        /// <param name="e">The encoding bytes.</param>
        /// <param name="s">The specification bytes.</param>
        /// <param name="usesRecommendedParameters">Indicates if the group and g array uses the recommended parameters.</param>
        public IssuerParameters(byte[] uidp, Group group, string uidh, GroupElement[] g, GroupElement gd, byte[] e, byte[] s, bool usesRecommendedParameters)
        {
            this.uidp = uidp;
            this.group = group;
            this.uidh = uidh;
            this.g = g;
            this.gd = gd;
            this.e = e;
            this.s = s;
            this.usesRecommendedParameters = usesRecommendedParameters;
            IssuerParameters.serializer = new Serializer(this);
        }

        /// <summary>
        /// Gets or sets the Issuer parameters UID.
        /// </summary>
        public byte[] UidP
        {
            get { return uidp; }
            set { uidp = value; }
        }

        /// <summary>
        /// Gets or sets the group description.
        /// </summary>
        public Group Gq
        {
            get { return group; }
            set { group = value; }
        }

        /// <summary>
        /// Gets or sets the hash algorithm identifier.
        /// </summary>
        public string UidH
        {
            get { return uidh; }
            set { uidh = value; }
        }

        /// <summary>
        /// Gets or sets the generator values.
        /// </summary>
        public GroupElement[] G
        {
            get { return g; }
            set { g = value; }
        }

        /// <summary>
        /// Gets or sets the Device generator value.
        /// </summary>
        public GroupElement Gd
        {
            get { return gd; }
            set { gd = value; }
        }

        /// <summary>
        /// Gets or sets the encoding bytes.
        /// </summary>
        public byte[] E
        {
            get { return e; }
            set { e = value; }
        }

        /// <summary>
        /// Gets or sets the specification bytes.
        /// </summary>
        public byte[] S
        {
            get { return s; }
            set { s = value; }
        }

        /// <summary>
        /// Gets or sets the indicator for usage of the recommended parameters.
        /// </summary>
        public bool UsesRecommendedParameters
        {
            get { return usesRecommendedParameters; }
            set { usesRecommendedParameters = value; }
        }

        /// <summary>
        /// Returns true if Device-protection is supported.
        /// </summary>
        public bool IsDeviceSupported
        {
            get { return gd != null; }
        }

        /// <summary>
        /// Verifies the Issuer parameters.
        /// </summary>
        public void Verify()
        {
            ProtocolHelper.VerifyIssuerParameters(this, UsesRecommendedParameters);
        }

        /// <summary>
        /// Gets a newly instanciated hash function with the algorithm specified in Uidh.
        /// </summary>
        public HashFunction HashFunction
        {
            get { return new HashFunction(UidH); }
        }

        private byte[] ComputeDigest(bool deviceProtected)
        {
            HashFunction H = this.HashFunction;
            H.Hash(UidP);
            H.Hash(group);
            if (deviceProtected)
            {
                // Gd must be part of the Gi array    
                GroupElement[] GWithGd = new GroupElement[G.Length + 1];
                Array.Copy(G, GWithGd, G.Length);
                GWithGd[G.Length] = gd;
                H.Hash(GWithGd);
            }
            else
            {
                H.Hash(G);
            }
            H.Hash(E);
            H.Hash(S);
            return H.Digest;
        }

        /// <summary>
        /// Gets the Issuer parameter digest.
        /// <param name="deviceProtected">True if the digest is for a device-protected token, false otherwise.</param>
        /// </summary>
        internal byte[] Digest(bool deviceProtected)
        {
            if (deviceProtected && !IsDeviceSupported)
            {
                throw new InvalidOperationException("Issuer parameters does not support device issuance");
            }

            // we keep two cached digests, one for device-protected tokens and one for software-only tokens
            // (even if the issuer params supports devices).
            int index = (deviceProtected ? 1 : 0);
            if (digest[index] == null)
            {
                digest[index] = ComputeDigest(deviceProtected);
            }
            return digest[index];
        }

        /// <summary>
        /// The FieldZq object associated with this IssuerParameters object.
        /// </summary>
        public FieldZq Zq
        {
            get {
                if (this.fieldZq == null)
                {
                    if (Gq == null)
                    {
                        throw new ArgumentNullException("Gq is null");
                    }
                    fieldZq = FieldZq.CreateFieldZq(Gq.Q); 
                }
                return fieldZq;
            }
        }

        /// <summary>
        /// Gets the algorithm OID for the active hash function. OIDs are taken from http://msdn.microsoft.com/en-us/library/aa381133(VS.85).aspx.
        /// </summary>
        public string HashFunctionOID { 
            get {
                if (UidH.Equals("SHA", StringComparison.OrdinalIgnoreCase) ||
                    UidH.Equals("SHA1", StringComparison.OrdinalIgnoreCase) ||
                    UidH.Equals("System.Security.Cryptography.SHA1", StringComparison.OrdinalIgnoreCase) ||
                    UidH.Equals("http://www.w3.org/2000/09/xmldsig#sha1", StringComparison.OrdinalIgnoreCase))
                {
                    return ProtocolHelper.GetHashOID(ProtocolHelper.SupportedHashFunctions.SHA1);
                }
                else if (UidH.Equals("SHA256", StringComparison.OrdinalIgnoreCase) ||
                    UidH.Equals("SHA-256", StringComparison.OrdinalIgnoreCase) ||
                    UidH.Equals("System.Security.Cryptography.SHA256", StringComparison.OrdinalIgnoreCase) ||
                    UidH.Equals("http://www.w3.org/2001/04/xmlenc#sha256", StringComparison.OrdinalIgnoreCase))
                {
                    return ProtocolHelper.GetHashOID(ProtocolHelper.SupportedHashFunctions.SHA256);
                }
                else if (UidH.Equals("SHA384", StringComparison.OrdinalIgnoreCase) ||
                    UidH.Equals("SHA-384", StringComparison.OrdinalIgnoreCase) ||
                    UidH.Equals("System.Security.Cryptography.SHA384", StringComparison.OrdinalIgnoreCase))
                {
                    return ProtocolHelper.GetHashOID(ProtocolHelper.SupportedHashFunctions.SHA384);
                }
                else if (UidH.Equals("SHA512", StringComparison.OrdinalIgnoreCase) ||
                    UidH.Equals("SHA-512", StringComparison.OrdinalIgnoreCase) ||
                    UidH.Equals("System.Security.Cryptography.SHA512", StringComparison.OrdinalIgnoreCase))
                {
                    return ProtocolHelper.GetHashOID(ProtocolHelper.SupportedHashFunctions.SHA512);
                }
                else
                {
                    throw new InvalidOperationException("Hash algorithm has no associated OID. Use a hash from the SHA familly.");
                }
            } 
        }



        private static Serializer serializer;

        /// <summary>
        /// Serialize a UProveCrypto type to a JSON string
        /// </summary>
        public string Serialize<T>(T obj)
        {
            return serializer.GetJson(obj);
        }

        /// <summary>
        /// Serialize this IssuerParameters to a JSON string
        /// </summary>
        public string Serialize()
        {
            try
            {
                return serializer.GetJson(this);
            }
            catch
            {
                throw;
            }
        }

        /// <summary>
        /// Deserialize a JSON string into a UProveCrypto object
        /// </summary>
        public T Deserialize<T>(string serializedString)
        {
            try
            {
                return serializer.FromJson<T>(serializedString);
            }
            catch
            {
                throw;
            }
        }


        #region Serialization

        [DataMember(Name = "uidp", Order = 1)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _uidp;

        [DataMember(Name = "descGq", Order = 2)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal GroupSerializable _group;

        [DataMember(Name = "uidh", Order = 3)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _uidh;

        [DataMember(Name = "g", Order = 4)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string[] _g;

        [DataMember(Name = "gd", EmitDefaultValue = false, Order = 5)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _gd;

        [DataMember(Name = "e", EmitDefaultValue = false, Order = 6)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _e;

        [DataMember(Name = "s", EmitDefaultValue = false, Order = 7)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _s;

        [OnSerializing]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnSerializing(StreamingContext context)
        {
            _uidp  = this.UidP.ToBase64String();
            _group = new GroupSerializable(this.Gq);
            _uidh  = this.UidH;
            _e     = this.E.ToBase64String();
            _s     = this.S.ToBase64String();

            if (ParameterSet.ContainsParameterSet(this.group.GroupName) &&
                (this.UsesRecommendedParameters == true))
            {
                this._g = new string[] { this.G[0].ToBase64String() };
            }
            else
            {
                this._g = this.G.ToBase64StringArray();
                this._gd = (this.Gd == null) ? null : this.Gd.ToBase64String();
            }
        }

        [OnDeserialized]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnDeserialized(StreamingContext context)
        {
            if (_uidp == null)
                throw new UProveSerializationException("uidp");
            if (_group == null)
                throw new UProveSerializationException("descGq");
            if (_uidh == null)
                throw new UProveSerializationException("uidh");
            if (_g == null)
                throw new UProveSerializationException("g");

            this.UidP = _uidp.ToByteArray();
            this.Gq   = _group.ToGroup();
            this.UidH = _uidh;

            this.Gd = _gd.ToGroupElement(this.Gq);
            this.E = (_e == null) ? new byte[] {} : _e.ToByteArray();
            this.S = _s.ToByteArray();

            ParameterSet defaultParamSet;
            if (ParameterSet.TryGetNamedParameterSet(this.Gq.GroupName, out defaultParamSet)) // named
            {
                if ((_g.Length == 1)) // only have g0
                {
                    ProtocolHelper.GenerateIssuerParametersCryptoData(this, defaultParamSet.G, false);
                    this.G[0] = _g.ToGroupElementArray(this.Gq)[0];
                    this.UsesRecommendedParameters = true;
                }
                else if (_g.Length == E.Length + 2) // we got all the G elements
                {
                    this.G = _g.ToGroupElementArray(this.Gq);
                }
                else
                {
                    throw new UProveSerializationException("Invalid number of elements in G");
                }
                if (this.gd == null)
                {
                    // named group always support devices
                    this.Gd = defaultParamSet.Gd;
                }
            }
            else  // custom - use all provided elements of G
            {
                if (_g.Length != E.Length + 2)
                {
                    throw new UProveSerializationException("Invalid number of elements in G");
                }
                this.G = _g.ToGroupElementArray(this.Gq);
            }
        }

        #endregion Serialization

    }
}
