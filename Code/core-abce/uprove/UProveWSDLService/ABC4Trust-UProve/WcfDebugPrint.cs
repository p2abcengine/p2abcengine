using System;
using System.Collections.Generic;
using System.Linq;
using System.ServiceModel;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using System.Text;
using ABC4TrustSmartCard;

namespace abc4trust_uprove
{
  class WcfDebugPrint : IDispatchMessageInspector, IServiceBehavior
  {
    private Log _logger;

    public WcfDebugPrint()
    {
      ParseConfigManager.ProfileInfo pInfo = ParseConfigManager.profileInfo;
      _logger = Logger.Instance.getLog(pInfo.loggerToUse);
    }

    #region IDispatchMessageInspector Members

    public object AfterReceiveRequest(ref System.ServiceModel.Channels.Message request, System.ServiceModel.IClientChannel channel, System.ServiceModel.InstanceContext instanceContext)
    {
      try
      {
        DebugPrintObject dObj = new DebugPrintObject
        {
          action = OperationContext.Current.IncomingMessageHeaders.Action.Split('/').ToList().Last()
        };

        _logger.write("RPC call '{0}' started with data '{1}'", dObj.action, request.ToString());
        return dObj;
      }
      catch (Exception)
      {
        DebugPrintObject dObj = new DebugPrintObject
        {
          action = OperationContext.Current.IncomingMessageHeaders.Action.Split('/').ToList().Last()
        };
        _logger.write("RPC call '{0}' started with data '{1}'", dObj.action, request.ToString());
        return dObj;
      }
    }

    public void BeforeSendReply(ref System.ServiceModel.Channels.Message reply, object correlationState)
    {
      DebugPrintObject dObj = (DebugPrintObject) correlationState;
      _logger.write("RPC call '{0}' ending with data '{1}'", dObj.action, reply.ToString());
    }

    #endregion


    #region IServiceBehavior Members

    public void AddBindingParameters(ServiceDescription serviceDescription, System.ServiceModel.ServiceHostBase serviceHostBase, System.Collections.ObjectModel.Collection<ServiceEndpoint> endpoints, System.ServiceModel.Channels.BindingParameterCollection bindingParameters)
    {
    }

    public void ApplyDispatchBehavior(ServiceDescription serviceDescription, System.ServiceModel.ServiceHostBase serviceHostBase)
    {
      foreach (ChannelDispatcher dispatcher in serviceHostBase.ChannelDispatchers)
      {
        foreach (var endpoint in dispatcher.Endpoints)
        {
          endpoint.DispatchRuntime.MessageInspectors.Add(new WcfDebugPrint());
        }
      }
    }

    public void Validate(ServiceDescription serviceDescription, System.ServiceModel.ServiceHostBase serviceHostBase)
    {
    }

    #endregion

    
  }
}
