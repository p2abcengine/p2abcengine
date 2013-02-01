using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.ServiceModel;
using System.Text;
using System.Threading;
using abc4trust_uprove.DataObjects;
using ABC4TrustSmartCard;
using UProveCrypto;

namespace abc4trust_uprove
{
  [ServiceBehavior(InstanceContextMode = InstanceContextMode.PerSession,
                   ConcurrencyMode = ConcurrencyMode.Multiple,
                   Namespace = "http://abc4trust-uprove/Service1")]
  public class Service1 : IService1
  {
    private static volatile Service1 instance;
    private static object syncRoot = new Object();

    private Service1() { }
    private static Log cOut = Logger.Instance.getLog(LoggerDefine.OUT_CONSOLE);
    private static Log dOut = Logger.Instance.getLog(LoggerDefine.DEBUG_CONSOLE);

    public static Service1 Instance
    {
      get
      {
        if (instance == null)
        {
          lock (syncRoot)
          {
            if (instance == null)
              instance = new Service1();
          }
        }

        return instance;
      }
    }

    private static System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();

    private static ConcurrentDictionary<string, SessionData> sessionDB = new ConcurrentDictionary<string, SessionData>();

    // run the clean up session thread every 5 min.
    private static Timer sessionCleaner = new Timer(cleanSession, null, TimeSpan.Zero, TimeSpan.FromMinutes(5));
    private static int DevicePseudonymIndex = 254;


    private static void cleanSession(Object obj)
    {
      //we automatic clean sessions that at least have not been accessed in 40 min.
      TimeSpan tSpan = TimeSpan.FromMinutes(40);
      foreach (KeyValuePair<string, SessionData> kv in sessionDB)
      {
        SessionData sData = kv.Value;
        if ((DateTime.Now - sData.lastAccessed) > tSpan)
        {
          sessionDB.TryRemove(kv.Key, out sData);
        }
      }
    }

    public string login(bool useVirtualDevice, string pinCode, int credID, int groupID, int proverID, int securityLevel)
    {

      LoginComposite loginParam = new LoginComposite(useVirtualDevice, pinCode, credID, groupID, proverID);
      SessionData sData = new SessionData(loginParam, SessionData.generateSessionKey(), securityLevel);
      sessionDB.TryAdd(sData.sessionID, sData);
      return sData.sessionID;
    }

    public void logout(string sessionID)
    {
      VerifySessionId(sessionID);

      SessionData sData;
      sessionDB.TryRemove(sessionID, out sData);
    }

    private void VerifySessionId(string sessionID)
    {
      if (!sessionDB.ContainsKey(sessionID))
      {
        throw new ArgumentException("Not a valid session");
      }
    }

    // Ping method for checking the actual availability of the UProve Webservice without having to invoke Uprove specific methods. 
    public string ping()
    {
      return "pong";
    }

    /// <summary>
    /// Sets up the U-Prove issuer parameters.
    /// </summary>
    /// <param name="uniqueIdentifier">The unique identifier for the issuer parameters.</param>
    /// <param name="attributeEncoding">An array of byte describing how each attribute is encoded in the token (byte value 0 = direct encoding, byte value 1 = hashed using the algorithm identified by <code>hash</code>).</param>
    /// <param name="hash">The hash algorithm to use.</param>
    /// <returns>The generated issuer parameters.</returns>
    public IssuerKeyAndParametersComposite setupIssuerParameters(string uniqueIdentifier, byte[] attributeEncoding, string hash, string sessionID)
    {
      /*
       *  issuer setup
       */
      cOut.write("Setting up Issuer parameters: " + uniqueIdentifier);
      VerifySessionId(sessionID);

      try
      {
        IssuerSetupParameters isp = new IssuerSetupParameters();

        // pick the group construction (defaults to subgroup, but ECC is more efficient)
        // TODOv2: Enable the UProve module to switch between ECC and subgroup based on ie. a config file.
        // isp.GroupConstruction = GroupConstruction.ECC;
        // right now, we need to use the subgroup construction to interop with Idemix
        isp.GroupConstruction = GroupConstruction.Subgroup;
        isp.ParameterSet = sessionDB[sessionID].parameterSet;
        isp.UidH = hash;

        // pick a unique identifier for the issuer params
        isp.UidP = encoding.GetBytes(uniqueIdentifier);

        // set the encoding parameters for the attributes
        isp.E = attributeEncoding;

        // specification field unused in ABC4Trust
        isp.S = null;

        // generate the serializable IssuerKeyAndParameters		
        IssuerKeyAndParameters ikap = isp.Generate(true);
        IssuerKeyAndParametersComposite ikpc = new IssuerKeyAndParametersComposite();
        ikpc.IssuerParameters = ConvertUtils.convertIssuerParameters(ikap.IssuerParameters);
        ikpc.PrivateKey = ikap.PrivateKey.ToByteArray();

        return ikpc;

      }
      catch (Exception e)
      {
        cOut.write(e.ToString());
        DebugUtils.DebugPrint(e.StackTrace.ToString());
      }

      return null;
    }

    /// <summary>
    /// Verifies the issuer parameters.
    /// </summary>
    /// <param name="ipc">The issuer parameters.</param>
    /// <returns>True if the parameters are valid, false otherwise.</returns>
    public bool verifyIssuerParameters(IssuerParametersComposite ipc, string sessionID)
    {
      VerifySessionId(sessionID);
      cOut.write("Verifying Issuer parameters: ");
      try
      {
        IssuerParameters ip = ConvertUtils.convertIssuerParametersComposite(ipc,  sessionDB[sessionID].parameterSet);
        ip.Verify();
        return true;
      }
      catch (Exception e)
      {
        cOut.write("Exception caught: " + e.Message);
        DebugUtils.DebugPrint(e.StackTrace.ToString());
        return false;
      }

    }

    /// <summary>
    /// Sets the issuer private key.
    /// </summary>
    /// <param name="issuerPrivateKeyParam">The issuer private key.</param>
    public void setIssuerPrivateKey(byte[] issuerPrivateKeyParam, string sessionID)
    {
      cOut.write("Setting Issuer Private Key...");
      VerifySessionId(sessionID);
      try
      {
        // Store the privateKey in privateKeysDictionary using the sessionKey as key
        sessionDB[sessionID].privateKey = issuerPrivateKeyParam;
      }
      catch (Exception e)
      {
        cOut.write(e.ToString());
        DebugUtils.DebugPrint(e.StackTrace.ToString());
      }

    }

    // issuerPrivateKey must be set using setIssuerPrivateKey() before calling this method
    public FirstIssuanceMessageComposite getFirstMessage(string[] attributesParam, IssuerParametersComposite ipc, int numberOfTokensParam, string sessionID, byte[] hd)
    {
      /*
       *  token issuance - generate first message
       */

      cOut.write("Issuing U-Prove tokens - generate first message, issuer side");

      VerifySessionId(sessionID);
      try
      {

        // specify the attribute values agreed to by the Issuer and Prover
        int numberOfAttributes = attributesParam.Length;
        byte[][] attributes = new byte[numberOfAttributes][];
        for (int i = 0; i < numberOfAttributes; i++)
        {
          attributes[i] = encoding.GetBytes(attributesParam[i]);
        }

        IssuerParameters ip = ConvertUtils.convertIssuerParametersComposite(ipc, sessionDB[sessionID].parameterSet);
        byte[] issuerPrivateKey = sessionDB[sessionID].privateKey;
        if (issuerPrivateKey == null)
        {
          cOut.write("Issuer side, issuerPrivateKey is null. Did you forget to add the issuer private key for the given sessionKey?");
          return null;
        }
        BigInteger bi = new BigInteger(1, issuerPrivateKey);
        IssuerKeyAndParameters ikap = new IssuerKeyAndParameters(bi, ip);

        // setup the issuer and generate the first issuance message

        GroupElement hdG = sessionDB[sessionID].parameterSet.Group.CreateGroupElement(hd);

        Issuer issuer = new Issuer(ikap, numberOfTokensParam, attributes, null, hdG);

        // Store the issuer in issuersDictionary using the sessionKey as key
        sessionDB[sessionID].issuer = issuer;

        FirstIssuanceMessage fi = issuer.GenerateFirstMessage();

        // Convert FirstIssuanceMessage members to serializable FirstIssuanceMessageComposite
        FirstIssuanceMessageComposite fic = ConvertUtils.convertFirstIssuanceMessage(fi);

        // Add the sessionKey to FirstIssuanceMessageComposite
        fic.SessionKey = sessionID;

        return fic;
      }
      catch (Exception e)
      {
        cOut.write(e.ToString());
        DebugUtils.DebugPrint(e.StackTrace.ToString());
      }

      return null;
    }

    public SecondIssuanceMessageComposite getSecondMessage(string[] attributesParam, IssuerParametersComposite ipc, int numberOfTokensParam, FirstIssuanceMessageComposite firstMessage, string sessionID)
    {
      /*
       *  token issuance - generate second message
       */

      cOut.write("Issuing U-Prove tokens - generate second message, prover side");
      VerifySessionId(sessionID);

      try
      {
        string tokenInformationParam = null;
        string proverInformationParam = null;

        // specify the attribute values agreed to by the Issuer and Prover
        int numberOfAttributes = attributesParam.Length;
        byte[][] attributes = new byte[numberOfAttributes][];
        for (int i = 0; i < numberOfAttributes; i++)
        {
          attributes[i] = encoding.GetBytes(attributesParam[i]);
        }

        // specify the special field values
        byte[] tokenInformation = (tokenInformationParam == null) ? new byte[] { } : encoding.GetBytes(tokenInformationParam);
        byte[] proverInformation = (proverInformationParam == null) ? new byte[] { } : encoding.GetBytes(proverInformationParam);

        // specify the number of tokens to issue
        int numberOfTokens = numberOfTokensParam;

        IssuerParameters ip = ConvertUtils.convertIssuerParametersComposite(ipc, sessionDB[sessionID].parameterSet);

        // Convert serializable FirstIssuanceMessageComposite members to FirstIssuanceMessage
        FirstIssuanceMessage fi = ConvertUtils.convertFirstIssuanceMessageComposite(firstMessage, ip);

        // setup the prover and generate the second issuance message
        Prover prover = new Prover(ip, numberOfTokens, attributes, tokenInformation, proverInformation, sessionDB[sessionID].deviceManager.GetDevice());

        // Store the prover in proversDictionary using the sessionKey as key
        sessionDB[sessionID].prover = prover;

        SecondIssuanceMessage sm = prover.GenerateSecondMessage(fi);

        // Convert SecondIssuanceMessage members to serializable SecondIssuanceMessageComposite
        SecondIssuanceMessageComposite smc = ConvertUtils.convertSecondIssuanceMessage(sm);

        // Add the sessionKey to SecondIssuanceMessageComposite
        smc.SessionKey = sessionID;

        return smc;

      }
      catch (Exception e)
      {
        cOut.write(e.ToString());
        DebugUtils.DebugPrint(e.StackTrace.ToString());
      }

      return null;
    }

    public ThirdIssuanceMessageComposite getThirdMessage(SecondIssuanceMessageComposite secondMessage, string sessionID)
    {
      /*
             *  token issuance - generate third message
            */

      cOut.write("Issuing U-Prove tokens - generate third message, issuer side");
      VerifySessionId(sessionID);
      try
      {
        // Convert serializable SecondIssuanceMessageComposite members to SecondIssuanceMessage
        SecondIssuanceMessage si = ConvertUtils.convertSecondIssuanceMessageComposite(secondMessage);

        // Get unique session key from SecondIssuanceMessageComposite for retrieving the correct Issuer instance from issuersDictionary
        string sessionKey = secondMessage.SessionKey;
        //VerifySessionId(sessionKey);

        // Retrieve correct Issuer instance from issuersDictionary for the given sessionKey
        if (sessionDB.ContainsKey(sessionID))
        {
          Issuer issuer = sessionDB[sessionID].issuer;

          ThirdIssuanceMessage tim = issuer.GenerateThirdMessage(si);

          // Convert ThirdIssuanceMessage members to serializable ThirdIssuanceMessageComposite
          ThirdIssuanceMessageComposite timc = ConvertUtils.convertThirdIssuanceMessage(tim);

          // Add the sessionKey to ThirdIssuanceMessageComposite
          timc.SessionKey = sessionID;

          return timc;
        }
      }
      catch (Exception e)
      {
        cOut.write(e.ToString());
        DebugUtils.DebugPrint(e.StackTrace.ToString());
      }

      return null;
    }

    public List<UProveKeyAndTokenComposite> generateTokens(ThirdIssuanceMessageComposite thirdMessage, string sessionID)
    {
      /*
             *  token issuance - generate tokens.
            */

      cOut.write("Issuing U-Prove tokens - generate tokens, prover side");
      VerifySessionId(sessionID);

      try
      {
        // Convert serializable ThirdIssuanceMessageComposite members to ThirdIssuanceMessage.
        ThirdIssuanceMessage tm = ConvertUtils.convertThirdIssuanceMessageComposite(thirdMessage);

        // Retrieve correct Prover instance from proversDictionary for the given sessionKey.
        if (sessionDB.ContainsKey(sessionID))
        {
          Prover prover = sessionDB[sessionID].prover;
          // generate the tokens.
          UProveKeyAndToken[] upkt = prover.GenerateTokens(tm);

          // Serialize them.
          cOut.write("Generating tokens...");

          List<UProveKeyAndTokenComposite> tokens = new List<UProveKeyAndTokenComposite>();

          foreach (UProveKeyAndToken t in upkt)
          {
            byte[] bigInt = t.PrivateKey.ToByteArray();
            UProveKeyAndTokenComposite keyAndTokenComposite = new UProveKeyAndTokenComposite();
            keyAndTokenComposite.PrivateKey = bigInt;
            keyAndTokenComposite.Token = ConvertUtils.convertUProveToken(t.Token);
            tokens.Add(keyAndTokenComposite);
          }

          return tokens;
        }

      }
      catch (Exception e)
      {
        cOut.write(e.ToString());
        DebugUtils.DebugPrint(e.StackTrace.ToString());
      }

      return null;
    }

    public PresentationProofComposite proveToken(string[] attributesParam, int[] disclosedIndices, int[] committedIndices, string messageParam, string verifierScopeParam, IssuerParametersComposite ipc, UProveTokenComposite tokenComposite, byte[] tokenPrivateKeyParam, string sessionID)
    {
      /*
       *  token presentation
       */

      cOut.write("Presenting a U-Prove token");
      VerifySessionId(sessionID);
      try
      {
        // specify the attribute values agreed to by the Issuer and Prover
        int numberOfAttributes = attributesParam.Length;
        byte[][] attributes = new byte[numberOfAttributes][];
        for (int i = 0; i < numberOfAttributes; i++)
        {
          attributes[i] = encoding.GetBytes(attributesParam[i]);
        }

        IssuerParameters ip = ConvertUtils.convertIssuerParametersComposite(ipc, sessionDB[sessionID].parameterSet);
        // the application-specific message that the prover will sign. Typically this is a nonce combined
        // with any application-specific transaction data to be signed.
        byte[] message = encoding.GetBytes(messageParam);

        // the application-specific verifier scope from which a scope-exclusive pseudonym will be created
        // (if null, then a pseudonym will not be presented)
        byte[] scope = null;
        if (verifierScopeParam != null && verifierScopeParam != "null")
        {
          scope = encoding.GetBytes(verifierScopeParam);
        }

        // generate the presentation proof
        UProveToken uProveToken = ConvertUtils.convertUProveTokenComposite(ip, tokenComposite);
        byte[] bigInt = tokenPrivateKeyParam;
        DeviceManager dManager = sessionDB[sessionID].deviceManager;
        UProveKeyAndToken keyAndToken = new UProveKeyAndToken();
        keyAndToken.PrivateKey = new BigInteger(1, bigInt);
        keyAndToken.Token = uProveToken;
        byte[] proofSession = null;
        if (!dManager.IsVirtualDevice)
        {
          SmartCardDevice smartDevice = (SmartCardDevice)dManager.GetDevice();
          smartDevice.ProofSession = smartDevice.Device.BeginCommitment(1);
          byte[] proofSessionRaw = smartDevice.ProofSession;
          proofSession = new byte[1 + proofSessionRaw.Length];
          proofSession[0] = 1;
          Buffer.BlockCopy(proofSessionRaw, 0, proofSession, 1, proofSessionRaw.Length);
        }
        BigInteger[] commitmentValues;
        PresentationProof p =
          PresentationProof.Generate(ip,
                                     disclosedIndices,
                                     committedIndices,
                                     scope != null ? DevicePseudonymIndex : 0,
                                     scope,
                                     message,
                                     proofSession,
                                     dManager.GetDevice().GetPresentationContext(),
                                     keyAndToken,
                                     attributes,
                                     out commitmentValues);
#if DEBUG
        dManager.pDebug = p;
#endif

        return ConvertUtils.convertPresentationProof(p, commitmentValues, ProtocolHelper.ComputeTokenID(ip, uProveToken), proofSession);
      }
      catch (Exception e)
      {
        cOut.write(e.ToString());
        DebugUtils.DebugPrint(e.StackTrace.ToString());
      }

      return null;
    }

    public bool verifyTokenProof(PresentationProofComposite proof, int[] disclosedIndices, int[] committedIndices, string messageParam, string verifierScopeParam, IssuerParametersComposite ipc, UProveTokenComposite token, string sessionID)
    {
      /*
             *  token verification
            */

      cOut.write("Verifying a U-Prove token");
      VerifySessionId(sessionID);
      IssuerParameters ip = ConvertUtils.convertIssuerParametersComposite(ipc, sessionDB[sessionID].parameterSet);

      // the application-specific message that the prover will sign. Typically this is a nonce combined
      // with any application-specific transaction data to be signed.
      byte[] message = encoding.GetBytes(messageParam);

      // the application-specific verifier scope from which a scope-exclusive pseudonym will be created
      // (if null, then a pseudonym will not be presented)
      byte[] scope = null;
      if (verifierScopeParam != "null")
      {
        scope = encoding.GetBytes(verifierScopeParam);
      }

      // verify the presentation proof
      try
      {
        byte[] tokenId;
        byte[] proofSession;

        UProveToken t = ConvertUtils.convertUProveTokenComposite(ip, token);
        PresentationProof p = ConvertUtils.convertPresentationProofComposite(ip, proof, out tokenId, out proofSession);

        p.Verify(ip,
                 disclosedIndices,
                 committedIndices,
                 scope != null ? DevicePseudonymIndex : 0,
                 scope,
                 message,
                 proofSession,
                 t);
        if (proof.TokenID != null && !ProtocolHelper.ComputeTokenID(ip, t).SequenceEqual(proof.TokenID))
        {
          cOut.write("Invalid Token ID");
          return false;
        }
        return true;
      }
      catch (Exception e)
      {
        cOut.write("Exception caught: " + e.Message);
        DebugUtils.DebugPrint(e.StackTrace.ToString());
        return false;
      }
    }

    public PseudonymComposite presentPseudonym(string messageParam, string verifierScopeParam, string sessionID)
    {
      // invoke the device to compute the pseudonym value and response.
      // if a scope-exclusive pseudonym is requested (when scope != null) then 
      // the device-computed scope-exclusive pseudonym is used. Otherwise, the 
      // device's public key and initial witness are used in lieu of the pseudonym
      // value and commitment, respectively.

      cOut.write("presentPseudonym()");
      VerifySessionId(sessionID);
      GroupElement A = null;
      GroupElement P = null;
      BigInteger R = null;
      try
      {
        DeviceManager dManager = sessionDB[sessionID].deviceManager;
        bool scopeExclusive = (verifierScopeParam != null && verifierScopeParam != "null" && verifierScopeParam.Length > 0);
        IDevicePresentationContext ctx = dManager.GetDevice().GetPresentationContext();
        byte[] proofSession = null;
        if (!dManager.IsVirtualDevice)
        {
          SmartCardDevice smartDevice = (SmartCardDevice)dManager.GetDevice();
          smartDevice.ProofSession = smartDevice.Device.BeginCommitment(1);
          proofSession = smartDevice.ProofSession;
        }
        if (scopeExclusive)
        {
          ctx.GetInitialWitnessesAndPseudonym(encoding.GetBytes(verifierScopeParam), out A, out P);
        }
        else
        {
          P = dManager.GetDevice().GetDevicePublicKey();
          A = ctx.GetInitialWitness();
        }
        if (dManager.IsVirtualDevice)
        {
          R = ctx.GetDeviceResponse(encoding.GetBytes(messageParam), null, dManager.HashFunctionOID);
        }
        else
        {
          R = ctx.GetDeviceResponse(proofSession, encoding.GetBytes(messageParam), dManager.HashFunctionOID);
        }
      }

      catch (Exception e)
      {
        cOut.write("Exception caught: " + e.Message);
        DebugUtils.DebugPrint(e.StackTrace.ToString());
        return new PseudonymComposite();
      }

      return ConvertUtils.convertPseudonym(new Pseudonym(A, P, R));
    }

    public bool verifyPseudonym(string messageParam, string verifierScopeParam, PseudonymComposite pseudonym, string sessionID)
    {
      // we validate the pseudonym here because the U-Prove SDK only supports this while
      // validating a presentation proof

      cOut.write("verfiyPseudonym()");
      VerifySessionId(sessionID);
      try
      {
        DeviceManager dManager = sessionDB[sessionID].deviceManager;
        dManager.EnsureDeviceInit();
        Pseudonym p = ConvertUtils.convertPseudonymComposite(pseudonym, dManager);
        bool scopeExclusive = (verifierScopeParam != null && verifierScopeParam != "null" && verifierScopeParam.Length > 0);
        GroupElement baseElement = null;
        if (scopeExclusive)
        {
          baseElement = ProtocolHelper.GenerateScopeElement(dManager.Gq, encoding.GetBytes(verifierScopeParam));
        }
        else
        {
          baseElement = dManager.Gd;
        }
        BigInteger c = dManager.ComputeDeviceChallenge(encoding.GetBytes(messageParam));
        // A =?= g^R P^c
        return p.A.Equals(baseElement.Exponentiate(p.R).Multiply(p.P.Exponentiate(c)));
      }

      catch (Exception e)
      {
        cOut.write("Exception caught: " + e.Message);
        DebugUtils.DebugPrint(e.StackTrace.ToString());
        return false;
      }
    }

    public byte[] generateSecret(string sessionID)
    {
      VerifySessionId(sessionID);
      return new byte[] { 9, 8, 0, 5, 2 }; // FIXME (TODO) currently, user secret use the built-in device secret
    }

    public void setSecret(byte[] secret, string sessionID)
    {
      VerifySessionId(sessionID);
      // FIXME (TODO) currently, user secret use the built-in device secret
    }

  }
}
