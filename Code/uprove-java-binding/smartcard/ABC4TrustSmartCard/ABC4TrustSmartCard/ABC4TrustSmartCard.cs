using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ABC4TrustSmartCard;
using PCSC;
using PCSC.Iso7816;

namespace ABC4TrustSmartCard
{
  [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1063:ImplementIDisposableCorrectly")]
  public class ABC4TrustSmartCard : IABC4TrustSmartCard, IDisposable
  {
    private string cardName;
    private IsoCard card;
    public SmartCardIO sIO { get; set; }
    private ProofStatus pStatus { get; set; }
    private byte[] proofSesson { get; set; }
    public bool doProfile {get; set;}
    public ParseConfigManager.ProfileInfo pInfo {get; set;}

    // ABC4Trust smartcard class application.
    private static byte CLA = 0xBC;


    public ABC4TrustSmartCard(String cardName)
    {
      this.cardName = cardName;
      sIO = new SmartCardIO();
      card = sIO.TryConnect(cardName);
      pStatus = ProofStatus.NOTSET;
      this.doProfile = ParseConfigManager.doTimeProfile();
      if (doProfile)
      {
        pInfo = ParseConfigManager.profileInfo;
      }

    }

    private bool getDataFromResponse(Response resp, out byte[] data)
    {
      List<byte> tmp = new List<byte>();
      foreach (ResponseApdu respApdu in resp.ResponseApduList)
      {
        tmp.AddRange(respApdu.GetData());
      }
      data = tmp.ToArray();
      return (tmp.Count != 0);
    }


    [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1063:ImplementIDisposableCorrectly")]
    public void Dispose()
    {
      this.sIO.Dispose();
    }

    public ErrorCode GetVersion(out String version)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case2Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x16; // Instruction: GET VERSION
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Le = 0x40;  // Expected length of the returned data
      DebugUtils.DebugPrintBegin(null);

      // Transmit the Command APDU to the card and receive the response
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      if (!err.IsOK)
      {
        version = "";
        return err;
      }
      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        List<char> d = data.Select(i => (char)(i & 0xFF)).ToList<char>();
        version = string.Join("", d).Trim();
        DebugUtils.DebugPrintEnd(data);
        return err;
        
      }
      version = "";
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode GetMode(out CardMode mode)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case2Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x02; // Instruction: GET MODE
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Le = 0x01;  // Expected length of the returned data
      DebugUtils.DebugPrintBegin( null);
      // Transmit the Command APDU to the card and receive the response
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      if (!err.IsOK)
      {
        mode = CardMode.UNKNOWN;
        return err;
      }
      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        mode = (CardMode)data[0];
        DebugUtils.DebugPrintEnd( data);
        return err;
        
      }
      mode = CardMode.UNKNOWN;
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode GetDeviceID(byte[] pin, out byte[] deviceID)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case2Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x14; // Instruction: GET DEVICE ID
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Le = 0x02;  // Expected length of the returned data
      DebugUtils.DebugPrintBegin( null);
      // Transmit the Command APDU to the card and receive the response
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      if (!err.IsOK)
      {
        deviceID = new byte[2];
        return err;
      }

      bool hasData = getDataFromResponse(resp, out deviceID);
      if (hasData)
      {
        DebugUtils.DebugPrintBegin(deviceID);
        return err;
      
      }
      deviceID = new byte[2];
      return new ErrorCode(0x90, 0x01);


    }

    public ErrorCode SetRootMode(byte[] accMode)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x04; // Instruction: SET ROOT MODE
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Data = accMode; //Data to send
      DebugUtils.DebugPrintBegin( accMode);
      Response resp = card.Transmit(apdu);
      DebugUtils.DebugPrintEnd( null);
      return new ErrorCode(resp.SW1, resp.SW2);
    }
    
    public ErrorCode SetWorkingMode()
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case1);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x06; // Instruction: SET WORKING MODE
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      DebugUtils.DebugPrintBegin( null);
      Response resp = card.Transmit(apdu);
      DebugUtils.DebugPrintEnd( null);
      return new ErrorCode(resp.SW1, resp.SW2);
    }

    public ErrorCode SetVirginMode(byte[] mac)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x08; // Instruction: SET VIRGIN MODE
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Data = mac; //Data to send
      DebugUtils.DebugPrintBegin( mac);
      Response resp = card.Transmit(apdu);
      DebugUtils.DebugPrintEnd( null);
      return new ErrorCode(resp.SW1, resp.SW2);
    }

    public ErrorCode GetChallenge(int size, out byte[] response)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x1C; // Instruction: GET CHALLENGE
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Le = 0x10; // length of reponse
      apdu.Data = new byte[1] { (byte)size };
      DebugUtils.DebugPrintBegin( apdu.Data);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      if (!err.IsOK)
      {
        response = new byte[1];
        return err;
      }

      bool hasData = getDataFromResponse(resp, out response);
      if (hasData)
      {
        DebugUtils.DebugPrintEnd(response);
        return err;
      }
      response = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode GetPinTrials(out int pinTrialsLeft)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case2Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x0A; // Instruction: GET PIN TRIALS LEFT
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Le = 0x01; // length of response
      DebugUtils.DebugPrintBegin( null);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      if (!err.IsOK)
      {
        pinTrialsLeft = -1;
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData) 
      {
        pinTrialsLeft = data[0];
        DebugUtils.DebugPrintEnd( data);
        return err;
      }
      pinTrialsLeft = -1;
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode GetPukTrials(out int pukTrialsLeft)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case2Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x0C; // Instruction: GET PUK TRIALS LEFT
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Le = 0x01; // length of response
      DebugUtils.DebugPrintBegin( null);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      if (!err.IsOK)
      {
        pukTrialsLeft = -1;
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
          pukTrialsLeft = data[0];
          DebugUtils.DebugPrintEnd( null);
          return err;
      }
      pukTrialsLeft = -1;
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode SetPin(byte[] oldPin, byte[] newPin)
    {
      // TODO need to check byte array length == 4 for old and new pin arrays
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x0E; // Instruction: SET PIN
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] data = oldPin.Concat(newPin).ToArray();
      apdu.Data = data;
      DebugUtils.DebugPrintBegin( data);
      Response resp = card.Transmit(apdu);
      DebugUtils.DebugPrintEnd( null);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      return err;
    }

    public ErrorCode ResetPin(byte[] puk, byte[] pin)
    {
      // TODO need to check byte array length == 8 for puk and length == 4 for pin
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x10; // Instruction: RESET PIN
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] data = puk.Concat(pin).ToArray();
      apdu.Data = data;
      DebugUtils.DebugPrintBegin( data);
      Response resp = card.Transmit(apdu);
      DebugUtils.DebugPrintEnd( null);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      return err;
    }

    public ErrorCode GetMemorySpace(byte[] pin, out byte[] memSpace)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x18; // Instruction: GET MEMORY SPACE
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Le = 0x02; // length of output
      apdu.Data = pin;
      DebugUtils.DebugPrintBegin( apdu.Data);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      if (!err.IsOK)
      {
        memSpace = new byte[2];
        return err;
      }

      bool hasData = getDataFromResponse(resp, out memSpace);
      if (hasData)
      {
          DebugUtils.DebugPrintEnd(memSpace);
          return err;
      }
      memSpace = new byte[2];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode StoreBlob(byte[] pin, byte[] uri)
    {
      // TODO check that pin == 4 long and uri max length 200
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x70; // Instruction: SET BLOB STORE
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] data = pin.Concat(uri).ToArray();
      apdu.Data = data;
      DebugUtils.DebugPrintBegin( apdu.Data);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode PutData(byte[] data)
    {
      // TODO check that data max 2048
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Extended);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x1A; // Instruction: PUT DATA
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Data = data;
      DebugUtils.DebugPrintBegin( apdu.Data);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode AuthData(byte keyID)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x1E; // Instruction: AUTHENTICATE DATA
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] data = new byte[] { keyID };
      apdu.Data = data;
      DebugUtils.DebugPrintBegin( apdu.Data);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode SetAuthKey(byte keyID)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x20; // Instruction: SET AUTHENTICATION KEY
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] data = new byte[] { keyID };
      apdu.Data = data;
      DebugUtils.DebugPrintBegin( apdu.Data);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode GetAuthKeys(byte[] pin, out byte[] keys)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x22; // Instruction: LIST AUTHENTICATION KEYS
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Data = pin;
      DebugUtils.DebugPrintBegin( apdu.Data);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        keys = new byte[3];
        return err;
      }

      bool hasData = getDataFromResponse(resp, out keys);
      if (hasData)
      {
          DebugUtils.DebugPrintEnd(keys);
          return err;
      }
      keys = new byte[3];
      return new ErrorCode(0x90, 0x01);    
    }

    public ErrorCode ReadAuthKey(byte[] pin, byte keyID, out byte[] key)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x24; // Instruction: READ AUTHENTICATION KEY
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = pin.Concat(new byte[] {keyID}).ToArray();
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        key = new byte[55];
        return err;
      }


      bool hasData = getDataFromResponse(resp, out key);
      if (hasData)
      {
        DebugUtils.DebugPrintEnd(key);
        return err;
       
      }
      key = new byte[55];
      return new ErrorCode(0x90, 0x01);    
    }

    public ErrorCode RemoveAuthKey(byte keyID)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x26; // Instruction: REMOVE AUTHENTICATION KEY
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] data = new byte[] { keyID };
      apdu.Data = data;
      DebugUtils.DebugPrintBegin( apdu.Data);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode InitDevice(byte[] someID, byte[] size, out byte[] response)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x12; // Instruction: INITIALIZE DEVICE
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      //apdu.Le = 0;
      byte[] opData = new byte[] { someID[0], someID[1], size[0], size[1] };
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        response = new byte[1];
        return err;
      }

      bool hasData = getDataFromResponse(resp, out response);
      if (hasData)
      {
        DebugUtils.DebugPrintEnd(response);
        return err;
      }
      
      response = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }
  
    public ErrorCode SetGroupComponent(byte groupID, int comptype)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x28; // Instruction: SET GROUP COMPONENT
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] data = new byte[] { groupID, (byte)comptype};
      apdu.Data = data;
      DebugUtils.DebugPrintBegin( apdu.Data);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode SetGenerator(byte groupID, int genID)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x2A; // Instruction: SET GENERATOR
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] data = new byte[] { groupID, (byte)genID };
      apdu.Data = data;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode ListGroups(byte[] pin, out byte[] groupIDs)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x2C; // Instruction: LIST GROUPS
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Data = pin;
      DebugUtils.DebugPrintBegin( apdu.Data);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      if (!err.IsOK)
      {
        groupIDs = new byte[1];
        return err;
      }

      bool hasData = getDataFromResponse(resp, out groupIDs);
      if (hasData)
      {
        DebugUtils.DebugPrintEnd(groupIDs);
        return err;
      }
      groupIDs = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode ReadGroup(byte[] pin, byte groupID, out byte[] sizeOfM,
                        out byte[] sizeOfQ, out byte[] sizeOfF, out int t)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x2E; // Instruction: READ GROUP
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = pin.Concat(new byte[] {groupID}).ToArray();
      apdu.Data = opData;
      apdu.Le = 0x07;
      DebugUtils.DebugPrintBegin( apdu.Data);
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      sizeOfM = new byte[2];
      sizeOfQ = new byte[2];
      sizeOfF = new byte[2];
      t = 0;
      if (!err.IsOK)
      {
        return err;
      }
      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        Buffer.BlockCopy(data, 0, sizeOfM, 0, 2);
        Buffer.BlockCopy(data, 2, sizeOfQ, 0, 2);
        Buffer.BlockCopy(data, 4, sizeOfF, 0, 2);
        t = (int)data[6];
        DebugUtils.DebugPrintBegin(data);
        return err;
      }
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode ReadGroupComponent(byte[] pin, byte groupID, int comptype, out byte[] groupComp)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x30; // Instruction: READ GROUP COMPOENENT
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[6] { pin[0], pin[1], pin[2], pin[3], groupID, (byte)comptype };
      apdu.Data = opData;
      //apdu.Le = 0x06;
      DebugUtils.DebugPrintEnd( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      
      if (!err.IsOK)
      {
        groupComp = new byte[1];
        return err;
      }

      bool hasData = getDataFromResponse(resp, out groupComp);
      if (hasData)
      {
        DebugUtils.DebugPrintEnd(groupComp);
        return err;
      
      }
      groupComp = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode ReadGenerator(byte[] pin, byte groupID, int genID, out byte[] generator)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x32; // Instruction: READ GENERATOR
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[6] { pin[0], pin[1], pin[2], pin[3], groupID, (byte)genID };
      apdu.Data = opData;
      apdu.Le = 0x06;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        generator= new byte[1];
        return err;
      }

      bool hasData = getDataFromResponse(resp, out generator);
      if (hasData)
      {
        DebugUtils.DebugPrintEnd(generator);
        return err;
      }
      generator = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode RemoveGroup(byte groupID)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x34; // Instruction: REMOVE GROUP
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] data = new byte[] { groupID };
      apdu.Data = data;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode SetCounter(byte counterID, byte keyID, int index, int threshold, byte[] cursor)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x36; // Instruction: SET COUNTER
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[8];
      opData[0] = counterID;
      opData[1] = keyID;
      opData[2] = (byte)index;
      opData[3] = (byte)threshold;
      Buffer.BlockCopy(cursor, 0, opData, 4, cursor.Length);
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode IncCounter(byte keyID, byte[] sig)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Extended);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x38; // Instruction: INCREMENT COUNTER
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[sig.Length + 1];
      opData[0] = keyID;
      Buffer.BlockCopy(sig, 0, opData, 1, sig.Length);
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode ListCounters(byte[] pin, out byte[] counters)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x3A; // Instruction: LIST COUNTERS
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Data = pin;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      if (!err.IsOK)
      {
        counters = new byte[1];
        return err;
      }


      bool hasData = getDataFromResponse(resp, out counters);
      if (hasData)
      {
        DebugUtils.DebugPrintEnd(counters);
        return err;
      }
      counters = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode ReadCounter(byte[] pin, byte counterID, out byte keyID,
                                 out int index, out int threshold, byte[] cursor)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x3C; // Instruction: READ COUNTER
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[5];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      opData[4] = counterID;
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);
      
      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      keyID = 0x00;
      index = 0;
      threshold = 0;
      cursor = new byte[4];
      if (!err.IsOK)
      {
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        keyID = data[0];
        index = data[1];
        threshold = data[2];
        Buffer.BlockCopy(data, 3, cursor, 0, 4);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode RemoveCounter(byte counterID)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x3C; // Instruction: REMOVE COUNTER
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[] { counterID };
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode SetIssuer(byte issuerID, byte groupID, byte genID1, byte genID2, byte numpres, byte counterID)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x40; // Instruction: SET ISSUER
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[] { issuerID, groupID, genID1, genID2, numpres, counterID };
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode ListIssuers(byte[] pin, out byte[] issuers)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x42; // Instruction: LIST ISSUERS
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Data = pin;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        issuers = new byte[1];
        return err;
      }

      bool hasData = getDataFromResponse(resp, out issuers);
      if (hasData)
      {
        DebugUtils.DebugPrintEnd(issuers);
        return err;
      }
      issuers = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode ReadIssuer(byte[] pin, byte issuerID, out byte groupID, out byte genID1,
                                out byte genID2, out byte numpres, out byte counterID)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x44; // Instruction: READ ISSUER
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[5];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      opData[4] = issuerID;
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      groupID = 0x00;
      genID1 = 0x00;
      genID2 = 0x00;
      numpres = 0x00;
      counterID = 0x00;

      if (!err.IsOK)
      {
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        groupID = data[0];
        genID1 = data[1];
        genID2 = data[2];
        numpres = data[3];
        counterID = data[4];
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode RemoveIssuer(byte issuerID)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x46; // Instruction: REMOVE ISSUER
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[] { issuerID };
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode SetProver(byte proverID, byte[] kSize, byte[] cSize, byte[] credIds)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x48; // Instruction: SET PROVER
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[1 + 2 + 2 + credIds.Length];
      opData[0] = proverID;
      Buffer.BlockCopy(kSize, 0, opData, 1, 2);
      Buffer.BlockCopy(cSize, 0, opData, 3, 2);
      Buffer.BlockCopy(credIds, 0, opData, 5, credIds.Length);
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode ReadProver(byte[] pin, byte proverID, out byte[] kSize, out byte[] cSize,
                                out byte[] proofSession, out int proofStatus, out byte[] credIds)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x4A; // Instruction: READ PROVER
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[5];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      opData[4] = proverID;
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      kSize = new byte[2];
      cSize = new byte[2];
      proofSession = new byte[16];
      proofStatus = 0;
      credIds = new byte[1]; // must be resized before assigned data

      if (!err.IsOK)
      {
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        Buffer.BlockCopy(data, 0, kSize, 0, 2);
        Buffer.BlockCopy(data, 1, cSize, 0, 2);
        Buffer.BlockCopy(data, 3, proofSession, 0, 16);
        proofStatus = data[19];
        int len = data.Length - 21;
        credIds = new byte[len];
        Buffer.BlockCopy(data, 20, credIds, 0, len);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      return new ErrorCode(0x90, 0x01);
    }
    
    public ErrorCode RemoveProver(byte proverID)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x4C; // Instruction: REMOVE PROVER
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[] { proverID };
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }


    public ErrorCode BeginCommitments(byte[] pin, byte proverID, out byte[] outProofSession)
    {
      if (pStatus == ProofStatus.RESPONSE)
      {
        throw new System.Exception(String.Format("End current proof session first. ProofStatus : {0}", pStatus));
      } 
      else if (pStatus == ProofStatus.COMMITMNET)
      {
        // continue with this session.
        outProofSession = new byte[1]; 
        return new ErrorCode(0x90, 0x00);
      }
      
      byte[] pSession;
      ErrorCode err = BeginCommitmentsImpl(pin, proverID, out pSession);
      if (!err.IsOK)
      {
        err.Command = "BeginCommitments";
        throw err;
      }
      if (this.proofSesson == null)
      {
        this.proofSesson = new byte[pSession.Length];
      }
      Buffer.BlockCopy(pSession, 0, this.proofSesson, 0, pSession.Length);
      pStatus = ProofStatus.COMMITMNET;
      outProofSession = new byte[pSession.Length];
      Buffer.BlockCopy(pSession, 0, outProofSession, 0, pSession.Length);
      return new ErrorCode(err.SW1, err.SW2);
    }

    public void EndCommitments()
    {
      if (pStatus == ProofStatus.RESPONSE)
      {
        throw new System.Exception(String.Format("End current proof session first. ProofStatus : {0}", pStatus));
      }
      pStatus = ProofStatus.NOTSET;
      this.proofSesson = null;
    }

    private ErrorCode BeginCommitmentsImpl(byte[] pin, byte proverID, out byte[] proofSession)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x4E; // Instruction: START COMMITMENTS
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[5];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      opData[4] = proverID;
      apdu.Data = opData;
      apdu.Le = 0x10;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      proofSession = new byte[16];

      if (!err.IsOK)
      {
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        Buffer.BlockCopy(data, 0, proofSession, 0, data.Length);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      return err;
    }

    public ErrorCode BeginResponse(byte[] pin, byte proverID, byte[] input)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Extended);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x50; // Instruction: START RESPONSE
      apdu.P1 = 0x01;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[4 + 1 + input.Length];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      opData[pin.Length] = proverID;
      Buffer.BlockCopy(input, 0, opData, pin.Length + 1, input.Length);
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode SetCredential(byte[] pin, byte credID, byte issuerID)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Extended);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x52; // Instruction: SET CREDENTIAL
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[4 + 1 + 1];
      Buffer.BlockCopy(pin, 0, opData, 0, 4);
      opData[3] = credID;
      opData[4] = issuerID;
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode ListCredentials(byte[] pin, out byte[] credIDs)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x54; // Instruction: LIST CREDENTIALS
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Data = pin;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        credIDs = new byte[1];
        return err;
      }

      bool hasData = getDataFromResponse(resp, out credIDs);
      if (hasData)
      {
        DebugUtils.DebugPrintEnd(credIDs);
        return err;
      }
      credIDs = new byte[1];
      return err;
    }

    public ErrorCode ReadCredential(byte[] pin, byte credID, out byte issuerID, out byte[] vSize, out byte[] kSize, out int status, out byte prescount)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x56; // Instruction: READ CREDENTIAL
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[5];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      opData[4] = credID;
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      issuerID = 0x00;
      vSize = new byte[2];
      kSize = new byte[2];
      status = 0;
      prescount = 0x00;
      

      if (!err.IsOK)
      {
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        issuerID = data[0];
        Buffer.BlockCopy(data, 1, vSize, 0, 2);
        Buffer.BlockCopy(data, 3, kSize, 0, 2);
        status = (int)data[5];
        prescount = data[6];
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode RemoveCredential(byte[] pin, byte credID)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x58; // Instruction: REMOVE CREDENTIAL
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[5];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      opData[4] = credID;
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

    public ErrorCode GetCredentialPublicKey(byte[] pin, byte credID, out byte[] pubKey)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x5A; // Instruction: GET CREDENTIAL PUBLIC KEY
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[5];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      opData[4] = credID;
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin(apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
   
      if (!err.IsOK)
      {
        pubKey = new byte[1];
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        pubKey = new byte[data.Length];
        Buffer.BlockCopy(data, 0, pubKey, 0, data.Length);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      pubKey = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode GetIssuanceCommitment(byte[] pin, byte credID, out byte[] C)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x5C; // Instruction: GET ISSUANCE COMMITMENT
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[5];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      opData[4] = credID;
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        C = new byte[1];
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        C = new byte[data.Length];
        Buffer.BlockCopy(data, 0, C, 0, data.Length);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      C = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode GetIssuanceResponse(byte[] pin, byte credID, out byte[] R)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x5C; // Instruction: GET ISSUANCE COMMITMENT
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[5];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      opData[4] = credID;
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        R = new byte[1];
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        R = new byte[data.Length];
        Buffer.BlockCopy(data, 0, R, 0, data.Length);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      R = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode GetPresentationCommitment(byte[] pin, byte credID, out byte[] C)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x60; // Instruction: GET PRESENTATION COMMITMENT
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[5];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      opData[4] = credID;
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        C = new byte[1];
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
          C = new byte[data.Length];
          Buffer.BlockCopy(data, 0, C, 0, data.Length);
          DebugUtils.DebugPrintEnd( data);
          return err;
      }
      
      C = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode GetPresentationResponse(byte[] pin, byte credID, out byte[] R)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x62; // Instruction: GET PRESENTATION RESPONSE
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[5];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      opData[4] = credID;
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        R = new byte[1];
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        R = new byte[data.Length];
        Buffer.BlockCopy(data, 0, R, 0, data.Length);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      R = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode GetDevicePublicKey(byte[] pin, out byte[] pubKey)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x64; // Instruction: GET DEVICE PUBLIC KEY
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[4];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin(apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        pubKey = new byte[1];
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        pubKey = new byte[data.Length];
        Buffer.BlockCopy(data, 0, pubKey, 0, data.Length);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      pubKey = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

  
    public ErrorCode GetDeviceCommitment(byte[] pin, out byte[] C)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x66; // Instruction: GET DEVICE COMMITMENT
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Data = pin;
      DebugUtils.DebugPrintBegin(apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        C = new byte[1];
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        C = new byte[data.Length];
        Buffer.BlockCopy(data, 0, C, 0, data.Length);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      C = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }


    public ErrorCode GetDeviceResponse(byte[] pin, out byte[] R)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x68; // Instruction: GET DEVICE REPONSE
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      apdu.Data = pin;
      DebugUtils.DebugPrintBegin(apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        R = new byte[1];
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        R = new byte[data.Length];
        Buffer.BlockCopy(data, 0, R, 0, data.Length);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      R = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    
    public ErrorCode GetScopeExlusivePseudonym(byte[] pin, byte[] scope, out byte[] pseudonym)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x6A; // Instruction: GET SCOPE-EXCLUSIVE PSEUDONYM
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[pin.Length + scope.Length];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      Buffer.BlockCopy(scope, 0, opData, 4, scope.Length);
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin(apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        pseudonym = new byte[1];
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        pseudonym = new byte[data.Length];
        Buffer.BlockCopy(data, 0, pseudonym, 0, data.Length);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      pseudonym = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode GetScopeExlusiveCommitment(byte[] pin, byte[] scope, out byte[] C)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x6C; // Instruction: GET SCOPE-EXCLUSIVE COMMITMENT
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[pin.Length + scope.Length];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      Buffer.BlockCopy(scope, 0, opData, 4, scope.Length);
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin(apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        C = new byte[1];
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        C = new byte[data.Length];
        Buffer.BlockCopy(data, 0, C, 0, data.Length);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      C = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }


    public ErrorCode GetScopeExlusiveResponse(byte[] pin, byte[] scope, out byte[] R)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x6E; // Instruction: GET SCOPE-EXCLUSIVE RESPONSE
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[pin.Length + scope.Length];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      Buffer.BlockCopy(scope, 0, opData, 4, scope.Length);
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        R = new byte[1];
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        R = new byte[data.Length];
        Buffer.BlockCopy(data, 0, R, 0, data.Length);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      R = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode ReadBlob(byte[] pin, byte[] uri, out byte[] blob)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case4Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x74; // Instruction: READ BLOB
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[pin.Length + uri.Length];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      Buffer.BlockCopy(uri, 0, opData, 4, uri.Length);
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);

      if (!err.IsOK)
      {
        blob = new byte[1];
        return err;
      }

      byte[] data;
      bool hasData = getDataFromResponse(resp, out data);
      if (hasData)
      {
        blob = new byte[data.Length];
        Buffer.BlockCopy(data, 0, blob, 0, data.Length);
        DebugUtils.DebugPrintEnd(data);
        return err;
      }
      blob = new byte[1];
      return new ErrorCode(0x90, 0x01);
    }

    public ErrorCode RemoveBlob(byte[] pin, byte[] uri)
    {
      CommandApdu apdu = card.ConstructCommandApdu(IsoCase.Case3Short);
      apdu.CLA = CLA; // Class
      apdu.INS = 0x76; // Instruction: REMOVE BLOB
      apdu.P1 = 0x00;  // Parameter 1
      apdu.P2 = 0x00;  // Parameter 2
      byte[] opData = new byte[pin.Length + uri.Length];
      Buffer.BlockCopy(pin, 0, opData, 0, pin.Length);
      Buffer.BlockCopy(uri, 0, opData, 4, uri.Length);
      apdu.Data = opData;
      DebugUtils.DebugPrintBegin( apdu.Data);

      Response resp = card.Transmit(apdu);
      ErrorCode err = new ErrorCode(resp.SW1, resp.SW2);
      DebugUtils.DebugPrintEnd( null);
      return err;
    }

  }
}
