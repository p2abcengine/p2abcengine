using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;

namespace abc4trust_uprove
{
  public class AbcTimer : IDisposable
  {

    private Stopwatch _stopWatch;

    public AbcTimer()
    {
      _stopWatch = new Stopwatch();
    }

    public void Start()
    {
      _stopWatch.Start();
    }

    public void Stop()
    {
      _stopWatch.Stop();
    }

    public TimeSpan getElapsed()
    {
      return _stopWatch.Elapsed;
    }

    /*
    public string getElapsed()
    {
      return _stopWatch.Elapsed.ToString();
    }*/

    public void Dispose()
    {
      if (_stopWatch.IsRunning)
      {
        _stopWatch.Stop();
      }
    }
  }
}
