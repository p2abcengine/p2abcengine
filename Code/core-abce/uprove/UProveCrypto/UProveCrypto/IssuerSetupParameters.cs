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
using UProveCrypto.Math;

namespace UProveCrypto
{
    /// <summary>
    /// Specifies the setup parameters that will be used to generate the issuer parameters. At a
    /// minimum, the <see cref="UidP"/> and either the <see cref="E"/> or 
    /// <see cref="NumberOfAttributes"/> attributes must be set. Default
    /// values are described in the documentation of each attribute.
    /// </summary>
    public class IssuerSetupParameters
    {
        private IssuerParameters ip;
        private bool? useRecommendedParameterSet;

        /// <summary>
        /// Returns the max number of attributes when using the recommended parameters.
        /// </summary>
        public static int RecommendedParametersMaxNumberOfAttributes { get { return ParameterSet.NumberOfIssuerGenerators; } }

        /// <summary>
        /// Constructs a new <code>IssuerSetupParameters</code> instance.
        /// </summary>
        public IssuerSetupParameters()
        {
            ip = new IssuerParameters();
            // set the defaults
            ip.UidH = "SHA-256";
            GroupConstruction = GroupType.Subgroup;
        }

        /// <summary>
        /// Gets or sets the group construction. Defaults to <see cref="GroupType.Subgroup"/>.
        /// </summary>
        public GroupType GroupConstruction { get; set; }

        /// <summary>
        /// Gets or sets the parameter set. Defaults to the value returned by <see cref="M:GetDefaultParameterSet"/>.
        /// </summary>
        public ParameterSet ParameterSet { get; set; }

        /// <summary>
        /// Gets or sets if recommended parameters are used. Defaults to <code>true</code>.
        /// </summary>
        public bool UseRecommendedParameterSet { 
            get { return useRecommendedParameterSet.HasValue ? useRecommendedParameterSet.Value : true; }
            set {useRecommendedParameterSet = value;} }
        
        /// <summary>
        /// Gets or sets the group description. This attribute should only be used if a non-recommended
        /// group is used. There is no default value.
        /// </summary>
        public Group Gq
        {
            get { return ip.Gq; }
            set { ip.Gq = value; }
        }
        
        /// <summary>
        /// Gets or sets the E array. This attribute is automatically set when
        /// setting the <see cref="NumberOfAttributes"/> attribute using the
        /// default encoding values.
        /// </summary>
        public byte[] E
        {
            get { return ip.E; }
            set { ip.E = value; }
        }

        /// <summary>
        /// Gets or sets the number of attributes to be encoded in the token.
        /// Setting this property will also set the corresponding <see cref="E"/>
        /// attribute using <see cref="M:GetDefaultEValues"/>.
        /// </summary>
        public int NumberOfAttributes
        {
            get
            {
                if (E == null)
                {
                    return 0;
                }
                else
                {
                    return E.Length;
                }
            }
            set
            {
                E = GetDefaultEValues(value);
            }
        }

        /// <summary>
        /// Gets or sets the hash algorithm unique identifier. Defaults to <code>SHA-256</code>.
        /// </summary>
        public string UidH
        {
            get { return ip.UidH; }
            set { ip.UidH = value; }
        }

        /// <summary>
        /// Gets of sets the Issuer parameters unique identifier. This value must be set, there is
        /// no default.
        /// </summary>
        public byte[] UidP
        {
            get { return ip.UidP; }
            set { ip.UidP = value; }
        }

        /// <summary>
        /// Gets or sets the specification value. Defaults to <code>null</code>.
        /// </summary>
        public byte[] S
        {
            get { return ip.S; }
            set { ip.S = value; }
        }

        /// <summary>
        /// Validates the consistency of the object. This method is called by the <see cref="Generate"/> method.
        /// </summary>
        public void Validate()
        {
            if (ip.Gq != null)
            {
                try
                {
                    ip.Gq.Verify();
                }
                catch (InvalidUProveArtifactException e)
                {
                    throw new ArgumentException(e.Message);
                }
            }

            if (ip.UidP == null)
            {
                throw new ArgumentNullException("UidP is null");
            }

            try
            {
                HashFunction h = ip.HashFunction;
            }
            catch (Exception)
            {
                throw new ArgumentException("Hash algorithm specified by UidH is not supported:" + ip.UidH);
            }

            if (ip.E == null)
            {
                throw new ArgumentNullException("E is null");
            }

            if (ip.E.Length > RecommendedParametersMaxNumberOfAttributes)
            {
                if (useRecommendedParameterSet.HasValue && useRecommendedParameterSet.Value)
                {
                    throw new ArgumentException("When using recommended parameters, the maximum number of attributes is " + RecommendedParametersMaxNumberOfAttributes);
                }
                useRecommendedParameterSet = false;
            }
        }

        /// <summary>
        /// Generates a fresh Issuer key and parameters.
        /// </summary>
        /// <param name="supportDevice">If true, the parameters will support issuing Device-protected tokens. Defaults to <code>false</code>.</param>
        /// <returns>A Issuer key and parameters instance.</returns>
        public IssuerKeyAndParameters Generate(bool supportDevice = false)
        {
            // first validate the data we have
            Validate();

            GroupElement[] gValues = null;
            if (ip.Gq == null)
            {
                if (this.ParameterSet != null)
                {
                    ip.Gq = this.ParameterSet.Group;
                    gValues = this.ParameterSet.G;
                    if (supportDevice)
                    {
                        ip.Gd = this.ParameterSet.Gd;
                    }
                    // is that a known parameter set?
                    ip.UsesRecommendedParameters = ParameterSet.ContainsParameterSet(this.ParameterSet.Name); 
                }
                else
                {
                    ParameterSet defaultParamSet = IssuerSetupParameters.GetDefaultParameterSet(this.GroupConstruction);
                    ip.Gq = defaultParamSet.Group;
                    if (UseRecommendedParameterSet)
                    {
                        gValues = defaultParamSet.G;
                        // recommended groups always support devices
                        ip.Gd = defaultParamSet.Gd;
                        ip.UsesRecommendedParameters = true;
                    }
                }
            }

            FieldZqElement y0 = ProtocolHelper.GenerateIssuerParametersCryptoData(ip, gValues, supportDevice);
            return new IssuerKeyAndParameters(y0, ip);
        }

        /// <summary>
        /// Returns the default parameter set for a specified construction. For <see cref="GroupType.Subgroup"/>,
        /// the default is <see cref="SubgroupParameterSets.ParamSetL2048N256V1"/>; for <see cref="GroupType.ECC"/>,
        /// the default is <see cref="ECParameterSets.ParamSet_EC_P256_V1"/>.
        /// </summary>
        /// <param name="construction">Group construction to use. Defaults to <see cref="GroupType.Subgroup"/>.</param>
        /// <returns>The default parameter set.</returns>
        public static ParameterSet GetDefaultParameterSet(GroupType construction = GroupType.Subgroup)
        {
            if (construction == GroupType.Subgroup)
            {
                return SubgroupParameterSets.ParamSetL2048N256V1;
            }
            else if (construction == GroupType.ECC)
            {
                return ECParameterSets.ParamSet_EC_P256_V1;
            }
            else
            {
                throw new ArgumentException("unsupported group construction: " + construction);
            }
        }

        /// <summary>
        /// Get the default <code>E</code> values for a specified number of attributes. This returns an
        /// array initized with <code>0x01</code>, to allow arbitrary-size attributes.
        /// </summary>
        /// <param name="numberOfAttributes">Number of attributes to encode in U-Prove tokens.</param>
        /// <returns>A byte-array initialized with 0x01 bytes.</returns>
        public static byte[] GetDefaultEValues(int numberOfAttributes)
        {
            byte[] E = new byte[numberOfAttributes];
            for (int i = 0; i < numberOfAttributes; i++)
            {
                E[i] = 1;
            }
            return E;
        }
    }
}
