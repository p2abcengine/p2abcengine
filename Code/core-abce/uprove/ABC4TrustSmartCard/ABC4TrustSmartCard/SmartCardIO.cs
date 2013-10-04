using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using PCSC;
using PCSC.Iso7816;

namespace ABC4TrustSmartCard
{
  [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1063:ImplementIDisposableCorrectly")]
  public class SmartCardIO : ISmartCardIO, IDisposable
  {
    private SCardScope scope = SCardScope.System;
    private SCardContext ctx;
    private SCardReader reader;
    public SCardReader GetReader() { return reader; }

    [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1063:ImplementIDisposableCorrectly")]
    public void Dispose()
    {
      reader.Dispose();
      ctx.Dispose();
    }

    public SmartCardIO()
    {
      ctx = new SCardContext();
      ctx.Establish(this.scope);
      reader = new SCardReader(ctx);
    }

    public IsoReader TryConnect(String cardName)
    {
      try
      {
        IsoReader card = new IsoReader(this.reader);
        card.Connect(cardName, SCardShareMode.Shared, SCardProtocol.Any);
        return card;
      }
      catch (PCSCException)
      {
        return null;
      }
    }

    public List<String> GetConnected()
    {
      List<String> ret = new List<String>();
      List<String> readers = new List<string>(ctx.GetReaders());
      foreach (string s in readers)
      {
        IsoReader f = TryConnect(s);
        if (f == null)
        {
          continue;
        }
        ret.Add(s);
      }
      return ret;
    }

    // returns a new monitor each time.
    public SCardMonitor getMonitor()
    {
      SCardMonitor monitor = new SCardMonitor(ctx, scope);
      return monitor;
    }

    public String[] getReaders()
    {
      return ctx.GetReaders();
    }

  }
}
