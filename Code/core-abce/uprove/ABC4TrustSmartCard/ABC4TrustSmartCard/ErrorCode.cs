using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Security.Permissions;
using System.Runtime.Serialization;

namespace ABC4TrustSmartCard
{
  /// <summary>
  /// Errorcodes as specified in the ABC4TRust smartcard doc. Error codes 0x90 0xXX (where XX is not 00) is local impl
  /// errors
  /// </summary>
  [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Usage", "CA2237:MarkISerializableTypesWithSerializable")]
  public class ErrorCode : System.Exception
  {
    public ErrorCode(int iSW1, int iSW2)
    {
      this.SW1 = iSW1;
      this.SW2 = iSW2;
    }
    public int SW1 { get; set; }
    public int SW2 { get; set; }

    public String Command { get; set; }

    public bool IsOK
    {
      get
      {
        if (SW1 == 0x90 && SW2 == 0x00)
        {
          return true;
        }
        return false;
      }
    }
  }
}
