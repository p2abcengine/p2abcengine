using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.Text;
using System.Threading;
using UProveCrypto;

namespace UProveWCFServiceLib
{
  [ServiceBehavior(InstanceContextMode = InstanceContextMode.Single,
                   ConcurrencyMode = ConcurrencyMode.Multiple)]
  public class UProveRestServiceProver : IUProveRestServiceProver
  {
    private static volatile UProveRestServiceProver _instance;
    private static object _syncRoot = new Object();

    private static ConcurrentDictionary<Guid, ProverInstanceData> proverInstanceDB = new ConcurrentDictionary<Guid, ProverInstanceData>();
    // run the clean up session thread every 5 min.
    private static Timer sessionCleaner = new Timer(CleanSession, null, TimeSpan.Zero, TimeSpan.FromMinutes(5));

    private static void CleanSession(Object obj)
    {
      //we automatic clean sessions that at least have not been accessed in 40 min.
      TimeSpan tSpan = TimeSpan.FromMinutes(40);
      foreach (KeyValuePair<Guid, ProverInstanceData> kv in proverInstanceDB)
      {
        ProverInstanceData sData = kv.Value;
        if ((DateTime.Now - sData.LastAccessed) > tSpan)
        {
          proverInstanceDB.TryRemove(kv.Key, out sData);
        }
      }
    }

    private UProveRestServiceProver()
    {
      // do nothing 
    }

    #region instance

    public static UProveRestServiceProver Instance
    {
      get
      {
        if (_instance == null)
        {
          lock (_syncRoot)
          {
            if (_instance == null)
              _instance = new UProveRestServiceProver();
          }
        }
        return _instance;
      }
    }
    #endregion

    public SecondIssuanceMessageInfo SecondMessage(SecondIssuanceMessageSpec spec)
    {
      if (spec.IssuerParameter == null)
      {
        ApiArgumentFault fault = new ApiArgumentFault();
        fault.Details = "Issuer with unique ID was found";
        fault.Argument = "IssuerSetupParametersSpec.ID";
        fault.ArgumentValue = spec.IssuerParameter.Serialize();
        throw new FaultException<ApiArgumentFault>(fault);
      }
      ProverProtocolParameters pProtoParam = new ProverProtocolParameters(spec.IssuerParameter);
      pProtoParam.NumberOfTokens = spec.NumberOfTokens;
      if (!String.IsNullOrWhiteSpace(spec.TokenInfomation))
      {
        pProtoParam.TokenInformation = ExtensionMethods.ToByteArray(spec.TokenInfomation);
      }

      if (!String.IsNullOrWhiteSpace(spec.Gamma))
      {

        pProtoParam.Gamma = ExtensionMethods.ToGroupElement(spec.Gamma, spec.IssuerParameter);
      }

      if (!String.IsNullOrWhiteSpace(spec.DevicePublicKey))
      {
        pProtoParam.DevicePublicKey = ExtensionMethods.ToGroupElement(spec.DevicePublicKey, spec.IssuerParameter);
      }

      if (!String.IsNullOrWhiteSpace(spec.ProverInfomation))
      {
        pProtoParam.ProverInformation = ExtensionMethods.ToByteArray(spec.ProverInfomation);
      }

      if (spec.RandomData != null)
      {
        pProtoParam.ProverRandomData = spec.RandomData;
      }
      if (spec.BatchSecurityLevel > 0)
      {
        pProtoParam.BatchValidationSecurityLevel = spec.BatchSecurityLevel;
      }
      pProtoParam.Validate();
      Prover prover = pProtoParam.CreateProver();

      SecondIssuanceMessage secondMessage = prover.GenerateSecondMessage(spec.FirstMessage);
      PostSecondMessageState postSecondMessageState = prover.ExportPostSecondMessageState();

      SecondIssuanceMessageInfo simInfo = new SecondIssuanceMessageInfo();
      simInfo.PostSecondMessageState = postSecondMessageState;
      simInfo.SecondMessage = secondMessage;

      Guid issuerInstance = Guid.NewGuid();
      simInfo.ProverInstanceID = issuerInstance;
      proverInstanceDB.TryAdd(issuerInstance, new ProverInstanceData(prover));

      return simInfo;
    }

    public GenerateTokensInfo GenerateTokens(GenerateTokensSpec spec)
    {
      // first we will try to lookup the cache value of the prover object.
      Prover prover = null;
      if (spec.ProverInstanceID != null)
      {
        ProverInstanceData proverInstance;
        bool ok = proverInstanceDB.TryGetValue(spec.ProverInstanceID, out proverInstance);
        if (ok)
        {
          prover = proverInstance.Prover;
        }
        else
        {
          //XXX add log about Prover not found in cache.
        }
      }

      if (prover != null)
      {
        return HandleGenerateTokens(prover, spec.ThirdMessage);
      }

      if (spec.SecondMessageState != null && spec.IssuerParameter != null)
      {
        prover = new Prover(spec.IssuerParameter, spec.SecondMessageState);
        proverInstanceDB.TryAdd(spec.ProverInstanceID, new ProverInstanceData(prover));
        return HandleGenerateTokens(prover, spec.ThirdMessage);

      }

      ApiArgumentFault fault = new ApiArgumentFault();
      fault.Details = "Prover with unique ID was found or SecondMessageState and IssuerParameter not provided.";
      fault.Argument = "GenerateTokensSpec.ProverInstanceID/GenerateTokensSpec.SecondMessageState/GenerateTokensSpec.IssuerParameter";
      fault.ArgumentValue = spec.ProverInstanceID.ToString();
      throw new FaultException<ApiArgumentFault>(fault);

    }

    private GenerateTokensInfo HandleGenerateTokens(Prover prover, ThirdIssuanceMessage thirdIssuanceMessage)
    {
      GenerateTokensInfo gInfo = new GenerateTokensInfo();
      gInfo.Tokens = prover.GenerateTokens(thirdIssuanceMessage, true);
      return gInfo;
    }
    
  }
}
