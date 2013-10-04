using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using ABC4TrustSmartCard;
using SecureDataStore;

namespace UProveService
{
  class Program
  {

    static private void SetupLoggers()
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



    static void Main(string[] args)
    {
      // optimize jit compile on multi-core systems.
      ProfileOptimization.SetProfileRoot(ParseConfigManager.GetStorePath());
      ProfileOptimization.StartProfile("uProveWebService.Profile");


      SetupLoggers();

      UProveThreadWorkerIssuer workerThread = new UProveThreadWorkerIssuer();
      Thread interOp = new Thread(new ThreadStart(workerThread.Startup));
      interOp.IsBackground = true;
      interOp.Start();

      UProveThreadWorkerProver workerThreadProver = new UProveThreadWorkerProver();
      Thread interOpProver = new Thread(new ThreadStart(workerThreadProver.Startup));
      interOpProver.IsBackground = true;
      interOpProver.Start();

      UProveThreadWorkerInfo workerThreadInfo = new UProveThreadWorkerInfo();
      Thread interOpInfo = new Thread(new ThreadStart(workerThreadInfo.Startup));
      interOpInfo.IsBackground = true;
      interOpInfo.Start();




      Console.ReadLine();
      workerThread.Dispose();
      workerThreadInfo.Dispose();

    }
  }
}
