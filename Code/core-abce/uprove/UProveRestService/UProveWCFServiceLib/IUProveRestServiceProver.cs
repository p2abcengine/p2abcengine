using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Text;
using UProveCrypto;

namespace UProveWCFServiceLib
{
  [ServiceContract(SessionMode = SessionMode.Allowed)]
  public interface IUProveRestServiceProver
  {
    [OperationContract]
    [WebInvoke(Method = "POST",
               RequestFormat = WebMessageFormat.Json,
               ResponseFormat = WebMessageFormat.Json,
               UriTemplate = "SecondMessage",
               BodyStyle = WebMessageBodyStyle.Bare)]
    [FaultContract(typeof(RuntimeFault))]
    [Description("Create second uprove message for creating a new token.")]
    SecondIssuanceMessageInfo SecondMessage(SecondIssuanceMessageSpec spec);

    [OperationContract]
    [WebInvoke(Method = "POST",
               RequestFormat = WebMessageFormat.Json,
               ResponseFormat = WebMessageFormat.Json,
               UriTemplate = "Token",
               BodyStyle = WebMessageBodyStyle.Bare)]
    [FaultContract(typeof(RuntimeFault))]
    [Description("Generate tokens as the last step in the issuer protocol.")]
    GenerateTokensInfo GenerateTokens(GenerateTokensSpec spec);
  }

  internal class ProverInstanceData : InstanceData
  {
    private Prover _prover;

    internal ProverInstanceData(Prover issuer)
    {
      this.LastAccessed = DateTime.Now;
      _prover = issuer;
    }

    public Prover Prover
    {
      set
      {
        _prover = value;
        this.LastAccessed = DateTime.Now;
      }
      get
      {
        this.LastAccessed = DateTime.Now;
        return _prover;
      }
    }
  }
}
