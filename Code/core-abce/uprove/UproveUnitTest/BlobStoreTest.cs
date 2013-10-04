using System;
using System.Collections.Generic;
using System.Numerics;
using System.Text;
using ABC4TrustSmartCard;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace UProve_ABC4Trust_unitTest
{
  [TestClass]
  public class BlobStoreTest
  {
    private SmartCard smartCard;
    private static String pString = "98123248929977781234033599438430872512413464343146397351387389379354368678144441573871246352773205104826862682926853362525445766087875638152522866999171082979077110521492402490396873693935392516032428981301612806156847276776118279635414146466050412914757988580508268698346492883186329475125347579894931495911";
    private static String qString = "153675233447601346431048868855343067422415662106390990153196543740996536617679216226026465835429401901713210990800796200382364371445046479284855834231353845317728582606162246819027954438959188793527020710865829941771890543954962333617286117444708446256701507879424546361300317931079057381850093323907216844089";
    private static String pin = "1234";
    private BigInteger p;
    private BigInteger q;
    private byte[] bPin = Encoding.ASCII.GetBytes(pin);


    public BlobStoreTest()
    {
      LoggerUtils.setupLoggers();
      List<CardInfo> lst = SmartCardUtils.GetReaderNames();
      String readerName = lst[0].ReaderName;
      smartCard = new SmartCard(readerName, pin);
      CardMode mode = this.smartCard.GetCardMode();
      if (mode != CardMode.ROOT)
      {
        this.smartCard.SetCardInRootMode();
      }
      BigInteger.TryParse(pString, out p);
      BigInteger.TryParse(qString, out q);
      KeyPair pq = new KeyPair(p, q);
      String puk = this.smartCard.InitDevice(pq, pin);
    }

    private void storeBlob(byte[] data, byte[] uri)
    {
      ErrorCode err = smartCard.device.PutData(data);
      if (!err.IsOK)
      {
        throw err;
      }

      err = smartCard.device.StoreBlob(bPin, uri);
      if (!err.IsOK)
      {
        throw err;
      }
    }

    private byte[] getBLob(byte[] uri)
    {
      byte[] outBLob;
      ErrorCode err = smartCard.device.ReadBlob(bPin, uri, out outBLob);
      if (!err.IsOK)
      {
        throw err;
      }
      return outBLob;
    }

    private void cleanUpBlob(byte[] uri, bool nothrow=false)
    {
      ErrorCode err = smartCard.device.RemoveBlob(bPin, uri);
      if (!err.IsOK && !nothrow)
      {
        throw err;
      }
    }


    [TestMethod]
    public void StoreInBlobStoreOneSmallStore()
    {
      byte[] data = Utils.GetBytes("foobar");
      byte[] uri = Utils.GetBytes("myUri");

      storeBlob(data, uri);
      byte[] outBlob = getBLob(uri);
      cleanUpBlob(uri);

      CollectionAssert.AreEqual(outBlob, data);
    }

    [TestMethod]
    public void StoreInBlobStoreOneLargeStore()
    {
      // 255 bytes.
      byte[] dataRaw = Utils.GetBytes("879342439843894389438934893489893489348934893489349884389348934893489349834893489348934893498348934893498348839438493984879234987342983489348980989088080994389348934898943893489438943894389893489348943984389348934983489349834989834983489438943893498437432879342987342879342798342897342987342897234987342879");
      byte[] data = new byte[512];
      Array.Copy(dataRaw, data, 512);
      byte[] uri = Utils.GetBytes("myUri");

      storeBlob(data, uri);
      byte[] outBlob = getBLob(uri);
      cleanUpBlob(uri);

      CollectionAssert.AreEqual(outBlob, data);
    }

    [TestMethod]
    public void StoreInBlobStoreManyLargeStores()
    {
      byte[] dataRaw = Utils.GetBytes("879342879234987342983489348980989088080994389348934898943893489438943894389893489348943984389348934983489349834989834983489438943893498437432879342987342879342798342897342987342897234987342879");
      byte[] data = new byte[255];
      Array.Copy(dataRaw, data, 255);

      List<byte[]> uris = new List<byte[]>();
      
      for (int i = 0; i != 5; ++i)
      {
        byte[] d = Utils.GetBytes("myfunuri" + i.ToString());
        uris.Add(d);
        cleanUpBlob(d, true);
      }

      foreach (byte[] d in uris)
      {
        storeBlob(data, d);
      }

      foreach (byte[] d in uris)
      {
        byte[] outBlob = getBLob(d);
        cleanUpBlob(d);
        // same data out as in.
        CollectionAssert.AreEqual(outBlob, data);
      }
    }

    [TestMethod]
    public void StoreInBlobStoreManyLargeStoresListBlobs()
    {
      byte[] data = Utils.GetBytes("foobar");
      List<byte[]> uris = new List<byte[]>();

      for (int i = 0; i != 5; ++i)
      {
        byte[] d = Utils.GetBytes("myfunuri" + i.ToString());
        uris.Add(d);
        cleanUpBlob(d, true);
      }



    }



  } // end class
} // end namespace

