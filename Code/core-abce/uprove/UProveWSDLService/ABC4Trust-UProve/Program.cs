using System;
using System.ServiceModel;
using System.ServiceModel.Description;
using Thinktecture.ServiceModel.Extensions.Description;
using ABC4TrustSmartCard;
using System.Diagnostics;

/**
 * ABC4Trust UProve/Java WebService interop, server-side implementation.
 * 
 * WSDL generation ONLY works when running under .NET on Windows - everything else is ok using Mono on other platforms.
 * 
 * @author Raphael Dobers
 */


namespace abc4trust_uprove
{
    // The Main method which exposes UProve as a webservice
    class Program
    {

      static void setupLoggers()
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
        setupLoggers();
        Log cOut = Logger.Instance.getLog(LoggerDefine.OUT_CONSOLE);
        // Create a WSHttpBinding instance
        WSHttpBinding binding = new WSHttpBinding();
        binding.Security.Mode = SecurityMode.None;

        binding.Namespace = "http://abc4trust-uprove/Service1";
        string baseAddress = "http://127.0.0.1:8080/abc4trust-webservice/";

        if (args.Length > 0)
        {
          try
          {
            int port = int.Parse(args[0]);
            cOut.write("Starting UProve WebService on port: " + port);
          }
          catch (Exception ex)
          {
            cOut.write("Exception while parsing port number from args: " + ex.Message);
            DebugUtils.DebugPrint(ex.StackTrace.ToString());
          }

          baseAddress = "http://127.0.0.1:" + args[0] + "/abc4trust-webservice/";
        }
        
        try
        {
          FlatWsdlServiceHost host = new FlatWsdlServiceHost(typeof(Service1));

          // Check to see if the service host already has a ServiceMetadataBehavior
          System.ServiceModel.Description.ServiceMetadataBehavior smb = host.Description.Behaviors.Find<ServiceMetadataBehavior>();
          System.ServiceModel.Description.ServiceDebugBehavior sdb = host.Description.Behaviors.Find<ServiceDebugBehavior>();

          // If not, add one
          if (smb == null)
          {
            smb = new ServiceMetadataBehavior();
          }

          if (sdb == null)
          {
            sdb = new ServiceDebugBehavior();
          }

          sdb.IncludeExceptionDetailInFaults = true;
          smb.HttpGetEnabled = true;
          smb.HttpGetUrl = new Uri(baseAddress + "wsdl");

          cOut.write("Fetch WSDL using .NET on Windows at: " + smb.HttpGetUrl.ToString());

          host.Description.Behaviors.Add(smb);

          // add time profile logger if needed.
          if (ParseConfigManager.SetupTimeProfiles())
          {
            WcfProfileLogger pExt = new WcfProfileLogger();
            host.Description.Behaviors.Add(pExt);
          }

          // add debug printer behaviors if needed
          if (ParseConfigManager.DebugPrintMessageData())
          {
            WcfDebugPrint dPrint = new WcfDebugPrint();
            host.Description.Behaviors.Add(dPrint);
          }


          // Add a service endpoint using the created binding
          ServiceEndpoint endp = host.AddServiceEndpoint(typeof(IService1), binding, baseAddress);
          endp.Behaviors.Add(new FlatWsdl());

          host.Open();
          cOut.write("UProve WebService listening on {0} . . .", baseAddress);
          cOut.write("Press Enter to exit");
          Console.ReadLine();
          host.Close();

        }
        catch (Exception ex)
        {
          cOut.write("Exception while running UProve WebService: " + ex.Message);
          DebugUtils.DebugPrint(ex.StackTrace.ToString());
        }
      }
    }
  }


