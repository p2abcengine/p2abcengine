using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UProveCrypto;

namespace abc4trust_uprove.DataObjects
{
  public sealed class SessionData
  {
    public SessionData(LoginComposite inParm, String sessionID, int securityLevel)
    {
      SmartCardParams sParams = new SmartCardParams(inParm.PinCode, inParm.CredID, inParm.GroupID, inParm.ProverID);
      _securityLevel = securityLevel;
      _parameterSet = SecurityLevelUtils.getRecommendedSet(securityLevel);
      deviceManager = new DeviceManager(sParams, parameterSet, inParm.UseVirtualDevice);
      
      lastAccessed = DateTime.Now;
      this.sessionID = sessionID;
    }

    public static string ConvertStringArrayToString(string[] array)
    {
      //
      // Concatenate all the elements into a StringBuilder.
      //
      StringBuilder builder = new StringBuilder();
      foreach (string value in array)
      {
        builder.Append(value);
        builder.Append('.');
      }
      return builder.ToString();
    }

    DateTime _lastAccessed;
    DeviceManager _deviceManager;
    string _sessionID;
    Issuer _issuer;
    Prover _prover;
    byte[] _privateKey;
    ParameterSet _parameterSet;
    int _securityLevel;
    GroupDescription _group;
    GroupElement _groupElement;

    public GroupElement groupElement
    {
      get
      {
        return _groupElement;
      }
      set
      {
        _groupElement = value;
      }
    }

    public GroupDescription group
    {
      get
      {
        return _group;
      }
      set
      {
        _group = value;
      }
    }

    public ParameterSet parameterSet
    {
      get
      {
        return _parameterSet;
      }
      set
      {
        _parameterSet = value;
      }
    }

    public int securityLevel
    {
      get
      {
        return _securityLevel;
      }
      set
      {
        _securityLevel = value;
      }
    }

    public DateTime lastAccessed
    {
      get
      {
        return _lastAccessed;
      }
      set
      {
        _lastAccessed = value;
      }
    }

    public DeviceManager deviceManager
    {
      get
      {
        _lastAccessed = DateTime.Now;
        return _deviceManager;
      }
      set
      {
        _deviceManager = value;
        _lastAccessed = DateTime.Now;
      }
    }

    public string sessionID
    {
      get
      {
        _lastAccessed = DateTime.Now;
        return _sessionID;
      }
      set
      {
        _lastAccessed = DateTime.Now;
        _sessionID = value;
      }
    }

    public Issuer issuer
    {
      get
      {
        _lastAccessed = DateTime.Now;
        return _issuer;
      }
      set
      {
        _lastAccessed = DateTime.Now;
        _issuer = value;
      }
    }

    public Prover prover
    {
      get
      {
        _lastAccessed = DateTime.Now;
        return _prover;
      }
      set
      {
        _lastAccessed = DateTime.Now;
        _prover = value;
      }
    }

    public byte[] privateKey
    {
      get
      {
        _lastAccessed = DateTime.Now;
        return _privateKey;
      }
      set
      {
        _lastAccessed = DateTime.Now;
        _privateKey = value;
      }
    }
    

    public static string generateSessionKey()
    {
      return System.Guid.NewGuid().ToString();
    }
  }

}
