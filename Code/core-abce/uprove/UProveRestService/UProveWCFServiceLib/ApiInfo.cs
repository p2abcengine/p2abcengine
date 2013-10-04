using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ABC4TrustSmartCard;
using UProveCrypto;

namespace UProveWCFServiceLib
{

  public static class EnumUtil
  {
    public static IEnumerable<T> GetValues<T>()
    {
      return Enum.GetValues(typeof(T)).Cast<T>();
    }
  }

  public class ApiInfo
  {
    private static volatile ApiInfo _instance;
    private static object _syncRoot = new Object();

    private ServerInfo _serverInfo;

    public static ApiInfo Instance
    {
      get
      {
        if (_instance == null)
        {
          lock (_syncRoot)
          {
            if (_instance == null)
              _instance = new ApiInfo();
          }
        }
        return _instance;
      }
    }

    private ApiInfo()
    {
      //this._address = address;
      _serverInfo = new ServerInfo();
      _serverInfo.address = new Uri(ParseConfigManager.GetAddress(), ParseConfigManager.GetApiPath());
      _serverInfo.serverUUID = System.Guid.NewGuid();
      _serverInfo.uProveVersion = new Version("1.1.2.24483");
      _serverInfo.UProveSupportedGroups = new List<GroupType>();
      _serverInfo.UProveSupportedGroups.Add(GroupType.ECC);
      _serverInfo.UProveSupportedGroups.Add(GroupType.Subgroup);
      _serverInfo.ProtocolVersion = new KeyValuePair<string, ProtocolVersion>(ProtocolVersion.V1_1.ToString(), ProtocolVersion.V1_1); ;
    }

    internal ServerInfo GetApiInfo()
    {
      return _serverInfo;
    }

    internal Dictionary<string, GroupType> GetGroupTypeMap()
    {
      Dictionary<string, GroupType> map = new Dictionary<string, GroupType>();
      var gtList = EnumUtil.GetValues<GroupType>();
      foreach (GroupType gt in gtList)
      {
        map.Add(gt.ToString(), gt);
      }

      return map;
    }



    internal List<ParameterSetInfo> GetSupportedParameterSets()
    {
      List<ParameterSetInfo> lst = new List<ParameterSetInfo>();

      ParameterSetInfo i1 = new ParameterSetInfo();
      i1.Name = SubgroupParameterSets.ParamSet_SG_1024160_V1Name;
      i1.Type = GroupType.Subgroup;
      i1.NumberOfGenerators = IssuerSetupParameters.RecommendedParametersMaxNumberOfAttributes;
      i1.HashFunction = ProtocolHelper.SupportedHashFunctions.SHA1;
      lst.Add(i1);

      ParameterSetInfo i2 = new ParameterSetInfo();
      i2.Name = SubgroupParameterSets.ParamSet_SG_2048256_V1Name;
      i2.Type = GroupType.Subgroup;
      i2.NumberOfGenerators = IssuerSetupParameters.RecommendedParametersMaxNumberOfAttributes;
      i2.HashFunction = ProtocolHelper.SupportedHashFunctions.SHA256;
      lst.Add(i2);

      ParameterSetInfo i3 = new ParameterSetInfo();
      i3.Name = SubgroupParameterSets.ParamSet_SG_3072256_V1Name;
      i3.Type = GroupType.Subgroup;
      i3.NumberOfGenerators = IssuerSetupParameters.RecommendedParametersMaxNumberOfAttributes;
      i3.HashFunction = ProtocolHelper.SupportedHashFunctions.SHA256;
      lst.Add(i3);

      ParameterSetInfo i4 = new ParameterSetInfo();
      i4.Name = ECParameterSets.ParamSet_EC_P256_V1Name;
      i4.Type = GroupType.ECC;
      i4.NumberOfGenerators = IssuerSetupParameters.RecommendedParametersMaxNumberOfAttributes;
      i4.HashFunction = ProtocolHelper.SupportedHashFunctions.SHA256;
      lst.Add(i4);

      ParameterSetInfo i5 = new ParameterSetInfo();
      i5.Name = ECParameterSets.ParamSet_EC_P384_V1Name;
      i5.Type = GroupType.ECC;
      i5.NumberOfGenerators = IssuerSetupParameters.RecommendedParametersMaxNumberOfAttributes;
      i5.HashFunction = ProtocolHelper.SupportedHashFunctions.SHA384;
      lst.Add(i5);

      ParameterSetInfo i6 = new ParameterSetInfo();
      i6.Name = ECParameterSets.ParamSet_EC_P521_V1Name;
      i6.Type = GroupType.ECC;
      i6.NumberOfGenerators = IssuerSetupParameters.RecommendedParametersMaxNumberOfAttributes;
      i6.HashFunction = ProtocolHelper.SupportedHashFunctions.SHA512;
      lst.Add(i6);

      return lst;
    }

    internal Dictionary<string, ProtocolHelper.SupportedHashFunctions> GetSupportedShaFunctions()
    {
      Dictionary<string, ProtocolHelper.SupportedHashFunctions> map = new Dictionary<string, ProtocolHelper.SupportedHashFunctions>();
      var gtList = EnumUtil.GetValues<ProtocolHelper.SupportedHashFunctions>();
      foreach (ProtocolHelper.SupportedHashFunctions gt in gtList)
      {
        map.Add(gt.ToString(), gt);
      }

      return map;
    }
  }
}
