

using System.Runtime.Serialization;
namespace UProveWCFServiceLib
{
  [DataContract]
  public abstract class RuntimeFault
  {
    private string _details;

    [DataMember]
    public string Details
    {
      set { _details = value; }
      get { return _details; }
    }
  }

  [DataContract]
  public class ApiArgumentFault : RuntimeFault
  {
    private string _argument;
    private string _value;
    
    [DataMember]
    public string Argument
    {
      set { _argument = value; }
      get { return _argument; }
    }

    [DataMember]
    public string ArgumentValue
    {
      set { _value = value; }
      get { return _value; }
    }
  }







}