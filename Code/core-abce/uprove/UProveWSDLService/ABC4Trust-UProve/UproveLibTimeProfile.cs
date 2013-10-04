using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ABC4TrustSmartCard;

namespace abc4trust_uprove
{
  public class UproveLibTimeProfile : IDisposable
  {

    private Log logger;
    private string methodName;
    private AbcTimer timer;
    private bool doProfile;
    public UproveLibTimeProfile(string methodName, bool doProfile, Log logger)
    {
      this.doProfile = doProfile;
      if (!this.doProfile)
      {
        return;
      }
      this.methodName = methodName;
      this.logger = logger;
      this.timer = new AbcTimer();
      this.timer.Start();
    }


    #region IDisposable Members

    public void Dispose()
    {
      if (!this.doProfile)
      {
        return;
      }
      this.timer.Stop();
      double t = this.timer.getElapsed().TotalMilliseconds;

      logger.write("--> Uprove lib method '{0}' was running for '{1}' {2}", methodName, Math.Round(t), "miliseconds");
    }

    #endregion
  }
}
