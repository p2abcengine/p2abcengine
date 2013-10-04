using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace UProveWCFServiceLib
{
  public abstract class InstanceData
  {
    private DateTime _lastAccessed;

    public DateTime LastAccessed
    {
      set { _lastAccessed = value; }
      get { return _lastAccessed; }
    }

  }
}
