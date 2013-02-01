using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using PCSC.Iso7816;

namespace ABC4TrustSmartCard
{
  interface ISmartCardIO
  {
    List<String> GetConnected();
    IsoCard TryConnect(String cardName);
  }
}
