using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

namespace ABC4TrustSmartCard
{

  public static class LoggerDefine
  {
    public static String OUT_CONSOLE = "std_out";
    public static String DEBUG_CONSOLE = "debug_out";
  }


  public sealed class Logger
  {

    private static volatile Logger instance;
    private static object syncRoot = new Object();
    private static ConcurrentDictionary<string, Log> loggers = new ConcurrentDictionary<string, Log>();

    private Logger() { }

    public static Logger Instance
    {
      get
      {
        if (instance == null)
        {
          lock (syncRoot)
          {
            if (instance == null)
              instance = new Logger();
          }
        }

        return instance;
      }
    }

    [Flags]
    public enum Level
    {
      Debug = 1,
      Verbose = 2,
      Info = 4,
      Warn = 8,
      Error = 16,
    }

    [Flags]
    public enum LogType
    {
      Console = 1,
      Error = 2, 
      File = 4,
    }


    public void AppendLoggerSpec(LoggerSpec spec)
    {
      if (!loggers.ContainsKey(spec.name))
      {
        loggers.TryAdd(spec.name, new Log());
      }
      if ((spec.logType & Logger.LogType.Console) != 0)
      {
        getLog(spec.name).Add(new LogWriterConsole(spec.level, spec.dateFormat));
      }
      if ((spec.logType & Logger.LogType.Error) != 0)
      {
        loggers[spec.name].Add(new LogWriterErrorConsole(spec.level, spec.dateFormat));
      }
      if ((spec.logType & Logger.LogType.File) != 0)
      {
        loggers[spec.name].Add(new LogWriterFile(spec.fileName, spec.level, spec.dateFormat));
      }
    }

    public Log getLog(string name)
    {
      if (!loggers.ContainsKey(name))
      {
        throw new Exception("some error");
      }
      return loggers[name];
    }

  }


  public sealed class LoggerSpec
  {
    public String name { get; set; }
    public Logger.Level level { get; set; }
    public Logger.LogType logType { get; set; }
    public string fileName { get; set; }
    public string dateFormat { get; set; }
  }


  public sealed class Log
  {

    private List<LogWriter> logWriters = new List<LogWriter>();

    internal void Add(LogWriter w) {
      logWriters.Add(w);
    }

    private Logger.Level _logLevel = Logger.Level.Info;
    public Logger.Level logLevel 
    { 
      get 
      { 
        return _logLevel;
      } 
      set
      {
        _logLevel = value;
      }
    }

    public void write(string text)
    {
      foreach (LogWriter l in logWriters)
      {
        l.write(text, _logLevel);
      }
    }

    public void write(string format, params Object[] arg)
    {
      foreach (LogWriter l in logWriters)
      {
        l.write(String.Format(format, arg), logLevel);
      }
    }
  }


  public abstract class LogWriter
  {
    protected LogWriter(Logger.Level level, string dateFormat)
    {
      this.level = level;
      this.dateFormat = dateFormat;
    }


    abstract public void write(string text, Logger.Level level = Logger.Level.Info);
    protected Logger.Level level { get; set; }
    protected string dateFormat { get; set; }
    protected string getTime
    {
      get
      {
        if (String.IsNullOrEmpty(this.dateFormat))
        {
          return "";
        }
        return String.Format(dateFormat, DateTime.Now);
      }
    }
  }

  public class LogWriterConsole : LogWriter
  {
    public LogWriterConsole(Logger.Level level, string dateFormat) : base(level, dateFormat)
    {   
    }

    public override void write(string text, Logger.Level level = Logger.Level.Info)
    {
      if (level <= this.level)
      {
        Console.Out.WriteLine(this.getTime + text);
      }
    }
  }

  public class LogWriterErrorConsole : LogWriter
  {

    public LogWriterErrorConsole(Logger.Level level, string dateFormat) : base(level, dateFormat)
    {
    }

    public override void write(string text, Logger.Level level = Logger.Level.Info)
    {
      if (level <= this.level)
      {
        Console.Error.WriteLine(this.getTime + text);
      }
    }
  }


  public sealed class LogWriterFile : LogWriter, IDisposable
  {
    private static object sync = new Object();
    private string filename;

    public LogWriterFile(string fileBasename, Logger.Level level, string dateFormat) : base(level, dateFormat)
    {
      this.filename = fileBasename + ".log";
    }

    public override void write(string text, Logger.Level level = Logger.Level.Info)
    {
      if (level <= this.level)
      {
        lock (sync)
        {
          using (StreamWriter writer = new StreamWriter(filename, true))
          {
            writer.WriteLine(this.getTime + text);
          }
        }
      }
    }

    #region IDisposable Members

    public void Dispose()
    {
 
    }

    #endregion
  }
 

}
