using System;
using System.Security.Cryptography;
using UProveCrypto;
using ABC4TrustSmartCard;
using System.Collections.Generic;


namespace abc4trust_uprove
{
  /// <summary>
  /// Implements a device wrapper around the abc4trust smardcard
  /// </summary>
  internal class SmartCardDevice : IDevice, IDisposable
  {
    private readonly GroupDescription Gq;
    private readonly GroupElement Gd;
    private SmartCard device;

    public SmartCard Device { get { return device; } }

    private string pin;
    
    private int credID;
    private int groupID;
    private int proverID;
    private static Log cOut = Logger.Instance.getLog(LoggerDefine.OUT_CONSOLE);

    public byte[] ProofSession { get; set; }
    public GroupDescription getGroupDescription() { return Gq; }
    public GroupElement getGroupElement() { return Gd; }


    /// <summary>
    /// Constructs a new SampleDevice instance.
    /// </summary>
    /// <param name="gq">The group construction.</param>
    /// <param name="gd">The device generator.</param>
    public SmartCardDevice(GroupDescription gq, GroupElement gd, SmartCardParams smartCardParam)
    {
      pin = smartCardParam.pin;
      credID = smartCardParam.credID;
      groupID = smartCardParam.groupID;
      proverID = smartCardParam.proverID;
     
      // As SnartCardDevice do not provide a way to lookup card readr names
      // we provide a small potion of logic to lookup a card and cardreader
      List<CardInfo> cardInfoList = SmartCardUtils.GetReaderNames();
      // loop until we find a card with the status of "working mode". if none found
      // throw
      String readerName = null;
      foreach (CardInfo i in cardInfoList)
      {
        if (i.CardMode == (int)CardMode.WORKING)
        {
          readerName = i.ReaderName;
          break;
        }
      }
      if (readerName == null)
      {
        // TODO create a better exception
        throw new Exception("No card founds in working mode");
      }
      this.device = new SmartCard(readerName, pin);
      // As the group and generator is set from the java init service we will only verify
      // TODO fix to see that group 0 is set on the hw smartcard.
      //if (!this.device.IsGeneratorSet(groupID))
      //{
        // TODO Find better exception
       // throw new Exception("No generator is set on the card to use this group");
      //}

      this.Gq = gq;
      this.Gd = gd;
    }

    /// <summary>
    /// Returns the Device public key <code>h_d</code>.
    /// Note: It returns the public key of the creadential ID
    /// </summary>
    /// <returns>
    ///   <code>h_d</code>.
    /// </returns>
    GroupElement IDevice.GetDevicePublicKey()
    {
      byte[] pubKey = this.device.GetDevicePublicKey(true);
      GroupElement gElement = this.Gq.CreateGroupElement(pubKey); 
      return gElement;
    }

    /// <summary>
    /// Gets the presentation context.
    /// </summary>
    /// <returns>
    /// A presentation context.
    /// </returns>
    IDevicePresentationContext IDevice.GetPresentationContext()
    {
      return new DevicePresentationContext(this);
    }

    /// <summary>
    /// A device presentation context
    /// </summary>
    public sealed class DevicePresentationContext : IDevicePresentationContext
    {
      /// <summary>
      /// The device
      /// </summary>
      SmartCardDevice device;

      /// <summary>
      /// Initializes a new instance of the <see cref="DevicePresentationContext"/> class.
      /// </summary>
      /// <param name="device">The device.</param>
      public DevicePresentationContext(SmartCardDevice device)
      {
        this.device = device;
      }

      GroupElement IDevicePresentationContext.GetInitialWitness()
      {
        DebugUtils.DebugPrintBegin(null);
        if (this.device == null)
        {
          throw new DeviceException("Invalid context.");
        }

        try
        {
          // call GetPresCommitment, to ensure correct counter value on hw card.
          int credStatus = this.device.device.GetCreadentialStatus((byte)this.device.credID);
          if (credStatus == 0)
          {
            this.device.device.GetIssuanceCommitment((byte)this.device.credID);
          }
          else if (credStatus == 2)
          {
            this.device.device.GetPresCommitment(this.device.proverID, this.device.credID);
          }
        }
        catch (ErrorCode ex)
        {
          cOut.write("Counter do not have the correct value " + ex.Message);
          DebugUtils.DebugPrintErrorCodes(ex);
          this.device.device.EndCommitment();
          throw;
        }

        byte[] com = this.device.device.GetDeviceCommitment(true);
#if DEBUG
        this.comForDebug = com;
#endif
        GroupElement gElement = this.device.Gq.CreateGroupElement(com);
        this.device.device.EndCommitment();
        DebugUtils.DebugPrintEnd(com);
        return gElement;
      }

      GroupElement IDevicePresentationContext.GetInitialWitnessesAndPseudonym(byte[] scope, out GroupElement apPrime, out GroupElement Ps)
      {
        // return a_d, ap', and p_s from page 19 u-prove spec.
        // the scope-exclusive pseudonym derivation function seems to be different from what is used
        // in U-Prove, so if a smartcard-based pseudonym is requested, better to call it directly without using this interface.
        //this.device.ProofSession = this.device.device.BeginCommitment(this.device.proverID);
        if (this.device.credID != -1)
        {
          try
          {
            // call GetPresCommitment, to ensure correct counter value on hw card.

            int credStatus = this.device.device.GetCreadentialStatus((byte)this.device.credID);
            cOut.write("cred status : GetInitialWitnessesAndPseudonym " + credStatus);
            if (credStatus == 0)
            {
              this.device.device.GetIssuanceCommitment((byte)this.device.credID);
            }
            else if (credStatus == 2)
            {
              this.device.device.GetPresCommitment(this.device.proverID, this.device.credID);
            }
          }
          catch (ErrorCode ex)
          {
            cOut.write("Counter do not have the correct value " + ex.Message);
            DebugUtils.DebugPrintErrorCodes(ex);
            this.device.device.EndCommitment();
            throw;
          }
        }
      
        byte[] a_dBig = this.device.device.GetDeviceCommitment(true);
#if DEBUG
        this.comForDebug = a_dBig;
#endif
        byte[] apPrimeBig = this.device.device.GetScopeExclusiveCommitment(scope, true);
        byte[] psBig = this.device.device.GetScopeExclusivePseudonym(scope, true);
        
        this.device.device.EndCommitment();

        apPrime = this.device.Gq.CreateGroupElement(apPrimeBig);
        Ps = this.device.Gq.CreateGroupElement(psBig);
        
        return this.device.Gq.CreateGroupElement(a_dBig);
      }

      // TODO this should be GetPresentationResponse. But as this will not be changed in the uprove.dll we will keep the
      // name here as well.
      BigInteger IDevicePresentationContext.GetDeviceResponse(byte[] messageForDevice, byte[] partialChallengeDigest, string hashOID)
      {
        DebugUtils.DebugPrintBegin(null);
        if (this.device == null)
        {
          throw new DeviceException("Invalid context.");
        }

        // TODO: check that hashOID is consistant with ip.HashFunction
#if SILVERLIGHT
                string hashFunctionName;

                if (hashOID == "1.3.14.3.2.26")
                {
                    hashFunctionName = "sha1";
                }
                else if (hashOID == "2.16.840.1.101.3.4.2.1")
                {
                    hashFunctionName = "sha256";
                }
                else
                {
                    // Let the HashFunction creation fail
                    hashFunctionName = hashOID;
                }
#else
        Oid oid = new Oid(hashOID);
        string hashFunctionName = oid.FriendlyName ?? hashOID;
#endif
        BigInteger q = this.device.Gq.Q;
        
        
        byte[] input = new byte[1 + this.device.ProofSession.Length + partialChallengeDigest.Length];
        input[0] = 0x1;
        Buffer.BlockCopy(this.device.ProofSession, 0, input, 1, this.device.ProofSession.Length);
        Buffer.BlockCopy(partialChallengeDigest, 0, input, 1 + this.device.ProofSession.Length, partialChallengeDigest.Length);

        //call Response(c, "secret", kx), return as a BigInteger        
        this.device.device.BeginResponse(this.device.proverID, input);
        if (this.device.credID != -1)
        {
          try
          {
            // call GetPresCommitment, to ensure correct counter value on hw card.
            int credStatus = this.device.device.GetCreadentialStatus((byte)this.device.credID);
            if (credStatus == 1)
            {
              this.device.device.GetIssuanceResponse((byte)this.device.credID);
            }
          }
          catch (ErrorCode ex)
          {
            cOut.write(ex.Message);
            DebugUtils.DebugPrintErrorCodes(ex);
            this.device.device.EndCommitment();
            throw;
          }
        }
        byte[] response = this.device.device.GetDeviceResponse(true);

        BigInteger responseBigInteger = new BigInteger(1, response);
        
        return responseBigInteger;
      }

      public void Dispose()
      {
        this.device = null;
      }

#if DEBUG
      public byte[] comForDebug { get; set; }
#endif
    }

    public void Dispose()
    {
      this.device.Dispose();
    }
  }
}
