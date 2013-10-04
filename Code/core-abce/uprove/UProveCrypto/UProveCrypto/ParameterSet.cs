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
    /// Specifies a group and group generators to create a set of issuer parameters.
    /// </summary>
    public class ParameterSet
    {
        /// <summary>
        /// Constructs a new parameter set.
        /// </summary>
        /// <param name="name">The name of set.</param>
        /// <param name="group">The group.</param>
        /// <param name="g">An array of group generators.</param>
        /// <param name="gd">The device generator.</param>
        internal ParameterSet(string name, Group group, GroupElement[] g, GroupElement gd)
        {
            this.Name = name;
            this.Group = group;
            this.G = g;
            this.Gd = gd;
        }

        /// <summary>
        /// Gets or sets the set name.
        /// </summary>
        public string Name { get; private set; }

        /// <summary>
        /// Gets or sets the set group.
        /// </summary>
        public Group Group { get; private set; }

        /// <summary>
        /// Gets or sets the generators.
        /// </summary>
        public GroupElement[] G { get; private set; }

        /// <summary>
        /// Gets or sets the set device generator.
        /// </summary>
        public GroupElement Gd { get; private set; }

        /// <summary>
        /// Returns <code>true</code> if the requested parameter set is found, <code>false</code> otherwise.
        /// </summary>
        /// <param name="oid">The OID of the parameters set.</param>
        /// <returns><code>true</code> if the requested parameter set is found, <code>false</code> otherwise.</returns>
        public static bool ContainsParameterSet(string oid)
        {
            if (oid == SubgroupParameterSets.ParamSet_SG_2048256_V1Name ||
                oid == SubgroupParameterSets.ParamSet_SG_3072256_V1Name ||
                oid == SubgroupParameterSets.ParamSet_SG_1024160_V1Name ||
                oid == ECParameterSets.ParamSet_EC_P256_V1Name ||
                oid == ECParameterSets.ParamSet_EC_P384_V1Name ||
                oid == ECParameterSets.ParamSet_EC_P521_V1Name)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        
        /// <summary>
        /// Returns the identified parameter set if it exists. Valid <paramref name="oid"/> values are
        /// <see cref="SubgroupParameterSets.ParamSet_SG_2048256_V1Name"/>, 
        /// <see cref="SubgroupParameterSets.ParamSet_SG_3072256_V1Name"/>,
        /// <see cref="ECParameterSets.ParamSet_EC_P256_V1Name"/>,
        /// <see cref="ECParameterSets.ParamSet_EC_P384_V1Name"/>, and
        /// <see cref="ECParameterSets.ParamSet_EC_P521_V1Name"/>.
        /// </summary>
        /// <param name="oid">The OID of the parameters set.</param>
        /// <param name="set">The requested parameter set, if found.</param>
        /// <returns><code>true</code> if the requested parameter set is found, <code>false</code> otherwise.</returns>
        public static bool TryGetNamedParameterSet(string oid, out ParameterSet set)
        {
            set = null;

            if (oid == SubgroupParameterSets.ParamSet_SG_2048256_V1Name)
            {
                set = SubgroupParameterSets.ParamSetL2048N256V1;
            }
            else if (oid == SubgroupParameterSets.ParamSet_SG_3072256_V1Name)
            {
                set = SubgroupParameterSets.ParamSetL3072N256V1;
            }
            else if (oid == SubgroupParameterSets.ParamSet_SG_1024160_V1Name)
            {
                set = SubgroupParameterSets.ParamSetL1024N160V1;
            }
            else if (oid == ECParameterSets.ParamSet_EC_P256_V1Name)
            {
                set = ECParameterSets.ParamSet_EC_P256_V1;
            }
            else if (oid == ECParameterSets.ParamSet_EC_P384_V1Name)
            {
                set = ECParameterSets.ParamSet_EC_P384_V1;
            }
            else if (oid == ECParameterSets.ParamSet_EC_P521_V1Name)
            {
                set = ECParameterSets.ParamSet_EC_P521_V1;
            }
            return set != null;
        }

        /// <summary>
        /// The number of issuer generators included in the (pre-generated) parameter sets.
        /// </summary>
        public static readonly int NumberOfIssuerGenerators = 50;
    }

}
