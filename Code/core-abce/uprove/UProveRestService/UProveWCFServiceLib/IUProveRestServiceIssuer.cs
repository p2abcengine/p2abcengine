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
  public interface IUProveRestServiceIssuer 
  {
    [OperationContract]
    [WebInvoke(Method = "POST",
               RequestFormat = WebMessageFormat.Json,
               ResponseFormat = WebMessageFormat.Json,
               UriTemplate = "IssuerSetupParameters",
               BodyStyle = WebMessageBodyStyle.Bare)]
    [FaultContract(typeof(RuntimeFault))]
    [Description("Create a new Issuer of uprove tokens.")]
    IssuerKeyAndParameters CreateIssuerSetupParameters(IssuerSetupParametersSpec spec);

    [OperationContract]
    [WebInvoke(Method = "GET",
               RequestFormat = WebMessageFormat.Json,
               ResponseFormat = WebMessageFormat.Json,
               UriTemplate = "IssuerSetupParameters",
               BodyStyle = WebMessageBodyStyle.Bare)]
    [Description("Return a list of all known Issuers")]
    List<IssuerParameterInfo> ListIssuerSetupParameters();

    [OperationContract]
    [WebInvoke(Method = "GET",
               RequestFormat = WebMessageFormat.Json,
               ResponseFormat = WebMessageFormat.Json,
               UriTemplate = "IssuerSetupParameters/{id}",
               BodyStyle = WebMessageBodyStyle.Bare)]
    [FaultContract(typeof(RuntimeFault))]
    [Description("Return Issuer parameter for an Issuer")]
    IssuerParameterInfo GetIssuerSetupParameter(string id);

    [OperationContract]
    [WebInvoke(Method = "DELETE",
               RequestFormat = WebMessageFormat.Json,
               ResponseFormat = WebMessageFormat.Json,
               UriTemplate = "IssuerSetupParameters/{id}",
               BodyStyle = WebMessageBodyStyle.Bare)]
    [FaultContract(typeof(RuntimeFault))]
    [Description("Delete an Issuer")]
    void DeleteIssuerSetupParameter(string id);



    [OperationContract]
    [WebInvoke(Method = "POST",
               RequestFormat = WebMessageFormat.Json,
               ResponseFormat = WebMessageFormat.Json,
               UriTemplate = "FirstMessage",
               BodyStyle = WebMessageBodyStyle.Bare)]
    [FaultContract(typeof(RuntimeFault))]
    [Description("Create first uprove message for creating a new tokens.")]
    FirstIssuanceMessageInfo FirstMessage(FirstIssuanceMessageSpec spec);

    [OperationContract]
    [WebInvoke(Method = "POST",
               RequestFormat = WebMessageFormat.Json,
               ResponseFormat = WebMessageFormat.Json,
               UriTemplate = "ThirdMessage",
               BodyStyle = WebMessageBodyStyle.Bare)]
    [FaultContract(typeof(RuntimeFault))]
    [Description("Create thrid uprove message for creating a new tokens.")]
    ThirdIssuanceMessageInfo ThirdMessage(ThirdIssuanceMessageSpec spec);



  }

  internal class IssuerInstanceData : InstanceData
  {
    private Issuer _issuer;

    internal IssuerInstanceData(Issuer issuer)
    {
      this.LastAccessed = DateTime.Now;
      _issuer = issuer;
    }

    public Issuer Issuer
    {
      set
      {
        _issuer = value;
        this.LastAccessed = DateTime.Now;
      }
      get
      {
        this.LastAccessed = DateTime.Now;
        return _issuer;
      }
    }

  }
  
}




