using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Text;
using ABC4TrustSmartCard;

namespace abc4trust_uprove
{

  public static class ParseConfigManager
  {
    public static void SetupConfigLoggers()
    {
      bool logToFile = Boolean.Parse(ConfigurationManager.AppSettings["logToFile"]);
      if (!logToFile)
      {
        return;
      }

      string sectionName = "loggerSection";
      try
      {
        LoggerSection lSection = (LoggerSection)ConfigurationManager.GetSection(sectionName);

        LoggerCollection lCol = lSection.Loggers;
        foreach (LoggerConfigElement lElement in lCol) {
          LoggerSpec logFile = new LoggerSpec();
          logFile.name = lElement.loggerName;
          logFile.level = Logger.Level.Info;
          logFile.dateFormat = "{0:dd/MM/yyyy H:mm:ss zzz} : ";
          logFile.logType = Logger.LogType.File;
          logFile.fileName = Path.Combine(lElement.path, lElement.fileBaseName);
          Logger.Instance.AppendLoggerSpec(logFile);
          Console.Out.WriteLine(lElement.loggerName);
        } 
      } catch (Exception ex) {
        Console.Out.WriteLine(ex.Message);
      }
      
    }

  }

  public class LoggerSection : ConfigurationSection
  {
    // Create a configuration section. 
    public LoggerSection() { }

     // Set or get the ConsoleElement. 
    [ConfigurationProperty("", IsDefaultCollection = true)]
    public LoggerCollection Loggers
    {
      get
      {
        return (LoggerCollection)base[""];
      }
    }
  }

  public sealed class LoggerCollection : ConfigurationElementCollection
  {
    protected override ConfigurationElement CreateNewElement()
    {
      return new LoggerConfigElement();
    }
    protected override object GetElementKey(ConfigurationElement element)
    {
      return ((LoggerConfigElement)element).loggerName;
    }
    public override ConfigurationElementCollectionType CollectionType
    {
      get
      {
        return ConfigurationElementCollectionType.BasicMap;
      }
    }
    protected override string ElementName
    {
      get
      {
        return "logger";
      }
    }
  }


  public class LoggerConfigElement : ConfigurationElement
  {
    // Create the element. 
    public LoggerConfigElement() { }

    // Create the element. 
    public LoggerConfigElement(string loggerName, string fileBaseName, string path)
    {
      this.loggerName = loggerName;
      this.fileBaseName = fileBaseName;
      this.path = path;

    }

    // get or set the filename prop.
    [ConfigurationProperty("loggerName",
      DefaultValue = "foobar",
      IsRequired = true,
      IsKey= true)]
    public string loggerName
    {
      get
      {
        return (string)this["loggerName"];
      }
      set
      {
        this["loggerName"] = value;
      }
    }

    // get or set the filename prop.
    [ConfigurationProperty("fileBaseName",
      DefaultValue = "uprove-log",
      IsRequired = true)]
    public string fileBaseName
    {
      get
      {
        return (string)this["fileBaseName"];
      }
      set
      {
        this["fileBaseName"] = value;
      }
    }

    // get or set the path prop.
    [ConfigurationProperty("path",
      DefaultValue = ".",
      IsRequired = true)]
    public string path
    {
      get
      {
        return (string)this["path"];
      }
      set
      {
        this["path"] = value;
      }
    }
  }

}
