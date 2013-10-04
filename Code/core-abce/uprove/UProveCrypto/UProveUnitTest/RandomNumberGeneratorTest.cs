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
using UProveCrypto.Math;

namespace UProveUnitTest
{
    /// <summary>
    ///This is a test class for RandomNumberGeneratorTest and is intended
    ///to contain all RandomNumberGeneratorTest Unit Tests
    ///</summary>
    [TestClass()]
    public class RandomNumberGeneratorTest
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

        //private void CheckArray(BigInteger[] a, BigInteger expected)
        //{
        //    foreach (BigInteger i in a)
        //    {
        //        Assert.AreEqual<BigInteger>(i, expected);
        //    }
        //}
        
        /// <summary>
        ///A test for GetRandomValue
        ///</summary>
        [TestMethod()]
        public void GetRandomValueTest()
        {
            RandomElementTest(1, false, true); // cant force non-zero here b/c 0 is the only el
            RandomElementTest(2, true, true);
            RandomElementTest(2, false, true);
            RandomElementTest(5, true, true);
            RandomElementTest(5, false, true);
            RandomElementTest(631, true, true); 
            RandomElementTest(631, false, true);
        }

        private void RandomElementTest(int fieldSize, bool nonZero, bool checkDistribution)
        {
            byte[] modulusBytes = BitConverter.GetBytes(fieldSize);
            Array.Reverse(modulusBytes); // need big endian
            FieldZq field = FieldZq.CreateFieldZq(modulusBytes);

            Dictionary<FieldZqElement, int> counts = new Dictionary<FieldZqElement, int>();

            int rangeSize = (nonZero) ? fieldSize - 1 : fieldSize;
            int iters = (checkDistribution) ? 1000 * rangeSize : 5 * rangeSize;

            for (int i = 0; i < iters; i++)
            {
                FieldZqElement el = field.GetRandomElement(nonZero);

                if (counts.ContainsKey(el))
                {
                    int val = counts[el];
                    val++;
                    counts.Remove(el);
                    counts.Add(el, val);
                }
                else
                {
                    counts.Add(el, 1);
                }

                if (nonZero)
                {
                    Assert.AreNotEqual(el, field.Zero);
                }
            }

            double expectedHitRate = 1.0f / (double)rangeSize;
            double errorMargin = .3 * expectedHitRate;

            foreach (KeyValuePair<FieldZqElement, int> kvp in counts)
            {
                double hitRate = (double)kvp.Value / (double)iters;

                if (Math.Abs(hitRate - expectedHitRate) > errorMargin)
                {
                    Assert.Fail("Random number generator did not produce a good distribution");
                }
            }
        }
    }
}
