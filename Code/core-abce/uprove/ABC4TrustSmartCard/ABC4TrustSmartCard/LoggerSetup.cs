using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Text;
using ABC4TrustSmartCard;

namespace ABC4TrustSmartCard
{

  public static class ParseConfigManager
  {
    
    #region static methods to parse config

    public static String LOGGER_SECITON = "loggerSection";
    public static String PROFILE_SECTION = "timeProfileSection";

    public class ProfileInfo
    {
      public String loggerToUse { get; set; }
      public TimeProfileElement.timeunit timeAs { get; set; }
      
    }

    private static string filePath() {
      // Get the application path needed to obtain
      // the application configuration file.
      string applicationName =
          Environment.GetCommandLineArgs()[0];

      applicationName = String.Concat(applicationName, ".exe");


      string exePath = System.IO.Path.Combine(Environment.CurrentDirectory, applicationName);

      // Get the configuration file. The file name has
      // this format appname.exe.config.
      return exePath;
      
    }

    public static Configuration config = ConfigurationManager.OpenExeConfiguration(ConfigurationUserLevel.None);
 
    public static string getAppValue(string name)
    {
      //Configuration appConfig = ConfigurationManager.OpenExeConfiguration(ConfigurationUserLevel.None);
      // Get the appSettings section.
      System.Configuration.AppSettingsSection appSettings =
          (System.Configuration.AppSettingsSection)config.GetSection("appSettings");
      return appSettings.Settings[name].Value;
    }


    public static Uri GetAddress()
    {
      try
      {
        string addStr = getAppValue("baseAddress");
        return new Uri(addStr);
      }
      catch (Exception ex)
      {
        Console.Out.WriteLine(ex.Message);
        throw;
      }
    }

    public static string GetApiPath()
    {
      try
      {
        string apiPath = getAppValue("apiPath");
        return apiPath;
      }
      catch (Exception ex)
      {
        Console.Out.WriteLine(ex.Message);
        throw;
      }
    }

    public static string GetIssuerApiPath()
    {
      try
      {
        string apiPath = getAppValue("apiIssuerPath");
        return apiPath;
      }
      catch (Exception ex)
      {
        Console.Out.WriteLine(ex.Message);
        throw;
      }
    }

    public static string GetProverApiPath()
    {
      try
      {
        string apiPath = getAppValue("apiProverPath");
        return apiPath;
      }
      catch (Exception ex)
      {
        Console.Out.WriteLine(ex.Message);
        throw;
      }
    }

    public static string GetStorePath()
    {
      try
      {
        string storePath = getAppValue("storePath");
        return storePath;
      }
      catch (Exception ex)
      {
        Console.Out.WriteLine(ex.Message);
        throw;
      }
    }


    public static string GetApiInfoPath()
    {
      try
      {
        string apiPath = getAppValue("apiInfoPath");
        return apiPath;
      }
      catch (Exception ex)
      {
        Console.Out.WriteLine(ex.Message);
        throw;
      }
    }


    public static bool DoTimeProfile()
    {
      try
      {
        bool doTimeProfile = Boolean.Parse(getAppValue("timeprofile"));
        return doTimeProfile;
      }
      catch (Exception ex)
      {
        Console.Out.WriteLine(ex.Message);
        return false;
      }
    }
    
    public static bool DebugPrintMessageData()
    {
      try
      {
        bool debugPrintMessageData = Boolean.Parse(getAppValue("debugPrintMessageData"));
        return debugPrintMessageData;
      }
      catch (Exception ex)
      {
        Console.Out.WriteLine(ex.Message);
        return false;
      }
    }
    
    public static ProfileInfo profileInfo
    {
      get
      {
        try
        {
          ProfileInfo pInfo = new ProfileInfo();
          TimeProfileSection tSection = (TimeProfileSection)config.GetSection(ParseConfigManager.PROFILE_SECTION);
          bool logToFile = Boolean.Parse(getAppValue("logToFile"));
          string loggerToUse = tSection.timeProfile.loggerToUse;

          pInfo.loggerToUse = loggerToUse;
          pInfo.timeAs = tSection.timeProfile.timeAs;
          return pInfo;
        }
        catch (Exception ex)
        {
          Console.Out.WriteLine(ex.Message);
          return new ProfileInfo();
        }
      }
    }


    public static bool SetupTimeProfiles()
    {
      if (!DoTimeProfile())
      {
        return false;
      }

      try
      {
        TimeProfileSection tSection = (TimeProfileSection)config.GetSection(ParseConfigManager.PROFILE_SECTION);
        bool logToFile = Boolean.Parse(getAppValue("logToFile"));
        string loggerToUse = tSection.timeProfile.loggerToUse;
        if (!logToFile)
        {
          setupProfileLogger(loggerToUse);
        }
      }
      catch (Exception ex)
      {
        Console.Out.WriteLine(ex.Message);
        return false;
      }
      return true;
    }

    private static void setupProfileLogger(string loggerToUse)
    {
      LoggerSection lSection = (LoggerSection)config.GetSection(ParseConfigManager.LOGGER_SECITON);

      LoggerCollection lCol = lSection.Loggers;
      foreach (LoggerConfigElement lElement in lCol)
      {
        if (lElement.loggerName != loggerToUse)
        {
          continue;
        }
        LoggerSpec logFile = new LoggerSpec();
        logFile.name = lElement.loggerName;
        logFile.level = Logger.Level.Info;
        logFile.dateFormat = "{0:dd/MM/yyyy H:mm:ss zzz} : ";
        logFile.logType = Logger.LogType.File;
        logFile.fileName = Path.Combine(lElement.path, lElement.fileBaseName);
        Logger.Instance.AppendLoggerSpec(logFile);
        return;
      }
      throw new Exception("Logger specified: " + loggerToUse + ", could not be found");
    }


    public static void SetupConfigLoggers()
    {
      try
      {
        bool logToFile = Boolean.Parse(getAppValue("logToFile"));

        if (!logToFile)
        {
          return;
        }
      }
      catch (Exception ex)
      {
        Console.Out.WriteLine(ex.Message);
        return;
      }

      try
      {
        LoggerSection lSection = (LoggerSection)config.GetSection(ParseConfigManager.LOGGER_SECITON);

        LoggerCollection lCol = lSection.Loggers;
        foreach (LoggerConfigElement lElement in lCol)
        {
          LoggerSpec logFile = new LoggerSpec();
          logFile.name = lElement.loggerName;
          logFile.level = Logger.Level.Info;
          logFile.dateFormat = "{0:dd/MM/yyyy H:mm:ss zzz} : ";
          logFile.logType = Logger.LogType.File;
          logFile.fileName = Path.Combine(lElement.path, lElement.fileBaseName);
          Logger.Instance.AppendLoggerSpec(logFile);
        }
      }
      catch (Exception ex)
      {
        Console.Out.WriteLine(ex.Message);
      }

    }

  }

    #endregion
  
  #region helper/container classes to parse config.

  public class TimeProfileSection : ConfigurationSection
  {
    // Create a configuration section. 
    public TimeProfileSection() { }

    [ConfigurationProperty("timeProfile")]
    public TimeProfileElement timeProfile
    {
      get
      {
        return (TimeProfileElement)this["timeProfile"];
      }
    }

  }

  public class TimeProfileElement : ConfigurationElement
  {
    // Create the element. 
    public TimeProfileElement() { }

    public enum timeunit { miliseconds = 0, seconds = 1 }

    // Create the element. 
    public TimeProfileElement(string loggerToUse, string timeAs)
    {
      // xxx handle if loggers is not setup.
      this.loggerToUse = loggerToUse;
      this.timeAs = ToTimeUnit(timeAs);

    }

    // get or set the filename prop.
    [ConfigurationProperty("loggerToUse",
      DefaultValue = "timeProfile",
      IsRequired = true)]
    public string loggerToUse
    {
      get
      {
        return (string)this["loggerToUse"];
      }
      set
      {
        this["loggerToUse"] = value;
      }
    }

    // get or set the filename prop.
    [ConfigurationProperty("timeAs",
      DefaultValue = timeunit.miliseconds,
      IsRequired = true)]
    public timeunit timeAs
    {
      get
      {
        return (timeunit)this["timeAs"];

      }
      set
      {
        this["timeAs"] = FromTimeUnit(value);
      }
    }

    private timeunit ToTimeUnit(string p)
    {
      if (p.Equals("seconds"))
      {
        return timeunit.seconds;
      }
      else if (p.Equals("miliseconds"))
      {
        return timeunit.miliseconds;
      }
      else
      {
        throw new Exception("work time as entry");
      }
    }

    private string FromTimeUnit(timeunit p)
    {
      if (p == timeunit.seconds)
      {
        return "seconds";
      }
      else if (p == timeunit.miliseconds)
      {
        return "miliseconds";
      }
      return "";
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
      DefaultValue = "std_out",
      IsRequired = true,
      IsKey = true)]
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
  #endregion

}
