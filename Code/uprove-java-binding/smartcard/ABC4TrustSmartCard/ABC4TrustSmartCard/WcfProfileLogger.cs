using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Linq;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Configuration;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using System.Text;

namespace ABC4TrustSmartCard
{

  public class WcfProfileLogger : IDispatchMessageInspector, IServiceBehavior
  {
    private Log logger;
    private TimeProfileElement.timeunit tUnit;

    public WcfProfileLogger() {
      ParseConfigManager.ProfileInfo pInfo = ParseConfigManager.profileInfo;
      bool doProfile = ParseConfigManager.doTimeProfile();
      if (doProfile)
      {
        logger = Logger.Instance.getLog(pInfo.loggerToUse);
        tUnit = pInfo.timeAs;
      }
      else
      {
        throw new Exception("Should not call wcfProfileLogger if not doing time profile");
      }
    }
    
    #region IDispatchMessageInspector
    public object AfterReceiveRequest(ref Message request, IClientChannel channel,
        InstanceContext instanceContext)
    {
      try
      {
        ProfilingObject pObject = new ProfilingObject();

        pObject.action = OperationContext.Current.IncomingMessageHeaders.Action.Split('/').ToList().Last();
        pObject.timer = new AbcTimer();
        logger.write("RPC call '{0}' started", pObject.action);
        pObject.timer.Start();

        return pObject;
      } catch (Exception) {
        ProfilingObject pObject = new ProfilingObject();
        pObject.timer = new AbcTimer();
        pObject.action = "Unknown";
        logger.write("RPC call '{0}' started", pObject.action);
        pObject.timer.Start();
        return pObject;
      }
    }

    

    public void BeforeSendReply(ref Message reply, object correlationState)
    {
      ProfilingObject pObject = (ProfilingObject)correlationState;
      pObject.timer.Stop();
      double t = Utils.GetTime(pObject.timer.getElapsed(), tUnit);
      logger.write("RPC call '{0}' ending.  Was running for '{1}' {2}", pObject.action, Math.Round(t), tUnit.ToString());
    }

    #endregion

    #region IServiceBehavior

    public void ApplyDispatchBehavior(ServiceDescription serviceDescription,
        ServiceHostBase serviceHostBase)
    {
      foreach (ChannelDispatcher dispatcher in serviceHostBase.ChannelDispatchers)
      {
        foreach (var endpoint in dispatcher.Endpoints)
        {
          endpoint.DispatchRuntime.MessageInspectors.Add(new WcfProfileLogger());
        }
      }
    }

    public void AddBindingParameters(ServiceDescription serviceDescription,
        ServiceHostBase serviceHostBase, Collection<ServiceEndpoint> endpoints,
        BindingParameterCollection bindingParameters)
    {
    }

    public void Validate(ServiceDescription serviceDescription,
        ServiceHostBase serviceHostBase)
    {
    }

    #endregion
  }

  public class WcfProfileLoggerExtension : BehaviorExtensionElement
  {
    protected override object CreateBehavior()
    {
      return new WcfProfileLogger();
    }

    public override Type BehaviorType
    {
      get
      {
        return typeof(WcfProfileLogger);
      }
    }
  }

}
