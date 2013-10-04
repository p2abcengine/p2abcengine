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
using System.ComponentModel;

#if BOUNCY_CASTLE
using UProveCrypto.Math.BC;
#endif

namespace UProveCrypto.Math
{
    /// <summary>
    /// Describes a group Gq using the subgroup construction.
    /// </summary>
    [DataContract]
    public abstract class SubgroupGroup : Group
    {
        /// <summary>
        /// The value p in big-endian byte form.
        /// </summary>
        protected byte[] p;

        /// <summary>
        /// The domain parameter seed if verifiable generation is used.
        /// </summary>
        public byte[] DomainParameterSeed { get; internal set; }

        /// <summary>
        /// Creates a SubgroupGroup.
        /// </summary>
        /// <param name="p">The value p.</param>
        /// <param name="q">The value q.</param>
        /// <param name="g">The value g.</param>
        /// <param name="groupName">The known name of the group, or null.</param>
        /// <param name="domainParameterSeed">The domain parameter seed if verifiable 
        /// generation is used, or null.</param>
        public static SubgroupGroup CreateSubgroupGroup(
            byte[] p, 
            byte[] q, 
            byte[] g, 
            string groupName, 
            byte[] domainParameterSeed)
        {
#if BOUNCY_CASTLE            
            return new SubgroupGroupBCImpl(p, q, g, groupName, domainParameterSeed);
#endif
        }

        /// <summary>
        /// Protected constructor for child types to call only.
        /// </summary>
        /// <param name="q">The value of q.</param>
        /// <param name="groupName">The known name of the group, or null.</param>
        /// <param name="domainParameterSeed">The domain parameter seed if verifiable 
        /// generation is used, or null.</param>
        protected SubgroupGroup(
            byte[] q, 
            string groupName, 
            byte[] domainParameterSeed) : 
            base(GroupType.Subgroup, q, groupName) 
        { 
            DomainParameterSeed = domainParameterSeed; 
        }

        /// <summary>
        /// Gets the value p.
        /// </summary>
        public abstract byte[] P { get; internal set; }

        #region Serialization

        /// <summary>
        /// The serializable representation of this object.
        /// </summary>
        [DataMember(Name = "sg")]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal SubgroupGroupSerializable SubgroupGroupSerializable;

        /// <summary>
        /// Custom serializer for this object.
        /// </summary>
        /// <param name="context">The streaming context.</param>
        [OnSerializing]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnSerializing(StreamingContext context)
        {
            this.SubgroupGroupSerializable = 
                new SubgroupGroupSerializable(this);
        }

        /// <summary>
        /// Custom deserializer for this object.
        /// </summary>
        /// <param name="context">The streaming context.</param>
        [OnDeserialized]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnDeserialized(StreamingContext context)
        {
            // reuse the serializable class's method to deserialize the group
            SubgroupGroup sg = 
                this.SubgroupGroupSerializable.ToSubgroupGroup();

            this.P = sg.P;
            this.Q = sg.Q;
            this.G = sg.G;
            this.GroupName = sg.GroupName;
            this.DomainParameterSeed = sg.DomainParameterSeed;
        }

        #endregion Serialization
    }
}
