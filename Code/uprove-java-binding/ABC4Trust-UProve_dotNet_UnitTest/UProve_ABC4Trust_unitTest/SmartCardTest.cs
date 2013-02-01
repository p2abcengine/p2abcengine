using System;
using System.Collections.Generic;
using System.Numerics;
using abc4trust_uprove;
using ABC4TrustSmartCard;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace UProve_ABC4Trust_unitTest
{

  

  [TestClass]
  public class SmartCardTest
  {

    private SmartCard smartCard;
    //public static DeviceManager deviceManager = new DeviceManager(false);

    public SmartCardTest()
    {
      List<CardInfo> lst = SmartCardUtils.GetReaderNames();
      String readerName = lst[0].ReaderName;
      smartCard = new SmartCard(readerName, "5304");
    }

    // The card must have been setup by the java part.
    [TestMethod]
    public void TestGetDeviceCommitment()
    {
      CardMode cardMode = this.smartCard.GetCardMode();
      Assert.AreEqual<CardMode>(CardMode.WORKING, cardMode, "Not in working mode.");

      this.smartCard.BeginCommitment(1);

      BigInteger deviceCommitment = this.smartCard.GetDeviceCommitment();
      Assert.AreEqual<BigInteger>(1, deviceCommitment.Sign, "The BigInteger must be positive");

      this.smartCard.EndCommitment();
    }

    [TestMethod]
    public void TestGetScopeExclusiveCommitment()
    {
      CardMode cardMode = this.smartCard.GetCardMode();
      Assert.AreEqual<CardMode>(CardMode.WORKING, cardMode, "Not in working mode.");

      this.smartCard.BeginCommitment(1);

      BigInteger scopeExlusive = this.smartCard.GetScopeExclusiveCommitment("MyVeryNiceScopeThatJustKeepGettingBetterAndBetter");
      Assert.AreEqual<BigInteger>(1, scopeExlusive.Sign, "The BigInteger must be positive");
      
      this.smartCard.EndCommitment();
    }

    [TestMethod]
    public void TestGetScopeExlusivePseudonym()
    {
      CardMode cardMode = this.smartCard.GetCardMode();
      Assert.AreEqual<CardMode>(CardMode.WORKING, cardMode, "Not in working mode.");

      this.smartCard.BeginCommitment(1);

      BigInteger scopeExlusiveP = this.smartCard.GetScopeExclusivePseudonym("MyVeryNiceScopeThatJustKeepGettingBetterAndBetter");
      Assert.AreEqual<BigInteger>(1, scopeExlusiveP.Sign, "The BigInteger must be positive");

      Console.Out.WriteLine(scopeExlusiveP);
      this.smartCard.EndCommitment();
    }

    [TestMethod]
    public void TestAllInSameCommitment()
    {
      CardMode cardMode = this.smartCard.GetCardMode();
      Assert.AreEqual<CardMode>(CardMode.WORKING, cardMode, "Not in working mode.");

      this.smartCard.BeginCommitment(1);

      BigInteger deviceCommitment = this.smartCard.GetDeviceCommitment();
      Assert.AreEqual<BigInteger>(1, deviceCommitment.Sign, "The BigInteger must be positive");

      BigInteger scopeExlusive = this.smartCard.GetScopeExclusiveCommitment("MyVeryNiceScopeThatJustKeepGettingBetterAndBetter");
      Assert.AreEqual<BigInteger>(1, scopeExlusive.Sign, "The BigInteger must be positive");

      BigInteger scopeExlusiveP = this.smartCard.GetScopeExclusivePseudonym("MyVeryNiceScopeThatJustKeepGettingBetterAndBetter");
      Assert.AreEqual<BigInteger>(1, scopeExlusiveP.Sign, "The BigInteger must be positive");

      this.smartCard.EndCommitment();

    }




  }
}
