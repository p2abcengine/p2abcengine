using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;

namespace ABC4TrustSmartCard
{
  public static class StringExtensions
  {
    public static IEnumerable<String> SplitInParts(this String s, Int32 partLength)
    {
      if (s == null)
        throw new ArgumentNullException("s");
      if (partLength <= 0)
        throw new ArgumentException("Part length has to be positive.", "partLength");
      for (var i = 0; i < s.Length; i += partLength)
        yield return s.Substring(i, Math.Min(partLength, s.Length - i));
    }
  }

   public static class Utils
   {
     public static byte[] GetBytesFromString(String str, int splitNum = 2)
     {
       List<String> listAs2Byte = str.SplitInParts(splitNum).ToList<String>();
       byte[] ret = new byte[listAs2Byte.Count];
       int i = 0;
       foreach (String s in listAs2Byte)
       {
         ret[i] = (byte)Convert.ToInt32(s, 16);
         ++i;
       }
       return ret;
     }


     public static string ByteArrayToString(byte[] byteArray)
     {
       StringBuilder hex = new StringBuilder(byteArray.Length * 2);
       foreach (byte b in byteArray)
         hex.AppendFormat("{0:x2}-", b);
       return hex.ToString();
     }


     public static bool ByteArrayCompare(byte[] a1, byte[] a2)
     {
       IStructuralEquatable eqa1 = a1;
       return eqa1.Equals(a2, StructuralComparisons.StructuralEqualityComparer);
     }

     public static void PrintByteArrayToConsole(byte[] data)
     {
       Log cOut = Logger.Instance.getLog(LoggerDefine.OUT_CONSOLE);
       String output = BitConverter.ToString(data);
       cOut.write(output);
     }


     public static string PrintAsHexValue(int p)
     {
       StringBuilder hex = new StringBuilder(2);
       hex.AppendFormat("{0:x2}-", p);
       return hex.ToString();
     }
   }


}
