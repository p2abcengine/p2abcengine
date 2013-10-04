using System;
using System.Collections.Generic;
using System.Linq;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using System.Text;

namespace ABC4TrustSmartCard
{
  public class WcfTimeProfileEndPointLogger : IDispatchMessageInspector, IEndpointBehavior
  {
    private Log _logger;
    private TimeProfileElement.timeunit _tUnit;

    public WcfTimeProfileEndPointLogger()
    {
      ParseConfigManager.ProfileInfo pInfo = ParseConfigManager.profileInfo;
      bool doProfile = ParseConfigManager.DoTimeProfile();
      if (doProfile)
      {
        _logger = Logger.Instance.getLog(pInfo.loggerToUse);
        _tUnit = pInfo.timeAs;
      }
      else
      {
        throw new Exception("Should not call wcfProfileLogger if not doing time profile");
      }
    }

    #region IDispatchMessageInspector Members

    public object AfterReceiveRequest(ref System.ServiceModel.Channels.Message request, System.ServiceModel.IClientChannel channel, System.ServiceModel.InstanceContext instanceContext)
    {
      Uri requestUri = request.Headers.To;

      ProfilingObject pObject = new ProfilingObject();
      try
      {
        pObject.action = requestUri.PathAndQuery;
        pObject.timer = new AbcTimer();
        pObject.timer.Start();
        return pObject;
      }
      catch (Exception)
      {
        ProfilingObject pObjectEx = new ProfilingObject();
        pObjectEx.timer = new AbcTimer();
        pObjectEx.action = "Unknown";
        pObjectEx.timer.Start();
        return pObjectEx;
      }
    }

    public void BeforeSendReply(ref System.ServiceModel.Channels.Message reply, object correlationState)
    {
      ProfilingObject pObject = (ProfilingObject)correlationState;
      pObject.timer.Stop();
      double t = Utils.GetTime(pObject.timer.getElapsed(), _tUnit);
      _logger.write("RPC call '{0}'. Was running for '{1}' {2}", pObject.action, Math.Round(t), _tUnit.ToString());
    }

    #endregion


    #region IEndpointBehavior Members

    public void AddBindingParameters(ServiceEndpoint endpoint, System.ServiceModel.Channels.BindingParameterCollection bindingParameters)
    {
    }

    public void ApplyClientBehavior(ServiceEndpoint endpoint, ClientRuntime clientRuntime)
    {
    }

    public void ApplyDispatchBehavior(ServiceEndpoint endpoint, EndpointDispatcher endpointDispatcher)
    {
      endpointDispatcher.DispatchRuntime.MessageInspectors.Add(this); 
    }

    public void Validate(ServiceEndpoint endpoint)
    {
    }

    #endregion
  }
}
