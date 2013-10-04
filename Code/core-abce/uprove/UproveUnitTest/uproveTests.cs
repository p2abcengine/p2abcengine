using System;
using Xunit;
using UProveCrypto;

namespace UProve_ABC4Trust_unitTest
{

  public class uproveTests
  {

    private static String Hash_1_3_6_1_4_1_311_75_1_1_1 = "1.3.6.1.4.1.311.75.1.1.1";
    private static String Hash_1_3_6_1_4_1_311_75_1_1_0 = "1.3.6.1.4.1.311.75.1.1.0";
    private static String Hash_1_3_6_1_4_1_311_75_1_1_2 = "1.3.6.1.4.1.311.75.1.1.2";
    private static System.Text.UTF8Encoding encoding = new System.Text.UTF8Encoding();

    private ParameterSet getRecommendedSet(int securityLevel)
    {
      string recommendedParameters = null;
      switch (securityLevel)
      {
        case 1024:
          recommendedParameters = Hash_1_3_6_1_4_1_311_75_1_1_0;
          break;
        case 2048:
          recommendedParameters = Hash_1_3_6_1_4_1_311_75_1_1_1;
          break;
        case 3072:
          recommendedParameters = Hash_1_3_6_1_4_1_311_75_1_1_2;
          break;
        default:
          throw new ArgumentException("unsupported security level");
      }

      ParameterSet parameterSet;
      ParameterSet.TryGetNamedParameterSet(recommendedParameters, out parameterSet);
      return parameterSet;
    }

    private IssuerSetupParameters getIssuerSetupParameters(byte[] attributeEncoding, string name, bool forceRecSet = false)
    {
      IssuerSetupParameters isp = new IssuerSetupParameters();
      
      if (attributeEncoding.Length <= 21 || forceRecSet)
      {
        isp.ParameterSet = getRecommendedSet(2048);
        isp.UseRecommendedParameterSet = true;
      }
      else
      {
        isp.UseRecommendedParameterSet = false;
      }

      isp.UidH = "SHA-256";
      isp.UidP = encoding.GetBytes(name);

      // set the encoding parameters for the attributes
      isp.GroupConstruction = GroupConstruction.Subgroup;
      isp.E = attributeEncoding;

      // specification field unused in ABC4Trust
      isp.S = null;

      return isp;
    }


    [Fact]
    public void issuerParamsTest22AttributesFailsStdParameterSet()
    {
      byte[] attributeEncoding = new byte[] {0x0, 0x1, 0x0, 0x1, 0x1, 0x1, 0x0, 0x1, 0x0, 0x1, 0x1, 0x1, 0x0, 0x1, 0x0, 0x1, 0x1, 0x0, 0x1, 0x0, 0x1, 0x0 };
      IssuerSetupParameters isp = this.getIssuerSetupParameters(attributeEncoding, "foobar", true);
      // generate the serializable IssuerKeyAndParameters
      Assert.Throws<ArgumentException>(delegate { IssuerKeyAndParameters ikap = isp.Generate(true); });
    }

    [Fact]
    public void issuerParamsTest22AttributesOKWithCustomParameterSet()
    {
      byte[] attributeEncoding = new byte[] { 0x0, 0x1, 0x0, 0x1, 0x1, 0x1, 0x0, 0x1, 0x0, 0x1, 0x1, 0x1, 0x0, 0x1, 0x0, 0x1, 0x1, 0x0, 0x1, 0x0, 0x1, 0x0 };
      IssuerSetupParameters isp = this.getIssuerSetupParameters(attributeEncoding, "foobar");

      // generate the serializable IssuerKeyAndParameters
      IssuerKeyAndParameters ikap;
      Assert.DoesNotThrow(delegate { ikap = isp.Generate(true);});
    }

    [Fact]
    public void completeUProveProtocolTest()
    {
      byte[] attributeEncoding = new byte[] { 0x0, 0x1 };
      IssuerSetupParameters isp = this.getIssuerSetupParameters(attributeEncoding, "foobar");

      // generate the serializable IssuerKeyAndParameters
      IssuerKeyAndParameters ikap = null;
      Assert.DoesNotThrow(delegate { ikap = isp.Generate(true); });
      Assert.DoesNotThrow(delegate { ikap.IssuerParameters.Verify(); });

      byte[] issuerPrivateKey = new byte[] { 187, 133, 215, 21, 39, 178, 240, 67, 170, 4, 148, 139, 213, 36, 164, 114, 146, 228, 243, 140, 61, 76,
                                            174, 136, 137, 65, 238, 59, 7, 198, 34, 129 };



      int numberOfTokens = 10;
      string[] attributesString = new string[] { "foo", "bar" };
      int numberOfAttributes = attributesString.Length;
      byte[][] attributes = new byte[numberOfAttributes][];
      for (int i = 0; i < numberOfAttributes; i++)
      {
        attributes[i] = encoding.GetBytes(attributesString[i]);
      }
      BigInteger bi = new BigInteger(1, issuerPrivateKey);
      IssuerKeyAndParameters ikapFristMessage = new IssuerKeyAndParameters(bi, ikap.IssuerParameters);

 

      GroupElement hdG = ikap.IssuerParameters.Gq.CreateGroupElement(defines.hd);
      Issuer issuer = new Issuer(ikapFristMessage, numberOfTokens, attributes, null, hdG);

      FirstIssuanceMessage fi = issuer.GenerateFirstMessage();

      byte[] attributes2nd = (byte[])attributes.Clone();
      byte[] tokenInformation = new byte[] { };
      byte[] proverInformation = new byte[] { };
      Prover prover = new Prover(ip, numberOfTokens, attributes, tokenInformation, proverInformation, sessionDB[sessionID].deviceManager.GetDevice());


        /*
        * issue steps. creds.               
       *  setupIssuerParams.                
       *                                    
       * setIssuerPrivateKey. {byte[32]} : 		

       * 
       *  getFirstMessage - 		[0]	"42595520544300663591556673075677003532579993719172074290116620403700505383419"	string
   

       * 
       * getSecondMessage - with outputfrom getFristMessage.
       * 
       * getThirdMessage - with output from getSecondMessage.
       * 
       * generateTokens - with output from getThirdMessage
       * 
       * proveToken - commitedIndices 0x00000001 , 
       * messageParms "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n<abc:Message xmlns:abc=\"http://abc4trust.eu/wp2/abcschemav1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://abc4trust.eu/wp2/abcschemav1.0 ../../../../../../../../../abc4trust-xml/src/main/resources/xsd/schema.xsd\">\n<abc:Nonce>r9MQ57udpiWRwA==</abc:Nonce>\n</abc:Message>\n"
       * Token, 
       * -		tokenPrivateKeyParam	{byte[0x00000020]}	byte[]
    [0x00000000]	0x37	byte
    [0x00000001]	0xaa	byte
    [0x00000002]	0xb6	byte
    [0x00000003]	0xa9	byte
    [0x00000004]	0xd5	byte
    [0x00000005]	0x56	byte
    [0x00000006]	0xd8	byte
    [0x00000007]	0x21	byte
    [0x00000008]	0x77	byte
    [0x00000009]	0xe1	byte
    [0x0000000a]	0x7b	byte
    [0x0000000b]	0x5a	byte
    [0x0000000c]	0xf3	byte
    [0x0000000d]	0xad	byte
    [0x0000000e]	0xf7	byte
    [0x0000000f]	0x83	byte
    [0x00000010]	0x3c	byte
    [0x00000011]	0x2d	byte
    [0x00000012]	0xcf	byte
    [0x00000013]	0xdd	byte
    [0x00000014]	0x8a	byte
    [0x00000015]	0xbf	byte
    [0x00000016]	0x62	byte
    [0x00000017]	0xae	byte
    [0x00000018]	0xeb	byte
    [0x00000019]	0x44	byte
    [0x0000001a]	0x59	byte
    [0x0000001b]	0x6f	byte
    [0x0000001c]	0x1b	byte
    [0x0000001d]	0x09	byte
    [0x0000001e]	0x6d	byte
    [0x0000001f]	0xf0	byte

       * 
       * verifyTokenProof - proff from proveToken, otherwise same values.
       * 
       * 
       * 
       * */


    }
  }
}
