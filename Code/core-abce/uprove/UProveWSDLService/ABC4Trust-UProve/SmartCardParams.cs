using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ComponentModel;
using System.ComponentModel.DataAnnotations;

namespace abc4trust_uprove
{
  public class SmartCardParams
  {
    [StringLength(4, ErrorMessage = "Pin code cannot exceed 4 characters. ")]
    public string pin { get; set; }

    [Range(0, 255)]
    public int credID { get; set; }

    [Range(0, 255)]
    public int groupID { get; set; }

    [Range(0, 255)]
    public int proverID { get; set; }

    public SmartCardParams() { }
    public SmartCardParams(string pin, int credID, int groupID, int proverID) {
      this.pin = pin;
      this.credID = credID;
      this.groupID = groupID;
      this.proverID = proverID;
    }

  }
}
