using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.Text;
using UProveCrypto;

namespace UProveWCFServiceLib
{
  [ServiceBehavior(InstanceContextMode = InstanceContextMode.Single,
                   ConcurrencyMode = ConcurrencyMode.Multiple)]
  public class UProveRestServiceInfo : IUProveRestServiceInfo
  {
    private static ApiInfo _apiInfo = ApiInfo.Instance;
    private static volatile UProveRestServiceInfo _instance;
    private static object _syncRoot = new Object();

    #region instance
    
    public static UProveRestServiceInfo Instance
    {
      get
      {
        if (_instance == null)
        {
          lock (_syncRoot)
          {
            if (_instance == null)
              _instance = new UProveRestServiceInfo();
          }
        }
        return _instance;
      }
    }
    #endregion

    public ServerInfo ServerInfo()
    {
      return _apiInfo.GetApiInfo();
    }

    public Dictionary<string, GroupType> GroupTypeMap()
    {
      return _apiInfo.GetGroupTypeMap();
    }

    public List<ParameterSetInfo> ParameterSet()
    {
      return _apiInfo.GetSupportedParameterSets();
    }

    public Dictionary<string, ProtocolHelper.SupportedHashFunctions> SupportedShaFunctions()
    {
      return _apiInfo.GetSupportedShaFunctions();
    }

  }
}
