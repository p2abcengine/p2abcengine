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

using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Text;
using UProveCrypto;
using UProveCrypto.Math;

// ignore warnings for obsolete methods
#pragma warning disable 0618

namespace UProveUnitTest
{
    [TestClass]
    public class EndToEndTest
    {
        private static System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();

        [TestMethod]
        public void SkipTokenValidationTest()
        {
            ushort[] BatchValidationSecurityLevels = { 0, 20 };
            foreach (ushort batchValidationSecurityLevel in BatchValidationSecurityLevels)
            {
                // Issuer setup
                IssuerKeyAndParameters ikap;
                IssuerProtocolParameters ipp;
                ProverProtocolParameters ppp;
                StaticTestHelpers.GenerateTestIssuanceParameters("SkipTokenValidationTest", null, 1, true, 10, out ikap, out ipp, out ppp);
                IssuerParameters ip = ikap.IssuerParameters;

                Issuer issuer = ipp.CreateIssuer();
                FirstIssuanceMessage msg1 = issuer.GenerateFirstMessage();

                // set the validation security level. Value 0 will use sequential validation, value > 0 will use batch
                ppp.BatchValidationSecurityLevel = batchValidationSecurityLevel;
                
                Prover prover = ppp.CreateProver();
                SecondIssuanceMessage msg2 = prover.GenerateSecondMessage(msg1);
                ThirdIssuanceMessage msg3 = issuer.GenerateThirdMessage(msg2);
                // skip token validation. Token should be valid
                UProveKeyAndToken[] upkt = prover.GenerateTokens(msg3, true);
                ProtocolHelper.VerifyTokenSignature(ip, upkt[0].Token);

                //
                // Issue an invalid token. Don't skip validation, an exception should be thrown
                //
                issuer = ipp.CreateIssuer();
                msg1 = issuer.GenerateFirstMessage();
                ppp.TokenInformation = encoding.GetBytes("different token information field value");
                ppp.Gamma = null; // reset the attribute-dependent precomputed gamma value
                prover = ppp.CreateProver();
                msg2 = prover.GenerateSecondMessage(msg1);
                msg3 = issuer.GenerateThirdMessage(msg2);
                try
                {
                    upkt = prover.GenerateTokens(msg3, false);
                    Assert.Fail("Expected InvalidUProveArtifactException");
                }
                catch (InvalidUProveArtifactException)
                {
                    // expected
                }

                //
                // Issue an invalid token. Skip validation, and make sure token is invalid
                //
                issuer = ipp.CreateIssuer();
                msg1 = issuer.GenerateFirstMessage();
                prover = ppp.CreateProver();
                msg2 = prover.GenerateSecondMessage(msg1);
                msg3 = issuer.GenerateThirdMessage(msg2);
                upkt = prover.GenerateTokens(msg3, true);
                try
                {
                    ProtocolHelper.VerifyTokenSignature(ip, upkt[0].Token);
                }
                catch (InvalidUProveArtifactException)
                {
                    // expected
                }
            }

        }

        [TestMethod]
        public void PseudonymAndCommitmentsTest()
        {
            // Issuer setup
            IssuerSetupParameters isp = new IssuerSetupParameters();
            isp.UidP = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            isp.E = new byte[] { (byte)1, (byte)1, (byte)1, (byte)1 };
            isp.UseRecommendedParameterSet = true;
            isp.S = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            IssuerKeyAndParameters ikap = isp.Generate();
            IssuerParameters ip = ikap.IssuerParameters;

            // Issuance
            byte[][] attributes = new byte[][] { encoding.GetBytes("Attribute 1"), encoding.GetBytes("Attribute 2"), encoding.GetBytes("Attribute 3"), encoding.GetBytes("Attribute 4") };
            byte[] tokenInformation = new byte[] { };
            byte[] proverInformation = new byte[] { };
            int numberOfTokens = 1;

            IssuerProtocolParameters ipp = new IssuerProtocolParameters(ikap);
            ipp.Attributes = attributes;
            ipp.NumberOfTokens = numberOfTokens;
            ipp.TokenInformation = tokenInformation;
            Issuer issuer = ipp.CreateIssuer();
            FirstIssuanceMessage msg1 = issuer.GenerateFirstMessage();
            ProverProtocolParameters ppp = new ProverProtocolParameters(ip);
            ppp.Attributes = attributes;
            ppp.NumberOfTokens = numberOfTokens;
            ppp.TokenInformation = tokenInformation;
            ppp.ProverInformation = proverInformation;
            Prover prover = ppp.CreateProver();
            SecondIssuanceMessage msg2 = prover.GenerateSecondMessage(msg1);
            ThirdIssuanceMessage msg3 = issuer.GenerateThirdMessage(msg2);
            UProveKeyAndToken[] upkt = prover.GenerateTokens(msg3);

            // Pseudonym
            int[] disclosed = new int[0];
            int[] committed = new int[] { 2, 4 };
            byte[] message = encoding.GetBytes("this is the presentation message, this can be a very long message");
            byte[] scope = encoding.GetBytes("scope");
            PresentationProof proof;
            FieldZqElement[] tildeO;

            // Valid presentation
            proof = PresentationProof.Generate(ip, disclosed, committed, 1, scope, message, null, null, upkt[0], attributes, out tildeO);
            proof.Verify(ip, disclosed, committed, 1, scope, message, null, upkt[0].Token);

            // Invalid pseudonym (wrong scope)
            proof = PresentationProof.Generate(ip, disclosed, committed, 1, scope, message, null, null, upkt[0], attributes, out tildeO);
            try { proof.Verify(ip, disclosed, committed, 1, encoding.GetBytes("bad scope"), message, null, upkt[0].Token); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            // Invalid pseudonym (wrong attribute)
            try { proof.Verify(ip, disclosed, committed, 2, scope, message, null, upkt[0].Token); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            // Invalid commitment (null list)
            try { proof.Verify(ip, disclosed, null, 2, scope, message, null, upkt[0].Token); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            // Invalid commitment (wrong committed values)
            try { proof.Verify(ip, disclosed, new int[] { 1, 4 }, 2, scope, message, null, upkt[0].Token); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            // Invalid commitment (wront number of committed values)
            try { proof.Verify(ip, disclosed, new int[] { 1 }, 2, scope, message, null, upkt[0].Token); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            // Invalid commitment (value)
            proof.Commitments[0].TildeA[0]++;
            try { proof.Verify(ip, disclosed, committed, 2, scope, message, null, upkt[0].Token); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            // Ensure tildeO is correct
            GroupElement Cx2 = proof.Commitments[0].TildeC;         // x2 is the first committed attribute
            FieldZqElement x2 = ProtocolHelper.ComputeXi(ip, 1, attributes[1]);       // attributes[] is zero indexed.
            FieldZqElement tildeO2 = tildeO[0];
                    // double check that Cx2 is computed correctly.
            GroupElement Cx2Prime = ip.Gq.G.Exponentiate(x2);
            Cx2Prime = Cx2Prime.Multiply(ip.G[1].Exponentiate(tildeO2));
            Assert.IsTrue(Cx2Prime.Equals(Cx2));

        }

        [TestMethod]
        public void DevicePseudonymTest()
        {
            // Issuer setup
            IssuerSetupParameters isp = new IssuerSetupParameters();
            isp.UidP = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            isp.E = new byte[] { (byte)1, (byte)1, (byte)1, (byte)1 };
            isp.UseRecommendedParameterSet = true;
            isp.S = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            IssuerKeyAndParameters ikap = isp.Generate(true);
            IssuerParameters ip = ikap.IssuerParameters;

            // Issuance
            byte[][] attributes = new byte[][] { encoding.GetBytes("Attribute 1"), encoding.GetBytes("Attribute 2"), encoding.GetBytes("Attribute 3"), encoding.GetBytes("Attribute 4") };
            byte[] tokenInformation = new byte[] { };
            byte[] proverInformation = new byte[] { };
            int numberOfTokens = 1;

            IDevice device = new VirtualDevice(ip);
            GroupElement hd = device.GetDevicePublicKey();

            IssuerProtocolParameters ipp = new IssuerProtocolParameters(ikap);
            ipp.Attributes = attributes;
            ipp.NumberOfTokens = numberOfTokens;
            ipp.TokenInformation = tokenInformation;
            ipp.DevicePublicKey = hd;
            Issuer issuer = ipp.CreateIssuer();
            FirstIssuanceMessage msg1 = issuer.GenerateFirstMessage();
            ProverProtocolParameters ppp = new ProverProtocolParameters(ip);
            ppp.Attributes = attributes;
            ppp.NumberOfTokens = numberOfTokens;
            ppp.TokenInformation = tokenInformation;
            ppp.ProverInformation = proverInformation;
            ppp.DevicePublicKey = hd;
            Prover prover = ppp.CreateProver();
            SecondIssuanceMessage msg2 = prover.GenerateSecondMessage(msg1);
            ThirdIssuanceMessage msg3 = issuer.GenerateThirdMessage(msg2);
            UProveKeyAndToken[] upkt = prover.GenerateTokens(msg3);

            // Pseudonym
            int[] disclosed = new int[0];
            byte[] message = encoding.GetBytes("this is the presentation message, this can be a very long message");
            byte[] messageForDevice = encoding.GetBytes("message for Device");
            byte[] scope = encoding.GetBytes("scope");
            PresentationProof proof;
            FieldZqElement[] tildeO;

            // Valid presentation
            IDevicePresentationContext deviceCtx = device.GetPresentationContext();
            proof = PresentationProof.Generate(ip, disclosed, null, PresentationProof.DeviceAttributeIndex, scope, message, messageForDevice, deviceCtx, upkt[0], attributes, out tildeO); 
            proof.Verify(ip, disclosed, null, PresentationProof.DeviceAttributeIndex, scope, message, messageForDevice, upkt[0].Token);

            // Invalid pseudonym (wrong scope)
            deviceCtx = device.GetPresentationContext();
            proof = PresentationProof.Generate(ip, disclosed, null, PresentationProof.DeviceAttributeIndex, scope, message, messageForDevice, deviceCtx, upkt[0], attributes, out tildeO);
            try { proof.Verify(ip, disclosed, null, PresentationProof.DeviceAttributeIndex, encoding.GetBytes("bad scope"), message, messageForDevice, upkt[0].Token); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            // Ensure tildeO is correct, in this case it should be empty because there are no comitted attributes
            Assert.IsTrue(tildeO == null || tildeO.Length == 0);
        }

        [TestMethod]
        public void LongTest()
        {
            int numberOfAttribs = 25;
            // Issuer setup
            IssuerSetupParameters isp = new IssuerSetupParameters();
            isp.UidP = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            isp.E = new byte[numberOfAttribs];
            isp.UseRecommendedParameterSet = true;
            for (int i = 0; i < numberOfAttribs; i++)
            {
                isp.E[i] = (byte) (i%2); // alternate between 0 (direct encoding) and 1 (hash)
            }

            isp.S = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            IssuerKeyAndParameters ikap = isp.Generate();
            IssuerParameters ip = ikap.IssuerParameters;
            ip.Verify();

            // Issuance
            byte[][] attributes = new byte[numberOfAttribs][];
            attributes[0] = new byte[] { 0x00 };
            attributes[1] = null;
            attributes[2] = new byte[] { 0x00 };
            attributes[3] = encoding.GetBytes("This is a very long value that doesn't fit in one attribute, but this is ok since we hash this value");
            for (int index = 4; index < numberOfAttribs; index++)
            {
                // for the rest, we just encode random Zq values
                attributes[index] = ip.Zq.GetRandomElement(false).ToByteArray();
            }
            byte[] tokenInformation = new byte[] { 0x01 };
            byte[] proverInformation = new byte[] { 0x01 };
            int numberOfTokens = 10;

            IssuerProtocolParameters ipp = new IssuerProtocolParameters(ikap);
            ipp.Attributes = attributes;
            ipp.NumberOfTokens = numberOfTokens;
            ipp.TokenInformation = tokenInformation;
            Issuer issuer = ipp.CreateIssuer();
            FirstIssuanceMessage msg1 = issuer.GenerateFirstMessage();
            ProverProtocolParameters ppp = new ProverProtocolParameters(ip);
            ppp.Attributes = attributes;
            ppp.NumberOfTokens = numberOfTokens;
            ppp.TokenInformation = tokenInformation;
            ppp.ProverInformation = proverInformation;
            Prover prover = ppp.CreateProver();
            SecondIssuanceMessage msg2 = prover.GenerateSecondMessage(msg1);
            ThirdIssuanceMessage msg3 = issuer.GenerateThirdMessage(msg2);
            // issue token
            UProveKeyAndToken[] upkt = prover.GenerateTokens(msg3);

            // Presentation
            for (int i = 1; i <= numberOfAttribs; i++)
            {
                // disclose each attribute one by one
                int[] disclosed = new int[] { i };
                byte[] message = encoding.GetBytes("this is the presentation message, this can be a very long message");

                // generate the presentation proof
                PresentationProof proof = PresentationProof.Generate(ip, disclosed, message, null, null, upkt[0], attributes);

                // verify the presentation proof
                proof.Verify(ip, disclosed, message, null, upkt[0].Token);
            }


            // Pseudonym
            for (int i = 1; i <= numberOfAttribs; i++)
            {
                // present each attribute as a pseudonym
                int[] disclosed = new int[0];
                byte[] message = encoding.GetBytes("this is the presentation message, this can be a very long message");
                byte[] scope = encoding.GetBytes("scope" + i);
                FieldZqElement[] unused;

                // generate the presentation proof
                PresentationProof proof = PresentationProof.Generate(ip, disclosed, null, i, scope, message, null, null, upkt[0], attributes, out unused);

                // verify the presentation proof
                proof.Verify(ip, disclosed, null, i, scope, message, null, upkt[0].Token);
            }

        }
        
        private GroupType[] groupConstructions = { GroupType.Subgroup, GroupType.ECC };
        private string[] supportedHashFunctions = { "SHA256", "SHA512" };
        [TestMethod]
        public void TestEndToEnd()
        {
            Random random = new Random();
            int attributeLength = 10;
            foreach (GroupType groupConstruction in groupConstructions)
            {
                foreach (string hashFunction in supportedHashFunctions)
                {
                    //Console.WriteLine("Hash = " + hashFunction);
                    for (int numberOfAttribs = 0; numberOfAttribs <= 3; numberOfAttribs++)
                    {
                        //Console.WriteLine("NumberOfAttribs = " + numberOfAttribs);
                        for (int e = 0; e <= 1; e++)
                        {
                            foreach (bool supportDevice in new bool[] { false, true })
                            {
                                // Issuer setup
                                IssuerSetupParameters isp = new IssuerSetupParameters();
                                isp.GroupConstruction = groupConstruction;
                                isp.UidP = encoding.GetBytes("unique UID");
                                isp.UidH = hashFunction;
                                isp.E = new byte[numberOfAttribs];
                                for (int i = 0; i < numberOfAttribs; i++)
                                {
                                    isp.E[i] = (byte)e;
                                }
                                isp.S = encoding.GetBytes("specification");
                                IssuerKeyAndParameters ikap = isp.Generate(supportDevice);
                                IssuerParameters ip = ikap.IssuerParameters;
                                ip.Verify();

                                IDevice device = null;
                                GroupElement hd = null;
                                if (supportDevice)
                                {
                                    device = new VirtualDevice(ip);
                                    hd = device.GetDevicePublicKey();
                                }

                                // Issuance
                                byte[][] attributes = new byte[numberOfAttribs][];
                                for (int index = 0; index < numberOfAttribs; index++)
                                {
                                    attributes[index] = new byte[attributeLength];
                                    random.NextBytes(attributes[index]);
                                }
                                byte[] tokenInformation = encoding.GetBytes("token information");
                                byte[] proverInformation = encoding.GetBytes("prover information");
                                int numberOfTokens = (int)Math.Pow(2, numberOfAttribs);

                                IssuerProtocolParameters ipp = new IssuerProtocolParameters(ikap);
                                ipp.Attributes = attributes;
                                ipp.NumberOfTokens = numberOfTokens;
                                ipp.TokenInformation = tokenInformation;
                                ipp.DevicePublicKey = hd;
                                Issuer issuer = ipp.CreateIssuer();
                                FirstIssuanceMessage msg1 = issuer.GenerateFirstMessage();
                                ProverProtocolParameters ppp = new ProverProtocolParameters(ip);
                                ppp.Attributes = attributes;
                                ppp.NumberOfTokens = numberOfTokens;
                                ppp.TokenInformation = tokenInformation;
                                ppp.ProverInformation = proverInformation;
                                ppp.DevicePublicKey = hd;
                                Prover prover = ppp.CreateProver();
                                SecondIssuanceMessage msg2 = prover.GenerateSecondMessage(msg1);
                                ThirdIssuanceMessage msg3 = issuer.GenerateThirdMessage(msg2);
                                // issue token
                                UProveKeyAndToken[] upkt = prover.GenerateTokens(msg3);

                                // Presentation
                                for (int i = 0; i < numberOfTokens; i++)
                                {
                                    List<int> disclosedList = new List<int>();
                                    //Console.Write("Disclosed list = ");
                                    for (int index = 0; index < numberOfAttribs; index++)
                                    {
                                        if ((((int)Math.Pow(2, index)) & i) != 0)
                                        {
                                            //Console.Write((index + 1) + ", ");
                                            disclosedList.Add(index + 1);
                                        }
                                    }
                                    //Console.WriteLine();

                                    int[] disclosed = disclosedList.ToArray();
                                    byte[] message = encoding.GetBytes("message");
                                    byte[] deviceMessage = null;
                                    IDevicePresentationContext deviceContext = null;
                                    if (supportDevice)
                                    {
                                        deviceMessage = encoding.GetBytes("message");
                                        deviceContext = device.GetPresentationContext();
                                    }

                                    // generate the presentation proof
                                    PresentationProof proof = PresentationProof.Generate(ip, disclosed, message, deviceMessage, deviceContext, upkt[i], attributes);

                                    // verify the presentation proof
                                    proof.Verify(ip, disclosed, message, deviceMessage, upkt[i].Token);

                                    //
                                    // negative cases
                                    //
                                    if (numberOfAttribs > 0)
                                    {
                                        // modify issuer params (change specification);
                                        IssuerParameters ip2 = new IssuerParameters(ip.UidP, ip.Gq, ip.UidH, ip.G, ip.Gd, ip.E, ip.S, ip.UsesRecommendedParameters);
                                        ip2.S = encoding.GetBytes("wrong issuer params");
                                        try { proof.Verify(ip2, disclosed, message, null, upkt[i].Token); Assert.Fail(); }
                                        catch (InvalidUProveArtifactException) { }

                                        // modify disclosed list
                                        int[] disclosed2;
                                        if (disclosed.Length == 0)
                                        {
                                            disclosed2 = new int[] { 1 };
                                        }
                                        else
                                        {
                                            disclosed2 = new int[] { };
                                        }
                                        try { proof.Verify(ip, disclosed2, message, deviceMessage, upkt[i].Token); Assert.Fail(); }
                                        catch (InvalidUProveArtifactException) { }

                                        // modify message
                                        try { proof.Verify(ip, disclosed, encoding.GetBytes("wrong message"), deviceMessage, upkt[i].Token); Assert.Fail(); }
                                        catch (InvalidUProveArtifactException) { }

                                        // modify token
                                        try { proof.Verify(ip, disclosed, message, deviceMessage, upkt[(i + 1) % numberOfTokens].Token); Assert.Fail(); }
                                        catch (InvalidUProveArtifactException) { }

                                        // modify proof
                                        proof.A = encoding.GetBytes("wrong proof");
                                        try { proof.Verify(ip, disclosed, message, deviceMessage, upkt[i].Token); Assert.Fail(); }
                                        catch (InvalidUProveArtifactException) { }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        [TestMethod]
        public void CollaborativeIssuanceTest()
        {
            // Issuer setup
            IssuerSetupParameters isp = new IssuerSetupParameters();
            isp.UidP = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            isp.E = new byte[] { (byte)1, (byte)1, (byte)1, (byte)1 };
            isp.UseRecommendedParameterSet = true;
            isp.S = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            IssuerKeyAndParameters ikap = isp.Generate();
            IssuerParameters ip = ikap.IssuerParameters;

            // Issuance
            
            byte[][] attributes = new byte[][] { encoding.GetBytes("Attribute 1"), encoding.GetBytes("Attribute 2"), encoding.GetBytes("Attribute 3"), encoding.GetBytes("Attribute 4") };
            byte[] tokenInformation = new byte[] { };
            byte[] proverInformation = new byte[] { };
            int numberOfTokens = 2;

            // Test cases
            // 1: CA-RA split (a party trusted by the issuer provides the gamma value)
            int numTestCases = 1;

            for (int testCase = 1; testCase <= numTestCases; testCase++)
            {
                ProverProtocolParameters ppp = new ProverProtocolParameters(ip);
                ppp.Attributes = attributes;
                ppp.NumberOfTokens = numberOfTokens;
                ppp.TokenInformation = tokenInformation;
                ppp.ProverInformation = proverInformation;
                Prover prover = ppp.CreateProver();

                IssuerProtocolParameters ipp = new IssuerProtocolParameters(ikap);
                if (testCase == 1)
                {
                    ipp.Gamma = ProtocolHelper.ComputeIssuanceInput(ip, attributes, tokenInformation, null);
                }
                ipp.NumberOfTokens = numberOfTokens;
                Issuer issuer = ipp.CreateIssuer();

                FirstIssuanceMessage msg1 = issuer.GenerateFirstMessage();
                SecondIssuanceMessage msg2 = prover.GenerateSecondMessage(msg1);
                ThirdIssuanceMessage msg3 = issuer.GenerateThirdMessage(msg2);
                UProveKeyAndToken[] upkt = prover.GenerateTokens(msg3);

                // use the token to make sure everything is ok
                int[] disclosed = new int[0];
                byte[] message = encoding.GetBytes("this is the presentation message, this can be a very long message");
                FieldZqElement[] unused;
                byte[] scope = null;
                PresentationProof proof = PresentationProof.Generate(ip, disclosed, null, 1, scope, message, null, null, upkt[0], attributes, out unused);
                proof.Verify(ip, disclosed, null, 1, scope, message, null, upkt[0].Token);
            }
        }

        Random random = new Random();
        private bool CoinFlip()
        {
            return (random.Next(2) == 0);
        }

        private byte[] GetRandomBytes(int maxSize)
        {
            int size = random.Next(maxSize); // empty is ok
            byte[] bytes = new byte[size];
            random.NextBytes(bytes);
            return bytes;
        }
        const int MaxByteArrayLength = 20;

        private string PrintList(List<int> dList)
        {
            bool first = true;
            StringBuilder sb = new StringBuilder();
            sb.Append("[");
            foreach (int i in dList)
            {
                if (!first) sb.Append(",");
                sb.Append(i);
                first = false;
            }
            sb.Append("]");
            return sb.ToString();
        }

        private void RunFuzzedTest(bool useSubgroupConstruction, string hashFunction, int numberOfAttributes, bool supportDevice, int numberOfTokens, int[] dArray, int[] cArray, int pseudonymIndex)
        {
            // Issuer setup
            IssuerSetupParameters isp = new IssuerSetupParameters();
            isp.GroupConstruction = useSubgroupConstruction ? GroupType.Subgroup : GroupType.ECC;
            isp.UidP = GetRandomBytes(MaxByteArrayLength);
            isp.UidH = hashFunction;
            isp.E = IssuerSetupParameters.GetDefaultEValues(numberOfAttributes);
            isp.S = GetRandomBytes(MaxByteArrayLength);
            IssuerKeyAndParameters ikap = isp.Generate(supportDevice);
            IssuerParameters ip = ikap.IssuerParameters;
            ip.Verify();

            IDevice device = null;
            GroupElement hd = null;
            if (supportDevice)
            {
                device = new VirtualDevice(ip);
                hd = device.GetDevicePublicKey();
            }

            // Issuance
            byte[][] attributes = new byte[numberOfAttributes][];
            for (int index = 0; index < numberOfAttributes; index++)
            {
                attributes[index] = GetRandomBytes(MaxByteArrayLength);
            }
            byte[] tokenInformation = GetRandomBytes(MaxByteArrayLength);
            byte[] proverInformation = GetRandomBytes(MaxByteArrayLength);

            IssuerProtocolParameters ipp = new IssuerProtocolParameters(ikap);
            ipp.Attributes = attributes;
            ipp.NumberOfTokens = numberOfTokens;
            ipp.TokenInformation = tokenInformation;
            ipp.DevicePublicKey = hd;
            Issuer issuer = ipp.CreateIssuer();

            string msg1 = ip.Serialize(issuer.GenerateFirstMessage());
                                
            ProverProtocolParameters ppp = new ProverProtocolParameters(ip);
            ppp.Attributes = attributes;
            ppp.NumberOfTokens = numberOfTokens;
            ppp.TokenInformation = tokenInformation;
            ppp.ProverInformation = proverInformation;
            ppp.DevicePublicKey = hd;
            Prover prover = ppp.CreateProver();
            string msg2 = ip.Serialize(prover.GenerateSecondMessage(ip.Deserialize<FirstIssuanceMessage>(msg1)));
            string msg3 = ip.Serialize(issuer.GenerateThirdMessage(ip.Deserialize<SecondIssuanceMessage>(msg2)));
            // issue token
            UProveKeyAndToken[] upkt = prover.GenerateTokens(ip.Deserialize<ThirdIssuanceMessage>(msg3));

            // Presentation
            byte[] message = GetRandomBytes(MaxByteArrayLength);
            byte[] deviceMessage = null;
            IDevicePresentationContext deviceContext = null;
            if (supportDevice)
            {
                deviceMessage = GetRandomBytes(MaxByteArrayLength);
                deviceContext = device.GetPresentationContext();
            }
            int tokenIndex = random.Next(upkt.Length);

            // generate the presentation proof
            PresentationProof proof = PresentationProof.Generate(ip, dArray, message, deviceMessage, deviceContext, upkt[tokenIndex], attributes);

            // verify the presentation proof
            proof.Verify(ip, dArray, message, deviceMessage, upkt[tokenIndex].Token);

        }


        /// <summary>
        /// Tests random protocol options
        /// </summary>
        [TestMethod]
        public void FuzzTest()
        {
            for (int repeat = 0; repeat < 20; repeat++)
            {
                StringBuilder msg = new StringBuilder("Test options: ");

                int numberOfPregenGenerators = ParameterSet.NumberOfIssuerGenerators;
                bool useSubgroupConstruction = CoinFlip(); msg.Append("group=" + (useSubgroupConstruction ? "subgroup" : "ECC"));
                int numberOfAttributes = CoinFlip() ?
                    // small number of attributes to use recommeded params [0, max]
                    random.Next(numberOfPregenGenerators + 1) :
                    // large number of attributes to use custom generators [max + 1, max + 10]
                    random.Next(10) + numberOfPregenGenerators + 1;
                msg.Append(", n=" + numberOfAttributes);
                bool deviceProtected = CoinFlip(); msg.Append(", device=" + (deviceProtected ? "true" : "false"));
                int numberOfTokens = random.Next(5) + 1; msg.Append(", #tokens=" + numberOfTokens);
                List<int> dList = new List<int>();
                List<int> uList = new List<int>();
                List<int> cList = new List<int>();
                for (int i = 1; i <= numberOfAttributes; i++)
                {
                    if (CoinFlip())
                    {
                        dList.Add(i); // disclosed attributes
                    }
                    else
                    {
                        uList.Add(i); // undisclosed attributes
                        if (CoinFlip())
                        {
                            cList.Add(i); // committed attributes
                        }
                    }
                }
                msg.Append(", D=" + PrintList(dList));
                msg.Append(", C=" + PrintList(cList));
                int[] dArray = dList.ToArray();
                int[] cArray = cList.ToArray();
                int pseudonymIndex = 0;
                if (uList.Count > 0)
                {
                    pseudonymIndex = CoinFlip() ? 0 : uList[random.Next(uList.Count)]; msg.Append(", p=" + pseudonymIndex); // random undisclosed attribute
                }
                try
                {
                    RunFuzzedTest(useSubgroupConstruction, "SHA256", numberOfAttributes, deviceProtected, numberOfTokens, dArray, cArray, pseudonymIndex);
                }
                catch (Exception e)
                {
                    Assert.Fail(msg.ToString() + ". Exception: " + e.StackTrace);
                }
            }
        }


    }
}
