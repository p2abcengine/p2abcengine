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
using System.Linq;
using System.Collections.Generic;
using System.Text;
using UProveCrypto.Math;


namespace UProveCrypto
{
    /// <summary>
    /// Provides some helper functions for the U-Prove protocols.
    /// </summary>
    public static class ProtocolHelper
    {
        static Dictionary<SupportedHashFunctions, string> HashOIDMap;

        /// <summary>
        /// Hash functions supported by U-Prove parameter sets.
        /// </summary>
        public enum SupportedHashFunctions { 
            /// <summary> SHA-1 </summary>
            SHA1,
            /// <summary> SHA-256 </summary>
            SHA256,
            /// <summary> SHA-384 </summary>
            SHA384,
            /// <summary> SHA-512 </summary>
            SHA512 
        };

        // security parameter proposed for DSA p:2048/q:256 in http://csrc.nist.gov/publications/fips/fips186-3/fips_186-3.pdf
        // for Miller-Rabin primality testing
        internal const int PrimalityTestingCertainty = 56;

        static ProtocolHelper()
        {
            // initialize the HashOIDMap
            HashOIDMap = new Dictionary<SupportedHashFunctions, string>();
            HashOIDMap.Add(SupportedHashFunctions.SHA1, "1.3.14.3.2.26");
            HashOIDMap.Add(SupportedHashFunctions.SHA256, "2.16.840.1.101.3.4.2.1");
            HashOIDMap.Add(SupportedHashFunctions.SHA384, "2.16.840.1.101.3.4.2.2");
            HashOIDMap.Add(SupportedHashFunctions.SHA512, "2.16.840.1.101.3.4.2.3");
        }

        /// <summary>
        /// Generates the Issuer parameters cryptographic data, <code>ip</code> will be updated with the cryptographic data.
        /// </summary>
        /// <param name="ip">An instanciated Issuer parameters; the Gq and E properties must be set.</param>
        /// <param name="gValues">The issuer generators to use, or null.</param>
        /// <param name="supportDevice">Indicates if the device generator must be generated.</param>
        /// <returns>The Issuer parameters private key.</returns>
        internal static FieldZqElement GenerateIssuerParametersCryptoData(IssuerParameters ip, GroupElement[] gValues, bool supportDevice)
        {
            if (ip == null)
            {
                throw new ArgumentNullException("ip");
            }
            if (ip.Gq == null)
            {
                throw new ArgumentException("Group description is not set");
            }
            int n = ip.E == null ? 0 : ip.E.Length;
            Group Gq = ip.Gq;
            ip.G = new GroupElement[n + 2];

            FieldZqElement privateKey;
            if (gValues == null)
            {
                FieldZqElement[] y = ip.Zq.GetRandomElements(n + 2, false);
                privateKey = y[0];

                for (int i = 0; i < (n + 2); i++)
                {
                    ip.G[i] = Gq.G.Exponentiate(y[i]);
                }
            }
            else
            {
                // g_0
                privateKey = ip.Zq.GetRandomElement(false);
                ip.G[0] = Gq.G.Exponentiate(privateKey);

                // g_1,..,g_n
                for (int i = 1; i < (n + 1); i++)
                {
                    ip.G[i] = gValues[i - 1];
                }

                // g_t
                int t = n + 1;
                ip.G[t] = gValues[gValues.Length - 1];
            }

            if (supportDevice)
            {
                if (ip.Gd == null)
                {
                    ip.Gd = Gq.G.Exponentiate(ip.Zq.GetRandomElement(false));
                }
            }
            return privateKey;
        }

        /// <summary>
        /// Verifies the Issuer parameters.
        /// </summary>
        /// <param name="ip">The Issuer parameters to verify.</param>
        /// <param name="usesRecommededParameters">If true then use recommended parameters.</param>
        public static void VerifyIssuerParameters(IssuerParameters ip, bool usesRecommededParameters)
        {
            Group Gq = ip.Gq;

            ParameterSet set;
            if (!usesRecommededParameters &&
                !ParameterSet.TryGetNamedParameterSet(ip.Gq.GroupName, out set))
            {
                // not a known group, verify it
                ip.Gq.Verify();
            }
            
            // verify public key elements
            ip.Gq.ValidateGroupElement(ip.G[0]);
            if (!usesRecommededParameters)
            {
                for (int i = 1; i < ip.G.Length; i++)
                {
                    ip.Gq.ValidateGroupElement(ip.G[i]);
                }
            }
        }

        internal static FieldZqElement ComputeXt(IssuerParameters ip, byte[] TI, bool supportDevice)
        {
            HashFunction hash = ip.HashFunction;
            hash.Hash((byte) 1);
            hash.Hash(ip.Digest(supportDevice));
            hash.Hash(TI);
            return ip.Zq.GetElementFromDigest(hash.Digest);
        }

        /// <summary>
        /// Computes the value x_i.  
        /// A field element is computed from an attribute value 
        /// </summary>
        /// <param name="ip"> The issuer paramters</param>
        /// <param name="i"> The index of the attribute</param>
        /// <param name="A"> An array contianing the attributes</param>
        /// <returns></returns>
        public static FieldZqElement ComputeXi(IssuerParameters ip, int i, byte[] A)
        {
            FieldZqElement xi;

            if (ip.E[i] == 0x01)            // hash
            {
                if (A == null)
                {
                    xi = ip.Zq.Zero;
                }
                else
                {
                    HashFunction hash = ip.HashFunction;
                    hash.Hash(A);
                    xi = ip.Zq.GetElementFromDigest(hash.Digest);
                }
            }
            else if (ip.E[i] == 0x00)       // do not hash
            {
                if (A == null)
                {
                    throw new ArgumentNullException( "A", "A can't be null when ip.E[i] == 0x00" );
                }
                else
                {
                    xi = ip.Zq.GetElement(A);
                }
            }
            else
            {
                throw new ArgumentException("invalid E[" + i + "] value");
            }

            return xi;
        }

        /// <summary>
        /// Verifies a U-Prove token signature.
        /// </summary>
        /// <param name="ip">The Issuer parameters corresponding to the U-Prove token.</param>
        /// <param name="upt">The U-Prove token to verify.</param>
        /// <exception cref="InvalidUProveArtifactException">If the U-Prove token is invalid.</exception>
        public static void VerifyTokenSignature(IssuerParameters ip, UProveToken upt)
        {
            Group Gq = ip.Gq;
            FieldZq Zq = ip.Zq;

            if (upt.H == Gq.Identity)
            {
                throw new InvalidUProveArtifactException("Invalid U-Prove token (public key H = 1)");
            }

            HashFunction hash = ip.HashFunction;
            hash.Hash(upt.H);
            hash.Hash(upt.PI);
            hash.Hash(upt.SigmaZPrime);
            hash.Hash(Gq.G.Exponentiate(upt.SigmaRPrime) * ip.G[0].Exponentiate(upt.SigmaCPrime.Negate()));
            hash.Hash(upt.H.Exponentiate(upt.SigmaRPrime) * upt.SigmaZPrime.Exponentiate(upt.SigmaCPrime.Negate()));
            if (upt.SigmaCPrime != Zq.GetElementFromDigest(hash.Digest)) 
            {
                throw new InvalidUProveArtifactException("Invalid U-Prove token signature");
            }
        }

        /// <summary>
        /// Computes the U-Prove token identifier.
        /// </summary>
        /// <param name="ip">The issuer parameters associated with <code>upt</code>.</param>
        /// <param name="upt">The U-Prove token from which to compute the identifier.</param>
        /// <returns></returns>
        public static byte[] ComputeTokenID(IssuerParameters ip, UProveToken upt)
        {
            HashFunction hash = ip.HashFunction;
            hash.Hash(upt.H);
            hash.Hash(upt.SigmaZPrime);
            hash.Hash(upt.SigmaCPrime);
            hash.Hash(upt.SigmaRPrime);
            return hash.Digest;
        }

        /// <summary>
        /// Computes the value <c>gamma</c>, an input to the issuance protocol. 
        /// </summary>
        /// <param name="ip">The issuer parameters</param>
        /// <param name="A"> The attribute values, or null if the token contains no attributes </param>
        /// <param name="TI">The token information field</param>
        /// <param name="hd">The device public key, or <c>null</c> if device binding is not supported by the issuer paramters.</param>
        /// <returns>The group element gamma </returns>
        public static GroupElement ComputeIssuanceInput(IssuerParameters ip, byte[][] A, byte[] TI, GroupElement hd)
        {
            if (ip == null)
            {
                throw new ArgumentNullException("Issuer parameters are null");
            }
            int n = 0;
            bool supportDevice = (hd != null);
            if (supportDevice && !ip.IsDeviceSupported)
            {
                throw new InvalidOperationException("Issuer parameters does not support devices");
            }
            if (A != null)
            {
                n = A.Length;
            }
            Group Gq = ip.Gq;
            GroupElement gamma = ip.G[0];
            for (int i = 0; i < n; i++)
            {
                FieldZqElement xi = ComputeXi(ip, i, A[i]);
                gamma = gamma * ip.G[i+1].Exponentiate(xi);
            }
            FieldZqElement xt = ComputeXt(ip, TI, supportDevice);
            gamma = gamma * ip.G[n+1].Exponentiate(xt);

            // Multiply-in the device public key for device-protected tokens
            if (supportDevice)
            {
                gamma = gamma * hd;
            }
            return gamma;
        }

        internal static FieldZqElement GenerateChallenge(IssuerParameters ip, UProveToken upt, byte[] a, int pseudonymIndex, byte[] ap, GroupElement Ps, byte[] m, byte[] md, int[] disclosed, FieldZqElement[] disclosedX, int[] committed, CommitmentValues[] commitments, out byte[] mdPrime)
        {
            bool hasCommitments = (committed != null && committed.Length > 0);
            if (hasCommitments)
            {
                if (committed.Length != commitments.Length)
                {
                    throw new ArgumentException("Inconsistent committed indices and commitment values");
                }
            }
        
            HashFunction hash = ip.HashFunction;
            hash.Hash(ComputeTokenID(ip, upt));
            hash.Hash(a);
            hash.Hash(disclosed);
            hash.Hash(disclosedX);
            if (!hasCommitments)
            {
                hash.HashNull(); // C
                hash.HashNull(); // < {tildeC} >
                hash.HashNull(); // < {tildeA} >
            }
            else
            {
                hash.Hash(committed);
                hash.Hash(commitments.Length); // length of < {tildeC} >
                for (int i = 0; i < commitments.Length; i++)
                {
                    hash.Hash(commitments[i].TildeC);
                }
                hash.Hash(commitments.Length); // length of < {tildeA} >
                for (int i = 0; i < commitments.Length; i++)
                {
                    hash.Hash(commitments[i].TildeA);
                }
            }
            hash.Hash(pseudonymIndex == PresentationProof.DeviceAttributeIndex ? 0 : pseudonymIndex);
            hash.Hash(ap);
            hash.Hash(Ps);
            hash.Hash(m);
            mdPrime = hash.Digest;
            if (upt.IsDeviceProtected)
            {
                hash = ip.HashFunction;
                hash.Hash(md);
                hash.Hash(mdPrime);
                return ip.Zq.GetElementFromDigest(hash.Digest);
            }
            else
            {
                return ip.Zq.GetElementFromDigest(mdPrime);
            }
        }

        internal static FieldZqElement GenerateChallengeForDevice(FieldZq zq, HashFunction hash, byte[] md, byte[] mdPrime)
        {
            hash.Hash(md);
            hash.Hash(mdPrime);
            return zq.GetElementFromDigest(hash.Digest);
        }

        /// <summary>
        /// Return a string representation of the OID for the hash function <c>hashAlg</c>
        /// </summary>
        /// <param name="hashAlg">a hash function</param>
        /// <returns>A string representaiton of the hash function's OID.</returns>
        public static string GetHashOID(SupportedHashFunctions hashAlg)
        {
            return HashOIDMap[hashAlg];
        }

        private const int ScopeElementIndex = 253;
        /// <summary>
        /// Create the group element associated with a scope.
        /// </summary>
        public static GroupElement GenerateScopeElement(Group Gq, byte[] scope)
        {
            int counter;
            return Gq.DeriveElement(scope, ScopeElementIndex, out counter);
        }

        /// <summary>
        /// Concatenate two byte arrays.
        /// </summary>
        /// <returns>A new array containing the concatenation of the inputs.</returns>
        public static byte[] Concatenate(byte[] a, byte[] b)
        {
            if (a == null) {
                return b;
            } else if (b == null) {
                return a;
            }
            byte[] c = new byte[a.Length + b.Length];
            int index = 0;
            System.Buffer.BlockCopy(a, 0, c, index, a.Length);
            index += a.Length;
            System.Buffer.BlockCopy(b, 0, c, index, b.Length);
            return c;
        }
    }
}
