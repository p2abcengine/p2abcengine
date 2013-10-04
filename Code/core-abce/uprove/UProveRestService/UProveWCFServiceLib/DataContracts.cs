using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Runtime.Serialization;
using UProveCrypto;
namespace UProveWCFServiceLib
{

  [DataContract]
  public class FirstIssuanceMessageInfo
  {
    private FirstIssuanceMessage _fim;
    private PostFirstMessageState _postFirstMessageState;
    private Guid _instanceID;
   
    [DataMember(IsRequired = true)]
    public FirstIssuanceMessage FirstMessage
    {
      set { _fim = value; }
      get { return _fim; }
    }

    [DataMember(IsRequired = true)]
    public Guid IssuerInstanceID
    {
      set { _instanceID = value; }
      get { return _instanceID; }
    }

    [DataMember(IsRequired = true)]
    public PostFirstMessageState FirstMessageState
    {
      set { _postFirstMessageState = value; }
      get { return _postFirstMessageState; }
    }

  }

  [DataContract]
  public class SecondIssuanceMessageInfo
  {
    private SecondIssuanceMessage _secondMessage;
    private PostSecondMessageState _secondMessageState;
    private Guid _instanceID;


    [DataMember(IsRequired = true)]
    public SecondIssuanceMessage SecondMessage
    {
      set { _secondMessage = value; }
      get { return _secondMessage; }
    }

    [DataMember(IsRequired = true)]
    public PostSecondMessageState PostSecondMessageState
    {
      set { _secondMessageState = value; }
      get { return _secondMessageState; }
    }

    [DataMember(IsRequired = true)]
    public Guid ProverInstanceID
    {
      set { _instanceID = value; }
      get { return _instanceID; }
    }

  }

  
  [DataContract]
  public class ThirdIssuanceMessageInfo
  {
    private ThirdIssuanceMessage _thirdMessage;
    
    [DataMember(IsRequired = true)]
    public ThirdIssuanceMessage ThirdMessage
    {
      set { _thirdMessage = value; }
      get { return _thirdMessage; }
    }
  }

  [DataContract]
  public class GenerateTokensInfo
  {
    private UProveKeyAndToken[] _tokens;

    [DataMember(IsRequired = true)]
    public UProveKeyAndToken[] Tokens
    {
      set { _tokens = value; }
      get { return _tokens; }
    }
  }



  [DataContract]
  public abstract class MessageSpecBase
  {
    private int _numberOfTokens;
    private string _tokenInfomation;
    private string _gamma;
    private string _devicePubKey;

    [DataMember(IsRequired = true)]
    public int NumberOfTokens
    {
      set { _numberOfTokens = value; }
      get { return _numberOfTokens; }
    }

    [DataMember(IsRequired = false)]
    public string TokenInfomation
    {
      set { _tokenInfomation = value; }
      get { return _tokenInfomation; }
    }

    [DataMember(IsRequired = false)]
    public string Gamma
    {
      set { _gamma = value; }
      get { return _gamma; }
    }

    [DataMember(IsRequired = false)]
    public string DevicePublicKey
    {
      set { _devicePubKey = value; }
      get { return _devicePubKey; }
    }

  }

  [DataContract]
  public class ThirdIssuanceMessageSpec
  {
    private Guid _instanceID;
    private IssuerKeyAndParameters _issuerKeyAndParameter;
    private SecondIssuanceMessage _secondMessage;
    private PostFirstMessageState _postFirstMessageState;

    [DataMember(IsRequired = true)]
    public SecondIssuanceMessage SecondMessage
    {
      set { _secondMessage = value; }
      get { return _secondMessage; }
    }

    [DataMember(IsRequired=false)]
    public IssuerKeyAndParameters IssuerKeyAndParameter
    {
      set { _issuerKeyAndParameter = value; }
      get { return _issuerKeyAndParameter; }
    }

    [DataMember(IsRequired = false)]
    public Guid IssuerInstanceID
    {
      set { _instanceID = value; }
      get { return _instanceID; }
    }

    [DataMember(IsRequired = false)]
    public PostFirstMessageState FistMessageState
    {
      set { _postFirstMessageState = value; }
      get { return _postFirstMessageState; }
    }
  }

  [DataContract]
  public class SecondIssuanceMessageSpec : MessageSpecBase
  {
    private IssuerParameters _issuerParam;
    private FirstIssuanceMessage _firstMessage;
    private string _proverInformation;
    private ProverRandomData _randomData;
    private ushort _batchSecurityLevel;

    [DataMember(IsRequired = true)]
    public IssuerParameters IssuerParameter
    {
      set { _issuerParam = value; }
      get { return _issuerParam; }
    }

    [DataMember(IsRequired = true)]
    public FirstIssuanceMessage FirstMessage
    {
      set { _firstMessage = value; }
      get { return _firstMessage; }
    }

    [DataMember(IsRequired = false)]
    public string ProverInfomation
    {
      set { _proverInformation = value; }
      get { return _proverInformation; }
    }

    [DataMember(IsRequired = false)]
    public ProverRandomData RandomData
    {
      set { _randomData = value; }
      get { return _randomData; }
    }

    [DataMember(IsRequired = false)]
    public ushort BatchSecurityLevel
    {
      set { _batchSecurityLevel = value; }
      get { return _batchSecurityLevel; }
    }
  }


  [DataContract]
  public class FirstIssuanceMessageSpec : MessageSpecBase
  {
    private string _issuerId;
    private List<string> _attributes;

    [DataMember(IsRequired = true)]
    public string IssuerID
    {
      set { _issuerId = value; }
      get { return _issuerId; }
    }

    [DataMember(IsRequired = true)]
    public List<string> Attributes
    {
      set { _attributes = value; }
      get { return _attributes; }
    }
  }


  [DataContract]
  public class IssuerParameterInfo
  {
    private String _parameterName;
    private IssuerParameters _issuerParamSet;

    [DataMember]
    public String ParameterSetName
    {
      set { _parameterName = value; }
      get { return _parameterName; }
    }

    [DataMember]
    public IssuerParameters IssuerParameterSet
    {
      set { _issuerParamSet = value; }
      get { return _issuerParamSet; }
    }

  }

  [DataContract]
  public class GenerateTokensSpec
  {
    private PostSecondMessageState _seondMessageState;
    private ThirdIssuanceMessage _thirdMessage;
    private IssuerParameters _issuerPara;
    private Guid _proverID;

    [DataMember(IsRequired = true)]
    public ThirdIssuanceMessage ThirdMessage
    {
      set { _thirdMessage = value; }
      get { return _thirdMessage; }
    }

    [DataMember(IsRequired = false)]
    public IssuerParameters IssuerParameter
    {
      set { _issuerPara = value; }
      get { return _issuerPara; }
    }

    [DataMember(IsRequired = false)]
    public Guid ProverInstanceID
    {
      set { _proverID = value; }
      get { return _proverID; }
    }

    [DataMember(IsRequired = false)]
    public PostSecondMessageState SecondMessageState
    {
      set { _seondMessageState = value; }
      get { return _seondMessageState; }
    }
  }


  [DataContract]
  public class IssuerSetupParametersSpec
  {
    private ProtocolHelper.SupportedHashFunctions _hashFunction;
    private String _issuerID;
    private String _parameterName;
    private Int32 _numberOfAttributes;
    private byte[] _attributeEncoding;
    private Nullable<Boolean> _useRecommendedParameterSet;
    private Nullable<GroupType> _groupType;
    private bool _storeOnServer;

    // For now we only supports predefined ParameterSets.
    //private ParameterSet _parameterSet;


    [DataMember(IsRequired = true)]
    public ProtocolHelper.SupportedHashFunctions HashFunction
    {
      set { _hashFunction = value; }
      get { return _hashFunction; }
    }

    [DataMember(IsRequired=true)]
    public bool StoreOnServer
    {
      set { _storeOnServer = value; }
      get { return _storeOnServer; }
    }

    [DataMember(IsRequired = true)]
    public String IssuerID
    {
      set { _issuerID = value; }
      get { return _issuerID; }
    }

    [DataMember(IsRequired = true)]
    public Int32 NumberOfAttributes
    {
      set { _numberOfAttributes = value; }
      get { return _numberOfAttributes; }
    }

    [DataMember(IsRequired = false)]
    public byte[] AttributeEncoding
    {
      set { _attributeEncoding = value; }
      get { return _attributeEncoding; }
    }

    [DataMember(IsRequired = false)]
    public Nullable<Boolean> UseRecommendedParameterSet
    {
      set { _useRecommendedParameterSet = value; }
      get { return _useRecommendedParameterSet; }
    }

    [DataMember(IsRequired = false)]
    public Nullable<GroupType> GroupConstruction
    {
      set { _groupType = value; }
      get { return _groupType; }
    }

    [DataMember(IsRequired = true)]
    public String ParameterSetName
    {
      set { _parameterName = value; }
      get { return _parameterName; }
    }

    /*
    [DataMember(IsRequired = false)]
    public ParameterSet PreDefinedParameterSet
    {
      set { _parameterSet = value; }
      get { return _parameterSet; }
    }
    */

  }

}