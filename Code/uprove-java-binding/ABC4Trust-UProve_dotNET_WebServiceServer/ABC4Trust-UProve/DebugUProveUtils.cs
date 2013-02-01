using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ABC4TrustSmartCard;
using UProveCrypto;

namespace abc4trust_uprove
{
  internal static class DebugUProveUtils
  {
    public static void VerifySmartCard(SmartCardDevice smartCardDevice, byte[] com, byte[] response, string hashFunctionName, byte[] proofSession, byte[] challangeMgs)
    {
      BigInteger resp = new BigInteger(1, response);
      //BigInteger comBig = new BigInteger(1, com);
      HashFunction hash = new HashFunction(hashFunctionName);
      //hash.Hash(new byte[1] {0x1});
      //hash.Hash(1);
      byte[] proofSessionFoo = new byte[1 + proofSession.Length];
      proofSessionFoo[0] = 1;
      Buffer.BlockCopy(proofSession, 0, proofSessionFoo, 1, proofSession.Length);
      hash.Hash(proofSessionFoo);
      hash.Hash(challangeMgs);
      byte[] cByte = hash.Digest;
      BigInteger c = new BigInteger(1, cByte);
      byte[] devicePubKeyByte = smartCardDevice.Device.GetDevicePublicKey(true);
      //BigInteger devicePubKey = new BigInteger(1, devicePubKeyByte);
      SubgroupGroupDescription subGq = (SubgroupGroupDescription)smartCardDevice.getGroupDescription();
      SubgroupGroupElement leftSide = (SubgroupGroupElement)smartCardDevice.getGroupElement().Exponentiate(resp);
      SubgroupGroupElement pk = (SubgroupGroupElement)subGq.CreateGroupElement(devicePubKeyByte);
      SubgroupGroupElement pkc = (SubgroupGroupElement)pk.Exponentiate(c.Negate());
      SubgroupGroupElement rightSide = (SubgroupGroupElement)pkc.Multiply(smartCardDevice.getGroupDescription().CreateGroupElement(com));

      Console.Out.WriteLine("Printing left and right side");
      Utils.PrintByteArrayToConsole(leftSide.GetEncoded());
      Utils.PrintByteArrayToConsole(rightSide.GetEncoded());
      
    }
  }
}
