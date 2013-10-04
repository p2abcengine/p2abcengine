using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ABC4TrustSmartCard;
using UProveCrypto;

namespace abc4trust_uprove
{
  internal static class SecurityLevelUtils
  {

    internal static ParameterSet getRecommendedSet(int securityLevel)
    {
      string recommendedParameters = null;
      switch (securityLevel)
      {
        case 1024:
          recommendedParameters = StaticDefine.Hash_1_3_6_1_4_1_311_75_1_1_0;
          break;
        case 2048:
          recommendedParameters = StaticDefine.Hash_1_3_6_1_4_1_311_75_1_1_1;
          break;
        case 3072:
          recommendedParameters = StaticDefine.Hash_1_3_6_1_4_1_311_75_1_1_2;
          break;
        default:
          throw new ArgumentException("unsupported security level"); // TODOv2: we can actually ask the lib to generate params for this size
      }

      Log cOut = Logger.Instance.getLog(LoggerDefine.OUT_CONSOLE);
      cOut.write("Security level: " + recommendedParameters);
      ParameterSet parameterSet;
      ParameterSet.TryGetNamedParameterSet(recommendedParameters, out parameterSet);
      return parameterSet;
    }

    internal static ParameterSet getRecommendedSet(string recommendedParameters)
    {
      ParameterSet parameterSet;
      ParameterSet.TryGetNamedParameterSet(recommendedParameters, out parameterSet);
      return parameterSet;
    }

    // XXX if ever using ecc change this to include that.
    internal static string getHashfunction(ParameterSet pSet)
    {
      if (pSet.Name == "1.3.6.1.4.1.311.75.1.1.0")
      {
        return "1.3.14.3.2.26"; // sha-1
      }
      else
      {
        return "2.16.840.1.101.3.4.2.1"; //sha-256
      }
    }

  }
}
