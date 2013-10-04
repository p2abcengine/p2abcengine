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
using UProveCrypto;
using UProveCrypto.Math;

namespace UProveUnitTest
{
    
    
    /// <summary>
    ///This is a test class for IssuerTest and is intended
    ///to contain all IssuerTest Unit Tests
    ///</summary>
    [TestClass()]
    public class IssuerTest
    {


        private TestContext testContextInstance;

        /// <summary>
        ///Gets or sets the test context which provides
        ///information about and functionality for the current test run.
        ///</summary>
        public TestContext TestContext
        {
            get
            {
                return testContextInstance;
            }
            set
            {
                testContextInstance = value;
            }
        }

        #region Additional test attributes
        // 
        //You can use the following additional attributes as you write your tests:
        //
        //Use ClassInitialize to run code before running the first test in the class
        //[ClassInitialize()]
        //public static void MyClassInitialize(TestContext testContext)
        //{
        //}
        //
        //Use ClassCleanup to run code after all tests in a class have run
        //[ClassCleanup()]
        //public static void MyClassCleanup()
        //{
        //}
        //
        //Use TestInitialize to run code before running each test
        //[TestInitialize()]
        //public void MyTestInitialize()
        //{
        //}
        //
        //Use TestCleanup to run code after each test has run
        //[TestCleanup()]
        //public void MyTestCleanup()
        //{
        //}
        //
        #endregion

        [TestMethod()]
        public void TestIssuerSetupParameters()
        {
            System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();

            byte[][] A = new byte[][] { encoding.GetBytes("attribute value") };
            byte[] TI = encoding.GetBytes("TI value");
            IssuerSetupParameters isp = new IssuerSetupParameters();
            isp.GroupConstruction = GroupType.Subgroup;
            isp.UidP = encoding.GetBytes("UIDP value");
            isp.E = new byte[] { 1 };
            IssuerKeyAndParameters ikap = isp.Generate();

            ikap.IssuerParameters.Verify();

            // invalidate the issuer parameters

            IssuerParameters ip = ikap.IssuerParameters;
            SubgroupGroupElement sgG0 = (SubgroupGroupElement) ip.G[0];
            byte[] g0Bytes = ip.G[0].GetEncoded();
            g0Bytes[g0Bytes.Length - 1]++;
            ip.G[0] = (SubgroupGroupElement) ip.Gq.CreateGroupElement(g0Bytes);

            try
            {
                ip.Verify();
                Assert.Fail();
            }
            catch (InvalidUProveArtifactException) { }
            
        }

        /// <summary>
        ///A test for Issuer
        ///</summary>
        [TestMethod()]
        public void TestIssuer()
        {
            byte[][] A = new byte[][] {};
            byte[] TI = null;
            IssuerSetupParameters isp = new IssuerSetupParameters();
            isp.UidP = new byte[] { 0 };
            isp.E = new byte[] { 0 };
            IssuerKeyAndParameters ikap = isp.Generate();
            IssuerProtocolParameters ipp;

            try { ipp = new IssuerProtocolParameters(null); Assert.Fail(); } catch (ArgumentNullException) { }
            try 
            {
                ipp = new IssuerProtocolParameters(ikap);
                ipp.Attributes = A;
                ipp.NumberOfTokens = -1;
                ipp.TokenInformation = TI;
                ipp.Validate();
                Assert.Fail();
            } catch (ArgumentException) { }
            try
            {
                ipp = new IssuerProtocolParameters(ikap);
                ipp.Attributes = A;
                ipp.NumberOfTokens = 0;
                ipp.TokenInformation = TI;
                ipp.Validate();
                Assert.Fail(); 
            } catch (ArgumentException) { }
            ipp = new IssuerProtocolParameters(ikap);
            ipp.Attributes = A;
            ipp.NumberOfTokens = 1;
            ipp.TokenInformation = TI;
            ipp.Validate();
            Issuer issuer = ipp.CreateIssuer();

            FirstIssuanceMessage msg1 = null;
            SecondIssuanceMessage msg2 = null;
            ThirdIssuanceMessage msg3 = null;
            
            try { msg3 = issuer.GenerateThirdMessage(msg2); Assert.Fail(); } catch (InvalidOperationException) { }
            msg1 = issuer.GenerateFirstMessage();
            try { msg1 = issuer.GenerateFirstMessage(); Assert.Fail(); } catch (InvalidOperationException) { }
            msg2 = new ProverProtocolParameters(ikap.IssuerParameters).CreateProver().GenerateSecondMessage(msg1);
            msg3 = issuer.GenerateThirdMessage(msg2);
            try { msg3 = issuer.GenerateThirdMessage(msg2); Assert.Fail(); } catch (InvalidOperationException) { }
        }


        /// <summary>
        ///A test for Issuer state export
        ///</summary>
        [TestMethod()]
        public void TestIssuerStateExport()
        {
            IssuerKeyAndParameters ikap;
            IssuerProtocolParameters ipp;
            ProverProtocolParameters ppp;
            StaticTestHelpers.GenerateTestIssuanceParameters("test issuer params", "test spec", 3, true, 6, out ikap, out ipp, out ppp);

            // generate test issuer parameters
            Issuer issuer = ipp.CreateIssuer();
            IssuerParameters ip = ikap.IssuerParameters;
            Prover prover = ppp.CreateProver();

            FirstIssuanceMessage msg1 = null;
            SecondIssuanceMessage msg2 = null;
            ThirdIssuanceMessage msg3 = null;

            msg1 = issuer.GenerateFirstMessage();
            PostFirstMessageState state = issuer.ExportPostFirstMessageState();
            string serializedState = ikap.IssuerParameters.Serialize(state);
            msg2 = prover.GenerateSecondMessage(msg1);
            
            // make sure the original issuer is unusable
            try
            {
                issuer.GenerateThirdMessage(msg2);
                Assert.Fail();
            }
            catch (Exception)
            {
                // expected
            }

            // complete the issuance with a new issuer instance
            Issuer issuer2 = new Issuer(ikap, ikap.IssuerParameters.Deserialize<PostFirstMessageState>(serializedState));
            msg3 = issuer2.GenerateThirdMessage(msg2);
            // make sure tokens are valid by completing the issuance
            prover.GenerateTokens(msg3);
        }



    }
}
