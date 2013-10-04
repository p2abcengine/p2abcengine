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
using UProveCrypto;
using UProveCrypto.Math;

namespace UProveUnitTest
{
    
    
    /// <summary>
    ///This is a test class for GroupTest and is intended
    ///to contain all GroupTest Unit Tests
    ///</summary>
    [TestClass()]
    public class SubgroupGroupTest
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
        ///A test for VerifyGroup
        ///</summary>
        [TestMethod()]
        public void VerifyGroupTest()
        {
            SubgroupGroup Gq = SubgroupGroup.CreateSubgroupGroup(
                StaticTestHelpers.IntToBigEndianBytes(2), // p
                StaticTestHelpers.IntToBigEndianBytes(2), // q
                StaticTestHelpers.IntToBigEndianBytes(0), // g
                null,
                null);
            try { Gq.Verify(); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            Gq = SubgroupGroup.CreateSubgroupGroup(
                            StaticTestHelpers.IntToBigEndianBytes(6), // p not prime
                            StaticTestHelpers.IntToBigEndianBytes(2), // q
                            StaticTestHelpers.IntToBigEndianBytes(0), // g
                            null,
                            null);
            try { Gq.Verify(); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            Gq = SubgroupGroup.CreateSubgroupGroup(
                            StaticTestHelpers.IntToBigEndianBytes(7), // p
                            StaticTestHelpers.IntToBigEndianBytes(2), // q
                            StaticTestHelpers.IntToBigEndianBytes(0), // g
                            null,
                            null);
            try { Gq.Verify(); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            Gq = SubgroupGroup.CreateSubgroupGroup(
                            StaticTestHelpers.IntToBigEndianBytes(7), // p
                            StaticTestHelpers.IntToBigEndianBytes(4), // q not prime
                            StaticTestHelpers.IntToBigEndianBytes(0), // g
                            null,
                            null);
            try { Gq.Verify(); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            Gq = SubgroupGroup.CreateSubgroupGroup(
                            StaticTestHelpers.IntToBigEndianBytes(7), // p
                            StaticTestHelpers.IntToBigEndianBytes(5), // q doesnt divide p - 1
                            StaticTestHelpers.IntToBigEndianBytes(0), // g
                            null,
                            null);
            try { Gq.Verify(); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            Gq = SubgroupGroup.CreateSubgroupGroup(
                            StaticTestHelpers.IntToBigEndianBytes(7), // p
                            StaticTestHelpers.IntToBigEndianBytes(3), // q
                            StaticTestHelpers.IntToBigEndianBytes(0), // g
                            null,
                            null);
            try { Gq.Verify(); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            Gq = SubgroupGroup.CreateSubgroupGroup(
                            StaticTestHelpers.IntToBigEndianBytes(7), // p
                            StaticTestHelpers.IntToBigEndianBytes(3), // q
                            StaticTestHelpers.IntToBigEndianBytes(1), // g invalid value
                            null,
                            null);
            try { Gq.Verify(); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            Gq = SubgroupGroup.CreateSubgroupGroup(
                StaticTestHelpers.IntToBigEndianBytes(7), // p
                StaticTestHelpers.IntToBigEndianBytes(3), // q
                StaticTestHelpers.IntToBigEndianBytes(5), // g^q mod p != 1
                null,
                null);
            try { Gq.Verify(); Assert.Fail(); }
            catch (InvalidUProveArtifactException) { }

            // Valid subgroup description
            Gq = SubgroupGroup.CreateSubgroupGroup(
                StaticTestHelpers.IntToBigEndianBytes(7), // p
                StaticTestHelpers.IntToBigEndianBytes(3), // q
                StaticTestHelpers.IntToBigEndianBytes(2), // g
                null,
                null);
            Gq.Verify();
        }

    }
}
