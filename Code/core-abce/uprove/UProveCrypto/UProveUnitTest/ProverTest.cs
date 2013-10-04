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

// ignore warnings for obsolete methods
#pragma warning disable 0618

namespace UProveUnitTest
{
    
    
    /// <summary>
    ///This is a test class for ProverTest and is intended
    ///to contain all ProverTest Unit Tests
    ///</summary>
    [TestClass()]
    public class ProverTest
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

        /// <summary>
        ///A test for Prover
        ///</summary>
        [TestMethod()]
        public void TestProver()
        {
            byte[][] A = new byte[][] { };
            byte[] TI = null;
            byte[] PI = null;
            IssuerSetupParameters isp = new IssuerSetupParameters();
            isp.UidP = new byte[] { 0 };
            isp.E = new byte[] { 0 };
            IssuerKeyAndParameters ikap = isp.Generate();
            IssuerProtocolParameters ipp = new IssuerProtocolParameters(ikap);
            ipp.Attributes = A;
            ipp.NumberOfTokens = 1;
            ipp.TokenInformation = TI;
            Issuer issuer = ipp.CreateIssuer();

            FirstIssuanceMessage msg1 = null;
            SecondIssuanceMessage msg2 = null;
            ThirdIssuanceMessage msg3 = null;
    
            msg1 = issuer.GenerateFirstMessage();
            try { new Prover(null, 1, A, TI, PI, null); Assert.Fail(); } catch (ArgumentNullException) { }
            try { new Prover(ikap.IssuerParameters, -1, A, TI, PI, null); Assert.Fail(); } catch (ArgumentException) { }
            try { new Prover(ikap.IssuerParameters, 0, A, TI, PI, null); Assert.Fail(); } catch (ArgumentException) { }
            Prover prover = new Prover(ikap.IssuerParameters, 1, A, TI, PI, null);
            try { prover.GenerateTokens(msg3); Assert.Fail(); } catch (InvalidOperationException) { }
            msg2 = prover.GenerateSecondMessage(msg1);
            try { msg2 = prover.GenerateSecondMessage(msg1); Assert.Fail(); } catch (InvalidOperationException) { }
            msg3 = issuer.GenerateThirdMessage(msg2);
            prover.GenerateTokens(msg3);
            try { prover.GenerateTokens(msg3); Assert.Fail(); } catch (InvalidOperationException) { }
        }


        /// <summary>
        ///A test for Prover state export
        ///</summary>
        [TestMethod()]
        public void TestProverStateExport()
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
            msg2 = prover.GenerateSecondMessage(msg1);

            PostSecondMessageState state = prover.ExportPostSecondMessageState();
            string serializedState = ip.Serialize(state);

            // complete the issuance with a new prover instance
            msg3 = issuer.GenerateThirdMessage(msg2);
            Prover prover2 = new Prover(ip, ip.Deserialize<PostSecondMessageState>(serializedState));
            prover2.GenerateTokens(msg3);
            // make sure the original prover is unusable
            try
            {
                prover.GenerateTokens(msg3);
                Assert.Fail();
            }
            catch (Exception)
            {
                // expected
            }
        
        }



    }
}
