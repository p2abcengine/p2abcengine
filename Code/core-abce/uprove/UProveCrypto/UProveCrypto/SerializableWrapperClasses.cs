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

using System.Runtime.Serialization;
using UProveCrypto.Math;

namespace UProveCrypto
{

    #region Serializable Wrapper Classes

    /// <summary>
    /// This class is a serializable version of Group used only during serialization.
    /// Serializing Group will result in the creation and serialization of this class instead.
    /// This class is also created upon deserialization. The ToGroup() method will be called
    /// by the surrogate class to create a new Group from this class.
    /// </summary>
    [DataContract]
    public class GroupSerializable
    {
        /// <summary>
        /// The type of the group.
        /// </summary>
        [DataMember(Order=0)]
        public string type;

        /// <summary>
        /// The name of the group.
        /// </summary>
        [DataMember(Name = "name", Order = 1, EmitDefaultValue = false)]
        public string name;

        /// <summary>
        /// The subgroup group description.
        /// </summary>
        [DataMember(Name = "sgDesc", Order = 1, EmitDefaultValue = false)]
        public SubgroupGroupSerializable sgDesc;

        /// <summary>
        /// Construct a GroupSerializable object from a Group object.
        /// </summary>
        /// <param name="group">The Group object being serialized.</param>
        public GroupSerializable(Group group)
        {
            if (this.InRecommendedGroup(group.GroupName))
            {
                this.type = "named";
                this.name = group.GroupName;
                this.sgDesc = null;
            }
            else if (group.Type == GroupType.ECC)
            {
                this.type = "ec";
                this.name = null;
                this.sgDesc = null;
            }
            else if (group.Type == GroupType.Subgroup)
            {
                this.type = "sg";
                this.sgDesc = new SubgroupGroupSerializable((SubgroupGroup)group);
                this.name = null;
            }
            else
            {
                throw new UProveSerializationException("Invalid GroupConstruction");
            }

            return;
        }

        /// <summary>
        /// Deserialize this object into a Group object.
        /// </summary>
        /// <returns>The Group object represented by this GroupSerializable object.</returns>
        public Group ToGroup()
        {
            ParameterSet parameterSet;
            if ((sgDesc != null ? 1 : 0) + (name != null ? 1 : 0) > 1)
            {
                throw new UProveSerializationException("Only one of 'name' or 'sgDesc' can be set");
            }

            switch (type)
            {
                case "sg":
                    return sgDesc.ToSubgroupGroup();

                case "named":
                    if (ParameterSet.TryGetNamedParameterSet(name, out parameterSet) == false)
                        throw new UProveSerializationException("Unsupported named group :" + this.name);
                    break;

                default:
                    throw new UProveSerializationException("Invalid GroupConstruction: " + this.type);
            }

            return parameterSet.Group;
        }

        private bool InRecommendedGroup(string groupName)
        {
            switch (groupName)
            {
                case ECParameterSets.ParamSet_EC_P256_V1Name:
                case ECParameterSets.ParamSet_EC_P384_V1Name:
                case ECParameterSets.ParamSet_EC_P521_V1Name:
                case SubgroupParameterSets.ParamSet_SG_2048256_V1Name:
                case SubgroupParameterSets.ParamSet_SG_3072256_V1Name:
                case SubgroupParameterSets.ParamSet_SG_1024160_V1Name:
                    return true;

                default:
                    return false;
            }
        }

    }

    /// <summary>
    /// This class is a serializable version of SubgroupGroup used only during serialization.
    /// Serializing SubgroupGroup will result in the creation and serialization of this class instead.
    /// This class is also created upon deserialization. The ToSubgroupGroup() method will be called
    /// by the surrogate class to create a new SubgroupGroup from this class.
    /// </summary>
    [DataContract]
    public class SubgroupGroupSerializable
    {
        /// <summary>
        /// The parameter p.
        /// </summary>
        [DataMember(Order = 1)]
        public string p;

        /// <summary>
        /// The parameter q.
        /// </summary>
        [DataMember(Order = 2)]
        public string q;

        /// <summary>
        /// The generator g.
        /// </summary>
        [DataMember(Order = 3)]
        public string g;

        /// <summary>
        /// Create a SubgrouGroupSerializable object from a SubgroupGroup.
        /// </summary>
        /// <param name="group">The group to serialize.</param>
        public SubgroupGroupSerializable(SubgroupGroup group)
        {
            if (group.P == null || group.Q == null || group.G == null)
            {
                throw new UProveSerializationException("P, Q, G cannot be null");
            }

            this.p = group.P.ToBase64String();
            this.q = group.Q.ToBase64String();
            this.g = group.G.ToBase64String();
        }

        /// <summary>
        /// Create a SubgroupGroup from this serialized form.
        /// </summary>
        /// <returns>A SubgroupGroup object.</returns>
        public SubgroupGroup ToSubgroupGroup()
        {
            if (p == null || q == null || g == null)
            {
                throw new UProveSerializationException("p, q, g cannot be null");
            }

            SubgroupGroup group = SubgroupGroup.CreateSubgroupGroup(
                p.ToByteArray(), 
                q.ToByteArray(), 
                g.ToByteArray(), 
                null, 
                null);
            return group;
        }
    }

    #endregion

}
