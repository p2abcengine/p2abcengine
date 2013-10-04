using System;
using System.Collections.Generic;
using System.Linq;
using System.ServiceModel;
using System.ServiceModel.Description;
using System.ServiceModel.Web;
using System.Text;
using ABC4TrustSmartCard;
using UProveWCFServiceLib;

namespace UProveService
{
  class UProveThreadWorkerProver : IDisposable
  {
    private WebServiceHost _host;
    private ServiceEndpoint _serviceEndPoint;

    public UProveThreadWorkerProver()
    {
      WebHttpBinding binding = new WebHttpBinding();
      UProveRestServiceProver instance = UProveRestServiceProver.Instance;
      _host = new WebServiceHost(instance, new Uri(ParseConfigManager.GetAddress(), ParseConfigManager.GetProverApiPath()));
      _serviceEndPoint = _host.AddServiceEndpoint(typeof(IUProveRestServiceProver), binding, "");

      WebHttpBehavior enableHelp = new WebHttpBehavior();
      enableHelp.HelpEnabled = true;
      enableHelp.DefaultOutgoingResponseFormat = WebMessageFormat.Json;
      enableHelp.DefaultOutgoingRequestFormat = WebMessageFormat.Json;
      _serviceEndPoint.EndpointBehaviors.Add(enableHelp);

      if (ParseConfigManager.SetupTimeProfiles())
      {
        _serviceEndPoint.Behaviors.Add(new WcfTimeProfileEndPointLogger());
      }

    }


    public void Startup()
    {
      _host.Open();
    }


    #region IDisposable Members

    public void Dispose()
    {
      _host.Close();
    }

    #endregion
  }
}
