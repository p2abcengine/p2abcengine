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
using UProveCrypto.Math;

namespace UProveUnitTest
{
    /// <summary>
    ///This is a test class for FieldZqTest and is intended
    ///to contain all FieldZqTest Unit Tests
    ///</summary>
    [TestClass()]
    public class FieldZqTest
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
        ///A test for Inverse
        ///</summary>
        [TestMethod()]
        public void InverseTest()
        {
            FieldZq Zq = FieldZq.CreateFieldZq(new byte[] { 0x05 });
            try 
            { 
                FieldZqElement inverse = Zq.Zero.Invert(); 
                Assert.Fail(); 
            } 
            catch (ArgumentOutOfRangeException) 
            { 
            };

            Assert.AreEqual<FieldZqElement>(Zq.One.Invert(), Zq.One);
            Assert.AreEqual<FieldZqElement>(Zq.GetElement(2).Invert(), Zq.GetElement(3));
            Assert.AreEqual<FieldZqElement>(Zq.GetElement(3).Invert(), Zq.GetElement(2));
            Assert.AreEqual<FieldZqElement>(Zq.GetElement(4).Invert(), Zq.GetElement(4));
        }

        /// <summary>
        ///A test for Negate
        ///</summary>
        [TestMethod()]
        public void NegateTest()
        {
            FieldZq Zq = FieldZq.CreateFieldZq(new byte[] { 0x05 });
            Assert.AreEqual<FieldZqElement>(Zq.Zero.Negate(), Zq.Zero);
            Assert.AreEqual<FieldZqElement>(Zq.GetElement(1).Negate(), Zq.GetElement(4));
            Assert.AreEqual<FieldZqElement>(Zq.GetElement(2).Negate(), Zq.GetElement(3));
            Assert.AreEqual<FieldZqElement>(Zq.GetElement(3).Negate(), Zq.GetElement(2));
            Assert.AreEqual<FieldZqElement>(Zq.GetElement(4).Negate(), Zq.GetElement(1));
        }
    }
}
