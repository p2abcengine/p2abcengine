using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.Text;
using System.Threading;
using ABC4TrustSmartCard;
using SecureDataStore;
using UProveCrypto;

namespace UProveWCFServiceLib
{

  /**
   * For now this service will store state in memory and that is why we are running in Single InstanceContextMode
   * For the long haul it should shift to use a persistent data structure.
   */

  [ServiceBehavior(InstanceContextMode = InstanceContextMode.Single,
                   ConcurrencyMode = ConcurrencyMode.Multiple)]
  public class UProveRestServiceIssuer : IUProveRestServiceIssuer
  {
    private static volatile UProveRestServiceIssuer _instance;
    private static object _syncRoot = new Object();
    private static Guid _serverUUID = System.Guid.NewGuid();

    private static DiskPersistBasicStore<IssuerKeyAndParameters> issuerStore =
      DiskPersistBasicStoreManager<IssuerKeyAndParameters>.Instance.GetDiskPersistBasicStore(ParseConfigManager.GetStorePath());

    private static ConcurrentDictionary<Guid, IssuerInstanceData> issuerInstanceDB = new ConcurrentDictionary<Guid, IssuerInstanceData>();
    // run the clean up session thread every 5 min.
    private static Timer sessionCleaner = new Timer(CleanSession, null, TimeSpan.Zero, TimeSpan.FromMinutes(5));

    private static void CleanSession(Object obj)
    {
      //we automatic clean sessions that at least have not been accessed in 40 min.
      TimeSpan tSpan = TimeSpan.FromMinutes(40);
      foreach (KeyValuePair<Guid, IssuerInstanceData> kv in issuerInstanceDB)
      {
        IssuerInstanceData sData = kv.Value;
        if ((DateTime.Now - sData.LastAccessed) > tSpan)
        {
          issuerInstanceDB.TryRemove(kv.Key, out sData);
        }
      }
    }


    private UProveRestServiceIssuer()
    {
      // do nothing 
    }

    #region instance
    
    public static UProveRestServiceIssuer Instance
    {
      get
      {
        if (_instance == null)
        {
          lock (_syncRoot)
          {
            if (_instance == null)
              _instance = new UProveRestServiceIssuer();
          }
        }
        return _instance;
      }
    }
    #endregion


    public IssuerKeyAndParameters CreateIssuerSetupParameters(IssuerSetupParametersSpec spec)
    {
      IssuerSetupParameters isp = new IssuerSetupParameters();
      isp.GroupConstruction = spec.GroupConstruction ?? GroupType.Subgroup;
      isp.UidP = ExtensionMethods.ToByteArray(spec.IssuerID);
      isp.E = spec.AttributeEncoding != null ? spec.AttributeEncoding : IssuerSetupParameters.GetDefaultEValues(spec.NumberOfAttributes);
      isp.UseRecommendedParameterSet = spec.UseRecommendedParameterSet ?? true;

      if (issuerStore.HasValue(spec.IssuerID) && spec.StoreOnServer)
      {
        ApiArgumentFault fault = new ApiArgumentFault();
        fault.Details = "Issuer with unique ID was found";
        fault.Argument = "IssuerSetupParametersSpec.ID";
        fault.ArgumentValue = spec.ParameterSetName;
        throw new FaultException<ApiArgumentFault>(fault);
      }

      // look up ParameterSet.
      if (isp.UseRecommendedParameterSet)
      {
        isp.ParameterSet = IssuerSetupParameters.GetDefaultParameterSet(isp.GroupConstruction);
        // XXX add a check here to see if the name of the default parameterset is that same as
        // specified in spec.ParameterSetName and that match with the sha method specified.
      }
      else
      {
        ParameterSet pSet;
        if (ParameterSet.TryGetNamedParameterSet(spec.ParameterSetName, out pSet))
        {
          isp.ParameterSet = pSet;
        }
        else
        {
          ApiArgumentFault fault = new ApiArgumentFault();
          fault.Details = "Member value vas not found";
          fault.Argument = "IssuerSetupParametersSpec.ParameterSetName";
          fault.ArgumentValue = spec.ParameterSetName;
          throw new FaultException<ApiArgumentFault>(fault);
        }
      }

      // specification field unused in ABC4Trust
      isp.S = null;

      IssuerKeyAndParameters issuerKeyParam = isp.Generate(true);
      if (spec.StoreOnServer) { 
        issuerStore.AddValue(spec.IssuerID, issuerKeyParam);
      }
      return issuerKeyParam;
    }

    public List<IssuerParameterInfo> ListIssuerSetupParameters()
    {
      List<IssuerParameterInfo> infoLst = new List<IssuerParameterInfo>();

      ICollection<string> keys = issuerStore.GetAllKeys();
      foreach (string i in keys)
      {
        IssuerParameterInfo infoItem = new IssuerParameterInfo();
        infoItem.ParameterSetName = i;
        infoItem.IssuerParameterSet = issuerStore.GetValue(i).IssuerParameters;
        infoLst.Add(infoItem);
      }
      return infoLst;
    }

    public IssuerParameterInfo GetIssuerSetupParameter(string id)
    {
      IssuerParameterInfo infoItem = new IssuerParameterInfo();

      if (!issuerStore.HasValue(id))
      {
        ApiArgumentFault fault = new ApiArgumentFault();
        fault.Details = "Issuer with unique ID was found";
        fault.Argument = "ID";
        fault.ArgumentValue = id;
        throw new FaultException<ApiArgumentFault>(fault);
      }

      IssuerKeyAndParameters issuerParam = issuerStore.GetValue(id);
      infoItem.ParameterSetName = id;
      infoItem.IssuerParameterSet = issuerParam.IssuerParameters;

      return infoItem;
    }

    public void DeleteIssuerSetupParameter(string id)
    {
      if (!issuerStore.HasValue(id))
      {
        ApiArgumentFault fault = new ApiArgumentFault();
        fault.Details = "Issuer with unique ID was found";
        fault.Argument = "ID";
        fault.ArgumentValue = id;
        throw new FaultException<ApiArgumentFault>(fault);
      }
      issuerStore.RemoveValue(id);
    }


    public FirstIssuanceMessageInfo FirstMessage(FirstIssuanceMessageSpec spec)
    {
      if (!issuerStore.HasValue(spec.IssuerID))
      {
        ApiArgumentFault fault = new ApiArgumentFault();
        fault.Details = "Issuer with unique ID was found";
        fault.Argument = "FirstIssuanceMessageSpec.IssuerID";
        fault.ArgumentValue = spec.IssuerID;
        throw new FaultException<ApiArgumentFault>(fault);
      }
      IssuerKeyAndParameters ikp = issuerStore.GetValue(spec.IssuerID);

      IssuerProtocolParameters ipp = new IssuerProtocolParameters(ikp);
      ipp.NumberOfTokens = spec.NumberOfTokens;
      ipp.Attributes = ConvertToByteArray(spec.Attributes);
      if (!String.IsNullOrWhiteSpace(spec.TokenInfomation))
      {
        ipp.TokenInformation = ExtensionMethods.ToByteArray(spec.TokenInfomation);
      }
      
      if (!String.IsNullOrWhiteSpace(spec.Gamma))
      {

        ipp.Gamma = ExtensionMethods.ToGroupElement(spec.Gamma, ikp.IssuerParameters);
      }

      if (!String.IsNullOrWhiteSpace(spec.DevicePublicKey))
      {
        ipp.DevicePublicKey = ExtensionMethods.ToGroupElement(spec.DevicePublicKey, ikp.IssuerParameters);
      }

      ipp.Validate();
      Issuer issuer = ipp.CreateIssuer();
      FirstIssuanceMessage firstMessage = issuer.GenerateFirstMessage();
      FirstIssuanceMessageInfo ret = new FirstIssuanceMessageInfo();
      ret.FirstMessage = firstMessage;
      Guid issuerInstance = Guid.NewGuid();
      ret.IssuerInstanceID = issuerInstance;
      ret.FirstMessageState = issuer.ExportPostFirstMessageState();
      issuerInstanceDB.TryAdd(issuerInstance, new IssuerInstanceData(issuer));
      return ret;
    }


    private ThirdIssuanceMessageInfo HandleThirdMessageInfo(Issuer issuer, SecondIssuanceMessage secondMessage)
    {
      ThirdIssuanceMessageInfo thirdMessageInfo = new ThirdIssuanceMessageInfo();
      thirdMessageInfo.ThirdMessage = issuer.GenerateThirdMessage(secondMessage);
      return thirdMessageInfo;

    }

    public ThirdIssuanceMessageInfo ThirdMessage(ThirdIssuanceMessageSpec spec)
    {
      // first we will try to lookup the cache value of the issuer object.
      Issuer issuer = null;
      if (spec.IssuerInstanceID != null)
      {
        IssuerInstanceData issuerInstance;
        bool ok = issuerInstanceDB.TryGetValue(spec.IssuerInstanceID, out issuerInstance);
        if (ok)
        {
          issuer = issuerInstance.Issuer;
        }
        else
        {
          //XXX add log about Issuer not found in cache.
        }
      }

      if (issuer != null)
      {
        return HandleThirdMessageInfo(issuer, spec.SecondMessage);
      }

      if (spec.FistMessageState != null && spec.IssuerKeyAndParameter != null)
      {
        issuer = new Issuer(spec.IssuerKeyAndParameter, spec.FistMessageState);
        issuerInstanceDB.TryAdd(spec.IssuerInstanceID, new IssuerInstanceData(issuer));
        return HandleThirdMessageInfo(issuer, spec.SecondMessage);

      }

      ApiArgumentFault fault = new ApiArgumentFault();
      fault.Details = "Issuer with unique ID was found or FirstMessageState and IssuerKeyAndParameter not provided.";
      fault.Argument = "ThirdIssuanceMessageSpec.IssuerInstanceID/ThirdIssuanceMessageSpec.FirstMessageState/ThirdIssuanceMessageSpec.IssuerKeyAndParameter";
      fault.ArgumentValue = spec.IssuerInstanceID.ToString();
      throw new FaultException<ApiArgumentFault>(fault);

    }




    private byte[][] ConvertToByteArray(List<string> list)
    {
      byte[][] ret = new byte[list.Count][];
      for (int i = 0; i != list.Count; ++i)
      {
        ret[i] = ExtensionMethods.ToByteArray(list[i]);
      }
      return ret;
    }


  }
}
