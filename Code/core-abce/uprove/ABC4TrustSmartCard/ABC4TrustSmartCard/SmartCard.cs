using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using PCSC;
using System.Numerics;

namespace ABC4TrustSmartCard
{
  sealed public class SmartCardTransaction : IDisposable
  {
    private ABC4TrustSmartCard device;
    private SCardReaderDisposition disMethod;
    private AbcTimer timer;
    private Log profileLogger;

    private double getTime(TimeSpan t)
    {
      if (device.pInfo.timeAs == TimeProfileElement.timeunit.miliseconds)
      {
        return t.TotalMilliseconds;
      }
      else
      {
        return t.TotalSeconds;
      }
    }

    public SmartCardTransaction(ABC4TrustSmartCard device, SCardReaderDisposition disMethod = SCardReaderDisposition.Leave)
    {
      this.device = device;
      this.disMethod = disMethod;
      int noRetry = 10;
      int i = 0;
      if (device.doProfile)
      {
        profileLogger = Logger.Instance.getLog(device.pInfo.loggerToUse);
        timer = new AbcTimer();
        timer.Start();
      }

      while (TryTransaction() != 0)
      {
        if (i < noRetry)
        {
          System.Threading.Thread.Sleep(1000);
        }
        else
        {
          // TODO find a better exception.
          throw new Exception("Could not get exclusive lock on card");
        }
        ++i;
      } 
    }

    private SCardError TryTransaction()
    {
      SCardError transError = this.device.sIO.GetReader().BeginTransaction();
      return transError;
    }


    public void Dispose()
    {
      this.device.sIO.GetReader().EndTransaction(disMethod);
      
      if (device.doProfile)
      {
        timer.Stop();
        double t = getTime(timer.getElapsed());
        System.Diagnostics.StackTrace st = new System.Diagnostics.StackTrace(new System.Diagnostics.StackFrame(1));
        string methodName = st.GetFrame(0).GetMethod().Name;
        profileLogger.write("--> Hardware smartcard method '{0}' was running for '{1}' {2}", methodName, Math.Round(t), device.pInfo.timeAs.ToString());
        timer.Dispose();
      }
      
    }
  }

  [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1063:ImplementIDisposableCorrectly")]
  public class SmartCard : ISmartCard, IDisposable
  {
    private String Username = "abc4trust";
    private String Password = "lite";

    private byte[] pin;
    public ABC4TrustSmartCard device { get; set; }
    //private string readerName;

    [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1063:ImplementIDisposableCorrectly")]
    public void Dispose()
    {
      device.Dispose();
    }

    public SmartCard(String readerName, byte[] pin)
    {
      this.device = new ABC4TrustSmartCard(readerName);
      this.pin = pin;
    }

    public SmartCard(String readerName, string pin, bool doProfile = false)
    {
      this.device = new ABC4TrustSmartCard(readerName);
      this.pin = fixPin(pin); // (Encoding.ASCII.GetBytes(pin);
    }

    private byte[] fixPin(string pin) 
    {
        string tmp = pin.PadLeft(4, '0');
        return Encoding.ASCII.GetBytes(tmp);
    }


    // NEVER USED!!!
    public String InitDevice(KeyPair keyPair, String pin)
    {
      byte[] pk = keyPair.N.ToByteArray();
      Array.Reverse(pk, 0, pk.Length);
      ErrorCode errPutData = device.PutData(pk);
      if (!errPutData.IsOK)
      {
        errPutData.Command = "PutData";
        DebugUtils.DebugPrintErrorCodes(errPutData);
        throw errPutData;
      }
      ErrorCode errSetAuth = device.SetAuthKey(0x00);
      if (!errSetAuth.IsOK)
      {
        errSetAuth.Command = "SetAuthKey";
        DebugUtils.DebugPrintErrorCodes(errSetAuth);
        throw errSetAuth;
      }
      byte[] rawData;
      ErrorCode errInitDevice = device.InitDevice(new byte[] { 0x00, 0x01}, new byte[] {0x00, 0x20 }, out rawData);
      if (!errInitDevice.IsOK)
      {
        errInitDevice.Command = "InitDevice";
        DebugUtils.DebugPrintErrorCodes(errInitDevice);
        throw errInitDevice;
      }
      Array.Reverse(rawData, 0, rawData.Length);
      byte[] decryptData = SmartCardCrypto.Decrypt(keyPair, rawData);
      byte[] initPin = new byte[4];
      byte[] initPuk = new byte[8];
      Buffer.BlockCopy(decryptData, 0, initPin, 0, 4);
      Buffer.BlockCopy(decryptData, 4, initPuk, 0, 8);

      byte[] pinA = fixPin(pin); // Encoding.ASCII.GetBytes(pin);
      ErrorCode errSetPin = device.SetPin(initPin, pinA);
      if (!errSetPin.IsOK)
      {
        errSetPin.Command = "SetPin";
        DebugUtils.DebugPrintErrorCodes(errSetPin);
        throw errSetPin;
      }
      return BitConverter.ToString(initPuk);
    }

    public void ResetDevice()
    {
      
      DebugUtils.DebugPrintBegin(null);
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device, SCardReaderDisposition.Leave))
      {
        // do nothing.
      }
      DebugUtils.DebugPrintEnd(null);
    }
    public void SetCardInRootMode()
    {
      
      /* byte[] accMac = new byte[] { 0xE6, 0x63, 0x6D, 0x90, 0x49, 0x84, 0x02, 0xDF }; */
      byte[] accMac = new byte[] { 0xDD, 0xE8, 0x90, 0x96, 0x3E, 0xF8, 0x09, 0x0E };
      /*
       * Old root access macs:
       * { 0xE6, 0x63, 0x6D, 0x90, 0x49, 0x84, 0x02, 0xDF };
       */
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        device.SetRootMode(accMac);
      }
    }

    public void SetCardInWorkingMode()
    {
      device.SetWorkingMode();
    }

    public CardMode GetCardMode()
    {
      CardMode mode;
      device.GetMode(out mode);
      return mode;
    }

    public void SetVirginMode()
    {
      byte[] cha;
      ErrorCode chaError = this.device.GetChallenge(16, out cha);
      if (!chaError.IsOK)
      {
        chaError.Command = "GetChallenge";
        DebugUtils.DebugPrintErrorCodes(chaError);
        throw chaError;
      }

      // Format challange value and get mac from server via http
      String webUrl = String.Format("http://www.cryptoexperts.com/abc4trustlite/hasher.php?key={0}", BitConverter.ToString(cha).Replace("-", ""));
      WebRequest request = WebRequest.Create(webUrl);
      NetworkCredential networkCredential = new NetworkCredential(this.Username, this.Password);
      request.Credentials = networkCredential;
      HttpWebResponse  response = (HttpWebResponse)request.GetResponse();

      if (response.StatusCode != HttpStatusCode.OK) {
        throw new System.Exception(String.Format("Error while talking to cryptoexperts. : {0}", response.StatusCode));
      }

      Stream dataStream = response.GetResponseStream();
      StreamReader reader = new StreamReader(dataStream);
      
      string responseFromServer = reader.ReadToEnd().Split(' ')[1];

      // format the response string as a byte array
      byte[] mac = Utils.GetBytesFromString(responseFromServer);
      ErrorCode errVirgin = this.device.SetVirginMode(mac);
      if (!errVirgin.IsOK)
      {
        errVirgin.Command = "SetVirginMode";
        DebugUtils.DebugPrintErrorCodes(errVirgin);
        throw errVirgin;
      }
    }

    public void SetGroupOfUnknownOrder(BigInteger modulus)
    {
      byte[] mod = modulus.ToByteArray();
      // need to convert it to big-endian.
      Array.Reverse(mod, 0, mod.Length);
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        ErrorCode err = this.device.PutData(mod);
        if (!err.IsOK)
        {
          err.Command = "PutData";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
        err = this.device.SetGroupComponent(0x00, 0);
        if (!err.IsOK)
        {
          err.Command = "SetGroupComponent";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
      } 
    }

    public bool IsGeneratorSet(int groupID)
    {
      byte gID = (byte)groupID;
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        byte[] sizeOfM;
        byte[] sizeOfQ;
        byte[] sizeOfF;
        int t;
        ErrorCode err = this.device.ReadGroup(this.pin, gID, out sizeOfM, out sizeOfQ, out sizeOfF, out t);
        if (!err.IsOK)
        {
          err.Command = "ReadGroup";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
        return t > 0;
      }
    }

    public byte[] GetCredentialPublicKey(int credID, bool raw = false)
    {
      byte cID = (byte)credID;
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        byte[] pubKey;
        ErrorCode err = this.device.GetCredentialPublicKey(pin, cID, out pubKey);
        if (!err.IsOK)
        {
          err.Command = "GetCredentialPublicKey";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
        if (!raw)
        {
          Array.Reverse(pubKey, 0, pubKey.Length);
        }
        return pubKey;
      }
    }

    public byte[] GetPresCommitment(int proverID, int credID, bool raw = false)
    {
      byte pID = (byte)proverID;
      byte cID = (byte)credID;
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        byte[] C;
        ErrorCode err = this.device.GetPresentationCommitment(pin, cID, out C);
        if (!err.IsOK)
        {
          err.Command = "GetPresentationCommitment";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
        if (!raw)
        {
          Array.Reverse(C, 0, C.Length);
        }
        return C;
      }
    }

    public byte[] BeginCommitment(int proverID)
    {
      byte pID = (byte)proverID;
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        byte[] pSession;
        this.device.BeginCommitments(pin, pID, out pSession);
        return pSession;
      }
    }

    public void EndCommitment()
    {
      this.device.EndCommitments();
    }

    public byte[] GetDeviceCommitment(bool raw = false)
    {
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        byte[] C;
        ErrorCode err = this.device.GetDeviceCommitment(pin, out C);
        if (!err.IsOK)
        {
          err.Command = "GetPresentationCommitment";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
        if (!raw)
        {
          byte[] data = new byte[C.Length + 1];
          byte[] tmpData = C;
          // ensure that we add 0x00 to data to force it to be signed.
          data[0] = 0x00;
          Buffer.BlockCopy(tmpData, 0, data, 1, tmpData.Length);
          Array.Reverse(data, 0, data.Length);
          return data;  
        }
        else
        {
          return C;
        }
      }

    }

    public void BeginResponse(int proverID, byte[] input)
    {
      byte pID = (byte)proverID;
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        ErrorCode err = this.device.BeginResponse(pin, pID, input);
        if (!err.IsOK)
        {
          err.Command = "BeginResponse";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
      }
    }


    // TODO fix name and call sites of this.
    public byte[] GetPresResponse(int credID)
    {
        byte cID = (byte)credID;
        byte[] R;
        ErrorCode err = this.device.GetPresentationResponse(pin, cID, out R);
        if (!err.IsOK)
        {
          err.Command = "GetPresentationResponse";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
        return R;
      }


    public byte[] GetDeviceResponse(bool raw = false)
    {
      
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        byte[] R;
        ErrorCode err = this.device.GetDeviceResponse(this.pin, out R);
        if (!err.IsOK)
        {
          err.Command = "GetDeviceResponse";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
        if (!raw)
        {
          Array.Reverse(R, 0, R.Length);
        }
        return R;
      }
    }


    public byte[] GetScopeExclusiveCommitment(byte[] scopeURI, bool raw = false)
    {
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        byte[] C;
        ErrorCode err = this.device.GetScopeExlusiveCommitment(this.pin, scopeURI, out C);

        if (!err.IsOK)
        {
          err.Command = "GetScopeExlusiveCommitment";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
        if (!raw)
        {
          byte[] data = new byte[C.Length + 1];
          byte[] tmpData = C;
          // ensure that we add 0x00 to data to force it to be signed.
          data[0] = 0x00;
          Buffer.BlockCopy(tmpData, 0, data, 1, tmpData.Length);
          Array.Reverse(data, 0, data.Length);
          return data;
        }
        else
        {
          return C;
        }
      }
    }

    public byte[] GetScopeExclusivePseudonym(byte[] scopeURI, bool raw = false)
    {
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        byte[] C;
        ErrorCode err = this.device.GetScopeExlusivePseudonym(this.pin, scopeURI, out C);

        if (!err.IsOK)
        {
          err.Command = "GetScopeExlusivePseudonym";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
        if (!raw)
        {
          byte[] data = new byte[C.Length + 1];
          byte[] tmpData = C;
          // ensure that we add 0x00 to data to force it to be signed.
          data[0] = 0x00;
          Buffer.BlockCopy(tmpData, 0, data, 1, tmpData.Length);
          Array.Reverse(data, 0, data.Length);
          return data;
        }
        else
        {
          return C;
        }
      }
    }

    public byte[] GetDevicePublicKey(bool raw = false)
    {
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        byte[] pubKey;
        ErrorCode err = this.device.GetDevicePublicKey(this.pin, out pubKey);

        if (!err.IsOK)
        {
          err.Command = "GetDevicePublicKey";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }

        if (!raw)
        {
          byte[] data = new byte[pubKey.Length + 1];
          byte[] tmpData = pubKey;
          // ensure that we add 0x00 to data to force it to be signed.
          data[0] = 0x00;
          Buffer.BlockCopy(tmpData, 0, data, 1, tmpData.Length);
          //Data from the card is in big-endian. need to reverse it.
          Array.Reverse(data, 0, data.Length);
          return data;
        }
        else
        {
          return pubKey;
        }
      }
    }

    public void GetIssuanceCommitment(byte credID)
    {
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        byte[] c;
        ErrorCode err = this.device.GetIssuanceCommitment(this.pin, credID, out c);

        if (!err.IsOK)
        {
          err.Command = "GetIssuanceCommitment";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
      }



    }
    
    
    public void GetIssuanceResponse(byte credID) {

      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        byte[] c;
        ErrorCode err = this.device.GetIssuanceResponse(this.pin, credID, out c);

        if (!err.IsOK)
        {
          err.Command = "GetIssuanceResponse";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
      }
    }

    public byte[] ReadGroupComponent(int groupID, int compType, bool raw = false)
    {
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        byte[] c;
        ErrorCode err = this.device.ReadGroupComponent(this.pin, (byte)groupID, compType, out c);

        if (!err.IsOK)
        {
          err.Command = "ReadGroupComponent";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }

        if (!raw)
        {
          byte[] data = new byte[c.Length + 1];
          byte[] tmpData = c;
          // ensure that we add 0x00 to data to force it to be signed.
          data[0] = 0x00;
          Buffer.BlockCopy(tmpData, 0, data, 1, tmpData.Length);
          //Data from the card is in big-endian. need to reverse it.
          Array.Reverse(data, 0, data.Length);
          return data;
        }
        return c;

      }
    }


    public int GetCreadentialStatus(byte credID)
    {
      using (SmartCardTransaction scT = new SmartCardTransaction(this.device))
      {
        byte issuerID;
        byte[] vSize;
        byte[] kSize;
        int status;
        byte prescount;
        ErrorCode err = this.device.ReadCredential(this.pin, credID, out issuerID, out vSize, out kSize, out status, out prescount);

        if (!err.IsOK)
        {
          err.Command = "ReadCredential";
          DebugUtils.DebugPrintErrorCodes(err);
          throw err;
        }
        return status;
      }

    }



  }
}
