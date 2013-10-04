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
using System.Linq;
using UProveCrypto;
using System.Globalization;
using UProveCrypto.Math;
using System.Diagnostics;

namespace UProveCryptoTest
{
    [TestClass]
    [DeploymentItem(@"TestVectorData\testvectorssubgroup_Device_doc.txt")]
    [DeploymentItem(@"TestVectorData\testvectorsEC_Device_doc.txt")]
    [DeploymentItem(@"TestVectorData\testvectorssubgroup_doc.txt")]
    [DeploymentItem(@"TestVectorData\testvectorsEC_doc.txt")]
    public class TestVectorsTest
    {
        public static GroupElement CreateGroupElement(Group Gq, string value)
        {
            if (Gq is SubgroupGroup)
            {
                return Gq.CreateGroupElement(HexToBytes(value));
            }
            else
            {
                ECGroup ecGq = Gq as ECGroup;
                string[] point = value.Split(',');
                return ecGq.CreateGroupElement(HexToBytes(point[0]), HexToBytes(point[1]));
            }
        }

        [TestMethod]
        public void HashFormattingTest()
        {
            HashFunction hash;

            // byte
            hash = new HashFunction(TestVectorData.HashVectors.UIDh);
            byte b = 0x01;
            hash.Hash(b);
            Assert.IsTrue(HexToBytes(TestVectorData.HashVectors.hash_byte).SequenceEqual(hash.Digest), "hash_byte");

            // octet string
            hash = new HashFunction(TestVectorData.HashVectors.UIDh);
            byte[] octetString = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 };
            hash.Hash(octetString);
            Assert.IsTrue(HexToBytes(TestVectorData.HashVectors.hash_octetstring).SequenceEqual(hash.Digest), "hash_octetstring");

            // null
            hash = new HashFunction(TestVectorData.HashVectors.UIDh);
            hash.HashNull();
            Assert.IsTrue(HexToBytes(TestVectorData.HashVectors.hash_null).SequenceEqual(hash.Digest), "hash_null");

            // list
            hash = new HashFunction(TestVectorData.HashVectors.UIDh);
            hash.Hash(3); // list length
            hash.Hash(b);
            hash.Hash(octetString);
            hash.HashNull();
            Assert.IsTrue(HexToBytes(TestVectorData.HashVectors.hash_list).SequenceEqual(hash.Digest), "hash_list");

            // subgroup 1.3.6.1.4.1.311.75.1.1.1
            hash = new HashFunction(TestVectorData.HashVectors.UIDh);
            hash.Hash(SubgroupParameterSets.ParamSetL2048N256V1.Group);
            Assert.IsTrue(HexToBytes(TestVectorData.HashVectors.hash_subgroup).SequenceEqual(hash.Digest), "hash_subgroup");

            // ec group 1.3.6.1.4.1.311.75.1.2.1
            hash = new HashFunction(TestVectorData.HashVectors.UIDh);
            hash.Hash(ECParameterSets.ParamSet_EC_P256_V1.Group);
            Assert.IsTrue(HexToBytes(TestVectorData.HashVectors.hash_ecgroup).SequenceEqual(hash.Digest), "hash_ecgroup");
        }

        public static byte[] HexToBytes(string hexString)
        {
            int length = hexString.Length;
            if ((length % 2) != 0)
            {
                // prepend 0
                hexString = "0" + hexString;
            }

            byte[] bytes = new byte[hexString.Length / 2];
            for (int i = 0; i < length; i += 2)
            {
                try
                {
                    bytes[i / 2] = Byte.Parse(hexString.Substring(i, 2), NumberStyles.HexNumber);
                }
                catch (Exception)
                {
                    throw new ArgumentException("hexString is invalid");
                }
            }
            return bytes;
        }

        IssuerKeyAndParameters LoadIssuerKeyAndParameters(bool useSubgroupConstruction, string oid, bool supportDevice, Dictionary<string, string> vectors)
        {
            IssuerSetupParameters isp = new IssuerSetupParameters();
            
            isp.UseRecommendedParameterSet = true;
            isp.GroupConstruction = useSubgroupConstruction ? GroupType.Subgroup : GroupType.ECC;
            isp.UidP = HexToBytes(vectors["UIDp"]);
            isp.UidH = vectors["UIDh"];
            isp.E = new byte[] 
            { 
                byte.Parse(vectors["e1"]),
                byte.Parse(vectors["e2"]),
                byte.Parse(vectors["e3"]),
                byte.Parse(vectors["e4"]),
                byte.Parse(vectors["e5"]) 
            };
            isp.S = HexToBytes(vectors["S"]);
            IssuerKeyAndParameters ikap = isp.Generate(supportDevice);
            Assert.AreEqual<string>(ikap.IssuerParameters.Gq.GroupName, oid);
            return ikap;
        }

        static readonly string[] separator = new string[] { " = " };
        /// <summary>
        /// Read a test vector file. EC points are treated specially: two lines in the
        /// file are merged as one comma-separated value in the returned dictionary
        /// </summary>
        /// <param name="filePath"></param>
        /// <returns></returns>
        Dictionary<string, string> GetTestVectors(string filePath)
        {
            string ecPoint = null;
            Dictionary<string, string> dic = new Dictionary<string, string>();
            foreach (var row in System.IO.File.ReadAllLines(filePath))
            {
                string[] columns = row.Split(separator, StringSplitOptions.None);
                if (columns.Length > 1) // skip headers and comments
                {
                    if (ecPoint != null)
                    {
                        // we started to read an EC point
                        if (!columns[0].EndsWith(".y"))
                        {
                            throw new System.IO.IOException("y point coordinate expected. " + columns[0]);
                        }
                        ecPoint += columns[1];
                        dic.Add(columns[0].Substring(0, columns[0].Length - 2), ecPoint);
                        ecPoint = null;
                    }
                    else if (columns[0].EndsWith(".x"))
                    {
                        // first part of an EC point
                        ecPoint = columns[1];
                        ecPoint += ",";
                    }
                    else
                    {
                        dic.Add(columns[0], columns[1]);
                    }
                }
            }

            return dic;
        }

        public static int stringToInt(string s)
        {
            return int.Parse(s);
        }


        [TestMethod]
        public void ProtocolTest()
        {
            Stopwatch sw = new Stopwatch();
            sw.Start();
            bool[] bools = new bool[] { true, false };
            foreach (bool isSubgroupConstruction in bools)
            {
                foreach (bool supportDevice in bools)
                {
                    var vectors =
                        supportDevice ?
                        (isSubgroupConstruction ?
                            GetTestVectors("testvectorssubgroup_Device_doc.txt")
                            :
                            GetTestVectors("testvectorsEC_Device_doc.txt"))
                        :
                        (isSubgroupConstruction ?
                            GetTestVectors("testvectorssubgroup_doc.txt")
                            :
                            GetTestVectors("testvectorsEC_doc.txt"));

                    IssuerKeyAndParameters ikap = LoadIssuerKeyAndParameters(isSubgroupConstruction, vectors["GroupName"], supportDevice, vectors);
                    FieldZq Zq = ikap.IssuerParameters.Zq;
                    // replace random y0/g0 with test vector values
                    ikap.PrivateKey = Zq.GetElement(HexToBytes(vectors["y0"]));
                    ikap.IssuerParameters.G[0] = CreateGroupElement(ikap.IssuerParameters.Gq, vectors["g0"]);
                    Assert.AreEqual(ikap.IssuerParameters.G[0], ikap.IssuerParameters.Gq.G.Exponentiate(ikap.PrivateKey), "g0 computation");
                    IssuerParameters ip = ikap.IssuerParameters;
                    ip.Verify();

                    /*
                     * issuance
                     */

                    byte[][] A = new byte[][] {
                        HexToBytes(vectors["A1"]), 
                        HexToBytes(vectors["A2"]),
                        HexToBytes(vectors["A3"]),
                        HexToBytes(vectors["A4"]),
                        HexToBytes(vectors["A5"])
                        };

                    Assert.AreEqual(Zq.GetElement(HexToBytes(vectors["x1"])), ProtocolHelper.ComputeXi(ip, 0, A[0]), "x1");
                    Assert.AreEqual(Zq.GetElement(HexToBytes(vectors["x2"])), ProtocolHelper.ComputeXi(ip, 1, A[1]), "x2");
                    Assert.AreEqual(Zq.GetElement(HexToBytes(vectors["x3"])), ProtocolHelper.ComputeXi(ip, 2, A[2]), "x3");
                    Assert.AreEqual(Zq.GetElement(HexToBytes(vectors["x4"])), ProtocolHelper.ComputeXi(ip, 3, A[3]), "x4");
                    Assert.AreEqual(Zq.GetElement(HexToBytes(vectors["x5"])), ProtocolHelper.ComputeXi(ip, 4, A[4]), "x5");

                    byte[] TI = HexToBytes(vectors["TI"]);
                    Assert.IsTrue(HexToBytes(vectors["P"]).SequenceEqual(ip.Digest(supportDevice)), "P");
                    Assert.AreEqual(Zq.GetElement(HexToBytes(vectors["xt"])), ProtocolHelper.ComputeXt(ip, TI, supportDevice), "xt");

                    IDevice device = null;
                    GroupElement hd = null;
                    if (supportDevice)
                    {
                        device = new VirtualDevice(ip, Zq.GetElement(HexToBytes(vectors["xd"])), Zq.GetElement(HexToBytes(vectors["wdPrime"])));
                        IDevicePresentationContext context = device.GetPresentationContext();
                        // Test device responses
                        Assert.AreEqual(CreateGroupElement(ip.Gq, vectors["hd"]), device.GetDevicePublicKey(), "hd");
                        Assert.AreEqual(CreateGroupElement(ip.Gq, vectors["ad"]), context.GetInitialWitness(), "ad");
                        Assert.AreEqual(Zq.GetElement(HexToBytes(vectors["rdPrime"])), context.GetDeviceResponse(HexToBytes(vectors["md"]), HexToBytes(vectors["mdPrime"]), ip.HashFunctionOID), "rdPrime");
                        hd = CreateGroupElement(ip.Gq, vectors["hd"]);
                    }

                    IssuerProtocolParameters ipp = new IssuerProtocolParameters(ikap);
                    ipp.Attributes = A;
                    ipp.NumberOfTokens = 1;
                    ipp.TokenInformation = TI;
                    ipp.DevicePublicKey = hd;
                    ipp.PreGeneratedW = new FieldZqElement[] { Zq.GetElement(HexToBytes(vectors["w"])) };
                    Issuer issuer = ipp.CreateIssuer();
                    byte[] PI = HexToBytes(vectors["PI"]);

                    ProverProtocolParameters ppp = new ProverProtocolParameters(ip);
                    ppp.Attributes = A;
                    ppp.NumberOfTokens = 1;
                    ppp.TokenInformation = TI;
                    ppp.ProverInformation = PI;
                    ppp.DevicePublicKey = hd;
                    ppp.ProverRandomData = new ProverRandomData(
                        new FieldZqElement[] { Zq.GetElement(HexToBytes(vectors["alpha"])) },
                        new FieldZqElement[] { Zq.GetElement(HexToBytes(vectors["beta1"])) },
                        new FieldZqElement[] { Zq.GetElement(HexToBytes(vectors["beta2"])) });
                    Prover prover = ppp.CreateProver();

                    FirstIssuanceMessage msg1 = issuer.GenerateFirstMessage();
                    Assert.AreEqual(msg1.sigmaZ, CreateGroupElement(ip.Gq, vectors["sigmaZ"]), "sigmaZ");
                    Assert.AreEqual(msg1.sigmaA[0], CreateGroupElement(ip.Gq, vectors["sigmaA"]), "sigmaA");
                    Assert.AreEqual(msg1.sigmaB[0], CreateGroupElement(ip.Gq, vectors["sigmaB"]), "sigmaB");

                    SecondIssuanceMessage msg2 = prover.GenerateSecondMessage(msg1);
                    Assert.AreEqual(msg2.sigmaC[0], Zq.GetElement(HexToBytes(vectors["sigmaC"])), "sigmaC");

                    ThirdIssuanceMessage msg3 = issuer.GenerateThirdMessage(msg2);
                    Assert.AreEqual(msg3.sigmaR[0], Zq.GetElement(HexToBytes(vectors["sigmaR"])), "sigmaR");

                    UProveKeyAndToken[] upkt = prover.GenerateTokens(msg3);
                    Assert.AreEqual(upkt[0].PrivateKey, Zq.GetElement(HexToBytes(vectors["alphaInverse"])), "alphaInverse");
                    UProveToken token = upkt[0].Token;
                    Assert.AreEqual(token.H, CreateGroupElement(ip.Gq, vectors["h"]), "h");
                    Assert.AreEqual(token.SigmaZPrime, CreateGroupElement(ip.Gq, vectors["sigmaZPrime"]), "sigmaZPrime");
                    Assert.AreEqual(token.SigmaCPrime, Zq.GetElement(HexToBytes(vectors["sigmaCPrime"])), "sigmaCPrime");
                    Assert.AreEqual(token.SigmaRPrime, Zq.GetElement(HexToBytes(vectors["sigmaRPrime"])), "sigmaRPrime");
                    Assert.IsTrue(HexToBytes(vectors["UIDt"]).SequenceEqual(ProtocolHelper.ComputeTokenID(ip, token)), "UIDt");
                    Assert.IsTrue(supportDevice == token.IsDeviceProtected);

                    /*
                     * presentation
                     */

                    int[] disclosed = Array.ConvertAll<string, int>(vectors["D"].Split(','), new Converter<string, int>(stringToInt));
                    int[] committed = Array.ConvertAll<string, int>(vectors["C"].Split(','), new Converter<string, int>(stringToInt));
                    byte[] m = HexToBytes(vectors["m"]);
                    byte[] md = null;
                    IDevicePresentationContext deviceContext = null;
                    if (supportDevice)
                    {
                        md = HexToBytes(vectors["md"]);
                        deviceContext = device.GetPresentationContext();
                    }
                    int p;
                    if (!int.TryParse(vectors["p"], out p))
                    {
                        p = PresentationProof.DeviceAttributeIndex;
                    }
                    byte[] s = HexToBytes(vectors["s"]);
                    int commitmentIndex = committed[0];
                    ProverPresentationProtocolParameters pppp = new ProverPresentationProtocolParameters(ip, disclosed, m, upkt[0], A);
                    pppp.Committed = committed;
                    pppp.PseudonymAttributeIndex = p;
                    pppp.PseudonymScope = s;
                    pppp.DeviceMessage = md;
                    pppp.DeviceContext = deviceContext;
                    pppp.RandomData = new ProofGenerationRandomData(
                            Zq.GetElement(HexToBytes(vectors["w0"])),
                            new FieldZqElement[] { 
                                Zq.GetElement(HexToBytes(vectors["w1"])), 
                                Zq.GetElement(HexToBytes(vectors["w3"])),
                                Zq.GetElement(HexToBytes(vectors["w4"]))
                            },
                            supportDevice ? Zq.GetElement(HexToBytes(vectors["wd"])) : null,
                            new FieldZqElement[] { 
                                Zq.GetElement(HexToBytes(vectors["tildeO" + commitmentIndex])), 
                            },
                            new FieldZqElement[] { 
                                Zq.GetElement(HexToBytes(vectors["tildeW" + commitmentIndex]))
                            });
                    CommitmentPrivateValues cpv;
                    PresentationProof proof = PresentationProof.Generate(pppp, out cpv);
                    Assert.IsTrue(HexToBytes(vectors["a"]).SequenceEqual(proof.A), "a");
                    Assert.AreEqual(ProtocolHelper.GenerateScopeElement(ip.Gq, s), CreateGroupElement(ip.Gq, vectors["gs"]));
                    Assert.IsTrue(HexToBytes(vectors["ap"]).SequenceEqual(proof.Ap), "ap");
                    Assert.AreEqual(proof.Ps, CreateGroupElement(ip.Gq, vectors["Ps"]), "Ps");
                    Assert.IsTrue(HexToBytes(vectors["A2"]).SequenceEqual(proof.DisclosedAttributes[0]), "A2");
                    Assert.IsTrue(HexToBytes(vectors["A5"]).SequenceEqual(proof.DisclosedAttributes[1]), "A5");
                    Assert.AreEqual(proof.R[0], Zq.GetElement(HexToBytes(vectors["r0"])), "r0");
                    Assert.AreEqual(proof.R[1], Zq.GetElement(HexToBytes(vectors["r1"])), "r1");
                    Assert.AreEqual(proof.R[2], Zq.GetElement(HexToBytes(vectors["r3"])), "r3");
                    Assert.AreEqual(proof.R[3], Zq.GetElement(HexToBytes(vectors["r4"])), "r4");
                    if (supportDevice)
                    {
                        Assert.AreEqual(proof.R[4], Zq.GetElement(HexToBytes(vectors["rd"])), "rd");
                    }
                    Assert.AreEqual(proof.Commitments[0].TildeR, Zq.GetElement(HexToBytes(vectors["tildeR" + commitmentIndex])), "tildeR" + commitmentIndex);
                    Assert.IsTrue(cpv.TildeO.Length == 1);
                    Assert.AreEqual(cpv.TildeO[0], Zq.GetElement(HexToBytes(vectors["tildeO" + commitmentIndex])), "tildeO" + commitmentIndex);
                    VerifierPresentationProtocolParameters vppp = new VerifierPresentationProtocolParameters(ip, disclosed, m, upkt[0].Token);
                    vppp.Committed = committed;
                    vppp.PseudonymAttributeIndex = p;
                    vppp.PseudonymScope = s;
                    vppp.DeviceMessage = md;
                    proof.Verify(vppp);

                }
            }

            sw.Stop();
            Debug.WriteLine("Protocol Test Elapsed Time: " + sw.ElapsedMilliseconds + "ms");
        }
    }
}
