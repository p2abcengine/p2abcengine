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

      byte[] deviceCommitment = this.smartCard.GetDeviceCommitment();
      Assert.AreEqual<byte>(deviceCommitment[deviceCommitment.Length - 1], 0x00, "The BigInteger must be positive");

      this.smartCard.EndCommitment();
    }

    [TestMethod]
    public void TestGetScopeExclusiveCommitment()
    {
      CardMode cardMode = this.smartCard.GetCardMode();
      Assert.AreEqual<CardMode>(CardMode.WORKING, cardMode, "Not in working mode.");

      this.smartCard.BeginCommitment(1);

      byte[] scopeExlusive = this.smartCard.GetScopeExclusiveCommitment(Utils.GetBytes("MyVeryNiceScopeThatJustKeepGettingBetterAndBetter"));
      Assert.AreEqual<byte>(scopeExlusive[scopeExlusive.Length - 1], 0x00, "The BigInteger must be positive");
      
      this.smartCard.EndCommitment();
    }

    [TestMethod]
    public void TestGetScopeExlusivePseudonym()
    {
      CardMode cardMode = this.smartCard.GetCardMode();
      Assert.AreEqual<CardMode>(CardMode.WORKING, cardMode, "Not in working mode.");

      this.smartCard.BeginCommitment(1);

      byte[] scopeExlusiveP = this.smartCard.GetScopeExclusivePseudonym(Utils.GetBytes("MyVeryNiceScopeThatJustKeepGettingBetterAndBetter"));
      Assert.AreEqual<byte>(scopeExlusiveP[scopeExlusiveP.Length - 1], 0x00, "The BigInteger must be positive");

      Console.Out.WriteLine(scopeExlusiveP);
      this.smartCard.EndCommitment();
    }

    [TestMethod]
    public void TestAllInSameCommitment()
    {
      CardMode cardMode = this.smartCard.GetCardMode();
      Assert.AreEqual<CardMode>(CardMode.WORKING, cardMode, "Not in working mode.");

      this.smartCard.BeginCommitment(1);

      byte[] deviceCommitment = this.smartCard.GetDeviceCommitment();
      Assert.AreEqual<byte>(deviceCommitment[deviceCommitment.Length - 1], 0x00, "The BigInteger must be positive");

      byte[] scopeExlusive = this.smartCard.GetScopeExclusiveCommitment(Utils.GetBytes("MyVeryNiceScopeThatJustKeepGettingBetterAndBetter"));
      Assert.AreEqual<byte>(scopeExlusive[scopeExlusive.Length - 1], 0x00, "The BigInteger must be positive");

      byte[] scopeExlusiveP = this.smartCard.GetScopeExclusivePseudonym(Utils.GetBytes("MyVeryNiceScopeThatJustKeepGettingBetterAndBetter"));
      Assert.AreEqual<byte>(scopeExlusiveP[scopeExlusiveP.Length - 1], 0x00, "The BigInteger must be positive");

      this.smartCard.EndCommitment();

    }




  }
}
