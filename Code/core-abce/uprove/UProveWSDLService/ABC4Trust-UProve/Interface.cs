using System;
using System.Runtime.Serialization;
using System.Collections.Generic;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.ServiceModel.Description;
using UProveCrypto;
using System.Xml.Serialization;

/**
 * ABC4Trust UProve/Java WebService interop, server-side interface.
 * 
 * @author Raphael Dobers
 */

namespace abc4trust_uprove
{
  [ServiceContract(SessionMode = SessionMode.Allowed, Namespace = "http://abc4trust-uprove/Service1")]
  public interface IService1
  {

    [OperationContract]
    string ping();

    [OperationContract]
    string login(bool useVirtualDevice, string pinCode, int credID, int groupID, int proverID, int securityLevel);

    [OperationContract]
    void logout(String sessionID);

    [OperationContract]
    IssuerKeyAndParametersComposite setupIssuerParameters(string uniqueIdentifier, byte[] attributeEncoding, string hash, string sessionID);

    [OperationContract]
    bool verifyIssuerParameters(IssuerParametersComposite ipc, string sessionID);

    [OperationContract]
    void setIssuerPrivateKey(byte[] issuerPrivateKeyParam, string sessionID);

    [OperationContract]
    FirstIssuanceMessageComposite getFirstMessage(string[] attributesParam, IssuerParametersComposite ipc, int numberOfTokensParam, string sessionID, byte[] hd);

    [OperationContract]
    SecondIssuanceMessageComposite getSecondMessage(string[] attributesParam, IssuerParametersComposite ipc, int numberOfTokensParam, FirstIssuanceMessageComposite firstMessage, string sessionID);

    [OperationContract]
    ThirdIssuanceMessageComposite getThirdMessage(SecondIssuanceMessageComposite secondMessage, string sessionID);

    [OperationContract]
    List<UProveKeyAndTokenComposite> generateTokens(ThirdIssuanceMessageComposite thirdMessage, string sessionID);

    [OperationContract]
    PresentationProofComposite proveToken(string[] attributesParameters, int[] indicesOfDisclosedAttributes, int[] indicesOfCommittedAttributes, string messageParameter, string verifierScopeParameter, IssuerParametersComposite ipc, UProveTokenComposite tokenComposite, byte[] tokenPrivateKeyParam, string sessionID);

    [OperationContract]
    bool verifyTokenProof(PresentationProofComposite proof, int[] indicesOfDisclosedAttributes, int[] indicesOfCommittedAttributes, string messageParameter, string verifierScopeParameter, IssuerParametersComposite ipc, UProveTokenComposite token, string sessionID);

    [OperationContract]
    PseudonymComposite presentPseudonym(string messageParam, string verifierScopeParam, string sessionID);

    [OperationContract]
    bool verifyPseudonym(string messageParam, string verifierScopeParam, PseudonymComposite pseudonym, string sessionID);

    [OperationContract]
    byte[] generateSecret(string sessionID);

    [OperationContract]
    void setSecret(byte[] secret, string sessionID);
  }
  /*
     public GroupElement[] sigmaA { get; internal set; }
        [DataMember(Name = "sb")]
        public GroupElement[] sigmaB { get; internal set; }
        [DataMember(Name = "sz")]
        public GroupElement sigmaZ { get; internal set; }
     */
  [DataContract]
  public class FirstIssuanceMessageComposite
  {
    byte[][] sigmaA;
    byte[][] sigmaB;
    byte[] sigmaZ;
    string sessionKey;

    [DataMember]
    public byte[][] SigmaA
    {
      get { return sigmaA; }
      set { sigmaA = value; }
    }

    [DataMember]
    public byte[][] SigmaB
    {
      get { return sigmaB; }
      set { sigmaB = value; }
    }

    [DataMember]
    public byte[] SigmaZ
    {
      get { return sigmaZ; }
      set { sigmaZ = value; }
    }

    [DataMember]
    public string SessionKey
    {
      get { return sessionKey; }
      set { sessionKey = value; }
    }

  }
  /*
     [DataMember(Name = "sc")]
        public BigInteger[] sigmaC { get; internal set; }
     */

  [DataContract]
  public class SecondIssuanceMessageComposite
  {
    byte[][] sigmaC;
    string sessionKey;

    [DataMember]
    public byte[][] SigmaC
    {
      get { return sigmaC; }
      set { sigmaC = value; }
    }

    [DataMember]
    public string SessionKey
    {
      get { return sessionKey; }
      set { sessionKey = value; }
    }
  }

  /*
     public BigInteger[] sigmaR { get; internal set; }
     */
  [DataContract]
  public class ThirdIssuanceMessageComposite
  {
    byte[][] sigmaR;
    string sessionKey;

    [DataMember]
    public byte[][] SigmaR
    {
      get { return sigmaR; }
      set { sigmaR = value; }
    }

    [DataMember]
    public string SessionKey
    {
      get { return sessionKey; }
      set { sessionKey = value; }
    }
  }

  [DataContract]
  public class UProveKeyAndTokenComposite
  {
    byte[] privateKey;
    UProveTokenComposite token;

    [DataMember]
    public byte[] PrivateKey
    {
      get { return privateKey; }
      set { privateKey = value; }
    }

    [DataMember]
    public UProveTokenComposite Token
    {
      get { return token; }
      set { token = value; }
    }
  }

  [DataContract]
  public class IssuerKeyAndParametersComposite
  {
    byte[] privateKey;
    IssuerParametersComposite issuerParameters;

    [DataMember]
    public byte[] PrivateKey
    {
      get { return privateKey; }
      set { privateKey = value; }
    }

    [DataMember]
    public IssuerParametersComposite IssuerParameters
    {
      get { return issuerParameters; }
      set { issuerParameters = value; }
    }
  }

  [DataContract]
  public class PresentationProofComposite
  {
    byte[] a;
    byte[] ap;
    byte[][] disclosedAttributes;
    byte[] ps;
    byte[][] r;
    byte[][] tildeValues;
    byte[][] tildeO;
    byte[] tokenId;
    byte[] messageD; //message for device.


    [DataMember]
    public byte[] A
    {
      get { return a; }
      set { a = value; }
    }

    [DataMember]
    public byte[] Ap
    {
      get { return ap; }
      set { ap = value; }
    }

    [DataMember]
    public byte[][] DisclosedAttributes
    {
      get { return disclosedAttributes; }
      set { disclosedAttributes = value; }
    }

    [DataMember]
    public byte[] Ps
    {
      get { return ps; }
      set { ps = value; }
    }

    [DataMember]
    public byte[][] R
    {
      get { return r; }
      set { r = value; }
    }

    [DataMember]
    public byte[][] TildeValues
    {
      get
      {
        return tildeValues;
      }
      set
      {
        tildeValues = value;
      }
    }

    [DataMember]
    public byte[][] TildeO
    {
      get { return tildeO; }
      set { tildeO = value; }
    }

    [DataMember]
    public byte[] TokenID
    {
      get { return tokenId; }
      set { tokenId = value; }
    }

    [DataMember]
    public byte[] MessageD
    {
      get { return messageD; }
      set { messageD = value; }
    }

  }


  /*
     public GroupElement H { get; set; }
        public bool IsDeviceProtected { get; set; }
        public byte[] PI { get; set; }
        public BigInteger SigmaCPrime { get; set; }
        public BigInteger SigmaRPrime { get; set; }
        public GroupElement SigmaZPrime { get; set; }
        public byte[] TI { get; set; }
        public byte[] Uidp { get; set; }
     */

  [DataContract]
  public class UProveTokenComposite
  {
    byte[] h;
    bool isDeviceProtected;
    byte[] pi;
    byte[] sigmaCPrime;
    byte[] sigmaRPrime;
    byte[] sigmaZPrime;
    byte[] ti;
    byte[] uidp;

    [DataMember]
    public byte[] H
    {
      get { return h; }
      set { h = value; }
    }

    [DataMember]
    public bool IsDeviceProtected
    {
      get { return isDeviceProtected; }
      set { isDeviceProtected = value; }
    }

    [DataMember]
    public byte[] PI
    {
      get { return pi; }
      set { pi = value; }
    }

    [DataMember]
    public byte[] SigmaCPrime
    {
      get { return sigmaCPrime; }
      set { sigmaCPrime = value; }
    }

    [DataMember]
    public byte[] SigmaRPrime
    {
      get { return sigmaRPrime; }
      set { sigmaRPrime = value; }
    }

    [DataMember]
    public byte[] SigmaZPrime
    {
      get { return sigmaZPrime; }
      set { sigmaZPrime = value; }
    }

    [DataMember]
    public byte[] TI
    {
      get { return ti; }
      set { ti = value; }
    }

    [DataMember]
    public byte[] Uidp
    {
      get { return uidp; }
      set { uidp = value; }
    }

  }


  [DataContract]
  public class SubgroupGroupDescriptionComposite
  {
    byte[] p;
    byte[] q;
    byte[] g;
    byte[] gd;

    [DataMember]
    public byte[] P
    {
      get { return p; }
      set { p = value; }
    }

    [DataMember]
    public byte[] Q
    {
      get { return q; }
      set { q = value; }
    }

    [DataMember]
    public byte[] G
    {
      get { return g; }
      set { g = value; }
    }

    [DataMember]
    public byte[] Gd
    {
      get { return gd; }
      set { gd = value; }
    }

  }

  /**
       public byte[] E { get; set; }
        public GroupElement[] G { get; set; }
        public GroupElement Gd { get; set; }
        public GroupDescription Gq { get; set; }
        public string HashFunctionOID { get; }
        public bool IsDeviceSupported { get; }
        public byte[] S { get; set; }
        public string UidH { get; set; }
        public byte[] UidP { get; set; }
        public bool UsesRecommendedParameters { get; set; }
     */

  [DataContract]
  public class IssuerParametersComposite
  {
    byte[] e;
    byte[][] g;
    byte[] gd;
    SubgroupGroupDescriptionComposite gq; // GroupDescription is a name when usesRecommendedParameters = true
    string groupName;
    string hashFunctionOID;
    bool isDeviceSupported;
    byte[] s;
    string uidH;
    byte[] uidP;
    bool usesRecommendedParameters;

    [DataMember]
    public byte[] E
    {
      get { return e; }
      set { e = value; }
    }

    [DataMember]
    public byte[][] G
    {
      get { return g; }
      set { g = value; }
    }

    [DataMember]
    public byte[] Gd
    {
      get { return gd; }
      set { gd = value; }
    }

    [DataMember]
    public byte[] S
    {
      get { return s; }
      set { s = value; }
    }

    [DataMember]
    public byte[] UidP
    {
      get { return uidP; }
      set { uidP = value; }
    }

    [DataMember]
    public SubgroupGroupDescriptionComposite Gq
    {
      get { return gq; }
      set { gq = value; }
    }

    [DataMember]
    public string HashFunctionOID
    {
      get { return hashFunctionOID; }
      set { hashFunctionOID = value; }
    }

    [DataMember]
    public string UidH
    {
      get { return uidH; }
      set { uidH = value; }
    }

    [DataMember]
    public bool IsDeviceSupported
    {
      get { return isDeviceSupported; }
      set { isDeviceSupported = value; }
    }

    [DataMember]
    public bool UsesRecommendedParameters
    {
      get { return usesRecommendedParameters; }
      set { usesRecommendedParameters = value; }
    }

    [DataMember]
    public string GroupName
    {
      get
      {
        return groupName;
      }
      set
      {
        groupName = value;
      }
    }

  }

  [DataContract]
  public class PseudonymComposite
  {
    byte[] a;
    byte[] p;
    byte[] r;

    [DataMember]
    public byte[] A
    {
      get { return a; }
      set { a = value; }
    }

    [DataMember]
    public byte[] P
    {
      get { return p; }
      set { p = value; }
    }

    [DataMember]
    public byte[] R
    {
      get { return r; }
      set { r = value; }
    }
  }

  
  public class LoginComposite
  {
    bool useVirtualDevice;
    string pinCode;
    int credID;
    int groupID;
    int proverID;

    public LoginComposite(bool useVirtualDevice, string pinCode, int credID, int groupID, int proverID)
    {
      this.useVirtualDevice = useVirtualDevice;
      this.pinCode = pinCode;
      this.credID = credID;
      this.groupID = groupID;
      this.proverID = proverID;
    }

    public bool UseVirtualDevice
    {
      get
      {
        return useVirtualDevice;
      }
      set
      {
        useVirtualDevice = value;
      }
    }

    public string PinCode
    {
      get
      {
        return pinCode;
      }
      set
      {
        pinCode = value;
      }
    }

    public int CredID
    {
      get
      {
        return credID;
      }
      set
      {
        credID = value;
      }
    }

    public int GroupID
    {
      get
      {
        return groupID;
      }
      set
      {
        groupID = value;
      }
    }

    public int ProverID
    {
      get
      {
        return proverID;
      }
      set
      {
        proverID = value;
      }
    }
  }

}
