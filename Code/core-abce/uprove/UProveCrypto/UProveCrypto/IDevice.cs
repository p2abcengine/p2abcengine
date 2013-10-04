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
    /// Simple Device Interface
    /// </summary>
    public interface IDevice : IDisposable
    {
        /// <summary>
        /// Returns the Device public key <code>h_d</code>.
        /// </summary>
        /// <returns><code>h_d</code>.</returns>
        GroupElement GetDevicePublicKey();

        /// <summary>
        /// Gets the presentation context.
        /// </summary>
        /// <returns>A presentation context.</returns>
        IDevicePresentationContext GetPresentationContext();
    }

    /// <summary>
    /// Interface for a device presentation context
    /// </summary>
    public interface IDevicePresentationContext : IDisposable
    {
        /// <summary>
        /// Returns the Device initial witness for a U-Prove token presentation.
        /// </summary>
        /// <returns><code>a</code>.</returns>
        GroupElement GetInitialWitness();

        /// <summary>
        /// Returns the Device initial witness for a U-Prove token presentation.
        /// </summary>
        /// <param name="gs">A group element derived from the pseudonym scope</param>
        /// <param name="apPrime">The <code>ap'</code> value.</param>
        /// <param name="Ps">The <code>Ps</code> value.</param>
        /// <returns><code>a</code>.</returns>
        GroupElement GetInitialWitnessesAndPseudonym(GroupElement gs, out GroupElement apPrime, out GroupElement Ps);

        /// <summary>
        ///  Returns the Device response for a U-Prove token presentation.
        /// </summary>
        /// <param name="messageForDevice">The message for the Device.</param>
        /// <param name="partialChallengeDigest">The partial challenge digest.</param>
        /// <param name="hashOID">The hash algorithm OID for the challenge generation.</param>
        /// <returns><code>r_d</code>.</returns>
        FieldZqElement GetDeviceResponse(byte[] messageForDevice, byte[] partialChallengeDigest, string hashOID);
    }
}
