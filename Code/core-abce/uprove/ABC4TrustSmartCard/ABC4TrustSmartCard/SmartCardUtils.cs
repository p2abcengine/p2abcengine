using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ABC4TrustSmartCard
{
  public static class SmartCardUtils
  {
    public static List<CardInfo> GetReaderNames()
    {
      List<CardInfo> lst = new List<CardInfo>();
      SmartCardIO cardIO = new SmartCardIO();
      List<String> connectedCards = cardIO.GetConnected();
    
      foreach (String s in connectedCards)
      {
        CardInfo info = new CardInfo();
        ABC4TrustSmartCard smartCard = new ABC4TrustSmartCard(s);
        info.ReaderName = s;
        string version;
        ErrorCode err = smartCard.GetVersion(out version);
        if (err.IsOK)
        {
          info.CardVersion = version;
        }
        CardMode cardMode;
        err = smartCard.GetMode(out cardMode);
        if (err.IsOK)
        {
          info.CardMode = (int)cardMode;
        }
        lst.Add(info);
      }
      return lst;
    }

    public static System.Numerics.BigInteger GetBigInteger(byte[] p)
    {
      return new System.Numerics.BigInteger(p);
    }
  }
}
