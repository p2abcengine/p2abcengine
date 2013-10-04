using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Numerics;
using ABC4TrustSmartCard;

namespace UProve_ABC4Trust_unitTest
{
  /// <summary>
  /// Summary description for HardwareSmartCardTest
  /// </summary>
  [TestClass]
  public class HardwareSmartCardTest
  {
    private TestContext testContextInstance;
    private ABC4TrustSmartCard.SmartCard smartCard;
    String pString = "98123248929977781234033599438430872512413464343146397351387389379354368678144441573871246352773205104826862682926853362525445766087875638152522866999171082979077110521492402490396873693935392516032428981301612806156847276776118279635414146466050412914757988580508268698346492883186329475125347579894931495911";
    String qString = "153675233447601346431048868855343067422415662106390990153196543740996536617679216226026465835429401901713210990800796200382364371445046479284855834231353845317728582606162246819027954438959188793527020710865829941771890543954962333617286117444708446256701507879424546361300317931079057381850093323907216844089";
    private BigInteger p;
    private BigInteger q;
    private string pin = "1234";


    public HardwareSmartCardTest()
    {
      List<CardInfo> lst = SmartCardUtils.GetReaderNames();
      Assert.IsNotNull(lst);
      Assert.IsTrue(lst.Count > 0);
      String readerName = lst[0].ReaderName;
      BigInteger.TryParse(this.pString, out p);
      BigInteger.TryParse(this.qString, out q);

      smartCard = new SmartCard(readerName, "1234");
      try
      {
        this.smartCard.SetVirginMode();
      }
      catch (Exception)
      {
        Assert.Fail("Set the card into virgin mode failed");
      }
    }
       

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
    // You can use the following additional attributes as you write your tests:
    //
    // Use ClassInitialize to run code before running the first test in the class
    // [ClassInitialize()]
    // public static void MyClassInitialize(TestContext testContext) { }
    //
    // Use ClassCleanup to run code after all tests in a class have run
    // [ClassCleanup()]
    // public static void MyClassCleanup() { }
    //
    // Use TestInitialize to run code before running each test 
    // [TestInitialize()]
    // public void MyTestInitialize() { }
    //
    // Use TestCleanup to run code after each test has run
    // [TestCleanup()]
    // public void MyTestCleanup() { }
    //
    #endregion

    [TestMethod]
    public void SetModeTest()
    {
      try
      {
        // 1. set in virgin mode
        this.smartCard.SetVirginMode();
        CardMode mode = this.smartCard.GetCardMode();
        Assert.IsTrue(mode == CardMode.VIRGIN);
        
        // 2. set in root mode
        this.smartCard.SetCardInRootMode();
        mode = this.smartCard.GetCardMode();
        Assert.IsTrue(mode == CardMode.ROOT);

        // 3. set in working mode.
        this.smartCard.SetCardInWorkingMode();
        mode = this.smartCard.GetCardMode();
        Assert.IsTrue(mode == CardMode.WORKING);

        //reset the card again
        this.smartCard.SetVirginMode();

      }
      catch (Exception ex)
      {
        Assert.Fail( String.Format("Reset the device failed: {0}", ex.Message));
      }
    }

    [TestMethod]
    public void InitDevice()
    {
      try
      {
        CardMode mode = this.smartCard.GetCardMode();
        if (mode != CardMode.ROOT)
        {
          this.smartCard.SetCardInRootMode();
        }
        KeyPair pq = new KeyPair(p, q);
        String puk = this.smartCard.InitDevice(pq, pin);
      }
      catch (ErrorCode ex)
      {
        Assert.Fail(String.Format("Reset the device failed: {0}:{1} with command {2}", ex.SW1, ex.SW2, ex.Command));
      }
      catch (Exception ex)
      {
        Assert.Fail(String.Format("Reset the device failed: {0}", ex.Message));
      }
    }
  }
}
