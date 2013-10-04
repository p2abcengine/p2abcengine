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
#if NETFX_CORE
using Windows.Security.Cryptography;
#else
using System.Security.Cryptography;
#endif

using UProveCrypto.Math;


namespace UProveCrypto
{

    /// <summary>
    /// Implements a simple <code>Device</code> for the U-Prove protocol with device binding. 
    /// This implementation can only have one active session (context).
    /// </summary>
    public class VirtualDevice : IDevice, IDisposable
    {
        private readonly GroupElement Gd;
        private readonly Group Gq;
        private readonly FieldZq Zq;
        private readonly FieldZqElement xd;
        private readonly GroupElement hd;
        private readonly FieldZqElement wdPrime;

        /// <summary>
        /// Constructs a new VirtualDevice instance.
        /// </summary>
        /// <param name="ip">The Issuer parameters.</param>
        public VirtualDevice(IssuerParameters ip) : this(ip, null, null)
        {}

        /// <summary>
        /// Constructs a new VirtualDevice instance.
        /// </summary>
        /// <param name="ip">The Issuer parameters.</param>
        /// <param name="xd">The device private key.</param>
        public VirtualDevice(IssuerParameters ip, FieldZqElement xd)
            : this(ip, xd, null)
        {}

        /// <summary>
        /// Constructs a new VirtualDevice instance.
        /// </summary>
        /// <param name="ip">The Issuer parameters.</param>
        /// <param name="xd">The device private key.</param>
        /// <param name="preGenWdPrime">The pregenerated w_d prime value (for one presentation)</param>
        public VirtualDevice(IssuerParameters ip, FieldZqElement xd, FieldZqElement preGenWdPrime)
            : this(ip.Gq, ip.Gd, ip.Zq, xd, preGenWdPrime)
        {
        }

        /// <summary>
        /// Constructs a new VirtualDevice instance.
        /// </summary>
        /// <param name="parameterSet">The parameter set.</param>
        public VirtualDevice(ParameterSet parameterSet)
            : this(parameterSet, null, null)
        { }

        /// <summary>
        /// Constructs a new VirtualDevice instance.
        /// </summary>
        /// <param name="parameterSet">The parameter set.</param>
        /// <param name="xd">The device private key.</param>
        public VirtualDevice(ParameterSet parameterSet, FieldZqElement xd)
            : this(parameterSet, xd, null)
        { }

        /// <summary>
        /// Constructs a new VirtualDevice instance.
        /// </summary>
        /// <param name="parameterSet">The parameter set.</param>
        /// <param name="xd">The device private key.</param>
        /// <param name="preGenWdPrime">The pregenerated w_d prime value (for one presentation)</param>
        public VirtualDevice(ParameterSet parameterSet, FieldZqElement xd, FieldZqElement preGenWdPrime)
            : this(parameterSet.Group, parameterSet.Gd, FieldZq.CreateFieldZq(parameterSet.Group.Q), xd, preGenWdPrime)
        {
        }

        /// <summary>
        /// Private constructor - takes and sets all fields.
        /// </summary>
        /// <param name="Gq">The group</param>
        /// <param name="gd">The device generator</param>
        /// <param name="Zq">The Field associated to the group <c>Gq</c></param>
        /// <param name="xd">The xd.</param>
        /// <param name="preGenWdPrime">The pre gen wd prime.</param>
        VirtualDevice(Group Gq, GroupElement gd, FieldZq Zq, FieldZqElement xd, FieldZqElement preGenWdPrime)
        {
            if (xd != null && !Zq.IsElement(xd))
            {
                throw new ArgumentException("xd is not a valid Zq element");
            }
            this.Gd = gd;
            this.Gq = Gq;
            this.Zq = Zq;
            this.xd = xd ?? this.Zq.GetRandomElement(true);     // assign xd a random value if null
            this.wdPrime = preGenWdPrime;
            this.hd = this.Gd.Exponentiate(this.xd);
        }


        /// <summary>
        /// Returns the Device public key <code>h_d</code>.
        /// </summary>
        /// <returns>
        ///   <code>h_d</code>.
        /// </returns>
        GroupElement IDevice.GetDevicePublicKey()
        {
            return this.hd;
        }

        /// <summary>
        /// Gets the presentation context.
        /// </summary>
        /// <returns>
        /// A presentation context.
        /// </returns>
        IDevicePresentationContext IDevice.GetPresentationContext()
        {
            return new DevicePresentationContext(this);
        }

        /// <summary>
        /// Dispose this VirtualDevice
        /// </summary>
        public void Dispose()
        {
            // Since this class is implemented in managed code, there isn't much we can do
        }

        /// <summary>
        /// A device presentation context
        /// </summary>
        sealed class DevicePresentationContext : IDevicePresentationContext
        {
            /// <summary>
            /// The device
            /// </summary>
            VirtualDevice device;
            FieldZqElement wdPrime;

            /// <summary>
            /// Initializes a new instance of the <see cref="DevicePresentationContext"/> class.
            /// </summary>
            /// <param name="device">The device.</param>
            public DevicePresentationContext(VirtualDevice device)
            {
                this.device = device;
            }

            private string GetHashFunctionName(string hashOID)
            {
#if ( SILVERLIGHT || NETFX_CORE )
                string hashFunctionName;

                if (hashOID == "1.3.14.3.2.26")
                {
                    hashFunctionName = "sha1";
                }
                else if (hashOID == "2.16.840.1.101.3.4.2.1")
                {
                    hashFunctionName = "sha256";
                }
                else
                {
                    // Let the HashFunction creation fail
                    return hashOID;
                }
                return hashFunctionName;
#else
                Oid oid = new Oid(hashOID);

                return oid.FriendlyName ?? hashOID;
#endif
            }

            GroupElement IDevicePresentationContext.GetInitialWitness()
            {
                if (this.device == null)
                {
                    throw new DeviceException("Invalid context.");
                }
                if (this.wdPrime != null)
                {
                    throw new DeviceException("Initial witness already calculated.");
                }

                this.wdPrime = this.device.wdPrime ?? this.device.Zq.GetRandomElement(false);
                return this.device.Gd.Exponentiate(this.wdPrime);
            }

            GroupElement IDevicePresentationContext.GetInitialWitnessesAndPseudonym(GroupElement gs, out GroupElement apPrime, out GroupElement Ps)
            {
                if (this.device == null)
                {
                    throw new DeviceException("Invalid context.");
                }
                if (this.wdPrime != null)
                {
                    throw new DeviceException("Initial witness already calculated.");
                }

                this.wdPrime = this.device.wdPrime ?? this.device.Zq.GetRandomElement(false);
                
                // compute pseudonym
                apPrime = gs.Exponentiate(this.wdPrime);
                Ps = gs.Exponentiate(this.device.xd);

                // compute initial witness
                return this.device.Gd.Exponentiate(this.wdPrime);               
            }

            FieldZqElement IDevicePresentationContext.GetDeviceResponse(byte[] messageForDevice, byte[] partialChallengeDigest, string hashOID)
            {
                if (this.device == null)
                {
                    throw new DeviceException("Invalid context.");
                }
                if (this.wdPrime == null)
                {
                    throw new DeviceException("Initial witness not yet calculated.");
                }

                HashFunction hashFunction = new HashFunction(GetHashFunctionName(hashOID));
                FieldZqElement c = ProtocolHelper.GenerateChallengeForDevice(device.Zq, hashFunction, messageForDevice, partialChallengeDigest);
                FieldZqElement rdPrime = c.Negate() * device.xd + wdPrime;
                this.device = null;
                return rdPrime;
            }

            public void Dispose()
            {
                this.device = null;
            }
        }
    }
}
