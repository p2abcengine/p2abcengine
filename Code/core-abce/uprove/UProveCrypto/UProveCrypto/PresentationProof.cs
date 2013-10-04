//*********************************************************
//
//    Copyright (c) Microsoft. All rights reserved.
//
//    THIS CODE IS PROVIDED *AS IS* WITHOUT WARRANTY OF
//    ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING ANY
//    IMPLIED WARRANTIES OF FITNESS FOR A PARTICULAR
//    PURPOSE, MERCHANTABILITY, OR NON-INFRINGEMENT.
//
//*********************************************************

using System;
using System.Linq;
using System.Runtime.Serialization;
using System.ComponentModel;
using UProveCrypto.Math;


namespace UProveCrypto
{
    /// <summary>
    /// Specifies the random data for the presentation proof generation, if provided externally.
    /// Typically this data is generated at presentation time, and this mechanism is only used
    /// for testing (against test vectors).
    /// </summary>
    public class ProofGenerationRandomData
    {
        private FieldZqElement w0;
        private FieldZqElement[] w;
        private FieldZqElement wd;
        private FieldZqElement[] tildeO;
        private FieldZqElement[] tildeW;

        /// <summary>
        /// Constructs a new <code>ProofGenerationRandomData</code> instance.
        /// </summary>
        /// <param name="w0">The <code>w0</code> value.</param>
        /// <param name="w">The <code>w</code> values.</param>
        /// <param name="wd">The <code>wd</code> value if the token is Device-protected; null otherwise.</param>
        /// <param name="tildeO">The <code>tildeO</code> values (if attribute commitments are generated).</param>
        /// <param name="tildeW">The <code>tildeW</code> values (if attribute commitments are generated).</param>
        public ProofGenerationRandomData(FieldZqElement w0, FieldZqElement[] w, FieldZqElement wd, FieldZqElement[] tildeO, FieldZqElement[] tildeW)
        {
            this.w0 = w0;
            this.w = w;
            this.wd = wd;
            this.tildeO = tildeO;
            this.tildeW = tildeW;
        }

        /// <summary>
        /// Gets the <code>w0</code> value.
        /// </summary>
        public FieldZqElement W0
        {
            get { return w0; }
        }

        /// <summary>
        /// Gets the <code>w</code> values.
        /// </summary>
        public FieldZqElement[] W
        {
            get { return w; }
        }

        /// <summary>
        /// Gets the <code>wd</code> value.
        /// </summary>
        public FieldZqElement Wd
        {
            get { return wd; }
        }

        /// <summary>
        /// Gets the <code>tildeO</code> values.
        /// </summary>
        public FieldZqElement[] TildeO
        {
            get { return tildeO; }
        }

        /// <summary>
        /// Gets the <code>tildeW</code> values.
        /// </summary>
        public FieldZqElement[] TildeW
        {
            get { return tildeW; }
        }

        /// <summary>
        /// Clears the object.
        /// Note that this method does not guarantee the memory will be securely zeroized. 
        /// </summary>
        public void Clear()
        {
            w0 = null;
            if (w != null)
            {
                Array.Clear(w, 0, w.Length);
                w = null;
            }
            wd = null;
            if (tildeO != null)
            {
                Array.Clear(tildeO, 0, tildeO.Length);
                tildeO = null;
            }
            if (tildeW != null)
            {
                Array.Clear(tildeW, 0, tildeW.Length);
                tildeW = null;
            }
        }

        /// <summary>
        /// Generates a <code>ProofGenerationRandomData</code> instance using the internal RNG.
        /// </summary>
        /// <param name="numUndisclosed">Number of undisclosed attributes.</param>
        /// <param name="numCommitted">Number of committed attributes.</param>
        /// <param name="Zq">Field Zq</param>
        /// <param name="isDeviceProtected">True if a toke is device-protected.</param>
        /// <returns>A pregenerated set of random values.</returns>
        internal static ProofGenerationRandomData Generate(int numUndisclosed, int numCommitted, FieldZq Zq, bool isDeviceProtected)
        {
            return new ProofGenerationRandomData(
                Zq.GetRandomElement(false), 
                Zq.GetRandomElements(numUndisclosed, false), 
                isDeviceProtected ? Zq.GetRandomElement(false) : null,
                numCommitted > 0 ? Zq.GetRandomElements(numCommitted, false) : null,
                numCommitted > 0 ? Zq.GetRandomElements(numCommitted, false) : null
                );
        }
    }
    
    /// <summary>
    /// Contains the commitment values.
    /// </summary>
    public class CommitmentValues
    {
        /// <summary>
        /// Constructs a new instance.
        /// </summary>
        /// <param name="tildeC">The <code>tildeC</code> value.</param>
        /// <param name="tildeA">The <code>tildeA</code> value.</param>
        /// <param name="tildeR">The <code>tildeR</code> value.</param>
        public CommitmentValues(GroupElement tildeC, byte[] tildeA, FieldZqElement tildeR)
        {
            TildeC = tildeC;
            TildeA = tildeA;
            TildeR = tildeR;
        }

        /// <summary>
        /// Gets or sets the <code>tildeC</code> value.
        /// </summary>
        public GroupElement TildeC { get; set; }
        
        /// <summary>
        /// Gets or sets the <code>tildeA</code> value.
        /// </summary>
        public byte[] TildeA { get; set; }

        /// <summary>
        /// Gets or sets the <code>tildeR</code> value.
        /// </summary>
        public FieldZqElement TildeR { get; set; }

        /// <summary>
        /// Compare two presentation proofs.
        /// </summary>
        /// <param name="o">An object to compare against this presentation proof.</param>
        /// <returns>True if equal, false otherwise</returns>
        public override bool Equals(Object o)
        {
            if (o == null) { return false; }
            CommitmentValues cv = o as CommitmentValues;
            if ((System.Object)cv == null)
            {
                return false;
            }
            return TildeC.Equals(cv.TildeC) && TildeA.SequenceEqual(cv.TildeA) && TildeR.Equals(cv.TildeR);
        }

        /// <summary>
        /// Get the hash code for this PresentationProof
        /// </summary>
        /// <returns>the hash code</returns>
        public override int GetHashCode()
        {
            int hashCode = 0;

            foreach (byte b in this.TildeA)
            {
                hashCode ^= b;
            }

            return hashCode;
        }
    }

    /// <summary>
    /// Contains the commitment private values used by the Prover
    /// to perform further proofs on the committed values.
    /// </summary>
    public class CommitmentPrivateValues
    {
        /// <summary>
        /// The <code>tildeO</code> value of the commitments.
        /// </summary>
        public FieldZqElement[] TildeO { get; set; }

        /// <summary>
        /// Constructs a new instance.
        /// </summary>
        /// <param name="tildeO">The <code>tildeO</code> value of the commitments.</param>
        public CommitmentPrivateValues(FieldZqElement[] tildeO)
        {
            TildeO = tildeO;
        }
    }
    
    /// <summary>
    /// Represents a presentation proof.
    /// </summary>
    [DataContract]
    public class PresentationProof
    {
        /// <summary> A constant to indicate an attribute is stored on the device</summary>
        public readonly static int DeviceAttributeIndex = int.MaxValue;

        private byte[][] disclosedAttributes;
        private byte[] a;
        private byte[] ap;
        private GroupElement ps;
        private FieldZqElement[] r;
        private CommitmentValues[] commitments;

        /// <summary>
        /// Constructs a new <code>PresentationProof</code> instance.
        /// </summary>
        public PresentationProof()
        {
        }

        /// <summary>
        /// Constructs a new <code>PresentationProof</code> instance.
        /// </summary>
        /// <param name="disclosedAttributes">The disclosed attributes.</param>
        /// <param name="a">The <code>a</code> value.</param>
        /// <param name="ap">The <code>ap</code> value.</param>
        /// <param name="ps">The <code>Ps</code> value.</param>
        /// <param name="r">The <code>r</code> values.</param>
        /// <param name="commitments">The commitments values.</param>
        public PresentationProof(byte[][] disclosedAttributes, byte[] a, byte[] ap, GroupElement ps, FieldZqElement[] r, CommitmentValues[] commitments)
        {
            this.disclosedAttributes = disclosedAttributes;
            this.a = a;
            this.ap = ap;
            this.ps = ps;
            this.r = r;
            this.commitments = commitments;
        }

        /// <summary>
        /// Gets or sets the disclosed attributes.
        /// </summary>
        public byte[][] DisclosedAttributes
        {
            get { return disclosedAttributes; }
            set { disclosedAttributes = value; }
        }

        /// <summary>
        /// Gets or sets the <code>a</code> value.
        /// </summary>
        public byte[] A
        {
            get { return a; }
            set { a = value; }
        }

        /// <summary>
        /// Gets or sets the <code>ap</code> value.
        /// </summary>
        public byte[] Ap
        {
            get { return ap; }
            set { ap = value; }
        }

        /// <summary>
        /// Gets or sets the <code>Ps</code> value.
        /// </summary>
        public GroupElement Ps
        {
            get { return ps; }
            set { ps = value; }
        }

        /// <summary>
        /// Gets or sets the <code>r</code> values.
        /// </summary>
        public FieldZqElement[] R
        {
            get { return r; }
            set { r = value; }
        }

        /// <summary>
        /// Gets or sets the commitment values.
        /// </summary>
        public CommitmentValues[] Commitments
        {
            get { return commitments; }
            set { commitments = value; }
        }

        /// <summary>
        /// Generates a presentation proof including optionally presenting a pseudonym, creating attribute commitments, and passing pre-generated random values.
        /// </summary>
        /// <param name="ip">The issuer parameters corresponding to <code>upkt</code>.</param>
        /// <param name="disclosed">An ordered array of disclosed attribute indices.</param>
        /// <param name="committed">An ordered array of committed attribute indices.</param>
        /// <param name="pseudonymAttribIndex">Index of the attribute used to create a scope-exclusive pseudonym, or 0 if no pseudonym is to be presented. The index must not be part of the disclosed attributes.</param>
        /// <param name="gs">The pseudonym scope element, or null if no pseudonym is to be presented.</param>
        /// <param name="message">The presentation message.</param>
        /// <param name="messageD">The message for the Device, or null.</param>
        /// <param name="deviceContext">The active device context, if token is device-protected, or null.</param>
        /// <param name="upkt">The U-Proke key and token.</param>
        /// <param name="attributes">The token attributes.</param>
        /// <param name="preGenW">Optional pregenerated random data for the proof generation.</param>
        /// <param name="cpv">Returned commitment private values if commitments are computed.</param>
        /// <returns>A presentation proof.</returns>
        internal static PresentationProof Generate(IssuerParameters ip, int[] disclosed, int[] committed, int pseudonymAttribIndex, GroupElement gs, byte[] message, byte[] messageD, IDevicePresentationContext deviceContext, UProveKeyAndToken upkt, byte[][] attributes, ProofGenerationRandomData preGenW, out CommitmentPrivateValues cpv)
        {
            if (upkt.Token.IsDeviceProtected && deviceContext == null)
            {
                throw new ArgumentNullException("Device context is not initialized");
            }
            bool generateCommitments = (committed != null && committed.Length > 0);
            FieldZqElement[] tildeO = null;

            // make sure disclosed and committed lists are sorted
            if (disclosed == null)
            {
                // can't be null later, so make it an empty array
                disclosed = new int[] { };
            }
            Array.Sort(disclosed);
            if (generateCommitments)
            {
                Array.Sort(committed);
            }

            int n = 0;
            if (ip.E != null)
                n = ip.E.Length;

            bool presentPseudonym = false;

            if (gs != null)
            {
                if (pseudonymAttribIndex < 1 || (pseudonymAttribIndex > n && pseudonymAttribIndex != DeviceAttributeIndex))
                {
                    throw new ArgumentException("pseudonymAttribIndex must be between 1 and " + n + " (inclusive)");
                }
                if (disclosed.Contains(pseudonymAttribIndex))
                {
                    throw new ArgumentException("pseudonymAttribIndex cannot be in the disclosed attribute array");
                }
                presentPseudonym = true;
            }
            else if (pseudonymAttribIndex > 0)
            {
                throw new ArgumentNullException("gs is null");
            }
            else
            {
                pseudonymAttribIndex = 0;
            }

            Group Gq = ip.Gq;
            FieldZq Zq = ip.Zq;

            FieldZqElement xt = ProtocolHelper.ComputeXt(ip, upkt.Token.TI, upkt.Token.IsDeviceProtected);
            ProofGenerationRandomData random;
            if (preGenW == null)
            {
                random = ProofGenerationRandomData.Generate(n - disclosed.Length, generateCommitments ? committed.Length : 0, Zq, upkt.Token.IsDeviceProtected);
            }
            else
            {
                random = preGenW;
            }

            FieldZqElement[] x = new FieldZqElement[n];
            GroupElement temp = upkt.Token.H.Exponentiate(random.W0);
            int uIndex = 0;
            int dIndex = 0;
            int cIndex = 0;
            PresentationProof proof = new PresentationProof();
            proof.DisclosedAttributes = new byte[disclosed.Length][];
            int pseudonymRandomizerIndex = 0;
            if (generateCommitments)
            {
                proof.Commitments = new CommitmentValues[committed.Length];
                tildeO = new FieldZqElement[committed.Length];
            }
            HashFunction hash = ip.HashFunction;
            for (int i = 0; i < n; i++)
            {
                x[i] = ProtocolHelper.ComputeXi(ip, i, attributes[i]);
                if (!disclosed.Contains(i + 1))
                {
                    temp = temp * ip.G[i + 1].Exponentiate(random.W[uIndex]);
                    if (presentPseudonym)
                    {
                        if (pseudonymAttribIndex == (i + 1))
                        {
                            pseudonymRandomizerIndex = uIndex;
                        }
                    }

                    if (generateCommitments && committed.Contains(i + 1))
                    {
                        GroupElement tildeC = ip.Gq.G.Exponentiate(x[i]) * ip.G[1].Exponentiate(random.TildeO[cIndex]);
                        tildeO[cIndex] = random.TildeO[cIndex];
                        GroupElement temp2 = ip.Gq.G.Exponentiate(random.W[uIndex]) * ip.G[1].Exponentiate(random.TildeW[cIndex]);
                        hash.Hash(temp2);
                        byte[] tildeA = hash.Digest;
                        proof.Commitments[cIndex] = new CommitmentValues(tildeC, tildeA, null);

                        cIndex++;
                    }

                    uIndex++;
                }
                else if (generateCommitments && committed.Contains(i + 1))
                {
                    throw new ArgumentException("attribute " + (i + 1) + " cannot be both disclosed and committed");
                }
                else
                {
                    proof.DisclosedAttributes[dIndex] = attributes[i];
                    dIndex++;
                }
            }
            if (upkt.Token.IsDeviceProtected)
            {
                GroupElement ad;
                // pseudonym computed by device
                if (presentPseudonym && pseudonymAttribIndex == DeviceAttributeIndex)
                {
                    GroupElement apPrime;
                    GroupElement Ps;
                    ad = deviceContext.GetInitialWitnessesAndPseudonym(gs, out apPrime, out Ps);
                    hash.Hash(apPrime * gs.Exponentiate(random.Wd));
                    proof.Ap = hash.Digest;
                    proof.Ps = Ps;
                }
                else
                {
                    ad = deviceContext.GetInitialWitness();
                }
                temp = temp * ip.Gd.Exponentiate(random.Wd) * ad;
            }
            hash.Hash(temp);
            proof.a = hash.Digest;

            // pseudonym derived from one token attribute
            if (presentPseudonym && pseudonymAttribIndex != DeviceAttributeIndex)
            {
                hash.Hash(gs.Exponentiate(random.W[pseudonymRandomizerIndex]));
                proof.Ap = hash.Digest;
                proof.Ps = gs.Exponentiate(x[pseudonymAttribIndex - 1]);
            }

            byte[] mdPrime;
            FieldZqElement c = ProtocolHelper.GenerateChallenge(ip, upkt.Token, proof.a, pseudonymAttribIndex, proof.ap, proof.Ps, message, messageD, disclosed, GetDisclosedX(disclosed, x), committed, proof.Commitments, out mdPrime);
            proof.r = new FieldZqElement[1 + n - disclosed.Length + (upkt.Token.IsDeviceProtected ? 1 : 0)]; // r_0, {r_i} for undisclosed i, r_d
            proof.r[0] = c * upkt.PrivateKey + random.W0;
            uIndex = 1;
            for (int i = 1; i <= n; i++)
            {
                if (!disclosed.Contains(i))
                {
                    proof.r[uIndex] = c.Negate() * x[i - 1] + random.W[uIndex - 1];
                    uIndex++;
                }
            }
            if (upkt.Token.IsDeviceProtected)
            {
                proof.r[proof.r.Length - 1] = deviceContext.GetDeviceResponse(messageD, mdPrime, ip.HashFunctionOID) + random.Wd;
            }
            if (generateCommitments)
            {
                for (int i = 0; i < committed.Length; i++)
                {
                    proof.Commitments[i].TildeR = c.Negate() * random.TildeO[i] + random.TildeW[i];
                }
            }

            random.Clear();
            cpv = new CommitmentPrivateValues(tildeO);
            return proof;
        }

        /// <summary>
        /// Generates a presentation proof including optionally presenting a pseudonym, creating attribute commitments, and passing pre-generated random values.
        /// </summary>
        /// <param name="ip">The issuer parameters corresponding to <code>upkt</code>.</param>
        /// <param name="disclosed">An ordered array of disclosed attribute indices.</param>
        /// <param name="committed">An ordered array of committed attribute indices.</param>
        /// <param name="pseudonymAttribIndex">Index of the attribute used to create a scope-exclusive pseudonym, or 0 if no pseudonym is to be presented. The index must not be part of the disclosed attributes.</param>
        /// <param name="gs">The pseudonym scope element, or null if no pseudonym is to be presented.</param>
        /// <param name="message">The presentation message.</param>
        /// <param name="messageD">The message for the Device, or null.</param>
        /// <param name="deviceContext">The active device context, if token is device-protected, or null.</param>
        /// <param name="upkt">The U-Proke key and token.</param>
        /// <param name="attributes">The token attributes.</param>
        /// <param name="preGenW">Optional pregenerated random data for the proof generation.</param>
        /// <param name="tildeO">Returned tildeO values if commitments are computed.</param>
        /// <returns>A presentation proof.</returns>
        [Obsolete("Use Generate(ProverPresentationProtocolParameters pppp, out CommitmentPrivateValues cpv)")]        
        public static PresentationProof Generate(IssuerParameters ip, int[] disclosed, int[] committed, int pseudonymAttribIndex, GroupElement gs, byte[] message, byte[] messageD, IDevicePresentationContext deviceContext, UProveKeyAndToken upkt, byte[][] attributes, ProofGenerationRandomData preGenW, out FieldZqElement[] tildeO)
        {
            CommitmentPrivateValues cpv;
            PresentationProof proof = Generate(ip, disclosed, committed, pseudonymAttribIndex, gs, message, messageD, deviceContext, upkt, attributes, preGenW, out cpv);
            tildeO = cpv.TildeO;
            return proof;
        }

        /// <summary>
        /// Generates a presentation proof including optionally presenting a pseudonym, creating attribute commitments, and passing pre-generated random values.
        /// </summary>
        /// <param name="ip">The issuer parameters corresponding to <code>upkt</code>.</param>
        /// <param name="disclosed">An ordered array of disclosed attribute indices.</param>
        /// <param name="committed">An ordered array of committed attribute indices.</param>
        /// <param name="pseudonymAttribIndex">Index of the attribute used to create a scope-exclusive pseudonym, or 0 if no pseudonym is to be presented. The index must not be part of the disclosed attributes.</param>
        /// <param name="pseudonymScope">The pseudonym scope, or null if no pseudonym is to be presented.</param>
        /// <param name="message">The presentation message.</param>
        /// <param name="messageD">The message for the Device, or null.</param>
        /// <param name="deviceContext">The active device context, if token is device-protected, or null.</param>
        /// <param name="upkt">The U-Proke key and token.</param>
        /// <param name="attributes">The token attributes.</param>
        /// <param name="preGenW">Optional pregenerated random data for the proof generation.</param>
        /// <param name="tildeO">Returned tildeO values if commitments are computed.</param>
        /// <returns>A presentation proof.</returns>
        [Obsolete("Use Generate(ProverPresentationProtocolParameters pppp, out CommitmentPrivateValues cpv)")]
        public static PresentationProof Generate(IssuerParameters ip, int[] disclosed, int[] committed, int pseudonymAttribIndex, byte[] pseudonymScope, byte[] message, byte[] messageD, IDevicePresentationContext deviceContext, UProveKeyAndToken upkt, byte[][] attributes, ProofGenerationRandomData preGenW, out FieldZqElement[] tildeO)
        {
            GroupElement gs = ProtocolHelper.GenerateScopeElement(ip.Gq, pseudonymScope);
            return Generate(ip, disclosed, committed, pseudonymAttribIndex, gs, message, messageD, deviceContext, upkt, attributes, preGenW, out tildeO); 
        }

        /// <summary>
        /// Generates a presentation proof including optionally presenting a pseudonym, and creating attribute commitments.
        /// </summary>
        /// <param name="ip">The issuer parameters corresponding to <code>upkt</code>.</param>
        /// <param name="disclosed">An ordered array of disclosed attribute indices.</param>
        /// <param name="committed">An ordered array of committed attribute indices.</param>
        /// <param name="pseudonymAttribIndex">Index of the attribute used to create a scope-exclusive pseudonym, or 0 if no pseudonym is to be presented. The index must not be part of the disclosed attributes.</param>
        /// <param name="pseudonymScope">The pseudonym scope, or null if no pseudonym is to be presented.</param>
        /// <param name="message">The presentation message.</param>
        /// <param name="messageD">The message for the Device, or null.</param>
        /// <param name="deviceContext">The active device context, if token is device-protected, or null.</param>
        /// <param name="upkt">The U-Proke key and token.</param>
        /// <param name="attributes">The token attributes.</param>
        /// <param name="tildeO">Returned tildeO values if commitments are computed.</param>
        /// <returns>A presentation proof.</returns>
        [Obsolete("Use Generate(ProverPresentationProtocolParameters pppp, out CommitmentPrivateValues cpv)")]        
        public static PresentationProof Generate(IssuerParameters ip, int[] disclosed, int[] committed, int pseudonymAttribIndex, byte[] pseudonymScope, byte[] message, byte[] messageD, IDevicePresentationContext deviceContext, UProveKeyAndToken upkt, byte[][] attributes, out FieldZqElement[] tildeO)
        {
            return Generate(ip, disclosed, committed, pseudonymAttribIndex, pseudonymScope, message, messageD, deviceContext, upkt, attributes, null, out tildeO);
        }

        /// <summary>
        /// Generates a presentation proof.
        /// </summary>
        /// <param name="ip">The issuer parameters corresponding to <code>upkt</code>.</param>
        /// <param name="disclosed">An ordered array of disclosed attribute indices.</param>
        /// <param name="message">The presentation message.</param>
        /// <param name="messageD">The message for the Device, or null.</param>
        /// <param name="deviceContext">The active device context, if token is device-protected, or null.</param>
        /// <param name="upkt">The U-Proke key and token.</param>
        /// <param name="attributes">The token attributes.</param>
        /// <returns>A presentation proof.</returns>
        [Obsolete("Use Generate(ProverPresentationProtocolParameters pppp)")]        
        public static PresentationProof Generate(IssuerParameters ip, int[] disclosed, byte[] message, byte[] messageD, IDevicePresentationContext deviceContext, UProveKeyAndToken upkt, byte[][] attributes)
        {
            GroupElement gs = null;
            FieldZqElement[] unused;
            return Generate(ip, disclosed, null, 0, gs, message, messageD, deviceContext, upkt, attributes, null, out unused);
        }

        /// <summary>
        /// Generates a presentation proof.
        /// </summary>
        /// <param name="pppp">The Prover presentation parameters.</param>
        /// <param name="cpv">Returned commitment private values if commitments are computed.</param>
        /// <returns>A presentation proof.</returns>
        public static PresentationProof Generate(ProverPresentationProtocolParameters pppp, out CommitmentPrivateValues cpv)
        {
            return Generate(pppp.IP, pppp.Disclosed, pppp.Committed, pppp.PseudonymAttributeIndex, pppp.PseudonymScopeElement, pppp.Message, pppp.DeviceMessage, pppp.DeviceContext, pppp.KeyAndToken, pppp.Attributes, pppp.RandomData, out cpv);
        }

        /// <summary>
        /// Generates a presentation proof.
        /// </summary>
        /// <param name="pppp">The Prover presentation parameters.</param>
        /// <returns>A presentation proof.</returns>
        public static PresentationProof Generate(ProverPresentationProtocolParameters pppp)
        {
            CommitmentPrivateValues unused;
            if (pppp.Committed != null && pppp.Committed.Length > 0)
            {
                throw new ArgumentException("The ProverPresentationProtocolParameters's Committed attribute cannot be used with this method. Use Generate(ProverPresentationProtocolParameters pppp, out CommitmentPrivateValues cpv) instead.");
            }
            return Generate(pppp, out unused);
        }

        // Helper method for Generate(...)
        private static FieldZqElement[] GetDisclosedX(int[] disclosed, FieldZqElement[] x)
        {
            FieldZqElement[] disclosedX = new FieldZqElement[disclosed.Length];
            int index = 0;
            foreach (int i in disclosed)
            {
                disclosedX[index++] = x[i - 1];
            }
            return disclosedX;
        }

        /// <summary>
        /// Verifies a presentation proof.
        /// </summary>
        /// <param name="ip">The issuer parameters associated with <code>upt</code>.</param>
        /// <param name="disclosed">An ordered array of disclosed attribute indices.</param>
        /// <param name="message">The presentation message.</param>
        /// <param name="messageD">The message for the Device, or null.</param>
        /// <param name="upt">The U-Prove token.</param>
        /// <exception cref="InvalidUProveArtifactException">Thrown if the proof is invalid.</exception>
        [Obsolete("Use Verify(VerifierPresentationProtocolParameters vppp)")]
        public void Verify(IssuerParameters ip, int[] disclosed, byte[] message, byte[] messageD, UProveToken upt)
        {
            GroupElement gs = null;
            Verify(ip, disclosed, null, 0, gs, message, messageD, upt);
        }

        /// <summary>
        /// Verifies a presentation proof with a presented pseudonym and committed values.
        /// </summary>
        /// <param name="ip">The issuer parameters associated with <code>upt</code>.</param>
        /// <param name="disclosed">An ordered array of disclosed attribute indices.</param>
        /// <param name="committed">An ordered array of committed attribute indices.</param>
        /// <param name="pseudonymAttribIndex">Index of the attribute used to create a scope-exclusive pseudonym, or 0 if no pseudonym is to be presented. The index must not be part of the disclosed attributes.</param>
        /// <param name="gs">The pseudonym scope element, or null if no pseudonym is to be presented.</param>
        /// <param name="message">The presentation message.</param>
        /// <param name="messageD">The message for the Device, or null.</param>
        /// <param name="upt">The U-Prove token.</param>
        /// <exception cref="InvalidUProveArtifactException">Thrown if the proof is invalid.</exception>
        [Obsolete("Use Verify(VerifierPresentationProtocolParameters vppp)")]
        public void Verify(IssuerParameters ip, int[] disclosed, int[] committed, int pseudonymAttribIndex, GroupElement gs, byte[] message, byte[] messageD, UProveToken upt)
        {
            try
            {
                // make sure disclosed list is sorted
                if (disclosed == null)
                {
                    // can't be null later, so make it an empty array
                    disclosed = new int[] { };
                }
                Array.Sort(disclosed);
                Group Gq = ip.Gq;
                int n = ip.E.Length;

                bool presentPseudonym = false;
                if (gs != null && pseudonymAttribIndex != 0)
                {
                    if (pseudonymAttribIndex < 1 || (pseudonymAttribIndex > n && pseudonymAttribIndex != DeviceAttributeIndex))
                    {
                        throw new ArgumentException("pseudonymAttribIndex must be between 1 and " + n + " (inclusive)");
                    }
                    if (disclosed.Contains(pseudonymAttribIndex))
                    {
                        throw new ArgumentException("pseudonymAttribIndex cannot be in the disclosed attribute array");
                    }
                    presentPseudonym = true;
                }
                else // no nym
                {
                    pseudonymAttribIndex = 0;
                }
                bool verifyCommitments = (committed != null && committed.Length > 0);
                if (verifyCommitments)
                {
                    Array.Sort(committed);
                }

                ProtocolHelper.VerifyTokenSignature(ip, upt);
                FieldZqElement[] disclosedX = new FieldZqElement[disclosedAttributes.Length];
                GroupElement dAccumulator = ip.G[0] * ip.G[n + 1].Exponentiate(ProtocolHelper.ComputeXt(ip, upt.TI, upt.IsDeviceProtected));     // g0 * gt^xt
                GroupElement uAccumulator = upt.H.Exponentiate(this.r[0]);
                int dIndex = 0;
                int uIndex = 1;
                int cIndex = 0;
                int pseudonymResponseIndex = 0;
                int[] commitmentResponseIndices = verifyCommitments ? new int[committed.Length] : null;
                for (int i = 1; i <= n; i++)
                {
                    if (disclosed.Contains(i))
                    {
                        disclosedX[dIndex] = ProtocolHelper.ComputeXi(ip, i - 1, disclosedAttributes[dIndex]);
                        dAccumulator = dAccumulator * ip.G[i].Exponentiate(disclosedX[dIndex]);
                        dIndex++;
                    }
                    else
                    {
                        uAccumulator = uAccumulator * ip.G[i].Exponentiate(this.r[uIndex]);
                        if (presentPseudonym)
                        {
                            if (pseudonymAttribIndex == i)
                            {
                                pseudonymResponseIndex = uIndex;
                            }
                        }
                        if (verifyCommitments)
                        {
                            if (committed.Contains(i))
                            {
                                // remember which response correspond to which commitment
                                commitmentResponseIndices[cIndex] = uIndex;
                                cIndex++;
                            }
                        }
                        uIndex++;
                    }
                }
                if (pseudonymAttribIndex == DeviceAttributeIndex)
                {
                    pseudonymResponseIndex = this.r.Length - 1; // r_d is the last response in the array
                }

                byte[] unused; // verifier doesn't use the returned message for device
                FieldZqElement c = ProtocolHelper.GenerateChallenge(ip, upt, this.a, pseudonymAttribIndex, this.ap, this.ps, message, messageD, disclosed, disclosedX, committed, this.Commitments, out unused);

                HashFunction hash = ip.HashFunction;
                hash.Hash((dAccumulator.Exponentiate(c.Negate()) * uAccumulator * (upt.IsDeviceProtected ? ip.Gd.Exponentiate(this.r[this.r.Length -1]) : Gq.Identity)));
                if (!this.a.SequenceEqual(hash.Digest))
                {
                    throw new InvalidUProveArtifactException("Invalid presentation proof");
                }

                if (presentPseudonym)
                {
                    hash.Hash(this.ps.Exponentiate(c).Multiply(gs.Exponentiate(this.r[pseudonymResponseIndex])));
                    if (!this.ap.SequenceEqual(hash.Digest))
                    {
                        throw new InvalidUProveArtifactException("Invalid pseudonym");
                    }
                }

                if (verifyCommitments)
                {
                    for (int i = 0; i < commitmentResponseIndices.Length; i++)
                    {
                        CommitmentValues commitment = this.Commitments[i];
                        hash.Hash(commitment.TildeC.Exponentiate(c).Multiply(ip.Gq.G.Exponentiate(this.r[commitmentResponseIndices[i]])).Multiply(ip.G[1].Exponentiate(commitment.TildeR)));
                        if (!commitment.TildeA.SequenceEqual(hash.Digest))
                        {
                            throw new InvalidUProveArtifactException("Invalid commitment " + committed[i]);
                        }
                    }
                }
            }
            catch (ArgumentException)
            {
                throw new InvalidUProveArtifactException("Invalid presentation proof");
            }
            catch (IndexOutOfRangeException)
            {
                throw new InvalidUProveArtifactException("Invalid presentation proof");
            }
        }

        /// <summary>
        /// Verifies a presentation proof with a presented pseudonym and committed values.
        /// </summary>
        /// <param name="ip">The issuer parameters associated with <code>upt</code>.</param>
        /// <param name="disclosed">An ordered array of disclosed attribute indices.</param>
        /// <param name="committed">An ordered array of committed attribute indices.</param>
        /// <param name="pseudonymAttribIndex">Index of the attribute used to create a scope-exclusive pseudonym, or 0 if no pseudonym is to be presented. The index must not be part of the disclosed attributes.</param>
        /// <param name="pseudonymScope">The pseudonym scope, or null if no pseudonym is to be presented.</param>
        /// <param name="message">The presentation message.</param>
        /// <param name="messageD">The message for the Device, or null.</param>
        /// <param name="upt">The U-Prove token.</param>
        /// <exception cref="InvalidUProveArtifactException">Thrown if the proof is invalid.</exception>
        [Obsolete("Use Verify(VerifierPresentationProtocolParameters vppp)")]
        public void Verify(IssuerParameters ip, int[] disclosed, int[] committed, int pseudonymAttribIndex, byte[] pseudonymScope, byte[] message, byte[] messageD, UProveToken upt)
        {
            GroupElement gs = ProtocolHelper.GenerateScopeElement(ip.Gq, pseudonymScope);
            Verify(ip, disclosed, committed, pseudonymAttribIndex, gs, message, messageD, upt);
        }
        

        /// <summary>
        /// Verifies a presentation proof.
        /// </summary>
        /// <param name="vppp">The verifier presentation protocol parameters.</param>
        /// <exception cref="InvalidUProveArtifactException">Thrown if the proof is invalid.</exception>
        public void Verify(VerifierPresentationProtocolParameters vppp)
        {
#pragma warning disable 0618        // this call is deprecated in the public API
            Verify(vppp.IP, vppp.Disclosed, vppp.Committed, vppp.PseudonymAttributeIndex, vppp.PseudonymScopeElement, vppp.Message, vppp.DeviceMessage, vppp.Token);
#pragma warning restore 0618
        }

        #region Serialization

        [DataMember(Name = "D", Order = 1)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string[] _disclosedAttributes;

        [DataMember(Name = "a", Order = 2)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _a;

        [DataMember(Name = "ap", Order = 3, EmitDefaultValue = false)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _ap;

        [DataMember(Name = "Ps", Order = 4, EmitDefaultValue = false)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string _ps;

        [DataMember(Name = "r", Order = 5)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string[] _r;

        [DataMember(Name = "tc", Order = 6, EmitDefaultValue = false)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string[] _tc;

        [DataMember(Name = "ta", Order = 7, EmitDefaultValue = false)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string[] _ta;

        [DataMember(Name = "tr", Order = 8, EmitDefaultValue = false)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal string[] _tr;

        [OnSerializing]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnSerializing(StreamingContext context)
        {
            _disclosedAttributes = new string[this.DisclosedAttributes.Length];
            for (int i = 0; i < this.DisclosedAttributes.Length; i++)
            {
                _disclosedAttributes[i] = this.DisclosedAttributes[i].ToBase64String();
            }
            _a = this.A.ToBase64String();
            _r = new string[this.R.Length];
            for (int i = 0; i < this.R.Length; i++)
            {
                _r[i] = this.R[i].ToBase64String(); 
            }
            if (this.Ap != null)
            {
                _ap = this.Ap.ToBase64String();
            }
            if (this.Ps != null)
            {
                _ps = this.Ps.ToBase64String();
            }
            if (this.Commitments != null)
            {
                int size = this.Commitments.Length;
                _tc = new string[size];
                _ta = new string[size];
                _tr = new string[size];
                for (int i = 0; i < size; i++)
                {
                    _tc[i] = this.Commitments[i].TildeC.ToBase64String();
                    _ta[i] = this.Commitments[i].TildeA.ToBase64String();
                    _tr[i] = this.Commitments[i].TildeR.ToBase64String();
                }
            }
        }

        [OnDeserialized]
        [EditorBrowsable(EditorBrowsableState.Never)]
        internal void OnDeserialized(StreamingContext context)
        {
            if (_disclosedAttributes == null)
            {
                throw new UProveSerializationException("D");
            }

            if (_a == null)
            {
                throw new UProveSerializationException("a");
            }

            if (_r == null)
            {
                throw new UProveSerializationException("r");
            }

            if ((_ap == null && _ps != null) || (_ap != null && _ps == null))
            {
                throw new UProveSerializationException("Ps and ap must either both be set or both be empty");
            }

            bool hasCommitments = false;
            if (_ta != null || _tc != null || _tr != null)
            {
                if (_ta == null || _tc == null || _tr == null || _ta.Length != _tc.Length || _ta.Length != _tr.Length)
                {
                    throw new UProveSerializationException("Inconsistent commitment values");
                }
                hasCommitments = true;
            }

            this.DisclosedAttributes = new byte[_disclosedAttributes.Length][];
            for (int i = 0; i < _disclosedAttributes.Length; i++)
            {
                this.DisclosedAttributes[i] = _disclosedAttributes[i].ToByteArray();
            }
            this.A = _a.ToByteArray();
            this.R = new FieldZqElement[_r.Length];
            for (int i = 0; i < _r.Length; i++)
            {
                this.R[i] = _r[i].ToFieldZqElement(Serializer.ip.Zq);
            }
            if (_ap != null)
            {
                this.Ap = _ap.ToByteArray();
            }
            if (_ps != null)
            {
                this.Ps = _ps.ToGroupElement(Serializer.ip);
            }
            if (hasCommitments)
            {
                this.Commitments = new CommitmentValues[_tc.Length];
                for (int i = 0; i < _tc.Length; i++)
                {
                    this.Commitments[i] = new CommitmentValues(
                        _tc[i].ToGroupElement(Serializer.ip),
                        _ta[i].ToByteArray(),
                        _tr[i].ToFieldElement(Serializer.ip));
                }
            }
        }
            #endregion Serialization
    }
}
