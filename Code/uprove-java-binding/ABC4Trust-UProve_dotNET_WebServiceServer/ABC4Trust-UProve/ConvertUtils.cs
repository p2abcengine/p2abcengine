using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using abc4trust_uprove.DataObjects;
using ABC4TrustSmartCard;
using UProveCrypto;

namespace abc4trust_uprove
{

  public class Pseudonym
  {
    public Pseudonym() { }
    public Pseudonym(GroupElement A, GroupElement P, BigInteger R) { this.A = A; this.P = P; this.R = R; }
    public GroupElement A { get; set; }
    public GroupElement P { get; set; }
    public BigInteger R { get; set; }
  }


  public static class ConvertUtils
  {
    public static PseudonymComposite convertPseudonym(Pseudonym p)
    {
      PseudonymComposite pc = new PseudonymComposite();
      pc.A = p.A.GetEncoded();
      pc.P = p.P.GetEncoded();
      pc.R = p.R.ToByteArray();
      return pc;
    }

    public static Pseudonym convertPseudonymComposite(PseudonymComposite pc, DeviceManager dManager)
    {
      Pseudonym p = new Pseudonym();
      p.A = dManager.Gq.CreateGroupElement(pc.A);
      p.P = dManager.Gq.CreateGroupElement(pc.P);
      p.R = new BigInteger(1, pc.R);
      return p;
    }

    public static FirstIssuanceMessageComposite convertFirstIssuanceMessage(FirstIssuanceMessage fi)
    {
      FirstIssuanceMessageComposite fic = new FirstIssuanceMessageComposite();

      byte[][] byteArray1 = new byte[fi.sigmaA.Length][];
      for (int i = 0; i < byteArray1.Length; i++)
      {
        byteArray1[i] = fi.sigmaA[i].GetEncoded();
      }

      byte[][] byteArray2 = new byte[fi.sigmaB.Length][];
      for (int i = 0; i < byteArray2.Length; i++)
      {
        byteArray2[i] = fi.sigmaB[i].GetEncoded();
      }

      fic.SigmaA = byteArray1;
      fic.SigmaB = byteArray2;
      fic.SigmaZ = fi.sigmaZ.GetEncoded();

      return fic;

    }

    public static FirstIssuanceMessage convertFirstIssuanceMessageComposite(FirstIssuanceMessageComposite fic, IssuerParameters ip)
    {
      GroupElement[] geArray1 = new GroupElement[fic.SigmaA.Length];
      for (int i = 0; i < fic.SigmaA.Length; i++)
      {
        geArray1[i] = ip.Gq.CreateGroupElement(fic.SigmaA[i]);
      }

      GroupElement[] geArray2 = new GroupElement[fic.SigmaB.Length];
      for (int i = 0; i < fic.SigmaB.Length; i++)
      {
        geArray2[i] = ip.Gq.CreateGroupElement(fic.SigmaB[i]);
      }

      FirstIssuanceMessage fi = new FirstIssuanceMessage(ip.Gq.CreateGroupElement(fic.SigmaZ), geArray1, geArray2);
      return fi;
    }

    public static SecondIssuanceMessageComposite convertSecondIssuanceMessage(SecondIssuanceMessage sm)
    {
      SecondIssuanceMessageComposite smc = new SecondIssuanceMessageComposite();

      byte[][] byteArray1 = new byte[sm.sigmaC.Length][];
      for (int i = 0; i < byteArray1.Length; i++)
      {
        byteArray1[i] = sm.sigmaC[i].ToByteArray();
      }
      smc.SigmaC = byteArray1;

      return smc;
    }

    public static SecondIssuanceMessage convertSecondIssuanceMessageComposite(SecondIssuanceMessageComposite sic)
    {
      BigInteger[] biArray = new BigInteger[sic.SigmaC.Length];
      for (int i = 0; i < biArray.Length; i++)
      {
        biArray[i] = new BigInteger(1, sic.SigmaC[i]);
      }

      SecondIssuanceMessage si = new SecondIssuanceMessage(biArray);

      return si;
    }

    public static ThirdIssuanceMessageComposite convertThirdIssuanceMessage(ThirdIssuanceMessage tim)
    {
      ThirdIssuanceMessageComposite timc = new ThirdIssuanceMessageComposite();

      byte[][] byteArray1 = new byte[tim.sigmaR.Length][];
      for (int i = 0; i < byteArray1.Length; i++)
      {
        byte[] tmp = tim.sigmaR[i].ToByteArray();
        byteArray1[i] = tmp;
      }

      timc.SigmaR = byteArray1;

      return timc;
    }

    public static ThirdIssuanceMessage convertThirdIssuanceMessageComposite(ThirdIssuanceMessageComposite tic)
    {
      BigInteger[] biArray = new BigInteger[tic.SigmaR.Length];
      for (int i = 0; i < biArray.Length; i++)
      {
        biArray[i] = new BigInteger(1, tic.SigmaR[i]); 
      }

      ThirdIssuanceMessage tm = new ThirdIssuanceMessage(biArray);

      return tm;
    }

    //converts a byte array to a string in hex format
    //e.g. 0x55 ==> "55", also left pads with 0 so that 0x01 is "01" and not "1"
    public static string HexToString(byte[] buf, int len)
    {
      string Data1 = "";
      string sData = "";
      int i = 0;
      while (i < len)
      {
        //Data1 = String.Format(”{0:X}”, buf[i++]); //no joy, doesn’t pad
        Data1 = buf[i++].ToString("X"); //same as “%02X” in C
        sData += Data1;
      }
      return sData;
    }

    public static UProveTokenComposite convertUProveToken(UProveToken up)
    {
      UProveTokenComposite utc = new UProveTokenComposite();

      utc.H = up.H.GetEncoded();
      utc.IsDeviceProtected = up.IsDeviceProtected;
      utc.PI = up.PI;
      utc.SigmaCPrime = up.SigmaCPrime.ToByteArray();
      utc.SigmaRPrime = up.SigmaRPrime.ToByteArray();
      utc.SigmaZPrime = up.SigmaZPrime.GetEncoded();
      utc.TI = up.TI;
      utc.Uidp = up.Uidp;

      return utc;
    }

    public static UProveToken convertUProveTokenComposite(IssuerParameters ip, UProveTokenComposite utc)
    {
      UProveToken up = new UProveToken();

      up.H = ip.Gq.CreateGroupElement(utc.H);
      up.IsDeviceProtected = utc.IsDeviceProtected;
      up.PI = utc.PI;
      up.SigmaCPrime = new BigInteger(1, utc.SigmaCPrime);
      up.SigmaRPrime = new BigInteger(1, utc.SigmaRPrime);
      up.SigmaZPrime = ip.Gq.CreateGroupElement(utc.SigmaZPrime);
      up.TI = utc.TI;
      up.Uidp = utc.Uidp;

      return up;
    }

    public static IssuerParametersComposite convertIssuerParameters(IssuerParameters ip)
    {
      IssuerParametersComposite ipc = new IssuerParametersComposite();
      ipc.E = ip.E;

      byte[][] gtemp = new byte[ip.G.Length][];
      for (int i = 0; i < gtemp.Length; i++)
      {
        gtemp[i] = ip.G[i].GetEncoded();
      }

      ipc.G = gtemp;

      if (ip.Gd != null)
      {
        ipc.Gd = ip.Gd.GetEncoded();
      }

      ipc.Gq = ip.Gq.GroupName;
      ipc.HashFunctionOID = ip.HashFunctionOID;
      ipc.IsDeviceSupported = ip.IsDeviceSupported;
      ipc.S = ip.S;
      ipc.UidH = ip.UidH;
      ipc.UidP = ip.UidP;
      ipc.UsesRecommendedParameters = ip.UsesRecommendedParameters;

      return ipc;
    }

    public static IssuerParameters convertIssuerParametersComposite(IssuerParametersComposite ipc, ParameterSet pSet)
    {
      IssuerParameters ip = new IssuerParameters();

      ip.E = ipc.E;
      ip.Gq = pSet.Group;
      

      GroupElement[] geArray = new GroupElement[ipc.G.Length];
      for (int i = 0; i < ipc.G.Length; i++)
      {
        geArray[i] = ip.Gq.CreateGroupElement(ipc.G[i]);
      }

      ip.G = geArray;
      if (ipc.Gd != null)
      {
        ip.Gd = ip.Gq.CreateGroupElement(ipc.Gd);
      }
      else
      {
        ip.Gd = pSet.Gd;// parametersSet.Gd;
      }

      ip.S = ipc.S;
      ip.UidH = ipc.UidH;
      ip.UidP = ipc.UidP;
      ip.UsesRecommendedParameters = ipc.UsesRecommendedParameters;
      return ip;
    }

    public static PresentationProof convertPresentationProofComposite(IssuerParameters ip, PresentationProofComposite pc, out byte[] tokenID, out byte[] proofSession)
    {
      PresentationProof p = new PresentationProof();

      p.A = pc.A;
      p.Ap = (pc.Ap == null ? null : pc.Ap);
      p.DisclosedAttributes = pc.DisclosedAttributes;
      p.Ps = (pc.Ps == null ? null : ip.Gq.CreateGroupElement(pc.Ps));

      BigInteger[] biArray = new BigInteger[pc.R.Length];
      for (int i = 0; i < biArray.Length; i++)
      {
        biArray[i] = new BigInteger(1, pc.R[i]);
      }
      p.R = biArray;
      if (pc.TildeValues != null)
      {
        int numCommitments = pc.TildeValues.Length / 3;
        p.Commitments = new CommitmentValues[numCommitments];
        for (int i = 0; i < numCommitments; i++)
        {
          p.Commitments[i] = new CommitmentValues(
              ip.Gq.CreateGroupElement(pc.TildeValues[(i * 3)]), // tildeC
              pc.TildeValues[(i * 3) + 1], // tildaA
              new BigInteger(1, pc.TildeValues[(i * 3) + 2]) // tildeR
              );
        }

        // we ignore the tildeO values. This method is called by the verifier, and 
        // the tildeO values should never be sent to the verifier.
      }

      tokenID = pc.TokenID;

      proofSession = (pc.MessageD == null ? null : pc.MessageD);

      return p;
    }

    public static PresentationProofComposite convertPresentationProof(PresentationProof p, BigInteger[] commitmentValues, byte[] tokenId, byte[] proofSession)
    {
      PresentationProofComposite pc = new PresentationProofComposite();

      pc.A = p.A;
      pc.Ap = (p.Ap == null ? null : p.Ap);
      pc.DisclosedAttributes = p.DisclosedAttributes;
      pc.Ps = (p.Ps == null ? null : p.Ps.GetEncoded());

      byte[][] byteArray = new byte[p.R.Length][];
      for (int i = 0; i < byteArray.Length; i++)
      {
        byteArray[i] = p.R[i].ToByteArray();
      }
      pc.R = byteArray;
      if (p.Commitments != null)
      {
        pc.TildeValues = new byte[3 * p.Commitments.Length][];
        for (int i = 0; i < p.Commitments.Length; i++)
        {
          pc.TildeValues[(i * 3)] = p.Commitments[i].TildeC.GetEncoded();
          pc.TildeValues[(i * 3) + 1] = p.Commitments[i].TildeA;
          pc.TildeValues[(i * 3) + 2] = p.Commitments[i].TildeR.ToByteArray();
        }
      }
      if (commitmentValues != null)
      {
        if (commitmentValues.Length != p.Commitments.Length)
        {
          throw new ArgumentException("inconsistent commitment values");
        }
        pc.TildeO = new byte[commitmentValues.Length][];
        for (int i = 0; i < commitmentValues.Length; i++)
        {
          pc.TildeO[i] = commitmentValues[i].ToByteArray();
        }
      }
      pc.TokenID = tokenId;
      pc.MessageD = (proofSession == null ? null : proofSession);
      return pc;
    }


  }
}
