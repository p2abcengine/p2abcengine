using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ABC4TrustSmartCard;

namespace UProve_ABC4Trust_unitTest
{
  public static class LoggerUtils
  {
    public static void setupLoggers()
    {
      // normal console writer.
      LoggerSpec outConsole = new LoggerSpec();
      outConsole.name = LoggerDefine.OUT_CONSOLE;
      outConsole.level = Logger.Level.Info;
      outConsole.dateFormat = "{0:dd/MM/yyyy H:mm:ss zzz} : ";
      outConsole.logType = Logger.LogType.Console;
      Logger.Instance.AppendLoggerSpec(outConsole);

      // debug console writer.
      LoggerSpec debugConsole = new LoggerSpec();
      debugConsole.name = LoggerDefine.DEBUG_CONSOLE;
      debugConsole.level = Logger.Level.Info;
      debugConsole.dateFormat = "{0:dd/MM/yyyy H:mm:ss zzz} : ";
      debugConsole.logType = Logger.LogType.Console;
      Logger.Instance.AppendLoggerSpec(debugConsole);

      ParseConfigManager.SetupConfigLoggers();

    }

  }
}
