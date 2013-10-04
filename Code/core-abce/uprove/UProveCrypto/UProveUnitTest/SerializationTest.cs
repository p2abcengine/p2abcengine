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
using System.Diagnostics;
using System.IO;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters.Binary;
using UProveCrypto;
using UProveCrypto.Math;

// ignore warnings for obsolete methods
#pragma warning disable 0618

namespace UProveUnitTest
{
    [TestClass]
    [DeploymentItem(@"SerializationReference\", "SerializationReference")]
    public class SerializationTest
    {
        static bool CREATE_SERIALIZATION_TEST_FILES = false;

        static byte[] HexToBytes(string hexString)
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
                    bytes[i / 2] = Byte.Parse(hexString.Substring(i, 2), System.Globalization.NumberStyles.HexNumber);
                }
                catch (Exception)
                {
                    throw new ArgumentException("hexString is invalid");
                }
            }
            return bytes;
        }


        
        [TestMethod]
        public void TestSerialization()
        {

            // Create IssuerSetupParameters
            System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();

            for (int i = 0; i <= 1; i++)
            {
                for (int j = 0; j <= 1; j++)
                {
                    bool useCustomGroup = (i == 0);
                    bool useSubgroupConstruction = (j == 0);

                    IssuerSetupParameters isp = new IssuerSetupParameters();
                    if (useSubgroupConstruction)
                    {
                        isp.GroupConstruction = GroupType.Subgroup;
                        if (useCustomGroup)
                        {
                            byte[] p = HexToBytes("d21ae8d66e6c6b3ced0eb3df1a26c91bdeed013c17d849d30ec309813e4d3799f26db0d494e82ec61ea9fdc70bb5cbcaf2e5f18a836494f58e67c6d616480c37a7f2306101fc9f0f4768f9c9793c2be176b0b7c979b4065d3e835686a3f0b8420c6834cb17930386dedab2b07dd473449a48baab316286b421052475d134cd3b");
                            byte[] q = HexToBytes("fff80ae19daebc61f46356af0935dc0e81148eb1");
                            byte[] g = HexToBytes("abcec972e9a9dd8d133270cfeac26f726e567d964757630d6bd43460d0923a46aec0ace255ebf3ddd4b1c4264f53e68b361afb777a13cf0067dae364a34d55a0965a6cccf78852782923813cf8708834d91f6557d783ec75b5f37cd9185f027b042c1c72e121b1266a408be0bb7270d65917b69083633e1f3cd60624612fc8c1");
                            isp.Gq = SubgroupGroup.CreateSubgroupGroup(
                                p,
                                q,
                                g,
                                null, 
                                null);
                            isp.UidH = "SHA1";
                        }
                    }
                    else
                    {
                        isp.GroupConstruction = GroupType.ECC;
                        if (useCustomGroup)
                        {
                            continue;
                        }
                    }

                    isp.UidP = encoding.GetBytes("http://issuer/uprove/issuerparams/software");
                    isp.E = IssuerSetupParameters.GetDefaultEValues(3);
                    isp.S = encoding.GetBytes("application-specific specification");

                    // Generate IssuerKeyAndParameters
                    IssuerKeyAndParameters ikap = isp.Generate();

                    // Create an IssuerParameters
                    IssuerParameters ip = ikap.IssuerParameters;

                    VerifySerialization(useCustomGroup, useSubgroupConstruction, ip, ikap);
                    VerifySerialization(useCustomGroup, useSubgroupConstruction, ip, ikap.IssuerParameters);

                    // specify the attribute values
                    byte[][] attributes = new byte[][] {
                    encoding.GetBytes("first attribute value"),
                    encoding.GetBytes("second attribute value"),
                    encoding.GetBytes("third attribute value")   };

                    // specify the special field values
                    byte[] tokenInformation = encoding.GetBytes("token information value");
                    byte[] proverInformation = encoding.GetBytes("prover information value");

                    // specify the number of tokens to issue
                    int numberOfTokens = 5;

                    // setup the issuer and generate the first issuance message
                    IssuerProtocolParameters ipp = new IssuerProtocolParameters(ikap);
                    ipp.Attributes = attributes;
                    ipp.NumberOfTokens = numberOfTokens;
                    ipp.TokenInformation = tokenInformation;
                    Issuer issuer = ipp.CreateIssuer();
                    FirstIssuanceMessage firstMessage = issuer.GenerateFirstMessage();
                    VerifySerialization(useCustomGroup, useSubgroupConstruction, ip, firstMessage);

                    // setup the prover and generate the second issuance message
                    Prover prover = new Prover(ip, numberOfTokens, attributes, tokenInformation, proverInformation, null);
                    SecondIssuanceMessage secondMessage = prover.GenerateSecondMessage(firstMessage);
                    VerifySerialization(useCustomGroup, useSubgroupConstruction, ip, secondMessage);

                    // generate the third issuance message
                    ThirdIssuanceMessage thirdMessage = issuer.GenerateThirdMessage(secondMessage);
                    VerifySerialization(useCustomGroup, useSubgroupConstruction, ip, thirdMessage);

                    // generate the tokens
                    UProveKeyAndToken[] upkt = prover.GenerateTokens(thirdMessage);

                    string json = ip.Serialize(upkt);
                    UProveKeyAndToken[] upkt_fromJson = ip.Deserialize<UProveKeyAndToken[]>(json);
                    Assert.IsTrue(Verify(upkt[0], upkt_fromJson[0]));
                    Assert.IsTrue(Verify(upkt[1], upkt_fromJson[1]));
                    Assert.IsTrue(Verify(upkt[2], upkt_fromJson[2]));
                    Assert.IsTrue(Verify(upkt[3], upkt_fromJson[3]));
                    Assert.IsTrue(Verify(upkt[4], upkt_fromJson[4]));

                    VerifySerialization(useCustomGroup, useSubgroupConstruction, ip, upkt[0].Token);

                    /*
                     *  token presentation
                     */
                    // the indices of disclosed attributes
                    int[] disclosed = new int[] { 2 };
                    // the indices of committed attributes
                    int[] committed = new int[] { 3 };
                    // the returned commitment randomizers, to be used by an external proof module
                    FieldZqElement[] tildeO;
                    // the application-specific message that the prover will sign. Typically this is a nonce combined
                    // with any application-specific transaction data to be signed.
                    byte[] message = encoding.GetBytes("message");
                    // the application-specific verifier scope from which a scope-exclusive pseudonym will be created
                    // (if null, then a pseudonym will not be presented)
                    byte[] scope = encoding.GetBytes("verifier scope");
                    // generate the presentation proof
                    PresentationProof proof = PresentationProof.Generate(ip, disclosed, committed, 1, scope, message, null, null, upkt[0], attributes, out tildeO);
                    VerifySerialization(useCustomGroup, useSubgroupConstruction, ip, proof);

                    // verify the presentation proof
                    proof.Verify(ip, disclosed, committed, 1, scope, message, null, upkt[0].Token);
                }
            }

        }
        [TestMethod]
        public void TestSerializationReference()
        {

            // Create IssuerSetupParameters
            System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();
            foreach (string fileName in Directory.GetFiles("SerializationReference"))
            {
                FileStream f = File.OpenRead(fileName);
                BinaryFormatter bf = new BinaryFormatter();
                object[] parameters = (object[]) bf.Deserialize(f);
                f.Close();

                bool useCustomGroup = (bool) parameters[0];
                bool useSubgroupConstruction = (bool) parameters[1];
                string typeName = (string)parameters[2];
                string json = (string)parameters[3];

                IssuerSetupParameters isp = new IssuerSetupParameters();
                if (useSubgroupConstruction)
                {
                    isp.GroupConstruction = GroupType.Subgroup;
                    if (useCustomGroup)
                    {
                        byte[] p = HexToBytes("d21ae8d66e6c6b3ced0eb3df1a26c91bdeed013c17d849d30ec309813e4d3799f26db0d494e82ec61ea9fdc70bb5cbcaf2e5f18a836494f58e67c6d616480c37a7f2306101fc9f0f4768f9c9793c2be176b0b7c979b4065d3e835686a3f0b8420c6834cb17930386dedab2b07dd473449a48baab316286b421052475d134cd3b");
                        byte[] q = HexToBytes("fff80ae19daebc61f46356af0935dc0e81148eb1");
                        byte[] g = HexToBytes("abcec972e9a9dd8d133270cfeac26f726e567d964757630d6bd43460d0923a46aec0ace255ebf3ddd4b1c4264f53e68b361afb777a13cf0067dae364a34d55a0965a6cccf78852782923813cf8708834d91f6557d783ec75b5f37cd9185f027b042c1c72e121b1266a408be0bb7270d65917b69083633e1f3cd60624612fc8c1");
                        isp.Gq = SubgroupGroup.CreateSubgroupGroup(
                            p,
                            q,
                            g,
                            null,
                            null);
                        isp.UidH = "SHA1";
                    }
                }
                else
                {
                    isp.GroupConstruction = GroupType.ECC;
                    if (useCustomGroup)
                    {
                        continue;
                    }
                }

                isp.UidP = encoding.GetBytes("http://issuer/uprove/issuerparams/software");
                isp.E = IssuerSetupParameters.GetDefaultEValues(3);
                isp.S = encoding.GetBytes("application-specific specification");

                // Generate IssuerKeyAndParameters
                IssuerKeyAndParameters ikap = isp.Generate();

                // Create an IssuerParameters
                IssuerParameters ip = ikap.IssuerParameters;

                // check that we didn't send any null fields
                Assert.IsFalse(json.Contains(":null"));

                string roundTrip = "";
                if (typeName == "UProveCrypto.IssuerParameters")
                {
                    IssuerParameters obj = ip.Deserialize<IssuerParameters>(json);
                    roundTrip = ip.Serialize<IssuerParameters>(obj);
                }
                else if (typeName == "UProveCrypto.IssuerKeyAndParameters")
                {
                    IssuerKeyAndParameters obj = ip.Deserialize<IssuerKeyAndParameters>(json);
                    roundTrip = ip.Serialize<IssuerKeyAndParameters>(obj);
                }
                else if (typeName == "UProveCrypto.FirstIssuanceMessage")
                {
                    FirstIssuanceMessage obj = ip.Deserialize<FirstIssuanceMessage>(json);
                    roundTrip = ip.Serialize<FirstIssuanceMessage>(obj);
                }
                else if (typeName == "UProveCrypto.SecondIssuanceMessage")
                {
                    SecondIssuanceMessage obj = ip.Deserialize<SecondIssuanceMessage>(json);
                    roundTrip = ip.Serialize<SecondIssuanceMessage>(obj);
                }
                else if (typeName == "UProveCrypto.ThirdIssuanceMessage")
                {
                    ThirdIssuanceMessage obj = ip.Deserialize<ThirdIssuanceMessage>(json);
                    roundTrip = ip.Serialize<ThirdIssuanceMessage>(obj);
                }
                else if (typeName == "UProveCrypto.UProveKeyAndToken")
                {
                    UProveKeyAndToken obj = ip.Deserialize<UProveKeyAndToken>(json);
                    roundTrip = ip.Serialize<UProveKeyAndToken>(obj);
                }
                else if (typeName == "UProveCrypto.UProveToken")
                {
                    UProveToken obj = ip.Deserialize<UProveToken>(json);
                    roundTrip = ip.Serialize<UProveToken>(obj);
                }
                else if (typeName == "UProveCrypto.PresentationProof")
                {
                    PresentationProof obj = ip.Deserialize<PresentationProof>(json);
                    roundTrip = ip.Serialize<PresentationProof>(obj);
                }
                else
                {
                    Assert.Fail("Unrecognized type " + typeName + " in SerializationReference files");
                }

                Assert.AreEqual(json, roundTrip);
            }
        }


        [TestMethod]
        public void TestSerializationErrors()
        { 
            string ipJson = 
                @"{""uidp"":""aHR0cDovL2lzc3Vlci91cHJvdmUvaXNzdWVycGFyYW1zL3NvZnR3YXJl"",
                   ""descGq"":{
                      ""type"":""sg"",
                      ""sgDesc"":{
                             ""p"":""0hro1m5sazztDrPfGibJG97tATwX2EnTDsMJgT5NN5nybbDUlOguxh6p\/ccLtcvK8uXxioNklPWOZ8bWFkgMN6fyMGEB\/J8PR2j5yXk8K+F2sLfJebQGXT6DVoaj8LhCDGg0yxeTA4be2rKwfdRzRJpIuqsxYoa0IQUkddE0zTs="",
                             ""q"":""\/\/gK4Z2uvGH0Y1avCTXcDoEUjrE="",
                             ""g"":""q87Jcump3Y0TMnDP6sJvcm5WfZZHV2MNa9Q0YNCSOkauwKziVevz3dSxxCZPU+aLNhr7d3oTzwBn2uNko01VoJZabMz3iFJ4KSOBPPhwiDTZH2VX14PsdbXzfNkYXwJ7BCwccuEhsSZqQIvgu3Jw1lkXtpCDYz4fPNYGJGEvyME=""
                          }
                   },
                   ""uidh"":""SHA1"",
                   ""g"":[
                          ""OkklmmgheMsvW2M0QIKqBsTsjmkQUSybFwod\/Wu4Q9qd21VVkbgIHG0BwlhJtU4CRZ\/oxnX6+JySb2qzc+Kyqcy\/FrUki3wOuul\/qOS5TSxVavF5aRkcF3Lda0ilvtCevOoKSHwaz4sHnoNLITufKECfhrC9MewFIeK4ynuO9jA="",
                          ""ozL4rA+XfgPcvW9KTkAWuYcKW\/tiIGKCw3fSDb0yYSg\/+aczjmqY4GTy1ffJUYl+b4twm7XwB3Rey0aDqQN+XwS9qbs7fsxAj9OFFZiI7jDNzmBT6bGmeyxMkiYCWMZlYTE154+oasan\/wqtufC8N0BpEqAYQvCKyLMN6SABKGA="",
                          ""zkmer9J5f2M2VBcu0COTYHoWWmzuxwfMqpJQ5N9UE\/WMuDBOAZLmLzzT9cYiSB+fSCPrvTbNC6nExfJx0jGG4uPE32NDwVInbzWJldTDJEWa5XFIKxDRRUKN0UG1PbDp+BOMQ0kqeOTVn\/vrpuqwFRfBP2ZppIJXhKdDeiBUWI0="",
                          ""iApB3wHbVWU1A\/b3iPkXXXV86PD3JCED+ogSrxddmgJ3ALWOuLtRCe5O1MnmlmWzP22D3xQ7b2eWjI+hHp9ZhEToSUYrzx9RzIPFuRSflJbRKPv210pmtjx2QBOKq5Z5fxVE\/DM5MiWMQmeINNy8DNRMxqFJPkhoLu0V4uqDocs="",
                          ""selOZXQwG7AwW1veYigEW9Tl1INw\/V0rXkQDx4OtJ8lCvv2RpAh4SkoUoy6yL2yOVZqd5rosTP8snKYO5L1aE4aqZNuNL8GD\/XxXbW7tzf\/OqOAqilJG4C+2\/IQADK42p674HIkf6Txj5P+epGhcl8V+XYyFNKYhXmUq\/9ju1fY=""
                        ],
                   ""e"":""AQEB"",
                   ""s"":""YXBwbGljYXRpb24tc3BlY2lmaWMgc3BlY2lmaWNhdGlvbg==""
                }";

            IssuerParameters ip = new IssuerParameters(ipJson);

            TestError<IssuerParameters>(ip, RemoveValue(ipJson, "uidp"), "IssuerParameters:uidp");
            TestError<IssuerParameters>(ip, RemoveValue(ipJson, "uidh"), "IssuerParameters:uidh");
            TestError<IssuerParameters>(ip, RemoveArrayValue(ipJson, "g"), "IssuerParameters:g");
            TestError<IssuerParameters>(ip, RemoveObject(ipJson, "descGq"), "IssuerParameters:descGq");
            TestError<IssuerParameters>(ip, "{blah,blah,blah}", "IssuerParameters");
            TestError<IssuerParameters>(ip, "{{}", "IssuerParameters");

            string missingGelement = ipJson.Replace(
                "\"OkklmmgheMsvW2M0QIKqBsTsjmkQUSybFwod\\/Wu4Q9qd21VVkbgIHG0BwlhJtU4CRZ\\/oxnX6+JySb2qzc+Kyqcy\\/FrUki3wOuul\\/qOS5TSxVavF5aRkcF3Lda0ilvtCevOoKSHwaz4sHnoNLITufKECfhrC9MewFIeK4ynuO9jA=\",",
                "");
            TestError<IssuerParameters>(ip, missingGelement, "IssuerParameters:Invalid number of elements in G");
            TestError<IssuerParameters>(ip, RemoveValue(ipJson, "p"), "IssuerParameters:p, q, g cannot be null");
            TestError<IssuerParameters>(ip, RemoveValue(ipJson, "q"), "IssuerParameters:p, q, g cannot be null");

            string ipwk = "{\"ip\":{\"uidp\":\"aHR0cDovL2lzc3Vlci91cHJvdmUvaXNzdWVycGFyYW1zL3NvZnR3YXJl\",\"descGq\":{\"type\":\"sg\",\"sgDesc\":{\"p\":\"0hro1m5sazztDrPfGibJG97tATwX2EnTDsMJgT5NN5nybbDUlOguxh6p\\/ccLtcvK8uXxioNklPWOZ8bWFkgMN6fyMGEB\\/J8PR2j5yXk8K+F2sLfJebQGXT6DVoaj8LhCDGg0yxeTA4be2rKwfdRzRJpIuqsxYoa0IQUkddE0zTs=\",\"q\":\"\\/\\/gK4Z2uvGH0Y1avCTXcDoEUjrE=\",\"g\":\"q87Jcump3Y0TMnDP6sJvcm5WfZZHV2MNa9Q0YNCSOkauwKziVevz3dSxxCZPU+aLNhr7d3oTzwBn2uNko01VoJZabMz3iFJ4KSOBPPhwiDTZH2VX14PsdbXzfNkYXwJ7BCwccuEhsSZqQIvgu3Jw1lkXtpCDYz4fPNYGJGEvyME=\"}},\"uidh\":\"SHA1\",\"g\":[\"ybxHPsPkePTYYMiUGETQ822iQg+85hvv06Z8flO9e1YNZfS5r7OhKpeynQG0UJ\\/ESR4yLv8dhPQbcP\\/q\\/jeRNDMo0ADilNP6hG4X1DAR4zBkSxD6lYZdiHwR+6AIW06OzfL\\/kHKMFBlrgoRCH+XBb3krgp4AQbPgBwNA0Qoo2UY=\",\"pdo\\/xldIiFccsQHzwAAWdovZvqMyaUgACRaCKSne9tvO1hmT2Bq9pSx7ijv7tRuXxZgkCsK\\/jD3dw2iUGh\\/7GBxaOIM80aMuMZVq+nfwbH0Jnq5VfOd31V2Bu1Zk32QgYIIihKpF8tB1UMpMDnA3GMRMDg8HS9zylU4nRtvFR+g=\",\"OJAOz01vw+3Z1jKhOVr3oiRxELLbpg956KXik\\/Lhmmxra9Q7kSkD0fvmw6rOLNcnlxTeF9MXWVn6qxUvYRjh1ySHDLqF4l3o6UnzamMCZhtqnwoLFAnYxFy50ZDJwXIxZSpWDiolM8MKouLuav94mTjZRTTyfFAkKsqtbk1eo5U=\",\"IkJR\\/dTsz3e5V5QGSXbDd1b95OGHOmaXPA\\/cpkSYnqjmM190c5yCdDYrp1nxRd01me2gYEvWyCgvmAzAJFbWq\\/UTEzV+SoXbTfK9NgqavtCqftSLEmAE7abbLxMCJJORPLkTcuvdvyr2etlbJkJd0tcr58NngFDnFG6fVD2IZ7k=\",\"PhDwzgR9D+BSwgnwgswy6m6i\\/8B6\\/ToPtbBLEUw4g81wrL5FlwDnr53sXdiEdCPd\\/JIJVx62u8tZhbTZhxEVVpyCO4OPC1FW9lTyactU5fsrDoxix1oVD6hBaDYleWfH6i8EXZJaWdVwAWN\\/dh5Fil9dg8QM5RGeB7DFWAgLhNQ=\"],\"e\":\"AQEB\",\"s\":\"YXBwbGljYXRpb24tc3BlY2lmaWMgc3BlY2lmaWNhdGlvbg==\"},\"key\":\"JqhFTJQnvemGaFyAa+Zl+CbRuls=\"}";

            TestError<IssuerKeyAndParameters>(ip, RemoveObject(ipwk, "ip"), "IssuerKeyAndParameters:ip");
            TestError<IssuerKeyAndParameters>(ip, RemoveValue(ipwk, "key"), "IssuerKeyAndParameters:key");
            TestError<IssuerKeyAndParameters>(ip, RemoveValue(ipwk, "uidp"), "IssuerKeyAndParameters:uidp");
            TestError<IssuerKeyAndParameters>(ip, RemoveValue(ipwk, "uidh"), "IssuerKeyAndParameters:uidh");
            TestError<IssuerKeyAndParameters>(ip, RemoveArrayValue(ipwk, "g"), "IssuerKeyAndParameters:g");
            TestError<IssuerKeyAndParameters>(ip, RemoveObject(ipwk, "descGq"), "IssuerKeyAndParameters:descGq");
            TestError<IssuerKeyAndParameters>(ip, ipJson, "IssuerKeyAndParameters:ip");
            TestError<IssuerKeyAndParameters>(ip, "{{}", "IssuerKeyAndParameters");

            string message1 = "{\"sa\":[\"LnaEgB1ha9PIZ2DTTZ4CyN4rT6XZGBPAkpO5MMnvIa9mZTgH3wAcRJzHxZIMYw7YMTEZglpxlixzwAxb+iZsHu8V8iqa2olRlw+MOVOcqhSWKtqCTnaK8FZQaV69QosrhvWPntopm5I6Sj\\/x4Krv2Ln4sHHgxSRdLzPQTHIFMy0=\",\"ivd8mtzHTUsEk99FWaOpDcjMw1rfhMzZopv7Uo0zvIBSK3J9KXy71+D9+v8yzGr3G3l0LDo9Ha\\/tN8XY\\/AycvgvLHxNtFjBO2st8K2YApQAF2RMbyABYCKrKihWcBXDqBTVnMD\\/BI5Mq6NjGUmEuIaV9juYJIUkmqb4MBjhvLIw=\",\"nrr+yim8PJDtBq0gcmGOZYLqvjoKEVoq0nGqo\\/ijHLhpKx3GLnITte4QJzDuQEd\\/dPHKO\\/s\\/BH+IArFE6huVYSPaFO6qLd1Z5UrKAwd2RZd3rmJbIRevQSM70RbQUAM4P7jOYzSu3fTKvdYU+W7mDRlZf7KM8WYssTaSNXRYvqI=\",\"SJ6w9zTgwN8WgUmdFT9OkWIUdCTPN5mzfub22ZogtpgvUGH8hsNjFkVveO5XKiC2QUoAHblcBkwSt2kCN6BPnhKU5RxPtZe7JKeAp4taoMGYSLv7Co8aE7LrxIFpk1A2NSBMANcfpN8yDMDqS3vP+gpe4aEkP\\/gTwZ\\/VPEIB6aI=\",\"Nczg7v1RmoNVHiKKvXCdk8jvCLc96JU\\/lgjhl8iRGzVf8YvmgP7MiFydkNVVbhK5\\/PNP1N5Df++CSaewzyD1p0AN7p+B33G9dU7BE\\/0IiaXELjMl7YJEYc48Zh0utZcfvfdPf\\/8gUXIpXKbRDrC9MaH0a5C6IemaafCmct9EcU0=\"],\"sb\":[\"B1CJQyeH1AYi2VoMPFds4OULxZEZnO+qSHV9i99pXdlTidimZR08bjngdvwpc6ISfmPZbArGBx4WgNN6zOrMInql3MZhvVc\\/lBkKvt\\/UktAotWS\\/pMCwW6dcVABhcBuGyZYfu6\\/ywzV3PMun9nVY4GPRSWu7mpw1CjaN\\/lrvnY8=\",\"hVThpS6Rlsvpg4f4LEROBBJP2K4B5AKHaaSKID2MSYpJX+VW0cFM+w6OkoSfgnKT+uPyOssTuHW9yqKc8n1hyJ5d4i7yrau27GEWTK1Sy53jj2lYCnTFc5f2rb4IVm\\/gnT795TUa2TOwVnDH\\/boma\\/8HNvyAoCg9iEZq29pZXtQ=\",\"qf3THOi8qNiIZ3F2Upbpho0EPJukxpTWZrCn1AU5mf+pfYs0h6TC0HwAGzVqDRQ6XccM+yt0w1YoYwqW+Nn1nwf4kwnGoelru8Jotndsp\\/V5k7wY1OTIE8dDK8Ki+weMuWiAQegcPs+NxFXwjwnPf0IzNr3dA3R4\\/BrT5Ks8Aig=\",\"vwKZ2JAw9Z7oEWZi7uBsVlb\\/NitrOy+BNHMMhihWBr\\/YmFu52h3rvZfUbQeI17XjdvmqBlsPbBs34dRmHRh4SiaP5cxwqxv\\/IKmOt0lJQj0ivIKPaE9rhBraDhRGE6biNllsAjQGFvbG\\/EYVkaA1wbo++ox0iS6CZBrjU65l\\/Y8=\",\"xHPY++6Yc8RvfuBJ+fMsVSi7IxbtTQWiaSxaoGSENjs6rZnDK+5Jvhk2\\/fnxs2G2Xk6DECy7NrAKNJpWQTDQiAGkqBS0LdewSo07cYnTvV+dKcGjb7nOQtCyxxRv1RX42bP16ejZt8Ff0rcSEOn3SgS3tWhUf3wXachYR3uPxZo=\"],\"sz\":\"hI447D7Y2b6UyTyzW7zW9Ca1qCvvCBPPI+YiFr4ezVzoYddXsqsJhx6+qEerd3\\/cg4uo3loeIAYX8wq411n75fLOZcFSCcGm\\/005BwswFgCCrPr88YJejIbhd0kRPSzKalbGieM5AusE3hfwWV3JxkRxLHuhOLvqJUSlUbzNYME=\"}";

            TestError<FirstIssuanceMessage>(ip, RemoveValue(message1, "sz"), "FirstIssuanceMessage:sz");
            TestError<FirstIssuanceMessage>(ip, RemoveArrayValue(message1, "sa"), "FirstIssuanceMessage:sa");
            TestError<FirstIssuanceMessage>(ip, RemoveArrayValue(message1, "sb"), "FirstIssuanceMessage:sb");
            
            string message2 = "{\"sc\":[\"LH+f1uiiftJmIdnG\\/J4KpewQ+g8=\",\"bNwhXE4QksTwTu644oquCxod7hQ=\",\"1jeLI4Mp70sRQ77rX6El2lJYcyI=\",\"z68c60h+c3FXadIHtxzfCE6zn1A=\",\"uxsN8PgR+K985feVRXZkFvGJBo8=\"]}";

            TestError<SecondIssuanceMessage>(ip, RemoveArrayValue(message2, "sc"), "SecondIssuanceMessage:sc");

            string message3 = "{\"sr\":[\"Sip3P3szv\\/SJeYp4wFmkK1vxPJk=\",\"2KDPrSFqIZoI+CIMwikcX6zoSXM=\",\"4W40+xYlIdpey8dMuu\\/qTeOmVN4=\",\"mhZuV0KTpVlBjHILiHJIKg6Xn4s=\",\"mb89DCWD9XRVD1afjsZPd0snVSY=\"]}";

            TestError<ThirdIssuanceMessage>(ip, RemoveArrayValue(message3, "sr"), "ThirdIssuanceMessage:sr");

            string token = "{\"uidp\":\"aHR0cDovL2lzc3Vlci91cHJvdmUvaXNzdWVycGFyYW1zL3NvZnR3YXJl\",\"h\":\"WjQh6FGhkibh+YsFF2E4+CJaKq7goeQ8+jlNFQ+hxJ2orzRiGxlRRlDuGxDaaDrwaPQdtJ6yEhKNCNliJOdsloMx5osNr5FiGKE5CHst7qev9VYcNRNdmUjzcEHnLF1VnSjpMMu9XLUS80enARJWBEWtrh8t2egcgYFwxhsaUxQ=\",\"ti\":\"dG9rZW4gaW5mb3JtYXRpb24gdmFsdWU=\",\"pi\":\"cHJvdmVyIGluZm9ybWF0aW9uIHZhbHVl\",\"szp\":\"zDuCnvtnFc+sXYbURzlz6qe+8xiz2E8d3owf8I98OaEiU3+ar\\/3DZnC906ih\\/a4wmTt3mRB4vuWA2NQk5KpapTArJZEy43\\/nvf1rybMdVETvofyvN6RzsoHi9kkaXDwRe5twUfBmithSjzyHVHIvwq\\/gvxMIo1XrX9Ikg\\/1SHBo=\",\"scp\":\"W6kIN3v5zOlVM9B4rqMkYdGw4zc=\",\"srp\":\"S4XVtuR\\/R\\/\\/cIzawbaPEnnkUePI=\",\"d\":false}";

            TestError<UProveToken>(ip, RemoveValue(token, "uidp"), "UProveToken:uidp");
            TestError<UProveToken>(ip, RemoveValue(token, "h"), "UProveToken:h");
            TestError<UProveToken>(ip, RemoveValue(token, "szp"), "UProveToken:szp");
            TestError<UProveToken>(ip, RemoveValue(token, "scp"), "UProveToken:scp");
            TestError<UProveToken>(ip, RemoveValue(token, "srp"), "UProveToken:srp");

            string jsonProof = "{\"D\":[\"c2Vjb25kIGF0dHJpYnV0ZSB2YWx1ZQ==\"],\"a\":\"dxEDnCg8ZJBfNYcvZ6adR9Cmljs=\",\"ap\":\"pWkkbWldMiaxD3kGMFRnYVHAnVw=\",\"Ps\":\"G+kX80T0Dz79pSY48SanhvW59D5z7LUC2S7Cvc63duKVjlRU2XNLc0E2mUZ14JUgirLSVocJlUz0SuMI+y6FfOhFzbxE0pKN42Dpkl92FM8w\\/KiYgvuc7zaiWB1aQBKFbjyAz\\/7cWOBxQHTerMQdCWMeHwriCJijwz7obbNtR30=\",\"r0\":\"mLU0IRB5BcNB2i614WWsVvdzt2c=\",\"r\":[\"P69vzesNNKY\\/emnJj7pk81eQVbA=\",\"sInq0AmnvyyoVIk7U9\\/cLv0MvEU=\"]}";

            TestError<PresentationProof>(ip, RemoveArrayValue(jsonProof, "D"), "PresentationProof:D");
            TestError<PresentationProof>(ip, RemoveValue(jsonProof, "a"), "PresentationProof:a");
            TestError<PresentationProof>(ip, RemoveArrayValue(jsonProof, "r"), "PresentationProof:r");
        }


        public void TestError<T>(IssuerParameters issuerParameters, string json, string expectedError)
        {
            try
            {
                issuerParameters.Deserialize<T>(json);
                Assert.Fail("should fail");
            }
            catch (SerializationException exp)
            {
                Assert.AreEqual(expectedError, exp.Message);
            }
        }


        public string RemoveValue(string json, string value)
        { 
            // removes a value  "value":"somedata"
            string pattern = "\"" + value + @"\s*""\s*:\s*""[^""]+""\s*,?";
            return System.Text.RegularExpressions.Regex.Replace(json, pattern, "");
        }

        public string RemoveBool(string json, string value)
        {
            // removes a value  "value":"somedata"
            string pattern = "\"" + value + @"\s*""\s*:\s*(false|true)\s*,?";
            return System.Text.RegularExpressions.Regex.Replace(json, pattern, "");
        }

        public string RemoveArrayValue(string json, string value)
        {
            // removes a value  "value":"somedata"
            string pattern = "\"" + value + @"\s*""\s*:\s*\[[^\[\]]+\]\s*,?";
            return System.Text.RegularExpressions.Regex.Replace(json, pattern, "");
        }

        public string RemoveObject(string json, string value)
        {
            string pattern = "\"" + value + @""":{";
            int start = json.IndexOf(pattern);

            if (start < 1)
                throw new InvalidOperationException("could not find " + pattern);

            int index = start + pattern.Length;

            int matchCount = 1;

            int i = index;

            for (; i < json.Length; i++)
            {
                char c = json[i];
                if (c == '{') matchCount++;
                if (c == '}') matchCount--;

                if (matchCount == 0) break;
            }

            // also remove a trailing comma
            if (i+1 < json.Length && json[i + 1] == ',')
                i++;

            return json.Remove(start, i - start + 1);
        }

        public void VerifySerialization<T>(bool useCustomGroup, bool useSubroupConstruction, IssuerParameters ip, T obj)
        {
            // serialize the object to json string
            string json = ip.Serialize(obj);

            if (CREATE_SERIALIZATION_TEST_FILES)
                WriteSerializationTestFile(useCustomGroup, useSubroupConstruction, json, obj);

            // output the serialization string
            Debug.WriteLine(typeof(T).Name);
            //Debug.WriteLine(FormatJSON(json, ""));

            // check that we didn't send any null fields
            Assert.IsFalse(json.Contains(":null"));

            // deserialize the object into a new object of the same type
            T deserialized_object = ip.Deserialize<T>(json);

            // verify the new object is equal to the original

            if (typeof(T) == typeof(IssuerParameters))
            {
                Assert.IsTrue(Verify(obj as IssuerParameters, deserialized_object));
            }

            if (typeof(T) == typeof(IssuerKeyAndParameters))
            {
                Assert.IsTrue(Verify(obj as IssuerKeyAndParameters, deserialized_object));
            }

            if (typeof(T) == typeof(FirstIssuanceMessage))
            {
                Assert.IsTrue(Verify(obj as FirstIssuanceMessage, deserialized_object));
            }

            if (typeof(T) == typeof(SecondIssuanceMessage))
            {
                Assert.IsTrue(Verify(obj as SecondIssuanceMessage, deserialized_object));
            }

            if (typeof(T) == typeof(ThirdIssuanceMessage))
            {
                Assert.IsTrue(Verify(obj as ThirdIssuanceMessage, deserialized_object));
            }

            if (typeof(T) == typeof(UProveKeyAndToken))
            {
                Assert.IsTrue(Verify(obj as UProveKeyAndToken, deserialized_object));
            }

            if (typeof(T) == typeof(UProveToken))
            {
                Assert.IsTrue(Verify(obj as UProveToken, deserialized_object));
            }

            if (typeof(T) == typeof(PresentationProof))
            {
                Assert.IsTrue(Verify(obj as PresentationProof, deserialized_object));
            }

            // replace the original with the new object
            obj = deserialized_object;

        }

        private void WriteSerializationTestFile<T>(bool useCustomGroup, bool useSubgroupConstruction, string json, T obj)
        {
            FileStream fs = File.Open(Path.GetRandomFileName() + ".dat", FileMode.Create);

            BinaryFormatter bf = new BinaryFormatter();
            object[] objects = new object[4];
            objects[0] = useCustomGroup;
            objects[1] = useSubgroupConstruction;
            objects[2] = obj.GetType().FullName;
            objects[3] = json;
            bf.Serialize(fs, objects);

            fs.Close();
        }

        // check that two objects are of the same type
        public static T GetSameType<T, K>(T object1, K object2) where T : class
        {
            // if one or the other is null return false
            if ((object1 == null) && (object2 != null))
                return null;
            if ((object1 != null) && (object2 == null))
                return null;

            T obj = object2 as T;
            if (obj == null)
                return null;

            return object2 as T;
        }

        public static bool CompareFields<T>(T[] array1, T[] array2)
        {
            if (array1 == null & array2 == null)
                return true;

            if (array1 == null & array2 != null)
                return false;

            if (array1 != null & array2 == null)
                return false;

            // if we're comparing the same object, then we probably messed up somewhere
            if (Object.ReferenceEquals(array1, array2))
                return true;

            if (array1.Length != array2.Length)
                return false;

            for (int i = 0; i < array1.Length; i++)
            {
                
                if (array1[i].Equals(array2[i]) == false)
                    return false;

            }

            return true;
        }

        public static bool CompareFields<T>(T field1, T field2)
        {
            if (field1 == null & field2 == null)
                return true;

            if (field1 == null & field2 != null) 
                return false;

            if (field1 != null & field2 == null)
                return false;

            // if we're comparing the same object, then we probably messed up somewhere
            if (Object.ReferenceEquals(field1, field2))
                return true;

             return field1.Equals(field2);
        }

        public static bool Verify(IssuerParameters ip1, object obj)
        {
            IssuerParameters ip2 = GetSameType(ip1, obj);

            if (ip2 == null)
                return false;

            if (Object.ReferenceEquals(ip1, obj))
                return true;

            if (CompareFields(ip2.UidH, ip1.UidH) == false)
                return false;

            if (CompareFields(ip2.UidP, ip1.UidP) == false)
                return false;

            if (CompareFields(ip2.Gq, ip1.Gq) == false)
                return false;

            if (CompareFields(ip1.S, ip2.S) == false)
                return false;

            if (CompareFields<GroupElement>(ip1.Gd, ip2.Gd) == false)
                return false;

            if (CompareFields(ip1.G, ip2.G) == false)
                return false;

            return true;
        }

        public static bool Verify(IssuerKeyAndParameters ipk1, object obj)
        {
            IssuerKeyAndParameters ipk2 = GetSameType(ipk1, obj);

            if (ipk2 == null)
                return false;

            if (Object.ReferenceEquals(ipk1, ipk2))
                return true;

            if (Verify(ipk1.IssuerParameters, ipk2.IssuerParameters) == false)
                return false;

            if (CompareFields(ipk1.PrivateKey, ipk2.PrivateKey) == false)
                return false;

            return true;
        }

        public static bool Verify(FirstIssuanceMessage fim1, object obj)
        {
            FirstIssuanceMessage fim2 = GetSameType(fim1, obj);

            if (fim2 == null)
                return false;

            if (Object.ReferenceEquals(fim1, fim2))
                return true;

            if (CompareFields(fim1.sigmaA, fim2.sigmaA) == false)
                return false;

            if (CompareFields(fim1.sigmaB, fim2.sigmaB) == false)
                return false;

            if (CompareFields(fim1.sigmaZ, fim2.sigmaZ) == false)
                return false;

            return true;
        }

        public static bool Verify(SecondIssuanceMessage sim1, object obj)
        {
            SecondIssuanceMessage sim2 = GetSameType(sim1, obj);

            if (sim2 == null)
                return false;

            if (Object.ReferenceEquals(sim1, sim2))
                return true;

            if (CompareFields(sim1.sigmaC, sim2.sigmaC) == false)
                return false;

            return true;
        }

        public static bool Verify(ThirdIssuanceMessage tim1, object obj)
        {
            ThirdIssuanceMessage tim2 = GetSameType(tim1, obj);

            if (tim2 == null)
                return false;

            if (Object.ReferenceEquals(tim1, tim2))
                return true;

            if (CompareFields(tim1.sigmaR, tim2.sigmaR) == false)
                return false;

            return true;
        }

        public static bool Verify(UProveKeyAndToken ukat, object obj)
        {
            if (obj == null)
                return false;

            if (Object.ReferenceEquals(obj, ukat))
                return true;

            UProveKeyAndToken upkt_obj = obj as UProveKeyAndToken;
            if (upkt_obj == null)
                return false;

            if (upkt_obj.PrivateKey.Equals(ukat.PrivateKey) == false)
                return false;

            if (Verify(ukat.Token, upkt_obj.Token) == false)
                return false;

            return true;
        }

        public static bool Verify(UProveToken upt1, object obj)
        {
            UProveToken upt2 = GetSameType(upt1, obj);

            if (upt2 == null)
                return false;

            if (Object.ReferenceEquals(upt1, upt2))
                return true;

            if (CompareFields(upt2.Uidp, upt1.Uidp) == false)
                return false;

            if (CompareFields(upt2.H, upt1.H) == false)
                return false;

            if (CompareFields(upt2.TI, upt1.TI) == false)
                return false;

            if (CompareFields(upt2.PI, upt1.PI) == false)
                return false;

            if (CompareFields(upt2.SigmaZPrime, upt1.SigmaZPrime) == false)
                return false;

            if (CompareFields(upt2.SigmaCPrime, upt1.SigmaCPrime) == false)
                return false;

            if (CompareFields(upt2.SigmaRPrime, upt1.SigmaRPrime) == false)
                return false;

            if (upt2.IsDeviceProtected != upt1.IsDeviceProtected)
                return false;

            return true;

        }

        public static bool Verify(PresentationProof pp1, object obj)
        {
            PresentationProof pp2 = GetSameType(pp1, obj);

            if (pp2 == null)
                return false;

            if (Object.ReferenceEquals(pp1, pp2))
                return true;

            if (pp1.DisclosedAttributes.Length != pp2.DisclosedAttributes.Length)
                return false;

            for (int i = 0; i < pp1.DisclosedAttributes.Length; i++)
            {
                if (CompareFields(pp1.DisclosedAttributes[i], pp2.DisclosedAttributes[i]) == false)
                    return false;
            }

            //if (CompareFields(pp1.DisclosedAttributes, pp2.DisclosedAttributes) == false)
            //    return false;

            if (CompareFields(pp2.A, pp1.A) == false)
                return false;

            if (CompareFields(pp2.Ap, pp1.Ap) == false)
                return false;

            if (CompareFields(pp2.Ps, pp1.Ps) == false)
                return false;

            if (CompareFields(pp2.R, pp1.R) == false)
                return false;

            if (CompareFields(pp2.Commitments, pp1.Commitments) == false)
                return false;

            return true;
        }

        public string FormatJSON(string json, string pad)
        {
            int start = json.IndexOf('{');
            if (start < 0)
                return pad + json;

            int end = json.LastIndexOf('}');
            int count = (end - 1) - (start + 1);

            string pre = json.Substring(0, start) + "{" + Environment.NewLine;

            string mid = pad + FormatJSON(json.Substring(start + 1, count), pad + "    ") + Environment.NewLine + pad + "}" + Environment.NewLine; ;

            string post = json.Substring(end + 1, json.Length - (end + 1));

            return pre + mid + post;
        }
    }
}
