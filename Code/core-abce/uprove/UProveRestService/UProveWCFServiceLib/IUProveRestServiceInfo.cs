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
  public interface IUProveRestServiceInfo
  {
    [OperationContract]
    [WebInvoke(Method = "GET",
               ResponseFormat = WebMessageFormat.Json,
               UriTemplate = "ServerInfo",
               BodyStyle = WebMessageBodyStyle.Bare)]
    [Description("Returns information about what versions of uprove the server supports etc.")]
    ServerInfo ServerInfo();

    [OperationContract]
    [WebInvoke(Method = "GET",
               ResponseFormat = WebMessageFormat.Json,
               UriTemplate = "GroupTypeMap",
               BodyStyle = WebMessageBodyStyle.Bare)]
    [Description("Returns a map between the different uprove groups.")]
    Dictionary<string, GroupType> GroupTypeMap();

    [OperationContract]
    [WebInvoke(Method = "GET",
               ResponseFormat = WebMessageFormat.Json,
               UriTemplate = "SupportedShaFunctions",
               BodyStyle = WebMessageBodyStyle.Bare)]
    [Description("Returns a map of supported sha hash functions.")]
    Dictionary<string, ProtocolHelper.SupportedHashFunctions> SupportedShaFunctions();


    [OperationContract]
    [WebInvoke(Method = "GET",
               ResponseFormat = WebMessageFormat.Json,
               UriTemplate = "ParameterSet",
               BodyStyle = WebMessageBodyStyle.Bare)]
    [Description("Returns list of supported default parameter set for uprove.")]
    List<ParameterSetInfo> ParameterSet();


  }


  [DataContract]
  public class ParameterSetInfo
  {
    private string _name;
    private GroupType _type;
    private int _numberOfGenerators;
    private ProtocolHelper.SupportedHashFunctions _hashFun;

    [DataMember]
    public string Name
    {
      set { _name = value; }
      get { return _name; }
    }

    [DataMember]
    public GroupType Type
    {
      set { _type = value; }
      get { return _type; }
    }

    [DataMember]
    public int NumberOfGenerators
    {
      set { _numberOfGenerators = value; }
      get { return _numberOfGenerators; }
    }

    [DataMember]
    public ProtocolHelper.SupportedHashFunctions HashFunction
    {
      set { _hashFun = value; }
      get { return _hashFun; }
    }


  }




  [DataContract]
  public class ServerInfo
  {
    private Uri _address;
    private Guid _serverUUID;
    private Version _uproveVersion;
    private List<GroupType> _uproveSupportedGroups;
    private KeyValuePair<string, ProtocolVersion> _protocolVersion;

    [DataMember]
    public List<GroupType> UProveSupportedGroups
    {
      set { _uproveSupportedGroups = value; }
      get { return _uproveSupportedGroups; }
    }

    [DataMember]
    public KeyValuePair<string, ProtocolVersion> ProtocolVersion
    {
      set { _protocolVersion = value; }
      get { return _protocolVersion; }
    }

    [DataMember]
    public Guid serverUUID
    {
      set { _serverUUID = value; }
      get { return _serverUUID; }
    }

    [DataMember]
    public Version uProveVersion
    {
      set { _uproveVersion = value; }
      get { return _uproveVersion; }
    }

    [DataMember]
    public Uri address
    {
      get
      {
        return _address;
      }
      set
      {
        _address = value;
      }
    }
  }
}
